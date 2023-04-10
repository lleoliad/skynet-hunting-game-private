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
import org.skynet.components.hunting.battle.data.MatchInfoBO;
import org.skynet.components.hunting.battle.data.cvo.BattleCompleteInfoCVO;
import org.skynet.components.hunting.battle.data.cvo.BattleMatchingInfoCVO;
import org.skynet.components.hunting.battle.domain.MatchPlayerInfo;
import org.skynet.components.hunting.battle.enums.BattleMode;
import org.skynet.components.hunting.battle.query.CompleteQuery;
import org.skynet.components.hunting.battle.query.MatchingQuery;
import org.skynet.components.hunting.battle.service.BattleFeignService;
import org.skynet.components.hunting.rank.league.message.AddCoinMessage;
import org.skynet.components.hunting.rank.league.query.AddCoinsQuery;
import org.skynet.components.hunting.rank.league.query.PlayerLoginQuery;
import org.skynet.components.hunting.rank.league.service.RankLeagueFeignService;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.domain.ChapterWinChestData;
import org.skynet.components.hunting.user.domain.History;
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

@Api(tags = "比赛")
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

    @Resource
    private RankLeagueFeignService rankLeagueFeignService;

//    @PostMapping("/huntingMatch-confirmHuntingMatchComplete")
//    @ApiOperation("确认章节比赛结束")
//    public Map<String,Object>confirmHuntingMatchComplete(@RequestBody ConfirmHuntingMatchCompleteDTO request){
//        try{
//            GameEnvironment.timeMessage.computeIfAbsent("confirmHuntingMatchComplete", k -> new ArrayList<>());
//            ThreadLocalUtil.set(request.getServerTimeOffset());
//            long startTime = System.currentTimeMillis();
//            log.info("[cmd] confirmHuntingMatchComplete"+System.currentTimeMillis());
//            log.info(JSONObject.toJSONString(request));
//            CommonUtils.requestProcess(request,null,systemPropertiesConfig.getSupportRecordModeClient());
////            userDataService.ensureUserDataIdempotence(request.getUserUid(),request.getUserDataUpdateCount(),request.getGameVersion());
//            //验证确实开始了这个比赛,防止刷数据
//            String matchPath  = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//
//            HuntingMatchNowData huntingMatchNowData = huntingMatchService.getHuntingMatchNowData(matchPath, request.getMatchUUID());
//
//            if (huntingMatchNowData==null){
//                throw new BusinessException("比赛"+request.getMatchUUID()+"不存在");
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
//            //计算玩家和ai的分数,从录制文件计算
//            int localPlayerFinalScore = 0;
//
//            for (PlayerControlRecordData controlRecordData : allPlayerControlRecordsData) {
//
//                localPlayerFinalScore += controlRecordData.getFinalScore();
//            }
//            int aiFinalScore = 0;
//
//            //如果ai先手,那么ai会预加载多一局的数据,所以这里计算分数的时候,取的录制文件不要多于玩家局数
//            for (int i=0;i<huntingMatchNowData.getAiFetchedControlRecordsInfos().size()&&i<allPlayerControlRecordsData.size();i++){
//
//                InHuntingMatchAiFetchedControlRecordInfo aiFetchedControlRecordInfo = huntingMatchNowData.getAiFetchedControlRecordsInfos().get(i);
//                if(Objects.nonNull(aiFetchedControlRecordInfo)){
//                    aiFinalScore += aiFetchedControlRecordInfo.getFinalScore();
//                }
//            }
//            log.info("比赛"+request.getMatchUUID()+"分数，玩家："+localPlayerFinalScore+"，ai:"+aiFinalScore);
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
//                    String errorInfo = "比赛"+request.getMatchUUID()+",玩家上报分数和录像计算分数结果不一致,录像计算中,玩家:"+localPlayerFinalScore+
//                            ",ai:"+aiFinalScore+",玩家上报中,玩家"+request.getPlayerFinalScore()+",ai:"+request.getAiFinalScore();
//
//
//                    //不是正式服务器抛出错误，尽早检查错误
//                    if (!systemPropertiesConfig.getProduction()){
//                        throw new BusinessException(errorInfo);
//                    }else {
//                        log.error(errorInfo);
//                        //看需不需要另外作为对象存储
//                    }
//                }else {
//                    //分数检查没有问题,就以玩家上报分数为准
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
//            //存档
//            huntingMatchService.saveHuntingHistoryMatch(matchHistoryRef, request.getMatchUUID(),historyData);
//
//            log.info("记录比赛历史数据"+request.getMatchUUID());
//
//            //删除正在比赛的数据
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
//            //处理userData
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
//            //玩家金币
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
//                        throw new BusinessException("录制模式，但是比赛数据没有玩家选择的章节信息");
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
//            //玩家奖杯
//            if (recordModeData == null){
//                huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(),chapterTableValue,isPlayerWin,request.getGameVersion() );
//            }else {
//
//                if (huntingMatchNowData.getRecordModeSelectChapterId() == null){
//                    throw new BusinessException("录制模式，但是比赛数据没有玩家选择的章节信息");
//                }
//
//                ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getRecordModeSelectChapterId()));
//                huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(),playerSelectChapterTableValue,isPlayerWin,request.getGameVersion() );
//            }
//
//            //看是否有新章节解锁
//            CheckNewUnlockChapterBO checkNewUnlockChapterBO = huntingMatchService.checkNewUnlockChapter(userData,request.getGameVersion());
//            haveNewChapterUnlocked = checkNewUnlockChapterBO.getHaveNewChapterUnlocked();
//            newUnlockChapterId = checkNewUnlockChapterBO.getNewUnlockChapterId();
//
//            playerTrophyTo = userData.getTrophy();
//
//            //记录玩家完成章节比赛次数
//            huntingMatchService.recordChapterComplete(userData.getUuid(),
//                    chapterTableValue,
//                    huntingMatchNowData.getMatchId(),
//                    playerFireDetails,
//                    isPlayerWin,
//                    request.getGameVersion(),
//                    allPlayerControlRecordsData);
//
//            //记录动物击杀成就
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
//            //如果胜利,且已经完成了第一次PVP匹配教学,获得一个章节胜利宝箱
//            if (isPlayerWin
//                    && userDataService.isForceTutorialStepComplete(userData.getUuid(),ForceTutorialStepNames.forceCompleteFirstPvPMatch.getName())
//                    &&!request.getClientBuildInAppInfo().getRecordOnlyMode()){
//                newCreateChapterWinChestData = chestService.tryCreateChapterWinChest(userData.getUuid(),chapterTableValue.getId(),request.getGameVersion());
//            }
//
//            //刷新玩家历史数据
//            huntingMatchService.refreshPlayerHistoryData(userData.getUuid(),chapterTableValue,isPlayerWin,playerFireDetails,request.getGameVersion());
//
//            //保存玩家比赛得分
//            huntingMatchService.savePlayerChapterLatestMatchScoreCollection(userData.getUuid(),chapterTableValue.getId(),localPlayerFinalScore,request.getGameVersion());
//
//            if (haveNewChapterUnlocked){
//
//                //创建章节礼包
//                packageDataService.createChapterBonusPackageData(userData,newUnlockChapterId,request.getGameVersion());
//            }
//
//            //保存玩家录像
//            playerControlRecordDataService.savePlayerUploadControlRecords(request,
//                    userData.getUuid(),
//                    historyData,
//                    request.getAllEncodedControlRecordsData(),
//                    request.getAverageFrameRate(), request.getGameVersion());
//
//
//
//            //需要发送给客户端的数据
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
//    @ApiOperation("确认进入章节比赛")
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
//                //录制模式，更改玩家选择的章节
//                playerSelectChapterId = recordModeMatchTableValue.getChapterId();
//            }
//
//            Integer realChapterId = request.getChapterId();
//            Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(request.getGameVersion());
//            ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(realChapterId));
//
//            if (userData.getCoin() < chapterTableValue.getEntryFee()&&recordModeData == null){
//
//                throw new BusinessException("用户"+playerUUID+"金币不足,无法开始比赛");
//            }
//
//            //更新章节进入次数
//            Map<Integer, Integer> chapterEnteredCountMap = userData.getChapterEnteredCountMap();
//            int chapterEnteredCount = chapterEnteredCountMap.getOrDefault(request.getChapterId(),0);
//            chapterEnteredCount += 1;
//            chapterEnteredCountMap.put(realChapterId,chapterEnteredCount);
//            userDataSendToClient.setChapterEnteredCountMap(userData.getChapterEnteredCountMap());
//
//            //确定进入哪场比赛和动物
//            //因为上面章节进入次数已经+1了，这里-1
//            matchId  = huntingMatchService.determineEnterWhichMatch(playerUUID, realChapterId, chapterEnteredCount - 1,request.getGameVersion());
//
//            //玩家的武器
//            localPlayerWeaponInfo = huntingMatchService.generateLocalPlayerWeaponInfo(userData,request.getGameVersion());
//
//            //扣除入场费用
//            int entryFee = chapterTableValue.getEntryFee();
//            if (recordModeData != null){
//                ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(playerSelectChapterId));
//
//                if (Arrays.asList(GameConfig.recordModeFreeFeeChapterIdsArray).contains(playerSelectChapterId)){
//                    entryFee = 0;
//                }else {
//                    entryFee = playerSelectChapterTableValue.getEntryFee();
//                }
//                log.info("录制模式,入场费改为"+entryFee);
//            }
//            userData.setCoin(userData.getCoin() - entryFee);
//            userData.setCoin(Math.max(userData.getCoin(),0));
//            userDataSendToClient.setCoin(userData.getCoin());
//
//            //本次AI录像查找模式
//            aiRecordChooseMode = huntingMatchService.determineAIRecordChooseMode(playerUUID,String.valueOf(request.getChapterId()),request.getGameVersion());
//
//            if (aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.HighLevelWholeMatch)
//                    ||aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch)
//                    ||aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.LowLevelWholeMatch))
//            {
//                log.info("ai操作根据完整比赛获取,mode:"+aiRecordChooseMode);
//                //玩家随机先手
//                isLocalPlayerFirst = NumberUtils.randomFloat(0.0,1.0)<=0.5;
//                wholeMatchControlRecordsData = aiService.queryWholeMatchControlRecords(playerUUID,aiRecordChooseMode, playerSelectChapterId,matchId,request.getGameVersion());
//                if (wholeMatchControlRecordsData !=null){
//                    opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByWholeMatchRecordsData(wholeMatchControlRecordsData);
//                }
//            }
//
//            //如果整局比赛匹配模式没有匹配到任何信息，则转为回合匹配模式
//            if (opponentPlayerWeaponInfo==null&&
//                    (aiRecordChooseMode==HuntingMatchAIRecordChooseMode.HighLevelWholeMatch||
//                            aiRecordChooseMode==HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch||
//                            aiRecordChooseMode==HuntingMatchAIRecordChooseMode.LowLevelWholeMatch)){
//
//                log.info("无法找到完整比赛录像，切换为回合匹配");
//                /**
//                 * 如果是普通完整匹配和高水平完整匹配，则改为倾向失败的回合匹配
//                 * 如果是保底完整匹配，则改为倾向胜利的回合匹配
//                 */
//                HuntingMatchAIRecordChooseMode aiRecordChooseModeBefore = aiRecordChooseMode;
//                aiRecordChooseMode = aiService.convertAiRecordChooseModeFromWholeMatchToRound(aiRecordChooseMode);
//                log.info(aiRecordChooseModeBefore+"无法找到完整比赛录像，切换为回合匹配 "+aiRecordChooseMode);
//            }
//
//            if (aiRecordChooseMode==HuntingMatchAIRecordChooseMode.WinProneRound||
//                    aiRecordChooseMode==HuntingMatchAIRecordChooseMode.LoseProneRound||
//                    aiRecordChooseMode==HuntingMatchAIRecordChooseMode.drawProneRound
//            ){
//
//                log.info("ai操作根据回合匹配。mode:"+aiRecordChooseMode);
//                //确定使用哪个MatchAiRuleTable数据
//                matchAiRoundRuleTableValue = huntingMatchService.determineMatchAiRoundRuleTableValue(request.getChapterId(),chapterEnteredCount,aiRecordChooseMode,request.getGameVersion());
//
//                if (matchAiRoundRuleTableValue == null){
//                    throw new BusinessException("无法找到合适的matchAiRoundRuleTableValue");
//                }
//
//                log.info("匹配到MatchAIRoundRuleTable条目"+JSONObject.toJSONString(matchAiRoundRuleTableValue));
//                opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByMatchAiRoundRule(matchAiRoundRuleTableValue,request.getGameVersion());
//                isLocalPlayerFirst = matchAiRoundRuleTableValue.getIsPlayerFirst();
//            }
//
//            //使用子弹
//            if (recordModeData==null){
//                huntingMatchService.consumeBullet(userData, localPlayerWeaponInfo.getBulletId(),request.getGameVersion());
//                userDataSendToClient.setBulletCountMap(userData.getBulletCountMap());
//                userDataSendToClient.setEquippedBulletId(userData.getEquippedBulletId());
//            }
//
//            //生成匹配ai信息
//            //正常模式
//            if (recordModeData==null){
//
//                Integer count = wholeMatchControlRecordsData==null?null:wholeMatchControlRecordsData.getSafePlayerTrophyCount();
//                opponentPlayerInfo = huntingMatchService.generateOpponentPlayerInfo(userData,chapterTableValue.getId(), count, request.getGameVersion());
//            }
//            //录制模式
//            else {
//                log.info("生成对手信息，来自章节"+playerSelectChapterId);
//                opponentPlayerInfo = huntingMatchService.generateOpponentPlayerInfo(userData, request.getChapterId(), null,request.getGameVersion() );
//            }
//
//            userDataSendToClient.setHistory(userData.getHistory());
//            userDataService.userDataSettlement(userData, userDataSendToClient,true,request.getGameVersion());
//            Map<String, Object> map = CommonUtils.responsePrepare(null);
//            String huntingMatchUUID = NanoIdUtils.randomNanoId(30);
//
//            //记录玩家正在进行的比赛,结束比赛的时候,需要验证
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
//                log.info("录制模式，记录玩家选择的章节："+request.getChapterId());
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
//            //记录这个比赛正在进行
//            String matchPath  = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//            huntingMatchService.saveHuntingMatchNowData(matchPath,huntingMatchUUID,playerUUID,huntingMatchNowData);
//
//            log.info("开始比赛,"+huntingMatchNowData);
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
    @ApiOperation("确认进入章节比赛")
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

            History history = userData.getHistory();
            history.setTotalMatchCount(history.getTotalMatchCount() + 1);
            history.setMatchWonPercentage(history.getWonMatchCount() / history.getTotalMatchCount().floatValue());

            //确定进入哪场比赛和动物
            //因为上面章节进入次数已经+1了，这里-1
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
            Result<MatchInfoBO> matchingResult = battleFeignService.matching(MatchingQuery.builder()
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
                throw new BusinessException("无法从战斗服务器获取数据");
            }

            // if (fightInfo == null) {
            //     throw new BusinessException("无法从战斗服务器获取数据");
            // }

            //玩家的武器
            // JSONObject data = JSONObject.parseObject(fightInfo.get("data").toString());
            MatchInfoBO matchInfoBO = matchingResult.getData();
//            MatchInfo matchInfo = JSONUtil.toBean(data.get("matchInfo").toString(), MatchInfo.class);
            MatchPlayerInfo attacker = matchInfoBO.getAttacker();
            localPlayerWeaponInfo = new PlayerWeaponInfo(attacker.getGunId(), attacker.getGunLevel(), attacker.getBulletId());

            //todo 需要战斗服传matchId
            matchId = matchInfoBO.getMatchId(); //(Integer) data.get("matchId");

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

//            //本次AI录像查找模式
//            aiRecordChooseMode = huntingMatchService.determineAIRecordChooseMode(playerUUID,String.valueOf(request.getChapterId()),request.getGameVersion());
//
//            if (aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.HighLevelWholeMatch)
//                    ||aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch)
//                    ||aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.LowLevelWholeMatch))
//            {
//                log.info("ai操作根据完整比赛获取,mode:"+aiRecordChooseMode);
//                //玩家随机先手
//                isLocalPlayerFirst = NumberUtils.randomFloat(0.0,1.0)<=0.5;
//                wholeMatchControlRecordsData = aiService.queryWholeMatchControlRecords(playerUUID,aiRecordChooseMode, playerSelectChapterId,matchId,request.getGameVersion());
//                if (wholeMatchControlRecordsData !=null){
//                    opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByWholeMatchRecordsData(wholeMatchControlRecordsData);
//                }
//            }
//
//            //如果整局比赛匹配模式没有匹配到任何信息，则转为回合匹配模式
//            if (opponentPlayerWeaponInfo==null&&
//                    (aiRecordChooseMode==HuntingMatchAIRecordChooseMode.HighLevelWholeMatch||
//                            aiRecordChooseMode==HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch||
//                            aiRecordChooseMode==HuntingMatchAIRecordChooseMode.LowLevelWholeMatch)){
//
//                log.info("无法找到完整比赛录像，切换为回合匹配");
//                /**
//                 * 如果是普通完整匹配和高水平完整匹配，则改为倾向失败的回合匹配
//                 * 如果是保底完整匹配，则改为倾向胜利的回合匹配
//                 */
//                HuntingMatchAIRecordChooseMode aiRecordChooseModeBefore = aiRecordChooseMode;
//                aiRecordChooseMode = aiService.convertAiRecordChooseModeFromWholeMatchToRound(aiRecordChooseMode);
//                log.info(aiRecordChooseModeBefore+"无法找到完整比赛录像，切换为回合匹配 "+aiRecordChooseMode);
//            }
//
//            if (aiRecordChooseMode==HuntingMatchAIRecordChooseMode.WinProneRound||
//                    aiRecordChooseMode==HuntingMatchAIRecordChooseMode.LoseProneRound||
//                    aiRecordChooseMode==HuntingMatchAIRecordChooseMode.drawProneRound
//            ){
//
//                log.info("ai操作根据回合匹配。mode:"+aiRecordChooseMode);
//                //确定使用哪个MatchAiRuleTable数据
//                matchAiRoundRuleTableValue = huntingMatchService.determineMatchAiRoundRuleTableValue(request.getChapterId(),chapterEnteredCount,aiRecordChooseMode,request.getGameVersion());
//
//                if (matchAiRoundRuleTableValue == null){
//                    throw new BusinessException("无法找到合适的matchAiRoundRuleTableValue");
//                }
//
//                log.info("匹配到MatchAIRoundRuleTable条目"+JSONObject.toJSONString(matchAiRoundRuleTableValue));
////                opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByMatchAiRoundRule(matchAiRoundRuleTableValue,request.getGameVersion());
//                isLocalPlayerFirst = matchAiRoundRuleTableValue.getIsPlayerFirst();
//            }

            //todo 需要战斗服传的,是否是玩家先手
            isLocalPlayerFirst = BooleanUtil.isTrue(matchInfoBO.getIsLocalPlayerFirst());// data.get("isLocalPlayerFirst") == null || (boolean) data.get("isLocalPlayerFirst");

            // Defender defender = JSONUtil.toBean(data.get("defender").toString(), Defender.class);
            MatchPlayerInfo defender = matchInfoBO.getDefender();
            opponentPlayerWeaponInfo = new PlayerWeaponInfo(defender.getGunId(), defender.getGunLevel(), defender.getBulletId());
            //使用子弹
            if (recordModeData == null) {
                huntingMatchService.consumeBullet(userData, localPlayerWeaponInfo.getBulletId(), request.getGameVersion());
                userDataSendToClient.setBulletCountMap(userData.getBulletCountMap());
                userDataSendToClient.setEquippedBulletId(userData.getEquippedBulletId());
            }

            // Integer opponentTrophyCount;
            // opponentTrophyCount = (Integer) data.get("safePlayerTrophyCount");
            //生成匹配ai信息
            //正常模式
            if (recordModeData == null) {
                //todo 需要战斗服传的,安全杯数范围
                //如果战斗服传的杯数不是null，直接使用，如果时null，则自己计算
                opponentPlayerInfo = huntingMatchService.generateOpponentPlayerInfo(userData, chapterTableValue.getId(), defender.getTrophyCount(), request.getGameVersion());
            }
            //录制模式
            else {
                log.info("生成对手信息，来自章节" + playerSelectChapterId);
                opponentPlayerInfo = huntingMatchService.generateOpponentPlayerInfo(userData, request.getChapterId(), defender.getTrophyCount(), request.getGameVersion());
            }

            userDataSendToClient.setHistory(userData.getHistory());
            obsoleteUserDataService.userDataSettlement(userData, userDataSendToClient, true, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            String huntingMatchUUID = NanoIdUtils.randomNanoId(30);


//            //记录玩家正在进行的比赛,结束比赛的时候,需要验证
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

            //记录这个比赛正在进行
            String matchPath = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
            huntingMatchService.saveHuntingMatchNowData(matchPath, huntingMatchUUID, playerUUID, huntingMatchNowData);

//            log.info("开始比赛,"+huntingMatchNowData);

            //todo 当前比赛的uid
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

            // TODO 处理缺省值的错误
            String sv = JSON.toJSONString(map, SerializerFeature.WriteMapNullValue);
            String siv = JSON.toJSONString(map);
            if (sv.length() != siv.length()) {
                map = JSON.parseObject(siv);
                log.warn("战斗匹配缺省值异常:{}", siv);
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
    @ApiOperation("确认章节比赛结束")
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
            //验证确实开始了这个比赛,防止刷数据
            String matchPath = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//
            HuntingMatchNowData huntingMatchNowData = huntingMatchService.getHuntingMatchNowData(matchPath, request.getMatchUUID());
//
//            if (huntingMatchNowData==null){
//                throw new BusinessException("比赛"+request.getMatchUUID()+"不存在");
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
//                    throw new BusinessException("从战斗服获取到的数据为空");
//                }
//                     if (fightInfo != null) {
//                         data = JSONObject.parseObject(fightInfo.get("data").toString());
//                     }
                    if (completeResult.getSuccess()) {
                        data = completeResult.getData();
                    }
//                if (data  == null){

//                    throw new BusinessException("从战斗服获取到的数据为空");
//                }
                }
            } else {
                log.info("非正式环境，不发送战报");
                battleCompleteDto.setRoundReportData(new ArrayList<>());
            }


//            JSONObject data = null;
//            http://192.168.2.199:9301
//            if(!(request.getDirectlyWin() != null || request.getDirectlyLose()!=null)){
//                Map<String, Object> fightInfo = HttpUtil.getFightInfo("http://47.88.90.222:9301/battle/complete", battleCompleteDto);
////                if (fightInfo  == null){
////                    throw new BusinessException("从战斗服获取到的数据为空");
////                }
//                if (fightInfo != null){
//                    data = JSONObject.parseObject(fightInfo.get("data").toString());
//                }
////                if (data  == null){
////                    throw new BusinessException("从战斗服获取到的数据为空");
////                }
//            }

            //计算玩家和ai的分数,从录制文件计算
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


//            //如果ai先手,那么ai会预加载多一局的数据,所以这里计算分数的时候,取的录制文件不要多于玩家局数
//            for (int i=0;i<huntingMatchNowData.getAiFetchedControlRecordsInfos().size()&&i<allPlayerControlRecordsData.size();i++){
//
//                InHuntingMatchAiFetchedControlRecordInfo aiFetchedControlRecordInfo = huntingMatchNowData.getAiFetchedControlRecordsInfos().get(i);
//                if(Objects.nonNull(aiFetchedControlRecordInfo)){
//                    aiFinalScore += aiFetchedControlRecordInfo.getFinalScore();
//                }
//            }
            log.info("比赛" + request.getMatchUUID() + "分数，玩家：" + localPlayerFinalScore + "，ai:" + aiFinalScore);

            boolean playerDirectChangeResult = request.getPlatform().equals(PlatformName.UnityEditor.getPlatform())
                    && !SystemPropertiesConfig.PRODUCTION
                    && (request.getDirectlyWin() != null || request.getDirectlyLose() != null);

            if (!playerDirectChangeResult) {

                if (Math.abs(localPlayerFinalScore - request.getPlayerFinalScore()) > 1 || Math.abs(aiFinalScore - request.getAiFinalScore()) > 1) {
                    String errorInfo = "比赛" + request.getMatchUUID() + ",玩家上报分数和录像计算分数结果不一致,录像计算中,玩家:" + localPlayerFinalScore + ",ai:" + aiFinalScore + ",玩家上报中,玩家" + request.getPlayerFinalScore() + ",ai:" + request.getAiFinalScore();
                    //不是正式服务器抛出错误，尽早检查错误
                    if (!systemPropertiesConfig.getProduction()) {
                        throw new BusinessException(errorInfo);
                    } else {
                        log.error(errorInfo);
                        //看需不需要另外作为对象存储
                    }
                } else {
                    //分数检查没有问题,就以玩家上报分数为准
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

            //存档
//            huntingMatchService.saveHuntingHistoryMatch(matchHistoryRef, request.getMatchUUID(),historyData);

//            log.info("记录比赛历史数据"+request.getMatchUUID());

            UserDataSendToClient sendToClientUserData = GameEnvironment.prepareSendToClientUserData();

            int playerTrophyFrom = 0;
            int playerTrophyTo = 0;
            boolean haveNewChapterUnlocked = false;
            int newUnlockChapterId = -1;
            ChapterWinChestData newCreateChapterWinChestData = null;

            UserData userData = null;

            //处理userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            userData = GameEnvironment.userDataMap.get(request.getUserUid());

            PlayerRecordModeData recordModeData = userData.getServerOnly().getRecordModeData();

            boolean isPlayerWin = localPlayerFinalScore >= aiFinalScore;
            if (playerDirectChangeResult) {
                if (BooleanUtil.isTrue(request.getDirectlyWin())) {
                    isPlayerWin = true;
                } else if (BooleanUtil.isTrue(request.getDirectlyLose()))
                    isPlayerWin = false;
            }

            //todo 需要战斗服传chapterId
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

            //玩家金币
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
//                    //录制模式下的章节id
//                    if (huntingMatchNowData.getRecordModeSelectChapterId() == null){
//                        throw new BusinessException("录制模式，但是比赛数据没有玩家选择的章节信息");
//                    }
//
//                    ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getRecordModeSelectChapterId()));
//                    userData.setCoin(userData.getCoin()+playerSelectChapterTableValue.getReward());
//                    userData.getHistory().setTotalEarnedCoinByMatch(userData.getHistory().getTotalEarnedCoinByMatch()+playerSelectChapterTableValue.getReward());
//                    userData.getHistory().setTotalEarnedCoin(userData.getHistory().getTotalEarnedCoin()+playerSelectChapterTableValue.getReward());
//                }
            }

            //玩家奖杯
            huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(), chapterTableValue, isPlayerWin, request.getGameVersion());

//            if (recordModeData == null){
//                huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(),chapterTableValue,isPlayerWin,request.getGameVersion() );
//            }else {
//
//                if (huntingMatchNowData.getRecordModeSelectChapterId() == null){
//                    throw new BusinessException("录制模式，但是比赛数据没有玩家选择的章节信息");
//                }
//
//                //todo 当前比赛的章节id
//                ChapterTableValue playerSelectChapterTableValue = chapterTable.get(String.valueOf(huntingMatchNowData.getRecordModeSelectChapterId()));
//                huntingMatchService.winOrLoseTrophyInHuntingMatch(userData.getUuid(),playerSelectChapterTableValue,isPlayerWin,request.getGameVersion() );
//            }

            //看是否有新章节解锁
            CheckNewUnlockChapterBO checkNewUnlockChapterBO = huntingMatchService.checkNewUnlockChapter(userData, request.getGameVersion());
            haveNewChapterUnlocked = checkNewUnlockChapterBO.getHaveNewChapterUnlocked();
            newUnlockChapterId = checkNewUnlockChapterBO.getNewUnlockChapterId();

            playerTrophyTo = userData.getTrophy();

            //记录玩家完成章节比赛次数
            //todo 当前回合的matchId
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

            //记录动物击杀成就
            achievementService.updateAnimalKillAchievementData(userData.getUuid(),
                    chapterTableValue.getId(),
//                    huntingMatchNowData.getMatchId(),
                    playerFireDetails,
//                    isPlayerWin,
                    request.getGameVersion(),
                    allPlayerControlRecordsData);


            //如果胜利,且已经完成了第一次PVP匹配教学,获得一个章节胜利宝箱
            if (isPlayerWin
                    && obsoleteUserDataService.isForceTutorialStepComplete(userData.getUuid(), ForceTutorialStepNames.forceCompleteFirstPvPMatch.getName())
                    && !request.getClientBuildInAppInfo().getRecordOnlyMode()) {
                newCreateChapterWinChestData = chestService.tryCreateChapterWinChest(userData.getUuid(), chapterTableValue.getId(), request.getGameVersion());
            }

            //刷新玩家历史数据
            huntingMatchService.refreshPlayerHistoryData(userData.getUuid(), chapterTableValue, isPlayerWin, playerFireDetails, request.getGameVersion());

            //保存玩家比赛得分
            // huntingMatchService.savePlayerChapterLatestMatchScoreCollection(userData.getUuid(), chapterTableValue.getId(), localPlayerFinalScore, request.getGameVersion()); // 重构取消

            if (haveNewChapterUnlocked) {
                //创建章节礼包
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
//            //保存玩家录像
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

            //需要发送给客户端的数据
            sendToClientUserData.setChapterWinTrophyCountMap(userData.getChapterWinTrophyCountMap());
            sendToClientUserData.setTrophy(userData.getTrophy());
            sendToClientUserData.setPendingUnlockAnimationChapterId(userData.getPendingUnlockAnimationChapterId());
            sendToClientUserData.setUnlockedChapterIds(userData.getUnlockedChapterIds());
            sendToClientUserData.setAchievements(userData.getAchievements());
            sendToClientUserData.setChapterWinChestsData(userData.getChapterWinChestsData());
            sendToClientUserData.setCoin(userData.getCoin());
            sendToClientUserData.setChapterBonusPackagesData(userData.getChapterBonusPackagesData());
            sendToClientUserData.setHistory(userData.getHistory());
            sendToClientUserData.setChapterCompletedCountMap(userData.getChapterCompletedCountMap());
//            History history = new History();
//            BeanUtils.copyProperties(userData.getHistory(),history);
//            sendToClientUserData.setHistory(history);


            obsoleteUserDataService.userDataSettlement(userData, sendToClientUserData, true, request.getGameVersion());

            //删除正在比赛的数据
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

                // AddCoinMessage addCoinMessage = AddCoinMessage.builder().userId(request.getUserUid()).coin(addCount).build();
                // rocketMQTemplate.asyncSend(rankLeagueAddCoinTopic, addCoinMessage, new SendCallback() {
                //     @Override
                //     public void onSuccess(SendResult sendResult) {
                //         // log.info("async onSucess SendResult={}", sendResult);
                //         // log.info("发送增加段位赛金币成功：{}", addCoinMessage);
                //     }
                //
                //     @Override
                //     public void onException(Throwable throwable) {
                //         log.info("发送增加段位赛金币失败：{}", addCoinMessage);
                //     }
                // });

                Result<?> rankLeagueLoginResult = rankLeagueFeignService.playerAddCoins(AddCoinsQuery.builder()
                        .version(request.getGameVersion())
                        .userId(userData.getUuid())
                        .coin(addCount)
                        .build());

                if (rankLeagueLoginResult.getSuccess()) {
                    sendToClientUserData.setPlayerRankData(rankLeagueLoginResult.getData());
                }
            }

            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            GameEnvironment.userDataMap.remove(userUid);
            ThreadLocalUtil.remove();
        }
        // Map<String, Object> map = CommonUtils.responsePrepare(null);
        // log.info("战斗完成出现异常状态返回空数据，保证前端不要弹断线重连");
        // return map;
        return null;
    }


}
