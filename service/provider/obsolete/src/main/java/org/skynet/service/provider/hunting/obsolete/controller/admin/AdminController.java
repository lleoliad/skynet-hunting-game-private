package org.skynet.service.provider.hunting.obsolete.controller.admin;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.domain.ChestData;
import org.skynet.components.hunting.user.enums.ABTestGroup;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.result.ResponseResult;
import org.skynet.service.provider.hunting.obsolete.common.util.*;
import org.skynet.service.provider.hunting.obsolete.config.GameConfig;
import org.skynet.service.provider.hunting.obsolete.enums.HuntingMatchAIRecordChooseMode;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.CalculateAIFilterParametersBO;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.LocalPlayerCalculateAIFilterParametersBO;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.RecordDataAndBase64;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.*;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.skynet.service.provider.hunting.obsolete.pojo.table.*;
import org.skynet.service.provider.hunting.obsolete.service.*;
import org.skynet.starter.codis.service.CodisService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "管理员操作")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
@CrossOrigin
public class AdminController {

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private HuntingMatchService huntingMatchService;

    @Resource
    private AiService aiService;

    @Resource
    private PlayerControlRecordDataService playerControlRecordDataService;

    @Resource
    private AiRecordChooseRuleService aiRecordChooseRuleService;

    // @Resource
    // private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CodisService codisService;

    @PostMapping("admin-readUserData")
    @ApiOperation("获取某个用户的数据")
    public Map<String, Object> readUserData(@RequestBody BaseDTO dto) {

        try {
//            CommonUtils.processAdminRequest(dto.getAdminKey());
            UserData userData = RedisDBOperation.selectUserData("User:" + dto.getUserUid());

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", userData);
            return map;
        } catch (Exception e) {
            throw new BusinessException(e.toString(), -1);
        }
    }

    @PostMapping("admin-setUserData")
    @ApiOperation("设置某个用户的数据")
    public Map<String, Object> setUserData(@RequestBody SetUserDataDTO request) {
        try {
//            CommonUtils.processAdminRequest(request.getAdminKey());
            UserData userData = RedisDBOperation.selectUserData("User:" + request.getUserUid());

            obsoleteUserDataService.saveUserData(request.getUserData());

            return CommonUtils.responsePrepare(null);
        } catch (Exception e) {
            throw new BusinessException(e.toString(), -1);
        }
    }

    @PostMapping("mail-sendPlayerMail")
    @ApiOperation("向玩家发送邮件")
    public Map<String, Object> sendPlayerMail(@RequestBody MailDataDTO request) {

        try {
            CommonUtils.processAdminRequest(request.getAdminKey());
            obsoleteUserDataService.checkUserData(request.getTargetPlayerUid());
            if (request.getChestContent() != null) {

                ChestData chestData = request.getChestContent().getChestData();
                chestData.setCreateTime(TimeUtils.getUnixTimeSecond());
                chestData.setUid(NanoIdUtils.randomNanoId(30));
            }

            MailData mailData = new MailData(NanoIdUtils.randomNanoId(30),
                    request.getMailType(),
                    request.getTitle(),
                    request.getSenderName(),
                    TimeUtils.getUnixTimeMilliseconds(),
                    request.getMessage(),
                    request.getAttachmentType(),
                    request.getAttachmentId(),
                    request.getAttachmentCount(),
                    request.getChestContent());

            RedisDBOperation.insertUserInboxMail(request.getTargetPlayerUid(), mailData);
            log.info("收到新邮件" + JSONObject.toJSONString(mailData));

            Map<String, Object> map = CommonUtils.responsePrepare(null);

            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        }

        return null;
    }

    //根据输入的分数，找到其在对应的库中的最高排名和最低排名
    @PostMapping("getRankRange")
    @ApiOperation("根据输入的分数，找到其在对应的库中的最高排名和最低排名")
    public ResponseResult<Map<String, Object>> getRankRange(@RequestBody RankDto rankDto) {
        try {
            Integer lowestRank = -1;
            Integer highestRank = -1;
            //检验数据
            if (StringUtils.isEmpty(rankDto.getPoolName())) {
                throw new RuntimeException("数据库名不能为空");
            }
            if (StringUtils.isEmpty(rankDto.getScore())) {
                throw new RuntimeException("查询分数为空");
            }
            //检验数据库是否存在
            String poolName = rankDto.getPoolName();
            // if (Boolean.FALSE.equals(redisTemplate.hasKey(poolName))) {
            //     throw new RuntimeException("要查询的数据库不存在");
            // }
            if (Boolean.FALSE.equals(codisService.hasKey(poolName))) {
                throw new RuntimeException("要查询的数据库不存在");
            }
            List<MatchRecord> poolCollection = playerControlRecordDataService.getPoolCollection(poolName);
            List<Integer> scoreList = poolCollection.stream().map(MatchRecord::getScore).sorted((o1, o2) -> o2 - o1).collect(Collectors.toList());
            if (scoreList.size() == 0) {
                throw new RuntimeException("数据库中没有足够的数据");
            }
            LinkedList<Integer> integers = new LinkedList<>();
            Integer playerScore = rankDto.getScore();
            for (int i = 0; i < scoreList.size(); i++) {
                if (Objects.equals(scoreList.get(i), playerScore)) {
                    integers.add(i + 1);
                }
            }
            if (integers.size() == 0) {
                throw new RuntimeException("该分数在数据库中不存在");
            }
            lowestRank = integers.get(integers.size() - 1);
            highestRank = integers.get(0);
            Map<String, Object> result = new HashMap<>();
            result.put("lowestRank", lowestRank);
            result.put("highestRank", highestRank);
            return new ResponseResult<>(0, "SUCCESS", result);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult<>(-1, e.getMessage(), null);
        }
    }

    //    @PostMapping("battleScript")
    @ApiOperation("对战脚本")
    public Map<String, Object> battleScript(@RequestBody BattleScriptDTO request) {

        try {
            CommonUtils.processAdminRequest(request.getAdminKey());
            //确定进入哪场比赛和动物
            Integer matchId = determineEnterWhichMatch(request.getChapterId(), request.getChapterEnteredCount(), request.getGameVersion());
            //本次AI录像查找模式
            HuntingMatchAIRecordChooseMode aiRecordChooseMode = determineAIRecordChooseMode(request.getChapterEnteredCount(),
                    String.valueOf(request.getChapterId()),
                    request.getGameVersion(),
                    request.getWinningProbability(), request.getCultivateScore(), request.getLoseStreak(), request.getWinStreak());

            PlayerWeaponInfo opponentPlayerWeaponInfo = null;
            PlayerWeaponInfo localPlayerWeaponInfo = null;
            OpponentPlayerInfo opponentPlayerInfo = null;
            MatchAIRoundRuleTableValue matchAiRoundRuleTableValue = null;

            Map<String, Object> map = null;
            if (!(aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.WinProneRound) ||
                    aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.LoseProneRound) ||
                    aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.drawProneRound
                    ))) {
                map = queryWholeMatchControlRecords(
                        request.getTrophy(),
                        aiRecordChooseMode,
                        request.getChapterId(),
                        matchId,
                        request.getGameVersion(),
                        request.getMediaScore(),
                        request.getUserUid());
            }

            PlayerUploadWholeMatchControlRecordData wholeMatchControlRecordsData = null;
            Integer ranking = null;
            Integer poolSize = null;
            String segmentCollectionRef = null;
            if (map != null) {
                wholeMatchControlRecordsData = (PlayerUploadWholeMatchControlRecordData) map.get("recordData");
                ranking = (Integer) map.get("ranking");
                poolSize = (Integer) map.get("poolSize");
                segmentCollectionRef = (String) map.get("segmentCollectionRef");
            }

            if (wholeMatchControlRecordsData != null) {
                opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByWholeMatchRecordsData(wholeMatchControlRecordsData);

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
                matchAiRoundRuleTableValue = huntingMatchService.determineMatchAiRoundRuleTableValue(request.getChapterId(), request.getChapterEnteredCount(), aiRecordChooseMode, request.getGameVersion());

                if (matchAiRoundRuleTableValue == null) {
                    throw new BusinessException("无法找到合适的matchAiRoundRuleTableValue");
                }

                log.info("匹配到MatchAIRoundRuleTable条目" + JSONObject.toJSONString(matchAiRoundRuleTableValue));
                opponentPlayerWeaponInfo = aiService.generateAiWeaponInfoByMatchAiRoundRule(matchAiRoundRuleTableValue, request.getGameVersion());
            }
            //生成匹配ai信息
            Integer count = wholeMatchControlRecordsData == null ? null : wholeMatchControlRecordsData.getSafePlayerTrophyCount();
            opponentPlayerInfo = generateOpponentPlayerInfo(request.getTrophy(),
                    request.getChapterId(),
                    request.getChapterEnteredCount(),
                    count,
                    request.getUserUid(),
                    request.getGameVersion());

            Map<String, Object> fightMessageMap = new LinkedHashMap<>();
            Map<String, Object> returnMap = new LinkedHashMap<>();
            //ai相关属性
//            fightMessageMap.put("AIName",opponentPlayerInfo.getName());
//            fightMessageMap.put("AIIcon",opponentPlayerInfo.getIcon_base64());
            fightMessageMap.put("AITrophy", opponentPlayerInfo.getTrophy());
            fightMessageMap.put("aiRecordChooseMode", aiRecordChooseMode);


            //走回合匹配
            if (matchAiRoundRuleTableValue != null) {
                Integer windId = 0;
                Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(request.getGameVersion());
                ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(request.getChapterId()));
//                List<Integer> matchIdArray = chapterTableValue.getMatchIdArray();
                Long[] routeIds = GameEnvironment.matchToRouteUidMap.get(String.valueOf(matchId));
                Map<String, MatchTableValue> WindIdTable = GameEnvironment.matchTableMap.get(request.getGameVersion());
                if (WindIdTable != null) {
                    windId = WindIdTable.get(matchId.toString()).getWindId();
                }
//                List<Integer> animalIds = request.getAnimalIds();
//                List<Long> routeIds = request.getRouteIds();
                fightMessageMap.put("windId", windId);
                /**
                 int round,
                 int chapterId,
                 int matchAiRoundRuleId,
                 String gameVersion,
                 ABTestGroup abTestGroup,
                 int score,
                 double averagePrecision
                 */
                if (request.getChapterId() == 1) {
                    //三回合
                    MatchAiRoundRecordFilterParameters filterParameters1 = generateMatchAiRoundControlRecordFilterParameters(1, chapterTableValue.getId(), matchAiRoundRuleTableValue.getId(), request.getGameVersion(), null, request.getFirstScore(), request.getFirstPrecision());
                    MatchAiRoundRecordFilterParameters filterParameters2 = generateMatchAiRoundControlRecordFilterParameters(2, chapterTableValue.getId(), matchAiRoundRuleTableValue.getId(), request.getGameVersion(), null, request.getSecondScore(), request.getSecondPrecision());
                    MatchAiRoundRecordFilterParameters filterParameters3 = generateMatchAiRoundControlRecordFilterParameters(3, chapterTableValue.getId(), matchAiRoundRuleTableValue.getId(), request.getGameVersion(), null, request.getThirdScore(), request.getThirdPrecision());

                    RecordDataAndBase64 recordDataAndBase64ByRound1 = loadAiControlRecordData(filterParameters1, request.getGameVersion(), opponentPlayerWeaponInfo, routeIds[0], windId, request.getFirstPrecision());
                    RecordDataAndBase64 recordDataAndBase64ByRound2 = loadAiControlRecordData(filterParameters2, request.getGameVersion(), opponentPlayerWeaponInfo, routeIds[1], windId, request.getSecondPrecision());
                    RecordDataAndBase64 recordDataAndBase64ByRound3 = loadAiControlRecordData(filterParameters3, request.getGameVersion(), opponentPlayerWeaponInfo, routeIds[2], windId, request.getThirdPrecision());

                    //ai总分
                    int aiScore = 0;
                    aiScore = recordDataAndBase64ByRound1.getRecordData().getFinalScore() + recordDataAndBase64ByRound2.getRecordData().getFinalScore() + recordDataAndBase64ByRound3.getRecordData().getFinalScore();

                    fightMessageMap.put("aiScore", aiScore);
                    fightMessageMap.put("1-score", recordDataAndBase64ByRound1.getRecordData().getFinalScore());
                    fightMessageMap.put("1-precision", recordDataAndBase64ByRound1.getRecordData().getAverageShowPrecision());

                    fightMessageMap.put("2-score", recordDataAndBase64ByRound2.getRecordData().getFinalScore());
                    fightMessageMap.put("2-precision", recordDataAndBase64ByRound2.getRecordData().getAverageShowPrecision());

                    fightMessageMap.put("3-score", recordDataAndBase64ByRound3.getRecordData().getFinalScore());
                    fightMessageMap.put("3-precision", recordDataAndBase64ByRound3.getRecordData().getAverageShowPrecision());

                } else if (request.getChapterId() == 2) {
                    //四回合
                    MatchAiRoundRecordFilterParameters filterParameters1 = generateMatchAiRoundControlRecordFilterParameters(1, chapterTableValue.getId(), matchAiRoundRuleTableValue.getId(), request.getGameVersion(), null, request.getFirstScore(), request.getFirstPrecision());
                    MatchAiRoundRecordFilterParameters filterParameters2 = generateMatchAiRoundControlRecordFilterParameters(2, chapterTableValue.getId(), matchAiRoundRuleTableValue.getId(), request.getGameVersion(), null, request.getSecondScore(), request.getSecondPrecision());
                    MatchAiRoundRecordFilterParameters filterParameters3 = generateMatchAiRoundControlRecordFilterParameters(3, chapterTableValue.getId(), matchAiRoundRuleTableValue.getId(), request.getGameVersion(), null, request.getThirdScore(), request.getThirdPrecision());
                    MatchAiRoundRecordFilterParameters filterParameters4 = generateMatchAiRoundControlRecordFilterParameters(4, chapterTableValue.getId(), matchAiRoundRuleTableValue.getId(), request.getGameVersion(), null, request.getFourthScore(), request.getFourthPrecision());

                    RecordDataAndBase64 recordDataAndBase64ByRound1 = loadAiControlRecordData(filterParameters1, request.getGameVersion(), opponentPlayerWeaponInfo, routeIds[0], windId, request.getFirstPrecision());
                    RecordDataAndBase64 recordDataAndBase64ByRound2 = loadAiControlRecordData(filterParameters2, request.getGameVersion(), opponentPlayerWeaponInfo, routeIds[1], windId, request.getSecondPrecision());
                    RecordDataAndBase64 recordDataAndBase64ByRound3 = loadAiControlRecordData(filterParameters3, request.getGameVersion(), opponentPlayerWeaponInfo, routeIds[2], windId, request.getThirdPrecision());
                    RecordDataAndBase64 recordDataAndBase64ByRound4 = loadAiControlRecordData(filterParameters4, request.getGameVersion(), opponentPlayerWeaponInfo, routeIds[3], windId, request.getFourthPrecision());

                    //ai总分
                    int aiScore = 0;
                    aiScore = recordDataAndBase64ByRound1.getRecordData().getFinalScore() + recordDataAndBase64ByRound2.getRecordData().getFinalScore() + recordDataAndBase64ByRound3.getRecordData().getFinalScore() + recordDataAndBase64ByRound4.getRecordData().getFinalScore();

                    fightMessageMap.put("aiScore", aiScore);
                    fightMessageMap.put("1-score", recordDataAndBase64ByRound1.getRecordData().getFinalScore());
                    fightMessageMap.put("1-precision", recordDataAndBase64ByRound1.getRecordData().getAverageShowPrecision());

                    fightMessageMap.put("2-score", recordDataAndBase64ByRound2.getRecordData().getFinalScore());
                    fightMessageMap.put("2-precision", recordDataAndBase64ByRound2.getRecordData().getAverageShowPrecision());

                    fightMessageMap.put("3-score", recordDataAndBase64ByRound3.getRecordData().getFinalScore());
                    fightMessageMap.put("3-precision", recordDataAndBase64ByRound3.getRecordData().getAverageShowPrecision());

                    fightMessageMap.put("4-score", recordDataAndBase64ByRound4.getRecordData().getFinalScore());
                    fightMessageMap.put("4-precision", recordDataAndBase64ByRound4.getRecordData().getAverageShowPrecision());

                } else if (request.getChapterId() >= 3 && request.getChapterId() <= 12) {
                    //五回合
                    MatchAiRoundRecordFilterParameters filterParameters1 = generateMatchAiRoundControlRecordFilterParameters(1, chapterTableValue.getId(), matchAiRoundRuleTableValue.getId(), request.getGameVersion(), null, request.getFirstScore(), request.getFirstPrecision());
                    MatchAiRoundRecordFilterParameters filterParameters2 = generateMatchAiRoundControlRecordFilterParameters(2, chapterTableValue.getId(), matchAiRoundRuleTableValue.getId(), request.getGameVersion(), null, request.getSecondScore(), request.getSecondPrecision());
                    MatchAiRoundRecordFilterParameters filterParameters3 = generateMatchAiRoundControlRecordFilterParameters(3, chapterTableValue.getId(), matchAiRoundRuleTableValue.getId(), request.getGameVersion(), null, request.getThirdScore(), request.getThirdPrecision());
                    MatchAiRoundRecordFilterParameters filterParameters4 = generateMatchAiRoundControlRecordFilterParameters(4, chapterTableValue.getId(), matchAiRoundRuleTableValue.getId(), request.getGameVersion(), null, request.getFourthScore(), request.getFourthPrecision());
                    MatchAiRoundRecordFilterParameters filterParameters5 = generateMatchAiRoundControlRecordFilterParameters(5, chapterTableValue.getId(), matchAiRoundRuleTableValue.getId(), request.getGameVersion(), null, request.getFifthScore(), request.getFifthPrecision());


                    RecordDataAndBase64 recordDataAndBase64ByRound1 = loadAiControlRecordData(filterParameters1, request.getGameVersion(), opponentPlayerWeaponInfo, routeIds[0], windId, request.getFirstPrecision());
                    RecordDataAndBase64 recordDataAndBase64ByRound2 = loadAiControlRecordData(filterParameters2, request.getGameVersion(), opponentPlayerWeaponInfo, routeIds[1], windId, request.getSecondPrecision());
                    RecordDataAndBase64 recordDataAndBase64ByRound3 = loadAiControlRecordData(filterParameters3, request.getGameVersion(), opponentPlayerWeaponInfo, routeIds[2], windId, request.getThirdPrecision());
                    RecordDataAndBase64 recordDataAndBase64ByRound4 = loadAiControlRecordData(filterParameters4, request.getGameVersion(), opponentPlayerWeaponInfo, routeIds[3], windId, request.getFourthPrecision());
                    RecordDataAndBase64 recordDataAndBase64ByRound5 = loadAiControlRecordData(filterParameters5, request.getGameVersion(), opponentPlayerWeaponInfo, routeIds[4], windId, request.getFifthPrecision());

                    //ai总分
                    int aiScore = 0;
                    aiScore = recordDataAndBase64ByRound1.getRecordData().getFinalScore() + recordDataAndBase64ByRound2.getRecordData().getFinalScore() + recordDataAndBase64ByRound3.getRecordData().getFinalScore() + recordDataAndBase64ByRound4.getRecordData().getFinalScore() + recordDataAndBase64ByRound5.getRecordData().getFinalScore();

                    fightMessageMap.put("aiScore", aiScore);
                    fightMessageMap.put("1-score", recordDataAndBase64ByRound1.getRecordData().getFinalScore());
                    fightMessageMap.put("1-precision", recordDataAndBase64ByRound1.getRecordData().getAverageShowPrecision());

                    fightMessageMap.put("2-score", recordDataAndBase64ByRound2.getRecordData().getFinalScore());
                    fightMessageMap.put("2-precision", recordDataAndBase64ByRound2.getRecordData().getAverageShowPrecision());

                    fightMessageMap.put("3-score", recordDataAndBase64ByRound3.getRecordData().getFinalScore());
                    fightMessageMap.put("3-precision", recordDataAndBase64ByRound3.getRecordData().getAverageShowPrecision());

                    fightMessageMap.put("4-score", recordDataAndBase64ByRound4.getRecordData().getFinalScore());
                    fightMessageMap.put("4-precision", recordDataAndBase64ByRound4.getRecordData().getAverageShowPrecision());

                    fightMessageMap.put("5-score", recordDataAndBase64ByRound5.getRecordData().getFinalScore());
                    fightMessageMap.put("5-precision", recordDataAndBase64ByRound5.getRecordData().getAverageShowPrecision());

                } else {
                    throw new BusinessException("章节错误,chapterId不能小于1或大于12");
                }

                fightMessageMap.put("wholeMatch", false);
                //ai库编号
                fightMessageMap.put("matchAIRoundRuleID", matchAiRoundRuleTableValue.getId());
                //武器id
                List<Integer> weaponCombinationIdArray = matchAiRoundRuleTableValue.getAiWeaponCombinationId();
                Integer randomId = (Integer) NumberUtils.randomElementInArray(weaponCombinationIdArray);
                fightMessageMap.put("weaponId", randomId);
                fightMessageMap.put("gunId", opponentPlayerWeaponInfo.getGunId());
                fightMessageMap.put("gunLevel", opponentPlayerWeaponInfo.getGunLevel());
                fightMessageMap.put("bulletId", opponentPlayerWeaponInfo.getBulletId());
            }

            //走整局匹配
            if (wholeMatchControlRecordsData != null) {

                int aiScore = 0;
                Integer matchIdForWhole = wholeMatchControlRecordsData.getMatchId();
                Map<String, MatchTableValue> WindIdTable = GameEnvironment.matchTableMap.get(request.getGameVersion());
                Integer windId = 0;
                if (WindIdTable != null) {
                    windId = WindIdTable.get(matchIdForWhole.toString()).getWindId();
                }
                fightMessageMap.put("wholeMatch", true);
                fightMessageMap.put("ranking", ranking);
                fightMessageMap.put("poolSize", poolSize);
                fightMessageMap.put("poolName", segmentCollectionRef);
                fightMessageMap.put("gunId", wholeMatchControlRecordsData.getPlayerGunId());
                fightMessageMap.put("gunLevel", wholeMatchControlRecordsData.getPlayerGunLevel());
                fightMessageMap.put("bulletId", wholeMatchControlRecordsData.getPlayerBulletId());
                fightMessageMap.put("windId", windId);
                fightMessageMap.put("lowestRank", map.get("lowestRank"));
                fightMessageMap.put("highestRank", map.get("highestRank"));


                List<String> encodedBytes_base64 = wholeMatchControlRecordsData.getEncodedBytes_Base64();
                if (!StringUtils.isEmpty(encodedBytes_base64)) {

                    for (int i = 0; i < encodedBytes_base64.size(); i++) {


                        String temp = playerControlRecordDataService.decodeControlRecordData(encodedBytes_base64.get(i));
                        PlayerControlRecordData controlRecordData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
                        aiScore += controlRecordData.getFinalScore();
                        fightMessageMap.put((i + 1) + "-score", controlRecordData.getFinalScore());
                        fightMessageMap.put((i + 1) + "-precision", controlRecordData.getAverageShowPrecision());
                    }
                }

                fightMessageMap.put("aiScore", aiScore);
            }
            returnMap.put("fightMessageMap", fightMessageMap);
            returnMap.put("log", JSONObject.toJSONString(fightMessageMap));
            return returnMap;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        }
        return null;
    }


    public RecordDataAndBase64 loadAiControlRecordData(MatchAiRoundRecordFilterParameters filterParameters, String gameVersion, PlayerWeaponInfo weaponInfo, Long animalRouteUid, Integer windId, double averagePrecision) {

        MatchAiRoundControlQuery matchAiControlQuery = filterParameters.getOriginQuery();

        Map<String, MatchTableValue> matchTable = GameEnvironment.matchTableMap.get(gameVersion);


//        MatchTableValue matchTableValue = matchTable.get(String.valueOf(matchAiControlQuery.getMatchId()));

//        PlayerWeaponInfo weaponInfo = filterParameters.getOriginQuery().getAiWeaponInfo();

        //获得单回合的录像信息
        //MatchRoundControlRecordsArchived_3:animalId102:animalRouteUid2114898951:gunId3:gunLevel1:bulletId1
//        String version = gameVersion.substring(4);
//        String path = Path.getMatchRoundControlRecordsPoolCollectionPath(
//                gameVersion,
//                filterParameters.getAnimalId(),
//                animalRouteUid,
//                weaponInfo.getGunId(),
//                weaponInfo.getGunLevel(),
//                weaponInfo.getBulletId(),
//                windId,
//                averagePrecision);
        String singlePath = Path.getMatchSingleRoundControlRecordsPoolCollectionPath(
                gameVersion,
                filterParameters.getAnimalId(),
                animalRouteUid,
                weaponInfo.getGunId(),
                weaponInfo.getGunLevel(),
                weaponInfo.getBulletId(),
                windId,
                averagePrecision);


//        List<SingleRecordIndex> candidateList = new ArrayList<>();
        Map<PlayerControlRecordData, String> information = new HashMap<>();
        //最终拿去计算的单回合数据
        List<PlayerControlRecordData> filterPlayerControlRecordDocData = new ArrayList<>();

        //从单回合池子中取录像
        List<Object> list = RedisDBOperation.selectSingleRoundControlRecords(singlePath);
//        List<Object> list = RedisDBOperation.selectMatchRoundControlRecords(path);
        List<Object> tempList = new ArrayList<>();
        //每次选取的数目
        Integer count = NumberUtils.randomInt(50.0, 100.0);
        if (list != null && list.size() != 0) {
            List<String> zipStringList = new ArrayList<>();
            tempList.addAll(list);
            Collections.shuffle(tempList);
            //修改切割范围，防止下标越界
//            List<Object> subList = tempList.subList(0, count>tempList.size()?tempList.size():count);
            List<Object> subList = tempList;
            Collections.addAll(zipStringList, subList.toArray(new String[0]));
            for (String zip : zipStringList) {
                String unzipString = null;
                PlayerControlRecordDocData recordDocData = null;
                try {
                    unzipString = DeflaterUtils.unzipString(zip);
                    recordDocData = JSONObject.parseObject(unzipString, PlayerControlRecordDocData.class);
                    String temp = playerControlRecordDataService.decodeControlRecordData(recordDocData.getRawDataBase64());
                    PlayerControlRecordData docData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
                    information.put(docData, recordDocData.getRawDataBase64());
                    filterPlayerControlRecordDocData.add(docData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        //如果单回合池子中没有录像，再从整回合中的取
        //MatchSingleRoundControlRecordsArchived_3:animalId102:animalRouteUid2114898951:gunId3:gunLevel1:bulletId1
//        if(list.size() == 0){
//            String path = Path.getMatchRoundControlRecordsPoolCollectionPath(
//                    gameVersion,
//                    filterParameters.getAnimalId(),
//                    animalRouteUid,
//                    weaponInfo.getGunId(),
//                    weaponInfo.getGunLevel(),
//                    weaponInfo.getBulletId(),
//                    windId,
//                    averagePrecision);
////            String singlePath = Path.getMatchSingleRoundControlRecordsPoolCollectionPath(
////                    gameVersion,
////                    filterParameters.getAnimalId(),
////                    animalRouteUid,
////                    weaponInfo.getGunId(),
////                    weaponInfo.getGunLevel(),
////                    weaponInfo.getBulletId(),
////                    windId,
////                    averagePrecision);
//
//            List<Object> singleLists = RedisDBOperation.selectMatchRoundControlRecords(path);
////            List<Object> singleLists = RedisDBOperation.selectSingleRoundControlRecords(singlePath);
//            List<Object> tempSingleList= new ArrayList<>();
//            List<String> zipStringList = new ArrayList<>();
//            if (singleLists!=null&&singleLists.size()!=0){
//                tempSingleList.addAll(singleLists);
//                Collections.shuffle(tempSingleList);
//                //修改切割范围，防止下标越界
////                List<Object> subList = tempSingleList.subList(0, count>tempSingleList.size()?tempSingleList.size():count);
//                List<Object> subList = tempSingleList;
//                Collections.addAll(zipStringList,subList.toArray(new String[0]));
//                for (String zip : zipStringList) {
//                    String unzipString = null;
//                    PlayerControlRecordDocData recordDocData = null;
//                    try {
//                        unzipString = DeflaterUtils.unzipString(zip);
//                        recordDocData = JSONObject.parseObject(unzipString, PlayerControlRecordDocData.class);
//                        String temp = playerControlRecordDataService.decodeControlRecordData(recordDocData.getRawDataBase64());
//                        PlayerControlRecordData docData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
//                        information.put(docData,recordDocData.getRawDataBase64());
//                        filterPlayerControlRecordDocData.add(docData);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }
//        }

        List<PlayerControlRecordData> findDocs = new ArrayList<>();

        if (filterParameters.getAiScoreRange() != null && filterParameters.getSecureAIScoreRange() != null) {

            findDocs = searchRecordsWithScoreAndPrecisionInDatabase(filterPlayerControlRecordDocData, filterParameters.getAiScoreRange(), filterParameters.getAiShowPrecisionRange());
            //如果正常范围没有找到任何结果,尝试使用安全范围
            if (findDocs == null || findDocs.size() == 0) {

                findDocs = searchRecordsWithScoreAndPrecisionInDatabase(filterPlayerControlRecordDocData, filterParameters.getSecureAIScoreRange(), filterParameters.getAiShowPrecisionRange());

            }
        } else {

            findDocs = searchRecordsWithPrecisionInDatabase(filterPlayerControlRecordDocData, filterParameters.getAiShowPrecisionRange());
            if (findDocs == null || findDocs.size() == 0) {
                log.info("正常范围没有找到任何结果,尝试使用安全范围");
                findDocs = searchRecordsWithPrecisionInDatabase(filterPlayerControlRecordDocData, filterParameters.getSecureAIShowPrecisionRange());
            }
        }

        //如果安全范围都找不到,从任意精度里面找
        if (findDocs == null || findDocs.size() == 0) {

            log.error("过滤参数" + filterParameters + "无法找到合适的录制文件,放开所有条件查找");
            findDocs = searchRecordsWithPrecisionInDatabase(filterPlayerControlRecordDocData, new Double[]{0.0, 1.0});
        }

        PlayerControlRecordData resultRecord = null;
        RecordDataAndBase64 dataAndBase64 = null;
        if (findDocs != null && findDocs.size() > 0) {

            int index = (int) Math.floor(Math.random() * Math.floor(findDocs.size()));
            resultRecord = findDocs.get(index);

        }
        if (resultRecord != null) {
            String base64 = information.get(resultRecord);
            dataAndBase64 = new RecordDataAndBase64(resultRecord, base64);
        }

//        System.out.println("12312313");
//        if (dataAndBase64 == null){
//            String base64 = "j6lQbGF5ZXJVaWS+d05kN2hVRGdvMi16c2E2c2hNcGRVVE1TU3VTV0dVq0dhbWVWZXJzaW9upTEuMC43rVJlY29yZFZlcnNpb24CqVJlY29yZFVpZMCwUmVjb3JkRGF0YVNvdXJjZQG8QWxsQ29udHJvbFNlZ21lbnRSZWNvcmRzRGF0YZGFt1N0YXJ0QWltQXRvbUNvbnRyb2xEYXRhgqxTdGFydEFpbVRpbWXKP8bISLhBaW1JbkFuaW1hbExvY2FsUG9zaXRpb26TyjtEs13KvK/VGcq9ADqat0RyYWdnaW5nQ29udHJvbERhdGFMaXN03AAbkso/xshIk8o7RDaZyryv1yzKvQA90ZLKP9OcFZPKOz95csq8r5s5yr0AeyySyj/eS8eTyrk0cd3KO0EIKcq8b/iVkso/7UIjk8q51V7qyjx8Dt7Ku9A+CJLKP/L1MpPKudzX1Mo8fxVdyrvSaC+Syj/3O6mTyrnc1HvKPINok8q7yaWlkso/+4I8k8q52zYSyjyFc7XKu7hT9pLKP//IypPKucpN7Mo8h7C8yruFpGuSykACB5WTyrmwcO3KPImlGsq7H0FmkspABCrsk8q5j0X6yjyMievKumQiiJLKQAZOSZPKuWl8+8o8kVnHyjfAru6SykAIzIGTyrlUuYzKPJPijMo5pqxmkspACu+ck8q5R+dqyjyWPmzKOefrf5LKQA0SzpPKuT1dEMo8mdI5yjnpjaGSykAPNlmTyrk9k+HKPJufoMo5umVMkspAEVmCk8q5P2VIyjyeDV7KOVbpMpLKQBN82ZPKuUWvsso8nx6QyjiI792SykAVn8WTyrlI8VDKPKABS8q3R67ukspAF8L1k8q5TCwmyjyiMuHKuSfDTJLKQBpBMpPKuVc2zMo8pX+4yrn74u6SykAcZIGTyrlcorvKPKdhOsq6JlBbkspAHixnk8q5Y/7uyjynRWPKujffv5LKQCCquJPKuWhp9Mo8p0Lbyro8QIqSykAizhiTyrlrs3fKPKieF8q6UhLIkspAJPGSk8q5bGl5yjypiADKulhDrpLKQCdvs5PKuWYuUMo8q2P8yrpTNzuSykApkqeTyrlp/B/KPKtjXMq6VuautkhpdEZpcmVBdG9tQ29udHJvbERhdGGNqEZpcmVUaW1lykAqSS+4QWltSW5BbmltYWxMb2NhbFBvc2l0aW9uk8q5bucFyjyqVYrKulZ7d7lIaXRNZXNoVHJpYW5nbGVTdGFydEluZGV4zRzOvkhpdFRyaWFuZ2xlQmFyeWNlbnRyaWNQb3NpdGlvbpPKPfFhtMo/IR+1yj6BaCmuQnVsbGV0TW92ZVRpbWXKPA1+n7FJc0hpdENyaXRpY2FsUGFydMO6SGl0Q3JpdGljYWxQYXJ0Q29uZmlnSW5kZXgB2S1IaXRMb2NhbE5vcm1hbGl6ZWRQb3NpdGlvbkluQ3JpdGljYWxQYXJ0U3BhY2WSyr2d86jKvfgQAKtJc0ZsaWNrRmlyZcKwSGl0U2hvd1ByZWNpc2lvbso/cKPXr0hpdERpc3RhbmNlWWFyZMpCo5mau0hpdERpcmVjdGlvbkluVHJpYW5nbGVTcGFjZZPKvmsywso+Cm8syj92vY+mRGFtYWdlzOa5Tm90SGl0RmlyZUF0b21Db250cm9sRGF0YcC5UmVsZWFzZUFpbUF0b21Db250cm9sRGF0YcCqUmFuZG9tU2VlZNK5dIWmrkFuaW1hbFJvdXRlVWlkzn4OzAeoQW5pbWFsSWRlqEJ1bGxldElkAaVHdW5JZAOoR3VuTGV2ZWwBrElzQW5pbWFsS2lsbMOqRmluYWxTY29yZcy+tEF2ZXJhZ2VTaG93UHJlY2lzaW9uyj9wo9c=";
//            String playerUid = "wNd7hUDgo2-zsa6shMpdUTMSSuSWGU";
////            String gameVersion = "1.0.9";
//            PlayerControlRecordData recordData = new PlayerControlRecordData();
//            dataAndBase64 = new RecordDataAndBase64(recordData,base64);
//        }
        return dataAndBase64;

    }

    public List<PlayerControlRecordData> searchRecordsWithPrecisionInDatabase(List<PlayerControlRecordData> filterList, Double[] averageShowPrecisionRange) {


        filterList.sort(Comparator.comparingDouble(PlayerControlRecordData::getAverageShowPrecision));

        //每次查询文档个数
        int eachQueryDocCount = (new Random().nextInt(5) + 1) * 50;

        List<PlayerControlRecordData> foundRecordsDocData = new ArrayList<>();

        int count = 0;
        while (count < filterList.size()) {

            //上传的浮点数,有精度问题,例如0.71,存储到firestore后,可能变为0.7099999785423279,这时候它>=0.71是false
            //所以比较的下限要减去0.01,
            //比较上限的时候,要加上0.01,因为0.71可能是0.71000000112,这时候它 <= 0.71是false
            for (PlayerControlRecordData data : filterList) {
                if (data.getAverageShowPrecision() > averageShowPrecisionRange[0] - 0.001 && data.getAverageShowPrecision() < averageShowPrecisionRange[1] + 0.001) {
                    foundRecordsDocData.add(data);
                }
                count++;

            }

            if (foundRecordsDocData.size() > eachQueryDocCount) {
                break;
            }
        }
        return foundRecordsDocData;
    }


    public List<PlayerControlRecordData> searchRecordsWithScoreAndPrecisionInDatabase(List<PlayerControlRecordData> list, Integer[] scoreRange, Double[] averageShowPrecisionRange) {

//        list = list.stream().sorted(new Comparator<PlayerControlRecordData>() {
//            @Override
//            public int compare(PlayerControlRecordData o1, PlayerControlRecordData o2) {
//                //先按照finalScore排序，finalScore相等再按照averageShowPrecision排序
//                int i = o1.getFinalScore().compareTo(o2.getFinalScore());
//                if (i==0) if (o1.getAverageShowPrecision() < o2.getAverageShowPrecision())
//                    return 1;
//                return i;
//            }
//        }).collect(Collectors.toList());

//        Arrays.sort(averageShowPrecisionRange);
//        Integer cursorIncrement = (int) ((averageShowPrecisionRange[1] - averageShowPrecisionRange[0]) / CommonUtils.randomFloat(5.0, 10.0));
//        Integer cursorValue = averageShowPrecisionRange[0].intValue();
//        Integer lastFindCursorValue = cursorValue;
        //每次查询文档个数
        int eachQueryDocCount = (new Random().nextInt(5) + 1) * 50;

        List<PlayerControlRecordData> finalList = new ArrayList<>();
        int count = 0;
        while (count < list.size()) {

            //上传的浮点数,有精度问题,例如0.71,存储到firestore后,可能变为0.7099999785423279,这时候它>=0.71是false
            //所以比较的下限要减去0.01,
            //比较上限的时候,要加上0.01,因为0.71可能是0.71000000112,这时候它 <= 0.71是false

            for (PlayerControlRecordData data : list) {
//                if (data.getPlayerUid().equals(userUid)){
//                    continue;
//                }
                if (data.getFinalScore() >= scoreRange[0] && data.getFinalScore() <= scoreRange[1]) {
                    finalList.add(data);
                }
                count++;
            }

            if (finalList.size() >= eachQueryDocCount) {
                break;
            }
        }

        List<PlayerControlRecordData> resultList = new ArrayList<>();
        for (PlayerControlRecordData data : finalList) {
            if (data.getAverageShowPrecision() >= averageShowPrecisionRange[0] && data.getAverageShowPrecision() <= averageShowPrecisionRange[0]) {
                resultList.add(data);
            }
        }

        if (resultList.size() != 0) {
            return resultList;
        }

        return finalList;
    }


    public MatchAiRoundRecordFilterParameters generateMatchAiRoundControlRecordFilterParameters(int round,
                                                                                                int chapterId,
                                                                                                int matchAiRoundRuleId,
                                                                                                String gameVersion,
                                                                                                ABTestGroup abTestGroup,
                                                                                                int score,
                                                                                                double averagePrecision) {

        Map<String, MatchAIRoundRuleTableValue> matchAiRuleTable = GameEnvironment.matchAIRoundRuleTableMap.get(gameVersion);
        Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(gameVersion);

        ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(chapterId));
        MatchAIRoundRuleTableValue matchAiRuleTableValue = matchAiRuleTable.get(String.valueOf(matchAiRoundRuleId));


        int animalSequenceIndex = round - 1;
        if (animalSequenceIndex >= chapterTableValue.getMatchRouteAnimalSequence().size()) {

            return new MatchAiRoundRecordFilterParameters(null, null, null, null, null, null, "Round exceed");
        }

        int animalId = chapterTableValue.getMatchRouteAnimalSequence().get(animalSequenceIndex);


        //ruleIndex的index
        int ruleIndexIndex = round - 1;
        if (ruleIndexIndex < 0) {
            throw new BusinessException("过滤AI规则 filter：" + round + "round - 1 < 0");
        }

        ruleIndexIndex %= matchAiRuleTableValue.getRuleIndex().size();

        int ruleIndex = matchAiRuleTableValue.getRuleIndex().get(ruleIndexIndex);

        //是否是玩家先开枪
        Boolean isPlayerFirst = matchAiRuleTableValue.getIsPlayerFirst();
        if (isPlayerFirst) {

            //获取玩家先手规则数据
            Map<String, LocalPlayerFirstAiRecordChooseRule> playerFirstAiRecordChooseRuleTable = aiRecordChooseRuleService.loadPlayerFirstAiRecordChooseRule(gameVersion, abTestGroup);
            LocalPlayerFirstAiRecordChooseRule playerFirstAiRecordChooseRule = null;

            if (!playerFirstAiRecordChooseRuleTable.containsKey(String.valueOf(ruleIndex))) {
                throw new BusinessException("获取玩家先手ai record choose rule,没有rule index " + ruleIndex + ",table:" + JSONObject.toJSONString(playerFirstAiRecordChooseRuleTable));
            } else {
                playerFirstAiRecordChooseRule = playerFirstAiRecordChooseRuleTable.get(String.valueOf(ruleIndex));
            }

            LocalPlayerCalculateAIFilterParametersBO filterParameters = aiRecordChooseRuleService.localPlayerCalculateAIFilterParameters(playerFirstAiRecordChooseRule, score, averagePrecision);

            if (filterParameters != null) {

                log.info("过滤玩家先手AI规则 filter: 找到规则条目" + ruleIndex + ", AI过滤规则" + filterParameters);

                return new MatchAiRoundRecordFilterParameters(
                        animalId,
                        null,
                        filterParameters.getAiScoreRange(),
                        filterParameters.getAiShowPrecisionRange(),
                        filterParameters.getSecureAIScoreRange(),
                        filterParameters.getSecureAIShowPrecisionRange(),
                        null
                );
            } else {

                throw new BusinessException("过滤AI规则 filter: 找到规则条目" + ruleIndex + ", 但是无法创建FilterParameters");
            }

        } else {
            //获取ai先手规则数据
            Map<String, AiFirstAiRecordChooseRule> aiFirstAiRecordChooseRuleTable = aiRecordChooseRuleService.loadAiFirstAiRecordChooseRule(gameVersion, abTestGroup);
            AiFirstAiRecordChooseRule aiFirstAiRecordChooseRule = null;

            if (!aiFirstAiRecordChooseRuleTable.containsKey(String.valueOf(ruleIndex))) {

                throw new BusinessException("获取ai先手 ai record choose rule,没有rule index" + ruleIndex);

            } else {

                aiFirstAiRecordChooseRule = aiFirstAiRecordChooseRuleTable.get(String.valueOf(ruleIndex));

            }

            CalculateAIFilterParametersBO filterParameters = aiRecordChooseRuleService.calculateAIFilterParameters(aiFirstAiRecordChooseRule);
            log.info("过滤玩家先手规则 filter: " + "找到规则条目" + ruleIndex + ",AI过滤规则" + filterParameters);

            return new MatchAiRoundRecordFilterParameters(
                    animalId,
                    null,
                    null,
                    filterParameters.getAiShowPrecisionRange(),
                    null,
                    filterParameters.getSecureAIShowPrecisionRange(),
                    null
            );
        }

    }


    public OpponentPlayerInfo generateOpponentPlayerInfo(Integer trophy,
                                                         Integer chapterId,
                                                         Integer chapterEnteredCount,
                                                         Integer opponentTrophyCount,
                                                         String userUid,
                                                         String gameVersion) {

        final double aiProfileCount = 1071d;
        OpponentPlayerInfo opponentPlayerInfo = new OpponentPlayerInfo(null, null, null, null, null);

        Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(gameVersion);
        //对手奖杯根据玩家变化
        ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(chapterId));

        if (opponentTrophyCount == null) {
            opponentPlayerInfo.setTrophy(NumberUtils.randomInt(trophy * (1 - GameConfig.aiTrophyToPlayerChangeRatio), trophy * (1 + GameConfig.aiTrophyToPlayerChangeRatio)));
            opponentPlayerInfo.setTrophy(Math.max(chapterTableValue.getUnlockRequiresTrophyCount(), opponentPlayerInfo.getTrophy()));
        } else {
            opponentPlayerInfo.setTrophy(opponentTrophyCount);
            log.info("直接设置对手奖杯数:" + opponentTrophyCount);
        }

        boolean useTotalRandomAiProfile = NumberUtils.randomFloat(0d, 1d) <= GameConfig.randomNameAiRatioInPlayerMatching;

        //如果是第一章前三局,那么都是随机玩家信息
        if (chapterId == 1) {

            if (chapterEnteredCount <= 3) {
                useTotalRandomAiProfile = true;
            }
        }
        if (useTotalRandomAiProfile) {
            opponentPlayerInfo.setName(obsoleteUserDataService.createGuestName(userUid));
            opponentPlayerInfo.setIcon_base64(null);
            opponentPlayerInfo.setUseDefaultIcon(true);
        } else {
            //从服务器中获得对手名称和头像
            Integer aiProfileId = NumberUtils.randomInt(1.0, aiProfileCount);

            String collectionPath = Path.getDefaultAiProfileCollectionPath();

            String redisAiProfileId = collectionPath + ":" + aiProfileId;
            OpponentProfile opponentProfile = RedisDBOperation.selectOpponentProfile(redisAiProfileId);

            if (opponentProfile == null) {
                throw new BusinessException("获取ai profile id" + aiProfileId + "不存在");
            }
            opponentPlayerInfo.setName(opponentProfile.getName());
            opponentPlayerInfo.setIcon_base64(opponentProfile.getIcon_base64());
            opponentPlayerInfo.setUseDefaultIcon(opponentProfile.getUseDefaultIcon());
        }


        return opponentPlayerInfo;
    }

    private Integer determineEnterWhichMatch(Integer chapterId, Integer chapterEnteredCount, String gameVersion) {

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

    private HuntingMatchAIRecordChooseMode determineAIRecordChooseMode(Integer chapterEnteredCount,
                                                                       String chapterId,
                                                                       String gameVersion,
                                                                       Double chapterWinRate,
                                                                       Double playerCultivateScore,
                                                                       Integer loseStreak,
                                                                       Integer winStreak) {

        Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(gameVersion);
        ChapterTableValue chapterTableValue = chapterTable.get(chapterId);

        log.warn("当前章节的表配置信息：{}", JSONUtil.toJsonStr(chapterTableValue));


        //每个章节的前几次比赛，都是胜利倾向的回合匹配
        if (chapterEnteredCount <= chapterTableValue.getForceWinProneRoundMatchChapterEnterCount()) {
            log.info("章节进入次数" + chapterEnteredCount + ",进入胜利偏向回合匹配");
            return HuntingMatchAIRecordChooseMode.WinProneRound;
        }

        boolean forceRoundMatch = false;
        if (chapterEnteredCount <= chapterTableValue.getForceRoundMatchChapterEnterCount()) {
            forceRoundMatch = true;
        }

        /**
         * 1、新增胜率配置表：章节、保底胜率、较低胜率、较高胜率、最大养成胜率加成、最小养成分数、最大养成分数
         2、该章节保底胜率=保底胜率+养成胜率加成
         3、该章节较低胜率=较低胜率+养成胜率加成
         4、养成胜率加成=MAX(玩家养成分数-最小养成分数，0)/|最大养成分数-最小养成分数|*最大养成胜率加成
         * */

        double cultivateWinRateAddition =
                Math.max(0, Math.min(playerCultivateScore, chapterTableValue.getMaxCultivateScore()) - chapterTableValue.getMinCultivateScore())
                        * chapterTableValue.getMaxCultivateWinRateAddition()
                        / Math.max(0, chapterTableValue.getMaxCultivateScore() - chapterTableValue.getMinCultivateScore());

        log.warn("计算出的养成胜率加成{}", cultivateWinRateAddition);

        double minGuaranteeWinRate = chapterTableValue.getMinGuaranteeWinRate() + cultivateWinRateAddition;

        log.warn("计算出的最小胜率{}", minGuaranteeWinRate);

        double lowWinRate = chapterTableValue.getLowWinRate() + cultivateWinRateAddition;

        log.warn("计算出的保底胜率{}", lowWinRate);

        if (loseStreak >= GameConfig.ForceWinProneRoundMatchLoseStreakCount || chapterWinRate <= minGuaranteeWinRate) {
            //判断胜率，胜率最优先
            if (winStreak < GameConfig.ForceHighLevelWholeMatchWinStreakCount && loseStreak < GameConfig.ForceLowLevelWholeMatchLoseStreakCount) {
                log.warn("强制进行WinProneRound模式匹配");
                log.warn("玩家连胜次数：{}，玩家连败次数{}，玩家章节胜率：{}，minGuaranteeWinRate：{}", winStreak, loseStreak, chapterWinRate, minGuaranteeWinRate);
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

        //把连胜连败的条件放到最下面,连胜连败不看胜率
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
            throw new BusinessException("无法确定ai的选择规则。chapter entered count: " + chapterEnteredCount + ",chapter win rate: " + chapterWinRate + ",win streak: " +
                    winStreak + ", lose streak: " + loseStreak);
        }

        if (forceRoundMatch) {
            HuntingMatchAIRecordChooseMode matchModeBefore = wholeMatchMode;
            HuntingMatchAIRecordChooseMode roundMatchMode = aiService.convertAiRecordChooseModeFromWholeMatchToRound(wholeMatchMode);
            log.info("章节进入次数" + chapterEnteredCount + ",强制进入回合匹配. from" + matchModeBefore + "to " + roundMatchMode);
            return roundMatchMode;
        }
        return wholeMatchMode;
    }

    //为AI查找整场比赛的录像
    public Map<String, Object> queryWholeMatchControlRecords(Integer playerTrophy,
                                                             HuntingMatchAIRecordChooseMode aiRecordChooseMode,
                                                             Integer chapterId,
                                                             Integer matchId,
                                                             String gameVersion,
                                                             Integer playerMedianScore, String userUid) {


        if (aiRecordChooseMode == HuntingMatchAIRecordChooseMode.WinProneRound || aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LoseProneRound ||
                aiRecordChooseMode == HuntingMatchAIRecordChooseMode.drawProneRound) {
            throw new BusinessException("不支持的ai record choose mode: " + aiRecordChooseMode);
        }

        RangeInt trophySegmentRange = playerControlRecordDataService.getControlRecordTrophySegmentRange(chapterId, playerTrophy, gameVersion);

        //匹配池属性的key
        String metaDataDocRef = Path.getMatchControlRecordsPool(gameVersion, matchId, trophySegmentRange);

        String segmentCollectionRef = Path.getMatchControlRecordsPoolTrophySegmentMetaData(
                gameVersion,
                matchId,
                trophySegmentRange
        );
        //对应匹配池的属性
        MatchControlRecordsPoolMetaData poolMetaData = playerControlRecordDataService.getPoolTrophySegmentMetaData(metaDataDocRef, segmentCollectionRef);

        if (poolMetaData == null) {
            log.error(segmentCollectionRef + "暂时还未生成");
            return null;
        }

        //正式服务器下，才检查池子是否够数量，不然没法测试了
//        if (!poolMetaData.getIsUsable()){
//            log.info("操作池"+metaDataDocRef+"还不可用");
//            return null;
//        }


        //计算玩家在该池中的中位数排名
        double playerMedianScoreRankPercent = (playerMedianScore - poolMetaData.getLowestFinalScore()) * 1.0 / (poolMetaData.getHighestFinalScore() - poolMetaData.getLowestFinalScore());

        playerMedianScoreRankPercent = Math.min(Math.max(playerMedianScoreRankPercent, 0.0), 1.0);

        /**
         * 3、匹配方式 ：
         3.1、根据AI库中AI数据的总分数进行排行
         3.2、普通完整匹配：在总分排行20%-80%的AI中随机抽取
         3.3、高水平完整匹配：
         1、如果玩家中位数在前20%内，则在排行前20%的中随机
         2、如果玩家中位数不在前20%内，则在中位数以上中随机
         3.4、保底完整匹配：
         1、如果玩家中位数在后20%内，则在排行后20%中随机
         2、如果玩家中位数不在后20%内，则在中位数以下中随机
         */
        Integer[] targetScoreRange = new Integer[]{poolMetaData.getLowestFinalScore(), poolMetaData.getHighestFinalScore()};//保证一大一小，一开始两个都写成了getLowestFinalScore()方法
        //给数组排序，保证第一个数是最小值，第二个数是最大值
//        Arrays.sort(targetScoreRange);

        //拿出池子里的所有数据,并逆序排序
        //去匹配池找到录像的排名
        List<Object> dataList = RedisDBOperation.selectMatchControlRecordsPoolTrophySegmentCollectionContent(segmentCollectionRef);
        if (dataList == null || dataList.size() == 0) {
            log.warn("找不到对应的匹配池子：{}", segmentCollectionRef);
        }
//        List<MatchRecord> poolCollection = playerControlRecordDataService.getPoolCollection(segmentCollectionRef);
        List<MatchRecord> poolCollection = new ArrayList<>();
        Collections.addAll(poolCollection, dataList.toArray(new MatchRecord[0]));
        poolCollection.sort((o1, o2) -> o2.getScore() - o1.getScore());

        //符合条件的新池子
        List<MatchRecord> conditionList = new LinkedList<>();

        switch (aiRecordChooseMode) {

            case LowLevelWholeMatch:
                if (playerMedianScoreRankPercent <= 0.2) {
                    for (int i = (int) (poolCollection.size() * 0.8); i < poolCollection.size(); i++) {
                        conditionList.add(poolCollection.get(i));
                    }
                    targetScoreRange[1] = poolCollection.get((int) (poolCollection.size() * 0.8)).getScore();
                    targetScoreRange[0] = poolCollection.get(poolCollection.size() - 1).getScore();
//                    targetScoreRange[1]=NumberUtils.lerp(poolMetaData.getLowestFinalScore()*1.0,poolMetaData.getHighestFinalScore()*1.0,playerMedianScoreRankPercent);
                } else {
                    for (int i = (int) (poolCollection.size() * 0.5); i < poolCollection.size(); i++) {
                        conditionList.add(poolCollection.get(i));
                    }
                    targetScoreRange[1] = poolCollection.get((int) (poolCollection.size() * 0.5)).getScore();
                    targetScoreRange[0] = poolCollection.get(poolCollection.size() - 1).getScore();
//                    targetScoreRange[1]= playerMedianScore;
                }
                break;
            case MediumLevelWholeMatch:
                for (int i = (int) (poolCollection.size() * 0.2); i <= (int) (poolCollection.size() * 0.8); i++) {
                    conditionList.add(poolCollection.get(i));
                }
                targetScoreRange[1] = poolCollection.get((int) (poolCollection.size() * 0.2)).getScore();
                targetScoreRange[0] = poolCollection.get((int) (poolCollection.size() * 0.8)).getScore();
//                targetScoreRange[0] = NumberUtils.lerp(poolMetaData.getLowestFinalScore()*1.0,poolMetaData.getHighestFinalScore()*1.0,0.2);
//                targetScoreRange[1] = NumberUtils.lerp(poolMetaData.getLowestFinalScore()*1.0,poolMetaData.getHighestFinalScore()*1.0,0.8);
                break;
            case HighLevelWholeMatch:
                if (playerMedianScoreRankPercent > 0.8) {
                    for (int i = 0; i <= (int) (poolCollection.size() * 0.2); i++) {
                        conditionList.add(poolCollection.get(i));
                    }
                    targetScoreRange[1] = poolCollection.get(0).getScore();
                    targetScoreRange[0] = poolCollection.get((int) (poolCollection.size() * 0.2)).getScore();
//                    double v = RandomUtil.randomInt(80, 100) * 0.01;
//                    targetScoreRange[0] = NumberUtils.lerp(poolMetaData.getLowestFinalScore()*1.0,poolMetaData.getHighestFinalScore()*1.0,0.8);
                } else {
                    for (int i = 0; i < (int) (poolCollection.size() * 0.5); i++) {
                        conditionList.add(poolCollection.get(i));
                    }
                    targetScoreRange[1] = poolCollection.get(0).getScore();
                    targetScoreRange[0] = poolCollection.get((int) (poolCollection.size() * 0.5)).getScore();
//                    targetScoreRange[0] = playerMedianScore;
                }
                break;
        }

        if (conditionList.size() == 0) {
            log.warn("匹配池中找不到合适的数据");
            return null;
        }

        log.info("当前玩家匹配模式：{}", aiRecordChooseMode);
        log.info("玩家整局比赛匹配，目标分数：" + Arrays.toString(targetScoreRange) + ",collection path " + metaDataDocRef);

//        targetScoreRange[0] = Math.round(targetScoreRange[0]);
//        targetScoreRange[1] = Math.round(targetScoreRange[1]);

//        log.info("玩家整局比赛匹配，目标分数："+ Arrays.toString(targetScoreRange) +",collection path "+metaDataDocRef);

//        int cursorIncrement = (int) Math.floor((targetScoreRange[1]-targetScoreRange[0]) / (RandomUtil.randomInt(1,5)*1.0));
        List<MatchRecord> foundWholeMatchRecordsDataArray = new ArrayList<>();
//        int cursorStartValue = targetScoreRange[0];
//        int cursorEndValue = targetScoreRange[0] + cursorIncrement;

//        log.info("玩家整局比赛匹配，目标分数："+ Arrays.toString(targetScoreRange) +",collection path "+metaDataDocRef);
//        jiZja8I8n0nYQ68BR0H3vLZ0Grw_5D
//        OgAOwMAN_x3uIkG4YglXDMiTjtX2DM
        //获取玩家最近匹配的对局
        List<String> usedWholeMatchControlRecordsUids = playerControlRecordDataService.getPlayerUsedWholeMatchControlRecordsUids(userUid, chapterId, gameVersion);
        List<PlayerUploadWholeMatchControlRecordData> candidateList = null;
        int i = 0;
        int min = 1;
        int randomIndex = 0;
        while (++i <= usedWholeMatchControlRecordsUids.size() + 5) {
            int max = Math.min(200, conditionList.size());
            //制造一个随机数，当做索引下标
            if (max != min) {
                randomIndex = RandomUtil.randomInt(min, max);
            }
            //根据随机下标从池子中拿一个数据
            foundWholeMatchRecordsDataArray.add(conditionList.get(randomIndex));
            //根据MatchRecord中录像的索引找出所有符合条件的录像
            candidateList = playerControlRecordDataService.getPlayerUploadWholeMatchControlRecordDataCollection(foundWholeMatchRecordsDataArray, gameVersion);
            PlayerUploadWholeMatchControlRecordData recordData = candidateList.get(0);
            if (usedWholeMatchControlRecordsUids.contains(recordData.getUid()) || recordData.getPlayerUid().equals(userUid)) {
                continue;
            }

            //重新保存
            playerControlRecordDataService.savePlayerUsedWholeMatchControlRecordsUids(userUid, chapterId, recordData.getUid(), gameVersion);
            recordData.setSafePlayerTrophyCount(trophySegmentRange.clampInRange(recordData.getPlayerTrophyCount()));

            //去匹配池找到录像的排名
//            List<Object> dataList = RedisDBOperation.selectMatchControlRecordsPoolTrophySegmentCollectionContent(segmentCollectionRef);

//            List<MatchRecord> recordList = new LinkedList<>();
//            Collections.addAll(recordList, dataList.toArray(new MatchRecord[0]));
//            if (recordList.size() == 0) {
//                log.info("匹配池" + segmentCollectionRef + "不存在");
//                return null;
//            }

            List<Integer> scoreList = poolCollection.stream().map(MatchRecord::getScore).collect(Collectors.toList());
            //分数在池子里的排名
            //有序集合才能使用二分查找，所以这里想要使用二分查找，需要先给集合排序
//            Collections.sort(scoreList);
            //排名直接用的数组下标，导致分数越高，排名越高
            int ranking = Collections.binarySearch(scoreList, recordData.getPlayerTotalScore()) + 1;
            Map<String, Object> map = new HashMap<>();

            map.put("recordData", recordData);
            map.put("ranking", ranking);
            map.put("poolSize", scoreList.size());
            map.put("segmentCollectionRef", segmentCollectionRef);

            if (map.get("segmentCollectionRef") != null && map.get("recordData") != null) {
                RankDto rankDto = new RankDto(recordData.getPlayerTotalScore(), segmentCollectionRef);
                ResponseResult<Map<String, Object>> rankRange = getRankRange(rankDto);
                Map<String, Object> data = rankRange.getData();
//                result.put("lowestRank",lowestRank);
//                result.put("highestRank",highestRank);
                map.put("lowestRank", data.get("lowestRank"));
                map.put("highestRank", data.get("highestRank"));
            }

            return map;

        }


        //这次找的数目
//        Integer count = NumberUtils.randomInt(50.0, 100.0);


//        if (conditionList.size() != 0){
//
//            for (MatchRecord data : conditionList) {
//
//                //找够了就直接退出
//                if (data.getScore()>=cursorStartValue&&data.getScore()<=cursorEndValue){
//                    foundWholeMatchRecordsDataArray.add(data);
//                }
//                if (foundWholeMatchRecordsDataArray.size()>count)
//                    break;
//
//            }
//        }
//
//        if (foundWholeMatchRecordsDataArray.size()==0){
//            return null;
//        }
//
//        if (foundWholeMatchRecordsDataArray.size()<count){
//            log.info("池子里没有足够的数据,需要"+count+"个, 目标分数： "+ Arrays.toString(targetScoreRange) +",collection path "+metaDataDocRef);
//        }


        //玩家最近匹配到的对局


//        NumberUtils.shuffleArray(candidateList,0);
//        candidateList.sort((o1, o2) -> o2.getPlayerTotalScore() - o1.getPlayerTotalScore());

        //排除自己和最近匹配到对局
//        for (PlayerUploadWholeMatchControlRecordData recordData : candidateList) {
//
//            if (usedWholeMatchControlRecordsUids.contains(recordData.getUid())||recordData.getPlayerUid().equals(userUid)){
//                continue;
//            }
//
//            //重新保存
//            playerControlRecordDataService.savePlayerUsedWholeMatchControlRecordsUids(userUid,chapterId,recordData.getUid(),gameVersion);
//            recordData.setSafePlayerTrophyCount(trophySegmentRange.clampInRange(recordData.getPlayerTrophyCount()));
//
//            //去匹配池找到录像的排名
//            List<Object> dataList = RedisDBOperation.selectMatchControlRecordsPoolTrophySegmentCollectionContent(segmentCollectionRef);
//
//            List<MatchRecord> recordList = new LinkedList<>();
//            Collections.addAll(recordList,dataList.toArray(new MatchRecord[0]));
//            if (recordList.size()==0){
//                log.info("匹配池"+segmentCollectionRef+"不存在");
//                return null;
//            }
//
//            List<Integer> scoreList = recordList.stream().map(MatchRecord::getScore).collect(Collectors.toList());
//            //分数在池子里的排名
//            //有序集合才能使用二分查找，所以这里想要使用二分查找，需要先给集合排序
//            Collections.sort(scoreList);
//            //排名直接用的数组下标，导致分数越高，排名越高
//            int ranking = scoreList.size()-Collections.binarySearch(scoreList, recordData.getPlayerTotalScore())+1;
//            Map<String,Object> map = new HashMap<>();
//
//            map.put("recordData",recordData);
//            map.put("ranking",ranking);
//            map.put("poolSize",scoreList.size());
//            map.put("segmentCollectionRef",segmentCollectionRef);
//
//            return map;
//        }


        return null;
    }


    public MatchControlRecordsPoolMetaData getPoolTrophySegmentMetaData(String metaDataDocRef, String segmentCollectionRef) {

        //先去redis中找
        MatchControlRecordsPoolMetaData metaData = RedisDBOperation.selectMatchControlRecordsPoolMetaData(metaDataDocRef);
        if (metaData != null) {
            return metaData;
        }
        int count = Math.toIntExact(RedisDBOperation.selectMatchControlRecordsPoolTrophySegmentCollectionLength(segmentCollectionRef));
        boolean available = false;
        if (count >= GameConfig.wholeMatchRecordPoolValidMinRecordCount) {
            available = true;
        }
        List<Object> dataList = RedisDBOperation.selectMatchControlRecordsPoolTrophySegmentCollectionContent(segmentCollectionRef);

        List<MatchRecord> recordList = new LinkedList<>();
        Collections.addAll(recordList, dataList.toArray(new MatchRecord[0]));
        if (recordList.size() == 0) {
            log.info("匹配池" + segmentCollectionRef + "不存在");
            return null;
        }
        int lowest = recordList.get(0).getScore();
        int highest = recordList.get(0).getScore();
        for (MatchRecord record : recordList) {

            lowest = Math.min(record.getScore(), lowest);

            highest = Math.max(record.getScore(), highest);
        }
        MatchControlRecordsPoolMetaData newMetaData = new MatchControlRecordsPoolMetaData(lowest, highest, available);
        //保存一下
        RedisDBOperation.insertPoolTrophySegmentMetaData(metaDataDocRef, newMetaData);
        return newMetaData;
    }
}
