package org.skynet.service.provider.hunting.obsolete.controller.game;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.battle.data.BattleCompleteInfoCVO;
import org.skynet.components.hunting.battle.data.BattleMatchingInfoCVO;
import org.skynet.components.hunting.battle.domain.MatchPlayerInfo;
import org.skynet.components.hunting.battle.enums.BattleMode;
import org.skynet.components.hunting.battle.query.CompleteQuery;
import org.skynet.components.hunting.battle.query.MatchingQuery;
import org.skynet.components.hunting.battle.service.BattleFeignService;
import org.skynet.components.hunting.rank.league.message.AddCoinMessage;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.domain.ChapterWinChestData;
import org.skynet.components.hunting.user.domain.PlayerRecordModeData;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.HttpUtil;
import org.skynet.service.provider.hunting.obsolete.common.util.NanoIdUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.GameConfig;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.enums.ForceTutorialStepNames;
import org.skynet.service.provider.hunting.obsolete.enums.HuntingMatchAIRecordChooseMode;
import org.skynet.service.provider.hunting.obsolete.enums.PlatformName;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.module.dto.BattleCompleteDto;
import org.skynet.service.provider.hunting.obsolete.module.dto.BattleMatchDto;
import org.skynet.service.provider.hunting.obsolete.module.entity.Defender;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.CheckNewUnlockChapterBO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.ConfirmHuntingMatchCompleteDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.ConfirmHuntingMatchStartDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.ChapterTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.MatchAIRoundRuleTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.RecordModeMatchTableValue;
import org.skynet.service.provider.hunting.obsolete.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Api(tags = "??????")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class HuntingMatchController {

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

    @Value("${skynet.rocketmq.rank-league.topic.addCoinTopic}")
    private String rankLeagueAddCoinTopic;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private BattleFeignService battleFeignService;

//    @PostMapping("/huntingMatch-confirmHuntingMatchComplete")
//    @ApiOperation("????????????????????????")
//    public Map<String,Object>confirmHuntingMatchComplete(@RequestBody ConfirmHuntingMatchCompleteDTO request){
//        try{
//            GameEnvironment.timeMessage.computeIfAbsent("confirmHuntingMatchComplete", k -> new ArrayList<>());
//            ThreadLocalUtil.set(request.getServerTimeOffset());
//            long startTime = System.currentTimeMillis();
//            log.info("[cmd] confirmHuntingMatchComplete"+System.currentTimeMillis());
//            log.info(JSONObject.toJSONString(request));
//            CommonUtils.requestProcess(request,null,systemPropertiesConfig.getSupportRecordModeClient());
////            userDataService.ensureUserDataIdempotence(request.getUserUid(),request.getUserDataUpdateCount(),request.getGameVersion());
//            //?????????????????????????????????,???????????????
//            String matchPath  = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//
//            HuntingMatchNowData huntingMatchNowData = huntingMatchService.getHuntingMatchNowData(matchPath, request.getMatchUUID());
//
//            if (huntingMatchNowData==null){
//                throw new BusinessException("??????"+request.getMatchUUID()+"?????????");
//            }
//
//            int roundCount = request.getAllEncodedControlRecordsData().size();
//            List<PlayerControlRecordData> allPlayerControlRecordsData = new ArrayList<>();
//
//            for (String roundEncodedData : request.getAllEncodedControlRecordsData()) {
//                String temp = playerControlRecordDataService.decodeControlRecordData(roundEncodedData);
//                PlayerControlRecordData controlRecordData = JSONObject.parseObject(temp,PlayerControlRecordData.class);
//                allPlayerControlRecordsData.add(controlRecordData);
//            }
//
//            List<PlayerFireDetails> playerFireDetails = huntingMatchService.generateFireDetailsFromControlRecordData(allPlayerControlRecordsData);
//
//            //???????????????ai?????????,?????????????????????
//            int localPlayerFinalScore = 0;
//
//            for (PlayerControlRecordData controlRecordData : allPlayerControlRecordsData) {
//
//                localPlayerFinalScore += controlRecordData.getFinalScore();
//            }
//            int aiFinalScore = 0;
//
//            //??????ai??????,??????ai??????????????????????????????,?????????????????????????????????,??????????????????????????????????????????
//            for (int i=0;i<huntingMatchNowData.getAiFetchedControlRecordsInfos().size()&&i<allPlayerControlRecordsData.size();i++){
//
//                InHuntingMatchAiFetchedControlRecordInfo aiFetchedControlRecordInfo = huntingMatchNowData.getAiFetchedControlRecordsInfos().get(i);
//                if(Objects.nonNull(aiFetchedControlRecordInfo)){
//                    aiFinalScore += aiFetchedControlRecordInfo.getFinalScore();
//                }
//            }
//            log.info("??????"+request.getMatchUUID()+"??????????????????"+localPlayerFinalScore+"???ai:"+aiFinalScore);
//
//            boolean playerDirectChangeResult = request.getPlatform().equals(PlatformName.UnityEditor.getPlatform())
//                    &&!SystemPropertiesConfig.PRODUCTION
//                    &&(request.getDirectlyWin() != null || request.getDirectlyLose()!=null);
//
//            if (!playerDirectChangeResult){
//
//                if (Math.abs(localPlayerFinalScore - request.getPlayerFinalScore()) > 1||
//                    Math.abs(aiFinalScore - request.getAiFinalScore())>1){
//
//                    String errorInfo = "??????"+request.getMatchUUID()+",??????????????????????????????????????????????????????,???????????????,??????:"+localPlayerFinalScore+
//                            ",ai:"+aiFinalScore+",???????????????,??????"+request.getPlayerFinalScore()+",ai:"+request.getAiFinalScore();
//
//
//                    //??????????????????????????????????????????????????????
//                    if (!systemPropertiesConfig.getProduction()){
//                        throw new BusinessException(errorInfo);
//                    }else {
//                        log.error(errorInfo);
//                        //???????????????????????????????????????
//                    }
//                }else {
//                    //????????????????????????,??????????????????????????????
//                    localPlayerFinalScore = request.getPlayerFinalScore();
//                    aiFinalScore = request.getAiFinalScore();
//                }
//            }
//
//            String matchHistoryRef = Path.getHuntingMatchHistoryCollectionPath(request.getGameVersion());
//
//            HuntingMatchHistoryData historyData = new HuntingMatchHistoryData(request.getGameVersion(),
//                    huntingMatchNowData.getChapterId(),
//                    huntingMatchNowData.getMatchId(),
//                    request.getMatchUUID(),
//                    roundCount,
//                    localPlayerFinalScore,
//                    aiFinalScore,
//                    playerFireDetails,
//                    huntingMatchNowData,
//                    huntingMatchNowData.getStartTime(),
//                    TimeUtils.getUnixTimeSecond());
//
//            //??????
//            huntingMatchService.saveHuntingHistoryMatch(matchHistoryRef, request.getMatchUUID(),historyData);
//
//            log.info("????????????????????????"+request.getMatchUUID());
//
//            //???????????????????????????
//            huntingMatchService.removeHuntingMatchNowData(matchPath, request.getUserUid(),request.getMatchUUID());
//
//            UserDataSendToClient sendToClientUserData = GameEnvironment.prepareSendToClientUserData();
//
//            int playerTrophyFrom = 0;
//            int playerTrophyTo = 0;
//            boolean haveNewChapterUnlocked = false;
//            int newUnlockChapterId = -1;
//            ChapterWinChestData newCreateChapterWinChestData = null;
//
//            UserData userData = null;
//
//            //??????userData
//            userDataService.checkUserDataExist(request.getUserUid());
//            userData = GameEnvironment.userDataMap.get(request.getUserUid());
//
//            PlayerRecordModeData recordModeData = userData.getServerOnly().getRecordModeData();
//
//            boolean isPlayerWin = localPlayerFinalScore >= aiFinalScore;
//            if (playerDirectChangeResult){
//                if (request.getDirectlyWin()){
//                    isPlayerWin = true;
//                } else if (request.getDirectlyLose())
//                    isPlayerWin = false;
//            }
//
//            Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(request.getGameVersion());
//            ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getChapterId()));
//
//            if (userData.getTrophy()!=null){
//                playerTrophyFrom = userData.getTrophy();
//            }
//            //????????????
//            if (isPlayerWin){
//
//                if (recordModeData == null){
//                    userData.setCoin(userData.getCoin()+chapterTableValue.getReward());
//
//                    long totalEarnedCoinByMatch = userData.getHistory().getTotalEarnedCoinByMatch() + chapterTableValue.getReward();
//                    userData.getHistory().setTotalEarnedCoinByMatch(totalEarnedCoinByMatch);
//
//                    long totalEarnedCoin  = userData.getHistory().getTotalEarnedCoin() + chapterTableValue.getReward();
//                    userData.getHistory().setTotalEarnedCoin(totalEarnedCoin);
//
//                }else {
//
//                    if (huntingMatchNowData.getRecordModeSelectChapterId() == null){
//                        throw new BusinessException("??????????????????????????????????????????????????????????????????");
//                    }
//
//                    ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getRecordModeSelectChapterId()));
//                    userData.setCoin(userData.getCoin()+playerSelectChapterTableValue.getReward());
//                    userData.getHistory().setTotalEarnedCoinByMatch(userData.getHistory().getTotalEarnedCoinByMatch()+playerSelectChapterTableValue.getReward());
//                    userData.getHistory().setTotalEarnedCoin(userData.getHistory().getTotalEarnedCoin()+playerSelectChapterTableValue.getReward());
//                }
//
//            }
//
//            //????????????
//            if (recordModeData == null){
//                huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(),chapterTableValue,isPlayerWin,request.getGameVersion() );
//            }else {
//
//                if (huntingMatchNowData.getRecordModeSelectChapterId() == null){
//                    throw new BusinessException("??????????????????????????????????????????????????????????????????");
//                }
//
//                ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getRecordModeSelectChapterId()));
//                huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(),playerSelectChapterTableValue,isPlayerWin,request.getGameVersion() );
//            }
//
//            //???????????????????????????
//            CheckNewUnlockChapterBO checkNewUnlockChapterBO = huntingMatchService.checkNewUnlockChapter(userData,request.getGameVersion());
//            haveNewChapterUnlocked = checkNewUnlockChapterBO.getHaveNewChapterUnlocked();
//            newUnlockChapterId = checkNewUnlockChapterBO.getNewUnlockChapterId();
//
//            playerTrophyTo = userData.getTrophy();
//
//            //????????????????????????????????????
//            huntingMatchService.recordChapterComplete(userData.getUuid(),
//                    chapterTableValue,
//                    huntingMatchNowData.getMatchId(),
//                    playerFireDetails,
//                    isPlayerWin,
//                    request.getGameVersion(),
//                    allPlayerControlRecordsData);
//
//            //????????????????????????
//            achievementService.updateAnimalKillAchievementData(userData.getUuid(),
//                    chapterTableValue.getId(),
////                    huntingMatchNowData.getMatchId(),
//                    playerFireDetails,
////                    isPlayerWin,
//                    request.getGameVersion(),
//                    allPlayerControlRecordsData);
//
//
//
//            //????????????,???????????????????????????PVP????????????,??????????????????????????????
//            if (isPlayerWin
//                    && userDataService.isForceTutorialStepComplete(userData.getUuid(),ForceTutorialStepNames.forceCompleteFirstPvPMatch.getName())
//                    &&!request.getClientBuildInAppInfo().getRecordOnlyMode()){
//                newCreateChapterWinChestData = chestService.tryCreateChapterWinChest(userData.getUuid(),chapterTableValue.getId(),request.getGameVersion());
//            }
//
//            //????????????????????????
//            huntingMatchService.refreshPlayerHistoryData(userData.getUuid(),chapterTableValue,isPlayerWin,playerFireDetails,request.getGameVersion());
//
//            //????????????????????????
//            huntingMatchService.savePlayerChapterLatestMatchScoreCollection(userData.getUuid(),chapterTableValue.getId(),localPlayerFinalScore,request.getGameVersion());
//
//            if (haveNewChapterUnlocked){
//
//                //??????????????????
//                packageDataService.createChapterBonusPackageData(userData,newUnlockChapterId,request.getGameVersion());
//            }
//
//            //??????????????????
//            playerControlRecordDataService.savePlayerUploadControlRecords(request,
//                    userData.getUuid(),
//                    historyData,
//                    request.getAllEncodedControlRecordsData(),
//                    request.getAverageFrameRate(), request.getGameVersion());
//
//
//
//            //?????????????????????????????????
//            sendToClientUserData.setChapterWinTrophyCountMap(userData.getChapterWinTrophyCountMap());
//            sendToClientUserData.setTrophy(userData.getTrophy());
//            sendToClientUserData.setPendingUnlockAnimationChapterId(userData.getPendingUnlockAnimationChapterId());
//            sendToClientUserData.setUnlockedChapterIds(userData.getUnlockedChapterIds());
//            sendToClientUserData.setAchievements(userData.getAchievements());
//            sendToClientUserData.setChapterWinChestsData(userData.getChapterWinChestsData());
//            sendToClientUserData.setCoin(userData.getCoin());
//            sendToClientUserData.setChapterBonusPackagesData(userData.getChapterBonusPackagesData());
//            sendToClientUserData.setHistory(userData.getHistory());
////            History history = new History();
////            BeanUtils.copyProperties(userData.getHistory(),history);
////            sendToClientUserData.setHistory(history);
//
//
//            userDataService.userDataSettlement(userData, sendToClientUserData,true,request.getGameVersion());
//            Map<String, Object> map = CommonUtils.responsePrepare(null);
//
//            map.put("userData",sendToClientUserData);
//            map.put("playerTrophyFrom",playerTrophyFrom);
//            map.put("playerTrophyTo",playerTrophyTo);
//
//            if (newCreateChapterWinChestData!=null){
//                map.put("newCreateChapterWinChestData",newCreateChapterWinChestData);
//            }
//            long needTime = System.currentTimeMillis() - startTime;
//            GameEnvironment.timeMessage.get("confirmHuntingMatchComplete").add(needTime);
//            log.info("[cmd] confirmHuntingMatchComplete finish need time"+(needTime));
//            return map;
//        }catch (Exception e){
//
//            CommonUtils.responseException(request,e,request.getUserUid());
//        }finally {
//            ThreadLocalUtil.remove();
//        }
//        return null;
//    }


    public void test1() {

    }
//    @PostMapping("/huntingMatch-confirmHuntingMatchStart")
//    @ApiOperation("????????????????????????")
//    public Map<String,Object> confirmHuntingMatchStart(@RequestBody ConfirmHuntingMatchStartDTO request){
//        try {
//            GameEnvironment.timeMessage.computeIfAbsent("confirmHuntingMatchStart", k -> new ArrayList<>());
//            ThreadLocalUtil.set(request.getServerTimeOffset());
//            long startTime = System.currentTimeMillis();
//            log.info("[cmd] confirmHuntingMatchStart"+System.currentTimeMillis());
//            log.info(JSONObject.toJSONString(request));
//            CommonUtils.requestProcess(request,null,systemPropertiesConfig.getSupportRecordModeClient());
////            userDataService.ensureUserDataIdempotence(request.getUserUid(),request.getUserDataUpdateCount(),request.getGameVersion());
//
//            String playerUUID = request.getUserUid();
//            Integer playerSelectChapterId = request.getChapterId();
//
//            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
//
//            PlayerWeaponInfo localPlayerWeaponInfo = null;
//            PlayerWeaponInfo opponentPlayerWeaponInfo = null;
//            boolean isLocalPlayerFirst = true;
//            OpponentPlayerInfo opponentPlayerInfo = null;
//            Integer matchId = null;
//
//            HuntingMatchAIRecordChooseMode aiRecordChooseMode = HuntingMatchAIRecordChooseMode.WinProneRound;
//            MatchAIRoundRuleTableValue matchAiRoundRuleTableValue = null;
//            PlayerUploadWholeMatchControlRecordData wholeMatchControlRecordsData = null;
//            PlayerRecordModeData recordModeData = null;
//
//            userDataService.checkUserDataExist(playerUUID);
//            UserData userData = GameEnvironment.userDataMap.get(playerUUID);
//
//            recordModeData = userData.getServerOnly().getRecordModeData();
//
//            if (recordModeData != null){
//
//                Map<String, RecordModeMatchTableValue> recordModeMatchTable = GameEnvironment.recordModeMatchTableMap.get(request.getGameVersion());
//                RecordModeMatchTableValue recordModeMatchTableValue = recordModeMatchTable.get(String.valueOf(recordModeData.getRecordModeMatchTableId()));
//                //??????????????????????????????????????????
//                playerSelectChapterId = recordModeMatchTableValue.getChapterId();
//            }
//
//            Integer realChapterId = request.getChapterId();
//            Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(request.getGameVersion());
//            ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(realChapterId));
//
//            if (userData.getCoin() < chapterTableValue.getEntryFee()&&recordModeData == null){
//
//                throw new BusinessException("??????"+playerUUID+"????????????,??????????????????");
//            }
//
//            //????????????????????????
//            Map<Integer, Integer> chapterEnteredCountMap = userData.getChapterEnteredCountMap();
//            int chapterEnteredCount = chapterEnteredCountMap.getOrDefault(request.getChapterId(),0);
//            chapterEnteredCount += 1;
//            chapterEnteredCountMap.put(realChapterId,chapterEnteredCount);
//            userDataSendToClient.setChapterEnteredCountMap(userData.getChapterEnteredCountMap());
//
//            //?????????????????????????????????
//            //????????????????????????????????????+1????????????-1
//            matchId  = huntingMatchService.determineEnterWhichMatch(playerUUID, realChapterId, chapterEnteredCount - 1,request.getGameVersion());
//
//            //???????????????
//            localPlayerWeaponInfo = huntingMatchService.generateLocalPlayerWeaponInfo(userData,request.getGameVersion());
//
//            //??????????????????
//            int entryFee = chapterTableValue.getEntryFee();
//            if (recordModeData != null){
//                ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(playerSelectChapterId));
//
//                if (Arrays.asList(GameConfig.recordModeFreeFeeChapterIdsArray).contains(playerSelectChapterId)){
//                    entryFee = 0;
//                }else {
//                    entryFee = playerSelectChapterTableValue.getEntryFee();
//                }
//                log.info("????????????,???????????????"+entryFee);
//            }
//            userData.setCoin(userData.getCoin() - entryFee);
//            userData.setCoin(Math.max(userData.getCoin(),0));
//            userDataSendToClient.setCoin(userData.getCoin());
//
//            //??????AI??????????????????
//            aiRecordChooseMode = huntingMatchService.determineAIRecordChooseMode(playerUUID,String.valueOf(request.getChapterId()),request.getGameVersion());
//
//            if (aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.HighLevelWholeMatch)
//                    ||aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch)
//                    ||aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.LowLevelWholeMatch))
//            {
//                log.info("ai??????????????????????????????,mode:"+aiRecordChooseMode);
//                //??????????????????
//                isLocalPlayerFirst = NumberUtils.randomFloat(0.0,1.0)<=0.5;
//                wholeMatchControlRecordsData = aiService.queryWholeMatchControlRecords(playerUUID,aiRecordChooseMode, playerSelectChapterId,matchId,request.getGameVersion());
//                if (wholeMatchControlRecordsData !=null){
//                    opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByWholeMatchRecordsData(wholeMatchControlRecordsData);
//                }
//            }
//
//            //???????????????????????????????????????????????????????????????????????????????????????
//            if (opponentPlayerWeaponInfo==null&&
//                    (aiRecordChooseMode==HuntingMatchAIRecordChooseMode.HighLevelWholeMatch||
//                            aiRecordChooseMode==HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch||
//                            aiRecordChooseMode==HuntingMatchAIRecordChooseMode.LowLevelWholeMatch)){
//
//                log.info("??????????????????????????????????????????????????????");
//                /**
//                 * ??????????????????????????????????????????????????????????????????????????????????????????
//                 * ??????????????????????????????????????????????????????????????????
//                 */
//                HuntingMatchAIRecordChooseMode aiRecordChooseModeBefore = aiRecordChooseMode;
//                aiRecordChooseMode = aiService.convertAiRecordChooseModeFromWholeMatchToRound(aiRecordChooseMode);
//                log.info(aiRecordChooseModeBefore+"?????????????????????????????????????????????????????? "+aiRecordChooseMode);
//            }
//
//            if (aiRecordChooseMode==HuntingMatchAIRecordChooseMode.WinProneRound||
//                    aiRecordChooseMode==HuntingMatchAIRecordChooseMode.LoseProneRound||
//                    aiRecordChooseMode==HuntingMatchAIRecordChooseMode.drawProneRound
//            ){
//
//                log.info("ai???????????????????????????mode:"+aiRecordChooseMode);
//                //??????????????????MatchAiRuleTable??????
//                matchAiRoundRuleTableValue = huntingMatchService.determineMatchAiRoundRuleTableValue(request.getChapterId(),chapterEnteredCount,aiRecordChooseMode,request.getGameVersion());
//
//                if (matchAiRoundRuleTableValue == null){
//                    throw new BusinessException("?????????????????????matchAiRoundRuleTableValue");
//                }
//
//                log.info("?????????MatchAIRoundRuleTable??????"+JSONObject.toJSONString(matchAiRoundRuleTableValue));
//                opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByMatchAiRoundRule(matchAiRoundRuleTableValue,request.getGameVersion());
//                isLocalPlayerFirst = matchAiRoundRuleTableValue.getIsPlayerFirst();
//            }
//
//            //????????????
//            if (recordModeData==null){
//                huntingMatchService.consumeBullet(userData, localPlayerWeaponInfo.getBulletId(),request.getGameVersion());
//                userDataSendToClient.setBulletCountMap(userData.getBulletCountMap());
//                userDataSendToClient.setEquippedBulletId(userData.getEquippedBulletId());
//            }
//
//            //????????????ai??????
//            //????????????
//            if (recordModeData==null){
//
//                Integer count = wholeMatchControlRecordsData==null?null:wholeMatchControlRecordsData.getSafePlayerTrophyCount();
//                opponentPlayerInfo = huntingMatchService.generateOpponentPlayerInfo(userData,chapterTableValue.getId(), count, request.getGameVersion());
//            }
//            //????????????
//            else {
//                log.info("?????????????????????????????????"+playerSelectChapterId);
//                opponentPlayerInfo = huntingMatchService.generateOpponentPlayerInfo(userData, request.getChapterId(), null,request.getGameVersion() );
//            }
//
//            userDataSendToClient.setHistory(userData.getHistory());
//            userDataService.userDataSettlement(userData, userDataSendToClient,true,request.getGameVersion());
//            Map<String, Object> map = CommonUtils.responsePrepare(null);
//            String huntingMatchUUID = NanoIdUtils.randomNanoId(30);
//
//            //?????????????????????????????????,?????????????????????,????????????
//            HuntingMatchNowData huntingMatchNowData = new HuntingMatchNowData(
//                    huntingMatchUUID,
//                    playerUUID,
//                    playerSelectChapterId,
//                    null,
//                    matchId,
//                    localPlayerWeaponInfo,
//                    opponentPlayerWeaponInfo,
//                    TimeUtils.getUnixTimeSecond(),
//                    new ArrayList<>(),
//                    false,
//                    aiRecordChooseMode,
//                    null,
//                    null);
//
//            if (recordModeData != null){
//                log.info("?????????????????????????????????????????????"+request.getChapterId());
//                huntingMatchNowData.setRecordModeSelectChapterId(request.getChapterId());
//            }
//
//            if (matchAiRoundRuleTableValue != null){
//                huntingMatchNowData.setMatchAiRoundRuleTableId(matchAiRoundRuleTableValue.getId());
//            }
//
//            if (wholeMatchControlRecordsData != null){
//                huntingMatchNowData.setWholeMatchControlRecordsData(wholeMatchControlRecordsData);
//            }
//
//            //??????????????????????????????
//            String matchPath  = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//            huntingMatchService.saveHuntingMatchNowData(matchPath,huntingMatchUUID,playerUUID,huntingMatchNowData);
//
//            log.info("????????????,"+huntingMatchNowData);
//
//            RangeFloat confirmHuntingMatchStartElapseTimeRange = new RangeFloat(GameConfig.confirmHuntingMatchStartMinElapseTimeRangeArray.get_min(),
//                    GameConfig.confirmHuntingMatchStartMinElapseTimeRangeArray.get_max());
//
//            double playerMatchingTime = confirmHuntingMatchStartElapseTimeRange.random();
//
//            long elapseTime = Math.max(0, TimeUtils.getUnixTimeSecond() - request.getClientTime());
//            double needWaitTime = Math.max(0, playerMatchingTime - elapseTime);
//
//
//            map.put("userData",userDataSendToClient);
//            map.put("matchUid",huntingMatchUUID);
//            map.put("chapterId", playerSelectChapterId);
//            map.put("matchId",matchId);
//            map.put("localPlayerWeaponInfo",localPlayerWeaponInfo);
//            map.put("opponentPlayerWeaponInfo",opponentPlayerWeaponInfo);
//            map.put("isLocalPlayerFirst",isLocalPlayerFirst);
//            map.put("opponentPlayerInfo",opponentPlayerInfo);
//            map.put("aiRecordChooseMode",aiRecordChooseMode);
//            long needTime = System.currentTimeMillis() - startTime;
//            GameEnvironment.timeMessage.get("confirmHuntingMatchStart").add(needTime);
//            log.info("[cmd] confirmHuntingMatchStart finish need time"+(System.currentTimeMillis()-startTime));
//            return map;
//        }catch (Exception e){
//
//            CommonUtils.responseException(request,e,request.getUserUid());
//        }finally {
//            ThreadLocalUtil.remove();
//        }
//
//        return null;
//    }

    public void test2() {

    }


    @PostMapping("/huntingMatch-confirmHuntingMatchStart")
    @ApiOperation("????????????????????????")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> confirmHuntingMatchStart(@RequestBody ConfirmHuntingMatchStartDTO request) {
        try {
            // String fightUrl = systemPropertiesConfig.getFightUrl();
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
//            matchId  = huntingMatchService.determineEnterWhichMatch(playerUUID, realChapterId, chapterEnteredCount - 1,request.getGameVersion());

            double playerCultivateScore = obsoleteUserDataService.calculatePlayerCultivateScore(request.getUserUid(), request.getGameVersion());
            double cultivateWinRateAddition = Math.max(0, Math.min(playerCultivateScore, chapterTableValue.getMaxCultivateScore()) - chapterTableValue.getMinCultivateScore()) * chapterTableValue.getMaxCultivateWinRateAddition() /
                    Math.max(0, chapterTableValue.getMaxCultivateScore() - chapterTableValue.getMinCultivateScore());

            int gunId = userData.getEquippedGunId();
            int gunLevel = userData.getGunLevelMap().get(gunId);
            int bulletId = userData.getEquippedBulletId();
            BattleMatchDto matchDto = new BattleMatchDto(playerSelectChapterId, cultivateWinRateAddition, new PlayerWeaponInfo(gunId, gunLevel, bulletId), userData.getServerOnly().getRecordModeData() != null, userData.getTrophy());
            matchDto.setVersion(request.getGameVersion());
            matchDto.setUid(request.getUserUid());
//            http://192.168.2.199:9301
            //47.88.90.222
            // Map<String, Object> fightInfo = HttpUtil.getFightInfo(fightUrl + "/battle/matching", matchDto);
            Result<BattleMatchingInfoCVO> matchingResult = battleFeignService.matching(MatchingQuery.builder()
                    .version(request.getGameVersion())
                    .userId(request.getUserUid())
                    .chapterId(playerSelectChapterId)
                    .trophyCount(userData.getTrophy())
                    .cultivateWinRateAddition((float) cultivateWinRateAddition)
                    .playerWeaponInfo(MatchPlayerInfo.builder()
                            .gunId(gunId)
                            .gunLevel(gunLevel)
                            .bulletId(bulletId)
                            .build())
                    .isRecordMode(userData.getServerOnly().getRecordModeData() != null)
                    .battleMode(userData.getServerOnly().getRecordModeData() != null ? BattleMode.RECORD : BattleMode.NORMAL)
                    .build());
            if (matchingResult.failed()) {
                // return matchingResult.build();
                throw new BusinessException("????????????????????????????????????");
            }

            // if (fightInfo == null) {
            //     throw new BusinessException("????????????????????????????????????");
            // }

            //???????????????
            // JSONObject data = JSONObject.parseObject(fightInfo.get("data").toString());
            BattleMatchingInfoCVO battleMatchingInfoCVO = matchingResult.getData();
//            MatchInfo matchInfo = JSONUtil.toBean(data.get("matchInfo").toString(), MatchInfo.class);
            MatchPlayerInfo attacker = battleMatchingInfoCVO.getAttacker();
            localPlayerWeaponInfo = new PlayerWeaponInfo(attacker.getGunId(), attacker.getGunLevel(), attacker.getBulletId());

            //todo ??????????????????matchId
            matchId = battleMatchingInfoCVO.getMatchId(); //(Integer) data.get("matchId");

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

//            //??????AI??????????????????
//            aiRecordChooseMode = huntingMatchService.determineAIRecordChooseMode(playerUUID,String.valueOf(request.getChapterId()),request.getGameVersion());
//
//            if (aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.HighLevelWholeMatch)
//                    ||aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch)
//                    ||aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.LowLevelWholeMatch))
//            {
//                log.info("ai??????????????????????????????,mode:"+aiRecordChooseMode);
//                //??????????????????
//                isLocalPlayerFirst = NumberUtils.randomFloat(0.0,1.0)<=0.5;
//                wholeMatchControlRecordsData = aiService.queryWholeMatchControlRecords(playerUUID,aiRecordChooseMode, playerSelectChapterId,matchId,request.getGameVersion());
//                if (wholeMatchControlRecordsData !=null){
//                    opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByWholeMatchRecordsData(wholeMatchControlRecordsData);
//                }
//            }
//
//            //???????????????????????????????????????????????????????????????????????????????????????
//            if (opponentPlayerWeaponInfo==null&&
//                    (aiRecordChooseMode==HuntingMatchAIRecordChooseMode.HighLevelWholeMatch||
//                            aiRecordChooseMode==HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch||
//                            aiRecordChooseMode==HuntingMatchAIRecordChooseMode.LowLevelWholeMatch)){
//
//                log.info("??????????????????????????????????????????????????????");
//                /**
//                 * ??????????????????????????????????????????????????????????????????????????????????????????
//                 * ??????????????????????????????????????????????????????????????????
//                 */
//                HuntingMatchAIRecordChooseMode aiRecordChooseModeBefore = aiRecordChooseMode;
//                aiRecordChooseMode = aiService.convertAiRecordChooseModeFromWholeMatchToRound(aiRecordChooseMode);
//                log.info(aiRecordChooseModeBefore+"?????????????????????????????????????????????????????? "+aiRecordChooseMode);
//            }
//
//            if (aiRecordChooseMode==HuntingMatchAIRecordChooseMode.WinProneRound||
//                    aiRecordChooseMode==HuntingMatchAIRecordChooseMode.LoseProneRound||
//                    aiRecordChooseMode==HuntingMatchAIRecordChooseMode.drawProneRound
//            ){
//
//                log.info("ai???????????????????????????mode:"+aiRecordChooseMode);
//                //??????????????????MatchAiRuleTable??????
//                matchAiRoundRuleTableValue = huntingMatchService.determineMatchAiRoundRuleTableValue(request.getChapterId(),chapterEnteredCount,aiRecordChooseMode,request.getGameVersion());
//
//                if (matchAiRoundRuleTableValue == null){
//                    throw new BusinessException("?????????????????????matchAiRoundRuleTableValue");
//                }
//
//                log.info("?????????MatchAIRoundRuleTable??????"+JSONObject.toJSONString(matchAiRoundRuleTableValue));
////                opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByMatchAiRoundRule(matchAiRoundRuleTableValue,request.getGameVersion());
//                isLocalPlayerFirst = matchAiRoundRuleTableValue.getIsPlayerFirst();
//            }

            //todo ?????????????????????,?????????????????????
            isLocalPlayerFirst = BooleanUtil.isTrue(battleMatchingInfoCVO.getIsLocalPlayerFirst());// data.get("isLocalPlayerFirst") == null || (boolean) data.get("isLocalPlayerFirst");

            // Defender defender = JSONUtil.toBean(data.get("defender").toString(), Defender.class);
            MatchPlayerInfo defender = battleMatchingInfoCVO.getDefender();
            opponentPlayerWeaponInfo = new PlayerWeaponInfo(defender.getGunId(), defender.getGunLevel(), defender.getBulletId());
            //????????????
            if (recordModeData == null) {
                huntingMatchService.consumeBullet(userData, localPlayerWeaponInfo.getBulletId(), request.getGameVersion());
                userDataSendToClient.setBulletCountMap(userData.getBulletCountMap());
                userDataSendToClient.setEquippedBulletId(userData.getEquippedBulletId());
            }

            // Integer opponentTrophyCount;
            // opponentTrophyCount = (Integer) data.get("safePlayerTrophyCount");
            //????????????ai??????
            //????????????
            if (recordModeData == null) {
                //todo ?????????????????????,??????????????????
                //?????????????????????????????????null???????????????????????????null??????????????????
                opponentPlayerInfo = huntingMatchService.generateOpponentPlayerInfo(userData, chapterTableValue.getId(), defender.getTrophyCount(), request.getGameVersion());
            }
            //????????????
            else {
                log.info("?????????????????????????????????" + playerSelectChapterId);
                opponentPlayerInfo = huntingMatchService.generateOpponentPlayerInfo(userData, request.getChapterId(), defender.getTrophyCount(), request.getGameVersion());
            }

            userDataSendToClient.setHistory(userData.getHistory());
            obsoleteUserDataService.userDataSettlement(userData, userDataSendToClient, true, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            String huntingMatchUUID = NanoIdUtils.randomNanoId(30);


//            //?????????????????????????????????,?????????????????????,????????????
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
                huntingMatchNowData.setRecordModeSelectChapterId(playerSelectChapterId);
            }
//
//            if (matchAiRoundRuleTableValue != null){
//                huntingMatchNowData.setMatchAiRoundRuleTableId(matchAiRoundRuleTableValue.getId());
//            }
//
//            if (wholeMatchControlRecordsData != null){
//                huntingMatchNowData.setWholeMatchControlRecordsData(wholeMatchControlRecordsData);
//            }

            //??????????????????????????????
            String matchPath = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
            huntingMatchService.saveHuntingMatchNowData(matchPath, huntingMatchUUID, playerUUID, huntingMatchNowData);

//            log.info("????????????,"+huntingMatchNowData);

            //todo ???????????????uid
//            String matchUid = (String) data.get("matchUid");

//            RangeFloat confirmHuntingMatchStartElapseTimeRange = new RangeFloat(GameConfig.confirmHuntingMatchStartMinElapseTimeRangeArray.get_min(),
//                    GameConfig.confirmHuntingMatchStartMinElapseTimeRangeArray.get_max());

//            double playerMatchingTime = confirmHuntingMatchStartElapseTimeRange.random();
//
//            long elapseTime = Math.max(0, TimeUtils.getUnixTimeSecond() - request.getClientTime());
//            double needWaitTime = Math.max(0, playerMatchingTime - elapseTime);


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

            // TODO ????????????????????????
            String sv = JSON.toJSONString(map, SerializerFeature.WriteMapNullValue);
            String siv = JSON.toJSONString(map);
            if (sv.length() != siv.length()) {
                map = JSON.parseObject(siv);
                log.warn("???????????????????????????:{}", siv);
            }

            return map;
        } catch (Exception e) {

            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }


    @PostMapping("/huntingMatch-confirmHuntingMatchComplete")
    @ApiOperation("????????????????????????")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> confirmHuntingMatchComplete(@RequestBody ConfirmHuntingMatchCompleteDTO request) {
        String userUid = request.getUserUid();
        try {
            GameEnvironment.timeMessage.computeIfAbsent("confirmHuntingMatchComplete", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] confirmHuntingMatchComplete" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
//            userDataService.ensureUserDataIdempotence(request.getUserUid(),request.getUserDataUpdateCount(),request.getGameVersion());

            // String fightUrl = systemPropertiesConfig.getFightUrl();
            //?????????????????????????????????,???????????????
            String matchPath = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//
            HuntingMatchNowData huntingMatchNowData = huntingMatchService.getHuntingMatchNowData(matchPath, request.getMatchUUID());
//
//            if (huntingMatchNowData==null){
//                throw new BusinessException("??????"+request.getMatchUUID()+"?????????");
//            }

            int roundCount = request.getAllEncodedControlRecordsData().size();
            List<PlayerControlRecordData> allPlayerControlRecordsData = new ArrayList<>();

            for (String roundEncodedData : request.getAllEncodedControlRecordsData()) {
                String temp = playerControlRecordDataService.decodeControlRecordData(roundEncodedData);
                PlayerControlRecordData controlRecordData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
                allPlayerControlRecordsData.add(controlRecordData);
            }

            List<PlayerFireDetails> playerFireDetails = huntingMatchService.generateFireDetailsFromControlRecordData(allPlayerControlRecordsData);


            BattleCompleteInfoCVO data = null;


            BattleCompleteDto battleCompleteDto = new BattleCompleteDto();
            battleCompleteDto.setUid(request.getUserUid());
            battleCompleteDto.setVersion(request.getGameVersion());
            battleCompleteDto.setPlayerFinalScore(request.getPlayerFinalScore());
            battleCompleteDto.setAiFinalScore(request.getAiFinalScore());
            battleCompleteDto.setRoundCount(roundCount);
            battleCompleteDto.setDirectlyWin(request.getDirectlyWin());
            battleCompleteDto.setPlayerDirectChangeResult(request.getPlayerDirectChangeResult());
            if (systemPropertiesConfig.getProduction() || !request.getPlatform().equals(PlatformName.UnityEditor.getPlatform())) {
                battleCompleteDto.setRoundReportData(request.getAllEncodedControlRecordsData());
//            http://192.168.2.199:9301
                if (!(request.getDirectlyWin() != null || request.getDirectlyLose() != null)) {
                    // Map<String, Object> fightInfo = HttpUtil.getFightInfo(fightUrl + "/battle/complete", battleCompleteDto);
                    Result<BattleCompleteInfoCVO> completeResult = battleFeignService.complete(CompleteQuery.builder()
                            .version(request.getGameVersion())
                            .userId(request.getUserUid())
                            .playerFinalScore(request.getPlayerFinalScore())
                            .aiFinalScore(request.getAiFinalScore())
                            .totalRoundCount(roundCount)
                            .roundReportData(request.getAllEncodedControlRecordsData())
                            .playerDirectChangeResult(request.getPlayerDirectChangeResult())
                            .directlyWin(request.getDirectlyWin())
                            .build());

//                if (fightInfo  == null){
//                    throw new BusinessException("????????????????????????????????????");
//                }
//                     if (fightInfo != null) {
//                         data = JSONObject.parseObject(fightInfo.get("data").toString());
//                     }
                    if (completeResult.getSuccess()) {
                        data = completeResult.getData();
                    }
//                if (data  == null){

//                    throw new BusinessException("????????????????????????????????????");
//                }
                }
            } else {
                log.info("?????????????????????????????????");
                battleCompleteDto.setRoundReportData(new ArrayList<>());
            }


//            JSONObject data = null;
//            http://192.168.2.199:9301
//            if(!(request.getDirectlyWin() != null || request.getDirectlyLose()!=null)){
//                Map<String, Object> fightInfo = HttpUtil.getFightInfo("http://47.88.90.222:9301/battle/complete", battleCompleteDto);
////                if (fightInfo  == null){
////                    throw new BusinessException("????????????????????????????????????");
////                }
//                if (fightInfo != null){
//                    data = JSONObject.parseObject(fightInfo.get("data").toString());
//                }
////                if (data  == null){
////                    throw new BusinessException("????????????????????????????????????");
////                }
//            }

            //???????????????ai?????????,?????????????????????
            int localPlayerFinalScore = 0;

            for (PlayerControlRecordData controlRecordData : allPlayerControlRecordsData) {

                localPlayerFinalScore += controlRecordData.getFinalScore();
            }
            int aiFinalScore = request.getAiFinalScore();
            if (data != null) {
                // if (data.get("aiFinalScore") != null) {
                //     aiFinalScore = (int) data.get("aiFinalScore");
                // }

                aiFinalScore = data.getAiFinalScore();
            }


//            //??????ai??????,??????ai??????????????????????????????,?????????????????????????????????,??????????????????????????????????????????
//            for (int i=0;i<huntingMatchNowData.getAiFetchedControlRecordsInfos().size()&&i<allPlayerControlRecordsData.size();i++){
//
//                InHuntingMatchAiFetchedControlRecordInfo aiFetchedControlRecordInfo = huntingMatchNowData.getAiFetchedControlRecordsInfos().get(i);
//                if(Objects.nonNull(aiFetchedControlRecordInfo)){
//                    aiFinalScore += aiFetchedControlRecordInfo.getFinalScore();
//                }
//            }
            log.info("??????" + request.getMatchUUID() + "??????????????????" + localPlayerFinalScore + "???ai:" + aiFinalScore);

            boolean playerDirectChangeResult = request.getPlatform().equals(PlatformName.UnityEditor.getPlatform())
                    && !SystemPropertiesConfig.PRODUCTION
                    && (request.getDirectlyWin() != null || request.getDirectlyLose() != null);

            if (!playerDirectChangeResult) {

                if (Math.abs(localPlayerFinalScore - request.getPlayerFinalScore()) > 1 || Math.abs(aiFinalScore - request.getAiFinalScore()) > 1) {
                    String errorInfo = "??????" + request.getMatchUUID() + ",??????????????????????????????????????????????????????,???????????????,??????:" + localPlayerFinalScore + ",ai:" + aiFinalScore + ",???????????????,??????" + request.getPlayerFinalScore() + ",ai:" + request.getAiFinalScore();
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

//            String matchHistoryRef = Path.getHuntingMatchHistoryCollectionPath(request.getGameVersion());

//            HuntingMatchHistoryData historyData = new HuntingMatchHistoryData(request.getGameVersion(),
//                    huntingMatchNowData.getChapterId(),
//                    huntingMatchNowData.getMatchId(),
//                    request.getMatchUUID(),
//                    roundCount,
//                    localPlayerFinalScore,
//                    aiFinalScore,
//                    playerFireDetails,
//                    huntingMatchNowData,
//                    huntingMatchNowData.getStartTime(),
//                    TimeUtils.getUnixTimeSecond());

            //??????
//            huntingMatchService.saveHuntingHistoryMatch(matchHistoryRef, request.getMatchUUID(),historyData);

//            log.info("????????????????????????"+request.getMatchUUID());

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

            //todo ??????????????????chapterId
            Integer chapterId = huntingMatchNowData.getChapterId();
            if (data != null) {
                chapterId = data.getChapterId();//(Integer) data.get("chapterId");
            }

            Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(request.getGameVersion());
            ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(chapterId));

            if (userData.getTrophy() != null) {
                playerTrophyFrom = userData.getTrophy();
            }

            Long lastCoin = userData.getCoin();

            //????????????
            if (isPlayerWin) {

                userData.setCoin(userData.getCoin() + chapterTableValue.getReward());

                long totalEarnedCoinByMatch = userData.getHistory().getTotalEarnedCoinByMatch() + chapterTableValue.getReward();
                userData.getHistory().setTotalEarnedCoinByMatch(totalEarnedCoinByMatch);

                long totalEarnedCoin = userData.getHistory().getTotalEarnedCoin() + chapterTableValue.getReward();
                userData.getHistory().setTotalEarnedCoin(totalEarnedCoin);
//                if (recordModeData == null){
//                    userData.setCoin(userData.getCoin()+chapterTableValue.getReward());
//
//                    long totalEarnedCoinByMatch = userData.getHistory().getTotalEarnedCoinByMatch() + chapterTableValue.getReward();
//                    userData.getHistory().setTotalEarnedCoinByMatch(totalEarnedCoinByMatch);
//
//                    long totalEarnedCoin  = userData.getHistory().getTotalEarnedCoin() + chapterTableValue.getReward();
//                    userData.getHistory().setTotalEarnedCoin(totalEarnedCoin);
//
//                }else {
//                    //????????????????????????id
//                    if (huntingMatchNowData.getRecordModeSelectChapterId() == null){
//                        throw new BusinessException("??????????????????????????????????????????????????????????????????");
//                    }
//
//                    ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getRecordModeSelectChapterId()));
//                    userData.setCoin(userData.getCoin()+playerSelectChapterTableValue.getReward());
//                    userData.getHistory().setTotalEarnedCoinByMatch(userData.getHistory().getTotalEarnedCoinByMatch()+playerSelectChapterTableValue.getReward());
//                    userData.getHistory().setTotalEarnedCoin(userData.getHistory().getTotalEarnedCoin()+playerSelectChapterTableValue.getReward());
//                }
            }

            //????????????
            huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(), chapterTableValue, isPlayerWin, request.getGameVersion());

//            if (recordModeData == null){
//                huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(),chapterTableValue,isPlayerWin,request.getGameVersion() );
//            }else {
//
//                if (huntingMatchNowData.getRecordModeSelectChapterId() == null){
//                    throw new BusinessException("??????????????????????????????????????????????????????????????????");
//                }
//
//                //todo ?????????????????????id
//                ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getRecordModeSelectChapterId()));
//                huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(),playerSelectChapterTableValue,isPlayerWin,request.getGameVersion() );
//            }

            //???????????????????????????
            CheckNewUnlockChapterBO checkNewUnlockChapterBO = huntingMatchService.checkNewUnlockChapter(userData, request.getGameVersion());
            haveNewChapterUnlocked = checkNewUnlockChapterBO.getHaveNewChapterUnlocked();
            newUnlockChapterId = checkNewUnlockChapterBO.getNewUnlockChapterId();

            playerTrophyTo = userData.getTrophy();

            //????????????????????????????????????
            //todo ???????????????matchId
            Integer matchId = huntingMatchNowData.getMatchId();
            if (data != null) {
                matchId = data.getMatchId();//(Integer) data.get("matchId");
            }

            huntingMatchService.recordChapterComplete(userData.getUuid(),
                    chapterTableValue,
                    matchId,
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
            // huntingMatchService.savePlayerChapterLatestMatchScoreCollection(userData.getUuid(), chapterTableValue.getId(), localPlayerFinalScore, request.getGameVersion()); // ????????????

            if (haveNewChapterUnlocked) {
                //??????????????????
                packageDataService.createChapterBonusPackageData(userData, newUnlockChapterId, request.getGameVersion());
            }


//            BattleCompleteDto battleCompleteDto = new BattleCompleteDto();
//            battleCompleteDto.setUid(request.getUserUid());
//            battleCompleteDto.setVersion(request.getGameVersion());
//            battleCompleteDto.setPlayerFinalScore(request.getPlayerFinalScore());
//            battleCompleteDto.setAiFinalScore(request.getAiFinalScore());
//            battleCompleteDto.setRoundCount(chapterTableValue.getRound());
//            battleCompleteDto.setRoundReportData(request.getAllEncodedControlRecordsData());

//            ThreadLocalForFight.setBattleCompleteDto(battleCompleteDto);
//            //??????????????????
//            playerControlRecordDataService.savePlayerUploadControlRecords(request,
//                    userData.getUuid(),
//                    historyData,
//                    request.getAllEncodedControlRecordsData(),
//                    request.getAverageFrameRate(), request.getGameVersion());
//
//            battleCompleteDto = ThreadLocalForFight.getBattleCompleteDto()==null?battleCompleteDto:ThreadLocalForFight.getBattleCompleteDto();
//            if (battleCompleteDto.getRoundCount() == null){
//                battleCompleteDto.setRoundReportData(new ArrayList<>());
//            }
//            Map<String, Object> fightInfo = HttpUtil.getFightInfo("http://192.168.30.18:9301/battle/complete", battleCompleteDto);


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

            //???????????????????????????
            huntingMatchService.removeHuntingMatchNowData(matchPath, request.getUserUid(), request.getMatchUUID());

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

            if (lastCoin < userData.getCoin()) {
                Long addCount = userData.getCoin().longValue() - lastCoin;
                // String rankUrl = systemPropertiesConfig.getRankUrl();
                // if (!StringUtils.isBlank(rankUrl)) {
                //     Map<String, Object> fightInfo = HttpUtil.rankAddCoin(rankUrl + "/add/coins", new HashMap<String, Object>() {{
                //         put("userId", request.getUserUid());
                //         put("coin", addCount);
                //     }});
                // }

                AddCoinMessage addCoinMessage = AddCoinMessage.builder().userId(request.getUserUid()).coin(addCount).build();
                rocketMQTemplate.asyncSend(rankLeagueAddCoinTopic, addCoinMessage, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        // log.info("async onSucess SendResult={}", sendResult);
                        // log.info("????????????????????????????????????{}", addCoinMessage);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        log.info("????????????????????????????????????{}", addCoinMessage);
                    }
                });
            }

            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            GameEnvironment.userDataMap.remove(userUid);
            ThreadLocalUtil.remove();
        }
        // Map<String, Object> map = CommonUtils.responsePrepare(null);
        // log.info("?????????????????????????????????????????????????????????????????????????????????");
        // return map;
        return null;
    }


}
