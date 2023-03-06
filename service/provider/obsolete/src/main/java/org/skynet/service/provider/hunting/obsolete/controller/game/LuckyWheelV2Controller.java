package org.skynet.service.provider.hunting.obsolete.controller.game;

import com.alibaba.fastjson.JSON;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.LuckyWheelV2SpinRewardBO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.LuckyWheelDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.LuckyWheelV2Data;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.LuckyWheelV2PropertyTableValue;
import org.skynet.service.provider.hunting.obsolete.service.LuckyWheelService;
import org.skynet.service.provider.hunting.obsolete.service.UserDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Api(tags = "幸运转盘")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class LuckyWheelV2Controller {


    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private UserDataService userDataService;

    @Resource
    private LuckyWheelService luckyWheelService;


    @PostMapping("luckyWheelV2-luckyWheelV2RefreshContent")
    @ApiOperation("刷新转盘v2表盘内容")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> luckyWheelV2RefreshContent(@RequestBody LuckyWheelDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();

            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            luckyWheelService.refreshLuckyWheelV2Content(request.getUserUid(), request.getGameVersion());

            if (userData.getLuckyWheelV2Data().getNextFreeSpinUnixTime() <= 0) {
                userData.getLuckyWheelV2Data().setNextFreeSpinUnixTime(TimeUtils.getUnixTimeSecond() + 1000);
            }
            userDataService.userDataSettlement(userData, userDataSendToClient, true, request.getGameVersion());

//            if (userData.getLuckyWheelV2Data().getFreeSpinCount() <= 0){
//                userData.getLuckyWheelV2Data().setNextFreeSpinUnixTime(TimeUtils.getUnixTimeSecond() + 1000);
//            }


            userDataSendToClient.setHistory(userData.getHistory());
            userDataSendToClient.setLuckyWheelV2Data(userData.getLuckyWheelV2Data());

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", userDataSendToClient);

            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());

        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }

    @PostMapping("luckyWheelV2-luckyWheelV2Spin")
    @ApiOperation("获取转盘V2奖励")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> luckyWheelV2Spin(@RequestBody LuckyWheelDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            LuckyWheelV2SpinRewardBO spinReward = null;

            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();

            userDataService.checkUserDataExist(request.getUserUid());

            LuckyWheelV2PropertyTableValue luckyWheelV2PropertyTable = GameEnvironment.luckyWheelV2PropertyTableMap.get(request.getGameVersion());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());
            LuckyWheelV2Data luckyWheelV2Data = userData.getLuckyWheelV2Data();
            Long serverStandardDay = TimeUtils.getStandardTimeDay();

            luckyWheelService.refreshFreeSpinCount(request.getUserUid(), request.getGameVersion());

            long clientStandardDay = TimeUtils.convertUnixTimeSecondToStandardDay(request.getClientTime());
            //如果客户端不是服务器同一天，且今天还没有刷新转盘的内容
            if (clientStandardDay != serverStandardDay && luckyWheelV2Data.getLastRefreshLuckyWheelStandardDay() < serverStandardDay) {
                //客户端每次转钟，必须主动向服务器请求刷新
                //如果在一定时间内容，允许不刷新表盘，否则抛错让客户端下线
                if (TimeUtils.getSecondsFromNowToTargetStandardDay(serverStandardDay) < -60) {
                    throw new BusinessException("客户端今天还没有刷新转盘内容");
                }
            }

            //广告转盘
            if (request.getSpinByWatchRewardAd()) {
                if (userData.getAdvertisementData().getRemainedRewardAdCountToday() <= 0) {
                    throw new BusinessException("玩家今日已没有剩余的激励广告次数了");
                }

                userData.getAdvertisementData().setRemainedRewardAdCountToday(userData.getAdvertisementData().getRemainedRewardAdCountToday() - 1);
                userDataSendToClient.setAdvertisementData(userData.getAdvertisementData());
            } else {
                if (luckyWheelV2Data.getFreeSpinCount() <= 0) {
                    throw new BusinessException("当前免费转盘次数不足");
                }
                luckyWheelV2Data.setFreeSpinCount(luckyWheelV2Data.getFreeSpinCount() - 1);
                if (luckyWheelV2Data.getFreeSpinCount() <= 0) {
                    luckyWheelV2Data.setNextFreeSpinUnixTime(TimeUtils.getUnixTimeSecond());
                }
                luckyWheelService.refreshNextFreeSpinTime(request.getUserUid(), request.getGameVersion());


            }

            spinReward = luckyWheelService.spinLuckyWheelV2Reward(request.getUserUid(), request.getGameVersion());

            userDataSendToClient.setLuckyWheelV2Data(luckyWheelV2Data);
            userDataSendToClient.setCoin(userData.getCoin());
            userDataSendToClient.setGunCountMap(userData.getGunCountMap());
            userDataSendToClient.setGunLevelMap(userData.getGunLevelMap());
            userDataSendToClient.setBulletCountMap(userData.getBulletCountMap());
            userDataSendToClient.setDiamond(userData.getDiamond());
            userDataSendToClient.setBulletCountMap(userData.getBulletCountMap());
            userDataSendToClient.setHistory(userData.getHistory());


            userDataService.userDataSettlement(userData, userDataSendToClient, true, request.getGameVersion());

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", userDataSendToClient);
            map.put("spinReward", spinReward);
            log.info("luckyWheelV2Spin====" + JSON.toJSONString(map));

            return map;
        } catch (Exception e) {
            e.printStackTrace();
            CommonUtils.responseException(request, e, request.getUserUid());

        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }
}
