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
import org.skynet.service.provider.hunting.obsolete.config.VipV3Config;
import org.skynet.service.provider.hunting.obsolete.enums.GunLibraryType;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.VipDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.BulletReward;
import org.skynet.components.hunting.user.domain.PlayerVipV3Data;
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

@Api(tags = "vipV3")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class VipV3Controller {

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private ChestService chestService;

    @Resource
    private RankLeagueFeignService rankLeagueFeignService;

    @PostMapping("/vipV3-claimSVipV3DailyRewards")
    @ApiOperation("??????svip v3????????????")
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
            PlayerVipV3Data vipV3Data = userData.getVipV3Data();
            boolean[] playerVipV3Status = obsoleteUserDataService.getPlayerVipV3Status(userData);
            Long standardTimeDay = TimeUtils.getStandardTimeDay();

            boolean isSVipV3 = playerVipV3Status[1];
            if (!isSVipV3) {
                throw new BusinessException("????????????svip v3");
            }

            // ??????????????????????????????
            if (vipV3Data.getLastClaimSVipRewardsStandardDay().longValue() == standardTimeDay) {
                throw new BusinessException("?????????????????????svip v3?????????");
            }

            vipV3Data.setLastClaimSVipRewardsStandardDay(standardTimeDay);

            //??????????????????
            diamondRewardCount += VipV3Config.svipDailyDiamondRewardCount;

            //??????????????????
            Map<Integer, Integer> dailyBulletRewardCountMap = new HashMap<Integer, Integer>() {{
                put(VipV3Config.svipDailyRewardBulletId, VipV3Config.svipDailyRewardBulletCount);
            }};
            obsoleteUserDataService.addBulletToUserDataByIdCountData(userData, new ArrayList<BulletReward>() {
                {
                    add(new BulletReward(VipV3Config.svipDailyRewardBulletId, VipV3Config.svipDailyRewardBulletCount));
                }
            });

            Result<Float> rankAddition = rankLeagueFeignService.getRankAddition(GetRankAdditionQuery.builder().userId(request.getUserUid()).build());
            float additionValue = rankAddition.getData();

            // ?????????
            Integer playerHighestUnlockedChapterID = obsoleteUserDataService.playerHighestUnlockedChapterID(userData);
            Map<Integer, Integer> gunRewards = chestService.extractGunRewardsFromGunLibrary(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, VipV3Config.svipDailyRewardOrangeGunCount, request.getGameVersion(), false, null, additionValue);

            List<Integer> newUnlockedGunIDs = new ArrayList<>();
            obsoleteUserDataService.addGunToUserData(userData, gunRewards, newUnlockedGunIDs, request.getGameVersion());

            log.info("??????vip????????????: " + diamondRewardCount + ",????????????: " + JSONObject.toJSONString(dailyBulletRewardCountMap) + ",????????????" + JSONObject.toJSONString(gunRewards));

            userData.setDiamond(userData.getDiamond() + diamondRewardCount);
            userData.getHistory().setTotalEarnedDiamond(userData.getHistory().getTotalEarnedDiamond() + diamondRewardCount);

            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
            sendToClientData.setGunCountMap(userData.getGunCountMap());
            sendToClientData.setGunLevelMap(userData.getGunLevelMap());
            sendToClientData.setVipV3Data(vipV3Data);
            sendToClientData.setHistory(userData.getHistory());
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());

            List<Map<String, Integer>> rewardGunData = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : gunRewards.entrySet()) {
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

    @PostMapping("/vipV3-claimVipV3DailyRewards")
    @ApiOperation("??????vip v3????????????")
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
            PlayerVipV3Data vipV3Data = userData.getVipV3Data();
            boolean[] playerVipV3Status = obsoleteUserDataService.getPlayerVipV3Status(userData);
            Long standardTimeDay = TimeUtils.getStandardTimeDay();

            boolean isVipV3 = playerVipV3Status[0];
            if (!isVipV3) {
                throw new BusinessException("????????????vip v3");
            }

            //??????????????????????????????
            if (vipV3Data.getLastClaimVipRewardsStandardDay().longValue() == standardTimeDay) {
                throw new BusinessException("?????????????????????vip v3?????????");
            }

            vipV3Data.setLastClaimVipRewardsStandardDay(standardTimeDay);

            //??????????????????
            diamondRewardCount += VipV3Config.vipDailyDiamondRewardCount;

            //??????????????????
            Map<Integer, Integer> dailyBulletRewardCountMap = new HashMap<Integer, Integer>() {{
                put(VipV3Config.vipDailyRewardBulletId, VipV3Config.vipDailyRewardBulletCount);
            }};
            obsoleteUserDataService.addBulletToUserDataByIdCountData(userData, new ArrayList<BulletReward>() {
                {
                    add(new BulletReward(VipV3Config.vipDailyRewardBulletId, VipV3Config.vipDailyRewardBulletCount));
                }
            });

            log.info("??????vip????????????: " + diamondRewardCount + ",????????????: " + JSONObject.toJSONString(dailyBulletRewardCountMap));

            userData.setDiamond(userData.getDiamond() + diamondRewardCount);
            userData.getHistory().setTotalEarnedDiamond(userData.getHistory().getTotalEarnedDiamond() + diamondRewardCount);

            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
            sendToClientData.setVipV3Data(vipV3Data);
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
