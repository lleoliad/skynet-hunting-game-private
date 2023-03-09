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
      * 如果是高水平完整匹配，则改为倾向失败的回合匹配
      * 如果是普通完整匹配，改为倾向普通的回合匹配
        如果是保底完整匹配，则改为倾向胜利的回合匹配*/
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
            throw new BusinessException("无法将" + aiRecordChooseMode + "转为对应的回合匹配模式");
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
     * 加载Ai控制记录数据
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

        //获得单回合的录像信息
        String singlePath = Path.getMatchSingleRoundControlRecordsPoolCollectionPath(
                gameVersion,
                filterParameters.getAnimalId(),
                matchAiControlQuery.getRouteUid(),
                weaponInfo.getGunId(),
                weaponInfo.getGunLevel(),
                weaponInfo.getBulletId(),
                //1.0.10版本之后增加风力匹配
                matchTableValue.getWindId(),
                matchAiControlQuery.getPlayerAveragePrecision());
//        String path = Path.getMatchRoundControlRecordsPoolCollectionPath(
//                gameVersion,
//                filterParameters.getAnimalId(),
//                matchAiControlQuery.getRouteUid(),
//                weaponInfo.getGunId(),
//                weaponInfo.getGunLevel(),
//                weaponInfo.getBulletId(),
//                //1.0.10版本之后增加风力匹配
//                matchTableValue.getWindId(),
//                matchAiControlQuery.getPlayerAveragePrecision());

        Map<PlayerControlRecordData, String> information = new HashMap<>();
        //最终拿去计算的单回合数据
        List<PlayerControlRecordData> filterPlayerControlRecordDocData = new ArrayList<>();

        //从整回录像中中获取单回合录像
        List<Object> list = RedisDBOperation.selectSingleRoundControlRecords(singlePath);
//        List<Object> list = RedisDBOperation.selectMatchRoundControlRecords(path);
        List<Object> tempList = new ArrayList<>();
        //每次选取的数目
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
//                    log.warn("录像文件" + recordIndex.getWholeKey() + "丢失");
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
//                //第几局
//                String base64Data = recordData.getEncodedBytes_Base64().get(recordIndex.getRound());
//
//                String temp = playerControlRecordDataService.decodeControlRecordData(base64Data);
//                PlayerControlRecordData docData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
//                information.put(docData,base64Data);
//                filterPlayerControlRecordDocData.add(docData);
//            }
        }


        //整回合中没有数据，从单回合里面寻找数据
//        if(list==null||list.size()==0){
//            String path = Path.getMatchRoundControlRecordsPoolCollectionPath(
//                    gameVersion,
//                    filterParameters.getAnimalId(),
//                    matchAiControlQuery.getRouteUid(),
//                    weaponInfo.getGunId(),
//                    weaponInfo.getGunLevel(),
//                    weaponInfo.getBulletId(),
//                    //1.0.10版本之后增加风力匹配
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
            //如果正常范围没有找到任何结果,尝试使用安全范围
            if (findDocs == null || findDocs.size() == 0) {
                log.info("正常范围没有找到任何结果,尝试使用安全范围");
                findDocs = searchRecordsWithScoreAndPrecisionInDatabase(filterPlayerControlRecordDocData, filterParameters.getSecureAIScoreRange(), filterParameters.getSecureAIShowPrecisionRange(), filterParameters.getOriginQuery().getPlayerUid());

            }
        } else {

            findDocs = searchRecordsWithPrecisionInDatabase(filterPlayerControlRecordDocData, filterParameters.getAiShowPrecisionRange(), filterParameters.getOriginQuery().getPlayerUid());
            if (findDocs == null || findDocs.size() == 0) {
                log.info("正常范围没有找到任何结果,尝试使用安全范围");
                findDocs = searchRecordsWithPrecisionInDatabase(filterPlayerControlRecordDocData, filterParameters.getSecureAIShowPrecisionRange(), filterParameters.getOriginQuery().getPlayerUid());
            }
        }

        //如果安全范围都找不到,从任意精度里面找
        if (findDocs == null || findDocs.size() == 0) {

            log.error("过滤参数" + filterParameters + "无法找到合适的录制文件,放开所有条件查找");
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
//                                     //先按照finalScore排序，finalScore相等再按照averageShowPrecision排序
//                                     int i = o2.getFinalScore().compareTo(o1.getFinalScore());
//                                     if (i==0) if (o1.getAverageShowPrecision() < o2.getAverageShowPrecision())
//                                         return 1;
//                                     return i;
//                                 }
//                             }).collect(Collectors.toList());

        //每次查询文档个数
        int eachQueryDocCount = (new Random().nextInt(5) + 1) * 50;

        List<PlayerControlRecordData> finalList = new ArrayList<>();
        int count = 0;
        while (count < list.size()) {

            //上传的浮点数,有精度问题,例如0.71,存储到firestore后,可能变为0.7099999785423279,这时候它>=0.71是false
            //所以比较的下限要减去0.01,
            //比较上限的时候,要加上0.01,因为0.71可能是0.71000000112,这时候它 <= 0.71是false

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
            log.info("符合命中条件的录像共有{}条，返回resultList", resultList.size());
            return resultList;
        }


        log.info("找不到符合命中率条件的录像，返回finalList");
        return finalList;
    }

    @Override
    public List<PlayerControlRecordData> searchRecordsWithPrecisionInDatabase(List<PlayerControlRecordData> filterList, Double[] averageShowPrecisionRange, String userUid) {


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

    @Override
    public PlayerUploadWholeMatchControlRecordData queryWholeMatchControlRecords(String userUid, HuntingMatchAIRecordChooseMode aiRecordChooseMode, Integer chapterId, Integer matchId, String gameVersion) {

        if (aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.WinProneRound) ||
                aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.LoseProneRound) ||
                aiRecordChooseMode.equals(HuntingMatchAIRecordChooseMode.drawProneRound
                )) {
            throw new BusinessException("不支持的ai record choose mode: " + aiRecordChooseMode);
        }

        UserData userData = GameEnvironment.userDataMap.get(userUid);

        int playerTrophy = userData.getTrophy();

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
        if (systemPropertiesConfig.getProduction() && !poolMetaData.getIsUsable()) {
            log.info("操作池" + metaDataDocRef + "还不可用");
            return null;
        }

        int playerMedianScore = huntingMatchService.getPlayerChapterLatestMatchMedianScore(chapterId, userUid, gameVersion);

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

        List<MatchRecord> foundWholeMatchRecordsDataArray = new ArrayList<>();

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

            return recordData;

        }


//        List<MatchRecord> poolCollection = playerControlRecordDataService.getPoolCollection(segmentCollectionRef);
        //这次找的数目
//        Integer count = NumberUtils.randomInt(50.0, 100.0);
//
//
//        if (poolCollection!=null&&poolCollection.size()!=0){
//
//            for (MatchRecord data : poolCollection) {
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
//
//        //玩家最近匹配到的对局
//        List<String> usedWholeMatchControlRecordsUids = playerControlRecordDataService.getPlayerUsedWholeMatchControlRecordsUids(userData.getUuid(),chapterId,gameVersion);
//
//        //根据MatchRecord中录像的索引找出所有符合条件的录像
//        List<PlayerUploadWholeMatchControlRecordData> candidateList = playerControlRecordDataService.getPlayerUploadWholeMatchControlRecordDataCollection(foundWholeMatchRecordsDataArray,gameVersion);
//
//        NumberUtils.shuffleArray(candidateList,0);
//
//        //排除自己和最近匹配到对局
//        for (PlayerUploadWholeMatchControlRecordData recordData : candidateList) {
//
//            if (usedWholeMatchControlRecordsUids.contains(recordData.getUid())||recordData.getPlayerUid().equals(userUid)){
//                continue;
//            }
//
//            //重新保存
//            playerControlRecordDataService.savePlayerUsedWholeMatchControlRecordsUids(userData.getUuid(),chapterId,recordData.getUid(),gameVersion);
//            recordData.setSafePlayerTrophyCount(trophySegmentRange.clampInRange(recordData.getPlayerTrophyCount()));
//            return recordData;
//        }

        return null;
    }

    @Override
    public PlayerUploadWholeMatchControlRecordData testQueryWholeMatchControlRecords(String userUid, Integer chapterId, Integer matchId, String gameVersion, RangeInt trophySegmentRange) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);


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
            log.error("匹配池出错" + segmentCollectionRef);
            return null;
        }

        //正式服务器下，才检查池子是否够数量，不然没法测试了
        if (systemPropertiesConfig.getProduction() && !poolMetaData.getIsUsable()) {
            log.info("操作池" + metaDataDocRef + "还不可用");
            return null;
        }

        int playerMedianScore = huntingMatchService.getPlayerChapterLatestMatchMedianScore(chapterId, userUid, gameVersion);

        //计算玩家在该池中的中位数排名
        double playerMedianScoreRankPercent = (playerMedianScore - poolMetaData.getLowestFinalScore()) * 1.0 / (poolMetaData.getHighestFinalScore() - poolMetaData.getLowestFinalScore());

        playerMedianScoreRankPercent = Math.min(Math.max(playerMedianScoreRankPercent, 0.0), 1.0);


        Integer[] targetScoreRange = new Integer[]{poolMetaData.getLowestFinalScore(), poolMetaData.getLowestFinalScore()};


        log.info("玩家整局比赛匹配，目标分数：" + Arrays.toString(targetScoreRange) + ",collection path " + metaDataDocRef);

        List<MatchRecord> foundWholeMatchRecordsDataArray = new ArrayList<>();
        int cursorStartValue = targetScoreRange[0];
        int cursorEndValue = targetScoreRange[1];

        List<MatchRecord> poolCollection = playerControlRecordDataService.getPoolCollection(segmentCollectionRef);
        //这次找的数目
        Integer count = NumberUtils.randomInt(50.0, 100.0);
        if (poolCollection != null && poolCollection.size() != 0) {

            for (MatchRecord data : poolCollection) {

                //找够了就直接退出
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
            log.info("池子里没有足够的数据,需要" + count + "个, 目标分数： " + Arrays.toString(targetScoreRange) + ",collection path " + metaDataDocRef);
        }

        //玩家最近匹配到的对局
        List<String> usedWholeMatchControlRecordsUids = playerControlRecordDataService.getPlayerUsedWholeMatchControlRecordsUids(userData.getUuid(), chapterId, gameVersion);

        //根据MatchRecord中录像的索引找出所有符合条件的录像
        List<PlayerUploadWholeMatchControlRecordData> candidateList = playerControlRecordDataService.getPlayerUploadWholeMatchControlRecordDataCollection(foundWholeMatchRecordsDataArray, gameVersion);

        NumberUtils.shuffleArray(candidateList, 0);

        //排除自己和最近匹配到对局
        for (PlayerUploadWholeMatchControlRecordData recordData : candidateList) {

            if (usedWholeMatchControlRecordsUids.contains(recordData.getUid()) || recordData.getPlayerUid().equals(userUid)) {
                continue;
            }

            //重新保存
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
            throw new BusinessException("MatchAiRoundRule表格中，aiWeaponCombinationId填写有问题。" + matchAiRoundRuleTableValue);
        }
        Map<String, AiWeaponCombinationTableValue> aiWeaponCombinationTable = GameEnvironment.aiWeaponCombinationTableMap.get(gameVersion);

        if (!aiWeaponCombinationTable.containsKey(String.valueOf(randomId))) {
            throw new BusinessException("AiWeaponCombinationTable中，没有武器组合id" + randomId);
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

        //ruleIndex的index
        int ruleIndexIndex = aiQuery.getRound() - 1;
        if (ruleIndexIndex < 0) {
            throw new BusinessException("过滤AI规则 filter：" + aiQuery + "round - 1 < 0");
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

            LocalPlayerCalculateAIFilterParametersBO filterParameters = aiRecordChooseRuleService.localPlayerCalculateAIFilterParameters(playerFirstAiRecordChooseRule, aiQuery.getPlayerFinalScore(), aiQuery.getPlayerAveragePrecision());

            if (filterParameters != null) {

                log.info("过滤玩家先手AI规则 filter: " + aiQuery + "找到规则条目" + ruleIndex + ", AI过滤规则" + filterParameters);

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

                throw new BusinessException("过滤AI规则 filter: " + aiQuery + "找到规则条目" + ruleIndex + ", 但是无法创建FilterParameters");
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
            log.info("过滤玩家先手规则 filter: " + aiQuery + "找到规则条目" + ruleIndex + ",AI过滤规则" + filterParameters);
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


