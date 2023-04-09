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

@Api(tags = "战斗模块相关操作")
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
    @ApiOperation("确认进入章节比赛")
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
                //录制模式，更改玩家选择的章节
                playerSelectChapterId = recordModeMatchTableValue.getChapterId();
            }

            Integer realChapterId = request.getChapterId();
            Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(request.getGameVersion());
            ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(realChapterId));

            if (userData.getCoin() < chapterTableValue.getEntryFee() && recordModeData == null) {

                throw new BusinessException("用户" + playerUUID + "金币不足,无法开始比赛");
            }

            //更新章节进入次数
            Map<Integer, Integer> chapterEnteredCountMap = userData.getChapterEnteredCountMap();
            int chapterEnteredCount = chapterEnteredCountMap.getOrDefault(request.getChapterId(), 0);
            chapterEnteredCount += 1;
            chapterEnteredCountMap.put(realChapterId, chapterEnteredCount);
            userDataSendToClient.setChapterEnteredCountMap(userData.getChapterEnteredCountMap());

            //确定进入哪场比赛和动物
            //因为上面章节进入次数已经+1了，这里-1
            matchId = huntingMatchService.determineEnterWhichMatch(playerUUID, realChapterId, chapterEnteredCount - 1, request.getGameVersion());

            double playerCultivateScore = obsoleteUserDataService.calculatePlayerCultivateScore(request.getUserUid(), request.getGameVersion());
            double cultivateWinRateAddition = Math.max(0, Math.min(playerCultivateScore, chapterTableValue.getMaxCultivateScore()) - chapterTableValue.getMinCultivateScore()) * chapterTableValue.getMaxCultivateWinRateAddition() /
                    Math.max(0, chapterTableValue.getMaxCultivateScore() - chapterTableValue.getMinCultivateScore());

            int gunId = userData.getEquippedGunId();
            int gunLevel = userData.getGunLevelMap().get(gunId);
            int bulletId = userData.getEquippedBulletId();
            BattleMatchDto matchDto = new BattleMatchDto(request.getChapterId(), cultivateWinRateAddition, new PlayerWeaponInfo(gunId, gunLevel, bulletId), userData.getServerOnly().getRecordModeData() != null, userData.getTrophy());
            Map<String, Object> fightInfo = HttpUtil.getFightInfo("battle/complete/battle/matching", matchDto);

            //玩家的武器
            MatchInfo matchInfo = JSONUtil.toBean(fightInfo.get("matchInfo").toString(), MatchInfo.class);
            localPlayerWeaponInfo = matchInfo.getAttacker();
            opponentPlayerWeaponInfo = matchInfo.getDefender();
//            localPlayerWeaponInfo = huntingMatchService.generateLocalPlayerWeaponInfo(userData,request.getGameVersion());

            //扣除入场费用
            int entryFee = chapterTableValue.getEntryFee();
            if (recordModeData != null) {
                ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(playerSelectChapterId));

                if (Arrays.asList(GameConfig.recordModeFreeFeeChapterIdsArray).contains(playerSelectChapterId)) {
                    entryFee = 0;
                } else {
                    entryFee = playerSelectChapterTableValue.getEntryFee();
                }
                log.info("录制模式,入场费改为" + entryFee);
            }
            userData.setCoin(userData.getCoin() - entryFee);
            userData.setCoin(Math.max(userData.getCoin(), 0));
            userDataSendToClient.setCoin(userData.getCoin());

            //本次AI录像查找模式
            aiRecordChooseMode = huntingMatchService.determineAIRecordChooseMode(playerUUID, String.valueOf(request.getChapterId()), request.getGameVersion());

            if (aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.HighLevelWholeMatch)
                    || aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch)
                    || aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.LowLevelWholeMatch)) {
                log.info("ai操作根据完整比赛获取,mode:" + aiRecordChooseMode);
                //玩家随机先手
                isLocalPlayerFirst = NumberUtils.randomFloat(0.0, 1.0) <= 0.5;
                wholeMatchControlRecordsData = aiService.queryWholeMatchControlRecords(playerUUID, aiRecordChooseMode, playerSelectChapterId, matchId, request.getGameVersion());
                if (wholeMatchControlRecordsData != null) {
                    opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByWholeMatchRecordsData(wholeMatchControlRecordsData);
                }
            }

            //如果整局比赛匹配模式没有匹配到任何信息，则转为回合匹配模式
            if (opponentPlayerWeaponInfo == null &&
                    (aiRecordChooseMode == HuntingMatchAIRecordChooseMode.HighLevelWholeMatch ||
                            aiRecordChooseMode == HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch ||
                            aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LowLevelWholeMatch)) {

                log.info("无法找到完整比赛录像，切换为回合匹配");
                /**
                 * 如果是普通完整匹配和高水平完整匹配，则改为倾向失败的回合匹配
                 * 如果是保底完整匹配，则改为倾向胜利的回合匹配
                 */
                HuntingMatchAIRecordChooseMode aiRecordChooseModeBefore = aiRecordChooseMode;
                aiRecordChooseMode = aiService.convertAiRecordChooseModeFromWholeMatchToRound(aiRecordChooseMode);
                log.info(aiRecordChooseModeBefore + "无法找到完整比赛录像，切换为回合匹配 " + aiRecordChooseMode);
            }

            if (aiRecordChooseMode == HuntingMatchAIRecordChooseMode.WinProneRound ||
                    aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LoseProneRound ||
                    aiRecordChooseMode == HuntingMatchAIRecordChooseMode.drawProneRound
            ) {

                log.info("ai操作根据回合匹配。mode:" + aiRecordChooseMode);
                //确定使用哪个MatchAiRuleTable数据
                matchAiRoundRuleTableValue = huntingMatchService.determineMatchAiRoundRuleTableValue(request.getChapterId(), chapterEnteredCount, aiRecordChooseMode, request.getGameVersion());

                if (matchAiRoundRuleTableValue == null) {
                    throw new BusinessException("无法找到合适的matchAiRoundRuleTableValue");
                }

                log.info("匹配到MatchAIRoundRuleTable条目" + JSONObject.toJSONString(matchAiRoundRuleTableValue));
//                opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByMatchAiRoundRule(matchAiRoundRuleTableValue,request.getGameVersion());
                isLocalPlayerFirst = matchAiRoundRuleTableValue.getIsPlayerFirst();
            }


            //使用子弹
            if (recordModeData == null) {
                huntingMatchService.consumeBullet(userData, localPlayerWeaponInfo.getBulletId(), request.getGameVersion());
                userDataSendToClient.setBulletCountMap(userData.getBulletCountMap());
                userDataSendToClient.setEquippedBulletId(userData.getEquippedBulletId());
            }

            //生成匹配ai信息
            //正常模式
            if (recordModeData == null) {

                Integer count = wholeMatchControlRecordsData == null ? null : wholeMatchControlRecordsData.getSafePlayerTrophyCount();
                opponentPlayerInfo = huntingMatchService.generateOpponentPlayerInfo(userData, chapterTableValue.getId(), count, request.getGameVersion());
            }
            //录制模式
            else {
                log.info("生成对手信息，来自章节" + playerSelectChapterId);
                opponentPlayerInfo = huntingMatchService.generateOpponentPlayerInfo(userData, request.getChapterId(), null, request.getGameVersion());
            }

            userDataSendToClient.setHistory(userData.getHistory());
            obsoleteUserDataService.userDataSettlement(userData, userDataSendToClient, true, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            String huntingMatchUUID = NanoIdUtils.randomNanoId(30);

            //记录玩家正在进行的比赛,结束比赛的时候,需要验证
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
                log.info("录制模式，记录玩家选择的章节：" + request.getChapterId());
                huntingMatchNowData.setRecordModeSelectChapterId(request.getChapterId());
            }

            if (matchAiRoundRuleTableValue != null) {
                huntingMatchNowData.setMatchAiRoundRuleTableId(matchAiRoundRuleTableValue.getId());
            }

            if (wholeMatchControlRecordsData != null) {
                huntingMatchNowData.setWholeMatchControlRecordsData(wholeMatchControlRecordsData);
            }

            //记录这个比赛正在进行
            String matchPath = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
            huntingMatchService.saveHuntingMatchNowData(matchPath, huntingMatchUUID, playerUUID, huntingMatchNowData);

            log.info("开始比赛," + huntingMatchNowData);

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


//     //    @PostMapping("/huntingMatch-confirmHuntingMatchComplete")
//     @ApiOperation("确认章节比赛结束")
//     public Map<String, Object> confirmHuntingMatchComplete(@RequestBody ConfirmHuntingMatchCompleteDTO request) {
//         try {
//             GameEnvironment.timeMessage.computeIfAbsent("confirmHuntingMatchComplete", k -> new ArrayList<>());
//             ThreadLocalUtil.set(request.getServerTimeOffset());
//             long startTime = System.currentTimeMillis();
//             log.info("[cmd] confirmHuntingMatchComplete" + System.currentTimeMillis());
//             log.info(JSONObject.toJSONString(request));
//             CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
// //            userDataService.ensureUserDataIdempotence(request.getUserUid(),request.getUserDataUpdateCount(),request.getGameVersion());
//             //验证确实开始了这个比赛,防止刷数据
//             String matchPath = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//
//             HuntingMatchNowData huntingMatchNowData = huntingMatchService.getHuntingMatchNowData(matchPath, request.getMatchUUID());
//
//             if (huntingMatchNowData == null) {
//                 throw new BusinessException("比赛" + request.getMatchUUID() + "不存在");
//             }
//
//             int roundCount = request.getAllEncodedControlRecordsData().size();
//             List<PlayerControlRecordData> allPlayerControlRecordsData = new ArrayList<>();
//
//             for (String roundEncodedData : request.getAllEncodedControlRecordsData()) {
//                 String temp = playerControlRecordDataService.decodeControlRecordData(roundEncodedData);
//                 PlayerControlRecordData controlRecordData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
//                 allPlayerControlRecordsData.add(controlRecordData);
//             }
//
//             List<PlayerFireDetails> playerFireDetails = huntingMatchService.generateFireDetailsFromControlRecordData(allPlayerControlRecordsData);
//
//             //计算玩家和ai的分数,从录制文件计算
//             int localPlayerFinalScore = 0;
//
//             for (PlayerControlRecordData controlRecordData : allPlayerControlRecordsData) {
//
//                 localPlayerFinalScore += controlRecordData.getFinalScore();
//             }
//             int aiFinalScore = 0;
//
//             //如果ai先手,那么ai会预加载多一局的数据,所以这里计算分数的时候,取的录制文件不要多于玩家局数
//             for (int i = 0; i < huntingMatchNowData.getAiFetchedControlRecordsInfos().size() && i < allPlayerControlRecordsData.size(); i++) {
//
//                 InHuntingMatchAiFetchedControlRecordInfo aiFetchedControlRecordInfo = huntingMatchNowData.getAiFetchedControlRecordsInfos().get(i);
//                 if (Objects.nonNull(aiFetchedControlRecordInfo)) {
//                     aiFinalScore += aiFetchedControlRecordInfo.getFinalScore();
//                 }
//             }
//             log.info("比赛" + request.getMatchUUID() + "分数，玩家：" + localPlayerFinalScore + "，ai:" + aiFinalScore);
//
//             boolean playerDirectChangeResult = request.getPlatform().equals(PlatformName.UnityEditor.getPlatform())
//                     && !SystemPropertiesConfig.PRODUCTION
//                     && (request.getDirectlyWin() != null || request.getDirectlyLose() != null);
//
//             if (!playerDirectChangeResult) {
//
//                 if (Math.abs(localPlayerFinalScore - request.getPlayerFinalScore()) > 1 ||
//                         Math.abs(aiFinalScore - request.getAiFinalScore()) > 1) {
//
//                     String errorInfo = "比赛" + request.getMatchUUID() + ",玩家上报分数和录像计算分数结果不一致,录像计算中,玩家:" + localPlayerFinalScore +
//                             ",ai:" + aiFinalScore + ",玩家上报中,玩家" + request.getPlayerFinalScore() + ",ai:" + request.getAiFinalScore();
//
//
//                     //不是正式服务器抛出错误，尽早检查错误
//                     if (!systemPropertiesConfig.getProduction()) {
//                         throw new BusinessException(errorInfo);
//                     } else {
//                         log.error(errorInfo);
//                         //看需不需要另外作为对象存储
//                     }
//                 } else {
//                     //分数检查没有问题,就以玩家上报分数为准
//                     localPlayerFinalScore = request.getPlayerFinalScore();
//                     aiFinalScore = request.getAiFinalScore();
//                 }
//             }
//
//             String matchHistoryRef = Path.getHuntingMatchHistoryCollectionPath(request.getGameVersion());
//
//             HuntingMatchHistoryData historyData = new HuntingMatchHistoryData(request.getGameVersion(),
//                     huntingMatchNowData.getChapterId(),
//                     huntingMatchNowData.getMatchId(),
//                     request.getMatchUUID(),
//                     roundCount,
//                     localPlayerFinalScore,
//                     aiFinalScore,
//                     playerFireDetails,
//                     huntingMatchNowData,
//                     huntingMatchNowData.getStartTime(),
//                     TimeUtils.getUnixTimeSecond());
//
//             //存档
//             huntingMatchService.saveHuntingHistoryMatch(matchHistoryRef, request.getMatchUUID(), historyData);
//
//             log.info("记录比赛历史数据" + request.getMatchUUID());
//
//             //删除正在比赛的数据
//             huntingMatchService.removeHuntingMatchNowData(matchPath, request.getUserUid(), request.getMatchUUID());
//
//             UserDataSendToClient sendToClientUserData = GameEnvironment.prepareSendToClientUserData();
//
//             int playerTrophyFrom = 0;
//             int playerTrophyTo = 0;
//             boolean haveNewChapterUnlocked = false;
//             int newUnlockChapterId = -1;
//             ChapterWinChestData newCreateChapterWinChestData = null;
//
//             UserData userData = null;
//
//             //处理userData
//             obsoleteUserDataService.checkUserDataExist(request.getUserUid());
//             userData = GameEnvironment.userDataMap.get(request.getUserUid());
//
//             PlayerRecordModeData recordModeData = userData.getServerOnly().getRecordModeData();
//
//             boolean isPlayerWin = localPlayerFinalScore >= aiFinalScore;
//             if (playerDirectChangeResult) {
//                 if (request.getDirectlyWin()) {
//                     isPlayerWin = true;
//                 } else if (request.getDirectlyLose())
//                     isPlayerWin = false;
//             }
//
//             Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(request.getGameVersion());
//             ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getChapterId()));
//
//             if (userData.getTrophy() != null) {
//                 playerTrophyFrom = userData.getTrophy();
//             }
//             //玩家金币
//             if (isPlayerWin) {
//
//                 if (recordModeData == null) {
//                     userData.setCoin(userData.getCoin() + chapterTableValue.getReward());
//
//                     long totalEarnedCoinByMatch = userData.getHistory().getTotalEarnedCoinByMatch() + chapterTableValue.getReward();
//                     userData.getHistory().setTotalEarnedCoinByMatch(totalEarnedCoinByMatch);
//
//                     long totalEarnedCoin = userData.getHistory().getTotalEarnedCoin() + chapterTableValue.getReward();
//                     userData.getHistory().setTotalEarnedCoin(totalEarnedCoin);
//
//                 } else {
//
//                     if (huntingMatchNowData.getRecordModeSelectChapterId() == null) {
//                         throw new BusinessException("录制模式，但是比赛数据没有玩家选择的章节信息");
//                     }
//
//                     ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getRecordModeSelectChapterId()));
//                     userData.setCoin(userData.getCoin() + playerSelectChapterTableValue.getReward());
//                     userData.getHistory().setTotalEarnedCoinByMatch(userData.getHistory().getTotalEarnedCoinByMatch() + playerSelectChapterTableValue.getReward());
//                     userData.getHistory().setTotalEarnedCoin(userData.getHistory().getTotalEarnedCoin() + playerSelectChapterTableValue.getReward());
//                 }
//
//             }
//
//             //玩家奖杯
//             if (recordModeData == null) {
//                 huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(), chapterTableValue, isPlayerWin, request.getGameVersion());
//             } else {
//
//                 if (huntingMatchNowData.getRecordModeSelectChapterId() == null) {
//                     throw new BusinessException("录制模式，但是比赛数据没有玩家选择的章节信息");
//                 }
//
//                 ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getRecordModeSelectChapterId()));
//                 huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(), playerSelectChapterTableValue, isPlayerWin, request.getGameVersion());
//             }
//
//             //看是否有新章节解锁
//             CheckNewUnlockChapterBO checkNewUnlockChapterBO = huntingMatchService.checkNewUnlockChapter(userData, request.getGameVersion());
//             haveNewChapterUnlocked = checkNewUnlockChapterBO.getHaveNewChapterUnlocked();
//             newUnlockChapterId = checkNewUnlockChapterBO.getNewUnlockChapterId();
//
//             playerTrophyTo = userData.getTrophy();
//
//             //记录玩家完成章节比赛次数
//             huntingMatchService.recordChapterComplete(userData.getUuid(),
//                     chapterTableValue,
//                     huntingMatchNowData.getMatchId(),
//                     playerFireDetails,
//                     isPlayerWin,
//                     request.getGameVersion(),
//                     allPlayerControlRecordsData);
//
//             //记录动物击杀成就
//             achievementService.updateAnimalKillAchievementData(userData.getUuid(),
//                     chapterTableValue.getId(),
// //                    huntingMatchNowData.getMatchId(),
//                     playerFireDetails,
// //                    isPlayerWin,
//                     request.getGameVersion(),
//                     allPlayerControlRecordsData);
//
//
//             //如果胜利,且已经完成了第一次PVP匹配教学,获得一个章节胜利宝箱
//             if (isPlayerWin
//                     && obsoleteUserDataService.isForceTutorialStepComplete(userData.getUuid(), ForceTutorialStepNames.forceCompleteFirstPvPMatch.getName())
//                     && !request.getClientBuildInAppInfo().getRecordOnlyMode()) {
//                 newCreateChapterWinChestData = chestService.tryCreateChapterWinChest(userData.getUuid(), chapterTableValue.getId(), request.getGameVersion());
//             }
//
//             //刷新玩家历史数据
//             huntingMatchService.refreshPlayerHistoryData(userData.getUuid(), chapterTableValue, isPlayerWin, playerFireDetails, request.getGameVersion());
//
//             //保存玩家比赛得分
//             huntingMatchService.savePlayerChapterLatestMatchScoreCollection(userData.getUuid(), chapterTableValue.getId(), localPlayerFinalScore, request.getGameVersion());
//
//             if (haveNewChapterUnlocked) {
//
//                 //创建章节礼包
//                 packageDataService.createChapterBonusPackageData(userData, newUnlockChapterId, request.getGameVersion());
//             }
//
//
//             BattleCompleteDto battleCompleteDto = new BattleCompleteDto();
//             battleCompleteDto.setUid(request.getUserUid());
//             battleCompleteDto.setVersion(request.getGameVersion());
//             battleCompleteDto.setPlayerFinalScore(localPlayerFinalScore);
//             battleCompleteDto.setAiFinalScore(aiFinalScore);
//             battleCompleteDto.setRoundCount(chapterTableValue.getRound());
//
//             ThreadLocalForFight.setBattleCompleteDto(battleCompleteDto);
//             //保存玩家录像
//             playerControlRecordDataService.savePlayerUploadControlRecords(request,
//                     userData.getUuid(),
//                     historyData,
//                     request.getAllEncodedControlRecordsData(),
//                     request.getAverageFrameRate(), request.getGameVersion());
//
//             battleCompleteDto = ThreadLocalForFight.getBattleCompleteDto() == null ? battleCompleteDto : ThreadLocalForFight.getBattleCompleteDto();
//             Map<String, Object> fightInfo = HttpUtil.getFightInfo("http://192.168.30.18:9301/battle/complete", battleCompleteDto);
//
//
// //            BattleCompleteDto finalBattleCompleteDto = battleCompleteDto;
// //            RedisDBOperation.threadPool.execute(() -> {
// //                int code = -1;
// //                while (code != -1){
// //                    Map<String, Object> fightInfo = HttpUtil.getFightInfo("localhost:8080", finalBattleCompleteDto);
// //                    code = (int)fightInfo.get("code");
// //                }
// //            });
//
//             //需要发送给客户端的数据
//             sendToClientUserData.setChapterWinTrophyCountMap(userData.getChapterWinTrophyCountMap());
//             sendToClientUserData.setTrophy(userData.getTrophy());
//             sendToClientUserData.setPendingUnlockAnimationChapterId(userData.getPendingUnlockAnimationChapterId());
//             sendToClientUserData.setUnlockedChapterIds(userData.getUnlockedChapterIds());
//             sendToClientUserData.setAchievements(userData.getAchievements());
//             sendToClientUserData.setChapterWinChestsData(userData.getChapterWinChestsData());
//             sendToClientUserData.setCoin(userData.getCoin());
//             sendToClientUserData.setChapterBonusPackagesData(userData.getChapterBonusPackagesData());
//             sendToClientUserData.setHistory(userData.getHistory());
// //            History history = new History();
// //            BeanUtils.copyProperties(userData.getHistory(),history);
// //            sendToClientUserData.setHistory(history);
//
//
//             obsoleteUserDataService.userDataSettlement(userData, sendToClientUserData, true, request.getGameVersion());
//             Map<String, Object> map = CommonUtils.responsePrepare(null);
//
//             map.put("userData", sendToClientUserData);
//             map.put("playerTrophyFrom", playerTrophyFrom);
//             map.put("playerTrophyTo", playerTrophyTo);
//
//             if (newCreateChapterWinChestData != null) {
//                 map.put("newCreateChapterWinChestData", newCreateChapterWinChestData);
//             }
//             long needTime = System.currentTimeMillis() - startTime;
//             GameEnvironment.timeMessage.get("confirmHuntingMatchComplete").add(needTime);
//             log.info("[cmd] confirmHuntingMatchComplete finish need time" + (needTime));
//             return map;
//         } catch (Exception e) {
//
//             CommonUtils.responseException(request, e, request.getUserUid());
//         } finally {
//             ThreadLocalUtil.remove();
//         }
//         return null;
//     }


//     //    @PostMapping("ai-aiControlRecordDataQuery")
//     @ApiOperation("根据条件,找到合适的AI操作录制文件并返回")
//     public Map<String, Object> aiControlRecordDataQuery(@RequestBody AIControlRecordDataQueryDTO request) {
//
//         GameEnvironment.timeMessage.computeIfAbsent("aiControlRecordDataQuery", k -> new ArrayList<>());
//
//         try {
//             ThreadLocalUtil.set(request.getServerTimeOffset());
//             long startTime = System.currentTimeMillis();
//             log.info("[cmd] aiControlRecordDataQuery" + System.currentTimeMillis());
//             log.info(JSONObject.toJSONString(request));
//             CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
//
//             //载入当前玩家信息
//             String userUid = request.getUserUid();
//             obsoleteUserDataService.checkUserDataExist(userUid);
//             UserData userData = GameEnvironment.userDataMap.get(userUid);
//             UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
//
//             PlayerControlRecordData findControlRecordData = null;
//             String findControlRecordEncodeData = null;
//             //对手是否掉线，只有整局匹配才有可能出现
//             //当匹配到某一回合时，完整匹配的录像数据没有后续操作，则认为对手掉线
//             boolean isOpponentDisconnect = false;
//             //可能方法在重试的时候，同时到达多次，这样会造成同一个round取出不同的ai录像。
//             //对HuntingMatchNowData的操作放在事务中。
//             //>=1.0.10版本，后续该处理可以去掉，因为所有方法都处理了重发逻辑
//
//             //载入当前比赛信息
//             HuntingMatchNowData huntingMatchNowData = null;
//             String path = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//             huntingMatchNowData = huntingMatchService.getHuntingMatchNowData(path, request.getHuntingMatchNowUid());
//             if (huntingMatchNowData == null) {
//                 throw new BusinessException("玩家" + request.getUserUid() + "请求AI录制文件,但是不存在HuntingMatchNowId:" + request.getHuntingMatchNowUid());
//             }
//
//
//             HuntingMatchAIRecordChooseMode aiRecordChooseMode = huntingMatchNowData.getAiRecordChooseMode();
//
//
//             //回合匹配
//             if (aiRecordChooseMode == HuntingMatchAIRecordChooseMode.WinProneRound ||
//                     aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LoseProneRound ||
//                     aiRecordChooseMode == HuntingMatchAIRecordChooseMode.drawProneRound
//             ) {
//
//                 log.info("使用AI回合录制选择模式." + aiRecordChooseMode);
//                 if (huntingMatchNowData.getMatchAiRoundRuleTableId() == null) {
//                     throw new BusinessException("AI回合录制选择模式下，huntingMatchNowData.matchAiRoundRuleTableId不能是null");
//                 }
//
// //                MatchAiRoundControlQuery matchAiControlQuery = new MatchAiRoundControlQuery(
// //                        huntingMatchNowData.getMatchAiRoundRuleTableId(),
// //                        huntingMatchNowData.getChapterId(),
// //                        huntingMatchNowData.getMatchId(),
// //                        request.getRound(),
// //                        request.getAnimalRouteUid(),
// //                        request.getPlayerAverageShowPrecision(),
// //                        request.getPlayerFinalScore(),
// //                        request.getUserUid(),
// //                        huntingMatchNowData.getOpponentPlayerWeaponInfo());
// //
// //                log.info("查询原始参数"+JSONObject.toJSONString(matchAiControlQuery));
// //                MatchAiRoundRecordFilterParameters filterParameters = aiService.generateMatchAiRoundControlRecordFilterParameters(matchAiControlQuery,request.getGameVersion(),null);
// //                log.info("查询生成过滤参数"+JSONObject.toJSONString(filterParameters));
// //                if (filterParameters == null){
// //                    throw new BusinessException("玩家"+request.getUserUid()+"无法生成AI匹配过滤参数");
// //                }
// //                if (filterParameters.getSafeFailedReason()!=null){
// //                    log.info("AI回合查询安全失败，reason:"+filterParameters.getSafeFailedReason());
// //                    return CommonUtils.responsePrepare(null);
// //                }
//
// //                RecordDataAndBase64 recordDataAndBase64 = aiService.loadAiControlRecordData(filterParameters, request.getGameVersion());
//
//                 RecordDataAndBase64 recordDataAndBase64 = null;
//                 BattleStartDto battleStartDto = new BattleStartDto();
//                 Map<String, Object> fightInfo = HttpUtil.getFightInfo("192.168.30.18:9301/battle/start", battleStartDto);
//                 if (fightInfo != null) {
//                     Object data = fightInfo.get("data");
//                     Object recordData = JSONObject.parseObject(data.toString()).get("recordDataBase64");
//                     recordDataAndBase64 = JSONUtil.toBean(recordData.toString(), RecordDataAndBase64.class);
//                     log.warn("查询出的ai录像原始数据，recordDataAndBase64为{}", JSONUtil.toJsonStr(recordDataAndBase64));
//                 } else {
//                     log.warn("查询出的ai录像原始数据，recordDataAndBase64为空");
//                 }
//
//
//                 if (recordDataAndBase64 != null) {
//                     findControlRecordData = recordDataAndBase64.getRecordData();
//                     findControlRecordEncodeData = recordDataAndBase64.getBase64();
//                 }
// //                if (findControlRecordData==null){
// //                    log.error("原始查询参数"+matchAiControlQuery+",生成过滤参数"+filterParameters+", 无法匹配到任何操作数据,match uid"
// //                            +huntingMatchNowData.getMatchUid());
// //
// //                }else{
// //                    log.info("查找到录像"+findControlRecordData+", final score:"+findControlRecordData.getFinalScore()+", show precision: "
// //                            +findControlRecordData.getAverageShowPrecision());
// //
// //                }
//             }
//             //整局匹配
//             else if (aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LowLevelWholeMatch ||
//                     aiRecordChooseMode == HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch ||
//                     aiRecordChooseMode == HuntingMatchAIRecordChooseMode.HighLevelWholeMatch
//             ) {
//                 PlayerUploadWholeMatchControlRecordData wholeMatchControlRecordsData = huntingMatchNowData.getWholeMatchControlRecordsData();
//
//                 if (wholeMatchControlRecordsData == null) {
//                     throw new BusinessException("比赛" + huntingMatchNowData.getMatchUid() + "是AI整局操作匹配，但是wholeMatchControlRecordsData为null");
//                 }
//
//                 int roundIndex = request.getRound() - 1;
//                 if (roundIndex < 0) {
//                     log.error("比赛" + huntingMatchNowData.getMatchUid() + "根据整局操作匹配，但是round" + request.getRound() + ",round index" + roundIndex + "超出录像范围");
//                 }
//
//                 //检查后续回合是否有操作
//                 isOpponentDisconnect = true;
//                 if (roundIndex >= wholeMatchControlRecordsData.getEncodedBytes_Base64().size()) {
//                     isOpponentDisconnect = false;
//                 } else {
//                     for (int i = roundIndex; i < wholeMatchControlRecordsData.getEncodedBytes_Base64().size(); i++) {
//                         if (playerControlRecordDataService.checkControlRecordIsValid(wholeMatchControlRecordsData.getEncodedBytes_Base64().get(roundIndex))) {
//                             isOpponentDisconnect = false;
//                             break;
//                         }
//                     }
//                 }
//
//                 if (isOpponentDisconnect) {
//                     log.info("ai已经没有后续操作，可以认为断线");
//                 }
//
//                 if (!isOpponentDisconnect && roundIndex < wholeMatchControlRecordsData.getEncodedBytes_Base64().size()) {
//                     findControlRecordEncodeData = wholeMatchControlRecordsData.getEncodedBytes_Base64().get(roundIndex);
//                     String temp = playerControlRecordDataService.decodeControlRecordData(findControlRecordEncodeData);
//                     findControlRecordData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
//                     log.info("完整匹配获取AI录像。match uid: {}, round {}, final score:{}", huntingMatchNowData.getMatchUid(), request.getRound(), findControlRecordData.getFinalScore());
//                 }
//             } else {
//                 throw new BusinessException("未处理的ai record choose mode:" + aiRecordChooseMode);
//             }
//
//             InHuntingMatchAiFetchedControlRecordInfo aiFetchedDataInfo = new InHuntingMatchAiFetchedControlRecordInfo();
//
//             if (findControlRecordData == null) {
//
//                 aiFetchedDataInfo.setUid("");
//                 aiFetchedDataInfo.setRecordGameVersion("");
//                 aiFetchedDataInfo.setFinalScore(0);
//                 aiFetchedDataInfo.setFetchedTime(TimeUtils.getUnixTimeMilliseconds());
//             } else {
//                 aiFetchedDataInfo.setUid(findControlRecordData.getRecordUid());
//                 aiFetchedDataInfo.setRecordGameVersion(findControlRecordData.getGameVersion());
//                 aiFetchedDataInfo.setFinalScore(findControlRecordData.getFinalScore());
//                 aiFetchedDataInfo.setFetchedTime(TimeUtils.getUnixTimeMilliseconds());
//             }
//
//             List<InHuntingMatchAiFetchedControlRecordInfo> aiFetchedControlRecordInfos = huntingMatchNowData.getAiFetchedControlRecordsInfos();
//             if (aiFetchedControlRecordInfos.size() >= request.getRound()) {
//                 log.info("查询ai但是aiFetchedControlRecordsInfos数组长度" + aiFetchedControlRecordInfos.size() + ">=round" + request.getRound() + ",可能是同一局客户端反复请求");
//                 aiFetchedControlRecordInfos.add(request.getRound() - 1, aiFetchedDataInfo);
//             } else {
//                 aiFetchedControlRecordInfos.add(aiFetchedDataInfo);
//             }
//             huntingMatchNowData.setIsOpponentDisconnect(isOpponentDisconnect);
//
//             //保存匹配到的ai操作数据
//             String matchPath = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//             huntingMatchService.reSaveHuntingMatchNowData(matchPath, request.getHuntingMatchNowUid(), userUid, huntingMatchNowData);
//
//             Map<String, Object> map = CommonUtils.responsePrepare(null);
//             obsoleteUserDataService.userDataSettlement(userData, sendToClientData, false, request.getGameVersion());
//             if (findControlRecordData != null) {
//                 map.put("recordDataBase64", findControlRecordEncodeData);
//             }
//             if (isOpponentDisconnect) {
//                 map.put("isOpponentDisconnect", isOpponentDisconnect);
//             }
//             long needTime = System.currentTimeMillis() - startTime;
//             GameEnvironment.timeMessage.get("aiControlRecordDataQuery").add(needTime);
//             log.info("[cmd] aiControlRecordDataQuery finish need time" + (System.currentTimeMillis() - startTime));
//             return map;
//
//         } catch (Exception e) {
//
//             CommonUtils.responseException(request, e, request.getUserUid());
//         } finally {
//             ThreadLocalUtil.remove();
//         }
//
//         return null;
//     }


    //    @PostMapping("/login")
    @ApiOperation("玩家登录")
    public Map<String, Object> login(@RequestBody LoginDTO loginDTO) {

        String loginUserUid = null;
        try {
            GameEnvironment.timeMessage.computeIfAbsent("login", k -> new ArrayList<>());
            ThreadLocalUtil.set(loginDTO.getServerTimeOffset());
            log.warn("客户端请求时间偏移单位：{}", loginDTO.getServerTimeOffset());
            log.warn("threadLocal中的时间偏移单位：{}", ThreadLocalUtil.localVar.get());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] login" + System.currentTimeMillis());

            obsoleteUserDataService.createLoginSessionData(loginDTO);

            CommonUtils.requestProcess(loginDTO, false, systemPropertiesConfig.getSupportRecordModeClient());


            //获取请求信息中的内容
            UserData newUserData = null;
            String privateKey = loginDTO.getPrivateKey();
            boolean isNewUser = false;

            UserData loginUserData = null;
            if (StringUtils.isEmpty(loginDTO.getUserUid())) {
                log.warn("开始创建新用户");
                //新用户
                isNewUser = true;
                newUserData = obsoleteUserDataService.createNewPlayer(loginDTO.getGameVersion());
                if (newUserData != null) {
                    log.warn("新用户创建成功，用户信息为：{}", JSONUtil.toJsonStr(newUserData));
                } else {
                    log.warn("创建的用户数据为空");
                }
                privateKey = newUserData.getServerOnly().getPrivateKey();
                loginUserData = newUserData;
                //加载到登录用户环境中
                GameEnvironment.userDataMap.put(newUserData.getUuid(), newUserData);
            } else {
                log.warn("从redis中加载用户");
                // 从redis中加载用户
                //判断该玩家是否在线,在线则抛出异常，不执行登录操作
                synchronized (GameEnvironment.userDataMap) {
                    if (GameEnvironment.userDataMap.containsKey(loginDTO.getUserUid())) {
                        throw new BusinessException("该玩家已经在线");
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
            log.info("用户" + loginUserUid + "登录,token：" + userToken + ", time" + TimeUtils.getUnixTimeSecond());

            //确认新版本
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
            //向战斗服发消息，创建用户战斗信息
            if (!loginUserData.getIsCreateBattleInfo()) {
                UserInfoDto createUserInfo = getCreateUserInfo(loginUserData, loginDTO.getGameVersion());
                Map<String, Object> fightInfo = HttpUtil.getFightInfo("localhost:8080/user/create", createUserInfo);
                if (fightInfo == null || (int) fightInfo.get("code") != 0) {
                    log.error("创建用户战斗信息失败");
                } else {
                    loginUserData.setIsCreateBattleInfo(true);
                }
            } else {
                HttpUtil.getFightInfo("192.168.30.18:9301/user/online", loginUserData.getUuid());
            }


            //返回内容
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
            log.warn("下发给服务器的时间：{}", map.get("serverTime"));
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
            //礼包弹出推荐价格
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
            log.error("登录出错：" + e);
            CommonUtils.responseException(loginDTO, e, loginDTO.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }


    //    @PostMapping("/keepAlive")
    @ApiOperation("玩家保活")
    public Map<String, Object> keepAlive(@RequestBody BaseDTO baseDTO) {
        GameEnvironment.onlineUser.put(baseDTO.getUserUid(), new Date());
        HttpUtil.getFightInfo("192.168.30.18:9301/user/online", baseDTO.getUserUid());
        return CommonUtils.responsePrepare(null);
    }


    public UserInfoDto getCreateUserInfo(UserData userData, String gameVersion) {
        UserInfoDto userInfoDto = new UserInfoDto();
        UserInfo userInfo = new UserInfo();


        //计算胜率，生成战斗信息
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

        //生成chapterInfo
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

        //生成recordInfo
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
