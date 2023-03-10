package org.skynet.service.provider.hunting.obsolete.controller.game;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.battle.data.BattleStartInfoCVO;
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

@Api(tags = "AI????????????")
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
//    @ApiOperation("????????????,???????????????AI???????????????????????????")
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
//            //????????????????????????
//            String userUid = request.getUserUid();
//            userDataService.checkUserDataExist(userUid);
//            UserData userData = GameEnvironment.userDataMap.get(userUid);
//            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
//
//            PlayerControlRecordData findControlRecordData = null;
//            String findControlRecordEncodeData = null;
//            //?????????????????????????????????????????????????????????
//            //???????????????????????????????????????????????????????????????????????????????????????????????????
//            boolean isOpponentDisconnect = false;
//            //??????????????????????????????????????????????????????????????????????????????round???????????????ai?????????
//            //???HuntingMatchNowData???????????????????????????
//            //>=1.0.10?????????????????????????????????????????????????????????????????????????????????
//
//            //????????????????????????
//            HuntingMatchNowData huntingMatchNowData = null;
//            String path = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//            huntingMatchNowData = huntingMatchService.getHuntingMatchNowData(path, request.getHuntingMatchNowUid());
//            if (huntingMatchNowData==null){
//                throw new BusinessException("??????"+request.getUserUid()+"??????AI????????????,???????????????HuntingMatchNowId:"+request.getHuntingMatchNowUid());
//            }
//
//
//            HuntingMatchAIRecordChooseMode aiRecordChooseMode = huntingMatchNowData.getAiRecordChooseMode();
//
//
//            //????????????
//            if(aiRecordChooseMode == HuntingMatchAIRecordChooseMode.WinProneRound||
//                aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LoseProneRound||
//                aiRecordChooseMode == HuntingMatchAIRecordChooseMode.drawProneRound
//            ){
//
//                log.info("??????AI????????????????????????."+aiRecordChooseMode);
//                if (huntingMatchNowData.getMatchAiRoundRuleTableId()==null){
//                    throw new BusinessException("AI??????????????????????????????huntingMatchNowData.matchAiRoundRuleTableId?????????null");
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
//
//                RecordDataAndBase64 recordDataAndBase64 = aiService.loadAiControlRecordData(filterParameters, request.getGameVersion());
//
//                log.warn("????????????ai?????????????????????recordDataAndBase64???{}",JSONUtil.toJsonStr(recordDataAndBase64));
//
//                if (recordDataAndBase64 != null){
//                    findControlRecordData = recordDataAndBase64.getRecordData();
//                    findControlRecordEncodeData = recordDataAndBase64.getBase64();
//                }
//                if (findControlRecordData==null){
//                    log.error("??????????????????"+matchAiControlQuery+",??????????????????"+filterParameters+", ?????????????????????????????????,match uid"
//                            +huntingMatchNowData.getMatchUid());
//
//                }else{
//                    log.info("???????????????"+findControlRecordData+", final score:"+findControlRecordData.getFinalScore()+", show precision: "
//                            +findControlRecordData.getAverageShowPrecision());
//
//                }
//            }
//            //????????????
//            else if(aiRecordChooseMode == HuntingMatchAIRecordChooseMode.LowLevelWholeMatch||
//                aiRecordChooseMode == HuntingMatchAIRecordChooseMode.MediumLevelWholeMatch||
//                aiRecordChooseMode == HuntingMatchAIRecordChooseMode.HighLevelWholeMatch
//            ){
//                PlayerUploadWholeMatchControlRecordData wholeMatchControlRecordsData = huntingMatchNowData.getWholeMatchControlRecordsData();
//
//                if (wholeMatchControlRecordsData == null){
//                    throw new BusinessException("??????"+huntingMatchNowData.getMatchUid()+"???AI???????????????????????????wholeMatchControlRecordsData???null");
//                }
//
//                int roundIndex = request.getRound() -1;
//                if (roundIndex<0){
//                    log.error("??????"+huntingMatchNowData.getMatchUid()+"?????????????????????????????????round"+request.getRound()+",round index"+roundIndex+"??????????????????");
//                }
//
//                //?????????????????????????????????
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
//                    log.info("ai?????????????????????????????????????????????");
//                }
//
//                if (!isOpponentDisconnect&&roundIndex<wholeMatchControlRecordsData.getEncodedBytes_Base64().size()){
//                    findControlRecordEncodeData = wholeMatchControlRecordsData.getEncodedBytes_Base64().get(roundIndex);
//                    String temp = playerControlRecordDataService.decodeControlRecordData(findControlRecordEncodeData);
//                    findControlRecordData =  JSONObject.parseObject(temp, PlayerControlRecordData.class);
//                    log.info("??????????????????AI?????????match uid: {}, round {}, final score:{}",huntingMatchNowData.getMatchUid(),request.getRound(),findControlRecordData.getFinalScore());
//                }
//            }else {
//                throw new BusinessException("????????????ai record choose mode:"+aiRecordChooseMode);
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
//                log.info("??????ai??????aiFetchedControlRecordsInfos????????????"+aiFetchedControlRecordInfos.size()+">=round"+request.getRound()+",???????????????????????????????????????");
//                aiFetchedControlRecordInfos.add(request.getRound()-1,aiFetchedDataInfo);
//            }else {
//                aiFetchedControlRecordInfos.add(aiFetchedDataInfo);
//            }
//            huntingMatchNowData.setIsOpponentDisconnect(isOpponentDisconnect);
//
//            //??????????????????ai????????????
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
    @ApiOperation("????????????,???????????????AI???????????????????????????")
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

            //????????????????????????
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
                //     // log.warn("????????????ai?????????????????????recordDataAndBase64???{}", JSONUtil.toJsonStr(recordDataAndBase64));
                // }
                findControlRecordEncodeData = jsonObject.getRecordDataBase64();

                //??????????????????????????????????????????????????????????????????
                if (findControlRecordEncodeData == null) {
                    log.info("?????????????????????????????????");
                    //??????????????????????????????
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
                            log.info("????????????????????????{}", recordDataAndBase64);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // recordDataAndBase64 = null;
                            log.error("???????????????????????????????????????????????????");
                        }
                    }


                }


                isOpponentDisconnect = BooleanUtil.isTrue(jsonObject.getIsOpponentDisconnect());//jsonObject.get("isOpponentDisconnect") != null && (boolean) jsonObject.get("isOpponentDisconnect");

                // }
            } else {
                log.warn("????????????ai?????????????????????recordDataAndBase64??????");
            }


            // if (recordDataAndBase64 != null) {
            //     findControlRecordData = recordDataAndBase64.getRecordData();
            //     findControlRecordEncodeData = recordDataAndBase64.getBase64();
            // } else {
            //     log.warn("????????????ai?????????????????????recordDataAndBase64??????");
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
//                log.info("??????ai??????aiFetchedControlRecordsInfos????????????"+aiFetchedControlRecordInfos.size()+">=round"+request.getRound()+",???????????????????????????????????????");
//                aiFetchedControlRecordInfos.add(request.getRound()-1,aiFetchedDataInfo);
//            }else {
//                aiFetchedControlRecordInfos.add(aiFetchedDataInfo);
//            }
//            huntingMatchNowData.setIsOpponentDisconnect(isOpponentDisconnect);

            //??????????????????ai????????????
//            String matchPath  = Path.getHuntingMatchNowCollectionPath(request.getGameVersion());
//            huntingMatchService.reSaveHuntingMatchNowData(matchPath,request.getHuntingMatchNowUid(),userUid,huntingMatchNowData);

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, false, request.getGameVersion());
            if (findControlRecordEncodeData != null) {
                map.put("recordDataBase64", findControlRecordEncodeData);
            }
            //todo ????????????????????????
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
        log.info("?????????????????????????????????????????????????????????????????????????????????");
        return map;
    }


    @PostMapping("ai-searchAiRecordData")
    @ApiOperation("????????????,???????????????AI???????????????????????????")
    @RepeatSubmit(interval = 60000)
    public ResponseResult<RecordDataAndBase64> findAIForGame(@RequestBody SearchAiDto searchAiDto) {

        try {

            log.info("??????????????????????????????ai??????");

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

            log.info("?????????ai??????????????????{}", JSONUtil.toJsonStr(recordDataAndBase64));

            return new ResponseResult<>(200, "SUCCESS", recordDataAndBase64);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult<>(400, "FAILURE", null);
        }
    }


    private RecordDataAndBase64 findRecordDataFromLocal(String singlePath) {

        Map<PlayerControlRecordData, String> information = new HashMap<>();
        //????????????????????????????????????
        List<PlayerControlRecordData> filterPlayerControlRecordDocData = new ArrayList<>();

        //??????????????????????????????????????????
        log.info("?????????????????????{}", singlePath);
        List<Object> list = RedisDBOperation.selectSingleRoundControlRecordsForAveragePrecisionLevel(singlePath);
        //?????????????????????
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
