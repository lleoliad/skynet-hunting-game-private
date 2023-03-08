package org.skynet.service.provider.hunting.obsolete.controller.game;

import com.alibaba.fastjson.JSONObject;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.BaseDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.GiftPackageDataDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.service.UserDataService;
import org.skynet.service.provider.hunting.obsolete.service.impl.PromotionEventPackageDataServiceImpl;
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

@Api(tags = "礼包相关")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class GiftPackageDataController {

    @Resource
    private UserDataService userDataService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private PromotionEventPackageDataServiceImpl promotionEventPackageDataService;

    @PostMapping("giftPackage-refreshGiftPackageData")
    @ApiOperation("刷新用户礼包")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> refreshGiftPackageData(@RequestBody BaseDTO request) {

        try {
            GameEnvironment.timeMessage.computeIfAbsent("refreshGiftPackageData", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();

            log.info("[cmd] refreshGiftPackageData" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));

            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());
            if (userData == null) {
                userData = RedisDBOperation.selectUserData("User:" + request.getUserUid());
            }

            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();

            /*
             * 刷新礼包时，只添加新的礼包，不删除过期的礼包。因为删除过期的礼包极端情况下可能会导致服务器客户端不同步，
             * 例如正在内购某个快过期礼包的时候，礼包数据刷新了，服务器删除了该礼包，则会导致内购失败
             * 过期的礼包只在登陆的时候删除。*/
            //刷新子弹礼包
            promotionEventPackageDataService.refreshBulletGiftPackageNow(userData, true, request.getGameVersion());
            //刷新五日枪械礼包
            promotionEventPackageDataService.refreshFifthDayGunGiftPackageDataNow(userData, true, request.getGameVersion());
            //刷新枪械礼包
            promotionEventPackageDataService.refreshGunGiftPackageDataNow(userData, true, request.getGameVersion());

            userDataSendToClient.setAvailableBulletGiftPackageData(userData.getAvailableBulletGiftPackageData());
            userDataSendToClient.setAvailableGunGiftPackageData(userData.getAvailableGunGiftPackageData());
            userDataSendToClient.setAvailableFifthDayGunGiftPackageData(userData.getAvailableFifthDayGunGiftPackageData());
            userDataSendToClient.setHistory(userData.getHistory());
            userDataService.userDataSettlement(userData, userDataSendToClient, true, request.getGameVersion());


            Map<String, Object> map = CommonUtils.responsePrepare(null);

            map.put("userData", userDataSendToClient);

            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("refreshGiftPackageData").add(needTime);
            log.info("cmd giftPackage finish need time" + needTime);

            return map;

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }


    @PostMapping("giftPackage-refreshPromotionEventPackage")
    @ApiOperation("刷新活动礼包")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> refreshPromotionEventPackage(@RequestBody GiftPackageDataDTO request) {
        try {
            GameEnvironment.timeMessage.computeIfAbsent("refreshPromotionEventPackage", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();

            log.info("[cmd] refreshPromotionEventPackage" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));

            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());
            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();

            promotionEventPackageDataService.refreshPromotionEventPackageNow(userData, request.getGameVersion());
            promotionEventPackageDataService.refreshPromotionEventPackageV2Now(userData, request.getGameVersion());

            userDataSendToClient.setPromotionEventPackagesData(userData.getPromotionEventPackagesData());
            userDataSendToClient.setPromotionGiftPackagesV2Data(userData.getPromotionGiftPackagesV2Data());
            userDataSendToClient.setHistory(userData.getHistory());
            userDataService.userDataSettlement(userData, userDataSendToClient, true, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);

            map.put("userData", userDataSendToClient);

            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("refreshPromotionEventPackage").add(needTime);
            log.info("cmd giftPackage finish need time" + needTime);
            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }
}
