package org.skynet.service.provider.hunting.obsolete.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.skynet.components.hunting.game.data.BulletReward;
import org.skynet.components.hunting.game.data.ChestOpenResult;
import org.skynet.components.hunting.game.data.GunReward;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.domain.ChestData;
import org.skynet.components.hunting.user.domain.LuckyWheelData;
import org.skynet.components.hunting.user.domain.LuckyWheelV2Data;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.NanoIdUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.NumberUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.enums.ChestType;
import org.skynet.service.provider.hunting.obsolete.enums.GunLibraryType;
import org.skynet.service.provider.hunting.obsolete.enums.LuckyWheelV2RewardType;
import org.skynet.service.provider.hunting.obsolete.enums.Table;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.LuckyWheelV2SpinRewardBO;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
import org.skynet.service.provider.hunting.obsolete.service.LuckyWheelService;
import org.skynet.service.provider.hunting.obsolete.service.ObsoleteUserDataService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.table.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LuckyWheelServiceImpl implements LuckyWheelService {

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private ChestService chestService;

    @Override
    public LuckyWheelSpinReward spinLuckyWheelOnceReward(String userUid, String gameVersion, float additionValue) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);
        LuckyWheelData luckyWheelData = userData.getLuckyWheelData();

        Map<String, LuckyWheelRewardTableValue> LuckyWheelRewardTable = GameEnvironment.luckyWheelRewardTableMap.get(gameVersion);

        int totalSpinCount = luckyWheelData.getUseSpinCountInHistory() + 1;

        LuckyWheelRewardTableValue targetRewardTableValue = null;
        List<LuckyWheelRewardTableValue> loopRewardTableValues = new ArrayList<>();
        int rewardTableStartLoopIndex = Integer.MAX_VALUE;
        int rewardTableEndLoopIndex = Integer.MIN_VALUE;

        for (LuckyWheelRewardTableValue tableValue : LuckyWheelRewardTable.values()) {
            if (tableValue.getSpinCount() == totalSpinCount) {
                targetRewardTableValue = tableValue;
                break;
            }

            //记录可以loop的
            if (tableValue.getLoop()) {
                rewardTableStartLoopIndex = Math.min(tableValue.getId(), rewardTableStartLoopIndex);
                rewardTableEndLoopIndex = Math.max(tableValue.getId(), rewardTableEndLoopIndex);
                loopRewardTableValues.add(tableValue);

            }
        }

        if (targetRewardTableValue == null) {
            if (loopRewardTableValues.size() == 0) {
                throw new BusinessException("LuckyWheelRewardTable表没有初始化");
            }

            if (loopRewardTableValues.size() == 1) {
                targetRewardTableValue = loopRewardTableValues.get(0);
            }

            //从loop里面选一个
            int loopIndex = Math.max(0, totalSpinCount - rewardTableStartLoopIndex) % (rewardTableEndLoopIndex - rewardTableStartLoopIndex);
            targetRewardTableValue = loopRewardTableValues.get(loopIndex);
        }

        if (targetRewardTableValue == null) {
            throw new BusinessException("无法找到合适的" + Table.LuckyWheelReward.getName() + "value");
        }

        if (targetRewardTableValue.getRewardIndexWeights().size() != targetRewardTableValue.getRewardIndices().size()) {
            throw new BusinessException(Table.LuckyWheelReward.getName() + "的id" + targetRewardTableValue.getId() + "中的rewardIndexWeightsArray长度 不等于 rewardIndicesArray长度");
        }

        int rewardTotalWeight = 0;
        rewardTotalWeight = targetRewardTableValue.getRewardIndexWeights().stream().mapToInt(Integer::intValue).sum();

        //找到reward index
        Integer targetRewardIndex = null;
        for (int i = 0; i < targetRewardTableValue.getRewardIndices().size(); i++) {

            int rewardIndex = targetRewardTableValue.getRewardIndices().get(i);
            int rewardIndexWeight = targetRewardTableValue.getRewardIndexWeights().get(i);

            Integer random = NumberUtils.randomInt(0.0, (rewardTotalWeight + 1) * 1.0);
            if (random <= rewardIndexWeight) {

                targetRewardIndex = rewardIndex;
                break;
            }

            rewardTotalWeight -= rewardIndexWeight;
        }

        if (targetRewardIndex == null) {

            targetRewardIndex = targetRewardTableValue.getRewardIndices().get(targetRewardTableValue.getRewardIndices().size() - 1);
        }

        log.info("找到 reward index" + targetRewardIndex);

        Integer luckyWheelChapterId = luckyWheelData.getCurrentChapterId();
        Map<String, LuckyWheelSectorContentTableValue> LuckyWheelSectorContentTable = GameEnvironment.luckyWheelSectorContentTableMap.get(gameVersion);
        LuckyWheelSectorContentTableValue sectorContentTableValue = LuckyWheelSectorContentTable.get(String.valueOf(luckyWheelChapterId));

        if (sectorContentTableValue == null) {
            throw new BusinessException(Table.LuckyWheelSectorContent.getName() + "中 没有id" + luckyWheelChapterId);
        }

        Integer rewardChestType = sectorContentTableValue.getChestTypes().get(targetRewardIndex);
        if (rewardChestType >= ChestType.SILVER.getType()) {

            //宝箱奖励
            ChestData chestData = new ChestData(NanoIdUtils.randomNanoId(30), rewardChestType, luckyWheelChapterId, TimeUtils.getUnixTimeSecond());

            ChestOpenResult chestOpenReward = chestService.openChest(userData, chestData, gameVersion, additionValue);
            log.info("转盘获得宝箱奖励" + JSONObject.toJSONString(chestOpenReward));
            return new LuckyWheelSpinReward(targetRewardIndex, null, chestOpenReward);
        } else {

            //金币奖励
            Integer coinRewardAmount = sectorContentTableValue.getCoinAmounts().get(targetRewardIndex);
            log.info("转盘获得金币奖励" + coinRewardAmount);
            userData.setCoin(userData.getCoin() + coinRewardAmount);
            return new LuckyWheelSpinReward(targetRewardIndex, coinRewardAmount, null);

        }
    }

    @Override
    public void tryRefreshLuckyWheelContents(String userUid) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);
        LuckyWheelData luckyWheelData = userData.getLuckyWheelData();
        Long standardDay = TimeUtils.getStandardTimeDay();

        //没过一天,不刷新
        if (standardDay <= luckyWheelData.getLastRefreshLuckyWheelStandardDay()) {
            return;
        }
        luckyWheelData.setCurrentChapterId(obsoleteUserDataService.playerHighestUnlockedChapterID(userData));
        luckyWheelData.setCumulativeRewardSpinCount(0);
        luckyWheelData.setLastRefreshLuckyWheelStandardDay(standardDay);
        log.info("刷新转盘章节:" + JSONObject.toJSONString(luckyWheelData));

    }

    @Override
    public void refreshFreeSpinCount(String userUid, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);
        LuckyWheelV2Data luckyWheelV2Data = userData.getLuckyWheelV2Data();

        //可能时间过了很久，会增加多次免费次数
        long unixTimeNow = TimeUtils.getUnixTimeSecond();
        while (unixTimeNow >= luckyWheelV2Data.getNextFreeSpinUnixTime() &&
                luckyWheelV2Data.getFreeSpinCount() < GameEnvironment.luckyWheelPropertyTableMap.get(gameVersion).getDefaultFreeSpinCount()) {
            luckyWheelV2Data.setFreeSpinCount(luckyWheelV2Data.getFreeSpinCount() + 1);
            refreshNextFreeSpinTime(userUid, gameVersion);
            log.info("增加一次免费转盘次数" + JSONObject.toJSONString(luckyWheelV2Data));
        }
    }

    @Override
    public void refreshNextFreeSpinTime(String userUid, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);
        LuckyWheelV2Data luckyWheelV2Data = userData.getLuckyWheelV2Data();
        Long unixTimeNow = TimeUtils.getUnixTimeSecond();

        if (Objects.isNull(luckyWheelV2Data)) {
            return;
        }

        LuckyWheelV2PropertyTableValue luckyWheelV2Property = GameEnvironment.luckyWheelV2PropertyTableMap.get(gameVersion);

        if (luckyWheelV2Data.getFreeSpinCount() >= luckyWheelV2Property.getDefaultFreeSpinCount()) {

            //免费次数已经满了
            luckyWheelV2Data.setNextFreeSpinUnixTime(-1L);
        } else if (luckyWheelV2Data.getNextFreeSpinUnixTime() < 0) {

            //如果免费次数满状态使用了一次，nextFreeSpinUnixTime == -1，下一次解锁时间就是现在+增加时间
            luckyWheelV2Data.setNextFreeSpinUnixTime(unixTimeNow + luckyWheelV2Property.getFreeSpinIncreaseOnceSeconds());
        } else if (unixTimeNow >= luckyWheelV2Data.getNextFreeSpinUnixTime()) {

            //如果现在时间已经超过了下一次免费增加时间，那么下一次就是上一次时间+增加时间
            luckyWheelV2Data.setNextFreeSpinUnixTime(luckyWheelV2Data.getNextFreeSpinUnixTime() + luckyWheelV2Property.getFreeSpinIncreaseOnceSeconds());
        } else {
            //下一次时间没到,do nothing
        }

        if (userData.getLuckyWheelV2Data().getNextFreeSpinUnixTime() <= 0) {
            userData.getLuckyWheelV2Data().setNextFreeSpinUnixTime(TimeUtils.getUnixTimeSecond() + 1000);
        }

    }

    @Override
    public void refreshLuckyWheelV1FreeSpinCount(String userUid, String gameVersion) {
        UserData userData = GameEnvironment.userDataMap.get(userUid);
        LuckyWheelData luckyWheelData = userData.getLuckyWheelData();

        if (Objects.isNull(luckyWheelData)) {
            return;
        }

        LuckyWheelPropertyTable luckyWheelProperty = GameEnvironment.luckyWheelPropertyTableMap.get(gameVersion);
        Long unixTimeNow = TimeUtils.getUnixTimeSecond();
        while (unixTimeNow >= luckyWheelData.getNextFreeSpinUnixTime() && luckyWheelData.getFreeSpinCount() < luckyWheelProperty.getDefaultFreeSpinCount()) {
            luckyWheelData.setFreeSpinCount(luckyWheelData.getFreeSpinCount() + 1);
            refreshNextFreeSpinTime(userUid, gameVersion);
        }

    }

    @Override
    public void refreshLuckyWheelV2Content(String userUid, String gameVersion) {
        UserData userData = GameEnvironment.userDataMap.get(userUid);
        LuckyWheelV2Data luckyWheelV2Data = userData.getLuckyWheelV2Data();
        if (Objects.isNull(luckyWheelV2Data)) {
            return;
        }

        Long standardTimeDay = TimeUtils.getStandardTimeDay();
        if (luckyWheelV2Data.getLastRefreshLuckyWheelStandardDay() < standardTimeDay) {
            luckyWheelV2Data.setLastRefreshLuckyWheelStandardDay(standardTimeDay);
            Integer randomSectorContentId = getRandomLuckyWheelV2SectorContentId(gameVersion);
            luckyWheelV2Data.setSectorContentId(randomSectorContentId);
        }

    }

    @Override
    public Integer getRandomLuckyWheelV2SectorContentId(String gameVersion) {
        Map<String, LuckyWheelV2SectorContentTableValue> sectorContentTable = GameEnvironment.luckyWheelV2SectorContentTableMap.get(gameVersion);
        List<Integer> availableIds = sectorContentTable.values().stream().map(LuckyWheelV2SectorContentTableValue::getId).collect(Collectors.toList());
        return (Integer) NumberUtils.randomElementInArray(availableIds);
    }

    @Override
    public LuckyWheelV2SpinRewardBO spinLuckyWheelV2Reward(String userUid, String gameVersion, float additionValue) {
        UserData userData = GameEnvironment.userDataMap.get(userUid);
        LuckyWheelV2Data luckyWheelV2Data = userData.getLuckyWheelV2Data();

        Map<String, LuckyWheelV2RewardTableValue> luckyWheelV2RewardTable = GameEnvironment.luckyWheelV2RewardTableMap.get(gameVersion);

        luckyWheelV2Data.setUseSpinCountInHistory(luckyWheelV2Data.getUseSpinCountInHistory() + 1);
        Integer totalSpinCount = luckyWheelV2Data.getUseSpinCountInHistory();

        LuckyWheelV2RewardTableValue targetRewardTableValue = null;
        List<LuckyWheelV2RewardTableValue> loopRewardTableValues = Lists.newArrayList();
        int rewardTableStartLoopIndex = Integer.MAX_VALUE;
        int rewardTableEndLoopIndex = Integer.MIN_VALUE;

        for (Map.Entry<String, LuckyWheelV2RewardTableValue> entry : luckyWheelV2RewardTable.entrySet()) {
            LuckyWheelV2RewardTableValue luckyWheelRewardTableValue = entry.getValue();
            if (luckyWheelRewardTableValue.getSpinCount() == totalSpinCount) {
                targetRewardTableValue = luckyWheelRewardTableValue;
                break;
            }

            //记录可以loop的
            if (luckyWheelRewardTableValue.getLoop()) {
                rewardTableStartLoopIndex = Math.min(luckyWheelRewardTableValue.getId(), rewardTableStartLoopIndex);
                rewardTableEndLoopIndex = Math.max(luckyWheelRewardTableValue.getId(), rewardTableEndLoopIndex);

                loopRewardTableValues.add(luckyWheelRewardTableValue);
            }
        }

        if (targetRewardTableValue == null) {
            if (loopRewardTableValues.size() == 0) {
                throw new BusinessException("LuckyWheelRewardTable表没有loop条目");
            }

            if (loopRewardTableValues.size() == 1) {
                targetRewardTableValue = loopRewardTableValues.get(0);
            }

            //从loop里面选一个
            int loopIndex = Math.max(0, totalSpinCount - rewardTableStartLoopIndex) % (rewardTableEndLoopIndex - rewardTableStartLoopIndex);
            targetRewardTableValue = loopRewardTableValues.get(loopIndex);
        }

        if (targetRewardTableValue == null) {
            throw new BusinessException("无法找到合适的LuckyWheelRewardTableValue");
        }
        if (targetRewardTableValue.getRewardIndexWeights().size() != targetRewardTableValue.getRewardIndices().size()) {
            throw new BusinessException("LuckyWheelRewardTable id " + targetRewardTableValue.getId() + " 中的rewardIndexWeightsArray长度 不等于 rewardIndicesArray长度");
        }

        int rewardTotalWeight = 0;
        for (int i = 0; i < targetRewardTableValue.getRewardIndexWeights().size(); i++) {
            Integer weight = targetRewardTableValue.getRewardIndexWeights().get(i);
            rewardTotalWeight += weight;
        }

        //找到reward index
        Integer targetRewardIndex = null;
        for (int i = 0; i < targetRewardTableValue.getRewardIndices().size(); i++) {
            Integer rewardIndex = targetRewardTableValue.getRewardIndices().get(i);
            Integer rewardIndexWeight = targetRewardTableValue.getRewardIndexWeights().get(i);

            Integer random = NumberUtils.randomInt(0d, rewardTotalWeight + 1d);
            if (random <= rewardIndexWeight) {
                targetRewardIndex = rewardIndex;
                break;
            }
            rewardTotalWeight -= rewardIndexWeight;
        }

        //防止找不到
        if (targetRewardIndex == null) {
            targetRewardIndex = targetRewardTableValue.getRewardIndices().get(targetRewardTableValue.getRewardIndices().size() - 1);
        }
//        log.info("targetRewardTableValue:" + targetRewardTableValue.toString());
//        log.info("targetRewardIndex:" + targetRewardIndex);

        Integer sectorContentId = luckyWheelV2Data.getSectorContentId();
        Map<String, LuckyWheelV2SectorContentTableValue> luckyWheelV2SectorContentTable = GameEnvironment.luckyWheelV2SectorContentTableMap.get(gameVersion);

        LuckyWheelV2SectorContentTableValue sectorContentTableValue = luckyWheelV2SectorContentTable.get(sectorContentId.toString());

        if (sectorContentTableValue == null) {
            throw new BusinessException("LuckyWheelV2SectorContentTable中 没有id " + sectorContentId);
        }

        if (sectorContentTableValue.getRewardTypes().size() != sectorContentTableValue.getRewardAmounts().size()) {
            throw new BusinessException("LuckyWheelV2SectorContentTable表中id " + sectorContentTableValue.getId() + " 的rewardTypesArray数组长度!=rewardAmountsArray数组长度");
        }

        LuckyWheelV2RewardType rewardType = LuckyWheelV2RewardType.values()[sectorContentTableValue.getRewardTypes().get(targetRewardIndex) - 1];
//        log.info("rewardType:" + rewardType.getType());
//        log.info("luckyWheelV2SectorContentTable:" + luckyWheelV2SectorContentTable.toString());
        Integer rewardCount = sectorContentTableValue.getRewardAmounts().get(targetRewardIndex);
        int playerHighestUnlockedChapterID = obsoleteUserDataService.getPlayerHighestUnlockedChapterID(userData);

        LuckyWheelV2SpinRewardBO spinRewardResult = new LuckyWheelV2SpinRewardBO();
        spinRewardResult.setRewardIndex(targetRewardIndex);

        LuckyWheelV2PropertyTableValue luckyWheelV2PropertyTable = GameEnvironment.luckyWheelV2PropertyTableMap.get(gameVersion);

        switch (rewardType) {
            case Coin:
                userData.setCoin(userData.getCoin() + rewardCount);
                spinRewardResult.setRewardCoin(rewardCount);
                return spinRewardResult;
            case RandomGunCard: {
                /*
                 * 从random枪库中抽卡
                 * */
                Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
                gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Random, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, additionValue);
                List<Integer> newUnlockGunIds = Lists.newArrayList();

                List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
                obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, newUnlockGunIds, gameVersion);

                spinRewardResult.setRewardGunInfo(new RewardGunInfo(gunRewards, newUnlockGunIds));
//                spinRewardResult.getChestOpenResult().setNewUnlockedGunIDs(newUnlockGunIds);
//                userData.setCoin(userData.getCoin() + rewardCount);
//                spinRewardResult.setRewardCoin(rewardCount);
                return spinRewardResult;
            }
            case BlueGunCard: {
                /*
                 * 从common枪库中抽卡，都是蓝卡
                 * */
                Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
                gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Common, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, additionValue);
                List<Integer> newUnlockGunIds = Lists.newArrayList();

                List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
                obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, newUnlockGunIds, gameVersion);

                spinRewardResult.setRewardGunInfo(new RewardGunInfo(gunRewards, newUnlockGunIds));
//                spinRewardResult.getChestOpenResult().setNewUnlockedGunIDs(newUnlockGunIds);
//                userData.setCoin(userData.getCoin() + rewardCount);
//                spinRewardResult.setRewardCoin(rewardCount);
                return spinRewardResult;
            }
            case OrangeGunCard: {
                /*
                 * 从common枪库中抽卡，都是蓝卡
                 * */
                Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
                gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, additionValue);
                List<Integer> newUnlockGunIds = Lists.newArrayList();

                List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
                obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, newUnlockGunIds, gameVersion);

                spinRewardResult.setRewardGunInfo(new RewardGunInfo(gunRewards, newUnlockGunIds));
//                spinRewardResult.getChestOpenResult().setNewUnlockedGunIDs(newUnlockGunIds);
//                userData.setCoin(userData.getCoin() + rewardCount);
//                spinRewardResult.setRewardCoin(rewardCount);
                return spinRewardResult;
            }
            case RedGunCard: {
                /*
                 * 从common枪库中抽卡，都是蓝卡
                 * */
                Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
                gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Epic, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, additionValue);
                List<Integer> newUnlockGunIds = Lists.newArrayList();

                List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
                obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, newUnlockGunIds, gameVersion);

                spinRewardResult.setRewardGunInfo(new RewardGunInfo(gunRewards, newUnlockGunIds));
//                spinRewardResult.getChestOpenResult().setNewUnlockedGunIDs(newUnlockGunIds);
//                userData.setCoin(userData.getCoin() + rewardCount);
//                spinRewardResult.setRewardCoin(rewardCount);
                return spinRewardResult;
            }
            case FixBullet1: {
                Map<Integer, Integer> bulletRewardMap = Maps.newHashMap();
                bulletRewardMap.put(luckyWheelV2PropertyTable.getRewardType6BulletId(), rewardCount);
                List<BulletReward> bulletRewards = CommonUtils.convertBulletCountMapToBulletCountArray(bulletRewardMap);
                obsoleteUserDataService.addBulletToUserDataByIdCountData(userData, bulletRewards);
                spinRewardResult.setBulletRewards(bulletRewards);
//                userData.setCoin(userData.getCoin() + rewardCount);
//                spinRewardResult.setRewardCoin(rewardCount);
                return spinRewardResult;
            }
            case FixBullet2: {
                Map<Integer, Integer> bulletRewardMap = Maps.newHashMap();
                bulletRewardMap.put(luckyWheelV2PropertyTable.getRewardType7BulletId(), rewardCount);
                List<BulletReward> bulletRewards = CommonUtils.convertBulletCountMapToBulletCountArray(bulletRewardMap);
                obsoleteUserDataService.addBulletToUserDataByIdCountData(userData, bulletRewards);
                spinRewardResult.setBulletRewards(bulletRewards);
//                userData.setCoin(userData.getCoin() + rewardCount);
//                spinRewardResult.setRewardCoin(rewardCount);
                return spinRewardResult;
            }
            case FixBullet3: {
                Map<Integer, Integer> bulletRewardMap = Maps.newHashMap();
                bulletRewardMap.put(luckyWheelV2PropertyTable.getRewardType8BulletId(), rewardCount);
                List<BulletReward> bulletRewards = CommonUtils.convertBulletCountMapToBulletCountArray(bulletRewardMap);
                obsoleteUserDataService.addBulletToUserDataByIdCountData(userData, bulletRewards);
                spinRewardResult.setBulletRewards(bulletRewards);
//                userData.setCoin(userData.getCoin() + rewardCount);
//                spinRewardResult.setRewardCoin(rewardCount);
                return spinRewardResult;
            }
            case BronzeChest: {
                ChestData chestData = new ChestData(NanoIdUtils.randomNanoId(30), ChestType.BRONZE.getType(), playerHighestUnlockedChapterID, TimeUtils.getUnixTimeSecond());
                ChestOpenResult chestOpenResult = chestService.openChest(userData, chestData, gameVersion, additionValue);
                spinRewardResult.setChestOpenResult(chestOpenResult);
//                userData.setCoin(userData.getCoin() + rewardCount);
//                spinRewardResult.setRewardCoin(rewardCount);
                return spinRewardResult;
            }
            case SilverChest: {
                ChestData chestData = new ChestData(NanoIdUtils.randomNanoId(30), ChestType.SILVER.getType(), playerHighestUnlockedChapterID, TimeUtils.getUnixTimeSecond());
                ChestOpenResult chestOpenResult = chestService.openChest(userData, chestData, gameVersion, additionValue);
                spinRewardResult.setChestOpenResult(chestOpenResult);
//                userData.setCoin(userData.getCoin() + rewardCount);
//                spinRewardResult.setRewardCoin(rewardCount);
                return spinRewardResult;
            }
        }

        return null;
    }
}
