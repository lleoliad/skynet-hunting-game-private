package org.skynet.service.provider.hunting.obsolete.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.enums.ABTestGroup;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.DeflaterUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.NumberUtils;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.enums.HuntingMatchAIRecordChooseMode;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.CalculateAIFilterParametersBO;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.LocalPlayerCalculateAIFilterParametersBO;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.RecordDataAndBase64;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.service.AiRecordChooseRuleService;
import org.skynet.service.provider.hunting.obsolete.service.AiService;
import org.skynet.service.provider.hunting.obsolete.service.HuntingMatchService;
import org.skynet.service.provider.hunting.obsolete.service.PlayerControlRecordDataService;
import lombok.extern.slf4j.Slf4j;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.table.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    @Resource
    private AiRecordChooseRuleService aiRecordChooseRuleService;

    @Resource
    private PlayerControlRecordDataService playerControlRecordDataService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private HuntingMatchService huntingMatchService;

    @Override
    public HuntingMatchAIRecordChooseMode convertAiRecordChooseModeFromWholeMatchToRound(HuntingMatchAIRecordChooseMode aiRecordChooseMode) {

        /*
      * ?????????????????????????????????????????????????????????????????????
      * ???????????????????????????????????????????????????????????????
        ??????????????????????????????????????????????????????????????????*/
        HuntingMatchAIRecordChooseMode result = HuntingMatchAIRecordChooseMode.Unknown;
        switch (aiRecordChooseMode) {
            case HighLevelWholeMatch:
                result = HuntingMatchAIRecordChooseMode.LoseProneRound;
                break;
            case MediumLevelWholeMatch:
                result = HuntingMatchAIRecordChooseMode.drawProneRound;
                break;
            case LowLevelWholeMatch:
                result = HuntingMatchAIRecordChooseMode.WinProneRound;
                break;
        }
        if (result.equals(HuntingMatchAIRecordChooseMode.Unknown)) {
            throw new BusinessException("?????????" + aiRecordChooseMode + "?????????????????????????????????");
        }
        return result;
    }

    @Override
    public List<WeaponInfoAvailableStatusData> filterWeaponInfoAvailableForAllRoutes(List<PlayerControlRecordDistributionData> distributionDataArray, Integer requiresRoutesCount) {

        Map<String, WeaponInfoAvailableStatusData> weaponInfoAvailableStatusData = new LinkedHashMap<>();
        for (PlayerControlRecordDistributionData distributionData : distributionDataArray) {

            String key = distributionData.getGunId() + "_" + distributionData.getGunLevel() + "_" + distributionData.getBulletId();
            WeaponInfoAvailableStatusData availableData = null;
            if (!weaponInfoAvailableStatusData.containsKey(key)) {

                PlayerWeaponInfo weaponInfo = new PlayerWeaponInfo(distributionData.getGunId(), distributionData.getGunLevel(), distributionData.getBulletId());
                availableData = new WeaponInfoAvailableStatusData(weaponInfo, new ArrayList<>(), 0, 0);
                availableData.setControlRecordDataCount(availableData.getControlRecordDataCount() + distributionData.getTotalRecordDataCount());
                int tempCount = availableData.getAvailableDistributionCount() + (distributionData.getAvailable() ? 1 : 0);
                availableData.setAvailableDistributionCount(tempCount);
                weaponInfoAvailableStatusData.put(key, availableData);
            } else {
                availableData = weaponInfoAvailableStatusData.get(key);
            }

            if (!availableData.getRoutesIds().contains(distributionData.getRouteUid())) {

                availableData.getRoutesIds().add(distributionData.getRouteUid());
            }

        }
        List<WeaponInfoAvailableStatusData> results = new ArrayList<>();
        weaponInfoAvailableStatusData.forEach((key, value) -> {
            if (value.getRoutesIds().size() >= requiresRoutesCount) {
                results.add(value);
            }
        });

        return results;
    }

    @Override
    public WeaponInfoAvailableStatusData sortWeaponInfoAvailableStatusData(List<WeaponInfoAvailableStatusData> weaponInfoAvailableStatusData) {

        weaponInfoAvailableStatusData.sort((dataA, dataB) -> {

            if (!dataA.getAvailableDistributionCount().equals(dataB.getAvailableDistributionCount())) {

                return dataB.getAvailableDistributionCount() - dataA.getAvailableDistributionCount();
            }
            return dataB.getControlRecordDataCount() - dataA.getControlRecordDataCount();
        });

        return (WeaponInfoAvailableStatusData) NumberUtils.randomElementInArray(weaponInfoAvailableStatusData);
    }

    /**
     * ??????Ai??????????????????
     *
     * @param filterParameters
     * @param gameVersion
     * @return
     */
    @Override
    public RecordDataAndBase64 loadAiControlRecordData(MatchAiRoundRecordFilterParameters filterParameters, String gameVersion) {

        MatchAiRoundControlQuery matchAiControlQuery = filterParameters.getOriginQuery();

        Map<String, MatchTableValue> matchTable = GameEnvironment.matchTableMap.get(gameVersion);

        MatchTableValue matchTableValue = matchTable.get(String.valueOf(matchAiControlQuery.getMatchId()));

        PlayerWeaponInfo weaponInfo = filterParameters.getOriginQuery().getAiWeaponInfo();

        //??????????????????????????????
        String singlePath = Path.getMatchSingleRoundControlRecordsPoolCollectionPath(
                gameVersion,
                filterParameters.getAnimalId(),
                matchAiControlQuery.getRouteUid(),
                weaponInfo.getGunId(),
                weaponInfo.getGunLevel(),
                weaponInfo.getBulletId(),
                //1.0.10??????????????????????????????
                matchTableValue.getWindId(),
                matchAiControlQuery.getPlayerAveragePrecision());
//        String path = Path.getMatchRoundControlRecordsPoolCollectionPath(
//                gameVersion,
//                filterParameters.getAnimalId(),
//                matchAiControlQuery.getRouteUid(),
//                weaponInfo.getGunId(),
//                weaponInfo.getGunLevel(),
//                weaponInfo.getBulletId(),
//                //1.0.10??????????????????????????????
//                matchTableValue.getWindId(),
//                matchAiControlQuery.getPlayerAveragePrecision());

        Map<PlayerControlRecordData, String> information = new HashMap<>();
        //????????????????????????????????????
        List<PlayerControlRecordData> filterPlayerControlRecordDocData = new ArrayList<>();

        //??????????????????????????????????????????
        List<Object> list = RedisDBOperation.selectSingleRoundControlRecords(singlePath);
//        List<Object> list = RedisDBOperation.selectMatchRoundControlRecords(path);
        List<Object> tempList = new ArrayList<>();
        //?????????????????????
        Integer count = NumberUtils.randomInt(50.0, 100.0);
        if (list != null && list.size() != 0) {
            List<String> zipStringList = new ArrayList<>();
            if (list != null && list.size() != 0) {
                tempList.addAll(list);
                Collections.shuffle(tempList);
//                List<Object> subList = tempList.subList(0, Math.min(list.size(),count));
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
//            tempList.addAll(list);
//            Collections.shuffle(tempList);
//            List<Object> subList = tempList.subList(0, Math.min(list.size(),count));
//            Iterator<Object> iterator = subList.iterator();
//            while (iterator.hasNext()){
//                try {
//                    SingleRecordIndex single = (SingleRecordIndex)iterator.next();
//                    candidateList.add(single);
//                }catch (Exception e){
//                    log.warn("SingleRecordIndex warn"+iterator.next());
//                    iterator.remove();
//                }
//
//            }
//            List<String> wholeKeyList = candidateList.stream().map(SingleRecordIndex::getWholeKey).collect(Collectors.toList());
//            List<Object> wholeKeyTempList = RedisDBOperation.MultipleGet(wholeKeyList);
//            List<String> zipList = new ArrayList<>();
//            Collections.addAll(zipList,wholeKeyTempList.toArray(new String[0]));
//
//            for (int i = 0; i < candidateList.size(); i++) {
//                String zipRecord = zipList.get(i);
//                SingleRecordIndex recordIndex = candidateList.get(i);
//                if (zipRecord==null) {
//                    log.warn("????????????" + recordIndex.getWholeKey() + "??????");
//                    continue;
//                }
//                String unzipString = null;
//                PlayerUploadWholeMatchControlRecordData recordData = null;
//                try {
//                    unzipString = DeflaterUtils.unzipString(zipRecord);
//                    recordData = JSONObject.parseObject(unzipString, PlayerUploadWholeMatchControlRecordData.class);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                //?????????
//                String base64Data = recordData.getEncodedBytes_Base64().get(recordIndex.getRound());
//
//                String temp = playerControlRecordDataService.decodeControlRecordData(base64Data);
//                PlayerControlRecordData docData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
//                information.put(docData,base64Data);
//                filterPlayerControlRecordDocData.add(docData);
//            }
        }


        //?????????????????????????????????????????????????????????
//        if(list==null||list.size()==0){
//            String path = Path.getMatchRoundControlRecordsPoolCollectionPath(
//                    gameVersion,
//                    filterParameters.getAnimalId(),
//                    matchAiControlQuery.getRouteUid(),
//                    weaponInfo.getGunId(),
//                    weaponInfo.getGunLevel(),
//                    weaponInfo.getBulletId(),
//                    //1.0.10??????????????????????????????
//                    matchTableValue.getWindId(),
//                    matchAiControlQuery.getPlayerAveragePrecision());
//            List<Object> singleLists = RedisDBOperation.selectMatchRoundControlRecords(path);
////            List<Object> singleLists = RedisDBOperation.selectSingleRoundControlRecords(singlePath);
//            List<Object> tempSingleList= new ArrayList<>();
//            List<String> zipStringList = new ArrayList<>();
//            if (singleLists!=null&&singleLists.size()!=0){
//                tempSingleList.addAll(singleLists);
//                Collections.shuffle(tempSingleList);
////                List<Object> subList = tempSingleList.subList(0, Math.min(singleLists.size(),count));
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

            findDocs = searchRecordsWithScoreAndPrecisionInDatabase(filterPlayerControlRecordDocData, filterParameters.getAiScoreRange(), filterParameters.getAiShowPrecisionRange(), filterParameters.getOriginQuery().getPlayerUid());
            //??????????????????????????????????????????,????????????????????????
            if (findDocs == null || findDocs.size() == 0) {
                log.info("????????????????????????????????????,????????????????????????");
                findDocs = searchRecordsWithScoreAndPrecisionInDatabase(filterPlayerControlRecordDocData, filterParameters.getSecureAIScoreRange(), filterParameters.getSecureAIShowPrecisionRange(), filterParameters.getOriginQuery().getPlayerUid());

            }
        } else {

            findDocs = searchRecordsWithPrecisionInDatabase(filterPlayerControlRecordDocData, filterParameters.getAiShowPrecisionRange(), filterParameters.getOriginQuery().getPlayerUid());
            if (findDocs == null || findDocs.size() == 0) {
                log.info("????????????????????????????????????,????????????????????????");
                findDocs = searchRecordsWithPrecisionInDatabase(filterPlayerControlRecordDocData, filterParameters.getSecureAIShowPrecisionRange(), filterParameters.getOriginQuery().getPlayerUid());
            }
        }

        //??????????????????????????????,????????????????????????
        if (findDocs == null || findDocs.size() == 0) {

            log.error("????????????" + filterParameters + "?????????????????????????????????,????????????????????????");
            findDocs = searchRecordsWithPrecisionInDatabase(filterPlayerControlRecordDocData, new Double[]{0.0, 1.0}, filterParameters.getOriginQuery().getPlayerUid());
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

        return dataAndBase64;

    }

    @Override
    public List<PlayerControlRecordData> searchRecordsWithScoreAndPrecisionInDatabase(List<PlayerControlRecordData> list, Integer[] scoreRange, Double[] averageShowPrecisionRange, String userUid) {

//        list = list.stream().sorted(new Comparator<PlayerControlRecordData>() {
//                                 @Override
//                                 public int compare(PlayerControlRecordData o1, PlayerControlRecordData o2) {
//                                     //?????????finalScore?????????finalScore???????????????averageShowPrecision??????
//                                     int i = o2.getFinalScore().compareTo(o1.getFinalScore());
//                                     if (i==0) if (o1.getAverageShowPrecision() < o2.getAverageShowPrecision())
//                                         return 1;
//                                     return i;
//                                 }
//                             }).collect(Collectors.toList());

        //????????????????????????
        int eachQueryDocCount = (new Random().nextInt(5) + 1) * 50;

        List<PlayerControlRecordData> finalList = new ArrayList<>();
        int count = 0;
        while (count < list.size()) {

            //??????????????????,???????????????,??????0.71,?????????firestore???,????????????0.7099999785423279,????????????>=0.71???false
            //??????????????????????????????0.01,
            //?????????????????????,?????????0.01,??????0.71?????????0.71000000112,???????????? <= 0.71???false

            for (PlayerControlRecordData data : list) {
                if (data.getPlayerUid().equals(userUid)) {
                    continue;
                }
                if (data.getFinalScore() > scoreRange[0] && data.getFinalScore() < scoreRange[1]) {
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
            log.info("?????????????????????????????????{}????????????resultList", resultList.size());
            return resultList;
        }


        log.info("????????????????????????????????????????????????finalList");
        return finalList;
    }

    @Override
    public List<PlayerControlRecordData> searchRecordsWithPrecisionInDatabase(List<PlayerControlRecordData> filterList, Double[] averageShowPrecisionRange, String userUid) {


        filterList.sort(Comparator.comparingDouble(PlayerControlRecordData::getAverageShowPrecision));

        //????????????????????????
        int eachQueryDocCount = (new Random().nextInt(5) + 1) * 50;

        List<PlayerControlRecordData> foundRecordsDocData = new ArrayList<>();

        int count = 0;
        while (count < filterList.size()) {

            //??????????????????,???????????????,??????0.71,?????????firestore???,????????????0.7099999785423279,????????????>=0.71???false
            //??????????????????????????????0.01,
            //?????????????????????,?????????0.01,??????0.71?????????0.71000000112,???????????? <= 0.71???false
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

    @Override
    public PlayerUploadWholeMatchControlRecordData queryWholeMatchControlRecords(String userUid, HuntingMatchAIRecordChooseMode aiRecordChooseMode, Integer chapterId, Integer matchId, String gameVersion) {

        if (aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.WinProneRound) ||
                aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.LoseProneRound) ||
                aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.drawProneRound
                )) {
            throw new BusinessException("????????????ai record choose mode: " + aiRecordChooseMode);
        }

        UserData userData = GameEnvironment.userDataMap.get(userUid);

        int playerTrophy = userData.getTrophy();

        RangeInt trophySegmentRange = playerControlRecordDataService.getControlRecordTrophySegmentRange(chapterId, playerTrophy, gameVersion);

        //??????????????????key
        String metaDataDocRef = Path.getMatchControlRecordsPool(gameVersion, matchId, trophySegmentRange);

        String segmentCollectionRef = Path.getMatchControlRecordsPoolTrophySegmentMetaData(
                gameVersion,
                matchId,
                trophySegmentRange
        );
        //????????????????????????
        MatchControlRecordsPoolMetaData poolMetaData = playerControlRecordDataService.getPoolTrophySegmentMetaData(metaDataDocRef, segmentCollectionRef);

        if (poolMetaData == null) {
            log.error(segmentCollectionRef + "??????????????????");
            return null;
        }

        //???????????????????????????????????????????????????????????????????????????
        if (systemPropertiesConfig.getProduction() && !poolMetaData.getIsUsable()) {
            log.info("?????????" + metaDataDocRef + "????????????");
            return null;
        }

        int playerMedianScore = huntingMatchService.getPlayerChapterLatestMatchMedianScore(chapterId, userUid, gameVersion);

        //??????????????????????????????????????????
        double playerMedianScoreRankPercent = (playerMedianScore - poolMetaData.getLowestFinalScore()) * 1.0 / (poolMetaData.getHighestFinalScore() - poolMetaData.getLowestFinalScore());

        playerMedianScoreRankPercent = Math.min(Math.max(playerMedianScoreRankPercent, 0.0), 1.0);

        /**
         * 3??????????????? ???
         3.1?????????AI??????AI??????????????????????????????
         3.2???????????????????????????????????????20%-80%???AI???????????????
         3.3???????????????????????????
         1??????????????????????????????20%?????????????????????20%????????????
         2?????????????????????????????????20%????????????????????????????????????
         3.4????????????????????????
         1??????????????????????????????20%?????????????????????20%?????????
         2?????????????????????????????????20%????????????????????????????????????
         */
//        Integer[] targetScoreRange = new Integer[]{poolMetaData.getLowestFinalScore(),poolMetaData.getLowestFinalScore()};
//
//        switch (aiRecordChooseMode){
//
//            case LowLevelWholeMatch:
//                if (playerMedianScoreRankPercent <=0.2){
//                    targetScoreRange[1]=NumberUtils.lerp(poolMetaData.getLowestFinalScore()*1.0,poolMetaData.getHighestFinalScore()*1.0,playerMedianScoreRankPercent);
//                }else {
//                    targetScoreRange[1]= playerMedianScore;
//                }
//                break;
//            case MediumLevelWholeMatch:
//                targetScoreRange[0] = NumberUtils.lerp(poolMetaData.getLowestFinalScore()*1.0,poolMetaData.getHighestFinalScore()*1.0,0.2);
//                targetScoreRange[1] = NumberUtils.lerp(poolMetaData.getLowestFinalScore()*1.0,poolMetaData.getHighestFinalScore()*1.0,0.8);
//                break;
//            case HighLevelWholeMatch:
//                if (playerMedianScoreRankPercent>0.8){
//                    targetScoreRange[0] = NumberUtils.lerp(poolMetaData.getLowestFinalScore()*1.0,poolMetaData.getHighestFinalScore()*1.0,0.8);
//                }else {
//                    targetScoreRange[0] = playerMedianScore;
//                }
//                break;
//        }
        Integer[] targetScoreRange = new Integer[]{poolMetaData.getLowestFinalScore(), poolMetaData.getHighestFinalScore()};//????????????????????????????????????????????????getLowestFinalScore()??????
        //???????????????????????????????????????????????????????????????????????????
//        Arrays.sort(targetScoreRange);

        //??????????????????????????????,???????????????
        //?????????????????????????????????
        List<Object> dataList = RedisDBOperation.selectMatchControlRecordsPoolTrophySegmentCollectionContent(segmentCollectionRef);
        if (dataList == null || dataList.size() == 0) {
            log.warn("?????????????????????????????????{}", segmentCollectionRef);
        }
//        List<MatchRecord> poolCollection = playerControlRecordDataService.getPoolCollection(segmentCollectionRef);
        List<MatchRecord> poolCollection = new ArrayList<>();
        Collections.addAll(poolCollection, dataList.toArray(new MatchRecord[0]));
        poolCollection.sort((o1, o2) -> o2.getScore() - o1.getScore());

        //????????????????????????
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
            log.warn("????????????????????????????????????");
            return null;
        }

        log.info("???????????????????????????{}", aiRecordChooseMode);
        log.info("??????????????????????????????????????????" + Arrays.toString(targetScoreRange) + ",collection path " + metaDataDocRef);

        List<MatchRecord> foundWholeMatchRecordsDataArray = new ArrayList<>();

        //?????????????????????????????????
        List<String> usedWholeMatchControlRecordsUids = playerControlRecordDataService.getPlayerUsedWholeMatchControlRecordsUids(userUid, chapterId, gameVersion);
        List<PlayerUploadWholeMatchControlRecordData> candidateList = null;
        int i = 0;
        int min = 1;
        int randomIndex = 0;
        while (++i <= usedWholeMatchControlRecordsUids.size() + 5) {
            int max = Math.min(200, conditionList.size());
            //??????????????????????????????????????????
            if (max != min) {
                randomIndex = RandomUtil.randomInt(min, max);
            }
            //?????????????????????????????????????????????
            foundWholeMatchRecordsDataArray.add(conditionList.get(randomIndex));
            //??????MatchRecord???????????????????????????????????????????????????
            candidateList = playerControlRecordDataService.getPlayerUploadWholeMatchControlRecordDataCollection(foundWholeMatchRecordsDataArray, gameVersion);
            PlayerUploadWholeMatchControlRecordData recordData = candidateList.get(0);
            if (usedWholeMatchControlRecordsUids.contains(recordData.getUid()) || recordData.getPlayerUid().equals(userUid)) {
                continue;
            }

            //????????????
            playerControlRecordDataService.savePlayerUsedWholeMatchControlRecordsUids(userUid, chapterId, recordData.getUid(), gameVersion);
            recordData.setSafePlayerTrophyCount(trophySegmentRange.clampInRange(recordData.getPlayerTrophyCount()));

            return recordData;

        }


//        List<MatchRecord> poolCollection = playerControlRecordDataService.getPoolCollection(segmentCollectionRef);
        //??????????????????
//        Integer count = NumberUtils.randomInt(50.0, 100.0);
//
//
//        if (poolCollection!=null&&poolCollection.size()!=0){
//
//            for (MatchRecord data : poolCollection) {
//
//                //????????????????????????
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
//            log.info("??????????????????????????????,??????"+count+"???, ??????????????? "+ Arrays.toString(targetScoreRange) +",collection path "+metaDataDocRef);
//        }
//
//        //??????????????????????????????
//        List<String> usedWholeMatchControlRecordsUids = playerControlRecordDataService.getPlayerUsedWholeMatchControlRecordsUids(userData.getUuid(),chapterId,gameVersion);
//
//        //??????MatchRecord???????????????????????????????????????????????????
//        List<PlayerUploadWholeMatchControlRecordData> candidateList = playerControlRecordDataService.getPlayerUploadWholeMatchControlRecordDataCollection(foundWholeMatchRecordsDataArray,gameVersion);
//
//        NumberUtils.shuffleArray(candidateList,0);
//
//        //????????????????????????????????????
//        for (PlayerUploadWholeMatchControlRecordData recordData : candidateList) {
//
//            if (usedWholeMatchControlRecordsUids.contains(recordData.getUid())||recordData.getPlayerUid().equals(userUid)){
//                continue;
//            }
//
//            //????????????
//            playerControlRecordDataService.savePlayerUsedWholeMatchControlRecordsUids(userData.getUuid(),chapterId,recordData.getUid(),gameVersion);
//            recordData.setSafePlayerTrophyCount(trophySegmentRange.clampInRange(recordData.getPlayerTrophyCount()));
//            return recordData;
//        }

        return null;
    }

    @Override
    public PlayerUploadWholeMatchControlRecordData testQueryWholeMatchControlRecords(String userUid, Integer chapterId, Integer matchId, String gameVersion, RangeInt trophySegmentRange) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);


        //??????????????????key
        String metaDataDocRef = Path.getMatchControlRecordsPool(gameVersion, matchId, trophySegmentRange);

        String segmentCollectionRef = Path.getMatchControlRecordsPoolTrophySegmentMetaData(
                gameVersion,
                matchId,
                trophySegmentRange
        );
        //????????????????????????
        MatchControlRecordsPoolMetaData poolMetaData = playerControlRecordDataService.getPoolTrophySegmentMetaData(metaDataDocRef, segmentCollectionRef);

        if (poolMetaData == null) {
            log.error("???????????????" + segmentCollectionRef);
            return null;
        }

        //???????????????????????????????????????????????????????????????????????????
        if (systemPropertiesConfig.getProduction() && !poolMetaData.getIsUsable()) {
            log.info("?????????" + metaDataDocRef + "????????????");
            return null;
        }

        int playerMedianScore = huntingMatchService.getPlayerChapterLatestMatchMedianScore(chapterId, userUid, gameVersion);

        //??????????????????????????????????????????
        double playerMedianScoreRankPercent = (playerMedianScore - poolMetaData.getLowestFinalScore()) * 1.0 / (poolMetaData.getHighestFinalScore() - poolMetaData.getLowestFinalScore());

        playerMedianScoreRankPercent = Math.min(Math.max(playerMedianScoreRankPercent, 0.0), 1.0);


        Integer[] targetScoreRange = new Integer[]{poolMetaData.getLowestFinalScore(), poolMetaData.getLowestFinalScore()};


        log.info("??????????????????????????????????????????" + Arrays.toString(targetScoreRange) + ",collection path " + metaDataDocRef);

        List<MatchRecord> foundWholeMatchRecordsDataArray = new ArrayList<>();
        int cursorStartValue = targetScoreRange[0];
        int cursorEndValue = targetScoreRange[1];

        List<MatchRecord> poolCollection = playerControlRecordDataService.getPoolCollection(segmentCollectionRef);
        //??????????????????
        Integer count = NumberUtils.randomInt(50.0, 100.0);
        if (poolCollection != null && poolCollection.size() != 0) {

            for (MatchRecord data : poolCollection) {

                //????????????????????????
//                if (data.getScore()>=cursorStartValue&&data.getScore()<=cursorEndValue){
                foundWholeMatchRecordsDataArray.add(data);
//                }
                if (foundWholeMatchRecordsDataArray.size() > count)
                    break;

            }
        }

        if (foundWholeMatchRecordsDataArray.size() == 0) {
            return null;
        }

        if (foundWholeMatchRecordsDataArray.size() < count) {
            log.info("??????????????????????????????,??????" + count + "???, ??????????????? " + Arrays.toString(targetScoreRange) + ",collection path " + metaDataDocRef);
        }

        //??????????????????????????????
        List<String> usedWholeMatchControlRecordsUids = playerControlRecordDataService.getPlayerUsedWholeMatchControlRecordsUids(userData.getUuid(), chapterId, gameVersion);

        //??????MatchRecord???????????????????????????????????????????????????
        List<PlayerUploadWholeMatchControlRecordData> candidateList = playerControlRecordDataService.getPlayerUploadWholeMatchControlRecordDataCollection(foundWholeMatchRecordsDataArray, gameVersion);

        NumberUtils.shuffleArray(candidateList, 0);

        //????????????????????????????????????
        for (PlayerUploadWholeMatchControlRecordData recordData : candidateList) {

            if (usedWholeMatchControlRecordsUids.contains(recordData.getUid()) || recordData.getPlayerUid().equals(userUid)) {
                continue;
            }

            //????????????
            playerControlRecordDataService.savePlayerUsedWholeMatchControlRecordsUids(userData.getUuid(), chapterId, recordData.getUid(), gameVersion);
            return recordData;
        }

        return null;
    }

    @Override
    public PlayerWeaponInfo generateAiWeaponInfoByWholeMatchRecordsData(PlayerUploadWholeMatchControlRecordData wholeMatchRecordsData) {

        return new PlayerWeaponInfo(wholeMatchRecordsData.getPlayerGunId(), wholeMatchRecordsData.getPlayerGunLevel(), wholeMatchRecordsData.getPlayerBulletId());
    }

    @Override
    public PlayerWeaponInfo generateAiWeaponInfoByMatchAiRoundRule(MatchAIRoundRuleTableValue matchAiRoundRuleTableValue, String gameVersion) {

        List<Integer> weaponCombinationIdArray = matchAiRoundRuleTableValue.getAiWeaponCombinationId();
        Integer randomId = (Integer) NumberUtils.randomElementInArray(weaponCombinationIdArray);

        if (randomId == null) {
            throw new BusinessException("MatchAiRoundRule????????????aiWeaponCombinationId??????????????????" + matchAiRoundRuleTableValue);
        }
        Map<String, AiWeaponCombinationTableValue> aiWeaponCombinationTable = GameEnvironment.aiWeaponCombinationTableMap.get(gameVersion);

        if (!aiWeaponCombinationTable.containsKey(String.valueOf(randomId))) {
            throw new BusinessException("AiWeaponCombinationTable????????????????????????id" + randomId);
        }
        AiWeaponCombinationTableValue tableValue = aiWeaponCombinationTable.get(String.valueOf(randomId));

        return new PlayerWeaponInfo(tableValue.getGunId(), tableValue.getGunLevel(), tableValue.getBulletId());
    }


    @Override
    public MatchAiRoundRecordFilterParameters generateMatchAiRoundControlRecordFilterParameters(MatchAiRoundControlQuery aiQuery, String gameVersion, ABTestGroup abTestGroup) {

        Map<String, MatchAIRoundRuleTableValue> matchAiRuleTable = GameEnvironment.matchAIRoundRuleTableMap.get(gameVersion);
        Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(gameVersion);

        ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(aiQuery.getChapterId()));
        MatchAIRoundRuleTableValue matchAiRuleTableValue = matchAiRuleTable.get(String.valueOf(aiQuery.getMatchAiRoundRuleTableId()));

        int animalSequenceIndex = aiQuery.getRound() - 1;
        if (animalSequenceIndex >= chapterTableValue.getMatchRouteAnimalSequence().size()) {

            return new MatchAiRoundRecordFilterParameters(null, null, null, null, null, null, "Round exceed");
        }

        int animalId = chapterTableValue.getMatchRouteAnimalSequence().get(animalSequenceIndex);

        //ruleIndex???index
        int ruleIndexIndex = aiQuery.getRound() - 1;
        if (ruleIndexIndex < 0) {
            throw new BusinessException("??????AI?????? filter???" + aiQuery + "round - 1 < 0");
        }

        ruleIndexIndex %= matchAiRuleTableValue.getRuleIndex().size();

        int ruleIndex = matchAiRuleTableValue.getRuleIndex().get(ruleIndexIndex);

        //????????????????????????
        Boolean isPlayerFirst = matchAiRuleTableValue.getIsPlayerFirst();
        if (isPlayerFirst) {

            //??????????????????????????????
            Map<String, LocalPlayerFirstAiRecordChooseRule> playerFirstAiRecordChooseRuleTable = aiRecordChooseRuleService.loadPlayerFirstAiRecordChooseRule(gameVersion, abTestGroup);
            LocalPlayerFirstAiRecordChooseRule playerFirstAiRecordChooseRule = null;

            if (!playerFirstAiRecordChooseRuleTable.containsKey(String.valueOf(ruleIndex))) {
                throw new BusinessException("??????????????????ai record choose rule,??????rule index " + ruleIndex + ",table:" + JSONObject.toJSONString(playerFirstAiRecordChooseRuleTable));
            } else {
                playerFirstAiRecordChooseRule = playerFirstAiRecordChooseRuleTable.get(String.valueOf(ruleIndex));
            }

            LocalPlayerCalculateAIFilterParametersBO filterParameters = aiRecordChooseRuleService.localPlayerCalculateAIFilterParameters(playerFirstAiRecordChooseRule, aiQuery.getPlayerFinalScore(), aiQuery.getPlayerAveragePrecision());

            if (filterParameters != null) {

                log.info("??????????????????AI?????? filter: " + aiQuery + "??????????????????" + ruleIndex + ", AI????????????" + filterParameters);

                return new MatchAiRoundRecordFilterParameters(
                        animalId,
                        aiQuery,
                        filterParameters.getAiScoreRange(),
                        filterParameters.getAiShowPrecisionRange(),
                        filterParameters.getSecureAIScoreRange(),
                        filterParameters.getSecureAIShowPrecisionRange(),
                        null
                );
            } else {

                throw new BusinessException("??????AI?????? filter: " + aiQuery + "??????????????????" + ruleIndex + ", ??????????????????FilterParameters");
            }

        } else {
            //??????ai??????????????????
            Map<String, AiFirstAiRecordChooseRule> aiFirstAiRecordChooseRuleTable = aiRecordChooseRuleService.loadAiFirstAiRecordChooseRule(gameVersion, abTestGroup);
            AiFirstAiRecordChooseRule aiFirstAiRecordChooseRule = null;

            if (!aiFirstAiRecordChooseRuleTable.containsKey(String.valueOf(ruleIndex))) {

                throw new BusinessException("??????ai?????? ai record choose rule,??????rule index" + ruleIndex);

            } else {

                aiFirstAiRecordChooseRule = aiFirstAiRecordChooseRuleTable.get(String.valueOf(ruleIndex));

            }

            CalculateAIFilterParametersBO filterParameters = aiRecordChooseRuleService.calculateAIFilterParameters(aiFirstAiRecordChooseRule);
            log.info("???????????????????????? filter: " + aiQuery + "??????????????????" + ruleIndex + ",AI????????????" + filterParameters);
            aiQuery.setPlayerAveragePrecision(RandomUtil.randomDouble(filterParameters.getAiShowPrecisionRange()[0], filterParameters.getAiShowPrecisionRange()[1]));
            return new MatchAiRoundRecordFilterParameters(
                    animalId,
                    aiQuery,
                    null,
                    filterParameters.getAiShowPrecisionRange(),
                    null,
                    filterParameters.getSecureAIShowPrecisionRange(),
                    null
            );
        }

    }
}


