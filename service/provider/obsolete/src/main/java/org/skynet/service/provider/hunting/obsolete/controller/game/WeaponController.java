package org.skynet.service.provider.hunting.obsolete.controller.game;

import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.enums.BulletQuality;
import org.skynet.service.provider.hunting.obsolete.enums.GunQuality;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.EquipBulletDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.GunDTO;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.BulletTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.GunTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.GunUpgradeCountTableValue;
import org.skynet.service.provider.hunting.obsolete.service.ObsoleteUserDataService;
import org.skynet.service.provider.hunting.obsolete.service.WeaponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Map;

@Api(tags = "武器")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class WeaponController {

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private WeaponService weaponService;


    @PostMapping("/weapon-equipGun")
    @ApiOperation("装备某把枪")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> equipGun(@RequestBody GunDTO dto) {
        try {
            GameEnvironment.timeMessage.computeIfAbsent("equipGun", k -> new ArrayList<>());
            ThreadLocalUtil.set(dto.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] equipGun" + System.currentTimeMillis());
            CommonUtils.requestProcess(dto, null, systemPropertiesConfig.getSupportRecordModeClient());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
            UserData userData = null;

            //处理userData
            obsoleteUserDataService.checkUserDataExist(dto.getUserUid());
            userData = GameEnvironment.userDataMap.get(dto.getUserUid());

            if (userData.getEquippedGunId().equals(dto.getGunId())) {
                throw new BusinessException("用户" + userData.getUuid() + "想要装备的枪" + dto.getGunId() + "已经装备了");
            }

            if (!weaponService.isGunUnlocked(userData, dto.getGunId())) {
                throw new BusinessException("想要装备的枪" + dto.getGunId() + "还没有解锁");
            }
            userData.setEquippedGunId(dto.getGunId());
            sendToClientData.setEquippedGunId(userData.getEquippedGunId());
            sendToClientData.setHistory(userData.getHistory());
            log.info("切换枪械" + userData.getEquippedGunId());

            //处理返回结果
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, dto.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);

            map.put("userData", sendToClientData);
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("equipGun").add(needTime);
            log.info("[cmd] equipGun finish need time" + (System.currentTimeMillis() - startTime));
            return map;
        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }

    @PostMapping("weapon-upgradeGun")
    @ApiOperation("枪械升级")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> upgradeGun(@RequestBody GunDTO request) {
        try {
            GameEnvironment.timeMessage.computeIfAbsent("upgradeGun", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] upgradeGun" + System.currentTimeMillis());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
            UserData userData = null;

            //处理userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());

            userData = GameEnvironment.userDataMap.get(request.getUserUid());

            int gunLevelBeforeUpgrade = -1;

            if (!userData.getGunLevelMap().containsKey(request.getGunId())) {
                throw new BusinessException("升级枪械,用户" + userData.getUuid() + "没有解锁枪械" + request.getGunId());
            } else {
                gunLevelBeforeUpgrade = userData.getGunLevelMap().get(request.getGunId());
            }

            Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(request.getGameVersion());
            GunTableValue gunValue = null;

            if (!gunTable.containsKey(String.valueOf(request.getGunId()))) {
                throw new BusinessException("升级枪械,用户" + userData.getUuid() + "GunTable没有key" + request.getGunId());
            } else {
                gunValue = gunTable.get(String.valueOf(request.getGunId()));
            }

            if (gunLevelBeforeUpgrade >= gunValue.getMaxLevel()) {
                throw new BusinessException("升级枪械,用户" + userData.getUuid() + "枪械" + request.getGunId() + "已经达到了最大等级" + gunValue.getMaxLevel() + ",无法升级");
            }

            int gunQuality = gunValue.getQuality();
            GunQuality quality = GunQuality.values()[gunQuality - 1];

            int upgradeCountRequires = getGunUpgradeRequiresCount(quality, gunLevelBeforeUpgrade, request.getGameVersion());

            int gunCountBeforeUpgrade = userData.getGunCountMap().getOrDefault(request.getGunId(), 0);

            if (gunCountBeforeUpgrade < upgradeCountRequires) {

                throw new BusinessException("升级枪械,用户" + userData.getUuid() + "枪械" + request.getGunId() + "数量不足.有" + gunCountBeforeUpgrade + ",需要" + upgradeCountRequires);
            }

            int upgradeCoinRequires = gunValue.getUpgradeCoinRequires().get(gunLevelBeforeUpgrade - 1);

            if (userData.getCoin() < upgradeCoinRequires) {
                throw new BusinessException("升级枪械,用户" + userData.getUuid() + "枪械" + request.getGunId() + "升级金币数量不足.有" + userData.getCoin() + ",需要" + upgradeCoinRequires);
            }

            //升级
            long tempCoin = userData.getCoin() - upgradeCoinRequires;
            userData.setCoin(tempCoin);

            int gunLevel = userData.getGunLevelMap().get(request.getGunId()) + 1;
            userData.getGunLevelMap().put(request.getGunId(), gunLevel);

            int tempCount = userData.getGunCountMap().get(request.getGunId()) - upgradeCountRequires;
            userData.getGunCountMap().put(request.getGunId(), tempCount);

            sendToClientData.setCoin(userData.getCoin());
            sendToClientData.setGunLevelMap(userData.getGunLevelMap());
            sendToClientData.setGunCountMap(userData.getGunCountMap());
            sendToClientData.setHistory(userData.getHistory());

            log.info("升级枪械" + request.getGunId() + ",level from " + gunLevelBeforeUpgrade + "to" + userData.getGunLevelMap().get(request.getGunId())
                    + ".剩余金币" + userData.getCoin() + ",剩余数量" + userData.getGunCountMap().get(request.getGunId()));

            //处理返回结果
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", sendToClientData);
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("upgradeGun").add(needTime);
            log.info("[cmd] upgradeGun finish need time" + (System.currentTimeMillis() - startTime));
            return map;

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }

    @PostMapping("weapon-equipBullet")
    @ApiOperation("装备子弹")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> equipBullet(@RequestBody EquipBulletDTO request) {
        try {
            GameEnvironment.timeMessage.computeIfAbsent("equipBullet", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("cmd completeAchievement" + System.currentTimeMillis());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
            UserData userData = null;

            //处理userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            userData = GameEnvironment.userDataMap.get(request.getUserUid());

            Map<String, BulletTableValue> bulletTable = GameEnvironment.bulletTableMap.get(request.getGameVersion());
            BulletTableValue bulletValue = new BulletTableValue();
            if (!bulletTable.containsKey(String.valueOf(request.getBulletId()))) {

                throw new BusinessException("用户" + userData.getUuid() + "想要装备子弹 BulletTable没有找到内容 bullet id:" + request.getBulletId());

            } else {
                bulletValue = bulletTable.get(String.valueOf(request.getBulletId()));
            }

            int bulletCount = userData.getBulletCountMap().getOrDefault(request.getBulletId(), 0);
            if (!bulletValue.getQuality().equals(BulletQuality.White.getType()) && bulletCount == 0) {

                //装备子弹必须有数量才能装备,除非是白色品质(数量无限)
                return CommonUtils.responsePrepare(null);
            }

            userData.setEquippedBulletId(request.getBulletId());
            sendToClientData.setEquippedBulletId(userData.getEquippedBulletId());
            log.info("装备子弹" + request.getBulletId());

            //处理返回结果
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", sendToClientData);
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("equipBullet").add(needTime);
            log.info("[cmd] equipBullet finish need time" + (System.currentTimeMillis() - startTime));
            return map;

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }


    /**
     * 获取枪械升级需要的数量
     *
     * @param gunQuality
     * @param gunLevel   是枪械当前等级
     * @return
     */
    private int getGunUpgradeRequiresCount(GunQuality gunQuality, Integer gunLevel, String gameVersion) {

        Map<String, GunUpgradeCountTableValue> gunUpgradeCountTable = GameEnvironment.gunUpgradeCountTableMap.get(gameVersion);
        GunUpgradeCountTableValue tableValue = gunUpgradeCountTable.get(String.valueOf(gunLevel));
        switch (gunQuality) {

            case White:
                throw new BusinessException("白色枪械不能升级，无法获取升级需要数量");

            case Blue:
                return tableValue.getBlueQuality();

            case Green:
                return tableValue.getGreenQuality();

            case Red:
                return tableValue.getRedQuality();

            case Orange:
                return tableValue.getOrangeQuality();

        }
        return -1;
    }

}
