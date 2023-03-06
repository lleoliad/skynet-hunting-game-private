package org.skynet.service.provider.hunting.obsolete.controller.game;

import com.alibaba.fastjson.JSONObject;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.GameConfig;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.MailDataDTO;
import com.cn.huntingrivalserver.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
import org.skynet.service.provider.hunting.obsolete.service.UserDataService;
import org.skynet.service.provider.hunting.obsolete.service.WeaponService;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api(tags = "邮件")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class MailController {

    @Resource
    private UserDataService userDataService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private ChestService chestService;

    @Resource
    private WeaponService weaponService;


    @PostMapping("mail-claimPlayerMailItems")
    @ApiOperation("获取邮件附加的物品")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> claimPlayerMailItems(@RequestBody MailDataDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            MailData mailData = RedisDBOperation.selectInboxMail(request.getMailUid());
            log.info("领取邮件" + JSONObject.toJSONString(mailData));

            List<Integer> unlockNewGunIds = new ArrayList<>();
            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();

            //处理userData
            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            switch (mailData.getAttachmentType()) {

                case None:
                    break;
                case Coin:
                    Integer coinCount = mailData.getAttachmentCount();
                    log.info("领取金币" + coinCount);
                    userData.setCoin(userData.getCoin() + coinCount);
                    userDataSendToClient.setCoin(userData.getCoin());
                    break;
                case Diamond:
                    Integer diamondCount = mailData.getAttachmentCount();
                    log.info("领取钻石" + diamondCount);
                    userData.setDiamond(userData.getDiamond() + diamondCount);
                    userDataSendToClient.setDiamond(userData.getDiamond());
                    break;
                case Chest:
                    MailChestContent chestContent = mailData.getChestContent();
                    log.info("领取宝箱" + JSONObject.toJSONString(chestContent));
                    chestService.openMailChest(request.getUserUid(), chestContent, unlockNewGunIds, request.getGameVersion());
                    if (chestContent.getGunRewards() != null) {
                        userDataSendToClient.setGunLevelMap(userData.getGunLevelMap());
                        userDataSendToClient.setGunCountMap(userData.getGunCountMap());
                    }
                    if (chestContent.getBulletRewards() != null) {
                        userDataSendToClient.setBulletCountMap(userData.getBulletCountMap());
                    }
                    break;
                case Gun:
                    Integer gunCount = mailData.getAttachmentCount();
                    Integer gunId = mailData.getAttachmentId();
                    log.info("领取gun, gun id" + gunId + ", count:" + gunCount);

                    List<GunReward> gunIdCountData = new ArrayList<>();
                    gunIdCountData.add(new GunReward(gunId, gunCount));
//                    Map<Integer, Integer> gunCountMap = Maps.newHashMap();
//                    gunCountMap.put(gunId, gunCount);
                    userDataService.addGunToUserDataByGunIdCountData(userData, gunIdCountData, unlockNewGunIds, request.getGameVersion());

                    userDataSendToClient.setGunLevelMap(userData.getGunLevelMap());
                    userDataSendToClient.setGunCountMap(userData.getGunCountMap());
                    break;
                case Bullet:
                    int bulletCount = mailData.getAttachmentCount();
                    int bulletId = mailData.getAttachmentId();
                    log.info("领取bullet, bullet id: " + bulletId + ", count" + bulletCount);

                    Map<Integer, Integer> bulletCountMap = Maps.newHashMap();
                    bulletCountMap.put(bulletId, bulletCount);
                    userDataService.addBulletToUserData(userData, bulletCountMap);

                    userDataSendToClient.setBulletCountMap(userData.getBulletCountMap());
                    break;
            }

            userDataSendToClient.setHistory(userData.getHistory());
            userDataService.userDataSettlement(userData, userDataSendToClient, true, request.getGameVersion());

            //删除该邮件
            RedisDBOperation.deleteMail(request.getUserUid(), mailData.getUid());
            //存档
            ArchivedMailData archivedMailData = new ArchivedMailData(mailData, TimeUtils.getUnixTimeSecond());
            RedisDBOperation.insertArchivedMailData(mailData.getUid(), archivedMailData);

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", userDataSendToClient);
            if (unlockNewGunIds.size() > 0) {
                map.put("unlockNewGunIds", unlockNewGunIds);
            }

            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }

    @PostMapping("mail-deletePlayerMail")
    @ApiOperation("删除玩家邮件")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> deletePlayerMail(@RequestBody MailDataDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            RedisDBOperation.deleteMail(request.getUserUid(), request.getMailUid());
            return CommonUtils.responsePrepare(null);

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }


    @PostMapping("mail-pullAllInboxMails")
    @ApiOperation("获取玩家邮件")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> pullAllInboxMails(@RequestBody MailDataDTO request) {

        try {
            log.info("要拉取邮件的玩家id：{}", request.getUserUid());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());

            //一次获取邮件个数
            int fetchMailOnceCount = 10;

            if (request.getCursorReceiveTime() == null) {
                request.setCursorReceiveTime(0L);
            }

            //这里直接按收取邮件的时间，数量作为判断条件返回
            List<MailData> resultMails = userDataService.getAllInboxMails(request.getUserUid(), fetchMailOnceCount, request.getCursorReceiveTime(), request.getCursorMailUid());
            log.info("邮件拉取结果：{}", resultMails);
            boolean maybeMore = resultMails.size() >= fetchMailOnceCount;

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("mails", resultMails);
            map.put("maybeMore", maybeMore);
            map.put("code", 0);
            map.put("serverTime", TimeUtils.getUnixTimeSecond());

            return map;

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;

    }

    @PostMapping("mail-pullLatestInboxMails")
    @ApiOperation("拉取最新的邮件")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> pullLatestInboxMails(@RequestBody MailDataDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());

            List<MailData> resultMails = new ArrayList<>();
            //如果客户端没有上报最后一封邮件的信息，说明进入游戏时收件箱是空的，这里直接返回所有邮件。
            if (request.getClientLatestMailReceiveTime() == null || request.getClientLatestMailUid() == null) {

                resultMails = userDataService.getAllInboxMails(request.getUserUid(), GameConfig.mailInboxCapacity, null, null);
            } else {

                resultMails = userDataService.getAllInboxMails(request.getUserUid(), GameConfig.mailInboxCapacity, request.getClientLatestMailReceiveTime(), request.getClientLatestMailUid());
            }

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("mails", resultMails);
            map.put("code", 0);
            map.put("serverTime", TimeUtils.getUnixTimeSecond());

            return map;

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }
}
