package org.skynet.service.provider.hunting.obsolete.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.domain.ChapterWinChestData;
import org.skynet.components.hunting.user.domain.ChestData;
import org.skynet.components.hunting.user.domain.ChestOpenIndexMap;
import org.skynet.components.hunting.user.domain.FreeChestData;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.NanoIdUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.config.GameConfig;
import org.skynet.service.provider.hunting.obsolete.enums.BulletLibraryType;
import org.skynet.service.provider.hunting.obsolete.enums.ChestType;
import org.skynet.service.provider.hunting.obsolete.enums.GunLibraryType;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.CoinAndDiamondBO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.*;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
import org.skynet.service.provider.hunting.obsolete.service.ObsoleteUserDataService;
import org.skynet.service.provider.hunting.obsolete.service.WeaponService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class ChestServiceImpl implements ChestService {

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private WeaponService weaponService;

    @Override
    public void openMailChest(String userUid, MailChestContent mailChestContent, List<Integer> unlockNewGunIds, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);
        userData.setCoin(userData.getCoin() + mailChestContent.getCoin());
        userData.setDiamond(userData.getDiamond() + mailChestContent.getDiamond());

        if (mailChestContent.getGunRewards() != null) {

            obsoleteUserDataService.addGunToUserData(userData, CommonUtils.convertGunCountArrayToGunCountMap(mailChestContent.getGunRewards()), unlockNewGunIds, gameVersion);
        }

        if (mailChestContent.getBulletRewards() != null) {
            obsoleteUserDataService.addBulletToUserData(userData, CommonUtils.convertBulletCountArrayToBulletCountMap(mailChestContent.getBulletRewards()));
        }
    }

    @Override
    public void refreshFreeChestsData(String uuid) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);
        Long unixTimeNow = TimeUtils.getUnixTimeSecond();

        //玩家解锁第三章之前的免费宝箱为2小时，当玩家解锁第三章以后，再次解锁的宝箱间隔时间为4小时
        int playerHighestUnlockedChapterId = obsoleteUserDataService.playerHighestUnlockedChapterID(userData);
        int freeChestUnlockSeconds = playerHighestUnlockedChapterId >= 3 ? GameConfig.longFreeChestUnlockSeconds : GameConfig.shortFreeChestUnlockSeconds;

        //创建数组
        if (userData.getFreeChestsData() == null || userData.getFreeChestsData().length == 0) {

            userData.setFreeChestsData(new FreeChestData[2]);
        }

        FreeChestData[] freeChestsData = userData.getFreeChestsData();

        //第一个槽位是空,第二个槽位是满,说明刚打开第一个箱子
        //这时候,第二个箱子放到第一个槽位,第二个槽位创建一个全新的宝箱
        Boolean createNewChest = false;
        if (freeChestsData[0] == null && freeChestsData[1] != null) {
            freeChestsData[0] = freeChestsData[1];
            freeChestsData[1] = null;
            createNewChest = true;
        }

        //放满箱子
        if (freeChestsData[0] == null) {
            freeChestsData[0] = createNewFreeChest(userData, unixTimeNow + freeChestUnlockSeconds);
            log.info("创建免费箱子at 0：" + JSONObject.toJSONString(freeChestsData[0]));
        }

        if (freeChestsData[1] == null) {
            FreeChestData firstChest = freeChestsData[0];
            long availableTime;
            if (createNewChest) {
                availableTime = Math.max(unixTimeNow + freeChestUnlockSeconds, firstChest.getAvailableUnixTime().intValue() + freeChestUnlockSeconds);
            } else {
                availableTime = firstChest.getAvailableUnixTime() + freeChestUnlockSeconds;
            }
            freeChestsData[1] = createNewFreeChest(userData, availableTime);
            log.info("创建免费箱子at 1：" + JSONObject.toJSONString(freeChestsData[1]));
        }


    }

    /**
     * 创建默认的免费箱子
     *
     * @param userData
     * @param availableTime
     * @return
     */
    @Override
    public FreeChestData createNewFreeChest(UserData userData, Long availableTime) {

        //免费箱子都是木头箱子
        ChestType freeChestType = ChestType.FREE;
        Integer freeChestLevel = obsoleteUserDataService.playerHighestUnlockedChapterID(userData);
        // FreeChestData freeChestData = new FreeChestData(NanoIdUtils.randomNanoId(30), freeChestType.getType(), freeChestLevel, TimeUtils.getUnixTimeSecond(), availableTime);
        //
        // return freeChestData;

        FreeChestData freeChestData = new FreeChestData();
        freeChestData.setUid(NanoIdUtils.randomNanoId(30));
        freeChestData.setChestType(freeChestType.getType());
        freeChestData.setLevel(freeChestLevel);
        freeChestData.setCreateTime(TimeUtils.getUnixTimeSecond());
        freeChestData.setAvailableUnixTime(availableTime);

        return freeChestData;
    }

//    /**
//     * 刷新章节进度宝箱数据
//     */
//    @Override
//    public void refreshChapterProgressChestData(String uuid) {
//
//        UserData userData = GameEnvironment.userDataMap.get(uuid);
//        //如果没有,则创建一个
//        if (userData.getChapterProgressChestsData() == null){
//
//            Integer chapterChestTaskIndex;
//            if (userData.getServerOnly().getChapterChestTaskIndex() == null){
//                chapterChestTaskIndex = 1;
//            }else {
//
//                chapterChestTaskIndex = userData.getServerOnly().getChapterChestTaskIndex();
//            }
//
//            Map<String, ChapterChestTaskTableValue> chapterChestTaskTable = GameEnvironment.chapterChestTaskTableMap;
//
//            //如果超过索引,则使用最后一个
//            if (!chapterChestTaskTable.containsKey(chapterChestTaskIndex.toString())){
//                    int maxIndex = 0;
//                for (String tableKey : chapterChestTaskTable.keySet()) {
//
//                    int index = Integer.parseInt(tableKey);
//                    if (index > maxIndex){
//
//                        maxIndex = index;
//                    }
//                }
//
//                chapterChestTaskIndex = maxIndex;
//            }
//
//            ChapterChestTaskTableValue chapterChestTaskValue = chapterChestTaskTable.get(String.valueOf(chapterChestTaskIndex));
//
//            if (chapterChestTaskValue == null){
//
//                throw new BusinessException("用户"+userData.getUuid()+"刷新章节箱子,在index"+chapterChestTaskIndex+"无法获得章节箱子配置");
//            }
//
//            ChestType chestType = chapterChestTaskValue.getChestType();
//            Integer chestLevel = userDataService.playerHighestUnlockedChapterID(userData);
//
//            userData.setChapterProgressChestsData(new ChapterProgressChestData(NanoIdUtils.randomNanoId(30),chestType.getType(),chestLevel,TimeUtils.getUnixTimeSecond(),0,chapterChestTaskValue.getMatchCompleteRequires()));
//
//            userData.getServerOnly().setChapterChestTaskIndex(chapterChestTaskIndex);
//
//            log.info("创建章节进度宝箱"+userData.getChapterProgressChestsData());
//
//        }
//    }

    /**
     * 生成一个章节胜利箱子
     *
     * @param chapterId
     * @param uuid
     * @return
     */
    @Override
    public ChapterWinChestData tryCreateChapterWinChestAsync(Integer chapterId, String uuid) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);

        //找到宝箱位置


        return null;
    }

    /**
     * 获取章节赢取宝箱位置信息
     *
     * @param uuid
     * @return
     */
    @Override
    public Integer getChapterWinChestSlotInfo(String uuid) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);

        //找到宝箱位置
        int emptySlotIndex = -1;

        for (int i = 0; i < ChapterWinChestConfig.ChestSlotAmount; i++) {

            if (i >= userData.getChapterWinChestsData().size()) {

                emptySlotIndex = i;
                break;
            }

            ChapterWinChestData slotChestData = userData.getChapterWinChestsData().get(i);

            if (slotChestData == null) {

                emptySlotIndex = i;
                break;
            }

        }
        return emptySlotIndex;

    }

    /**
     * 创建一个章节胜利宝箱
     *
     * @param chapterId
     * @param uuid
     * @param gameVersion
     * @return
     */
    @Override
    public ChapterWinChestData createChapterWinChest(Integer chapterId, String uuid, String gameVersion) {

        Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(gameVersion);

        ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(chapterId));

        UserData userData = GameEnvironment.userDataMap.get(uuid);
        Map<Integer, Integer> chapterWinCountMap = userData.getChapterWinCountMap();
        int chapterWinCount = chapterWinCountMap.getOrDefault(chapterTableValue.getId(), 0);

        //生成一个宝箱,胜利次数已经加了,所以这里减1
        int winChestIndex = Math.max(0, chapterWinCount - 1);

        //获取宝箱类型,如果第一个奖励数组遍历完成,在第二个数组中循环
        List<Integer> rewardChestTypeArray = chapterTableValue.getWinRewardChestType();
        if (winChestIndex >= chapterTableValue.getWinRewardChestType().size()) {

            rewardChestTypeArray = chapterTableValue.getFallbackWinRewardChestType();
            winChestIndex -= chapterTableValue.getWinRewardChestType().size();
            winChestIndex %= rewardChestTypeArray.size();
        }
        int chestIndex = rewardChestTypeArray.get(winChestIndex);
        ChestType winChestType = ChestType.values()[chestIndex - 1];
        int chestUnlockSeconds = ChapterWinChestConfig.getChestUnlockSeconds(winChestType);

        // return new ChapterWinChestData(NanoIdUtils.randomNanoId(30), winChestType.getType(), chapterTableValue.getId(), TimeUtils.getUnixTimeSecond(), -1L, (long) chestUnlockSeconds);

        ChapterWinChestData chapterWinChestData = new ChapterWinChestData();
        chapterWinChestData.setUid(NanoIdUtils.randomNanoId(30));
        chapterWinChestData.setChestType(winChestType.getType());
        chapterWinChestData.setLevel(chapterTableValue.getId());
        chapterWinChestData.setCreateTime(TimeUtils.getUnixTimeSecond());
        chapterWinChestData.setAvailableUnixTime(-1L);
        chapterWinChestData.setUnlockSecondsRequires((long) chestUnlockSeconds);
        return chapterWinChestData;
    }

    @Override
    public ChapterWinChestData tryCreateChapterWinChest(String uuid, Integer chapterId, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);

        //找到宝箱位置
        Integer emptySlotIndex = getChapterWinChestSlotInfo(uuid);

        //没有格子,不生成宝箱,来自需求:http://192.168.2.45/huntingrival/huntingrival-client/-/issues/154
        if (emptySlotIndex < 0) {
            return null;
        }
        ChapterWinChestData newChapterWinChest = createChapterWinChest(chapterId, uuid, gameVersion);
        userData.getChapterWinChestsData().add(emptySlotIndex, newChapterWinChest);

        return newChapterWinChest;
    }

//    @Override
//    public IncreaseChapterWinChestProgressResult tryIncreaseChapterWinChestProgress(String uuid, Integer chapterId) {
//
//        UserData userData = GameEnvironment.userDataMap.get(uuid);
//
//        IncreaseChapterWinChestProgressResult result = new IncreaseChapterWinChestProgressResult(null,null,null,null);
//        result.setChapterId(chapterId);
//
//        ChapterWinChestProgressData chapterWinChestProgressData = userData.getChapterWinChestProgressMap().get(chapterId);
//        result.setProgressBefore(chapterWinChestProgressData.getCurrentProgress());
//        result.setProgressAfter(chapterWinChestProgressData.getCurrentProgress());
//
//        //如果这一次就能获得宝箱,那么要先看是否有宝箱空位,如果没有,不增加
//        if (chapterWinChestProgressData.getCurrentProgress() == chapterWinChestProgressData.getProgressMax() -1){
//            Integer emptySlotIndex = getChapterWinChestSlotInfo(uuid);
//
//            if (emptySlotIndex < 0 ){
//                return result;
//            }
//        }
//
//        int current =  chapterWinChestProgressData.getCurrentProgress() + 1;
//        chapterWinChestProgressData.setCurrentProgress(current);
//        result.setProgressAfter(chapterWinChestProgressData.getCurrentProgress());
//
//        log.info("章节"+chapterId+"增加章节胜利宝箱进度"+chapterWinChestProgressData.getCurrentProgress()+"/"+chapterWinChestProgressData.getProgressMax());
//        if (chapterWinChestProgressData.getCurrentProgress() >= chapterWinChestProgressData.getProgressMax()){
//
//            chapterWinChestProgressData.setCurrentProgress(0);
//            chapterWinChestProgressData.setProgressMax(GameConfig.ChapterWinChestMaxProgress);
//
//            int emptySlotIndex = getChapterWinChestSlotInfo(uuid);
//            ChapterWinChestData newChapterWinChest = createChapterWinChest(chapterId,uuid);
//            userData.getChapterWinChestsData().add(emptySlotIndex,newChapterWinChest);
//
//            result.setObtainChapterWinChestData(newChapterWinChest);
//            return result;
//        }
//
//        return result;
//    }


    @Override
    public void saveChestOpenResult(ChestOpenResult chestOpenResult, String userUid, Integer userUpdateCount) {

        log.info("update count:" + userUpdateCount);
        String path = Path.getPlayerChestOpenResultDocPath(userUid);

        ArchiveChestOpenResult archiveResult = new ArchiveChestOpenResult(userUpdateCount, chestOpenResult);

        RedisDBOperation.insertChestOpenResult(path, archiveResult);

    }

    @Override
    public ChestOpenResult openChest(UserData userData, ChestData chestData, String gameVersion) {

        Map<String, ChestContentMapTableValue> chestContentMapTable = GameEnvironment.chestContentMapTableMap.get(gameVersion);
        ChestContentMapTableValue chestContentMapTableValue = null;

        Set<String> keySet = chestContentMapTable.keySet();
        for (String key : keySet) {

            ChestContentMapTableValue tableValue = chestContentMapTable.get(key);
            if (!tableValue.getChestType().equals(chestData.getChestType()) || !tableValue.getChestLevel().equals(chestData.getLevel())) {
                continue;
            }
            chestContentMapTableValue = tableValue;
            break;
        }

        if (chestContentMapTableValue == null) {

            throw new BusinessException("玩家" + userData.getUuid() + "请求开箱,无法在ChestContentMapTableValue表中,找到" + chestData + "箱子的条目");
        }

        if (chestContentMapTableValue.getRandomBulletCount() > 0
                && (chestContentMapTableValue.getHighChanceBulletCount() > 0 || chestContentMapTableValue.getLowChanceBulletCount() > 0)) {
            throw new BusinessException("玩家" + userData.getUuid() + "请求开箱,找到" + JSONObject.toJSONString(chestData) + "箱子的条目,但是randomBulletCount/highChanceBulletCount/lowChanceBulletCount有一个以上大于0");
        }

        //宝箱加成
        int[] playerChestRewardAddition = obsoleteUserDataService.getPlayerChestRewardAddition(userData.getUuid());
        ChestOpenResult chestOpenResult = new ChestOpenResult();
        chestOpenResult.setChestData(chestData);

        Map<String, ChestTableValue> chestTable = GameEnvironment.chestTableMap.get(gameVersion);
        Boolean enableDrawCountRequires = chestTable.get(chestData.getChestType().toString()).getEnableRewardLibraryDrawCountRequires();


        //gun
        Map<Integer, Integer> gunRewardMap = new HashMap<>();
        if (chestContentMapTableValue.getEpicGunCount() > 0) {
            gunRewardMap = extractGunRewardsFromGunLibrary(userData, GunLibraryType.Epic, chestData.getLevel(), chestContentMapTableValue.getEpicGunCount(), gameVersion, enableDrawCountRequires, gunRewardMap);
        }

        if (chestContentMapTableValue.getRareGunCount() > 0) {
            gunRewardMap = extractGunRewardsFromGunLibrary(userData, GunLibraryType.Rare, chestData.getLevel(), chestContentMapTableValue.getRareGunCount(), gameVersion, enableDrawCountRequires, gunRewardMap);
        }
        if (chestContentMapTableValue.getRandomGunCount() > 0) {
            gunRewardMap = extractGunRewardsFromGunLibrary(userData, GunLibraryType.Random, chestData.getLevel(), chestContentMapTableValue.getRandomGunCount(), gameVersion, enableDrawCountRequires, gunRewardMap);
        }

        if (gunRewardMap.size() > 0) {
            chestOpenResult.setGunRewards(new ArrayList<>());
            Set<Integer> keySet1 = gunRewardMap.keySet();
            for (Integer key : keySet1) {
                chestOpenResult.getGunRewards().add(new GunReward(key, gunRewardMap.get(key)));
            }

            //将枪数据加到角色数据中
            List<Integer> newUnlockGunIds = new ArrayList<>();
            obsoleteUserDataService.addGunToUserData(userData, CommonUtils.convertGunCountArrayToGunCountMap(chestOpenResult.getGunRewards()), newUnlockGunIds, gameVersion);
            chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        }

        //bullet
        Map<Integer, Integer> rewardBulletCountMap = new HashMap<>();
        if (chestContentMapTableValue.getRandomBulletCount() > 0) {
            getBulletRewardsAsync(userData, BulletLibraryType.Random, chestData.getLevel(), chestContentMapTableValue.getRandomBulletCount(), gameVersion, rewardBulletCountMap);
        } else if (chestContentMapTableValue.getHighChanceBulletCount() > 0) {
            getBulletRewardsAsync(userData, BulletLibraryType.HighChance, chestData.getLevel(), chestContentMapTableValue.getRandomBulletCount(), gameVersion, rewardBulletCountMap);
        } else if (chestContentMapTableValue.getLowChanceBulletCount() > 0) {
            getBulletRewardsAsync(userData, BulletLibraryType.LowChance, chestData.getLevel(), chestContentMapTableValue.getRandomBulletCount(), gameVersion, rewardBulletCountMap);
        }

        if (rewardBulletCountMap.size() > 0) {
            chestOpenResult.setBulletRewards(new ArrayList<>());
            rewardBulletCountMap.forEach((key, value) -> chestOpenResult.getBulletRewards().add(new BulletReward(key, rewardBulletCountMap.get(key))));

            obsoleteUserDataService.addBulletToUserData(userData, rewardBulletCountMap);
        }

        CoinAndDiamondBO coinAndDiamondRewards = getCoinAndDiamondRewards(userData, chestData.getChestType(), chestData.getLevel(), gameVersion);

        chestOpenResult.setCoin(coinAndDiamondRewards.getCoin());
        chestOpenResult.setDiamond(coinAndDiamondRewards.getDiamond());

        userData.setCoin(userData.getCoin() + coinAndDiamondRewards.getCoin());
        userData.getHistory().setTotalEarnedCoin(userData.getHistory().getTotalEarnedCoin() + coinAndDiamondRewards.getCoin());
        userData.setDiamond(userData.getDiamond() + coinAndDiamondRewards.getDiamond());
        userData.getHistory().setTotalEarnedDiamond(userData.getHistory().getTotalEarnedDiamond() + coinAndDiamondRewards.getDiamond());

        log.info("开箱结果" + chestOpenResult);
        return chestOpenResult;
    }

    @Override
    public List<Integer> getGunRewards(UserData userData, GunLibraryType gunLibraryType, Integer chestLevel, Integer getGunCount, String gameVersion) {

        Map<String, ChestGunTableValue> chestGunTable = GameEnvironment.chestGunTableMap.get(gameVersion);
        ChestGunTableValue chestGunTableValue = null;

        Set<String> keySet = chestGunTable.keySet();
        for (String key : keySet) {

            ChestGunTableValue tableValue = chestGunTable.get(key);
            if (tableValue.getLibraryType().equals(gunLibraryType.getType()) && tableValue.getChestLevel().equals(chestLevel)) {

                chestGunTableValue = tableValue;
                break;
            }
        }

        if (chestGunTableValue == null) {
            throw new BusinessException("玩家" + userData.getUuid() + "请求开箱,无法在 ChestGun 表中,找到 library type: " + gunLibraryType + ", chest level" + chestLevel + "条目");
        }

        ChestOpenIndexMap chestOpenIndexMap = userData.getServerOnly().getChestOpenIndexMap();
        String key = gunLibraryType + "_" + chestLevel;

        int index = chestOpenIndexMap.getGunRewardIndexMap().getOrDefault(key, 0);

        List<Integer> rewardGunIds = flattenGetElementFromRewardArray(chestGunTableValue.getRewardGunIds(),
                chestGunTableValue.getFallbackRewardGunIds(),
                index,
                getGunCount);

        chestOpenIndexMap.getGunRewardIndexMap().put(key, index + getGunCount);

        log.info("开箱枪,库类型 " + gunLibraryType + ", 宝箱等级 " + chestLevel + ", 获取枪数量" + getGunCount + ".结果" + JSONObject.toJSONString(rewardGunIds));

        return rewardGunIds;
    }

    @Override
    public List<Integer> getBulletRewards(UserData userData, BulletLibraryType bulletLibraryType, Integer chestLevel, Integer getBulletCount, String gameVersion, Map<Integer, Integer> resultBulletCountMap) {

        Map<String, ChestBulletTableValue> chestBulletTable = GameEnvironment.chestBulletTableMap.get(gameVersion);

        ChestBulletTableValue chestBulletTableValue = null;
        Set<String> keySet = chestBulletTable.keySet();
        for (String key : keySet) {

            ChestBulletTableValue tableValue = chestBulletTable.get(key);
            if (tableValue.getLibraryType().equals(bulletLibraryType.getType()) && tableValue.getChestLevel().equals(chestLevel)) {
                chestBulletTableValue = tableValue;
                break;
            }
        }

        if (chestBulletTableValue == null) {

            throw new BusinessException("玩家" + userData.getUuid() + "请求开箱,无法在 ChestBullet 表中,找到 library type: " + bulletLibraryType + ",chest level" + chestLevel + "条目");
        }

        ChestOpenIndexMap chestOpenIndexMap = userData.getServerOnly().getChestOpenIndexMap();
        String key = bulletLibraryType + "_" + chestLevel;
        int index = chestOpenIndexMap.getBulletRewardIndexMap().getOrDefault(key, 0);

        List<Integer> rewardBulletIds = flattenGetElementFromRewardArray(chestBulletTableValue.getRewardBulletIds(), chestBulletTableValue.getFallbackRewardBulletIds(), index, getBulletCount);

        chestOpenIndexMap.getBulletRewardIndexMap().put(key, index + getBulletCount);

        log.info("开箱子弹，库类型" + bulletLibraryType + ",宝箱等级" + chestLevel + ",获取数量" + getBulletCount + ".结果" + rewardBulletIds);
        resultBulletCountMap = CollectionUtils.isEmpty(resultBulletCountMap) ? new HashMap<>() : resultBulletCountMap;
        for (Integer bulletId : rewardBulletIds) {
            if (bulletId == 0) {
                continue;
            }
            if (resultBulletCountMap.get(bulletId) == null) {
                resultBulletCountMap.put(bulletId, 1);
            } else {
                resultBulletCountMap.put(bulletId, resultBulletCountMap.get(bulletId) + 1);
            }
        }

        return rewardBulletIds;

    }

    @Override
    public Boolean isFreeChestAvailable(FreeChestData freeChestData) {

        long unixTimeNow = TimeUtils.getUnixTimeSecond();
        return freeChestData.getAvailableUnixTime() <= unixTimeNow + GameConfig.chestOpenTimeDiffTolerance;
    }

    @Override
    public ArchiveChestOpenResult getLatestOpenedChestOpenResult(String uid) {


        return RedisDBOperation.selectLatestOpenedChestOpenResult(uid);
    }

    @Override
    public Map<Integer, Integer> extractGunRewardsFromGunLibraryAsync(UserData userData, GunLibraryType gunLibraryType, int chestLevel, int getGunCount, boolean enableDrawCountRequires, Map<Integer, Integer> resultMap, String gameVersion) {
        getGunCount = getGunCount + (int) Math.ceil(getGunCount * 0.2);
        //确保数据存在
        obsoleteUserDataService.upgradePlayerChestOpenIndexMapData(userData);

        Map<String, ChestGunTableValue> chestGunTable = GameEnvironment.chestGunTableMap.get(gameVersion);
        ChestGunTableValue chestGunTableValue = null;

        List<ChestGunTableValue> matchGunLibraryTypeTableValues = enableDrawCountRequires ? new ArrayList<>() : null;
        for (String key : chestGunTable.keySet()) {
            ChestGunTableValue tableValue = chestGunTable.get(key);
            if (gunLibraryType.getType() == tableValue.getLibraryType()) {
                if (matchGunLibraryTypeTableValues != null) {
                    matchGunLibraryTypeTableValues.add(tableValue);
                }
                if (chestLevel == tableValue.getChestLevel()) {
                    chestGunTableValue = tableValue;
                }
            }
        }

        if (chestGunTableValue == null) {
            throw new BusinessException("玩家" + userData.getUuid() + "请求开箱,无法在 ChestGun 表中,找到 library type: " + gunLibraryType.getType() + ",chest level " + chestLevel + " 条目.");
        }

        //需要匹配在当前类型库中抽取次数
        if (matchGunLibraryTypeTableValues != null) {
            matchGunLibraryTypeTableValues.sort(new Comparator<ChestGunTableValue>() {
                @Override
                public int compare(ChestGunTableValue o1, ChestGunTableValue o2) {
                    return o1.getDrawCountRequires() - o2.getDrawCountRequires();
                }
            });
            Map<Integer, Integer> playerGunLibraryDrawCountMap = userData.getServerOnly().getChestOpenIndexMap().getGunLibraryDrawCountMap();
            int drawCount = 0;
            if (gunLibraryType.getType() == GunLibraryType.Common.getType() || gunLibraryType.getType() == GunLibraryType.Random.getType()) {
                drawCount = playerGunLibraryDrawCountMap.getOrDefault(GunLibraryType.Common.getType(), 0) + playerGunLibraryDrawCountMap.getOrDefault(GunLibraryType.Random.getType(), 0);
            } else {
                drawCount = playerGunLibraryDrawCountMap.getOrDefault(gunLibraryType.getType(), 0);
            }

            ChestGunTableValue gunTableValueByDrawCount = null;
            for (ChestGunTableValue matchLibraryTypeAndChestLevelTableValue : matchGunLibraryTypeTableValues) {
                if (drawCount >= matchLibraryTypeAndChestLevelTableValue.getDrawCountRequires()) {
                    gunTableValueByDrawCount = matchLibraryTypeAndChestLevelTableValue;
                } else {
                    break;
                }
            }

            if (gunTableValueByDrawCount == null) {
                throw new BusinessException("无法在枪械类型库" + gunLibraryType.getType() + "中，找到对应抽取次数" + drawCount + "的条目");
            }

            chestGunTableValue = chestGunTableValue.getChestLevel() <= gunTableValueByDrawCount.getChestLevel() ? chestGunTableValue : gunTableValueByDrawCount;
        }

        if (chestGunTableValue == null) {
            throw new BusinessException("玩家" + userData.getUuid() + "请求开箱,无法在 ChestGun 表中,找到 library type: " + gunLibraryType.getType() + ",chest level " + chestLevel + " 条目. enableDrawCountRequires " + enableDrawCountRequires);
        }

        ChestOpenIndexMap chestOpenIndexMap = userData.getServerOnly().getChestOpenIndexMap();
        String key = gunLibraryType.getType() + "_" + chestLevel;
        Integer index = chestOpenIndexMap.getGunRewardIndexMap().getOrDefault(key, 0);

        List<Integer> rewardGunIds = flattenGetElementFromRewardArray(chestGunTableValue.getRewardGunIds(),
                chestGunTableValue.getFallbackRewardGunIds(),
                index,
                getGunCount);

        chestOpenIndexMap.getGunRewardIndexMap().put(key, index + getGunCount);

        //更新枪库抽取次数
        Map<Integer, Integer> gunLibraryDrawCountMap = chestOpenIndexMap.getGunLibraryDrawCountMap();
        gunLibraryDrawCountMap.put(gunLibraryType.getType(), gunLibraryDrawCountMap.getOrDefault(gunLibraryType.getType(), 0) + getGunCount);

        log.info("开箱枪,库类型 " + gunLibraryType.getType() + ", 宝箱等级 " + chestLevel + ", 获取枪数量 " + getGunCount + ". 结果 " + JSON.toJSONString(rewardGunIds));

        for (Integer gunId : rewardGunIds) {
            resultMap.put(gunId, resultMap.getOrDefault(gunId, 0) + 1);
        }

        return resultMap;
    }


//    @Override
//    public void populateSendToClientUserDataWithChestOpenResult(String uuid, ChestOpenResult chestOpenResult) {
//
//        UserData userData = GameEnvironment.userDataMap.get(uuid);
//
//        if (chestOpenResult.getCoin() != null&&chestOpenResult.getCoin()>0){
//
//        }
//    }

    public List<Integer> flattenGetElementFromRewardArray(List<Integer> rewards, List<Integer> fallbackRewards, Integer startIndex, Integer getCount) {

        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < getCount; i++) {

            int index = startIndex + i;
            if (index < rewards.size()) {
                result.add(rewards.get(index));
            } else {
                index %= fallbackRewards.size();
                result.add(fallbackRewards.get(index));
            }
        }

        return result;
    }

    public List<Integer> coinAndDiamondFlattenGetElementFromRewardArray(List<Integer> rewards, List<Integer> fallbackRewards, Integer startIndex, Integer getCount) {

        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < getCount; i++) {

            int index = startIndex + i;
            if (index < rewards.size()) {
                result.add(rewards.get(index));
            } else {
                index %= fallbackRewards.size();
                result.add(fallbackRewards.get(index));
            }
        }

        return result;
    }

    public CoinAndDiamondBO getCoinAndDiamondRewards(UserData userData, Integer chestType, Integer chestLevel, String gameVersion) {

        Map<String, ChestCoinDiamondTableValue> chestCoinDiamondTable = GameEnvironment.chestCoinDiamondTableMap.get(gameVersion);

        ChestCoinDiamondTableValue chestCoinDiamondTableValue = null;
        Set<String> keySet = chestCoinDiamondTable.keySet();
        for (String key : keySet) {
            ChestCoinDiamondTableValue tableValue = chestCoinDiamondTable.get(key);

            if (tableValue.getChestType().equals(chestType) && tableValue.getChestLevel().equals(chestLevel)) {
                chestCoinDiamondTableValue = tableValue;
                break;
            }
        }

        if (chestCoinDiamondTableValue == null) {
            throw new BusinessException("玩家" + userData.getUuid() + "请求开箱，无法在 ChestCoinDiamond 表中,找到 chest type: " + chestType + ",chest level" + chestLevel + "条目");
        }

        CoinAndDiamondBO result = new CoinAndDiamondBO();
        ChestOpenIndexMap chestOpenIndexMap = userData.getServerOnly().getChestOpenIndexMap();
        int coinAndDiamondRewardIndex = 0;

        if (chestOpenIndexMap.getCoinAndDiamondRewardIndex() != null) {
            coinAndDiamondRewardIndex = chestOpenIndexMap.getCoinAndDiamondRewardIndex();
        }

        List<Integer> coinList = coinAndDiamondFlattenGetElementFromRewardArray(chestCoinDiamondTableValue.getRewardCoinCount(),
                chestCoinDiamondTableValue.getFallbackRewardCoinCount(),
                coinAndDiamondRewardIndex,
                1);

        int coinRewardCount = coinList.get(0);

        List<Integer> diamondList = coinAndDiamondFlattenGetElementFromRewardArray(chestCoinDiamondTableValue.getRewardDiamondCount(),
                chestCoinDiamondTableValue.getFallbackRewardDiamondCount(),
                coinAndDiamondRewardIndex,
                1);

        int diamondRewardCount = diamondList.get(0);

        chestOpenIndexMap.setCoinAndDiamondRewardIndex(coinAndDiamondRewardIndex + 1);

        result.setCoin(coinRewardCount);
        result.setDiamond(diamondRewardCount);
        return result;
    }

    /**
     * 从枪械类型库中，抽出对应数量的枪械奖励，并将枪械id顺序放入结果中
     */
    @Override
    public Map<Integer, Integer> extractGunRewardsFromGunLibrary(UserData userData, GunLibraryType gunLibraryType, Integer chestLevel, Integer getGunCount, String gameVersion, Boolean enableDrawCountRequires, Map<Integer, Integer> resultMap) {
        getGunCount = getGunCount + (int) Math.ceil(getGunCount * 0.2);

        resultMap = CollectionUtils.isEmpty(resultMap) ? new HashMap<>() : resultMap;

        //确保数据存在
        obsoleteUserDataService.upgradePlayerChestOpenIndexMapData(userData);

        Map<String, ChestGunTableValue> chestGunTable = GameEnvironment.chestGunTableMap.get(gameVersion);
        ChestGunTableValue chestGunTableValue = null;

        List<ChestGunTableValue> matchGunLibraryTypeTableValues = enableDrawCountRequires ? new ArrayList<>() : null;
        for (String key : chestGunTable.keySet()) {
            ChestGunTableValue tableValue = chestGunTable.get(key);
            if (gunLibraryType.getType() == tableValue.getLibraryType()) {
                if (matchGunLibraryTypeTableValues != null) {
                    matchGunLibraryTypeTableValues.add(tableValue);
                }
                if (chestLevel == tableValue.getChestLevel()) {
                    chestGunTableValue = tableValue;
                }
            }
        }

        if (chestGunTableValue == null) {
            throw new BusinessException("玩家" + userData.getUuid() + "请求开箱,无法在 ChestGun 表中,找到 library type: " + gunLibraryType.getType() + ",chest level " + chestLevel + " 条目.");
        }

        //需要匹配在当前类型库中抽取次数
        if (matchGunLibraryTypeTableValues != null) {
            matchGunLibraryTypeTableValues.sort(new Comparator<ChestGunTableValue>() {
                @Override
                public int compare(ChestGunTableValue o1, ChestGunTableValue o2) {
                    return o1.getDrawCountRequires() - o2.getDrawCountRequires();
                }
            });
            Map<Integer, Integer> playerGunLibraryDrawCountMap = userData.getServerOnly().getChestOpenIndexMap().getGunLibraryDrawCountMap();
            int drawCount = 0;
            if (gunLibraryType.getType() == GunLibraryType.Common.getType() || gunLibraryType.getType() == GunLibraryType.Random.getType()) {
                drawCount = playerGunLibraryDrawCountMap.getOrDefault(GunLibraryType.Common.getType(), 0) + playerGunLibraryDrawCountMap.getOrDefault(GunLibraryType.Random.getType(), 0);
            } else {
                drawCount = playerGunLibraryDrawCountMap.getOrDefault(gunLibraryType.getType(), 0);
            }

            ChestGunTableValue gunTableValueByDrawCount = null;
            for (ChestGunTableValue matchLibraryTypeAndChestLevelTableValue : matchGunLibraryTypeTableValues) {
                if (drawCount >= matchLibraryTypeAndChestLevelTableValue.getDrawCountRequires()) {
                    gunTableValueByDrawCount = matchLibraryTypeAndChestLevelTableValue;
                } else {
                    break;
                }
            }

            if (gunTableValueByDrawCount == null) {
                throw new BusinessException("无法在枪械类型库" + gunLibraryType.getType() + "中，找到对应抽取次数" + drawCount + "的条目");
            }

            chestGunTableValue = chestGunTableValue.getChestLevel() <= gunTableValueByDrawCount.getChestLevel() ? chestGunTableValue : gunTableValueByDrawCount;
        }

        if (chestGunTableValue == null) {
            throw new BusinessException("玩家" + userData.getUuid() + "请求开箱,无法在 ChestGun 表中,找到 library type: " + gunLibraryType.getType() + ",chest level " + chestLevel + " 条目. enableDrawCountRequires " + enableDrawCountRequires);
        }

        ChestOpenIndexMap chestOpenIndexMap = userData.getServerOnly().getChestOpenIndexMap();
        String key = gunLibraryType.getType() + "_" + chestLevel;
        Integer index = chestOpenIndexMap.getGunRewardIndexMap().getOrDefault(key, 0);

        List<Integer> rewardGunIds = flattenGetElementFromRewardArray(chestGunTableValue.getRewardGunIds(),
                chestGunTableValue.getFallbackRewardGunIds(),
                index,
                getGunCount);

        chestOpenIndexMap.getGunRewardIndexMap().put(key, index + getGunCount);

        //更新枪库抽取次数
        Map<Integer, Integer> gunLibraryDrawCountMap = chestOpenIndexMap.getGunLibraryDrawCountMap();
        gunLibraryDrawCountMap.put(gunLibraryType.getType(), gunLibraryDrawCountMap.getOrDefault(gunLibraryType.getType(), 0) + getGunCount);

        log.info("开箱枪,库类型 " + gunLibraryType.getType() + ", 宝箱等级 " + chestLevel + ", 获取枪数量 " + getGunCount + ". 结果 " + JSON.toJSONString(rewardGunIds));

        for (Integer gunId : rewardGunIds) {
            resultMap.put(gunId, resultMap.getOrDefault(gunId, 0) + 1);
        }

        return resultMap;
    }

    private void getBulletRewardsAsync(UserData userData, BulletLibraryType bulletLibraryType, Integer level, Integer randomBulletCount, String gameVersion, Map<Integer, Integer> rewardBulletCountMap) {
        Map<String, ChestBulletTableValue> chestBulletTable = GameEnvironment.chestBulletTableMap.get(gameVersion);
        ChestBulletTableValue chestBulletTableValue = null;
        for (String key : chestBulletTable.keySet()) {
            ChestBulletTableValue tableValue = chestBulletTable.get(key);
            if (Objects.equals(bulletLibraryType.getType(), tableValue.getLibraryType()) && Objects.equals(tableValue.getChestLevel(), level)) {
                chestBulletTableValue = tableValue;
                break;
            }
        }

        if (chestBulletTableValue == null) {
            throw new BusinessException("玩家" + userData.getUuid() + "请求开箱,无法在 ChestBullet 表中,找到 library type:" + bulletLibraryType + ", chest level" + level + "条目");
        }

        ChestOpenIndexMap chestOpenIndexMap = userData.getServerOnly().getChestOpenIndexMap();
        String key = bulletLibraryType.getType() + "_" + level;
        Integer index = chestOpenIndexMap.getBulletRewardIndexMap().getOrDefault(key, 0);
        List<Integer> rewardBulletIds = flattenGetElementFromRewardArray(chestBulletTableValue.getRewardBulletIds(), chestBulletTableValue.getFallbackRewardBulletIds(), index, randomBulletCount);

        chestOpenIndexMap.getBulletRewardIndexMap().put(key, index + randomBulletCount);

        log.info("开箱子弹,库类型 " + bulletLibraryType.getType() + ", 宝箱等级 " + level + ", 获取枪数量 " + randomBulletCount + ". 结果 " + JSON.toJSONString(rewardBulletIds));

//        if (rewardBulletCountMap==null) {
//            rewardBulletCountMap = new HashMap<>();
//        }

        for (Integer bulletId : rewardBulletIds) {
            if (bulletId == 0) {
                continue;
            }
            if (rewardBulletCountMap.get(bulletId) == null) {
                rewardBulletCountMap.put(bulletId, 1);
            } else {
                rewardBulletCountMap.put(bulletId, rewardBulletCountMap.get(bulletId) + 1);
            }
        }

    }

}
