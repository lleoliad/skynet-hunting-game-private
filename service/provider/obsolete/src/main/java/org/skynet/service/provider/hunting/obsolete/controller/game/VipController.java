package org.skynet.service.provider.hunting.obsolete.controller.game;

import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.config.VipConfig;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.VipDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerVipData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
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

@Api(tags = "vip")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class VipController {

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private UserDataService userDataService;

    @PostMapping("/vip-claimVipDailyRewards")
    @ApiOperation("获得vip每日奖励")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> claimVipDailyRewards(@RequestBody VipDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            int diamondRewardCount = 0;
            //处理userData
            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());
            PlayerVipData vipData = userData.getVipData();
            boolean[] playerVipStatus = userDataService.getPlayerVipStatus(userData);
            Long standardTimeDay = TimeUtils.getStandardTimeDay();

            //是否可以获得每日奖励
            boolean canGetVipDailyReward = false;
            boolean canGetSvipDailyReward = false;
            if (request.getClaimVip() && playerVipStatus[0] && standardTimeDay - vipData.getLastClaimVipRewardsStandardDay() > 0) {

                canGetVipDailyReward = true;
                vipData.setLastClaimVipRewardsStandardDay(standardTimeDay);
            }
            if (request.getClaimSVip() && playerVipStatus[1] && standardTimeDay - vipData.getLastClaimSVipRewardsStandardDay() > 0) {

                canGetSvipDailyReward = true;
                vipData.setLastClaimSVipRewardsStandardDay(standardTimeDay);
            }

            //钻石每日奖励
            diamondRewardCount += canGetVipDailyReward && request.getClaimVip() ? VipConfig.vipEachDayDiamondRewardCount : 0;
            diamondRewardCount += canGetSvipDailyReward && request.getClaimSVip() ? VipConfig.svipEachDayDiamondRewardCount : 0;

            log.info("领取vip每日钻石: " + diamondRewardCount);
            userData.setDiamond(userData.getDiamond() + diamondRewardCount);
            userData.getHistory().setTotalEarnedDiamond(userData.getHistory().getTotalEarnedDiamond() + diamondRewardCount);

            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setVipData(vipData);
            sendToClientData.setHistory(userData.getHistory());
            userDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", sendToClientData);
            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }
}
