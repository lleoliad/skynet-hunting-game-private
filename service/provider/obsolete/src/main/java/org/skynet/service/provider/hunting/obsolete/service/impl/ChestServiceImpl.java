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

        //?????????????????????????????????????????????2??????????????????????????????????????????????????????????????????????????????4??????
        int playerHighestUnlockedChapterId = obsoleteUserDataService.playerHighestUnlockedChapterID(userData);
        int freeChestUnlockSeconds = playerHighestUnlockedChapterId >= 3 ? GameConfig.longFreeChestUnlockSeconds : GameConfig.shortFreeChestUnlockSeconds;

        //????????????
        if (userData.getFreeChestsData() == null || userData.getFreeChestsData().length == 0) {

            userData.setFreeChestsData(new FreeChestData[2]);
        }

        FreeChestData[] freeChestsData = userData.getFreeChestsData();

        //?????????????????????,?????????????????????,??????????????????????????????
        //?????????,????????????????????????????????????,??????????????????????????????????????????
        Boolean createNewChest = false;
        if (freeChestsData[0] == null && freeChestsData[1] != null) {
            freeChestsData[0] = freeChestsData[1];
            freeChestsData[1] = null;
            createNewChest = true;
        }

        //????????????
        if (freeChestsData[0] == null) {
            freeChestsData[0] = createNewFreeChest(userData, unixTimeNow + freeChestUnlockSeconds);
            log.info("??????????????????at 0???" + JSONObject.toJSONString(freeChestsData[0]));
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
            log.info("??????????????????at 1???" + JSONObject.toJSONString(freeChestsData[1]));
        }


    }

    /**
     * ???????????????????????????
     *
     * @param userData
     * @param availableTime
     * @return
     */
    @Override
    public FreeChestData createNewFreeChest(UserData userData, Long availableTime) {

        //??????????????????????????????
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
//     * ??????????????????????????????
//     */
//    @Override
//    public void refreshChapterProgressChestData(String uuid) {
//
//        UserData userData = GameEnvironment.userDataMap.get(uuid);
//        //????????????,???????????????
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
//            //??????????????????,?????????????????????
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
//                throw new BusinessException("??????"+userData.getUuid()+"??????????????????,???index"+chapterChestTaskIndex+"??????????????????????????????");
//            }
//
//            ChestType chestType = chapterChestTaskValue.getChestType();
//            Integer chestLevel = userDataService.playerHighestUnlockedChapterID(userData);
//
//            userData.setChapterProgressChestsData(new ChapterProgressChestData(NanoIdUtils.randomNanoId(30),chestType.getType(),chestLevel,TimeUtils.getUnixTimeSecond(),0,chapterChestTaskValue.getMatchCompleteRequires()));
//
//            userData.getServerOnly().setChapterChestTaskIndex(chapterChestTaskIndex);
//
//            log.info("????????????????????????"+userData.getChapterProgressChestsData());
//
//        }
//    }

    /**
     * ??????????????????????????????
     *
     * @param chapterId
     * @param uuid
     * @return
     */
    @Override
    public ChapterWinChestData tryCreateChapterWinChestAsync(Integer chapterId, String uuid) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);

        //??????????????????


        return null;
    }

    /**
     * ????????????????????????????????????
     *
     * @param uuid
     * @return
     */
    @Override
    public Integer getChapterWinChestSlotInfo(String uuid) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);

        //??????????????????
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
     * ??????????????????????????????
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

        //??????????????????,????????????????????????,???????????????1
        int winChestIndex = Math.max(0, chapterWinCount - 1);

        //??????????????????,???????????????????????????????????????,???????????????????????????
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

        //??????????????????
        Integer emptySlotIndex = getChapterWinChestSlotInfo(uuid);

        //????????????,???????????????,????????????:http://192.168.2.45/huntingrival/huntingrival-client/-/issues/154
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
//        //?????????????????????????????????,????????????????????????????????????,????????????,?????????
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
//        log.info("??????"+chapterId+"??????????????????????????????"+chapterWinChestProgressData.getCurrentProgress()+"/"+chapterWinChestProgressData.getProgressMax());
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
    public ChestOpenResult openChest(UserData userData, ChestData chestData, String gameVersion, float additionValue) {

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

            throw new BusinessException("??????" + userData.getUuid() + "????????????,?????????ChestContentMapTableValue??????,??????" + chestData + "???????????????");
        }

        if (chestContentMapTableValue.getRandomBulletCount() > 0
                && (chestContentMapTableValue.getHighChanceBulletCount() > 0 || chestContentMapTableValue.getLowChanceBulletCount() > 0)) {
            throw new BusinessException("??????" + userData.getUuid() + "????????????,??????" + JSONObject.toJSONString(chestData) + "???????????????,??????randomBulletCount/highChanceBulletCount/lowChanceBulletCount?????????????????????0");
        }

        //????????????
        // int[] playerChestRewardAddition = obsoleteUserDataService.getPlayerChestRewardAddition(userData.getUuid());
        ChestOpenResult chestOpenResult = new ChestOpenResult();
        chestOpenResult.setChestData(chestData);

        Map<String, ChestTableValue> chestTable = GameEnvironment.chestTableMap.get(gameVersion);
        Boolean enableDrawCountRequires = chestTable.get(chestData.getChestType().toString()).getEnableRewardLibraryDrawCountRequires();


        //gun
        Map<Integer, Integer> gunRewardMap = new HashMap<>();
        if (chestContentMapTableValue.getEpicGunCount() > 0) {
            gunRewardMap = extractGunRewardsFromGunLibrary(userData, GunLibraryType.Epic, chestData.getLevel(), chestContentMapTableValue.getEpicGunCount(), gameVersion, enableDrawCountRequires, gunRewardMap, additionValue);
        }

        if (chestContentMapTableValue.getRareGunCount() > 0) {
            gunRewardMap = extractGunRewardsFromGunLibrary(userData, GunLibraryType.Rare, chestData.getLevel(), chestContentMapTableValue.getRareGunCount(), gameVersion, enableDrawCountRequires, gunRewardMap, additionValue);
        }
        if (chestContentMapTableValue.getRandomGunCount() > 0) {
            gunRewardMap = extractGunRewardsFromGunLibrary(userData, GunLibraryType.Random, chestData.getLevel(), chestContentMapTableValue.getRandomGunCount(), gameVersion, enableDrawCountRequires, gunRewardMap, additionValue);
        }

        if (gunRewardMap.size() > 0) {
            chestOpenResult.setGunRewards(new ArrayList<>());
            Set<Integer> keySet1 = gunRewardMap.keySet();
            for (Integer key : keySet1) {
                chestOpenResult.getGunRewards().add(new GunReward(key, gunRewardMap.get(key)));
            }

            //?????????????????????????????????
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

        log.info("????????????" + chestOpenResult);
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
            throw new BusinessException("??????" + userData.getUuid() + "????????????,????????? ChestGun ??????,?????? library type: " + gunLibraryType + ", chest level" + chestLevel + "??????");
        }

        ChestOpenIndexMap chestOpenIndexMap = userData.getServerOnly().getChestOpenIndexMap();
        String key = gunLibraryType + "_" + chestLevel;

        int index = chestOpenIndexMap.getGunRewardIndexMap().getOrDefault(key, 0);

        List<Integer> rewardGunIds = flattenGetElementFromRewardArray(chestGunTableValue.getRewardGunIds(),
                chestGunTableValue.getFallbackRewardGunIds(),
                index,
                getGunCount);

        chestOpenIndexMap.getGunRewardIndexMap().put(key, index + getGunCount);

        log.info("?????????,????????? " + gunLibraryType + ", ???????????? " + chestLevel + ", ???????????????" + getGunCount + ".??????" + JSONObject.toJSONString(rewardGunIds));

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

            throw new BusinessException("??????" + userData.getUuid() + "????????????,????????? ChestBullet ??????,?????? library type: " + bulletLibraryType + ",chest level" + chestLevel + "??????");
        }

        ChestOpenIndexMap chestOpenIndexMap = userData.getServerOnly().getChestOpenIndexMap();
        String key = bulletLibraryType + "_" + chestLevel;
        int index = chestOpenIndexMap.getBulletRewardIndexMap().getOrDefault(key, 0);

        List<Integer> rewardBulletIds = flattenGetElementFromRewardArray(chestBulletTableValue.getRewardBulletIds(), chestBulletTableValue.getFallbackRewardBulletIds(), index, getBulletCount);

        chestOpenIndexMap.getBulletRewardIndexMap().put(key, index + getBulletCount);

        log.info("????????????????????????" + bulletLibraryType + ",????????????" + chestLevel + ",????????????" + getBulletCount + ".??????" + rewardBulletIds);
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
    public Map<Integer, Integer> extractGunRewardsFromGunLibraryAsync(UserData userData, GunLibraryType gunLibraryType, int chestLevel, int getGunCount, boolean enableDrawCountRequires, Map<Integer, Integer> resultMap, String gameVersion, float additionValue) {
        getGunCount = getGunCount + (int) Math.ceil(getGunCount * additionValue);
        //??????????????????
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
            throw new BusinessException("??????" + userData.getUuid() + "????????????,????????? ChestGun ??????,?????? library type: " + gunLibraryType.getType() + ",chest level " + chestLevel + " ??????.");
        }

        //?????????????????????????????????????????????
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
                throw new BusinessException("????????????????????????" + gunLibraryType.getType() + "??????????????????????????????" + drawCount + "?????????");
            }

            chestGunTableValue = chestGunTableValue.getChestLevel() <= gunTableValueByDrawCount.getChestLevel() ? chestGunTableValue : gunTableValueByDrawCount;
        }

        if (chestGunTableValue == null) {
            throw new BusinessException("??????" + userData.getUuid() + "????????????,????????? ChestGun ??????,?????? library type: " + gunLibraryType.getType() + ",chest level " + chestLevel + " ??????. enableDrawCountRequires " + enableDrawCountRequires);
        }

        ChestOpenIndexMap chestOpenIndexMap = userData.getServerOnly().getChestOpenIndexMap();
        String key = gunLibraryType.getType() + "_" + chestLevel;
        Integer index = chestOpenIndexMap.getGunRewardIndexMap().getOrDefault(key, 0);

        List<Integer> rewardGunIds = flattenGetElementFromRewardArray(chestGunTableValue.getRewardGunIds(),
                chestGunTableValue.getFallbackRewardGunIds(),
                index,
                getGunCount);

        chestOpenIndexMap.getGunRewardIndexMap().put(key, index + getGunCount);

        //????????????????????????
        Map<Integer, Integer> gunLibraryDrawCountMap = chestOpenIndexMap.getGunLibraryDrawCountMap();
        gunLibraryDrawCountMap.put(gunLibraryType.getType(), gunLibraryDrawCountMap.getOrDefault(gunLibraryType.getType(), 0) + getGunCount);

        log.info("?????????,????????? " + gunLibraryType.getType() + ", ???????????? " + chestLevel + ", ??????????????? " + getGunCount + ". ?????? " + JSON.toJSONString(rewardGunIds));

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
            throw new BusinessException("??????" + userData.getUuid() + "???????????????????????? ChestCoinDiamond ??????,?????? chest type: " + chestType + ",chest level" + chestLevel + "??????");
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
     * ????????????????????????????????????????????????????????????????????????id?????????????????????
     */
    @Override
    public Map<Integer, Integer> extractGunRewardsFromGunLibrary(UserData userData, GunLibraryType gunLibraryType, Integer chestLevel, Integer getGunCount, String gameVersion, Boolean enableDrawCountRequires, Map<Integer, Integer> resultMap, float additionValue) {
        getGunCount = getGunCount + (int) Math.ceil(getGunCount * additionValue);

        resultMap = CollectionUtils.isEmpty(resultMap) ? new HashMap<>() : resultMap;

        //??????????????????
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
            throw new BusinessException("??????" + userData.getUuid() + "????????????,????????? ChestGun ??????,?????? library type: " + gunLibraryType.getType() + ",chest level " + chestLevel + " ??????.");
        }

        //?????????????????????????????????????????????
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
                throw new BusinessException("????????????????????????" + gunLibraryType.getType() + "??????????????????????????????" + drawCount + "?????????");
            }

            chestGunTableValue = chestGunTableValue.getChestLevel() <= gunTableValueByDrawCount.getChestLevel() ? chestGunTableValue : gunTableValueByDrawCount;
        }

        if (chestGunTableValue == null) {
            throw new BusinessException("??????" + userData.getUuid() + "????????????,????????? ChestGun ??????,?????? library type: " + gunLibraryType.getType() + ",chest level " + chestLevel + " ??????. enableDrawCountRequires " + enableDrawCountRequires);
        }

        ChestOpenIndexMap chestOpenIndexMap = userData.getServerOnly().getChestOpenIndexMap();
        String key = gunLibraryType.getType() + "_" + chestLevel;
        Integer index = chestOpenIndexMap.getGunRewardIndexMap().getOrDefault(key, 0);

        List<Integer> rewardGunIds = flattenGetElementFromRewardArray(chestGunTableValue.getRewardGunIds(),
                chestGunTableValue.getFallbackRewardGunIds(),
                index,
                getGunCount);

        chestOpenIndexMap.getGunRewardIndexMap().put(key, index + getGunCount);

        //????????????????????????
        Map<Integer, Integer> gunLibraryDrawCountMap = chestOpenIndexMap.getGunLibraryDrawCountMap();
        gunLibraryDrawCountMap.put(gunLibraryType.getType(), gunLibraryDrawCountMap.getOrDefault(gunLibraryType.getType(), 0) + getGunCount);

        log.info("?????????,????????? " + gunLibraryType.getType() + ", ???????????? " + chestLevel + ", ??????????????? " + getGunCount + ". ?????? " + JSON.toJSONString(rewardGunIds));

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
            throw new BusinessException("??????" + userData.getUuid() + "????????????,????????? ChestBullet ??????,?????? library type:" + bulletLibraryType + ", chest level" + level + "??????");
        }

        ChestOpenIndexMap chestOpenIndexMap = userData.getServerOnly().getChestOpenIndexMap();
        String key = bulletLibraryType.getType() + "_" + level;
        Integer index = chestOpenIndexMap.getBulletRewardIndexMap().getOrDefault(key, 0);
        List<Integer> rewardBulletIds = flattenGetElementFromRewardArray(chestBulletTableValue.getRewardBulletIds(), chestBulletTableValue.getFallbackRewardBulletIds(), index, randomBulletCount);

        chestOpenIndexMap.getBulletRewardIndexMap().put(key, index + randomBulletCount);

        log.info("????????????,????????? " + bulletLibraryType.getType() + ", ???????????? " + level + ", ??????????????? " + randomBulletCount + ". ?????? " + JSON.toJSONString(rewardBulletIds));

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
