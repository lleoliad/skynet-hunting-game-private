package org.skynet.service.provider.hunting.obsolete.controller.module;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.domain.*;
import org.skynet.components.hunting.user.enums.ABTestGroup;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.*;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.GameConfig;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.config.VipV2Config;
import org.skynet.service.provider.hunting.obsolete.module.dto.BattleCompleteDto;
import org.skynet.service.provider.hunting.obsolete.module.dto.BattleMatchDto;
import org.skynet.service.provider.hunting.obsolete.module.dto.BattleStartDto;
import org.skynet.service.provider.hunting.obsolete.module.dto.UserInfoDto;
import org.skynet.service.provider.hunting.obsolete.module.util.ThreadLocalForFight;
import org.skynet.service.provider.hunting.obsolete.enums.*;
import org.skynet.service.provider.hunting.obsolete.module.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.CheckNewUnlockChapterBO;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.InitUserDataBO;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.RecordDataAndBase64;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.*;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.ChapterTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.LuckyWheelV2PropertyTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.MatchAIRoundRuleTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.RecordModeMatchTableValue;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.skynet.service.provider.hunting.obsolete.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@Api(tags = "????????????????????????")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
@CrossOrigin
public class FightModuleController {


    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private HuntingMatchService huntingMatchService;

    @Resource
    private PlayerControlRecordDataService playerControlRecordDataService;

    @Resource
    private ChestService chestService;

    @Resource
    private ChapterBonusPackageDataService packageDataService;

    @Resource
    private AiService aiService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private AchievementService achievementService;


    @Resource
    private SigninDiamondRewardTableService signinDiamondRewardTableService;

    @Resource
    private IAPService iapService;


    //    @PostMapping("/huntingMatch-confirmHuntingMatchStart")
    @ApiOperation("????????????????????????")
    public Map<String, Object> confirmHuntingMatchStart(@RequestBody ConfirmHuntingMatchStartDTO request) {
        try {
            GameEnvironment.timeMessage.computeIfAbsent("confirmHuntingMatchStart", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] confirmHuntingMatchStart" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
//            userDataService.ensureUserDataIdempotence(request.getUserUid(),request.getUserDataUpdateCount(),request.getGameVersion());

            String playerUUID = request.getUserUid();
            Integer playerSelectChapterId = request.getChapterId();

            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();

            PlayerWeaponInfo localPlayerWeaponInfo = null;
            PlayerWeaponInfo opponentPlayerWeaponInfo = null;
            boolean isLocalPlayerFirst = true;
            OpponentPlayerInfo opponentPlayerInfo = null;
            Integer matchId = null;

            HuntingMatchAIRecordChooseMode aiRecordChooseMode = HuntingMatchAIRecordChooseMode.WinProneRound;
            MatchAIRoundRuleTableValue matchAiRoundRuleTableValue = null;
            PlayerUploadWholeMatchControlRecordData wholeMatchControlRecordsData = null;
            PlayerRecordModeData recordModeData = null;

            obsoleteUserDataService.checkUserDataExist(playerUUID);
            UserData userData = GameEnvironment.userDataMap.get(playerUUID);

            recordModeData = userData.getServerOnly().getRecordModeData();

            if (recordModeData != null) {

                Map<String, RecordModeMatchTableValue> recordModeMatchTable = GameEnvironment.recordModeMatchTableMap.get(request.getGameVersion());
                RecordModeMatchTableValue recordModeMatchTableValue = recordModeMatchTable.get(String.valueOf(recordModeData.getRecordModeMatchTableId()));
                //??????????????????????????????????????????
                playerSelectChapterId = recordModeMatchTableValue.getChapterId();
            }

            Integer realChapterId = request.getChapterId();
            Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(request.getGameVersion());
            ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(realChapterId));

            if (userData.getCoin() < chapterTableValue.getEntryFee() && recordModeData == null) {

                throw new BusinessException("??????" + playerUUID + "????????????,??????????????????");
            }

            //????????????????????????
            Map<Integer, Integer> chapterEnteredCountMap = userData.getChapterEnteredCountMap();
            int chapterEnteredCount = chapterEnteredCountMap.getOrDefault(request.getChapterId(), 0);
            chapterEnteredCount += 1;
            chapterEnteredCountMap.put(realChapterId, chapterEnteredCount);
            userDataSendToClient.setChapterEnteredCountMap(userData.getChapterEnteredCountMap());

            //?????????????????????????????????
            //????????????????????????????????????+1????????????-1
            matchId = huntingMatchService.determineEnterWhichMatch(playerUUID, realChapterId, chapterEnteredCount - 1, request.getGameVersion());

            double playerCultivateScore = obsoleteUserDataService.calculatePlayerCultivateScore(request.getUserUid(), request.getGameVersion());
            double cultivateWinRateAddition = Math.max(0, Math.min(playerCultivateScore, chapterTableValue.getMaxCultivateScore()) - chapterTableValue.getMinCultivateScore()) * chapterTableValue.getMaxCultivateWinRateAddition() /
                    Math.max(0, chapterTableValue.getMaxCultivateScore() - chapterTableValue.getMinCultivateScore());

            int gunId = userData.getEquippedGunId();
            int gunLevel = userData.getGunLevelMap().get(gunId);
            int bulletId = userData.getEquippedBulletId();
            BattleMatchDto matchDto = new BattleMatchDto(request.getChapterId(), cultivateWinRateAddition, new PlayerWeaponInfo(gunId, gunLevel, bulletId), userData.getServerOnly().getRecordModeData() != null, userData.getTrophy());
            Map<String, Object> fightInfo = HttpUtil.getFightInfo("battle/complete/battle/matching", matchDto);

            //???????????????
            MatchInfo matchInfo = JSONUtil.toBean(fightInfo.get("matchInfo").toString(), MatchInfo.class);
            localPlayerWeaponInfo = matchInfo.getAttacker();
            opponentPlayerWeaponInfo = matchInfo.getDefender();
//            localPlayerWeaponInfo = huntingMatchService.generateLocalPlayerWeaponInfo(userData,request.getGameVersion());

            //??????????????????
            int entryFee = chapterTableValue.getEntryFee();
            if (recordModeData != null) {
                ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(playerSelectChapterId));

                if (Arrays.asList(GameConfig.recordModeFreeFeeChapterIdsArray).contains(playerSelectChapterId)) {
                    entryFee = 0;
                } else {
                    entryFee = playerSelectChapterTableValue.getEntryFee();
                }
                log.info("????????????,???????????????" + entryFee);
            }
            userData.setCoin(userData.getCoin() - entryFee);
            userData.setCoin(Math.max(userData.getCoin(), 0));
            userDataSendToClient.setCoin(userData.getCoin());

            //??????AI??????????????????
            aiRecordChooseMode = huntingMatchService.determineAIRecordChooseMode(playerUUID, String.valueOf(request.getChapterId()), request.getGameVersion());

            if (aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.HighLevelWholeMatch)
                    || aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch)
                    || aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.LowLevelWholeMatch)) {
                log.info("ai??????????????????????????????,mode:" + aiRecordChooseMode);
                //??????????????????
                isLocalPlayerFirst = NumberUtils.randomFloat(0.0, 1.0) <= 0.5;
                wholeMatchControlRecordsData = aiService.queryWholeMatchControlRecords(playerUUID, aiRecordChooseMode, playerSelectChapterId, matchId, request.getGameVersion());
                if (wholeMatchControlRecordsData != null) {
                    opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByWholeMatchRecordsData(wholeMatchControlRecordsData);
                }
            }

            //???????????????????????????????????????????????????????????????????????????????????????
            if (opponentPlayerWeaponInfo == null &&
                    (aiRecordChooseMode == HuntingMatchAIRecordChooseMode.HighLevelWholeMatch ||
                            aiRecordChooseMode == HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch ||
                            aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LowLevelWholeMatch)) {

                log.info("??????????????????????????????????????????????????????");
                /**
                 * ??????????????????????????????????????????????????????????????????????????????????????????
                 * ??????????????????????????????????????????????????????????????????
                 */
                HuntingMatchAIRecordChooseMode aiRecordChooseModeBefore = aiRecordChooseMode;
                aiRecordChooseMode = aiService.convertAiRecordChooseModeFromWholeMatchToRound(aiRecordChooseMode);
                log.info(aiRecordChooseModeBefore + "?????????????????????????????????????????????????????? " + aiRecordChooseMode);
            }

            if (aiRecordChooseMode == HuntingMatchAIRecordChooseMode.WinProneRound ||
                    aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LoseProneRound ||
                    aiRecordChooseMode == HuntingMatchAIRecordChooseMode.drawProneRound
            ) {

                log.info("ai???????????????????????????mode:" + aiRecordChooseMode);
                //??????????????????MatchAiRuleTable??????
                matchAiRoundRuleTableValue = huntingMatchService.determineMatchAiRoundRuleTableValue(request.getChapterId(), chapterEnteredCount, aiRecordChooseMode, request.getGameVersion());

                if (matchAiRoundRuleTableValue == null) {
                    throw new BusinessException("?????????????????????matchAiRoundRuleTableValue");
                }

                log.info("?????????MatchAIRoundRuleTable??????" + JSONObject.toJSONString(matchAiRoundRuleTableValue));
//                opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByMatchAiRoundRule(matchAiRoundRuleTableValue,request.getGameVersion());
                isLocalPlayerFirst = matchAiRoundRuleTableValue.getIsPlayerFirst();
            }


            //????????????
            if (recordModeData == null) {
                huntingMatchService.consumeBullet(userData, localPlayerWeaponInfo.getBulletId(), request.getGameVersion());
                userDataSendToClient.setBulletCountMap(userData.getBulletCountMap());
                userDataSendToClient.setEquippedBulletId(userData.getEquippedBulletId());
            }

            //????????????ai??????
            //????????????
            if (recordModeData == null) {

                Integer count = wholeMatchControlRecordsData == null ? null : wholeMatchControlRecordsData.getSafePlayerTrophyCount();
                opponentPlayerInfo = huntingMatchService.generateOpponentPlayerInfo(userData, chapterTableValue.getId(), count, request.getGameVersion());
            }
            //????????????
            else {
                log.info("?????????????????????????????????" + playerSelectChapterId);
                opponentPlayerInfo = huntingMatchService.generateOpponentPlayerInfo(userData, request.getChapterId(), null, request.getGameVersion());
            }

            userDataSendToClient.setHistory(userData.getHistory());
            obsoleteUserDataService.userDataSettlement(userData, userDataSendToClient, true, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            String huntingMatchUUID = NanoIdUtils.randomNanoId(30);

            //?????????????????????????????????,?????????????????????,????????????
            HuntingMatchNowData huntingMatchNowData = new HuntingMatchNowData(
                    huntingMatchUUID,
                    playerUUID,
                    playerSelectChapterId,
                    null,
                    matchId,
                    localPlayerWeaponInfo,
                    opponentPlayerWeaponInfo,
                    TimeUtils.getUnixTimeSecond(),
                    new ArrayList<>(),
                    false,
                    aiRecordChooseMode,
                    null,
                    null);

            if (recordModeData != null) {
                log.info("?????????????????????????????????????????????" + request.getChapterId());
                huntingMatchNowData.setRecordModeSelectChapterId(request.getChapterId());
            }

            if (matchAiRoundRuleTableValue != null) {
                huntingMatchNowData.setMatchAiRoundRuleTableId(matchAiRoundRuleTableValue.getId());
            }

            if (wholeMatchControlRecordsData != null) {
                huntingMatchNowData.setWholeMatchControlRecordsData(wholeMatchControlRecordsData);
            }

            //??????????????????????????????
            String matchPath = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
            huntingMatchService.saveHuntingMatchNowData(matchPath, huntingMatchUUID, playerUUID, huntingMatchNowData);

            log.info("????????????," + huntingMatchNowData);

            RangeFloat confirmHuntingMatchStartElapseTimeRange = new RangeFloat(GameConfig.confirmHuntingMatchStartMinElapseTimeRangeArray.get_min(),
                    GameConfig.confirmHuntingMatchStartMinElapseTimeRangeArray.get_max());

            double playerMatchingTime = confirmHuntingMatchStartElapseTimeRange.random();

            long elapseTime = Math.max(0, TimeUtils.getUnixTimeSecond() - request.getClientTime());
            double needWaitTime = Math.max(0, playerMatchingTime - elapseTime);


            map.put("userData", userDataSendToClient);
            map.put("matchUid", huntingMatchUUID);
            map.put("chapterId", playerSelectChapterId);
            map.put("matchId", matchId);
            map.put("localPlayerWeaponInfo", localPlayerWeaponInfo);
            map.put("opponentPlayerWeaponInfo", opponentPlayerWeaponInfo);
            map.put("isLocalPlayerFirst", isLocalPlayerFirst);
            map.put("opponentPlayerInfo", opponentPlayerInfo);
            map.put("aiRecordChooseMode", aiRecordChooseMode);
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("confirmHuntingMatchStart").add(needTime);
            log.info("[cmd] confirmHuntingMatchStart finish need time" + (System.currentTimeMillis() - startTime));
            return map;
        } catch (Exception e) {

            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }


    //    @PostMapping("/huntingMatch-confirmHuntingMatchComplete")
    @ApiOperation("????????????????????????")
    public Map<String, Object> confirmHuntingMatchComplete(@RequestBody ConfirmHuntingMatchCompleteDTO request) {
        try {
            GameEnvironment.timeMessage.computeIfAbsent("confirmHuntingMatchComplete", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] confirmHuntingMatchComplete" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
//            userDataService.ensureUserDataIdempotence(request.getUserUid(),request.getUserDataUpdateCount(),request.getGameVersion());
            //?????????????????????????????????,???????????????
            String matchPath = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());

            HuntingMatchNowData huntingMatchNowData = huntingMatchService.getHuntingMatchNowData(matchPath, request.getMatchUUID());

            if (huntingMatchNowData == null) {
                throw new BusinessException("??????" + request.getMatchUUID() + "?????????");
            }

            int roundCount = request.getAllEncodedControlRecordsData().size();
            List<PlayerControlRecordData> allPlayerControlRecordsData = new ArrayList<>();

            for (String roundEncodedData : request.getAllEncodedControlRecordsData()) {
                String temp = playerControlRecordDataService.decodeControlRecordData(roundEncodedData);
                PlayerControlRecordData controlRecordData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
                allPlayerControlRecordsData.add(controlRecordData);
            }

            List<PlayerFireDetails> playerFireDetails = huntingMatchService.generateFireDetailsFromControlRecordData(allPlayerControlRecordsData);

            //???????????????ai?????????,?????????????????????
            int localPlayerFinalScore = 0;

            for (PlayerControlRecordData controlRecordData : allPlayerControlRecordsData) {

                localPlayerFinalScore += controlRecordData.getFinalScore();
            }
            int aiFinalScore = 0;

            //??????ai??????,??????ai??????????????????????????????,?????????????????????????????????,??????????????????????????????????????????
            for (int i = 0; i < huntingMatchNowData.getAiFetchedControlRecordsInfos().size() && i < allPlayerControlRecordsData.size(); i++) {

                InHuntingMatchAiFetchedControlRecordInfo aiFetchedControlRecordInfo = huntingMatchNowData.getAiFetchedControlRecordsInfos().get(i);
                if (Objects.nonNull(aiFetchedControlRecordInfo)) {
                    aiFinalScore += aiFetchedControlRecordInfo.getFinalScore();
                }
            }
            log.info("??????" + request.getMatchUUID() + "??????????????????" + localPlayerFinalScore + "???ai:" + aiFinalScore);

            boolean playerDirectChangeResult = request.getPlatform().equals(PlatformName.UnityEditor.getPlatform())
                    && !SystemPropertiesConfig.PRODUCTION
                    && (request.getDirectlyWin() != null || request.getDirectlyLose() != null);

            if (!playerDirectChangeResult) {

                if (Math.abs(localPlayerFinalScore - request.getPlayerFinalScore()) > 1 ||
                        Math.abs(aiFinalScore - request.getAiFinalScore()) > 1) {

                    String errorInfo = "??????" + request.getMatchUUID() + ",??????????????????????????????????????????????????????,???????????????,??????:" + localPlayerFinalScore +
                            ",ai:" + aiFinalScore + ",???????????????,??????" + request.getPlayerFinalScore() + ",ai:" + request.getAiFinalScore();


                    //??????????????????????????????????????????????????????
                    if (!systemPropertiesConfig.getProduction()) {
                        throw new BusinessException(errorInfo);
                    } else {
                        log.error(errorInfo);
                        //???????????????????????????????????????
                    }
                } else {
                    //????????????????????????,??????????????????????????????
                    localPlayerFinalScore = request.getPlayerFinalScore();
                    aiFinalScore = request.getAiFinalScore();
                }
            }

            String matchHistoryRef = Path.getHuntingMatchHistoryCollectionPath(request.getGameVersion());

            HuntingMatchHistoryData historyData = new HuntingMatchHistoryData(request.getGameVersion(),
                    huntingMatchNowData.getChapterId(),
                    huntingMatchNowData.getMatchId(),
                    request.getMatchUUID(),
                    roundCount,
                    localPlayerFinalScore,
                    aiFinalScore,
                    playerFireDetails,
                    huntingMatchNowData,
                    huntingMatchNowData.getStartTime(),
                    TimeUtils.getUnixTimeSecond());

            //??????
            huntingMatchService.saveHuntingHistoryMatch(matchHistoryRef, request.getMatchUUID(), historyData);

            log.info("????????????????????????" + request.getMatchUUID());

            //???????????????????????????
            huntingMatchService.removeHuntingMatchNowData(matchPath, request.getUserUid(), request.getMatchUUID());

            UserDataSendToClient sendToClientUserData = GameEnvironment.prepareSendToClientUserData();

            int playerTrophyFrom = 0;
            int playerTrophyTo = 0;
            boolean haveNewChapterUnlocked = false;
            int newUnlockChapterId = -1;
            ChapterWinChestData newCreateChapterWinChestData = null;

            UserData userData = null;

            //??????userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            userData = GameEnvironment.userDataMap.get(request.getUserUid());

            PlayerRecordModeData recordModeData = userData.getServerOnly().getRecordModeData();

            boolean isPlayerWin = localPlayerFinalScore >= aiFinalScore;
            if (playerDirectChangeResult) {
                if (request.getDirectlyWin()) {
                    isPlayerWin = true;
                } else if (request.getDirectlyLose())
                    isPlayerWin = false;
            }

            Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(request.getGameVersion());
            ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getChapterId()));

            if (userData.getTrophy() != null) {
                playerTrophyFrom = userData.getTrophy();
            }
            //????????????
            if (isPlayerWin) {

                if (recordModeData == null) {
                    userData.setCoin(userData.getCoin() + chapterTableValue.getReward());

                    long totalEarnedCoinByMatch = userData.getHistory().getTotalEarnedCoinByMatch() + chapterTableValue.getReward();
                    userData.getHistory().setTotalEarnedCoinByMatch(totalEarnedCoinByMatch);

                    long totalEarnedCoin = userData.getHistory().getTotalEarnedCoin() + chapterTableValue.getReward();
                    userData.getHistory().setTotalEarnedCoin(totalEarnedCoin);

                } else {

                    if (huntingMatchNowData.getRecordModeSelectChapterId() == null) {
                        throw new BusinessException("??????????????????????????????????????????????????????????????????");
                    }

                    ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getRecordModeSelectChapterId()));
                    userData.setCoin(userData.getCoin() + playerSelectChapterTableValue.getReward());
                    userData.getHistory().setTotalEarnedCoinByMatch(userData.getHistory().getTotalEarnedCoinByMatch() + playerSelectChapterTableValue.getReward());
                    userData.getHistory().setTotalEarnedCoin(userData.getHistory().getTotalEarnedCoin() + playerSelectChapterTableValue.getReward());
                }

            }

            //????????????
            if (recordModeData == null) {
                huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(), chapterTableValue, isPlayerWin, request.getGameVersion());
            } else {

                if (huntingMatchNowData.getRecordModeSelectChapterId() == null) {
                    throw new BusinessException("??????????????????????????????????????????????????????????????????");
                }

                ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getRecordModeSelectChapterId()));
                huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(), playerSelectChapterTableValue, isPlayerWin, request.getGameVersion());
            }

            //???????????????????????????
            CheckNewUnlockChapterBO checkNewUnlockChapterBO = huntingMatchService.checkNewUnlockChapter(userData, request.getGameVersion());
            haveNewChapterUnlocked = checkNewUnlockChapterBO.getHaveNewChapterUnlocked();
            newUnlockChapterId = checkNewUnlockChapterBO.getNewUnlockChapterId();

            playerTrophyTo = userData.getTrophy();

            //????????????????????????????????????
            huntingMatchService.recordChapterComplete(userData.getUuid(),
                    chapterTableValue,
                    huntingMatchNowData.getMatchId(),
                    playerFireDetails,
                    isPlayerWin,
                    request.getGameVersion(),
                    allPlayerControlRecordsData);

            //????????????????????????
            achievementService.updateAnimalKillAchievementData(userData.getUuid(),
                    chapterTableValue.getId(),
//                    huntingMatchNowData.getMatchId(),
                    playerFireDetails,
//                    isPlayerWin,
                    request.getGameVersion(),
                    allPlayerControlRecordsData);


            //????????????,???????????????????????????PVP????????????,??????????????????????????????
            if (isPlayerWin
                    && obsoleteUserDataService.isForceTutorialStepComplete(userData.getUuid(), ForceTutorialStepNames.forceCompleteFirstPvPMatch.getName())
                    && !request.getClientBuildInAppInfo().getRecordOnlyMode()) {
                newCreateChapterWinChestData = chestService.tryCreateChapterWinChest(userData.getUuid(), chapterTableValue.getId(), request.getGameVersion());
            }

            //????????????????????????
            huntingMatchService.refreshPlayerHistoryData(userData.getUuid(), chapterTableValue, isPlayerWin, playerFireDetails, request.getGameVersion());

            //????????????????????????
            huntingMatchService.savePlayerChapterLatestMatchScoreCollection(userData.getUuid(), chapterTableValue.getId(), localPlayerFinalScore, request.getGameVersion());

            if (haveNewChapterUnlocked) {

                //??????????????????
                packageDataService.createChapterBonusPackageData(userData, newUnlockChapterId, request.getGameVersion());
            }


            BattleCompleteDto battleCompleteDto = new BattleCompleteDto();
            battleCompleteDto.setUid(request.getUserUid());
            battleCompleteDto.setVersion(request.getGameVersion());
            battleCompleteDto.setPlayerFinalScore(localPlayerFinalScore);
            battleCompleteDto.setAiFinalScore(aiFinalScore);
            battleCompleteDto.setRoundCount(chapterTableValue.getRound());

            ThreadLocalForFight.setBattleCompleteDto(battleCompleteDto);
            //??????????????????
            playerControlRecordDataService.savePlayerUploadControlRecords(request,
                    userData.getUuid(),
                    historyData,
                    request.getAllEncodedControlRecordsData(),
                    request.getAverageFrameRate(), request.getGameVersion());

            battleCompleteDto = ThreadLocalForFight.getBattleCompleteDto() == null ? battleCompleteDto : ThreadLocalForFight.getBattleCompleteDto();
            Map<String, Object> fightInfo = HttpUtil.getFightInfo("http://192.168.30.18:9301/battle/complete", battleCompleteDto);


//            BattleCompleteDto finalBattleCompleteDto = battleCompleteDto;
//            RedisDBOperation.threadPool.execute(() -> {
//                int code = -1;
//                while (code != -1){
//                    Map<String, Object> fightInfo = HttpUtil.getFightInfo("localhost:8080", finalBattleCompleteDto);
//                    code = (int)fightInfo.get("code");
//                }
//            });

            //?????????????????????????????????
            sendToClientUserData.setChapterWinTrophyCountMap(userData.getChapterWinTrophyCountMap());
            sendToClientUserData.setTrophy(userData.getTrophy());
            sendToClientUserData.setPendingUnlockAnimationChapterId(userData.getPendingUnlockAnimationChapterId());
            sendToClientUserData.setUnlockedChapterIds(userData.getUnlockedChapterIds());
            sendToClientUserData.setAchievements(userData.getAchievements());
            sendToClientUserData.setChapterWinChestsData(userData.getChapterWinChestsData());
            sendToClientUserData.setCoin(userData.getCoin());
            sendToClientUserData.setChapterBonusPackagesData(userData.getChapterBonusPackagesData());
            sendToClientUserData.setHistory(userData.getHistory());
//            History history = new History();
//            BeanUtils.copyProperties(userData.getHistory(),history);
//            sendToClientUserData.setHistory(history);


            obsoleteUserDataService.userDataSettlement(userData, sendToClientUserData, true, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);

            map.put("userData", sendToClientUserData);
            map.put("playerTrophyFrom", playerTrophyFrom);
            map.put("playerTrophyTo", playerTrophyTo);

            if (newCreateChapterWinChestData != null) {
                map.put("newCreateChapterWinChestData", newCreateChapterWinChestData);
            }
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("confirmHuntingMatchComplete").add(needTime);
            log.info("[cmd] confirmHuntingMatchComplete finish need time" + (needTime));
            return map;
        } catch (Exception e) {

            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }


    //    @PostMapping("ai-aiControlRecordDataQuery")
    @ApiOperation("????????????,???????????????AI???????????????????????????")
    public Map<String, Object> aiControlRecordDataQuery(@RequestBody AIControlRecordDataQueryDTO request) {

        GameEnvironment.timeMessage.computeIfAbsent("aiControlRecordDataQuery", k -> new ArrayList<>());

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] aiControlRecordDataQuery" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());

            //????????????????????????
            String userUid = request.getUserUid();
            obsoleteUserDataService.checkUserDataExist(userUid);
            UserData userData = GameEnvironment.userDataMap.get(userUid);
            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            PlayerControlRecordData findControlRecordData = null;
            String findControlRecordEncodeData = null;
            //?????????????????????????????????????????????????????????
            //???????????????????????????????????????????????????????????????????????????????????????????????????
            boolean isOpponentDisconnect = false;
            //??????????????????????????????????????????????????????????????????????????????round???????????????ai?????????
            //???HuntingMatchNowData???????????????????????????
            //>=1.0.10?????????????????????????????????????????????????????????????????????????????????

            //????????????????????????
            HuntingMatchNowData huntingMatchNowData = null;
            String path = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
            huntingMatchNowData = huntingMatchService.getHuntingMatchNowData(path, request.getHuntingMatchNowUid());
            if (huntingMatchNowData == null) {
                throw new BusinessException("??????" + request.getUserUid() + "??????AI????????????,???????????????HuntingMatchNowId:" + request.getHuntingMatchNowUid());
            }


            HuntingMatchAIRecordChooseMode aiRecordChooseMode = huntingMatchNowData.getAiRecordChooseMode();


            //????????????
            if (aiRecordChooseMode == HuntingMatchAIRecordChooseMode.WinProneRound ||
                    aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LoseProneRound ||
                    aiRecordChooseMode == HuntingMatchAIRecordChooseMode.drawProneRound
            ) {

                log.info("??????AI????????????????????????." + aiRecordChooseMode);
                if (huntingMatchNowData.getMatchAiRoundRuleTableId() == null) {
                    throw new BusinessException("AI??????????????????????????????huntingMatchNowData.matchAiRoundRuleTableId?????????null");
                }

//                MatchAiRoundControlQuery matchAiControlQuery = new MatchAiRoundControlQuery(
//                        huntingMatchNowData.getMatchAiRoundRuleTableId(),
//                        huntingMatchNowData.getChapterId(),
//                        huntingMatchNowData.getMatchId(),
//                        request.getRound(),
//                        request.getAnimalRouteUid(),
//                        request.getPlayerAverageShowPrecision(),
//                        request.getPlayerFinalScore(),
//                        request.getUserUid(),
//                        huntingMatchNowData.getOpponentPlayerWeaponInfo());
//
//                log.info("??????????????????"+JSONObject.toJSONString(matchAiControlQuery));
//                MatchAiRoundRecordFilterParameters filterParameters = aiService.generateMatchAiRoundControlRecordFilterParameters(matchAiControlQuery,request.getGameVersion(),null);
//                log.info("????????????????????????"+JSONObject.toJSONString(filterParameters));
//                if (filterParameters == null){
//                    throw new BusinessException("??????"+request.getUserUid()+"????????????AI??????????????????");
//                }
//                if (filterParameters.getSafeFailedReason()!=null){
//                    log.info("AI???????????????????????????reason:"+filterParameters.getSafeFailedReason());
//                    return CommonUtils.responsePrepare(null);
//                }

//                RecordDataAndBase64 recordDataAndBase64 = aiService.loadAiControlRecordData(filterParameters, request.getGameVersion());

                RecordDataAndBase64 recordDataAndBase64 = null;
                BattleStartDto battleStartDto = new BattleStartDto();
                Map<String, Object> fightInfo = HttpUtil.getFightInfo("192.168.30.18:9301/battle/start", battleStartDto);
                if (fightInfo != null) {
                    Object data = fightInfo.get("data");
                    Object recordData = JSONObject.parseObject(data.toString()).get("recordDataBase64");
                    recordDataAndBase64 = JSONUtil.toBean(recordData.toString(), RecordDataAndBase64.class);
                    log.warn("????????????ai?????????????????????recordDataAndBase64???{}", JSONUtil.toJsonStr(recordDataAndBase64));
                } else {
                    log.warn("????????????ai?????????????????????recordDataAndBase64??????");
                }


                if (recordDataAndBase64 != null) {
                    findControlRecordData = recordDataAndBase64.getRecordData();
                    findControlRecordEncodeData = recordDataAndBase64.getBase64();
                }
//                if (findControlRecordData==null){
//                    log.error("??????????????????"+matchAiControlQuery+",??????????????????"+filterParameters+", ?????????????????????????????????,match uid"
//                            +huntingMatchNowData.getMatchUid());
//
//                }else{
//                    log.info("???????????????"+findControlRecordData+", final score:"+findControlRecordData.getFinalScore()+", show precision: "
//                            +findControlRecordData.getAverageShowPrecision());
//
//                }
            }
            //????????????
            else if (aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LowLevelWholeMatch ||
                    aiRecordChooseMode == HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch ||
                    aiRecordChooseMode == HuntingMatchAIRecordChooseMode.HighLevelWholeMatch
            ) {
                PlayerUploadWholeMatchControlRecordData wholeMatchControlRecordsData = huntingMatchNowData.getWholeMatchControlRecordsData();

                if (wholeMatchControlRecordsData == null) {
                    throw new BusinessException("??????" + huntingMatchNowData.getMatchUid() + "???AI???????????????????????????wholeMatchControlRecordsData???null");
                }

                int roundIndex = request.getRound() - 1;
                if (roundIndex < 0) {
                    log.error("??????" + huntingMatchNowData.getMatchUid() + "?????????????????????????????????round" + request.getRound() + ",round index" + roundIndex + "??????????????????");
                }

                //?????????????????????????????????
                isOpponentDisconnect = true;
                if (roundIndex >= wholeMatchControlRecordsData.getEncodedBytes_Base64().size()) {
                    isOpponentDisconnect = false;
                } else {
                    for (int i = roundIndex; i < wholeMatchControlRecordsData.getEncodedBytes_Base64().size(); i++) {
                        if (playerControlRecordDataService.checkControlRecordIsValid(wholeMatchControlRecordsData.getEncodedBytes_Base64().get(roundIndex))) {
                            isOpponentDisconnect = false;
                            break;
                        }
                    }
                }

                if (isOpponentDisconnect) {
                    log.info("ai?????????????????????????????????????????????");
                }

                if (!isOpponentDisconnect && roundIndex < wholeMatchControlRecordsData.getEncodedBytes_Base64().size()) {
                    findControlRecordEncodeData = wholeMatchControlRecordsData.getEncodedBytes_Base64().get(roundIndex);
                    String temp = playerControlRecordDataService.decodeControlRecordData(findControlRecordEncodeData);
                    findControlRecordData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
                    log.info("??????????????????AI?????????match uid: {}, round {}, final score:{}", huntingMatchNowData.getMatchUid(), request.getRound(), findControlRecordData.getFinalScore());
                }
            } else {
                throw new BusinessException("????????????ai record choose mode:" + aiRecordChooseMode);
            }

            InHuntingMatchAiFetchedControlRecordInfo aiFetchedDataInfo = new InHuntingMatchAiFetchedControlRecordInfo();

            if (findControlRecordData == null) {

                aiFetchedDataInfo.setUid("");
                aiFetchedDataInfo.setRecordGameVersion("");
                aiFetchedDataInfo.setFinalScore(0);
                aiFetchedDataInfo.setFetchedTime(TimeUtils.getUnixTimeMilliseconds());
            } else {
                aiFetchedDataInfo.setUid(findControlRecordData.getRecordUid());
                aiFetchedDataInfo.setRecordGameVersion(findControlRecordData.getGameVersion());
                aiFetchedDataInfo.setFinalScore(findControlRecordData.getFinalScore());
                aiFetchedDataInfo.setFetchedTime(TimeUtils.getUnixTimeMilliseconds());
            }

            List<InHuntingMatchAiFetchedControlRecordInfo> aiFetchedControlRecordInfos = huntingMatchNowData.getAiFetchedControlRecordsInfos();
            if (aiFetchedControlRecordInfos.size() >= request.getRound()) {
                log.info("??????ai??????aiFetchedControlRecordsInfos????????????" + aiFetchedControlRecordInfos.size() + ">=round" + request.getRound() + ",???????????????????????????????????????");
                aiFetchedControlRecordInfos.add(request.getRound() - 1, aiFetchedDataInfo);
            } else {
                aiFetchedControlRecordInfos.add(aiFetchedDataInfo);
            }
            huntingMatchNowData.setIsOpponentDisconnect(isOpponentDisconnect);

            //??????????????????ai????????????
            String matchPath = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
            huntingMatchService.reSaveHuntingMatchNowData(matchPath, request.getHuntingMatchNowUid(), userUid, huntingMatchNowData);

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, false, request.getGameVersion());
            if (findControlRecordData != null) {
                map.put("recordDataBase64", findControlRecordEncodeData);
            }
            if (isOpponentDisconnect) {
                map.put("isOpponentDisconnect", isOpponentDisconnect);
            }
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("aiControlRecordDataQuery").add(needTime);
            log.info("[cmd] aiControlRecordDataQuery finish need time" + (System.currentTimeMillis() - startTime));
            return map;

        } catch (Exception e) {

            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }


    //    @PostMapping("/login")
    @ApiOperation("????????????")
    public Map<String, Object> login(@RequestBody LoginDTO loginDTO) {

        String loginUserUid = null;
        try {
            GameEnvironment.timeMessage.computeIfAbsent("login", k -> new ArrayList<>());
            ThreadLocalUtil.set(loginDTO.getServerTimeOffset());
            log.warn("????????????????????????????????????{}", loginDTO.getServerTimeOffset());
            log.warn("threadLocal???????????????????????????{}", ThreadLocalUtil.localVar.get());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] login" + System.currentTimeMillis());

            obsoleteUserDataService.createLoginSessionData(loginDTO);

            CommonUtils.requestProcess(loginDTO, false, systemPropertiesConfig.getSupportRecordModeClient());


            //??????????????????????????????
            UserData newUserData = null;
            String privateKey = loginDTO.getPrivateKey();
            boolean isNewUser = false;

            UserData loginUserData = null;
            if (StringUtils.isEmpty(loginDTO.getUserUid())) {
                log.warn("?????????????????????");
                //?????????
                isNewUser = true;
                newUserData = obsoleteUserDataService.createNewPlayer(loginDTO.getGameVersion());
                if (newUserData != null) {
                    log.warn("??????????????????????????????????????????{}", JSONUtil.toJsonStr(newUserData));
                } else {
                    log.warn("???????????????????????????");
                }
                privateKey = newUserData.getServerOnly().getPrivateKey();
                loginUserData = newUserData;
                //??????????????????????????????
                GameEnvironment.userDataMap.put(newUserData.getUuid(), newUserData);
            } else {
                log.warn("???redis???????????????");
                // ???redis???????????????
                //???????????????????????????,?????????????????????????????????????????????
                synchronized (GameEnvironment.userDataMap) {
                    if (GameEnvironment.userDataMap.containsKey(loginDTO.getUserUid())) {
                        throw new BusinessException("?????????????????????");
                    }
                }
                obsoleteUserDataService.checkUserDataExist(loginDTO.getUserUid());
                loginUserData = GameEnvironment.userDataMap.get(loginDTO.getUserUid());

            }

            double giftPackagePopUpRecommendPrice = 0;

            loginUserUid = newUserData == null ? loginDTO.getUserUid() : newUserData.getUuid();

            String userToken = null;

            InitUserDataBO initUserDataBO = obsoleteUserDataService.initUserData(loginUserData, privateKey, loginUserUid, loginDTO);

            loginUserData = initUserDataBO.getUserData();

            userToken = initUserDataBO.getToken();

            System.out.println("userToken:" + userToken);

            giftPackagePopUpRecommendPrice = iapService.getGiftPackagePopUpPriceRecommendPrice(loginUserData);

            obsoleteUserDataService.userDataTransaction(loginUserData, false, loginDTO.getGameVersion());
            loginUserData.getServerOnly().setLastLoginClientVersion(loginDTO.getGameVersion());
            GameEnvironment.userDataMap.remove(loginUserUid);

            boolean disableClientHuntingMatchReport = GameConfig.disableClientHuntingMatchReport;
            log.info("??????" + loginUserUid + "??????,token???" + userToken + ", time" + TimeUtils.getUnixTimeSecond());

            //???????????????
            ClientGameVersion latestClientGameVersion = GameConfig.latestClientGameVersion_Android;

            LuckyWheelV2PropertyTableValue luckyWheelV2PropertyTable = GameEnvironment.luckyWheelV2PropertyTableMap.get(loginDTO.getGameVersion());

            Integer newRequestId = obsoleteUserDataService.getUserMaxRequestIdNow(loginUserUid);

            if (loginDTO.getPlatform().equals(PlatformName.IOS.getPlatform())) {

                latestClientGameVersion = GameConfig.latestClientGameVersion_IOS;

            }


//            if (loginUserData.getLuckyWheelV2Data().getFreeSpinCount() <= 0){
//                loginUserData.getLuckyWheelV2Data().setNextFreeSpinUnixTime(TimeUtils.getUnixTimeSecond() + 1000);
//            }
            if (loginUserData.getLuckyWheelV2Data().getNextFreeSpinUnixTime() <= 0) {
                loginUserData.getLuckyWheelV2Data().setNextFreeSpinUnixTime(TimeUtils.getUnixTimeSecond() + 1000);
            }

            loginUserData.getChapterWinChestsData().removeIf(Objects::isNull);

            GameEnvironment.onlineUser.put(loginUserData.getUuid(), new Date());
            //????????????????????????????????????????????????
            if (!loginUserData.getIsCreateBattleInfo()) {
                UserInfoDto createUserInfo = getCreateUserInfo(loginUserData, loginDTO.getGameVersion());
                Map<String, Object> fightInfo = HttpUtil.getFightInfo("localhost:8080/user/create", createUserInfo);
                if (fightInfo == null || (int) fightInfo.get("code") != 0) {
                    log.error("??????????????????????????????");
                } else {
                    loginUserData.setIsCreateBattleInfo(true);
                }
            } else {
                HttpUtil.getFightInfo("192.168.30.18:9301/user/online", loginUserData.getUuid());
            }


            //????????????
            String latestClientVersion = ClientGameVersion.clientGameVersionEnumToString(latestClientGameVersion);
            ABTestGroup resultGroup = loginUserData.getServerOnly().getAbTestGroup();
            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
            userDataSendToClient.setAvailableFifthDayGunGiftPackageData(loginUserData.getAvailableFifthDayGunGiftPackageData());
            userDataSendToClient.setAvailableGunGiftPackageData(loginUserData.getAvailableGunGiftPackageData());
            userDataSendToClient.setAvailableBulletGiftPackageData(loginUserData.getAvailableBulletGiftPackageData());
            BeanUtils.copyProperties(loginUserData, userDataSendToClient);
            userDataSendToClient.setLuckyWheelV2Data(loginUserData.getLuckyWheelV2Data());
            userDataSendToClient.setChapterWinChestsData(loginUserData.getChapterWinChestsData());
//            userDataSendToClient.getHistory().setServer_only_matchTotalShots(null);
//            userDataSendToClient.getHistory().setServer_only_matchAllShotsPrecisionAccumulation(null);
//            userDataSendToClient.getAdvertisementData().setServer_only_lastRefreshRewardAdCountUnixDay(null);
//            userDataSendToClient.getVipData().setServer_only_lastRefreshLuckyWheelSVipSpinStandardTimeDay(null);
//            userDataSendToClient.getVipData().setServer_only_lastClearLuckyWheelVipSpinCountStandardTimeDay(null);
//            userDataSendToClient.getVipData().setServer_only_lastRefreshLuckyWheelVipSpinStandardTimeDay(null);
            History history = userDataSendToClient.getHistory();
            PlayerAdvertisementData advertisementData = userDataSendToClient.getAdvertisementData();
            PlayerVipData vipData = userDataSendToClient.getVipData();
            CommonUtils.responseRemoveServer(history);
            CommonUtils.responseRemoveServer(advertisementData);
            CommonUtils.responseRemoveServer(vipData);


            Map<String, Object> map = CommonUtils.responsePrepare(null);
            log.warn("??????????????????????????????{}", map.get("serverTime"));
            map.put("userData", userDataSendToClient);
            map.put("userToken", userToken);
            map.put("requestId", newRequestId);
            map.put("abTestGroup", resultGroup.getStatus());
            map.put("disableClientHuntingMatchReport", disableClientHuntingMatchReport);
            map.put("latestClientGameVersion", latestClientVersion);
            map.put("standardTimeOffset", GameConfig.standardTimeZoneOffset);
//            if (TimeUtils.getUnixTimeSecond()-loginUserData.getSignUpTime()>=345600){
//
//            }
            map.put("vipV2FunctionUnlockDay", VipV2Config.unlockVipFunctionAfterSignUpDayCount);
            map.put("luckyWheelV2FunctionUnlockDay", luckyWheelV2PropertyTable.getFunctionEnableDayFromSignUp());
            //????????????????????????
            map.put("giftPackagePopUpRecommendPrice", giftPackagePopUpRecommendPrice);

            if (isNewUser) {
                map.put("privateKey", privateKey);
            }

            obsoleteUserDataService.updateSessionToken(loginUserData, userToken, loginDTO.getRequestRandomId());

            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("login").add(needTime);
            log.info("[cmd] login finish need time" + (needTime));
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("???????????????" + e);
            CommonUtils.responseException(loginDTO, e, loginDTO.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }


    //    @PostMapping("/keepAlive")
    @ApiOperation("????????????")
    public Map<String, Object> keepAlive(@RequestBody BaseDTO baseDTO) {
        GameEnvironment.onlineUser.put(baseDTO.getUserUid(), new Date());
        HttpUtil.getFightInfo("192.168.30.18:9301/user/online", baseDTO.getUserUid());
        return CommonUtils.responsePrepare(null);
    }


    public UserInfoDto getCreateUserInfo(UserData userData, String gameVersion) {
        UserInfoDto userInfoDto = new UserInfoDto();
        UserInfo userInfo = new UserInfo();


        //?????????????????????????????????
        int totalGameCount = 0;
        for (Map.Entry<Integer, Integer> integerIntegerEntry : userData.getChapterEnteredCountMap().entrySet()) {
            totalGameCount += integerIntegerEntry.getValue();
        }
        int winGameCount = 0;
        for (Map.Entry<Integer, Integer> integerIntegerEntry : userData.getChapterWinCountMap().entrySet()) {
            winGameCount += integerIntegerEntry.getValue();
        }
        BattleInfo battleInfo = new BattleInfo
                (userData.getTrophy(),
                        userData.getHistory().getCurrentMatchWinStreak(),
                        userData.getHistory().getCurrentMatchLoseStreak(),
                        winGameCount * 1.0 / totalGameCount,
                        userData.getHistory().getMatchAverageHitPrecision(),
                        0, winGameCount, totalGameCount - winGameCount, totalGameCount
                );
        userInfo.setBattleInfo(battleInfo);

        //??????chapterInfo
        ChapterInfo chapterInfo = new ChapterInfo();
        Map<String, ChapterBattleInfo> chapterBattleInfos = new HashMap<>();
        for (Map.Entry<Integer, Integer> integerIntegerEntry : userData.getChapterEnteredCountMap().entrySet()) {
            ChapterBattleInfo chapterBattleInfo = new ChapterBattleInfo();
            List<Integer> playerChapterLatestMatchScore = huntingMatchService.getPlayerChapterLatestMatchScore(integerIntegerEntry.getKey(), userData.getUuid(), gameVersion);
            chapterBattleInfo.setLastFiveScores(playerChapterLatestMatchScore);
            chapterBattleInfo.setBattleCount(integerIntegerEntry.getValue());
            chapterBattleInfos.put(integerIntegerEntry.getKey().toString(), chapterBattleInfo);
        }
        chapterInfo.setChapterBattleInfos(chapterBattleInfos);
        userInfo.setChapterInfo(chapterInfo);

        //??????recordInfo
        RecordInfo recordInfo = new RecordInfo();
        PlayerRecordModeData recordModeData = userData.getServerOnly().getRecordModeData();
        if (recordModeData != null) {
            Map<String, RecordModeMatchTableValue> recordModeMatchTable = GameEnvironment.recordModeMatchTableMap.get(gameVersion);
            RecordModeMatchTableValue recordModeMatchTableValue = recordModeMatchTable.get(String.valueOf(recordModeData.getRecordModeMatchTableId()));
            PlayerWeaponInfo playerWeaponInfo = new PlayerWeaponInfo(recordModeMatchTableValue.getPlayerGunId(), recordModeMatchTableValue.getPlayerGunLevel(), recordModeMatchTableValue.getPlayerBulletId());
            recordInfo.setRecordModeMatchId(recordModeData.getRecordModeMatchTableId());
            recordInfo.setPlayerWeaponInfo(playerWeaponInfo);
        }
        userInfo.setRecordInfo(recordInfo);

        userInfoDto.setVersion(gameVersion);
        userInfoDto.setUid(userData.getUuid());
        userInfoDto.setUserInfo(userInfo);
        return userInfoDto;
    }


    public static void main(String[] args) {
        System.out.println(10 * 1.0 / 4);
    }


}
