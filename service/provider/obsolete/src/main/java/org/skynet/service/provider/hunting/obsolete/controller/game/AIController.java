package org.skynet.service.provider.hunting.obsolete.controller.game;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.battle.data.cvo.BattleStartInfoCVO;
import org.skynet.components.hunting.battle.query.StartQuery;
import org.skynet.components.hunting.battle.service.BattleFeignService;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.result.ResponseResult;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.DeflaterUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.HttpUtil;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.module.dto.BattleStartDto;
import org.skynet.service.provider.hunting.obsolete.module.dto.SearchAiDto;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.RecordDataAndBase64;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.AIControlRecordDataQueryDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerControlRecordData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerControlRecordDocData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.service.AiService;
import org.skynet.service.provider.hunting.obsolete.service.HuntingMatchService;
import org.skynet.service.provider.hunting.obsolete.service.ObsoleteUserDataService;
import org.skynet.service.provider.hunting.obsolete.service.PlayerControlRecordDataService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Api(tags = "AI相关操作")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class AIController {

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private HuntingMatchService huntingMatchService;

    @Resource
    private PlayerControlRecordDataService playerControlRecordDataService;

    @Resource
    private AiService aiService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private BattleFeignService battleFeignService;


//    @PostMapping("ai-aiControlRecordDataQuery")
//    @ApiOperation("根据条件,找到合适的AI操作录制文件并返回")
//    public Map<String,Object> aiControlRecordDataQuery(@RequestBody AIControlRecordDataQueryDTO request){
//
//        GameEnvironment.timeMessage.computeIfAbsent("aiControlRecordDataQuery", k -> new ArrayList<>());
//
//        try {
//            ThreadLocalUtil.set(request.getServerTimeOffset());
//            long startTime = System.currentTimeMillis();
//            log.info("[cmd] aiControlRecordDataQuery"+System.currentTimeMillis());
//            log.info(JSONObject.toJSONString(request));
//            CommonUtils.requestProcess(request,null,systemPropertiesConfig.getSupportRecordModeClient());
//
//            //载入当前玩家信息
//            String userUid = request.getUserUid();
//            userDataService.checkUserDataExist(userUid);
//            UserData userData = GameEnvironment.userDataMap.get(userUid);
//            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
//
//            PlayerControlRecordData findControlRecordData = null;
//            String findControlRecordEncodeData = null;
//            //对手是否掉线，只有整局匹配才有可能出现
//            //当匹配到某一回合时，完整匹配的录像数据没有后续操作，则认为对手掉线
//            boolean isOpponentDisconnect = false;
//            //可能方法在重试的时候，同时到达多次，这样会造成同一个round取出不同的ai录像。
//            //对HuntingMatchNowData的操作放在事务中。
//            //>=1.0.10版本，后续该处理可以去掉，因为所有方法都处理了重发逻辑
//
//            //载入当前比赛信息
//            HuntingMatchNowData huntingMatchNowData = null;
//            String path = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//            huntingMatchNowData = huntingMatchService.getHuntingMatchNowData(path, request.getHuntingMatchNowUid());
//            if (huntingMatchNowData==null){
//                throw new BusinessException("玩家"+request.getUserUid()+"请求AI录制文件,但是不存在HuntingMatchNowId:"+request.getHuntingMatchNowUid());
//            }
//
//
//            HuntingMatchAIRecordChooseMode aiRecordChooseMode = huntingMatchNowData.getAiRecordChooseMode();
//
//
//            //回合匹配
//            if(aiRecordChooseMode == HuntingMatchAIRecordChooseMode.WinProneRound||
//                aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LoseProneRound||
//                aiRecordChooseMode == HuntingMatchAIRecordChooseMode.drawProneRound
//            ){
//
//                log.info("使用AI回合录制选择模式."+aiRecordChooseMode);
//                if (huntingMatchNowData.getMatchAiRoundRuleTableId()==null){
//                    throw new BusinessException("AI回合录制选择模式下，huntingMatchNowData.matchAiRoundRuleTableId不能是null");
//                }
//
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
//                log.info("查询原始参数"+JSONObject.toJSONString(matchAiControlQuery));
//                MatchAiRoundRecordFilterParameters filterParameters = aiService.generateMatchAiRoundControlRecordFilterParameters(matchAiControlQuery,request.getGameVersion(),null);
//                log.info("查询生成过滤参数"+JSONObject.toJSONString(filterParameters));
//                if (filterParameters == null){
//                    throw new BusinessException("玩家"+request.getUserUid()+"无法生成AI匹配过滤参数");
//                }
//                if (filterParameters.getSafeFailedReason()!=null){
//                    log.info("AI回合查询安全失败，reason:"+filterParameters.getSafeFailedReason());
//                    return CommonUtils.responsePrepare(null);
//                }
//
//                RecordDataAndBase64 recordDataAndBase64 = aiService.loadAiControlRecordData(filterParameters, request.getGameVersion());
//
//                log.warn("查询出的ai录像原始数据，recordDataAndBase64为{}",JSONUtil.toJsonStr(recordDataAndBase64));
//
//                if (recordDataAndBase64 != null){
//                    findControlRecordData = recordDataAndBase64.getRecordData();
//                    findControlRecordEncodeData = recordDataAndBase64.getBase64();
//                }
//                if (findControlRecordData==null){
//                    log.error("原始查询参数"+matchAiControlQuery+",生成过滤参数"+filterParameters+", 无法匹配到任何操作数据,match uid"
//                            +huntingMatchNowData.getMatchUid());
//
//                }else{
//                    log.info("查找到录像"+findControlRecordData+", final score:"+findControlRecordData.getFinalScore()+", show precision: "
//                            +findControlRecordData.getAverageShowPrecision());
//
//                }
//            }
//            //整局匹配
//            else if(aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LowLevelWholeMatch||
//                aiRecordChooseMode == HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch||
//                aiRecordChooseMode == HuntingMatchAIRecordChooseMode.HighLevelWholeMatch
//            ){
//                PlayerUploadWholeMatchControlRecordData wholeMatchControlRecordsData = huntingMatchNowData.getWholeMatchControlRecordsData();
//
//                if (wholeMatchControlRecordsData == null){
//                    throw new BusinessException("比赛"+huntingMatchNowData.getMatchUid()+"是AI整局操作匹配，但是wholeMatchControlRecordsData为null");
//                }
//
//                int roundIndex = request.getRound() -1;
//                if (roundIndex<0){
//                    log.error("比赛"+huntingMatchNowData.getMatchUid()+"根据整局操作匹配，但是round"+request.getRound()+",round index"+roundIndex+"超出录像范围");
//                }
//
//                //检查后续回合是否有操作
//                isOpponentDisconnect =true;
//                if (roundIndex >= wholeMatchControlRecordsData.getEncodedBytes_Base64().size()){
//                    isOpponentDisconnect = false;
//                }else {
//                    for (int i = roundIndex; i < wholeMatchControlRecordsData.getEncodedBytes_Base64().size(); i++) {
//                        if (playerControlRecordDataService.checkControlRecordIsValid(wholeMatchControlRecordsData.getEncodedBytes_Base64().get(roundIndex))){
//                            isOpponentDisconnect = false;
//                            break;
//                        }
//                    }
//                }
//
//                if (isOpponentDisconnect){
//                    log.info("ai已经没有后续操作，可以认为断线");
//                }
//
//                if (!isOpponentDisconnect&&roundIndex<wholeMatchControlRecordsData.getEncodedBytes_Base64().size()){
//                    findControlRecordEncodeData = wholeMatchControlRecordsData.getEncodedBytes_Base64().get(roundIndex);
//                    String temp = playerControlRecordDataService.decodeControlRecordData(findControlRecordEncodeData);
//                    findControlRecordData =  JSONObject.parseObject(temp, PlayerControlRecordData.class);
//                    log.info("完整匹配获取AI录像。match uid: {}, round {}, final score:{}",huntingMatchNowData.getMatchUid(),request.getRound(),findControlRecordData.getFinalScore());
//                }
//            }else {
//                throw new BusinessException("未处理的ai record choose mode:"+aiRecordChooseMode);
//            }
//
//            InHuntingMatchAiFetchedControlRecordInfo aiFetchedDataInfo = new InHuntingMatchAiFetchedControlRecordInfo();
//
//            if (findControlRecordData == null){
//
//                aiFetchedDataInfo.setUid("");
//                aiFetchedDataInfo.setRecordGameVersion("");
//                aiFetchedDataInfo.setFinalScore(0);
//                aiFetchedDataInfo.setFetchedTime(TimeUtils.getUnixTimeMilliseconds());
//            }else {
//                aiFetchedDataInfo.setUid(findControlRecordData.getRecordUid());
//                aiFetchedDataInfo.setRecordGameVersion(findControlRecordData.getGameVersion());
//                aiFetchedDataInfo.setFinalScore(findControlRecordData.getFinalScore());
//                aiFetchedDataInfo.setFetchedTime(TimeUtils.getUnixTimeMilliseconds());
//            }
//
//            List<InHuntingMatchAiFetchedControlRecordInfo> aiFetchedControlRecordInfos = huntingMatchNowData.getAiFetchedControlRecordsInfos();
//            if (aiFetchedControlRecordInfos.size()>=request.getRound()){
//                log.info("查询ai但是aiFetchedControlRecordsInfos数组长度"+aiFetchedControlRecordInfos.size()+">=round"+request.getRound()+",可能是同一局客户端反复请求");
//                aiFetchedControlRecordInfos.add(request.getRound()-1,aiFetchedDataInfo);
//            }else {
//                aiFetchedControlRecordInfos.add(aiFetchedDataInfo);
//            }
//            huntingMatchNowData.setIsOpponentDisconnect(isOpponentDisconnect);
//
//            //保存匹配到的ai操作数据
//            String matchPath  = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//            huntingMatchService.reSaveHuntingMatchNowData(matchPath,request.getHuntingMatchNowUid(),userUid,huntingMatchNowData);
//
//            Map<String, Object> map = CommonUtils.responsePrepare(null);
//            userDataService.userDataSettlement(userData, sendToClientData,false,request.getGameVersion());
//            if (findControlRecordData != null){
//                map.put("recordDataBase64",findControlRecordEncodeData);
//            }
//            if (isOpponentDisconnect){
//                map.put("isOpponentDisconnect",isOpponentDisconnect);
//            }
//            long needTime = System.currentTimeMillis() - startTime;
//            GameEnvironment.timeMessage.get("aiControlRecordDataQuery").add(needTime);
//            log.info("[cmd] aiControlRecordDataQuery finish need time"+(System.currentTimeMillis()-startTime));
//            return map;
//
//        }catch (Exception e){
//
//            CommonUtils.responseException(request,e,request.getUserUid());
//        }finally {
//            ThreadLocalUtil.remove();
//        }
//
//        return null;
//    }


    @PostMapping("ai-aiControlRecordDataQuery")
    @ApiOperation("根据条件,找到合适的AI操作录制文件并返回")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> aiControlRecordDataQuery(@RequestBody AIControlRecordDataQueryDTO request) {

        GameEnvironment.timeMessage.computeIfAbsent("aiControlRecordDataQuery", k -> new ArrayList<>());

        String aiUrl = systemPropertiesConfig.getAiUrl();
        // String fightUrl = systemPropertiesConfig.getFightUrl();
        String userUid = request.getUserUid();

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] aiControlRecordDataQuery" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());

            //载入当前玩家信息
            obsoleteUserDataService.checkUserDataExist(userUid);
            UserData userData = GameEnvironment.userDataMap.get(userUid);
            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            PlayerControlRecordData findControlRecordData = null;
            String findControlRecordEncodeData = null;
            //对手是否掉线，只有整局匹配才有可能出现
            //当匹配到某一回合时，完整匹配的录像数据没有后续操作，则认为对手掉线
            boolean isOpponentDisconnect = false;
            //可能方法在重试的时候，同时到达多次，这样会造成同一个round取出不同的ai录像。
            //对HuntingMatchNowData的操作放在事务中。
            //>=1.0.10版本，后续该处理可以去掉，因为所有方法都处理了重发逻辑


            // RecordDataAndBase64 recordDataAndBase64 = null;
            BattleStartDto battleStartDto = new BattleStartDto();
            battleStartDto.setUid(request.getUserUid());
            battleStartDto.setVersion(request.getGameVersion());
            battleStartDto.setRound(request.getRound());
            battleStartDto.setPlayerScore(request.getPlayerFinalScore());
            battleStartDto.setAnimalRouteUid(request.getAnimalRouteUid());
            battleStartDto.setPlayerAveragePrecision(request.getPlayerAverageShowPrecision());
//            http://192.168.2.199:9301/battle/start
//            http://localhost:9301/battle/start
//             String fightFinalUrl = fightUrl + "/battle/start";
//             Map<String, Object> fightInfo = HttpUtil.getFightInfo(fightFinalUrl, battleStartDto);
            Result<BattleStartInfoCVO> startResult = battleFeignService.start(StartQuery.builder()
                    .version(request.getGameVersion())
                    .userId(request.getUserUid())
                    .playerAveragePrecision(request.getPlayerAverageShowPrecision().floatValue())
                    .playerScore(request.getPlayerFinalScore())
                    .round(request.getRound())
                    .animalRouteUid(request.getAnimalRouteUid())
                    .build());
            if (startResult.getSuccess()) {
                // Object data = fightInfo.get("data");
                // if (data != null) {
                //     JSONObject jsonObject = JSONObject.parseObject(data.toString());
                // Map jsonObject = (Map) startResult.getData();
                BattleStartInfoCVO jsonObject = startResult.getData();
                // Object recordData = jsonObject.get("recordDataBase64");
                // if (recordData != null) {
                //     findControlRecordEncodeData = recordData.toString();
                //     // log.warn("查询出的ai录像原始数据，recordDataAndBase64为{}", JSONUtil.toJsonStr(recordDataAndBase64));
                // }
                findControlRecordEncodeData = jsonObject.getRecordDataBase64();

                //如果战斗服返回的数据为空，再去本地数据库查找
                if (findControlRecordEncodeData == null) {
                    log.info("到本地数据库中查找数据");
                    //获得单回合的录像信息
//                        String singlePath = Path.getMatchSingleRoundControlRecordsPoolCollectionPath(
//                                request.getGameVersion(),
//                                Integer.valueOf(jsonObject.get("AnimalId").toString()),
//                                Long.valueOf(jsonObject.get("AnimalRouteUid").toString()),
//                                Integer.valueOf(jsonObject.get("GunId").toString()),
//                                Integer.valueOf(jsonObject.get("GunLevel").toString()),
//                                Integer.valueOf(jsonObject.get("BulletId").toString()),
//                                Integer.valueOf(jsonObject.get("WindId").toString()),
//                                Integer.valueOf(jsonObject.get("AveragePrecisionLevel").toString()));


//                        recordDataAndBase64 = findRecordDataFromLocal(singlePath);


                    SearchAiDto searchAiDto = new SearchAiDto(request.getGameVersion(),
                            jsonObject.getAnimalId(), //Integer.valueOf(jsonObject.get("animalId").toString()),
                            jsonObject.getAnimalRouteUid(),//Long.valueOf(jsonObject.get("animalRouteUid").toString()),
                            jsonObject.getGunId(), //Integer.valueOf(jsonObject.get("gunId").toString()),
                            jsonObject.getGunLevel(), //Integer.valueOf(jsonObject.get("gunLevel").toString()),
                            jsonObject.getBulletId(), //Integer.valueOf(jsonObject.get("bulletId").toString()),
                            jsonObject.getWindId(), //Integer.valueOf(jsonObject.get("windId").toString()),
                            jsonObject.getAveragePrecisionLevel()//Integer.valueOf(jsonObject.get("averagePrecisionLevel").toString())
                            );

                    Map<String, Object> aiInfo = HttpUtil.getAiInfo(aiUrl + "/huntingrival/ai-searchAiRecordData", searchAiDto);
                    if (aiInfo != null) {
                        try {
                            RecordDataAndBase64 recordDataAndBase64 = JSONUtil.toBean(aiInfo.get("data").toString(), RecordDataAndBase64.class);
                            findControlRecordEncodeData = recordDataAndBase64.getBase64();
                            log.info("找到的战斗信息：{}", recordDataAndBase64);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // recordDataAndBase64 = null;
                            log.error("战报格式转换错误，先将战报设置为空");
                        }
                    }


                }


                isOpponentDisconnect = BooleanUtil.isTrue(jsonObject.getIsOpponentDisconnect());//jsonObject.get("isOpponentDisconnect") != null && (boolean) jsonObject.get("isOpponentDisconnect");

                // }
            } else {
                log.warn("查询出的ai录像原始数据，recordDataAndBase64为空");
            }


            // if (recordDataAndBase64 != null) {
            //     findControlRecordData = recordDataAndBase64.getRecordData();
            //     findControlRecordEncodeData = recordDataAndBase64.getBase64();
            // } else {
            //     log.warn("查询出的ai录像原始数据，recordDataAndBase64为空");
            // }


//            InHuntingMatchAiFetchedControlRecordInfo aiFetchedDataInfo = new InHuntingMatchAiFetchedControlRecordInfo();
//
//            if (findControlRecordData == null){
//
//                aiFetchedDataInfo.setUid("");
//                aiFetchedDataInfo.setRecordGameVersion("");
//                aiFetchedDataInfo.setFinalScore(0);
//                aiFetchedDataInfo.setFetchedTime(TimeUtils.getUnixTimeMilliseconds());
//            }else {
//                aiFetchedDataInfo.setUid(findControlRecordData.getRecordUid());
//                aiFetchedDataInfo.setRecordGameVersion(findControlRecordData.getGameVersion());
//                aiFetchedDataInfo.setFinalScore(findControlRecordData.getFinalScore());
//                aiFetchedDataInfo.setFetchedTime(TimeUtils.getUnixTimeMilliseconds());
//            }

//            List<InHuntingMatchAiFetchedControlRecordInfo> aiFetchedControlRecordInfos = huntingMatchNowData.getAiFetchedControlRecordsInfos();
//            if (aiFetchedControlRecordInfos.size()>=request.getRound()){
//                log.info("查询ai但是aiFetchedControlRecordsInfos数组长度"+aiFetchedControlRecordInfos.size()+">=round"+request.getRound()+",可能是同一局客户端反复请求");
//                aiFetchedControlRecordInfos.add(request.getRound()-1,aiFetchedDataInfo);
//            }else {
//                aiFetchedControlRecordInfos.add(aiFetchedDataInfo);
//            }
//            huntingMatchNowData.setIsOpponentDisconnect(isOpponentDisconnect);

            //保存匹配到的ai操作数据
//            String matchPath  = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//            huntingMatchService.reSaveHuntingMatchNowData(matchPath,request.getHuntingMatchNowUid(),userUid,huntingMatchNowData);

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, false, request.getGameVersion());
            if (findControlRecordEncodeData != null) {
                map.put("recordDataBase64", findControlRecordEncodeData);
            }
            //todo 需要确定是否短线
            if (isOpponentDisconnect) {
                map.put("isOpponentDisconnect", isOpponentDisconnect);
            }
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("aiControlRecordDataQuery").add(needTime);
            log.info("[cmd] aiControlRecordDataQuery finish need time" + (System.currentTimeMillis() - startTime));
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            // CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            GameEnvironment.userDataMap.remove(userUid);
            ThreadLocalUtil.remove();
        }

        Map<String, Object> map = CommonUtils.responsePrepare(null);
        map.put("isOpponentDisconnect", false);
        // map.put("recordDataBase64", null);
        log.info("回合开始出现异常状态返回空数据，保证前端不要弹断线重连");
        return map;
    }


    @PostMapping("ai-searchAiRecordData")
    @ApiOperation("根据条件,找到合适的AI操作录制文件并返回")
    @RepeatSubmit(interval = 60000)
    public ResponseResult<RecordDataAndBase64> findAIForGame(@RequestBody SearchAiDto searchAiDto) {

        try {

            log.info("有人通过外部请求查找ai录像");

            String singlePath = Path.getMatchSingleRoundControlRecordsPoolCollectionPath(
                    searchAiDto.getGameVersion(),
                    searchAiDto.getAnimalId(),
                    searchAiDto.getAnimalRouteUid(),
                    searchAiDto.getGunId(),
                    searchAiDto.getGunLevel(),
                    searchAiDto.getBulletId(),
                    searchAiDto.getWindId(),
                    searchAiDto.getAveragePrecisionLevel());


            RecordDataAndBase64 recordDataAndBase64 = findRecordDataFromLocal(singlePath);

            log.info("找到的ai录像数据为：{}", JSONUtil.toJsonStr(recordDataAndBase64));

            return new ResponseResult<>(200, "SUCCESS", recordDataAndBase64);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult<>(400, "FAILURE", null);
        }
    }


    private RecordDataAndBase64 findRecordDataFromLocal(String singlePath) {

        Map<PlayerControlRecordData, String> information = new HashMap<>();
        //最终拿去计算的单回合数据
        List<PlayerControlRecordData> filterPlayerControlRecordDocData = new ArrayList<>();

        //从整回录像中中获取单回合录像
        log.info("数据查找路径：{}", singlePath);
        List<Object> list = RedisDBOperation.selectSingleRoundControlRecordsForAveragePrecisionLevel(singlePath);
        //每次选取的数目
        if (list != null && list.size() != 0) {
            List<String> zipStringList = new ArrayList<>();
            Collections.addAll(zipStringList, list.toArray(new String[0]));
            for (String zip : zipStringList) {
                String unzipString;
                PlayerControlRecordDocData recordDocData;
                try {
                    unzipString = DeflaterUtils.unzipString(zip);
                    recordDocData = JSONObject.parseObject(unzipString, PlayerControlRecordDocData.class);
                    if (recordDocData != null) {
                        String temp = playerControlRecordDataService.decodeControlRecordData(recordDocData.getRawDataBase64());
                        PlayerControlRecordData docData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
                        information.put(docData, recordDocData.getRawDataBase64());
                        filterPlayerControlRecordDocData.add(docData);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        PlayerControlRecordData resultRecord = null;
        RecordDataAndBase64 dataAndBase64 = null;
        if (filterPlayerControlRecordDocData.size() > 0) {

            int index = (int) Math.floor(Math.random() * Math.floor(filterPlayerControlRecordDocData.size()));
            resultRecord = filterPlayerControlRecordDocData.get(index);

        }
        if (resultRecord != null) {
            String base64 = information.get(resultRecord);
            dataAndBase64 = new RecordDataAndBase64(resultRecord, base64);
        }

        return dataAndBase64;


    }


}
