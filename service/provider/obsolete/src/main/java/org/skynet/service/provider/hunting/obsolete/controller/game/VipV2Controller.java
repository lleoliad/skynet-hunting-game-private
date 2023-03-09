package org.skynet.service.provider.hunting.obsolete.controller.game;

import com.alibaba.fastjson.JSONObject;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.config.VipV2Config;
import org.skynet.service.provider.hunting.obsolete.enums.GunLibraryType;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.VipDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.BulletReward;
import org.skynet.commons.hunting.user.domain.PlayerVipV2Data;
import org.skynet.commons.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "vipV2")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class VipV2Controller {

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private UserDataService userDataService;

    @Resource
    private ChestService chestService;

    @PostMapping("/vipV2-claimSVipV2DailyRewards")
    @ApiOperation("获取svip v2每日奖励")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> claimSVipV2DailyRewards(@RequestBody VipDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            int diamondRewardCount = 0;
            //处理userData
            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());
            PlayerVipV2Data vipV2Data = userData.getVipV2Data();
            boolean[] playerVipV2Status = userDataService.getPlayerVipV2Status(userData);
            Long standardTimeDay = TimeUtils.getStandardTimeDay();

            boolean isSVipV2 = playerVipV2Status[1];
            if (!isSVipV2) {
                throw new BusinessException("当前不是svip v2");
            }

            // 是否可以获得每日奖励
            if (vipV2Data.getLastClaimSVipRewardsStandardDay().longValue() == standardTimeDay) {
                throw new BusinessException("今天已经领取过svip v2奖励了");
            }

            vipV2Data.setLastClaimSVipRewardsStandardDay(standardTimeDay);

            //钻石每日奖励
            diamondRewardCount += VipV2Config.svipDailyDiamondRewardCount;

            //子弹每日奖励
            Map<Integer, Integer> dailyBulletRewardCountMap = new HashMap<Integer, Integer>() {{
                put(VipV2Config.svipDailyRewardBulletId, VipV2Config.svipDailyRewardBulletCount);
            }};
            userDataService.addBulletToUserDataByIdCountData(userData, new ArrayList<BulletReward>() {
                {
                    add(new BulletReward(VipV2Config.svipDailyRewardBulletId, VipV2Config.svipDailyRewardBulletCount));
                }
            });

            // 橙卡枪
            Integer playerHighestUnlockedChapterID = userDataService.playerHighestUnlockedChapterID(userData);
            Map<Integer, Integer> gunRewards = chestService.extractGunRewardsFromGunLibrary(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, VipV2Config.svipDailyRewardOrangeGunCount, request.getGameVersion(), false, null);

            List<Integer> newUnlockedGunIDs = new ArrayList<>();
            userDataService.addGunToUserData(userData, gunRewards, newUnlockedGunIDs, request.getGameVersion());

            log.info("领取vip每日钻石: " + diamondRewardCount + ",领取子弹: " + JSONObject.toJSONString(dailyBulletRewardCountMap) + ",橙卡枪：" + JSONObject.toJSONString(gunRewards));

            userData.setDiamond(userData.getDiamond() + diamondRewardCount);
            userData.getHistory().setTotalEarnedDiamond(userData.getHistory().getTotalEarnedDiamond() + diamondRewardCount);

            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
            sendToClientData.setGunCountMap(userData.getGunCountMap());
            sendToClientData.setGunLevelMap(userData.getGunLevelMap());
            sendToClientData.setVipV2Data(vipV2Data);
            sendToClientData.setHistory(userData.getHistory());
            userDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());

            List<Map<String, Integer>> rewardGunData = new ArrayList<>();
            // for (Integer gunId : gunRewards.keySet()) {
            for (Map.Entry<Integer, Integer> entry : gunRewards.entrySet()) {
                // Map<String, Integer> map = new HashMap<>();
                // map.put("gunId", gunId);
                // map.put("count", VipV2Config.svipDailyRewardOrangeGunCount);
                Integer gunId = entry.getKey();
                Integer count = entry.getValue();
                Map<String, Integer> map = new HashMap<>();
                map.put("gunId", gunId);
                map.put("count", count);
                rewardGunData.add(map);
            }

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", sendToClientData);
            map.put("rewardGunData", rewardGunData);
            map.put("newUnlockGunIds", newUnlockedGunIDs);
            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }

    @PostMapping("/vipV2-claimVipV2DailyRewards")
    @ApiOperation("获取vip v2每日奖励")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> claimVipV2DailyRewards(@RequestBody VipDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            int diamondRewardCount = 0;
            //处理userData
            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());
            PlayerVipV2Data vipV2Data = userData.getVipV2Data();
            boolean[] playerVipV2Status = userDataService.getPlayerVipV2Status(userData);
            Long standardTimeDay = TimeUtils.getStandardTimeDay();

            boolean isSVipV2 = playerVipV2Status[0];
            if (!isSVipV2) {
                throw new BusinessException("当前不是vip v2");
            }

            //是否可以获得每日奖励
            if (vipV2Data.getLastClaimVipRewardsStandardDay().longValue() == standardTimeDay) {
                throw new BusinessException("今天已经领取过vip v2奖励了");
            }

            vipV2Data.setLastClaimVipRewardsStandardDay(standardTimeDay);

            //钻石每日奖励
            diamondRewardCount += VipV2Config.vipDailyDiamondRewardCount;

            //子弹每日奖励
            Map<Integer, Integer> dailyBulletRewardCountMap = new HashMap<Integer, Integer>() {{
                put(VipV2Config.vipDailyRewardBulletId, VipV2Config.vipDailyRewardBulletCount);
            }};
            userDataService.addBulletToUserDataByIdCountData(userData, new ArrayList<BulletReward>() {
                {
                    add(new BulletReward(VipV2Config.vipDailyRewardBulletId, VipV2Config.vipDailyRewardBulletCount));
                }
            });

            log.info("领取vip每日钻石: " + diamondRewardCount + ",领取子弹: " + JSONObject.toJSONString(dailyBulletRewardCountMap));

            userData.setDiamond(userData.getDiamond() + diamondRewardCount);
            userData.getHistory().setTotalEarnedDiamond(userData.getHistory().getTotalEarnedDiamond() + diamondRewardCount);

            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
            sendToClientData.setVipV2Data(vipV2Data);
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
