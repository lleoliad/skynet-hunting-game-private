package org.skynet.service.provider.hunting.obsolete.controller.game;

import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.LuckyWheelDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.LuckyWheelData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.LuckyWheelSpinReward;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.LuckyWheelPropertyTable;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Api(tags = "幸运转盘")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class LuckyWheelController {


    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private UserDataService userDataService;

    @Resource
    private LuckyWheelService luckyWheelService;


    @PostMapping("luckyWheel-luckyWheelSpin")
    @ApiOperation("转动一次幸运转盘")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> luckyWheelSpin(@RequestBody LuckyWheelDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
//            userDataService.ensureUserDataIdempotence(request.getUserUid(),request.getUserDataUpdateCount(),request.getGameVersion());

            List<LuckyWheelSpinReward> spinRewards = new ArrayList<>();
            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
            Integer cumulativeDiamondRewardCount = null;


            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            if (Objects.isNull(userData.getLuckyWheelData())) {
                throw new BusinessException("玩家没有luckyWheelData数据");
            }
            LuckyWheelPropertyTable luckyWheelProperty = GameEnvironment.luckyWheelPropertyTableMap.get(request.getGameVersion());

            luckyWheelService.refreshLuckyWheelV1FreeSpinCount(request.getUserUid(), request.getGameVersion());

            long serverStandardDay = TimeUtils.getStandardTimeDay();
            long clientStandardDay = TimeUtils.convertUnixTimeSecondToStandardDay(request.getClientTime());
            log.info("准备转盘刷新逻辑,server standard day:" + serverStandardDay + ",client standard day:" + clientStandardDay);

            LuckyWheelData luckyWheelData = userData.getLuckyWheelData();
            //客户端与服务器在同一天，先刷新服务器上的转盘内容
            if (clientStandardDay == serverStandardDay) {
                luckyWheelService.tryRefreshLuckyWheelContents(request.getUserUid());
            }
            int spinCount = 0;

            if (!request.getIsVipSpin()) {

                //免费转盘
                if (request.getSpinByWatchRewardAd()) {

                    if (userData.getAdvertisementData().getRemainedRewardAdCountToday() <= 0) {
                        throw new BusinessException("玩家今日已没有剩余的激励广告次数了");
                    }

                    Integer currentRemainedRewardAdCount = userData.getAdvertisementData().getRemainedRewardAdCountToday();
                    userData.getAdvertisementData().setRemainedRewardAdCountToday(--currentRemainedRewardAdCount);

                } else {
                    if (luckyWheelData.getFreeSpinCount() <= 0) {
                        throw new BusinessException("当前免费转盘次数不足");
                    }
                    luckyWheelData.setFreeSpinCount(luckyWheelData.getFreeSpinCount() - 1);
                    luckyWheelService.refreshNextFreeSpinTime(request.getUserUid(), request.getGameVersion());
                }

                LuckyWheelSpinReward singleSpinReward = luckyWheelService.spinLuckyWheelOnceReward(request.getUserUid(), request.getGameVersion());
                spinRewards.add(singleSpinReward);
                spinCount = 1;
            } else {
                //vip转盘
                if (luckyWheelData.getVipSpinCount() <= 0) {
                    throw new BusinessException("玩家今日vip转盘次数已经用完");
                }

                //一次性加完
                for (Integer i = 0; i < luckyWheelData.getVipSpinCount(); i++) {

                    LuckyWheelSpinReward singleSpinReward = luckyWheelService.spinLuckyWheelOnceReward(request.getUserUid(), request.getGameVersion());
                    spinRewards.add(singleSpinReward);
                }

                spinCount = luckyWheelData.getVipSpinCount();
                luckyWheelData.setVipSpinCount(0);
            }

            //计算累计奖励
            Integer cumulativeRewardSpinCountStart = luckyWheelData.getCumulativeRewardSpinCount();
            luckyWheelData.setCumulativeRewardSpinCount(luckyWheelData.getCumulativeRewardSpinCount() + spinCount);
            Integer cumulativeRewardSpinCountEnd = luckyWheelData.getCumulativeRewardSpinCount();

            if (luckyWheelProperty.getCumulativeRewardSpinCount().size() !=
                    luckyWheelProperty.getCumulativeRewardDiamondCount().size()
            ) {
                throw new BusinessException("LuckyWheelProperty表中cumulativeRewardSpinCountArray数组长度 != cumulativeRewardDiamondCountArray数组长度");
            }

            List<Integer> luckyWheelCumulativeRewardSpinCountArray = luckyWheelProperty.getCumulativeRewardSpinCount();
            for (int i = 0; i < luckyWheelCumulativeRewardSpinCountArray.size(); i++) {

                Integer rewardRequiresCumulativeCount = luckyWheelCumulativeRewardSpinCountArray.get(i);
                Integer diamondReward = luckyWheelProperty.getCumulativeRewardDiamondCount().get(i);

                //vip转盘，可能一次获得多次累计奖励
                if (rewardRequiresCumulativeCount >= cumulativeRewardSpinCountStart && rewardRequiresCumulativeCount <= cumulativeRewardSpinCountEnd) {

                    if (cumulativeDiamondRewardCount == null) {
                        cumulativeDiamondRewardCount = 0;
                    }

                    cumulativeDiamondRewardCount += diamondReward;

                    log.info("转盘累计奖励钻石:" + cumulativeDiamondRewardCount + "at" + rewardRequiresCumulativeCount);
                }

                if (serverStandardDay != clientStandardDay) {
                    log.info("服务器标准日" + serverStandardDay + "!= 客户端 标准日" + clientStandardDay + ",延后刷新转盘内容");
                    luckyWheelService.tryRefreshLuckyWheelContents(request.getUserUid());
                }
                userDataSendToClient.setCoin(userData.getCoin());
                userDataSendToClient.setLuckyWheelData(luckyWheelData);
                if (cumulativeDiamondRewardCount != null) {
                    userData.setDiamond(userData.getDiamond() + cumulativeDiamondRewardCount);
                    userDataSendToClient.setDiamond(userData.getDiamond());
                }
            }
            userDataSendToClient.setHistory(userData.getHistory());
            userDataService.userDataSettlement(userData, userDataSendToClient, true, request.getGameVersion());

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", userDataSendToClient);
            map.put("spinRewards", spinRewards);

            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());

        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }
}
