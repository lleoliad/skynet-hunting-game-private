package org.skynet.service.provider.hunting.obsolete.controller.game;

import com.alibaba.fastjson.JSONObject;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.rank.league.query.GetRankAdditionQuery;
import org.skynet.components.hunting.rank.league.service.RankLeagueFeignService;
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
import org.skynet.components.hunting.user.domain.PlayerVipV2Data;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
import org.skynet.service.provider.hunting.obsolete.service.ObsoleteUserDataService;
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
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private ChestService chestService;

    @Resource
    private RankLeagueFeignService rankLeagueFeignService;

    @PostMapping("/vipV2-claimSVipV2DailyRewards")
    @ApiOperation("??????svip v2????????????")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> claimSVipV2DailyRewards(@RequestBody VipDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            int diamondRewardCount = 0;
            //??????userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            Result<Float> rankAddition = rankLeagueFeignService.getRankAddition(GetRankAdditionQuery.builder().userId(request.getUserUid()).build());
            float additionValue = rankAddition.getData();


            PlayerVipV2Data vipV2Data = userData.getVipV2Data();
            boolean[] playerVipV2Status = obsoleteUserDataService.getPlayerVipV2Status(userData);
            Long standardTimeDay = TimeUtils.getStandardTimeDay();

            boolean isSVipV2 = playerVipV2Status[1];
            if (!isSVipV2) {
                throw new BusinessException("????????????svip v2");
            }

            // ??????????????????????????????
            if (vipV2Data.getLastClaimSVipRewardsStandardDay().longValue() == standardTimeDay) {
                throw new BusinessException("?????????????????????svip v2?????????");
            }

            vipV2Data.setLastClaimSVipRewardsStandardDay(standardTimeDay);

            //??????????????????
            diamondRewardCount += VipV2Config.svipDailyDiamondRewardCount;

            //??????????????????
            Map<Integer, Integer> dailyBulletRewardCountMap = new HashMap<Integer, Integer>() {{
                put(VipV2Config.svipDailyRewardBulletId, VipV2Config.svipDailyRewardBulletCount);
            }};
            obsoleteUserDataService.addBulletToUserDataByIdCountData(userData, new ArrayList<BulletReward>() {
                {
                    add(new BulletReward(VipV2Config.svipDailyRewardBulletId, VipV2Config.svipDailyRewardBulletCount));
                }
            });

            // ?????????
            Integer playerHighestUnlockedChapterID = obsoleteUserDataService.playerHighestUnlockedChapterID(userData);
            Map<Integer, Integer> gunRewards = chestService.extractGunRewardsFromGunLibrary(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, VipV2Config.svipDailyRewardOrangeGunCount, request.getGameVersion(), false, null, additionValue);

            List<Integer> newUnlockedGunIDs = new ArrayList<>();
            obsoleteUserDataService.addGunToUserData(userData, gunRewards, newUnlockedGunIDs, request.getGameVersion());

            log.info("??????vip????????????: " + diamondRewardCount + ",????????????: " + JSONObject.toJSONString(dailyBulletRewardCountMap) + ",????????????" + JSONObject.toJSONString(gunRewards));

            userData.setDiamond(userData.getDiamond() + diamondRewardCount);
            userData.getHistory().setTotalEarnedDiamond(userData.getHistory().getTotalEarnedDiamond() + diamondRewardCount);

            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
            sendToClientData.setGunCountMap(userData.getGunCountMap());
            sendToClientData.setGunLevelMap(userData.getGunLevelMap());
            sendToClientData.setVipV2Data(vipV2Data);
            sendToClientData.setHistory(userData.getHistory());
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());

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
    @ApiOperation("??????vip v2????????????")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> claimVipV2DailyRewards(@RequestBody VipDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            int diamondRewardCount = 0;
            //??????userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());
            PlayerVipV2Data vipV2Data = userData.getVipV2Data();
            boolean[] playerVipV2Status = obsoleteUserDataService.getPlayerVipV2Status(userData);
            Long standardTimeDay = TimeUtils.getStandardTimeDay();

            boolean isSVipV2 = playerVipV2Status[0];
            if (!isSVipV2) {
                throw new BusinessException("????????????vip v2");
            }

            //??????????????????????????????
            if (vipV2Data.getLastClaimVipRewardsStandardDay().longValue() == standardTimeDay) {
                throw new BusinessException("?????????????????????vip v2?????????");
            }

            vipV2Data.setLastClaimVipRewardsStandardDay(standardTimeDay);

            //??????????????????
            diamondRewardCount += VipV2Config.vipDailyDiamondRewardCount;

            //??????????????????
            Map<Integer, Integer> dailyBulletRewardCountMap = new HashMap<Integer, Integer>() {{
                put(VipV2Config.vipDailyRewardBulletId, VipV2Config.vipDailyRewardBulletCount);
            }};
            obsoleteUserDataService.addBulletToUserDataByIdCountData(userData, new ArrayList<BulletReward>() {
                {
                    add(new BulletReward(VipV2Config.vipDailyRewardBulletId, VipV2Config.vipDailyRewardBulletCount));
                }
            });

            log.info("??????vip????????????: " + diamondRewardCount + ",????????????: " + JSONObject.toJSONString(dailyBulletRewardCountMap));

            userData.setDiamond(userData.getDiamond() + diamondRewardCount);
            userData.getHistory().setTotalEarnedDiamond(userData.getHistory().getTotalEarnedDiamond() + diamondRewardCount);

            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
            sendToClientData.setVipV2Data(vipV2Data);
            sendToClientData.setHistory(userData.getHistory());
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());

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
