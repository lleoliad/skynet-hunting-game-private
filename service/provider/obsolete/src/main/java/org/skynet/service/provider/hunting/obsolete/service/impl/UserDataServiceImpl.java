package org.skynet.service.provider.hunting.obsolete.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.extern.slf4j.Slf4j;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.*;
import org.skynet.service.provider.hunting.obsolete.config.*;
import org.skynet.service.provider.hunting.obsolete.enums.*;
import org.skynet.service.provider.hunting.obsolete.module.rank.entity.ClientRecord;
import org.skynet.service.provider.hunting.obsolete.module.rank.service.RankService;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.InitUserDataBO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.BaseDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.DeleteGUNDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.UpdateGUNDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.UpdatePropDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.*;
import org.skynet.service.provider.hunting.obsolete.service.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.xml.bind.DatatypeConverter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserDataServiceImpl implements UserDataService {

    private static final int min = 10000;
    private static final int max = 100000;
    //该时间之后注册的用户,会按照配置分配到对应的AB TEST GROUP中,否则默认在ABTestGroup.A中
    private static final String ABTestEffectUserSingInAfterDateTime = "2022-05-23";
    private static final long maxHashNumber = 4294967295L;

    @Resource
    private HuntingMatchService huntingMatchService;

    @Resource
    private ChestService chestService;

    @Resource
    private ChapterBonusPackageDataService chapterBonusPackageDataService;

    @Resource
    private PromotionEventPackageDataService promotionEventPackageDataService;

    @Resource
    private SigninDiamondRewardTableService signinDiamondRewardTableService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private WeaponService weaponService;

    @Resource
    private LuckyWheelService luckyWheelService;

    @Resource
    private RankService rankService;

    @Override
    public Integer userDataSettlement(UserData userData, UserDataSendToClient sendToClientData, Boolean incrementUpdateCount, String gameVersion) throws IllegalAccessException {

        int updateCount = userData.getUpdateCount();

        GameEnvironment.userDataMap.remove(userData.getUuid());

        if (incrementUpdateCount) {
            userData.setUpdateCount(++updateCount);
        }
        userData.getServerOnly().setLastLoginClientVersion(gameVersion);
        saveUserData(userData);

        if (sendToClientData.getHistory() != null) {
            History history = sendToClientData.getHistory();
            CommonUtils.responseRemoveServer(history);
        }
        if (sendToClientData.getAdvertisementData() != null) {
            PlayerAdvertisementData advertisementData = sendToClientData.getAdvertisementData();
            CommonUtils.responseRemoveServer(advertisementData);
        }
        if (sendToClientData.getVipData() != null) {
            PlayerVipData vipData = sendToClientData.getVipData();
            CommonUtils.responseRemoveServer(vipData);
        }

        sendToClientData.setUpdateCount(userData.getUpdateCount());
        return updateCount;
    }

    @Override
    public UserData createDefaultUserData(Long userId, String uuid, String gameVersion) {

        //玩家引导数据
        PlayerTutorialData tutorialData = new PlayerTutorialData(new HashMap<>());
        //玩家历史数据
        History history = new History(
                0L,
                0L,
                0L,
                0,
                0,
                0,
                0,
                0D,
                0,
                0,
                0,
                0,
                0D,
                0,
                0,
                0D);

        //宝箱内容表格index
        ChestOpenIndexMap chestOpenIndexMap = new ChestOpenIndexMap(new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), 0);

        ServerOnly serverOnly = new ServerOnly(
                ABTestGroup.A,
                NanoIdUtils.randomNanoId(24),
                null,
                "",
                chestOpenIndexMap,
                1,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                0L,
                0,
                null,
                -1,
                -1);

        FreeChestData[] freeChestDataList = new FreeChestData[2];
        freeChestDataList[0] = null;
        freeChestDataList[1] = null;
        LinkedAuthProviderData linkedAuthProviderData = new LinkedAuthProviderData("", "");

        UserData userData = new UserData(
                null,
                userId,
                0,
                TimeUtils.getUnixTimeSecond(),
                createGuestName(""),
                uuid,
                GameConfig.defaultCoinAmount,
                GameConfig.defaultDiamondAmount,
                0,
                tutorialData,
                new LinkedHashMap<>(),
                new ArrayList<>(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                GameConfig.defaultGunID,
                new LinkedHashMap<>(),
                new LinkedHashMap<>(),
                GameConfig.defaultBulletID,
                new LinkedHashMap<>(),
                new ArrayList<>(),
                freeChestDataList,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new HashMap<>(),
                -1,
                linkedAuthProviderData,
                serverOnly,
                history,
                new ArrayList<>(),
                createDefaultLuckyWheelData(gameVersion),
                createDefaultLuckyWheelV2Data(gameVersion),
                createDefaultPlayerAdvertisementData(),
                createDefaultPlayerVipData(),
                createDefaultPlayerVipV2Data(),
                createDefaultPlayerVipV3Data(),
                createDefaultPlayerRankData(),
                false,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                false,
                0
        );

        for (Integer defaultUnlockChapterID : GameConfig.defaultUnlockChapterIDsArray) {

            userData.getUnlockedChapterIds().add(defaultUnlockChapterID);
        }

        userData.getBulletCountMap().put(1, 0);

        userData.setEquippedGunId(GameConfig.defaultGunID);
        userData.setEquippedBulletId(GameConfig.defaultBulletID);

        userData.getGunLevelMap().put(userData.getEquippedGunId(), 1);
        userData.getGunCountMap().put(userData.getEquippedGunId(), 1);

        userData.getHistory().setTotalEarnedCoin(userData.getCoin());

        for (int i = 0; i < ChapterWinChestConfig.ChestSlotAmount; i++) {

            userData.getChapterWinChestsData().add(i, null);
        }

        return userData;
    }


    public PlayerRankData createDefaultPlayerRankData() {
        return new PlayerRankData(null,
                null,
                -1,
                -1, -1, -1,
                new ClientRecord(false, false), -1, -1L, -1);
    }

    @Override
    public PlayerVipData createDefaultPlayerVipData() {
        return new PlayerVipData(-1L,
                -1L,
                -1L,
                -1L,
                TimeUtils.getStandardTimeDay(),
                -1L,
                -1L);
    }

    @Override
    public PlayerVipV2Data createDefaultPlayerVipV2Data() {
        return new PlayerVipV2Data(-1L,
                -1L,
                -1L,
                -1L);
    }


    @Override
    public PlayerVipV3Data createDefaultPlayerVipV3Data() {
        return new PlayerVipV3Data(-1L,
                -1L,
                -1L,
                -1L);
    }


    @Override
    public PlayerAdvertisementData createDefaultPlayerAdvertisementData() {
        return new PlayerAdvertisementData(GameConfig.eachDayWatchRewardAdMaxCount, TimeUtils.getStandardTimeDay());
    }

    /**
     * 创建默认的幸运转盘
     *
     * @param gameVersion
     * @return
     */
    @Override
    public LuckyWheelData createDefaultLuckyWheelData(String gameVersion) {

        return new LuckyWheelData(TimeUtils.getStandardTimeDay(),
                1,
                GameEnvironment.luckyWheelPropertyTableMap.get(gameVersion).getDefaultFreeSpinCount(),
                -1L,
                0,
                0,
                0);
    }

    /**
     * 创建默认的幸运转盘V2
     *
     * @param gameVersion
     * @return
     */
    @Override
    public LuckyWheelV2Data createDefaultLuckyWheelV2Data(String gameVersion) {
        LuckyWheelV2PropertyTableValue luckyWheelProperty = GameEnvironment.luckyWheelV2PropertyTableMap.get(gameVersion);
        Integer randomSectorContentId = luckyWheelService.getRandomLuckyWheelV2SectorContentId(gameVersion);
        return new LuckyWheelV2Data(TimeUtils.getStandardTimeDay(),
                randomSectorContentId,
                luckyWheelProperty.getDefaultFreeSpinCount(),
                -1L,
                0);
    }


    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void saveUserData(UserData userData) {

        RedisDBOperation.insertUserData(userData);
    }


    /**
     * 让用户数据中没有初始化的数据初始化
     *
     * @param userData    玩家
     * @param gameVersion
     */
    @Override
    public UserData upgradeUser(UserData userData, String gameVersion) {

        if (userData.getSignUpTime() == null) {
            userData.setSignUpTime(TimeUtils.getUnixTimeSecond());
        }

        //初始化chapterWinChestsData
        if (userData.getChapterWinChestsData() == null) {
            for (int i = 0; i < ChapterWinChestConfig.ChestSlotAmount; i++) {

                userData.getChapterWinChestsData().add(i, null);
            }
        }

        if (userData.getLinkedAuthProviderData() == null) {
            userData.setLinkedAuthProviderData(new LinkedAuthProviderData("", ""));
        }

        upgradePlayerHistoryData(userData);
        userData = upgradeUserAchievementData(userData, gameVersion);

        if (userData.getChapterBonusPackagesData() == null) {
            userData.setChapterBonusPackagesData(new ArrayList<>());
        }

        if (userData.getPromotionEventPackagesData() == null) {
            userData.setPromotionEventPackagesData(new ArrayList<>());
        }
        //转盘相关数据
        upgradePlayerLuckyWheelData(userData, gameVersion);

        //每次广告次数
        userData.setAdvertisementData(userData.getAdvertisementData() == null ? createDefaultPlayerAdvertisementData() : userData.getAdvertisementData());

        //vip
        userData.setVipData(userData.getVipData() == null ? createDefaultPlayerVipData() : userData.getVipData());

        ChestOpenIndexMap map = new ChestOpenIndexMap(new HashMap<>(), new HashMap<>(), new HashMap<>(), 0);

        //vipV2
        userData.setVipV2Data(userData.getVipV2Data() == null ? createDefaultPlayerVipV2Data() : userData.getVipV2Data());

        //已购买礼包数据
        if (userData.getAvailableBulletGiftPackageData() == null) {
            userData.setAvailableBulletGiftPackageData(new ArrayList<>());
        }
        if (userData.getAvailableFifthDayGunGiftPackageData() == null) {
            userData.setAvailableFifthDayGunGiftPackageData(new ArrayList<>());
        }
        if (userData.getAvailableGunGiftPackageData() == null) {
            userData.setAvailableGunGiftPackageData(new ArrayList<>());
        }

        //初始化serverOnly
        if (userData.getServerOnly() == null) {


            ServerOnly serverOnly = new ServerOnly(null,
                    null,
                    null,
                    null,
                    map,
                    null,
                    null,
                    new ArrayList<>(),
                    null,
                    null,
                    0L,
                    0,
                    null,
                    -1,
                    -1);

            userData.setServerOnly(serverOnly);
        } else {

            ServerOnly serverOnly = userData.getServerOnly();
            if (serverOnly.getPurchasedPromotionEventPackagesKeys() == null) {
                serverOnly.setPurchasedPromotionEventPackagesKeys(new ArrayList<>());
            }
            if (serverOnly.getChestOpenIndexMap() == null) {
                serverOnly.setChestOpenIndexMap(map);
            }

            if (serverOnly.getLatestMatchedAiProfileIds() == null) {
                serverOnly.setLatestMatchedAiProfileIds(new ArrayList<>());
            }
            if (serverOnly.getLastSigninDiamondRewardCollectTime() == null) {
                serverOnly.setLastSigninDiamondRewardCollectTime(0L);
            }
            if (serverOnly.getSigninDiamondRewardCollectTimes() == null) {
                serverOnly.setSigninDiamondRewardCollectTimes(0);
            }
        }

        return userData;
    }

    @Resource
    private RedisDBOperation redisDBOperation;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserDataVOService userDataVOService;

    /**
     * 创建新玩家
     *
     * @param gameVersion
     * @return UserData
     */
    @Override
    @Transactional
    public UserData createNewPlayer(String gameVersion) {
        int tryCount = 0;
        Long userId = redisDBOperation.getIncr("userAutoIncrementId");
        //    在redis中查不到对应id用户,那么就新建用户
        String newKey = "User:" + userId;
        try {
            while (tryCount < 10) {
                String newUUID = NanoIdUtils.randomNanoId(30);
                if (!RedisDBOperation.checkKeyExist("User:" + newUUID)) {

                    UserData newUserData = createDefaultUserData(userId, newUUID, gameVersion);

                    //向数据库里插入用户数据
                    // userDataVOService.insertUser(newUserData);// TODO 取消游戏服务器用户数据的创建

                    log.info("userData:{}", newUserData);

                    RedisDBOperation.insertUserData(newUserData);


                    return newUserData;
                }
                tryCount++;
            }

            throw new BusinessException("无法创建新用户,因为生成的所有uuid都被占用了");
        } catch (BusinessException e) {
            redisTemplate.delete(newKey);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 从redis中按照uuid检查
     *
     * @param uuid 用户的uuid
     * @return UserData
     */
    @Override
    public Boolean checkUserData(String uuid) {
        uuid = "User:" + uuid;

        return RedisDBOperation.selectUserData(uuid) != null;
    }


    @Override
    public UserData assignUserToABTestGroup(UserData userData) {

        Long effectTime = LocalDateTime.parse(ABTestEffectUserSingInAfterDateTime + "T00:00:00").toInstant(ZoneOffset.of("+8")).toEpochMilli() / 1000;
        if (userData.getSignUpTime() <= effectTime) {

            userData.getServerOnly().setAbTestGroup(ABTestGroup.A);
        } else {
            userData.getServerOnly().setAbTestGroup(userUidToABTestGroup(userData.getUuid()));
        }

        log.info("分配到AB TEST GROUP" + userData.getServerOnly().getAbTestGroup());

        return userData;
    }

    /**
     * 如果玩家数据不存在某些任务,则添加上
     *
     * @param userData    玩家
     * @param gameVersion
     * @return
     */
    @Override
    public UserData upgradeUserAchievementData(UserData userData, String gameVersion) {

        if (userData.getAchievements() == null) {
            List<AchievementData> list = new ArrayList<>();
            userData.setAchievements(list);
        }
        //加载任务
        Map<String, AchievementTableValue> achievementTable = GameEnvironment.achievementTableMap.get(gameVersion);

        //需要有的任务类型
        List<Integer> achievementTypesRequires = new ArrayList<>();
        Set<String> keySet = achievementTable.keySet();
        for (String key : keySet) {
            AchievementTableValue tableValue = achievementTable.get(key);
            if (!achievementTypesRequires.contains(tableValue.getAchievementType())) {
                achievementTypesRequires.add(tableValue.getAchievementType());
            }
        }

        //现在玩家有的任务类型
        List<Integer> achievementTypesNow = new ArrayList<>();
        for (AchievementData achievement : userData.getAchievements()) {
            Integer achievementId = achievement.getAchievementId();
            Integer achievementType = achievementTable.get(achievementId.toString()).getAchievementType();
            if (!achievementTypesNow.contains(achievementType)) {
                achievementTypesNow.add(achievementType);
            }
        }

        if (achievementTypesRequires.size() != achievementTypesNow.size()) {
            //需要添加的任务类型
            List<Integer> needAddAchievementTypes = new ArrayList<>();

            for (Integer typeRequires : achievementTypesRequires) {
                if (!achievementTypesNow.contains(typeRequires)) {
                    needAddAchievementTypes.add(typeRequires);
                }
            }
            log.info("需要添加任务类型" + needAddAchievementTypes.toString());

            if (needAddAchievementTypes.size() > 0) {
                for (Integer needAddType : needAddAchievementTypes) {
                    List<AchievementTableValue> thisTypeAchievementTableValue = new ArrayList<>();
                    for (String key : keySet) {
                        AchievementTableValue tableValue = achievementTable.get(key);
                        if (tableValue.getAchievementType().equals(needAddType)) {
                            thisTypeAchievementTableValue.add(tableValue);
                        }
                    }

                    thisTypeAchievementTableValue.sort(new Comparator<AchievementTableValue>() {
                        @Override
                        public int compare(AchievementTableValue a, AchievementTableValue b) {
                            return a.getLevel() - b.getLevel();
                        }
                    });

                    AchievementTableValue achievementFirstData = thisTypeAchievementTableValue.get(0);
                    AchievementData achievementData = new AchievementData(achievementFirstData.getId(), 0, achievementFirstData.getObjective(), false);
                    userData.getAchievements().add(achievementData);
                }
            }

            if (userData.getAchievements().size() > achievementTypesRequires.size()) {
                throw new BusinessException("玩家" + userData.getUuid() + "的成就数量" + userData.getAchievements() + "比表里面的类型个数" + achievementTypesRequires.size() + "还多");
            }
        }
        return userData;
    }

//    /**
//     * 为每个章节创建章节胜利宝箱进度数据
//     */
//    @Override
//    public void createAllChapterWinChestProgressDataAsync(String uuid) {
//
//        UserData userData = GameEnvironment.userDataMap.get(uuid);
//        Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap;
//        Map<Integer, ChapterWinChestProgressData> chapterWinChestProgressMap = userData.getChapterWinChestProgressMap();
//
//        if (chapterWinChestProgressMap==null){
//            chapterWinChestProgressMap = new LinkedHashMap<>();
//            userData.setChapterWinChestProgressMap(chapterWinChestProgressMap);
//        }
//
//        Set<String> keySet = chapterTable.keySet();
//        for (String key : keySet) {
//
//            ChapterTableValue chapterTableValue = chapterTable.get(key);
//
//            ChapterWinChestProgressData chapterWinChestProgressData = chapterWinChestProgressMap.get(chapterTableValue.getId());
//            if (chapterWinChestProgressData == null){
//
//                chapterWinChestProgressData = new ChapterWinChestProgressData(0,GameConfig.ChapterWinChestMaxProgress);
//                chapterWinChestProgressMap.put(chapterTableValue.getId(),chapterWinChestProgressData);
//            }
//        }
//
//        if (GameEnvironment._sendToClientUserData != null){
//
//            GameEnvironment._sendToClientUserData.setChapterWinChestProgressMap(chapterWinChestProgressMap);
//        }
//
//    }

    @Override
    public void resolveUnCompleteHuntingMatchAsync(String userUid, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);

        //读取章节数据
        Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(gameVersion);

        //获取比赛索引
        List<String> matchNowDataUids = huntingMatchService.getPlayerHuntingMatchNowData(userUid);
        if (matchNowDataUids == null || matchNowDataUids.size() == 0)
            return;
        //删除所有未完成的比赛，并判定为输
        for (String uid : matchNowDataUids) {

            String matchPath = Path.getHuntingMatchNowCollectionPath(gameVersion);
            //所有没有完成的比赛都认为是输了
            //不用扣钱，因为比赛开始的时候已经扣了
            HuntingMatchNowData huntingMatchNowData = huntingMatchService.getHuntingMatchNowData(matchPath, uid);
            if (huntingMatchNowData.getRecordModeSelectChapterId() != null) {
                huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(), chapterTable.get(String.valueOf(huntingMatchNowData.getRecordModeSelectChapterId())), false, gameVersion);
            } else {
                huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(), chapterTable.get(String.valueOf(huntingMatchNowData.getChapterId())), false, gameVersion);
            }

            // 游戏服务器不需要存储战报
            // String matchHistoryRef = Path.getHuntingMatchHistoryCollectionPath(gameVersion);
            //
            // HuntingMatchHistoryData huntingMatchHistoryData = new HuntingMatchHistoryData(gameVersion,
            //         huntingMatchNowData.getChapterId(),
            //         huntingMatchNowData.getMatchId(),
            //         huntingMatchNowData.getMatchUid(),
            //         huntingMatchNowData.getAiFetchedControlRecordsInfos().size(),
            //         -1, -1, new ArrayList<>(),
            //         huntingMatchNowData,
            //         huntingMatchNowData.getStartTime(),
            //         TimeUtils.getUnixTimeSecond());
            //
            // huntingMatchService.saveHuntingHistoryMatch(matchHistoryRef, huntingMatchNowData.getMatchUid(), huntingMatchHistoryData);

            huntingMatchService.removeHuntingMatchNowData(matchPath, userUid, uid);
        }

    }

    @Override
    public void checkUserDataExist(String uuid) {

        //处理userData
        String key = "User:" + uuid;
        UserData userData = RedisDBOperation.selectUserData(key);
        GameEnvironment.userDataMap.put(uuid, userData);
    }

    /**
     * 玩家最高已解锁的章节ID
     *
     * @param userData
     * @return
     */
    @Override
    public Integer playerHighestUnlockedChapterID(UserData userData) {

        Integer highestID = -1;
        for (Integer unlockedChapterId : userData.getUnlockedChapterIds()) {
            if (unlockedChapterId > highestID) {
                highestID = unlockedChapterId;
            }
        }
        return highestID;
    }

    @Override
    public Integer userDataTransaction(UserData userData, Boolean incrementUpdateCount, String gameVersion) {

        operationUserData(userData, gameVersion);
        if (incrementUpdateCount) {
            Integer updateCount = userData.getUpdateCount();
            userData.setUpdateCount(++updateCount);
        }

        RedisDBOperation.insertUserData(userData);

        return userData.getUpdateCount();
    }

    @Override
    public void operationUserData(UserData userData, String gameVersion) {

        userData = upgradeUser(userData, gameVersion);
        String uuid = userData.getUuid();
        //解决之前没有完成的比赛
        resolveUnCompleteHuntingMatchAsync(uuid, gameVersion);

        chestService.refreshFreeChestsData(uuid);

//        chestService.refreshChapterProgressChestData(uuid);

        chapterBonusPackageDataService.checkChapterBonusPackageExpire(uuid);

        //各类礼包
        promotionEventPackageDataService.refreshPromotionEventPackageNow(userData, gameVersion);
        promotionEventPackageDataService.refreshBulletGiftPackageNow(userData, true, gameVersion);
        promotionEventPackageDataService.refreshFifthDayGunGiftPackageDataNow(userData, true, gameVersion);
        promotionEventPackageDataService.refreshGunGiftPackageDataNow(userData, true, gameVersion);
        //刷新第二版活动礼包
        promotionEventPackageDataService.refreshPromotionEventPackageV2Now(userData, gameVersion);

        luckyWheelService.refreshLuckyWheelV1FreeSpinCount(userData.getUuid(), gameVersion);
        //确认玩家段位信息
        // rankService.getRewardInfo(userData, gameVersion);

        refreshVipV1LuckyWheelCount(userData);
        //清理玩家邮箱
        cleanUpPlayerInboxMails(userData.getUuid());
        refreshPlayerAdvertisementData(userData.getUuid());
    }

    @Override
    public void ensureUserDataIdempotence(String userUid, Integer clientUserDataUpdateCount, String gameVersion) {

        if (ClientGameVersion._1_0_8_.toString().compareTo(gameVersion) < 0) {
            return;
        }
        if (clientUserDataUpdateCount == null) {
            //throw `该方法必须上传玩家的user data update count`
            return;
        }
        UserData userData = RedisDBOperation.selectUserData("User:" + userUid);
        if (userData.getUpdateCount() != clientUserDataUpdateCount) {
            throw new BusinessException("同一个客户端请求服务器收到了两次，踢玩家下线。server:" + userData.getUpdateCount() + ", client" + clientUserDataUpdateCount);
        }
    }

    @Override
    public void refreshPlayerAdvertisementData(String userUid) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);
        PlayerAdvertisementData advertisementData = userData.getAdvertisementData();
        Long standardTimeDay = TimeUtils.getStandardTimeDay();
        if (standardTimeDay > advertisementData.getServer_only_lastRefreshRewardAdCountUnixDay()) {

            advertisementData.setRemainedRewardAdCountToday(GameConfig.eachDayWatchRewardAdMaxCount);
            advertisementData.setServer_only_lastRefreshRewardAdCountUnixDay(standardTimeDay);
            log.info("刷新今日广告次数" + JSONObject.toJSONString(advertisementData));
        }
    }

    @Override
    public void cleanUpPlayerInboxMails(String userUid) {

        long size = RedisDBOperation.selectInboxMailLength(userUid);
        if (size <= GameConfig.mailInboxCapacity) {
            return;
        }

        for (int i = GameConfig.mailInboxCapacity; i < size; i++) {

            MailData mailData = RedisDBOperation.deleteRedundancyMail(userUid);
            //存档
            ArchivedMailData archivedMailData = new ArchivedMailData(mailData, TimeUtils.getUnixTimeSecond());
            RedisDBOperation.insertArchivedMailData(mailData.getUid(), archivedMailData);
        }
    }

    @Override
    public void refreshVipLuckyWheelCount(String userUid) {
        UserData userData = GameEnvironment.userDataMap.get(userUid);

        PlayerVipData vipData = userData.getVipData();
        LuckyWheelData luckyWheelData = userData.getLuckyWheelData();
        Long standardTimeDay = TimeUtils.getStandardTimeDay();

        boolean[] playerVipStatus = getPlayerVipStatus(userData);

        boolean isVip = playerVipStatus[0];
        boolean isSVip = playerVipStatus[1];
        boolean luckyWheelVipSpinCountCleared = false;
        if (standardTimeDay > vipData.getServer_only_lastClearLuckyWheelVipSpinCountStandardTimeDay()) {
            luckyWheelVipSpinCountCleared = true;
            luckyWheelData.setVipSpinCount(0);
            vipData.setServer_only_lastClearLuckyWheelVipSpinCountStandardTimeDay(standardTimeDay);
        }

        int addSpinCount = 0;
        if (isVip && standardTimeDay > vipData.getServer_only_lastRefreshLuckyWheelVipSpinStandardTimeDay()) {
            addSpinCount += VipConfig.vipLuckyWheelVipSpinCount;
            vipData.setServer_only_lastRefreshLuckyWheelVipSpinStandardTimeDay(standardTimeDay);
        }

        if (isSVip && standardTimeDay > vipData.getServer_only_lastRefreshLuckyWheelSVipSpinStandardTimeDay()) {

            addSpinCount += VipConfig.svipLuckyWheelVipSpinCount;
            vipData.setServer_only_lastRefreshLuckyWheelSVipSpinStandardTimeDay(standardTimeDay);
        }

        if (addSpinCount > 0 || luckyWheelVipSpinCountCleared) {

            luckyWheelData.setVipSpinCount(luckyWheelData.getVipSpinCount() + addSpinCount);

            log.info("新vip转盘次数。vip: " + JSONObject.toJSONString(vipData) + ", lucky wheel:" + JSONObject.toJSONString(luckyWheelData));
        }

    }

    @Override
    public void tryLoginAsGuest(String userUid, String deviceId) {
        UserData userData = GameEnvironment.userDataMap.get(userUid);
        //如果登陆了任何平台,不注册游客账号
        if (userData.getLinkedAuthProviderData() != null &&
                (!StringUtils.isEmpty(userData.getLinkedAuthProviderData().getFacebookUserId()) || !StringUtils.isEmpty(userData.getLinkedAuthProviderData().getGooglePlayUserId()))) {
            return;
        }

        if (deviceId != null) {
            AccountMapData accountMapData = RedisDBOperation.selectGuestAccount(deviceId, userUid);
            if (accountMapData != null) {
                log.info("device id " + deviceId + " 已经绑定过账号了");
                return;
            }
        }
        AccountMapData accountMapData = new AccountMapData(deviceId, userUid);
        RedisDBOperation.insertGuestAccount(userUid, deviceId, accountMapData);
        log.info("device id " + deviceId + " 绑定账号" + userUid);
    }

    @Override
    public InitUserDataBO initUserData(UserData userData, String privateKey, String loginUserUid, BaseDTO request) {

        if (!userData.getServerOnly().getPrivateKey().equals(privateKey)) {
            throw new BusinessException("登陆用户" + loginUserUid + "private key 不符合");
        }

        userData.getServerOnly().setLatestLoginTime(TimeUtils.getUnixTimeSecond());
        userData.getServerOnly().setLastLoginClientVersion(request.getGameVersion());

        tryLoginAsGuest(userData.getUuid(), request.getDeviceId());

        //生成token
        String userToken = JwtUtils.createToken(loginUserUid);
        //数据库备份token
        RedisDBOperation.insertUserToken(userData.getUuid(), userToken, 3L);
        //将用户分到AB TEST GROUP
        userData = assignUserToABTestGroup(userData);

        if (request.getClientBuildInAppInfo().getRecordOnlyMode() != null && request.getClientBuildInAppInfo().getRecordOnlyMode()) {
            assignPlayerRecordModeMatch(loginUserUid, request.getGameVersion());
        }

        return new InitUserDataBO(userData, userToken);
    }


//    @Override
//    public CheckGunContentBO getPlayerEquippedGunTableValueByAnimalSize(UserData userData, AnimalSizeType animalSizeType) {
//
//        Integer equippedGunIds = userData.getEquippedGunId();
//
//        Map<String, GunTableValue> gunTable = GameEnvironment.gunTableValueMap;
//
//        for (Integer gunId : equippedGunIds) {
//
//            String key = String.valueOf(gunId);
//            GunTableValue gunTableValue = gunTable.get(key);
//            if (gunTableValue.getCaliberType().equals(animalSizeType.getStatus())){
//
//                Integer gunLevel = userData.getGunLevelMap().get(key);
//
//                return new CheckGunContentBO(gunTableValue,gunLevel);
//            }
//        }
//
//        return null;
//    }

//    @Override
//    public BulletTableValue getPlayerEquippedBulletTableValueByAnimalSize(UserData userData, AnimalSizeType animalSizeType) {
//
//        Map<String, BulletTableValue> bulletTable = GameEnvironment.bulletTableValueMap;
//
//        List<Integer> equippedBulletIds = userData.getEquippedBulletId();
//
//        for (Integer bulletId : equippedBulletIds) {
//
//            BulletTableValue bulletTableValue = bulletTable.get(String.valueOf(bulletId));
//            if (bulletTableValue.getCaliberType().equals(animalSizeType.getStatus())){
//
//                return bulletTableValue;
//            }
//        }
//
//        return null;
//    }


    /**
     * 创建游客名字
     *
     * @param exceptName
     * @return
     */
    public String createGuestName(String exceptName) {
        int tryCount = 0;

        String guestName = "";

        do {
            int randomNumber = (int) (Math.random() * (max - min + 1) + min);
            guestName = "Guest" + randomNumber;
            if (exceptName != null && exceptName.equals(guestName)) {
                tryCount++;
            } else {
                return guestName;
            }
        } while (tryCount < 10);

        return guestName;
    }

    @Override
    public void addGunToUserDataByIdAndCountArray(UserData userData, List<Integer> rewardGunIDsArray, List<Integer> rewardGunCountsArray, List<Integer> newUnlockedGunIDs, String gameVersion) {

        List<GunReward> gunIdCountData = new ArrayList<>();
        for (int i = 0; i < rewardGunIDsArray.size(); i++) {


            int gunId = rewardGunIDsArray.get(i);
            int gunCount = rewardGunCountsArray.get(i);

//            if (userData.getGunCountMap().get(gunId) == null){
//                userData.getGunCountMap().put(gunId,gunCount);
//                return;
//            }else {
//
//            }

            GunReward gunReward = new GunReward(gunId, (int) Math.ceil(gunCount * (1 + 0.2)));
            gunIdCountData.add(gunReward);
        }

        addGunToUserDataByGunIdCountData(userData, gunIdCountData, newUnlockedGunIDs, gameVersion);
    }

    @Override
    public void addBulletToUserDataByIdAndCountArray(UserData userData, List<Integer> rewardBulletIDsArray, List<Integer> rewardBulletCountArray) {

        if (rewardBulletIDsArray.size() != rewardBulletCountArray.size()) {

            throw new BusinessException("增加子弹 id和数量数组长度不一致: addBulletsId: " + rewardBulletIDsArray + ", addBulletsCount:" + rewardBulletCountArray);
        }

        List<BulletReward> bulletIdCountData = new ArrayList<>();

        for (int i = 0; i < rewardBulletIDsArray.size(); i++) {
            int bulletId = rewardBulletIDsArray.get(i);
            int bulletCount = rewardBulletCountArray.get(i);
            BulletReward bulletReward = new BulletReward(bulletId, bulletCount);
            bulletIdCountData.add(bulletReward);
        }

        addBulletToUserDataByIdCountData(userData, bulletIdCountData);
    }

    @Override
    public void mergeRewardGunsToChestOpenResult(ChestOpenResult chestOpenResult, List<Integer> gunsId, List<Integer> gunsCount) {

        List<Integer> allGunsId = new ArrayList<>();
        List<Integer> allGunsCount = new ArrayList<>();

        if (chestOpenResult.getGunRewards() != null) {

            for (GunReward gunReward : chestOpenResult.getGunRewards()) {
                allGunsId.add(gunReward.getGunId());
                allGunsCount.add((gunReward.getCount()));
            }
        }

        for (int i = 0; i < gunsId.size(); i++) {

            int gunId = gunsId.get(i);
            int gunCount = gunsCount.get(i);
            allGunsId.add(gunId);
            allGunsCount.add((int) Math.ceil(gunCount * (1 + 0.2)));
        }

        chestOpenResult.setGunRewards(new ArrayList<>());

        for (int i = 0; i < allGunsId.size(); i++) {

            int gunId = allGunsCount.get(i);
            int gunCount = allGunsCount.get(i);

            chestOpenResult.getGunRewards().add(new GunReward(gunId, gunCount));
        }

    }

    @Override
    public void mergeGunCountMapToChestOpenResult(ChestOpenResult chestOpenResult, Map<Integer, Integer> gunCountMap) {
        Map<Integer, Integer> chestOpenResultGunCountMap = CommonUtils.convertGunCountArrayToGunCountMap(chestOpenResult.getGunRewards());
        for (Map.Entry<Integer, Integer> entry : gunCountMap.entrySet()) {
            Integer gunId = entry.getKey();
            Integer gunCount = entry.getValue();

            Integer countValue = chestOpenResultGunCountMap.getOrDefault(gunId, 0) + (int) Math.ceil(gunCount * (1 + 0.2));
            chestOpenResultGunCountMap.put(gunId, countValue);
        }
        chestOpenResult.setGunRewards(CommonUtils.convertGunCountMapToGunCountArray(chestOpenResultGunCountMap));
    }

    @Override
    public void mergeRewardBulletsToChestOpenResult(ChestOpenResult chestOpenResult, List<Integer> bulletsId, List<Integer> bulletsCount) {
        if (bulletsId.size() != bulletsCount.size()) {
            throw new BusinessException("function mergeRewardBulletsToChestOpenResult parameter bulletsId.length != bulletsCount.length");
        }
        Map<Integer, Integer> bulletCountMap = CommonUtils.convertBulletCountArrayToBulletCountMap(chestOpenResult.getBulletRewards());
        for (int i = 0; i < bulletsId.size(); i++) {
            int id = bulletsId.get(i);
            int count = bulletsCount.get(i);

            if (bulletCountMap.get(id) == null) {
                bulletCountMap.put(id, count);
            } else {
                bulletCountMap.put(id, bulletCountMap.get(id) + count);
            }
        }
        chestOpenResult.setBulletRewards(CommonUtils.convertBulletCountMapToBulletCountArray(bulletCountMap));
    }

    @Override
    public void addBulletToUserDataByIdCountData(UserData userData, List<BulletReward> bulletIdCountData) {

        for (BulletReward data : bulletIdCountData) {

            if (!userData.getBulletCountMap().containsKey(data.getBulletId())) {
                userData.getBulletCountMap().put(data.getBulletId(), 0);
            }
            int count = userData.getBulletCountMap().get(data.getBulletId()) + data.getCount();
            userData.getBulletCountMap().put(data.getBulletId(), count);
        }
    }

    @Override
    public void addBulletToUserData(UserData userData, Map<Integer, Integer> bulletCountMap) {

        for (Map.Entry<Integer, Integer> entry : bulletCountMap.entrySet()) {
            Integer bulletId = entry.getKey();
            Integer bulletCount = entry.getValue();
            int count = userData.getBulletCountMap().getOrDefault(bulletId, 0) + bulletCount;
            userData.getBulletCountMap().put(bulletId, count);
        }
    }

    @Override
    public void addGunToUserData(UserData userData, Map<Integer, Integer> gunCountMap, List<Integer> newUnlockGunIdsResult, String gameVersion) {
        for (Map.Entry<Integer, Integer> entry : gunCountMap.entrySet()) {
            Integer gunId = entry.getKey();
            Integer gunCount = entry.getValue();

            Integer current = userData.getGunCountMap().getOrDefault(gunId, 0);
            userData.getGunCountMap().put(gunId, current + gunCount);
        }

        //检查是否是新获得的枪械
        if (newUnlockGunIdsResult != null) {
            Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
            for (Map.Entry<Integer, Integer> entry : gunCountMap.entrySet()) {
                Integer gunId = entry.getKey();
                if (!newUnlockGunIdsResult.contains(gunId) && !weaponService.isGunUnlocked(userData, gunId)) {

                    GunTableValue gunTableValue = gunTable.get(String.valueOf(gunId));
                    if (gunTableValue == null) {
                        throw new BusinessException("没有找到枪械" + gunId);
                    }

                    Integer gunCount = userData.getGunCountMap().get(gunId);
                    if (gunCount >= gunTableValue.getUnlockPiecesRequires()) {
                        userData.getGunCountMap().put(gunId, gunCount - gunTableValue.getUnlockPiecesRequires());
                        userData.getGunLevelMap().put(gunId, 1);
                        newUnlockGunIdsResult.add(gunId);
                    }
                }
            }
        }
    }

    @Override
    public UserData getUserData(String uid) {

        return RedisDBOperation.selectUserData(uid);
    }

    @Override
    public boolean checkUserIfBlocking(String uid) {

        UserData userData = getUserData("User:" + uid);

        if (userData.getServerOnly().getStartBlockTime() <= TimeUtils.getUnixTimeSecond() && userData.getServerOnly().getEndBlockTime() >= TimeUtils.getUnixTimeSecond()) {
            return true;
        }

        return false;
    }

    @Override
    public List<UserData> getAllUserData() {

        Set<String> set = RedisDBOperation.scan("User" + "*");
        List<UserData> userDataList = new ArrayList<>();
        for (String userUid : set) {
            try {
                userDataList.add(getUserData(userUid));
            } catch (Exception e) {
                log.warn(userUid);
            }

        }
        return userDataList;
    }

    @Override
    public String removeUserData(String userUid) {


        boolean exist = RedisDBOperation.checkKeyExist("User:" + userUid);
        if (!exist) {
            return userUid + "该用户不存在";
        }

        Boolean result = RedisDBOperation.deleteUserData(userUid);
        if (result) {
            return userUid + "删除成功";
        } else {
            return userUid + "删除失败";
        }
    }

    @Override
    public String updateUserData(UserData userData) {

        boolean exist = RedisDBOperation.checkKeyExist("User:" + userData.getUuid());
        if (!exist) {
            return "该用户不存在";
        }
        RedisDBOperation.insertUserData(userData);
        return "更新成功";
    }

    @Override
    public String logoutUserData(String userUid) {

        RedisDBOperation.deleteUserToken(userUid);
        return userUid + "已下线";
    }

    @Override
    public String blockUserData(String userUid, Long[] blockTime) {

        UserData userData = RedisDBOperation.selectUserData("User:" + userUid);
        userData.getServerOnly().setStartBlockTime(blockTime[0]);
        userData.getServerOnly().setEndBlockTime(blockTime[1]);
        saveUserData(userData);
        return userUid + "封禁成功";
    }

    @Override
    public String updateUserGun(UpdateGUNDTO dto) {

        String key = "User:" + dto.getUserUid();
        UserData userData = getUserData(key);

        List<Map<String, Integer>> gunLevelList = dto.getGunLevelList();

        for (Map<String, Integer> map : gunLevelList) {
            int gunId = map.get("gunId");
            int level = map.get("gunLevel");
            //枪支1不能操作，未拥有的枪支不能操作
            if (gunId != 1) {
                Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get("1.0.7"); //这里暂时设置的是1.0.7
                GunTableValue gunTableValue = gunTable.get(String.valueOf(gunId));
                if (gunTableValue.getMaxLevel() <= level) {
                    throw new BusinessException("枪支等级不能大于最高等级");
                }
                userData.getGunLevelMap().put(gunId, level);
                Map<Integer, Integer> gunCountMap = userData.getGunCountMap();
                if (!gunCountMap.containsKey(gunId)) {
                    userData.getGunCountMap().put(gunId, 0);
                }
            }
        }

        saveUserData(userData);
        return "用户枪械信息更新成功";
    }

    @Override
    public String deleteUserGun(DeleteGUNDTO dto) {

        String key = "User:" + dto.getUserUid();
        UserData userData = getUserData(key);
        for (Integer gunId : dto.getGunId()) {
            if (!userData.getGunCountMap().containsKey(gunId)) {
                log.error("用户" + dto.getUserUid() + "未拥有" + dto.getGunId() + "这把枪");
                return "用户" + dto.getUserUid() + "未拥有" + dto.getGunId() + "这把枪";
            }

            if (gunId.equals(1)) {
                return "默认枪支不能删除";
            }

            if (userData.getEquippedGunId().equals(gunId)) {
                log.error("用户" + dto.getUserUid() + "正在装备" + dto.getGunId() + "这把枪" + "已将其替换成默认枪");
                userData.setEquippedGunId(1);
            }

//        userData.getGunCountMap().remove(dto.getGunId());
            userData.getGunLevelMap().remove(gunId);
        }

        saveUserData(userData);
        return "用户枪支删除成功";
    }

    @Override
    public String updateProp(UpdatePropDTO dto) {

        String key = "User:" + dto.getUserUid();
        UserData userData = getUserData(key);

        if (dto.getCoin() != null) {
            userData.setCoin(dto.getCoin());
        }

        if (dto.getDiamond() != null) {
            userData.setDiamond(dto.getDiamond());
        }
        if (dto.getBulletCountMap() != null) {
            List<Map<String, Integer>> bulletCountMap = dto.getBulletCountMap();
            for (Map<String, Integer> map : bulletCountMap) {
                int bulletId = map.get("bulletId");
                int bulletCount = map.get("bulletCount");
                userData.getBulletCountMap().put(bulletId, bulletCount);
            }
        }
        if (dto.getGunCountMap() != null) {
            List<Map<String, Integer>> gunCountMap = dto.getGunCountMap();
            for (Map<String, Integer> map : gunCountMap) {
                int gunId = map.get("gunId");
                int gunCount = map.get("gunCount");
                userData.getGunCountMap().put(gunId, gunCount);
            }
        }
        saveUserData(userData);

        return "更新用户养成数据成功";
    }

    @Override
    public void assignPlayerRecordModeMatch(String userUid, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);
        if (userData.getServerOnly().getRecordModeData() != null) {
            return;
        }

        Map<String, RecordModeMatchTableValue> recordModeMatchTable = GameEnvironment.recordModeMatchTableMap.get(gameVersion);
        Collection<RecordModeMatchTableValue> values = recordModeMatchTable.values();
        int totalWeight = values.stream().mapToInt(RecordModeMatchTableValue::getWeight).sum();
        int randomWeight = NumberUtils.randomInt(0.0, (totalWeight + 1) * 1.0);
        int checkWeight = 0;
        RecordModeMatchTableValue targetRecordModeMatchTableValue = null;
        Set<String> keySet = recordModeMatchTable.keySet();
        for (String key : keySet) {

            RecordModeMatchTableValue tableValue = recordModeMatchTable.get(key);
            checkWeight += tableValue.getWeight();
            if (checkWeight >= randomWeight) {
                targetRecordModeMatchTableValue = tableValue;
                break;
            }
        }

        if (targetRecordModeMatchTableValue == null) {
            throw new BusinessException("无法分配玩家RecordModeMatchTable id");
        }

        log.info("无法分配玩家RecordModeMatchTable Id" + targetRecordModeMatchTableValue + JSONObject.toJSONString(targetRecordModeMatchTableValue));

        PlayerRecordModeData recordModeData = new PlayerRecordModeData(targetRecordModeMatchTableValue.getId(), 0);

        userData.getServerOnly().setRecordModeData(recordModeData);
    }

    @Override
    public boolean isForceTutorialStepComplete(String userUid, String forceStepName) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);
        return userData.getTutorialData().getForceTutorialStepStatusMap().containsKey(forceStepName);
    }

    @Override
    public double calculatePlayerCultivateScore(String userUid, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);
        Integer gunId = userData.getEquippedGunId();
        Integer gunLevel = userData.getGunLevelMap().get(gunId);
        Integer bulletId = userData.getEquippedBulletId();

        Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
        GunTableValue gunTableValue = gunTable.get(String.valueOf(gunId));

        if (gunLevel - 1 >= gunTableValue.getLevelCultivateScores().size()) {
            throw new BusinessException("gun level-1" + (gunLevel - 1) + "大于GunTable" + gunId + "中的levelCultivateScoresArray数组长度" +
                    JSONObject.toJSONString(gunTableValue.getLevelCultivateScores()));
        }

        Integer gunCultivateScore = gunTableValue.getLevelCultivateScores().get(gunLevel - 1);

        Map<String, BulletTableValue> bulletTable = GameEnvironment.bulletTableMap.get(gameVersion);
        BulletTableValue bulletTableValue = bulletTable.get(String.valueOf(bulletId));

        return gunCultivateScore * bulletTableValue.getCultivateScoreAddition();
    }

    @Override
    public Double calculatePlayerChapterWinRate(UserData userData, Integer chapterId) {

        int chapterEnteredCount = userData.getChapterEnteredCountMap().getOrDefault(chapterId, 0);
        int chapterWinCount = userData.getChapterWinCountMap().getOrDefault(chapterId, 0);

        //一次都没有进入过,胜率为0
        if (chapterEnteredCount == 0) {
            return 0d;
        }

        return chapterWinCount * 1.0 / chapterEnteredCount;
    }

    @Override
    public void addGunToUserDataByGunIdCountData(UserData userData, List<GunReward> gunIdCountData, List<Integer> newUnlockGunIdsResult, String gameVersion) {


        for (GunReward data : gunIdCountData) {
            if (!userData.getGunCountMap().containsKey(data.getGunId())) {
                userData.getGunCountMap().put(data.getGunId(), 0);
            }
            int count = userData.getGunCountMap().get(data.getGunId()) + data.getCount();
            userData.getGunCountMap().put(data.getGunId(), count);
        }

        //检查是否是新获得的枪械
        if (newUnlockGunIdsResult != null) {
            for (GunReward reward : gunIdCountData) {
                if (!newUnlockGunIdsResult.contains(reward.getGunId()) && !weaponService.isGunUnlocked(userData, reward.getGunId())) {

                    Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
                    GunTableValue gunTableValue = null;
                    if (gunTable.containsKey(String.valueOf(reward.getGunId()))) {
                        gunTableValue = gunTable.get(String.valueOf(reward.getGunId()));
                    }

                    if (gunTableValue == null) {
                        throw new BusinessException("没有找到枪械" + reward.getGunId());
                    }

                    int gunCount = userData.getGunCountMap().get(reward.getGunId());
                    if (gunCount >= gunTableValue.getUnlockPiecesRequires()) {
                        log.info("解锁枪械" + reward.getGunId());
                        int count = userData.getGunCountMap().get(reward.getGunId());
                        count -= gunTableValue.getUnlockPiecesRequires();
                        userData.getGunCountMap().put(reward.getGunId(), count);
                        userData.getGunLevelMap().put(reward.getGunId(), 1);
                        newUnlockGunIdsResult.add(reward.getGunId());
                    }
                }
            }
        }
    }

    /**
     * 根据玩家Uid,将玩家分到对应的ab test 组中
     *
     * @param uid uid
     * @return ABTestGroup
     */
    public ABTestGroup userUidToABTestGroup(String uid) {

        int hashNumber = uid.hashCode();
        int totalWeight = 0;

        for (ABTestGroupConfigs config : ABTestGroupConfigs.values()) {
            totalWeight += config.getWeight();
        }

        int rangeMax = 0;

        for (ABTestGroupConfigs config : ABTestGroupConfigs.values()) {

            rangeMax += maxHashNumber * (config.getWeight() / totalWeight);
            totalWeight += config.getWeight();
            if (hashNumber <= rangeMax) {
                return config.getGroup();
            }

        }
        log.error("无法正确分配玩家AB TEST GROUP,默认分配到A");
        return ABTestGroup.A;
    }

    @Override
    public void deleteGuestAccountData(UserData userData, String deviceId) {


        AccountMapData accountMapData = RedisDBOperation.selectGuestAccount(deviceId, userData.getUuid());

        if (accountMapData == null) {
            log.info("没有关联任何游客账号");
            return;
        }

        RedisDBOperation.deleteGuestAccount(deviceId, userData.getUuid());

        log.info("删除关联的游客账号:" + JSONObject.toJSONString(accountMapData));
    }

    @Override
    public void saveUserProfileImage(String imageUrl, String userUid) {

        if (StringUtils.isEmpty(imageUrl)) {
            return;
        }

        try {
            String response = HttpUtil.getString(imageUrl);
            byte[] bytes = response.getBytes();
            String imageBase64 = DatatypeConverter.printBase64Binary(bytes);
            RedisDBOperation.insertUserProfileImageCollection(imageBase64, userUid);
            log.info("保存玩家" + userUid + "头像文件" + imageBase64);

        } catch (Exception e) {

            log.error("下载头像数据失败 url:" + imageUrl);
        }

    }

    @Override
    public String facebookAuthenticationValidate(String userUid, String accessToken) {

        String appId = systemPropertiesConfig.getFacebookAppId();
        String appSecret = systemPropertiesConfig.getFacebookAppSecret();
        String serverTokenGetUrl = "https://graph.facebook.com/oauth/access_token?client_id=" + appId + "&client_secret=" + appSecret + "&grant_type=client_credentials";
        String result = HttpUtil.getString(serverTokenGetUrl);
        log.warn("facebook validate result:{}", result);

        String serverToken = JSONObject.parseObject(result).getString("access_token");


        String debugTokenUrl = "https://graph.facebook.com/debug_token?input_token=" + accessToken + "&access_token=" + serverToken;
        result = HttpUtil.getString(debugTokenUrl);

        JSONObject jsonObject = JSONObject.parseObject(result);
        String data = jsonObject.getString("data");
        JSONObject jsonData = JSONObject.parseObject(data);
        log.warn("facebook debug validate result jsonData: " + jsonData);


        String validateUserId = jsonData.getString("user_id");
        String validateAppId = jsonData.getString("app_id");

        if (!validateUserId.equals(userUid)) {
            throw new BusinessException("facebook 登陆验证失败,user id不匹配");
        }

        if (!validateAppId.equals(appId)) {
            throw new BusinessException("facebook 登陆验证失败,app id不匹配");
        }
        return serverToken;
    }

    @Override
    public GoogleIdToken.Payload googleAuthenticationValidate(String userId, String idToken) {

        //https://developers.google.com/identity/sign-in/android/backend-auth
        //需要https请求
        String webClientId = systemPropertiesConfig.getGoogleWebClientId();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(webClientId)).build();
        GoogleIdToken ticket = null;
        GoogleIdToken.Payload payLoad = null;
        try {
            ticket = verifier.verify(idToken);
            payLoad = ticket.getPayload();

        } catch (Exception e) {
            throw new BusinessException("google登陆失败" + payLoad);
        }

        String verifiedUserId = payLoad.getSubject();
        if (!verifiedUserId.equals(userId)) {
            throw new BusinessException("google登陆,鉴权userid != 玩家上报userid");
        }

        log.info("google登录鉴权成功" + JSONObject.toJSONString(payLoad));
        return payLoad;
    }

    @Override
    public List<MailData> getAllInboxMails(String userUid, int pullMailCount, Long cursorReceiveTime, String cursorMailUid) {

        int index = 0;
        List<MailData> mailDataList = RedisDBOperation.selectUserMailData(userUid);
        //获取传过来mailUid的下标
        for (MailData mailData : mailDataList) {
            if (mailData.getUid().equals(cursorMailUid)) {
                index = mailDataList.indexOf(mailData);
            }
        }
        List<MailData> result = mailDataList;
        if (mailDataList.size() != 0) {
            result = mailDataList.subList(index, mailDataList.size()).stream().filter(value -> value.getReceiveTime() > cursorReceiveTime).limit(pullMailCount).collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public boolean checkContainBadWords(String newName) {

        String checkContent = newName.toLowerCase();

        for (String badWord : badWords) {
            if (checkContent.contains(badWord)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int[] getPlayerChestRewardAddition(String userUid) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);
        boolean[] playerVipStatus = getPlayerVipStatus(userData);

        boolean isVip = playerVipStatus[0];
        boolean isSVip = playerVipStatus[1];
        int coinAddition = 1;
        int cardAddition = 1;

        if (isVip) {
            coinAddition += VipConfig.vipChestCoinAmountAddition;
            cardAddition += VipConfig.vipChestCardAmountAddition;
        }

        if (isSVip) {
            coinAddition += VipConfig.svipChestCoinAmountAddition;
            cardAddition += VipConfig.svipChestCardAmountAddition;
        }

        return new int[]{coinAddition, cardAddition};
    }

    @Override
    public boolean[] getPlayerVipStatus(UserData userData) {

        Long standardTimeDay = TimeUtils.getStandardTimeDay();

        boolean isVip = userData.getVipData().getVipExpiredStandardDay() >= standardTimeDay;
        boolean isSVip = userData.getVipData().getSvipExpiredStandardDay() >= standardTimeDay;
        boolean isVipV2 = userData.getVipV2Data().getVipExpiredStandardDay() >= standardTimeDay;
        boolean isSVipV2 = userData.getVipV2Data().getSvipExpiredStandardDay() >= standardTimeDay;
        boolean isVipV3 = userData.getVipV3Data().getVipExpiredStandardDay() >= standardTimeDay;
        boolean isSVipV3 = userData.getVipV3Data().getSvipExpiredStandardDay() >= standardTimeDay;

        return new boolean[]{isVip, isSVip, isVipV2, isSVipV2, isVipV3, isSVipV3};
    }

    @Override
    public boolean[] getPlayerVipV2Status(UserData userData) {

        Long standardTimeDay = TimeUtils.getStandardTimeDay();

        boolean isVip = userData.getVipV2Data().getVipExpiredStandardDay() >= standardTimeDay;
        boolean isSVip = userData.getVipV2Data().getSvipExpiredStandardDay() >= standardTimeDay;

        return new boolean[]{isVip, isSVip};
    }


    @Override
    public boolean[] getPlayerVipV3Status(UserData userData) {

        Long standardTimeDay = TimeUtils.getStandardTimeDay();

        boolean isVip = userData.getVipV3Data().getVipExpiredStandardDay() >= standardTimeDay;
        boolean isSVip = userData.getVipV3Data().getSvipExpiredStandardDay() >= standardTimeDay;

        return new boolean[]{isVip, isSVip};
    }

    @Override
    public double purchaseVip(UserData userData, String productName, String gameVersion) {

        PlayerVipData vipData = userData.getVipData();
        if (vipData == null) {
            throw new BusinessException("玩家没有vip数据");
        }

        Long standardToday = TimeUtils.getStandardTimeDay();

        double price = 0d;
        int instantDiamondReward = 0;
        if (productName.equals(VipConfig.vipProductName)) {
            vipData.setVipExpiredStandardDay(Math.max(vipData.getVipExpiredStandardDay(), standardToday - 1) + VipConfig.vipDurationDays);
            vipData.setVipExpiredStandardDay(Math.max(standardToday, vipData.getVipExpiredStandardDay()));
            price = VipConfig.vipPrice;
            instantDiamondReward += VipConfig.vipPurchaseDiamondRewardCount;
        } else if (productName.equals(VipConfig.svipProductName)) {
            vipData.setSvipExpiredStandardDay(Math.max(vipData.getSvipExpiredStandardDay(), standardToday - 1) + VipConfig.svipDurationDays);
            vipData.setSvipExpiredStandardDay(Math.max(standardToday, vipData.getSvipExpiredStandardDay()));
            price = VipConfig.svipPrice;
            instantDiamondReward += VipConfig.svipPurchaseDiamondRewardCount;
        } else {
            throw new BusinessException("不是购买vip相关产品.product name " + productName);
        }
        userData.setDiamond(userData.getDiamond() + instantDiamondReward);
        log.info("购买vip/svip立即获得购买钻石 " + instantDiamondReward);

        //转盘相关奖励直接下发
        refreshVipV1LuckyWheelCount(userData);

        log.info("`购买vip完成. " + vipData.toString());
        return price;
    }

    @Override
    public double purchaseVipV2(UserData userData, String productName, String gameVersion) {
        PlayerVipV2Data vipV2Data = userData.getVipV2Data();

        Long standardToday = TimeUtils.getStandardTimeDay();

        double price = 0d;
        int instantDiamondReward = 0;
        if (productName.equals(VipV2Config.vipProductName)) {
            vipV2Data.setVipExpiredStandardDay(Math.max(vipV2Data.getVipExpiredStandardDay(), standardToday - 1) + VipV2Config.vipDurationDays);
            vipV2Data.setVipExpiredStandardDay(Math.max(standardToday, vipV2Data.getVipExpiredStandardDay()));
            price = VipV2Config.vipPrice;
            instantDiamondReward += VipV2Config.vipPurchaseDiamondRewardCount;
        } else if (productName.equals(VipV2Config.svipProductName)) {
            vipV2Data.setSvipExpiredStandardDay(Math.max(vipV2Data.getSvipExpiredStandardDay(), standardToday - 1) + VipV2Config.svipDurationDays);
            vipV2Data.setSvipExpiredStandardDay(Math.max(standardToday, vipV2Data.getSvipExpiredStandardDay()));
            price = VipV2Config.svipPrice;
            instantDiamondReward += VipV2Config.svipPurchaseDiamondRewardCount;
        } else {
            throw new BusinessException("不是购买vip相关产品.product name " + productName);
        }
        userData.setDiamond(userData.getDiamond() + instantDiamondReward);
        log.info("购买vip/svip v2 立即获得购买钻石 " + instantDiamondReward);

        log.info("`购买vip v2完成. " + vipV2Data.toString());
        return price;
    }


    @Override
    public double purchaseVipV3(UserData userData, String productName, String gameVersion) {
        PlayerVipV3Data vipV3Data = userData.getVipV3Data();

        Long standardToday = TimeUtils.getStandardTimeDay();

        double price = 0d;
        int instantDiamondReward = 0;
        if (productName.equals(VipV3Config.vipProductName)) {
            vipV3Data.setVipExpiredStandardDay(Math.max(vipV3Data.getVipExpiredStandardDay(), standardToday - 1) + VipV3Config.vipDurationDays);
            vipV3Data.setVipExpiredStandardDay(Math.max(standardToday, vipV3Data.getVipExpiredStandardDay()));
            price = VipV3Config.vipPrice;
            instantDiamondReward += VipV3Config.vipPurchaseDiamondRewardCount;
        } else if (productName.equals(VipV3Config.svipProductName)) {
            vipV3Data.setSvipExpiredStandardDay(Math.max(vipV3Data.getSvipExpiredStandardDay(), standardToday - 1) + VipV3Config.svipDurationDays);
            vipV3Data.setSvipExpiredStandardDay(Math.max(standardToday, vipV3Data.getSvipExpiredStandardDay()));
            price = VipV3Config.svipPrice;
            instantDiamondReward += VipV3Config.svipPurchaseDiamondRewardCount;
        } else {
            throw new BusinessException("不是购买vip相关产品.product name " + productName);
        }
        userData.setDiamond(userData.getDiamond() + instantDiamondReward);
        log.info("购买vip/svip v3 立即获得购买钻石 " + instantDiamondReward);

        log.info("`购买vip v3完成. " + vipV3Data.toString());
        return price;
    }


    @Override
    public void refreshVipV1LuckyWheelCount(UserData userData) {

    }

    @Override
    public int getPlayerHighestUnlockedChapterID(UserData userData) {
        int highestID = -1;
        for (int i = 0; i < userData.getUnlockedChapterIds().size(); i++) {
            Integer unlockedChapterId = userData.getUnlockedChapterIds().get(i);
            if (unlockedChapterId > highestID) {
                highestID = unlockedChapterId;
            }
        }

        return highestID;
    }

    @Override
    public void archiveServerResponse(BaseDTO responseBody, String functionName) {
        String userUid = responseBody.getUserUid();
        Integer requestId = responseBody.getRequestId();
        //>=1.0.10版本，每个消息会带着request id
        if (requestId != null) {
            ServerResponseArchiveData archiveData = new ServerResponseArchiveData(functionName, requestId, TimeUtils.getUnixTimeMilliseconds(), responseBody);
            RedisDBOperation.insertUserArchiveServerResponse(userUid, archiveData);
            log.info("archive server response: " + JSON.toJSONString(archiveData).substring(0, 1000));
        }
    }

    @Override
    public Integer getUserMaxRequestIdNow(String userUid) {
        ServerResponseArchiveData serverResponseArchiveData = redisDBOperation.selectNewUserArchiveServerResponse(userUid);
        if (Objects.nonNull(serverResponseArchiveData)) {
            return serverResponseArchiveData.getRequestId();
        }

        return 0;
    }

    @Override
    public LoginSessionData createLoginSessionData(BaseDTO request) {
        //新用户不创建session
        if (request.getUserUid() == null) {
            return null;
        }
        //暂时不考虑旧版本建立登陆保护
        if (request.getRequestRandomId() == null) {
            return null;
        }
        LoginSessionData createdSessionData = RedisDBOperation.selectUserLoginSessionDataDocPath(request.getUserUid());
        if (null != createdSessionData) {
            // if (createdSessionData.getRequestRandomId() == request.getRequestRandomId()) {
            //     if (request.getRetry() == null) {
            //         //我们不知道这个消息是客户端retry的消息，还是另外乱序的消息，直接踢客户端下线要求重新登录
            //         throw new BusinessException("无法处理的重复登陆请求");
            //     } else if (request.getRetry() < createdSessionData.getRetry()) {
            //         throw new BusinessException("已经被客户端抛弃的login请求");
            //     }
            // }
            //否则，这个请求是客户端新的登陆请求。
        }
        //build session
        createdSessionData = new LoginSessionData(TimeUtils.getUnixTimeSecond(), request.getUserToken(), request.getRequestRandomId(), 0);
        if (request.getRetry() != null) {
            createdSessionData.setRetry(request.getRetry());
        }
        RedisDBOperation.insertUserLoginSessionDataDocPath(request.getUserUid(), createdSessionData);
        log.info("init login session: " + JSON.toJSONString(createdSessionData));
        return createdSessionData;
    }

    @Override
    public void updateSessionToken(UserData userData, String token, Integer requestRandomId) {
        if (requestRandomId == null) {
            return;
        }
        LoginSessionData loginSessionData = RedisDBOperation.selectUserLoginSessionDataDocPath(userData.getUuid());
        if (null != loginSessionData) {
            loginSessionData.setCurrentToken(token);
            loginSessionData.setRequestRandomId(requestRandomId);
            RedisDBOperation.insertUserLoginSessionDataDocPath(userData.getUuid(), loginSessionData);
            log.info("刷新session token " + JSON.toJSONString(loginSessionData));
        } else {

            loginSessionData = new LoginSessionData(TimeUtils.getUnixTimeSecond(), token, requestRandomId, 0);
            RedisDBOperation.insertUserLoginSessionDataDocPath(userData.getUuid(), loginSessionData);
            log.info("创建新用户session  " + JSON.toJSONString(loginSessionData));
        }
    }

    private void upgradePlayerHistoryData(UserData userData) {
        //初始化history
        if (userData.getHistory() == null) {
            userData.setHistory(new History());
        }
        History history = userData.getHistory();
        history.setTotalEarnedCoinByMatch(history.getTotalEarnedCoinByMatch() == null ? 0 : history.getTotalEarnedCoinByMatch());
        history.setTotalEarnedCoin(history.getTotalEarnedCoin() == null ? 0 : history.getTotalEarnedCoin());
        // history.setTotalEarnedCoinByMatch(Math.max(userData.getCoin(), history.getTotalEarnedCoinByMatch()));
        history.setHighestTrophyCount(history.getHighestTrophyCount() == null ? 0 : history.getHighestTrophyCount());
        history.setCurrentMatchWinStreak(history.getCurrentMatchWinStreak() == null ? 0 : history.getCurrentMatchWinStreak());
        history.setBestMatchWinStreak(history.getBestMatchWinStreak() == null ? 0 : history.getBestMatchWinStreak());
        history.setMatchAverageHitPrecision(history.getMatchAverageHitPrecision() == null ? 0 : history.getMatchAverageHitPrecision());
        history.setTotalAnimalKillAmount(history.getTotalAnimalKillAmount() == null ? 0 : history.getTotalAnimalKillAmount());
        history.setPerfectAnimalKillAmount(history.getPerfectAnimalKillAmount() == null ? 0 : history.getPerfectAnimalKillAmount());
        history.setHeadShotTimes(history.getHeadShotTimes() == null ? 0 : history.getHeadShotTimes());
        history.setHeartShotTimes(history.getHeartShotTimes() == null ? 0 : history.getHeartShotTimes());
        history.setAccumulateMoneyPaid(history.getAccumulateMoneyPaid() == null ? 0 : history.getAccumulateMoneyPaid());
        history.setMoneyPaidCount(history.getMoneyPaidCount() == null ? 0 : history.getMoneyPaidCount());
        history.setServer_only_matchTotalShots(history.getServer_only_matchTotalShots() == null ? 0 : history.getServer_only_matchTotalShots());
        history.setServer_only_matchAllShotsPrecisionAccumulation(history.getServer_only_matchAllShotsPrecisionAccumulation() == null ? 0 : history.getServer_only_matchAllShotsPrecisionAccumulation());
        history.setCurrentMatchLoseStreak(history.getCurrentMatchLoseStreak() == null ? 0 : history.getCurrentMatchLoseStreak());

    }


    private void upgradePlayerLuckyWheelData(UserData userData, String gameVersion) {

        if (userData.getLuckyWheelData() == null) {
            userData.setLuckyWheelData(createDefaultLuckyWheelData(gameVersion));
        }
        LuckyWheelData luckyWheelData = userData.getLuckyWheelData();
        luckyWheelData.setVipSpinCount(luckyWheelData.getVipSpinCount() == null ? 0 : luckyWheelData.getVipSpinCount());
        luckyWheelData.setUseSpinCountInHistory(luckyWheelData.getUseSpinCountInHistory() == null ? 0 : luckyWheelData.getUseSpinCountInHistory());
    }

    @Override
    public void upgradePlayerChestOpenIndexMapData(UserData userData) {
        ChestOpenIndexMap chestOpenIndexMap = userData.getServerOnly().getChestOpenIndexMap();
        if (null == chestOpenIndexMap.getGunLibraryDrawCountMap()) {
            //升级玩家某个枪械库抽取了多少次
            Map<Integer, Integer> gunLibraryDrawCountMap = new HashMap<>();
            Map<String, Integer> gunRewardIndexMap = chestOpenIndexMap.getGunRewardIndexMap();
            for (Map.Entry<String, Integer> entry : gunRewardIndexMap.entrySet()) {
                String key = entry.getKey();
                String[] splits = key.split("_");
                if (splits.length != 2) {
                    throw new BusinessException("无法upgradePlayerChestOpenIndexMap，key无法解析： " + key);
                }

                Integer gunLibraryType = Integer.parseInt(splits[0]);
                Integer drawCount = gunRewardIndexMap.get(key);

                Integer original = gunLibraryDrawCountMap.get(gunLibraryType);
                if (original == null) {
                    // gunLibraryDrawCountMap.put(gunLibraryType, 0);
                    original = 0;
                }
                gunLibraryDrawCountMap.put(gunLibraryType, original + drawCount);
            }
            log.info("upgrade gunLibraryDrawCountMap: " + JSON.toJSONString(gunLibraryDrawCountMap));
            chestOpenIndexMap.setGunLibraryDrawCountMap(gunLibraryDrawCountMap);
        }
    }

    private void addGunToDrawCountMap(Map<Integer, Integer> gunLibraryDrawCountMap, Integer gunLibrary, Integer count) {
        Integer original = gunLibraryDrawCountMap.get(gunLibrary);
        if (original == null) {
            // gunLibraryDrawCountMap.put(gunLibraryType, 0);
            original = 0;
        }
        gunLibraryDrawCountMap.put(gunLibrary, original + count);
    }

    public void recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(UserData userData, Map<Integer, Integer> gunCountMap, String gameVersion) {
        //确保数据存在
        upgradePlayerChestOpenIndexMapData(userData);
        Map<Integer, Integer> gunLibraryDrawCountMap = userData.getServerOnly().getChestOpenIndexMap().getGunLibraryDrawCountMap();

        Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);

        for (Map.Entry<Integer, Integer> entry : gunCountMap.entrySet()) {
            Integer gunId = entry.getKey();
            Integer gunCount = entry.getValue();
            gunCount = (int) Math.ceil(gunCount * (1 + 0.2));

            GunTableValue gunTableValue = gunTable.get(gunId.toString());
            GunQuality quality = GunQuality.values()[gunTableValue.getQuality() - 1];

            switch (quality) {
                case White:
                    //不应该会给白色品质的枪
                    throw new BusinessException("不应该会给白色品质的枪。 id " + gunId);
                case Blue:
                    //所有蓝色品质都算common，也就是random库不会增加
                    addGunToDrawCountMap(gunLibraryDrawCountMap, GunLibraryType.Common.getType(), gunCount);
                    break;
                case Orange:
                    addGunToDrawCountMap(gunLibraryDrawCountMap, GunLibraryType.Rare.getType(), gunCount);
                    break;
                case Red:
                    addGunToDrawCountMap(gunLibraryDrawCountMap, GunLibraryType.Epic.getType(), gunCount);
                    break;
            }
        }
    }

    List<String> badWords = Arrays.asList(
            "2g1c",
            "acrotomophilia",
            "anal",
            "anilingus",
            "anus",
            "apeshit",
            "arsehole",
            "ass",
            "asshole",
            "assmunch",
            "autoerotic",
            "babeland",
            "bangbros",
            "bareback",
            "barenaked",
            "bastard",
            "bastardo",
            "bastinado",
            "bbw",
            "bdsm",
            "beaner",
            "beaners",
            "bestiality",
            "bimbos",
            "birdlock",
            "bitch",
            "bitches",
            "blowjob",
            "blumpkin",
            "bollocks",
            "bondage",
            "boner",
            "boob",
            "boobs",
            "bukkake",
            "bulldyke",
            "bullshit",
            "bunghole",
            "busty",
            "butt",
            "buttcheeks",
            "butthole",
            "camgirl",
            "camslut",
            "camwhore",
            "carpetmuncher",
            "circlejerk",
            "clit",
            "clitoris",
            "clusterfuck",
            "cock",
            "cocks",
            "coprolagnia",
            "coprophilia",
            "cornhole",
            "coon",
            "coons",
            "creampie",
            "cum",
            "cumming",
            "cunnilingus",
            "cunt",
            "darkie",
            "daterape",
            "deepthroat",
            "dendrophilia",
            "dick",
            "dildo",
            "dingleberry",
            "dingleberries",
            "doggiestyle",
            "doggystyle",
            "dolcett",
            "domination",
            "dominatrix",
            "dommes",
            "dvda",
            "ecchi",
            "ejaculation",
            "erotic",
            "erotism",
            "escort",
            "eunuch",
            "faggot",
            "fecal",
            "felch",
            "fellatio",
            "feltch",
            "femdom",
            "figging",
            "fingerbang",
            "fingering",
            "fisting",
            "footjob",
            "frotting",
            "fuck",
            "fuckin",
            "fucking",
            "fucktards",
            "fudgepacker",
            "futanari",
            "genitals",
            "goatcx",
            "goatse",
            "gokkun",
            "goodpoop",
            "goregasm",
            "grope",
            "g-spot",
            "guro",
            "handjob",
            "hardcore",
            "hentai",
            "homoerotic",
            "honkey",
            "hooker",
            "humping",
            "incest",
            "intercourse",
            "jailbait",
            "jigaboo",
            "jiggaboo",
            "jiggerboo",
            "jizz",
            "juggs",
            "kike",
            "kinbaku",
            "kinkster",
            "kinky",
            "knobbing",
            "lolita",
            "lovemaking",
            "masturbate",
            "milf",
            "motherfucker",
            "muffdiving",
            "nambla",
            "nawashi",
            "negro",
            "neonazi",
            "nigga",
            "nigger",
            "nimphomania",
            "nipple",
            "nipples",
            "nude",
            "nudity",
            "nympho",
            "nymphomania",
            "octopussy",
            "omorashi",
            "orgasm",
            "orgy",
            "paedophile",
            "paki",
            "panties",
            "panty",
            "pedobear",
            "pedophile",
            "pegging",
            "penis",
            "pissing",
            "pisspig",
            "playboy",
            "ponyplay",
            "poof",
            "poon",
            "poontang",
            "punany",
            "poopchute",
            "porn",
            "porno",
            "pornography",
            "pthc",
            "pubes",
            "pussy",
            "queaf",
            "queef",
            "quim",
            "raghead",
            "rape",
            "raping",
            "rapist",
            "rectum",
            "rimjob",
            "rimming",
            "sadism",
            "santorum",
            "scat",
            "schlong",
            "scissoring",
            "semen",
            "sex",
            "sexo",
            "sexy",
            "shemale",
            "shibari",
            "shit",
            "shitblimp",
            "shitty",
            "shota",
            "shrimping",
            "skeet",
            "slanteye",
            "slut",
            "s&m",
            "smut",
            "snatch",
            "snowballing",
            "sodomize",
            "sodomy",
            "spic",
            "splooge",
            "spooge",
            "spunk",
            "strapon",
            "strappado",
            "suck",
            "sucks",
            "swastika",
            "swinger",
            "threesome",
            "throating",
            "tit",
            "tits",
            "titties",
            "titty",
            "topless",
            "tosser",
            "towelhead",
            "tranny",
            "tribadism",
            "tubgirl",
            "tushy",
            "twat",
            "twink",
            "twinkie",
            "undressing",
            "upskirt",
            "urophilia",
            "vagina",
            "vibrator",
            "vorarephilia",
            "voyeur",
            "vulva",
            "wank",
            "wetback",
            "xx",
            "xxx",
            "yaoi",
            "yiffy",
            "zoophilia");
}
