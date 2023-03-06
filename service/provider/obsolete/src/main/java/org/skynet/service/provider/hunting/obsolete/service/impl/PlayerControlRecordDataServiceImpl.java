package org.skynet.service.provider.hunting.obsolete.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.DeflaterUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.NanoIdUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.NumberUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.config.GameConfig;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.controller.admin.RecordIndex;
import org.skynet.service.provider.hunting.obsolete.controller.module.dto.BattleCompleteDto;
import org.skynet.service.provider.hunting.obsolete.controller.module.util.ThreadLocalForFight;
import org.skynet.service.provider.hunting.obsolete.enums.PlatformName;
import org.skynet.service.provider.hunting.obsolete.enums.PlayerControlRecordSource;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.BaseDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.ChapterTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.RecordModeMatchTableValue;
import org.skynet.service.provider.hunting.obsolete.service.HuntingMatchService;
import org.skynet.service.provider.hunting.obsolete.service.PlayerControlRecordDataService;
import org.skynet.service.provider.hunting.obsolete.service.RecordIndexService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.msgpack.MessagePack;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.msgpack.value.Value;
import org.msgpack.value.impl.ImmutableStringValueImpl;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class PlayerControlRecordDataServiceImpl implements PlayerControlRecordDataService {

    @Resource
    private PlayerControlRecordDataService playerControlRecordDataService;

    @Resource
    private HuntingMatchService huntingMatchService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Override
    public String decodeControlRecordData(String encodeData) {

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decode = decoder.decode(encodeData);
        String temp = null;
        MessagePack messagePack = new MessagePack();
        try {
            temp = messagePack.read(decode).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    @Override
    public String encodeControlRecordData(PlayerControlRecordData recordParsedData) {

        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
//        String encodeToString1 = Base64Utils.encodeToString(JSONUtil.toJsonStr(recordParsedData).getBytes());
//        return encodeToString1;
        Base64.Encoder encoder = Base64.getEncoder();
        String encodeToString = null;
        byte[] bytes = new byte[0];
        try {
            bytes = objectMapper.writeValueAsBytes(recordParsedData);
            encodeToString = encoder.encodeToString(bytes);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return encodeToString;

    }


    @Override
    public RangeInt getControlRecordTrophySegmentRange(Integer chapterId, Integer trophyCount, String gameVersion) {

        Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(gameVersion);
        ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(chapterId));

        List<RangeInt> trophySegmentRanges = getChapterRecordsTrophySegmentRanges(chapterTableValue);

        for (RangeInt range : trophySegmentRanges) {
            if (range.contain(trophyCount)) {
                return range;
            }
        }
        throw new BusinessException("无法找到奖杯数量" + trophyCount + "应该放到章节" + chapterId + "的哪一个录像池奖杯分段中");
    }

    @Override
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

    @Override
    public List<MatchRecord> getPoolCollection(String path) {

        //根据分数删选，将筛选出来的录像池信息中的录像信息放到队列中返回
        List<Object> list = RedisDBOperation.selectMatchControlRecordsPoolTrophySegmentCollectionContent(path);
        List<MatchRecord> collection = new LinkedList<>();
        Collections.addAll(collection, list.toArray(new MatchRecord[0]));

        return collection;
    }

    @Override
    public LinkedList<String> getPlayerUsedWholeMatchControlRecordsUids(String userUid, Integer chapterId, String gameVersion) {

        //检查一下这里
        String collectionRef = Path.getPlayerChapterLatestMatchIDsCollectionPath(gameVersion, userUid, chapterId);

//        String key = collectionRef+":"+"matchUids";
        List<Object> list = RedisDBOperation.selectPlayerChapterLatestMatchPlayerUidCollection(collectionRef);

        LinkedList<String> uidList = new LinkedList<>();
        Collections.addAll(uidList, list.toArray(new String[0]));
        return uidList;
    }


    @Override
    public void savePlayerUsedWholeMatchControlRecordsUids(String userUid, Integer chapterId, String matchUid, String gameVersion) {

        String collectionRef = Path.getPlayerChapterLatestMatchIDsCollectionPath(gameVersion, userUid, chapterId);

        RedisDBOperation.insertPlayerChapterLatestMatchPlayerUidCollection(collectionRef, matchUid);
    }

    @Override
    public void savePlayerUploadControlRecords(BaseDTO request, String userUid, HuntingMatchHistoryData historyData, List<String> allEncodedControlRecordsData, Integer averageFrameRate, String gameVersion) {

        //正式服务器，不允许编辑器上传的录像
        if (systemPropertiesConfig.getProduction() && request.getPlatform().equals(PlatformName.UnityEditor.getPlatform())) {
            return;
        }

        UserData userData = GameEnvironment.userDataMap.get(userUid);

        int playerTrophy = userData.getTrophy();
        PlayerRecordModeData recordModeData = userData.getServerOnly().getRecordModeData();
        if (recordModeData != null) {

            Map<String, RecordModeMatchTableValue> recordModeMatchTable = GameEnvironment.recordModeMatchTableMap.get(gameVersion);
            List<Integer> recordModeTrophyRange = recordModeMatchTable.get(String.valueOf(recordModeData.getRecordModeMatchTableId())).getPlayerTrophyRangeArray();
            playerTrophy = NumberUtils.randomInt(recordModeTrophyRange.get(0) * 1.0, recordModeTrophyRange.get(1) * 1.0 + 1);
        }

        HuntingMatchNowData huntingMatchNowData = historyData.getHuntingMatchNowDataArchive();
        PlayerWeaponInfo localPlayerWeaponInfo = huntingMatchNowData.getLocalPlayerWeaponInfo();
        String dataUid = NanoIdUtils.randomNanoId(30);

        PlayerUploadWholeMatchControlRecordData saveData = new PlayerUploadWholeMatchControlRecordData(
                dataUid,
                gameVersion,
                historyData.getMatchUid(),
                userUid,
                playerTrophy,
                null,
                historyData.getChapterId(),
                localPlayerWeaponInfo.getGunId(),
                localPlayerWeaponInfo.getGunLevel(),
                localPlayerWeaponInfo.getBulletId(),
                historyData.getMatchId(),
                historyData.getPlayerFinalScore(),
                TimeUtils.getUnixTimeSecond(),
                allEncodedControlRecordsData,
                averageFrameRate >= GameConfig.validRecordMinFrameRateRequires
        );

        downloadPlayerWholeData(huntingMatchNowData.getIsOpponentDisconnect(), gameVersion, saveData);

    }

    @Override
    public List<PlayerUploadWholeMatchControlRecordData> getPlayerUploadWholeMatchControlRecordDataCollection(List<MatchRecord> list, String gameVersion) {

        //将录像索引取出来
        List<String> indexList = new ArrayList<>();

        for (MatchRecord record : list) {
            String recordKey = record.getDataKey();
            indexList.add(recordKey);
        }
        //获取的是压缩文件
        List<Object> tempList = RedisDBOperation.MultipleGet(indexList);
        List<String> zipList = new ArrayList<>();
        List<PlayerUploadWholeMatchControlRecordData> collection = new LinkedList<>();

        Collections.addAll(zipList, tempList.toArray(new String[0]));
        //解压缩
        for (String zip : zipList) {
            try {
                String unzipString = DeflaterUtils.unzipString(zip);
                PlayerUploadWholeMatchControlRecordData recordData = JSONObject.parseObject(unzipString, PlayerUploadWholeMatchControlRecordData.class);
                collection.add(recordData);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return collection;
    }

    /**
     * 对录像进行存档，这里存的是PlayerUploadWholeMatchControlRecordData,是对比赛录像的第一次存档
     *
     * @param key
     * @param saveData
     */
    private void saveMatchControlRecords(String key, PlayerUploadWholeMatchControlRecordData saveData) {

        //先压缩再存
        String jsonString = JSONObject.toJSONString(saveData);
        String zipString = DeflaterUtils.zipString(jsonString);
        RedisDBOperation.insertMatchControlRecords(key, zipString);
    }

    @Override
    public Boolean checkControlRecordIsValid(String encodeData) {

        try {
            String temp = decodeControlRecordData(encodeData);
            PlayerControlRecordData controlRecordData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
            return !StringUtils.isEmpty(controlRecordData.getAllControlSegmentRecordsData());
        } catch (Exception e) {
            //解码错误 返回false
            return false;
        }

    }

    @Deprecated
    @Override
    public PlayerControlRecordDocData createPlayerControlRecordDocData(String dataBytesBase64Encoded) {
        String recordData = dataBytesBase64Encoded;
        try {
            byte[] decode = Base64.getDecoder().decode(dataBytesBase64Encoded);
            Map<Value, Value> valueMap = org.msgpack.core.MessagePack.newDefaultUnpacker(decode).unpackValue().asMapValue().map();


//            Map<Value, Value> valueMap = MessagePack.newDefaultUnpacker(Base64.getDecoder().decode(record)).unpackValue().asMapValue().map();

            MessageBufferPacker packer = org.msgpack.core.MessagePack.newDefaultBufferPacker();
            packer.packMapHeader(valueMap.size());
            for (Map.Entry<Value, Value> entry : valueMap.entrySet()) {
                String key = entry.getKey().toString();
                entry.getKey().writeTo(packer);
                if (key.equals("RecordUid")) {
                    ImmutableStringValueImpl immutableStringValue = new ImmutableStringValueImpl(NanoIdUtils.randomNanoId(30));
                    immutableStringValue.writeTo(packer);
                } else {
                    entry.getValue().writeTo(packer);
                }
            }
            byte[] packedData = packer.toByteArray();
            recordData = new String(Base64.getEncoder().encode(packedData));
            log.warn("单回合录像重新编码完成");
//            log.warn("编码之后的recordData：{}",recordData);
        } catch (IOException e) {
            log.warn("单回录像重新压缩失败");
            throw new BusinessException(e.toString());
        }

        String temp = decodeControlRecordData(recordData);

        PlayerControlRecordData recordParsedData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
        long unixTimeNow = TimeUtils.getUnixTimeSecond();

//        //如果没用提供uid，则提供uid并写入文件
//        if (recordParsedData.getRecordUid() == null){
//
//            recordParsedData.setRecordUid(NanoIdUtils.randomNanoId(30));
//            dataBytesBase64Encoded = encodeControlRecordData(recordParsedData);
//
//        }

        //默认是玩家上传录像
        if (recordParsedData.getRecordDataSource() == null) {
            recordParsedData.setRecordDataSource(PlayerControlRecordSource.Player);
        }

        //        if (recordParsedData.getWindId() != null){
//            recordDocData.setWindId(recordParsedData.getWindId());
//        }

//        log.warn("重新压缩后的base64编码:{}",recordData);

        return new PlayerControlRecordDocData(
                recordParsedData.getRecordUid(),
                recordParsedData.getPlayerUid(),
                recordParsedData.getGameVersion(),
                recordParsedData.getRecordVersion(),
                recordParsedData.getAnimalRouteUid(),
                recordParsedData.getAnimalId(),
                recordParsedData.getBulletId(),
                recordParsedData.getGunId(),
                recordParsedData.getGunLevel(),
                recordParsedData.getWindId(),
                recordParsedData.getIsAnimalKill(),
                recordParsedData.getFinalScore(),
                Double.valueOf(recordParsedData.getAverageShowPrecision()),
                false,
                recordParsedData.getRecordDataSource(),
                recordData,
                unixTimeNow
        );
    }

    @Override
    public List<RangeInt> getChapterRecordsTrophySegmentRanges(ChapterTableValue chapterTableValue) {

        List<RangeInt> results = new ArrayList<>();
        List<Integer> trophySegmentArray = chapterTableValue.getRecordsTrophySegmentArray();
        for (int i = 0; i < trophySegmentArray.size(); i++) {
            int trophyCount = trophySegmentArray.get(i);
            int segmentStart = trophyCount;
            int segmentEnd = trophyCount;
            if (i == 0) {
                segmentStart = 0;
            } else {
                segmentStart = trophySegmentArray.get(i - 1);
            }

            results.add(new RangeInt(segmentStart, segmentEnd));
        }

        results.add(new RangeInt(trophySegmentArray.size() - 1, 9000000));
        return results;
    }

    @Resource
    private RecordIndexService recordIndexService;

    @Override
    public void downloadPlayerWholeData(Boolean IsOpponentDisconnect, String gameVersion, PlayerUploadWholeMatchControlRecordData wholeData) {

        String archiveCollectionRef = Path.getMatchControlRecordsArchivedCollectionPath(gameVersion);
        String recordKey = archiveCollectionRef + ":" + wholeData.getUid();
        saveMatchControlRecords(recordKey, wholeData);
        log.warn("保存所有整回合录像成功，路径为{}", recordKey);

        List<String> roundReportData = new ArrayList<>();
        BattleCompleteDto battleCompleteDto = ThreadLocalForFight.getBattleCompleteDto();

        //检查 是否有任何一回合的录像是合法的，如果有，就可以作为单句录像保存下来
        boolean isAllRecordsNotValid = true;
        if (!StringUtils.isEmpty(wholeData.getEncodedBytes_Base64())) {

            for (String encodedBytes : wholeData.getEncodedBytes_Base64()) {

                if (checkControlRecordIsValid(encodedBytes)) {
                    isAllRecordsNotValid = false;
                    break;
                }
            }
        }

        //如果对方没有断线，或者玩家自己不是每回合都没有操作，则可以作为整局匹配数据进入录像池
        if (!IsOpponentDisconnect && !isAllRecordsNotValid && wholeData.getUsable() && wholeData.getUsable()) {

            //确定这个录像应该放在录像池的哪一个奖杯分段中
            RangeInt recordTrophySegmentRange = getControlRecordTrophySegmentRange(wholeData.getChapterId(), wholeData.getPlayerTrophyCount(), gameVersion);
            String segmentCollectionRef = Path.getMatchControlRecordsPoolTrophySegmentMetaData(
                    gameVersion,
                    wholeData.getMatchId(),
                    recordTrophySegmentRange
            );

            saveDataToMatchControlRecordsPoolTrophySegmentCollection(segmentCollectionRef, recordKey, wholeData);
            log.info("保存检测过后的完整比赛录像文件到" + segmentCollectionRef);
        }

        //同时，比赛录像还要分开保存为单个录像，方便回合录像匹配
        if (wholeData.getUsable() && !StringUtils.isEmpty(wholeData.getEncodedBytes_Base64())) {

            for (int i = 0; i < wholeData.getEncodedBytes_Base64().size(); i++) {
                if (!checkControlRecordIsValid(wholeData.getEncodedBytes_Base64().get(i))) {
                    continue;
                }

//                String temp = playerControlRecordDataService.decodeControlRecordData(wholeData.getEncodedBytes_Base64().get(i));
//                PlayerControlRecordData controlRecordData = JSONObject.parseObject(temp,PlayerControlRecordData.class);

                PlayerControlRecordDocData controlRecordData = createPlayerControlRecordDocData(wholeData.getEncodedBytes_Base64().get(i));

                roundReportData.add(JSONUtil.toJsonStr(controlRecordData));

                saveSinglePlayerControlRecord(controlRecordData, recordKey, i, gameVersion);

                RedisDBOperation.threadPool.execute(() -> {
                    String key = Path.getMatchSingleRoundControlRecordsPoolCollectionPath(
                            gameVersion,
                            controlRecordData.getAnimalId(),
                            controlRecordData.getAnimalRouteUid(),
                            controlRecordData.getGunId(),
                            controlRecordData.getGunLevel(),
                            controlRecordData.getBulletId(),
                            controlRecordData.getWindId(),
                            controlRecordData.getAverageShowPrecision()
                    );
                    RecordIndex recordIndex = new RecordIndex(null,
                            controlRecordData.getUid(),
                            key,
                            controlRecordData.getPlayerUid(),
                            controlRecordData.getGameVersion(),
                            controlRecordData.getRecordVersion().longValue(),
                            controlRecordData.getAnimalRouteUid().toString(),
                            controlRecordData.getAnimalId().longValue(),
                            controlRecordData.getBulletId().longValue(),
                            controlRecordData.getGunId(),
                            controlRecordData.getGunLevel(),
                            controlRecordData.getWindId(),
                            controlRecordData.getIsAnimalKilled(),
                            controlRecordData.getFinalScore(),
                            controlRecordData.getAverageShowPrecision(),
                            controlRecordData.getSource().getType(),
                            controlRecordData.getUploadTime()
                    );
                    recordIndexService.save(recordIndex);
                });


            }
        }

        if (battleCompleteDto != null) {
            battleCompleteDto.setRoundReportData(roundReportData);
            ThreadLocalForFight.setBattleCompleteDto(battleCompleteDto);
        }

    }


    /**
     * 将录像索引保存到匹配的录像池子中并为更新池子中的玩家记录
     *
     * @param path
     * @param dataUid
     * @param saveData
     */
    public void saveDataToMatchControlRecordsPoolTrophySegmentCollection(String path, String dataUid, PlayerUploadWholeMatchControlRecordData saveData) {


        int score = saveData.getPlayerTotalScore();
        String playerUid = saveData.getPlayerUid();
        /*
         *保存的条件：
         *1.该局对应的玩家录像不超过10个
         *2.池子里的数量不超过1000
         */
        String playerPoolKey = "PlayerControlRecords:" + playerUid + ":" + path;
        MatchRecord record = new MatchRecord(dataUid, score, playerUid);

        //添加相关玩家的池子录像数量
        RedisDBOperation.insertPlayerRecordsInMatchControlRecordsPoolTrophySegment(playerPoolKey, record);

        RedisDBOperation.insertDataToMatchControlRecordsPoolTrophySegmentCollection(path, record);
        //如果这个库中玩家录像数量多余10个就删除
        if (RedisDBOperation.selectMatchRecordInPlayerRecordsLength(playerPoolKey) > 10) {
            MatchRecord playerOutRecord = RedisDBOperation.selectPlayerRecordsInMatchControlRecordsPoolTrophySegment(playerPoolKey);
            RedisDBOperation.deleteDataToMatchControlRecordsPoolTrophySegmentCollection(path, playerOutRecord);
        }


    }

    @Override
    public void downloadSingleData(PlayerControlRecordDocData recordDocData) {

        String key = Path.getMatchSingleRoundControlRecordsPoolCollectionPath(
                recordDocData.getGameVersion(),
                recordDocData.getAnimalId(),
                recordDocData.getAnimalRouteUid(),
                recordDocData.getGunId(),
                recordDocData.getGunLevel(),
                recordDocData.getBulletId(),
                recordDocData.getWindId(),
                recordDocData.getAverageShowPrecision()
        );

        RedisDBOperation.insertSingleRoundControlRecords(key, recordDocData);
    }

    @Override
    public String decodePlayerControlRecordData(String dataBytesBase64Encoded) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decode = decoder.decode(dataBytesBase64Encoded);
        String temp = null;
        MessagePack messagePack = new MessagePack();
        try {
            temp = messagePack.read(decode).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    /**
     * 存储单个录像,需要整个录像的标记和第几局的标记
     *
     * @param recordDocData
     * @param wholeRecordsKey
     * @param index
     * @param gameVersion
     */
    private void saveSinglePlayerControlRecord(PlayerControlRecordDocData recordDocData, String wholeRecordsKey, Integer index, String gameVersion) {

        String key = Path.getMatchRoundControlRecordsPoolCollectionPath(
                gameVersion,
                recordDocData.getAnimalId(),
                recordDocData.getAnimalRouteUid(),
                recordDocData.getGunId(),
                recordDocData.getGunLevel(),
                recordDocData.getBulletId(),
                recordDocData.getWindId(),
                recordDocData.getAverageShowPrecision()
        );


//        SingleRecordIndex singleRecord = new SingleRecordIndex(wholeRecordsKey,index);
        RedisDBOperation.insertSingleRoundControlRecords(key, recordDocData);
    }


}
