package org.skynet.service.provider.hunting.obsolete.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.skynet.components.hunting.robot.data.RobotPlayerBasicInfoBO;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.rank.league.query.GetPlayerRankQuery;
import org.skynet.components.hunting.rank.league.service.RankLeagueFeignService;
import org.skynet.components.hunting.robot.data.RobotPlayerInfoBO;
import org.skynet.components.hunting.robot.query.RobotsQuery;
import org.skynet.components.hunting.robot.service.RobotFactoryFeignService;
import org.skynet.components.hunting.user.domain.History;
import org.skynet.components.hunting.user.domain.PlayerRecordModeData;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.DeflaterUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.NumberUtils;
import org.skynet.service.provider.hunting.obsolete.config.GameConfig;
import org.skynet.service.provider.hunting.obsolete.enums.BulletQuality;
import org.skynet.service.provider.hunting.obsolete.enums.HuntingMatchAIRecordChooseMode;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.CheckNewUnlockChapterBO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.BulletTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.ChapterTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.MatchAIRoundRuleTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.RecordModeMatchTableValue;
import org.skynet.service.provider.hunting.obsolete.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HuntingMatchServiceImpl implements HuntingMatchService {


    @Autowired
    private AchievementService achievementService;

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private MatchWinRateTableService matchWinRateTableService;

    @Resource
    private WeaponService weaponService;

    @Resource
    private AiService aiService;

    @Resource
    private RankLeagueFeignService rankLeagueFeignService;

    @Resource
    private RobotFactoryFeignService robotFactoryFeignService;

    @Override
    public void saveHuntingMatchNowData(String path, String huntingMatchNowUid, String userUid, HuntingMatchNowData huntingMatchNowData) {

        String key = path + ":" + huntingMatchNowUid;
        RedisDBOperation.insertHuntingMatchNowData(key, huntingMatchNowData);
        RedisDBOperation.insertPlayerHuntingMatchNowData(userUid, huntingMatchNowUid);
    }

    @Override
    public void reSaveHuntingMatchNowData(String path, String huntingMatchNowUid, String userUid, HuntingMatchNowData huntingMatchNowData) {

        String key = path + ":" + huntingMatchNowUid;
        RedisDBOperation.insertHuntingMatchNowData(key, huntingMatchNowData);
        RedisDBOperation.setPlayerHuntingMatchNowData(userUid, huntingMatchNowUid);
    }

    @Override
    public void removeHuntingMatchNowData(String path, String userUid, String huntingMatchNowUid) {

        String key = path + ":" + huntingMatchNowUid;
        RedisDBOperation.deleteHuntingMatchNowData(key);
        RedisDBOperation.deletePlayerHuntingMatchNowData(userUid, huntingMatchNowUid);
    }

    @Override
    public List<String> getPlayerHuntingMatchNowData(String userUid) {


        // List<Object> list = RedisDBOperation.selectPlayerHuntingMatchNowData(userUid);
        // List<String> huntingMatchNowUids = new ArrayList<>();
        // Collections.addAll(huntingMatchNowUids, list.toArray(new String[0]));
        //
        // return huntingMatchNowUids;

        return null; // TODO Z

    }

    @Override
    public HuntingMatchHistoryData getHuntingMatchHistoryData(String gameVersion, String matchUid) {

        String path = Path.getHuntingMatchHistoryCollectionPath(gameVersion);
        HuntingMatchHistoryData huntingMatchHistoryData = null;
        try {
            huntingMatchHistoryData = RedisDBOperation.selectHuntingMatchHistoryData(path + ":" + matchUid);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return huntingMatchHistoryData;

    }

    @Override
    public HuntingMatchNowData getHuntingMatchNowData(String path, String huntingMatchNowUid) {

        String key = path + ":" + huntingMatchNowUid;
        return RedisDBOperation.selectHuntingMatchNowData(key);
    }

    /**
     * ??????????????????,?????????????????????
     *
     * @param chapterTableValue ????????????
     * @param isWin             ?????????
     * @param gameVersion
     */
    @Override
    public void winOrLoseTrophyInHuntingMatch(String uuid, ChapterTableValue chapterTableValue, boolean isWin, String gameVersion) {
        UserData userData = GameEnvironment.userDataMap.get(uuid);
        Map<Integer, Integer> chapterWinTrophyCountMap = userData.getChapterWinTrophyCountMap();
        if (isWin) {
            int alreadyRewardTrophyInChapter = chapterWinTrophyCountMap.getOrDefault(chapterTableValue.getId(), 0);
            //??????????????????????????????????????????
            int rewardTrophyCountInChapter = Math.min(chapterTableValue.getWinTrophyIncreaseCount() + alreadyRewardTrophyInChapter, chapterTableValue.getMaxAvailableTrophyCount());
            int trophyChangeInChapter = rewardTrophyCountInChapter - alreadyRewardTrophyInChapter;

            userData.setTrophy(userData.getTrophy() + trophyChangeInChapter);
            userData.getChapterWinTrophyCountMap().put(chapterTableValue.getId(), rewardTrophyCountInChapter);

            userData.getHistory().setHighestTrophyCount(Math.max(userData.getHistory().getHighestTrophyCount(), userData.getTrophy()));

            //????????????
            achievementService.updateTrophyAchievementData(userData, trophyChangeInChapter, chapterTableValue.getId(), gameVersion);
        } else {
            int winTrophyCountInChapter = chapterWinTrophyCountMap.getOrDefault(chapterTableValue.getId(), 0);
            int winTrophyCountInChapterNow = winTrophyCountInChapter - chapterTableValue.getLoseTrophyDecreaseCount();
            winTrophyCountInChapterNow = Math.max(0, winTrophyCountInChapterNow);
            int trophyChangeInChapter = winTrophyCountInChapterNow - winTrophyCountInChapter;

            userData.setTrophy(userData.getTrophy() + trophyChangeInChapter);
            userData.setTrophy(Math.max(0, userData.getTrophy()));

            userData.getChapterWinTrophyCountMap().put(chapterTableValue.getId(), winTrophyCountInChapterNow);
        }
    }

    @Override
    public int determineEnterWhichMatch(String userUid, Integer chapterId, Integer chapterEnteredCount, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);

        if (userData.getServerOnly().getRecordModeData() != null) {

            //???????????????????????????????????????match????????????
            PlayerRecordModeData recordModeData = userData.getServerOnly().getRecordModeData();
            Map<String, RecordModeMatchTableValue> recordModeMatchTable = GameEnvironment.recordModeMatchTableMap.get(gameVersion);
            RecordModeMatchTableValue targetRecordModeMatchTableValue = recordModeMatchTable.get(String.valueOf(recordModeData.getRecordModeMatchTableId()));

            int matchSequenceId = recordModeData.getMatchSequenceId();
            int resultMatchId = targetRecordModeMatchTableValue.getMatchIdSequence().get(matchSequenceId);
            log.info("????????????????????? ??????match " + resultMatchId + ",sequence id" + matchSequenceId);

            matchSequenceId++;
            matchSequenceId %= targetRecordModeMatchTableValue.getMatchIdSequence().size();
            recordModeData.setMatchSequenceId(matchSequenceId);

            return resultMatchId;
        } else {
            Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(gameVersion);
            ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(chapterId));
            List<Integer> matchIdArray = chapterTableValue.getMatchId();
            List<Integer> loopMatchIdsArray = chapterTableValue.getLoopMatchIds();
            int resultMatchId = 0;
            if (chapterEnteredCount >= matchIdArray.size()) {
                int loopIndex = (chapterEnteredCount - matchIdArray.size()) % loopMatchIdsArray.size();
                resultMatchId = loopMatchIdsArray.get(loopIndex);
            } else {
                resultMatchId = matchIdArray.get(chapterEnteredCount);
            }
            return resultMatchId;
        }

    }

    /**
     * ???????????????????????????
     *
     * @param userData
     * @return
     */
    @Override
    public PlayerWeaponInfo generateLocalPlayerWeaponInfo(UserData userData, String gameVersion) {

        if (userData.getServerOnly().getRecordModeData() != null) {

            //??????????????????????????????????????????
            PlayerRecordModeData recordModeData = userData.getServerOnly().getRecordModeData();
            Map<String, RecordModeMatchTableValue> recordModeMatchTable = GameEnvironment.recordModeMatchTableMap.get(gameVersion);

            RecordModeMatchTableValue tableValue = null;
            if (!recordModeMatchTable.containsKey(String.valueOf(recordModeData.getRecordModeMatchTableId()))) {

                throw new BusinessException("RecordModeMatchTable?????????id" + recordModeData.getMatchSequenceId());
            } else {
                tableValue = recordModeMatchTable.get(String.valueOf(recordModeData.getRecordModeMatchTableId()));
            }

            int gunId = tableValue.getPlayerGunId();
            int gunLevel = tableValue.getPlayerGunLevel();
            int bulletId = tableValue.getPlayerBulletId();
            PlayerWeaponInfo weaponInfo = new PlayerWeaponInfo(gunId, gunLevel, bulletId);
            log.info("????????????,????????????:" + JSONObject.toJSONString(weaponInfo));
            return weaponInfo;
        } else {
            int gunId = userData.getEquippedGunId();
            int gunLevel = userData.getGunLevelMap().get(gunId);
            int bulletId = userData.getEquippedBulletId();
            return new PlayerWeaponInfo(gunId, gunLevel, bulletId);
        }

    }


//    /**
//     * ???????????????AI????????????
//     * @param weaponInfo
//     * @return
//     */
//    @Override
//    public Integer calculateWeaponScore(PlayerWeaponInfo weaponInfo) {
//
//        Integer gunId = weaponInfo.getGunId();
//        Integer gunLevel = weaponInfo.getGunLevel();
//
//        Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap;
//
//        GunTableValue gunTableValue = gunTable.get(String.valueOf(gunId));
//
//        if (gunLevel -1 >= gunTableValue.getAiMatchScoreArray().size()){
//
//            throw new BusinessException("GunTable??????AiMatchScore??????,???????????? - 1"+(gunLevel-1)+">="+gunTableValue.getAiMatchScoreArray().size());
//        }
//
//        Integer gunAiMatchScore = gunTableValue.getAiMatchScoreArray().get(gunLevel - 1);
//
//        Map<String, BulletTableValue> bulletTable = GameEnvironment.bulletTableValueMap;
//
//        BulletTableValue bulletTableValue = bulletTable.get(String.valueOf(weaponInfo.getBulletId()));
//
//        int score = Math.round(gunAiMatchScore * bulletTableValue.getAiMatchScoreFactor());
//        return score;
//    }


    /**
     * ??????????????????????????????
     *
     * @param userData
     * @param chapterId
     * @param opponentTrophyCount
     * @param gameVersion
     * @return
     */
    @Override
    public OpponentPlayerInfo generateOpponentPlayerInfo(UserData userData, Integer chapterId, Integer opponentTrophyCount, String gameVersion) {

        final double aiProfileCount = 1071d;
        OpponentPlayerInfo opponentPlayerInfo = new OpponentPlayerInfo(null, null, null, null);

        Result<Integer> playerRankResult = rankLeagueFeignService.getPlayerRank(GetPlayerRankQuery.builder().userId(userData.getUuid()).build());
        if (playerRankResult.isSuccess()) {
            Result<RobotPlayerBasicInfoBO> robotPlayerBasicInfoBOResult = robotFactoryFeignService.randomGet(RobotsQuery.builder().rank(playerRankResult.getData()).build());
            if (robotPlayerBasicInfoBOResult.isSuccess()) {
                RobotPlayerBasicInfoBO robotPlayerBasicInfoBO = robotPlayerBasicInfoBOResult.getData();
                opponentPlayerInfo.setName(robotPlayerBasicInfoBO.getNickname());
                opponentPlayerInfo.setIcon_base64(robotPlayerBasicInfoBO.getHeadPic());
                opponentPlayerInfo.setTrophy(opponentTrophyCount);
                opponentPlayerInfo.setUseDefaultIcon(false);
                // return opponentPlayerInfo;
            }
        }

        if (StringUtils.isEmpty(opponentPlayerInfo.getName())) {
            opponentPlayerInfo.setName(obsoleteUserDataService.createGuestName(userData.getName()));
            opponentPlayerInfo.setIcon_base64(null);
            opponentPlayerInfo.setUseDefaultIcon(true);
            // return opponentPlayerInfo;
        }

        Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(gameVersion);
        //??????????????????????????????
        ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(chapterId));

        if (opponentTrophyCount == null) {
            opponentPlayerInfo.setTrophy(NumberUtils.randomInt(userData.getTrophy() * (1 - GameConfig.aiTrophyToPlayerChangeRatio), userData.getTrophy() * (1 + GameConfig.aiTrophyToPlayerChangeRatio)));
            opponentPlayerInfo.setTrophy(Math.max(chapterTableValue.getUnlockRequiresTrophyCount(), opponentPlayerInfo.getTrophy()));
        } else {
            opponentPlayerInfo.setTrophy(opponentTrophyCount);
            log.info("???????????????????????????:" + opponentTrophyCount);
        }

        // boolean useTotalRandomAiProfile = NumberUtils.randomFloat(0d, 1d) <= GameConfig.randomNameAiRatioInPlayerMatching;
        //
        // //???????????????????????????,??????????????????????????????
        // if (chapterId == 1) {
        //
        //     Integer firstChapterEnteredCount = 0;
        //     if (userData.getChapterEnteredCountMap().containsKey(1)) {
        //         firstChapterEnteredCount = userData.getChapterEnteredCountMap().get(1);
        //     }
        //     if (firstChapterEnteredCount <= 3) {
        //         useTotalRandomAiProfile = true;
        //     }
        // }
        // String osName = System.getProperty("os.name");
        // if (useTotalRandomAiProfile || osName.equals("Mac OS X")) {
        //     opponentPlayerInfo.setName(obsoleteUserDataService.createGuestName(userData.getName()));
        //     opponentPlayerInfo.setIcon_base64(null);
        //     opponentPlayerInfo.setUseDefaultIcon(true);
        // } else {
        //     //??????????????????????????????????????????
        //     List<Integer> latestMatchedAiProfileIds = userData.getServerOnly().getLatestMatchedAiProfileIds();
        //     Integer aiProfileId = NumberUtils.randomInt(1.0, aiProfileCount);
        //     int loopCount = 0;
        //     while (loopCount < 20) {
        //
        //         if (!latestMatchedAiProfileIds.contains(aiProfileId)) {
        //
        //             latestMatchedAiProfileIds.add(aiProfileId);
        //             //????????????
        //             if (latestMatchedAiProfileIds.size() > GameConfig.maxNotDuplicateMatchingAIProfileCount) {
        //                 latestMatchedAiProfileIds.subList(0, latestMatchedAiProfileIds.size() - GameConfig.maxNotDuplicateMatchingAIProfileCount);
        //             }
        //
        //             break;
        //         }
        //
        //         //ai??????id??????1?????????
        //         aiProfileId = NumberUtils.randomInt(1.0, aiProfileCount);
        //         loopCount++;
        //     }
        //
        //     String collectionPath = Path.getDefaultAiProfileCollectionPath();
        //
        //     String redisAiProfileId = collectionPath + ":" + aiProfileId;
        //     OpponentProfile opponentProfile = RedisDBOperation.selectOpponentProfile(redisAiProfileId);
        //
        //     if (opponentProfile == null) {
        //         throw new BusinessException("??????ai profile id" + aiProfileId + "?????????");
        //     }
        //     opponentPlayerInfo.setName(opponentProfile.getName());
        //     opponentPlayerInfo.setIcon_base64(opponentProfile.getIcon_base64());
        //     opponentPlayerInfo.setUseDefaultIcon(opponentProfile.getUseDefaultIcon());
        // }


        return opponentPlayerInfo;
    }

    @Override
    public void consumeBullet(UserData userData, Integer useBulletId, String gameVersion) {

        Map<String, BulletTableValue> bulletTable = GameEnvironment.bulletTableMap.get(gameVersion);
        BulletTableValue bulletTableValue = bulletTable.get(String.valueOf(useBulletId));

        //?????????????????????
        if (bulletTableValue.getQuality().equals(BulletQuality.White.getType())) {

            return;
        }

        Integer currCount = userData.getBulletCountMap().get(bulletTableValue.getId());
        if (currCount == 0) {

            throw new BusinessException("??????" + userData.getUuid() + "???????????????" + bulletTableValue.getId() + "???????????????0");
        }

        currCount -= 1;

        userData.getBulletCountMap().put(bulletTableValue.getId(), currCount);

        if (userData.getBulletCountMap().get(bulletTableValue.getId()) <= 0) {

            userData.setEquippedBulletId(GameConfig.defaultBulletID);
            log.info("???????????????" + bulletTableValue.getId() + "?????????0,?????????????????????" + userData.getEquippedBulletId());
        }

    }

    @Override
    public List<PlayerFireDetails> generateFireDetailsFromControlRecordData(List<PlayerControlRecordData> controlRecordData) {

        List<PlayerFireDetails> fireDetails = new ArrayList<>();

        for (int i = 0; i < controlRecordData.size(); i++) {

            PlayerControlRecordData recordData = controlRecordData.get(i);
            int round = i + 1;
            if (recordData.getAllControlSegmentRecordsData() != null) {

                for (int j = 0; j < recordData.getAllControlSegmentRecordsData().length; j++) {

                    ControlSegmentRecordData segmentRecordData = recordData.getAllControlSegmentRecordsData()[j];
                    boolean isLastSegment = j == recordData.getAllControlSegmentRecordsData().length - 1;

                    HitFireAtomControlData hitFireAtomControlData = segmentRecordData.getHitFireAtomControlData();

                    if (hitFireAtomControlData != null) {

                        boolean isPerfectShot = hitFireAtomControlData.getHitShowPrecision() > 0.99999 &&
                                hitFireAtomControlData.getHitShowPrecision() < 1.00001;

                        boolean isHitCriticalPart = hitFireAtomControlData.getIsHitCriticalPart();
                        int hitCriticalPartIndex = hitFireAtomControlData.getHitCriticalPartConfigIndex();

                        PlayerFireDetails fireDetail = new PlayerFireDetails(
                                round,
                                hitFireAtomControlData.getHitShowPrecision(),
                                isPerfectShot,
                                isLastSegment && recordData.getIsAnimalKill(),
                                isPerfectShot && isHitCriticalPart && hitCriticalPartIndex == 0,
                                isPerfectShot && isHitCriticalPart && hitCriticalPartIndex == 1);

                        fireDetails.add(fireDetail);
                    }

                }
            }

        }

        log.info("??????FireDetails" + fireDetails);

        return fireDetails;
    }

    @Override
    public CheckNewUnlockChapterBO checkNewUnlockChapter(UserData userData, String gameVersion) {

        CheckNewUnlockChapterBO result = new CheckNewUnlockChapterBO(false, 0);
        Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(gameVersion);

        Set<String> keySet = chapterTable.keySet();
        for (String key : keySet) {

            ChapterTableValue chapterTableValue = chapterTable.get(key);
            Integer chapterId = chapterTableValue.getId();

            if (userData.getUnlockedChapterIds().contains(chapterId)) {

                continue;
            }

            if (chapterTableValue.getUnlockRequiresTrophyCount() <= userData.getTrophy()) {

                log.info("????????????: " + chapterId);
                //???????????????
                userData.setPendingUnlockAnimationChapterId(chapterId);
                userData.getUnlockedChapterIds().add(chapterId);

                result.setHaveNewChapterUnlocked(true);
                result.setNewUnlockChapterId(chapterId);

                break;
            }

        }

        return result;
    }

    @Override
    public void recordChapterComplete(String uuid, ChapterTableValue chapterTableValue, Integer matchId, List<PlayerFireDetails> playerFireDetails, Boolean isWin, String gameVersion, List<PlayerControlRecordData> allPlayerControlRecordsData) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);

        Map<Integer, Integer> chapterCompletedCountMap = userData.getChapterCompletedCountMap();
        if (!chapterCompletedCountMap.containsKey(chapterTableValue.getId())) {
            chapterCompletedCountMap.put(chapterTableValue.getId(), 1);
        } else {
            int count = chapterCompletedCountMap.get(chapterTableValue.getId()) + 1;
            chapterCompletedCountMap.put(chapterTableValue.getId(), count);
        }
        userData.getChapterCompletedCountMap().putAll(chapterCompletedCountMap);

        //??????????????????
        if (isWin) {
            Map<Integer, Integer> chapterWinCountMap = userData.getChapterWinCountMap();
            if (!chapterWinCountMap.containsKey(chapterTableValue.getId())) {
                chapterWinCountMap.put(chapterTableValue.getId(), 1);
            } else {
                int count = chapterWinCountMap.get(chapterTableValue.getId()) + 1;
                chapterWinCountMap.put(chapterTableValue.getId(), count);
            }
        }
        achievementService.updateMatchDoneAchievementData(uuid, chapterTableValue.getId(), gameVersion);
    }

    @Override
    public void refreshPlayerHistoryData(String uuid, ChapterTableValue chapterTableValue, Boolean isWin, List<PlayerFireDetails> playerFireDetails, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);

        History history = userData.getHistory();

        //????????????,??????????????????,???????????????
        boolean isWinStreak = isWin && userData.getHistory().getCurrentMatchWinStreak() > 0;

        achievementService.updateMatchWinStreakInChapterOrAbove(userData, chapterTableValue.getId(), isWinStreak, gameVersion);

        if (isWin) {

            //????????????
            history.setCurrentMatchWinStreak(history.getCurrentMatchWinStreak() + 1);
            history.setCurrentMatchLoseStreak(0);
            if (history.getCurrentMatchWinStreak() > history.getBestMatchWinStreak()) {
                history.setBestMatchWinStreak(history.getCurrentMatchWinStreak());
            }
        } else {

            history.setCurrentMatchWinStreak(0);
            history.setCurrentMatchLoseStreak(history.getCurrentMatchLoseStreak() + 1);
        }

        for (PlayerFireDetails fireDetail : playerFireDetails) {

            history.setServer_only_matchTotalShots(history.getServer_only_matchTotalShots() + 1);
            double count = history.getServer_only_matchAllShotsPrecisionAccumulation() + fireDetail.getShowPrecision();
            history.setServer_only_matchAllShotsPrecisionAccumulation(count);

            if (fireDetail.getIsKillAnimal()) {

                history.setTotalAnimalKillAmount(history.getTotalAnimalKillAmount() + 1);
                if (fireDetail.getIsPerfect()) {

                    history.setPerfectAnimalKillAmount(history.getPerfectAnimalKillAmount() + 1);
                }
            }

            if (fireDetail.getIsHitHead()) {

                history.setHeadShotTimes(history.getHeadShotTimes() + 1);
            }

            if (fireDetail.getIsHitHeart()) {
                history.setHeartShotTimes(history.getHeartShotTimes() + 1);
            }

            //?????????????????????
            if (history.getServer_only_matchTotalShots() == 0) {

                history.setMatchAverageHitPrecision(0d);
            } else {
                double content = history.getServer_only_matchAllShotsPrecisionAccumulation() / history.getServer_only_matchTotalShots();
                history.setMatchAverageHitPrecision(content);
            }

        }

    }

    @Override
    public HuntingMatchAIRecordChooseMode determineAIRecordChooseMode(String userUid, String chapterId, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);

        Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(gameVersion);
        ChapterTableValue chapterTableValue = chapterTable.get(chapterId);
        int chapterEnteredCount = userData.getChapterEnteredCountMap().getOrDefault(Integer.valueOf(chapterId), 0);
        Integer winStreak = userData.getHistory().getCurrentMatchWinStreak();
        Integer loseStreak = userData.getHistory().getCurrentMatchLoseStreak();
        //??????????????????????????????????????????????????????????????????
        if (chapterEnteredCount <= chapterTableValue.getForceWinProneRoundMatchChapterEnterCount()) {
            log.info("??????????????????" + chapterEnteredCount + ",??????????????????????????????");
            return HuntingMatchAIRecordChooseMode.WinProneRound;
        }

        boolean forceRoundMatch = false;
        if (chapterEnteredCount <= chapterTableValue.getForceRoundMatchChapterEnterCount()) {
            forceRoundMatch = true;
        }

        /**
         * 1???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         2????????????????????????=????????????+??????????????????
         3????????????????????????=????????????+??????????????????
         4?????????????????????=MAX(??????????????????-?????????????????????0)/|??????????????????-??????????????????|*????????????????????????
         * */
        double chapterWinRate = obsoleteUserDataService.calculatePlayerChapterWinRate(userData, Integer.valueOf(chapterId));
        double playerCultivateScore = obsoleteUserDataService.calculatePlayerCultivateScore(userUid, gameVersion);
//        double cultivateWinRateAddition = Math.max(0,playerCultivateScore - chapterTableValue.getMinGuaranteeWinRate()) *chapterTableValue.getMaxCultivateWinRateAddition()/
//                Math.max(0,chapterTableValue.getMaxCultivateScore() - chapterTableValue.getMinCultivateScore());
//
//        double minGuaranteeWinRate =chapterTableValue.getMinGuaranteeWinRate() + cultivateWinRateAddition;
//        double lowWinRate = chapterTableValue.getLowWinRate() + cultivateWinRateAddition;

//        double cultivateWinRateAddition = Math.max(0,playerCultivateScore - chapterTableValue.getMinCultivateScore()) *chapterTableValue.getMaxCultivateWinRateAddition()/
//                Math.max(0,chapterTableValue.getMaxCultivateScore() - chapterTableValue.getMinCultivateScore());

        double cultivateWinRateAddition = Math.max(0, Math.min(playerCultivateScore, chapterTableValue.getMaxCultivateScore()) - chapterTableValue.getMinCultivateScore()) * chapterTableValue.getMaxCultivateWinRateAddition() /
                Math.max(0, chapterTableValue.getMaxCultivateScore() - chapterTableValue.getMinCultivateScore());

        double minGuaranteeWinRate = chapterTableValue.getMinGuaranteeWinRate() + cultivateWinRateAddition;
        double lowWinRate = chapterTableValue.getLowWinRate() + cultivateWinRateAddition;

        log.info("???????????? " + chapterId + "??????" + chapterWinRate + ",?????????????????? " +
                playerCultivateScore + ", lose streak: " + userData.getHistory().getCurrentMatchLoseStreak());

        if (loseStreak >= GameConfig.ForceWinProneRoundMatchLoseStreakCount || chapterWinRate <= minGuaranteeWinRate) {
            //??????????????????????????????
            if (winStreak < GameConfig.ForceHighLevelWholeMatchWinStreakCount && loseStreak < GameConfig.ForceLowLevelWholeMatchLoseStreakCount) {
                return HuntingMatchAIRecordChooseMode.WinProneRound;
            }
        }


        HuntingMatchAIRecordChooseMode wholeMatchMode = HuntingMatchAIRecordChooseMode.Unknown;

        if (chapterWinRate > lowWinRate && chapterWinRate < chapterTableValue.getHighWinRate() &&
                loseStreak < GameConfig.ForceLowLevelWholeMatchLoseStreakCount
                && winStreak < GameConfig.ForceHighLevelWholeMatchWinStreakCount) {

            wholeMatchMode = HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch;
        }

        if (chapterWinRate >= chapterTableValue.getHighWinRate() || winStreak >= GameConfig.ForceHighLevelWholeMatchWinStreakCount) {

            wholeMatchMode = HuntingMatchAIRecordChooseMode.HighLevelWholeMatch;
        }

//        if (chapterWinRate <= minGuaranteeWinRate||loseStreak>= GameConfig.ForceWinProneRoundMatchLoseStreakCount){
//
//            wholeMatchMode =  HuntingMatchAIRecordChooseMode.WinProneRound;
//        }

        if (loseStreak >= GameConfig.ForceLowLevelWholeMatchLoseStreakCount || chapterWinRate <= lowWinRate) {

            wholeMatchMode = HuntingMatchAIRecordChooseMode.LowLevelWholeMatch;
        }

        //???????????????????????????????????????,????????????????????????
//        if (loseStreak>=GameConfig.ForceLowLevelWholeMatchLoseStreakCount){
//            wholeMatchMode =  HuntingMatchAIRecordChooseMode.LowLevelWholeMatch;
//        }
        if (winStreak >= GameConfig.ForceHighLevelWholeMatchWinStreakCount) {
            wholeMatchMode = HuntingMatchAIRecordChooseMode.HighLevelWholeMatch;
//            int randomInt = RandomUtil.randomInt(1, 11);
//            if (randomInt>5){
//                wholeMatchMode =  HuntingMatchAIRecordChooseMode.HighLevelWholeMatch;
//            }else {
//                wholeMatchMode =  HuntingMatchAIRecordChooseMode.LoseProneRound;
//            }
//            wholeMatchMode =  HuntingMatchAIRecordChooseMode.HighLevelWholeMatch;
        }

        if (wholeMatchMode == HuntingMatchAIRecordChooseMode.Unknown) {
            throw new BusinessException("????????????ai??????????????????chapter entered count: " + chapterEnteredCount + ",chapter win rate: " + chapterWinRate + ",win streak: " +
                    winStreak + ", lose streak: " + loseStreak);
        }

        if (forceRoundMatch) {
            HuntingMatchAIRecordChooseMode matchModeBefore = wholeMatchMode;
            HuntingMatchAIRecordChooseMode roundMatchMode = aiService.convertAiRecordChooseModeFromWholeMatchToRound(wholeMatchMode);
            log.info("??????????????????" + chapterEnteredCount + ",????????????????????????. from" + matchModeBefore + "to " + roundMatchMode);
            return roundMatchMode;
        }

//        if (chapterWinRate <= lowWinRate||userData.getHistory().getCurrentMatchLoseStreak()>=GameConfig.ForceLowLevelWholeMatchLoseStreakCount){
//
//            wholeMatchMode =  HuntingMatchAIRecordChooseMode.LowLevelWholeMatch;
//        }
//
//        if (chapterWinRate>lowWinRate && chapterWinRate<chapterTableValue.getHighWinRate() &&
//            userData.getHistory().getCurrentMatchLoseStreak()<GameConfig.ForceLowLevelWholeMatchLoseStreakCount
//                &&userData.getHistory().getCurrentMatchWinStreak()<GameConfig.ForceHighLevelWholeMatchWinStreakCount){
//
//            wholeMatchMode =  HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch;
//        }
//
//        if (chapterWinRate >= chapterTableValue.getHighWinRate() || userData.getHistory().getCurrentMatchWinStreak() >= GameConfig.ForceHighLevelWholeMatchWinStreakCount){
//
//            wholeMatchMode =  HuntingMatchAIRecordChooseMode.HighLevelWholeMatch;
//        }
//
//        if (wholeMatchMode == HuntingMatchAIRecordChooseMode.Unknown){
//            throw new BusinessException("????????????ai??????????????????chapter entered count: "+chapterEnteredCount+",chapter win rate: "+chapterWinRate+",win streak: "+
//                    userData.getHistory().getCurrentMatchWinStreak()+", lose streak: "+userData.getHistory().getCurrentMatchLoseStreak());
//        }
//
//        if (forceRoundMatch){
//            HuntingMatchAIRecordChooseMode matchModeBefore = wholeMatchMode;
//            HuntingMatchAIRecordChooseMode roundMatchMode = aiService.convertAiRecordChooseModeFromWholeMatchToRound(wholeMatchMode);
//            log.info("??????????????????"+chapterEnteredCount+",????????????????????????. from"+matchModeBefore+"to "+roundMatchMode);
//            return roundMatchMode;
//        }
        return wholeMatchMode;
    }

    @Override
    public Integer getPlayerChapterLatestMatchMedianScore(Integer chapterId, String userUid, String gameVersion) {

        String path = Path.getPlayerChapterLatestMatchScoreCollectionPath(gameVersion, userUid, chapterId);

        List<Integer> list = new ArrayList<>();
        List<Object> tempList = RedisDBOperation.selectPlayerChapterLatestMatchScoreCollection(path);
        if (tempList == null || tempList.size() == 0) {
            return 0;
        }
        Collections.addAll(list, tempList.toArray(new Integer[0]));
        list = list.stream().sorted(Comparator.comparingInt(a -> a)).collect(Collectors.toList());

        int medianIndex = list.size() / 2;
        return list.get(medianIndex);

    }


    @Override
    public List<Integer> getPlayerChapterLatestMatchScore(Integer chapterId, String userUid, String gameVersion) {

        String path = Path.getPlayerChapterLatestMatchScoreCollectionPath(gameVersion, userUid, chapterId);

        List<Integer> list = new ArrayList<>();
        List<Object> tempList = RedisDBOperation.selectPlayerChapterLatestMatchScoreCollection(path);
        if (tempList == null || tempList.size() == 0) {
            list.add(0);
            list.add(0);
            list.add(0);
            list.add(0);
            list.add(0);
            return list;
        }
        Collections.addAll(list, tempList.toArray(new Integer[0]));

        return list;
//        list = list.stream().sorted(Comparator.comparingInt(a -> a)).collect(Collectors.toList());
//
//        int medianIndex = list.size()/2;
//        return list.get(medianIndex);

    }

    @Override
    public void savePlayerChapterLatestMatchScoreCollection(String userUid, Integer chapterId, Integer score, String gameVersion) {

        String path = Path.getPlayerChapterLatestMatchScoreCollectionPath(gameVersion, userUid, chapterId);
        RedisDBOperation.insertPlayerChapterLatestMatchScoreCollection(path, score);
    }

    @Override
    public MatchAIRoundRuleTableValue determineMatchAiRoundRuleTableValue(Integer chapterId, Integer chapterEnteredCount, HuntingMatchAIRecordChooseMode aiRecordChooseMode, String gameVersion) {

        //?????????????????????????????????????????????MatchAiRule???????????????ai??????
        if (aiRecordChooseMode != HuntingMatchAIRecordChooseMode.WinProneRound &&
                aiRecordChooseMode != HuntingMatchAIRecordChooseMode.LoseProneRound &&
                aiRecordChooseMode != HuntingMatchAIRecordChooseMode.drawProneRound) {
            return null;
        }

        List<MatchAIRoundRuleTableValue> winProneLoopTableValues = new ArrayList<>();
        List<MatchAIRoundRuleTableValue> loseProneLoopTableValues = new ArrayList<>();
        List<MatchAIRoundRuleTableValue> drawProneLoopTableValues = new ArrayList<>();
        int thisChapterMaxChapterEnteredCountInTable = 0;

        //??????????????????????????????????????????????????????????????????????????????index
        int winProneMaxChapterEnterCount = 0;
        int loseProneMaxChapterEnterCount = 0;
        int drawProneMaxChapterEnterCount = 0;

        Map<String, MatchAIRoundRuleTableValue> matchAIRuleTable = GameEnvironment.matchAIRoundRuleTableMap.get(gameVersion);

        Set<String> keySet = matchAIRuleTable.keySet();

        for (String key : keySet) {

            MatchAIRoundRuleTableValue tableValue = matchAIRuleTable.get(key);

            if (tableValue.getChapterID().equals(chapterId)) {

//                if (tableValue.getChapterEnteredCount()>thisChapterMaxChapterEnteredCountInTable){
//                    thisChapterMaxChapterEnteredCountInTable = tableValue.getChapterEnteredCount();
//                }
                if (tableValue.getChapterEnteredCount() > winProneMaxChapterEnterCount && aiRecordChooseMode == HuntingMatchAIRecordChooseMode.WinProneRound) {
                    winProneMaxChapterEnterCount = tableValue.getChapterEnteredCount();
                }

                if (tableValue.getChapterEnteredCount() > loseProneMaxChapterEnterCount && aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LoseProneRound) {
                    loseProneMaxChapterEnterCount = tableValue.getChapterEnteredCount();

                }
                if (tableValue.getChapterEnteredCount() > drawProneMaxChapterEnterCount && aiRecordChooseMode == HuntingMatchAIRecordChooseMode.drawProneRound) {
                    drawProneMaxChapterEnterCount = tableValue.getChapterEnteredCount();
                }

                //????????????????????????????????????
                if (tableValue.getAllowLoopPick()) {
                    if (tableValue.getWinLoseProne() == 0 && aiRecordChooseMode == HuntingMatchAIRecordChooseMode.WinProneRound) {
                        winProneLoopTableValues.add(tableValue);
                    } else if (tableValue.getWinLoseProne() == 1 && aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LoseProneRound) {
                        loseProneLoopTableValues.add(tableValue);
                    } else if (tableValue.getWinLoseProne() == 2 && aiRecordChooseMode == HuntingMatchAIRecordChooseMode.drawProneRound) {
                        drawProneLoopTableValues.add(tableValue);
                    }
                }

            }

            if (tableValue.getChapterID().equals(chapterId) && tableValue.getChapterEnteredCount().equals(chapterEnteredCount)) {
                //???????????????
                if (tableValue.getWinLoseProne() == 0 && aiRecordChooseMode == HuntingMatchAIRecordChooseMode.WinProneRound ||
                        tableValue.getWinLoseProne() == 1 && aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LoseProneRound)
                    return tableValue;
            }

            //????????????????????????????????????
//            if (tableValue.getChapterID().equals(chapterId)&&tableValue.getAllowLoopPick()){
//                if (tableValue.getWinLoseProne()==0&&aiRecordChooseMode == HuntingMatchAIRecordChooseMode.WinProneRound){
//                    winProneLoopTableValues.add(tableValue);
//                }else if (tableValue.getWinLoseProne()==1&&aiRecordChooseMode==HuntingMatchAIRecordChooseMode.LoseProneRound){
//                    loseProneLoopTableValues.add(tableValue);
//                }
//            }
        }

//        if (winProneLoopTableValues.size() != loseProneLoopTableValues.size()){
//            throw new BusinessException("chapter id"+chapterId+",chapter entered count"+chapterEnteredCount+"???MatchAiRoundRuleTable????????????loop??????"+
//                    "`,??????????????????????????????????????????.win:"+winProneLoopTableValues.size()+", lose"+loseProneLoopTableValues.size());
//        }

//        if (winProneLoopTableValues.size()==0){
//            throw new BusinessException("?????????MatchAiRoundRuleTable??????????????????????????????. chapter: "+chapterId+", chapter entered count"+chapterEnteredCount+", ai record choose mode:"+aiRecordChooseMode);
//        }

        //????????????????????????
//        int loopIndex = (chapterEnteredCount - thisChapterMaxChapterEnteredCountInTable-1)%winProneLoopTableValues.size();

        if (aiRecordChooseMode == HuntingMatchAIRecordChooseMode.WinProneRound) {
            return getRoundRuleTableValueFromLoopArray(winProneLoopTableValues, chapterEnteredCount, winProneMaxChapterEnterCount);
        } else if (aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LoseProneRound) {
            return getRoundRuleTableValueFromLoopArray(loseProneLoopTableValues, chapterEnteredCount, loseProneMaxChapterEnterCount);
        } else if (aiRecordChooseMode == HuntingMatchAIRecordChooseMode.drawProneRound) {
            return getRoundRuleTableValueFromLoopArray(drawProneLoopTableValues, chapterEnteredCount, drawProneMaxChapterEnterCount);
        }

        throw new BusinessException("????????????ai record choose rule:" + aiRecordChooseMode);
    }

    @Override
    public void saveHuntingHistoryMatch(String path, String uuid, HuntingMatchHistoryData historyData) {

        String key = path + ":" + uuid;
        String jsonString = JSONObject.toJSONString(historyData);
        String zipString = DeflaterUtils.zipString(jsonString);
        RedisDBOperation.insertHuntingMatchHistoryData(key, zipString);
    }


    private MatchAIRoundRuleTableValue getRoundRuleTableValueFromLoopArray(List<MatchAIRoundRuleTableValue> tableValues, Integer chapterEnteredCount, Integer maxChapterEnterCountInTable) {
        if (null == tableValues || tableValues.size() == 0) {
            log.warn("?????????????????????MatchAIRoundRuleTableValue");
            return null;
        }

        int loopIndex = (chapterEnteredCount - maxChapterEnterCountInTable - 1) % tableValues.size();
        return tableValues.get(Math.abs(loopIndex));
    }

}
