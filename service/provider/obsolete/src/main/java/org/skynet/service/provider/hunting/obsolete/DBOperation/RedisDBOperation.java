package org.skynet.service.provider.hunting.obsolete.DBOperation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.skynet.commons.context.exception.SkynetException;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.query.UserDataLandQuery;
import org.skynet.components.hunting.user.query.UserDataUpdateQuery;
import org.skynet.components.hunting.user.service.UserFeignService;
import org.skynet.commons.lang.common.Result;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.ApplicationContextUtil;
import org.skynet.service.provider.hunting.obsolete.common.util.DeflaterUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.HttpUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.CloudUserDataDto;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.table.PendingPurchaseOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.skynet.starter.codis.service.CodisService;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 要点 1.唯一原则：数据库里只有一份实体数据，其他的都是它的索引
 * 2.大数据要压缩、不常用的也压缩
 * 3.不能直接存java对象，需转成json字符串
 */
@Component
@ApiModel(value = "redisDBOperation对象", description = "redis数据库的相关操作")
@Slf4j
public class RedisDBOperation {

    @ApiModelProperty(value = "当前环境变量")
    private static RedisDBOperation redisDBOperation;

    // @Resource
    // private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CodisService codisService;

    @Resource
    private UserFeignService userFeignService;

    @PostConstruct
    public void init() {
        redisDBOperation = this;
        // redisDBOperation.redisTemplate = this.redisTemplate;
        redisDBOperation.codisService = this.codisService;
        redisDBOperation.userFeignService = this.userFeignService;
    }


    /*
     -------------
     整回合游戏录像
     -------------
     */

    /**
     * 对比赛结束录像信息进行存档
     * 不论录像质量如何，都要先存档
     * 采用key:value的形式
     *
     * @param key     示例:matchControlRecordsArchived_3:"录像uid"
     * @param zipData 录像压缩文件
     */
    public static void insertMatchControlRecords(String key, String zipData) {
        // redisDBOperation.redisTemplate.opsForValue().set(key, zipData);
        throw new RuntimeException("功能取消");
    }

    public static String selectMatchControlRecords(String key) {
        // return (String) redisDBOperation.redisTemplate.opsForValue().get(key);
        throw new RuntimeException("功能取消");
    }

    /*
     -------------
     单回合录像
     -------------
     */

    /**
     * 储存单回路录像的索引
     * 通过动物id，动物routeUid，gunId,gunLevel,bulletId拼接list的key，每个list放300个索引
     *
     * @param key
     * @param recordDocData
     */
    public static void insertMatchRoundControlRecordsPool(String key, PlayerControlRecordDocData recordDocData) {


//         //先压缩再存
//         String jsonString = JSONObject.toJSONString(recordDocData);
//         String zipString = DeflaterUtils.zipString(jsonString);
//         redisDBOperation.redisTemplate.opsForList().leftPush(key, zipString);
//         if (redisDBOperation.redisTemplate.opsForList().size(key) >= 100) {
//             redisDBOperation.redisTemplate.opsForList().rightPop(key);
//         }
//
// //        redisDBOperation.redisTemplate.opsForList().leftPush(key,recordIndex);
// //
// //        if (redisDBOperation.redisTemplate.opsForList().size(key)>=100){
// //            redisDBOperation.redisTemplate.opsForList().rightPop(key);
// //        }

        throw new RuntimeException("功能取消");
    }


    public static List<Object> selectMatchRoundControlRecords(String path) {

// //        return redisDBOperation.redisTemplate.opsForList().range(path, 0, -1);
//
//         //MatchRoundControlRecordsArchived_5:animalId101:animalRouteUid792117168:gunId1:gunLevel1:bulletId1:windId0:averageShowPrecision10
//         List<Object> resultList = new ArrayList<>();
//         String substring = path.substring(0, path.length() - 1);
//         try {
//             String number = substring.substring(substring.length() - 1);
//             Integer.parseInt(number);
//             substring = substring.substring(0, substring.length() - 1);
//         } catch (Exception e) {
//
//         }
//         for (int i = 1; i <= 11; i++) {
//             path = substring + i;
//             List<Object> range = redisDBOperation.redisTemplate.opsForList().range(path, 0, -1);
//             if (range != null) {
//                 resultList.addAll(range);
//             }
//         }
//         return resultList;

        throw new RuntimeException("功能取消");
    }

    /**
     * 早期没有回合匹配时的单回合对战数据，需要以另外一种方式来存
     *
     * @param key
     * @param recordDocData
     */
    public static void insertSingleRoundControlRecords(String key, PlayerControlRecordDocData recordDocData) {

        //先压缩再存
        String jsonString = JSONObject.toJSONString(recordDocData);
        String zipString = DeflaterUtils.zipString(jsonString);
        // redisDBOperation.redisTemplate.opsForList().leftPush(key, zipString);
        // if (redisDBOperation.redisTemplate.opsForList().size(key) >= 100) {
        //     redisDBOperation.redisTemplate.opsForList().rightPop(key);
        // }
        throw new RuntimeException("功能取消");
    }

    public static List<Object> selectSingleRoundControlRecords(String path) {

//        return redisDBOperation.redisTemplate.opsForList().range(path, 0, -1);

        // List<Object> resultList = new ArrayList<>();
        // String substring = path.substring(0, path.length() - 1);
        // try {
        //     String number = substring.substring(substring.length() - 1);
        //     Integer.parseInt(number);
        //     substring = substring.substring(0, substring.length() - 1);
        // } catch (Exception e) {
        //
        // }
        // for (int i = 1; i <= 11; i++) {
        //     path = substring + i;
        //     List<Object> range = redisDBOperation.redisTemplate.opsForList().range(path, 0, -1);
        //     if (range != null) {
        //         resultList.addAll(range);
        //     }
        // }
        // return resultList;

        throw new RuntimeException("功能取消");
    }


    public static List<Object> selectSingleRoundControlRecordsForAveragePrecisionLevel(String path) {

        // return redisDBOperation.redisTemplate.opsForList().range(path, 0, -1);
        throw new RuntimeException("功能取消");
    }



    /*
     -------------
     匹配池录像
     -------------
     */

    /**
     * 保存录像到对应的匹配池
     *
     * @param key    池子的信息 示例MatchControlRecordsPool:3:101:700-800
     * @param record 分数
     */
    public static void insertDataToMatchControlRecordsPoolTrophySegmentCollection(String key, MatchRecord record) {

        // redisDBOperation.redisTemplate.boundListOps(key).leftPush(record);
        //
        // List<MatchRecord> list = new ArrayList<>();
        // //匹配池长度大于1000就删除旧的
        // while (selectMatchControlRecordsPoolTrophySegmentCollectionLength(key) > 1000) {
        //     MatchRecord outRecord = (MatchRecord) redisDBOperation.redisTemplate.boundListOps(key).rightPop();
        //     list.add(outRecord);
        // }
        //
        // //删除相关玩家的池子录像数量
        // for (MatchRecord data : list) {
        //     //PlayerControlRecords:"userUid":MatchControlRecordsPool:3:101:700-800
        //     String playerDataKey = "PlayerControlRecords:" + data.getPlayerUid() + ":" + key;
        //     deletePlayerRecordsInMatchControlRecordsPoolTrophySegment(playerDataKey, data);
        // }
        throw new RuntimeException("功能取消");
    }

    /**
     * count为负表示从尾部开始删相同记录的数据（删旧的）
     * left为头部，right为尾部，数据都是头进尾出
     * 从匹配池中删除数据
     *
     * @param path
     * @param record 如何record为null则删最旧的数据
     */
    public static void deleteDataToMatchControlRecordsPoolTrophySegmentCollection(String path, MatchRecord record) {

        // if (!checkKeyExist(path) || redisDBOperation.redisTemplate.boundListOps(path).size() == 0) {
        //     throw new BusinessException("该玩家在池子里没有数据" + path);
        // }
        // if (record == null) {
        //     redisDBOperation.redisTemplate.boundListOps(path).rightPop();
        // } else
        //     redisDBOperation.redisTemplate.boundListOps(path).remove(-1, record);

        throw new RuntimeException("功能取消");
    }

    /**
     * 查询对应匹配池的长度
     *
     * @param path
     * @return
     */
    public static Long selectMatchControlRecordsPoolTrophySegmentCollectionLength(String path) {

//         if (!checkKeyExist(path)) {
// //            throw new BusinessException(path+"匹配池不存在");
//             log.warn(path + "匹配池不存在");
//         }
//
//         return redisDBOperation.redisTemplate.boundListOps(path).size();

        throw new RuntimeException("功能取消");
    }

    /**
     * 返回对应匹配池的所有数据
     *
     * @param path
     * @return
     */
    public static List<Object> selectMatchControlRecordsPoolTrophySegmentCollectionContent(String path) {

//         if (!checkKeyExist(path)) {
// //            throw new BusinessException(path+"匹配池不存在");
//             log.warn("匹配池不存在" + path);
//         }
//
//         return redisDBOperation.redisTemplate.boundListOps(path).range(0, -1);

        throw new RuntimeException("功能取消");
    }

    /*
     -------------
     匹配池的属性相关
     -------------
     */

    /**
     * 保存对应匹配池信息，过期时间为12小时
     *
     * @param path
     * @param data
     */
    public static void insertPoolTrophySegmentMetaData(String path, MatchControlRecordsPoolMetaData data) {

        // redisDBOperation.redisTemplate.opsForValue().set(path, data, 12, TimeUnit.HOURS);

        throw new RuntimeException("功能取消");
    }

    public static MatchControlRecordsPoolMetaData selectMatchControlRecordsPoolMetaData(String path) {

        // if (!checkKeyExist(path)) {
        //     log.warn(path + "匹配池信息不存在");
        //     return null;
        // }
        //
        // return (MatchControlRecordsPoolMetaData) redisDBOperation.redisTemplate.opsForValue().get(path);

        throw new RuntimeException("功能取消");
    }


     /*
     -------------
     在匹配池中的玩家信息相关
     -------------
     */

    /**
     * path示例 PlayerControlRecords:"userUid":MatchControlRecordsPool:3:101:700-800
     * 记录对应玩家在匹配池子里面的信息
     * 信息为次数和对应的matchRecord
     * 采用list存储
     * list的长度即为次数
     *
     * @param path   由玩家id和章节和杯数范围组成
     * @param record
     */
    public static void insertPlayerRecordsInMatchControlRecordsPoolTrophySegment(String path, MatchRecord record) {

        // redisDBOperation.redisTemplate.boundListOps(path).leftPush(record);
        throw new RuntimeException("功能取消");
    }

    /**
     * count为负表示从尾部开始删相同记录的数据（删旧的）
     * left为头部，right为尾部，数据都是头进尾出
     *
     * @param path
     * @param record 如何record为null则删最旧的数据
     * @return
     */
    public static void deletePlayerRecordsInMatchControlRecordsPoolTrophySegment(String path, MatchRecord record) {

        // if (!checkKeyExist(path) || redisDBOperation.redisTemplate.boundListOps(path).size() == 0) {
        //     throw new BusinessException("该玩家在池子里没有数据" + path);
        // }
        // if (record == null) {
        //     redisDBOperation.redisTemplate.boundListOps(path).rightPop();
        // } else
        //     redisDBOperation.redisTemplate.boundListOps(path).remove(-1, record);

        throw new RuntimeException("功能取消");
    }

    /**
     * 获取存储最长时间的MatchRecord
     *
     * @param path
     * @return
     */
    public static MatchRecord selectPlayerRecordsInMatchControlRecordsPoolTrophySegment(String path) {

        // return (MatchRecord) redisDBOperation.redisTemplate.boundListOps(path).rightPop();

        throw new RuntimeException("功能取消");
    }

    /**
     * 查询玩家在对应匹配池里面的数据量
     *
     * @param path
     * @return
     */
    public static Long selectMatchRecordInPlayerRecordsLength(String path) {

        // if (!checkKeyExist(path)) {
        //     log.info("该玩家在池子里没有数据" + path);
        //     return 0L;
        // }
        // return redisDBOperation.redisTemplate.boundListOps(path).size();

        throw new RuntimeException("功能取消");
    }

    /*
     -------------
     玩家最近比赛相关
     -------------
     */

    /**
     * path示例:playerControlRecords:playerChapterLatestMatchScore:"版本标记":"玩家自己的uuid":"章节id"
     * 返回对应玩家在对应章节中的最近几场比赛的分数记录
     *
     * @param path
     * @return
     */
    public static List<Object> selectPlayerChapterLatestMatchScoreCollection(String path) {

        // String key = "PlayerControlRecords" + ":" + path;
        // if (!checkKeyExist(key)) {
        //     log.info("玩家在对应章节没有分数记录");
        //     return null;
        // }
        // return redisDBOperation.redisTemplate.boundListOps(key).range(0, -1);

        throw new RuntimeException("功能取消");
    }

    /**
     * 保存玩家在对应章节中的分数记录
     *
     * @param path
     * @param score
     */
    public static void insertPlayerChapterLatestMatchScoreCollection(String path, Integer score) {

        // String key = "PlayerControlRecords" + ":" + path;
        // redisDBOperation.redisTemplate.boundListOps(key).leftPush(score);
        // if (redisDBOperation.redisTemplate.boundListOps(key).size() > 5) {
        //     redisDBOperation.redisTemplate.boundListOps(key).rightPop();
        // }

        throw new RuntimeException("功能取消");

    }

    /**
     * 返回玩家最近匹配到的对局
     * path 示例 playerControlRecords:playerChapterLatestMatchScore:"版本标记":"玩家自己的uuid"
     *
     * @param path
     * @return
     */
    public static List<Object> selectPlayerChapterLatestMatchPlayerUidCollection(String path) {

        // return redisDBOperation.redisTemplate.boundListOps(path).range(0, -1);
        throw new RuntimeException("功能取消");
    }

    /**
     * 保存玩家最近匹配到的对局，只保留50个
     *
     * @param path
     * @param matchUid
     */
    public static void insertPlayerChapterLatestMatchPlayerUidCollection(String path, String matchUid) {


        // redisDBOperation.redisTemplate.boundListOps(path).leftPush(matchUid);
        // //只保留50个
        // while (redisDBOperation.redisTemplate.boundListOps(path).size() >= 50) {
        //     redisDBOperation.redisTemplate.boundListOps(path).rightPop();
        // }

        throw new RuntimeException("功能取消");
    }

    /**
     * 玩家开始了一场比赛，设置标记
     *
     * @param userUid
     * @param huntingMatchUUid
     */
    public static void insertPlayerHuntingMatchNowData(String userUid, String huntingMatchUUid) {

        String key = "PlayerControlRecords:HuntingMatchNowUUid" + ":" + userUid;
        // redisDBOperation.redisTemplate.opsForList().leftPush(key, huntingMatchUUid);
        redisDBOperation.codisService.leftPushAll(key, huntingMatchUUid);
        // throw new RuntimeException("功能取消");
    }

    public static void setPlayerHuntingMatchNowData(String userUid, String huntingMatchUUid) {
        String key = "PlayerControlRecords:HuntingMatchNowUUid" + ":" + userUid;
        // redisDBOperation.redisTemplate.opsForList().leftPop(key);
        // redisDBOperation.redisTemplate.opsForList().leftPush(key, huntingMatchUUid);
        redisDBOperation.codisService.lpop(key);
        redisDBOperation.codisService.leftPushAll(key, huntingMatchUUid);
        // throw new RuntimeException("功能取消");
    }

    /**
     * 玩家结束了一场比赛，删除标记
     *
     * @param userUid
     * @param huntingMatchUUid
     */
    public static void deletePlayerHuntingMatchNowData(String userUid, String huntingMatchUUid) {

        String key = "PlayerControlRecords:HuntingMatchNowUUid" + ":" + userUid;
        // redisDBOperation.redisTemplate.opsForList().remove(key, 0, huntingMatchUUid);
        throw new RuntimeException("功能取消");

    }

    /**
     * 返回玩家所有正在进行中的比赛
     *
     * @param userUid
     * @return
     */
    public static List<Object> selectPlayerHuntingMatchNowData(String userUid) {

        String key = "PlayerControlRecords:HuntingMatchNowUUid" + ":" + userUid;
        // return redisDBOperation.redisTemplate.opsForList().range(key, 0, -1);
        throw new RuntimeException("功能取消");
    }

    /*
        ----------
        HuntingMatchNowData
        正在进行的比赛相关
        ----------
     */

    /**
     * 插入HuntingMatchNowData
     *
     * @param key
     * @param huntingMatchNowData
     */
    public static void insertHuntingMatchNowData(String key, HuntingMatchNowData huntingMatchNowData) {
        // redisDBOperation.redisTemplate.opsForValue().set(key, JSONObject.toJSONString(huntingMatchNowData), 30, TimeUnit.DAYS);
        redisDBOperation.codisService.set(key, JSONObject.toJSONString(huntingMatchNowData), 30 * 24 * 60 * 60 * 1000, TimeUnit.DAYS);
        // throw new RuntimeException("功能取消");
    }

    /**
     * 查找对应的正在进行的比赛数据
     *
     * @param key
     * @return
     */
    public static HuntingMatchNowData selectHuntingMatchNowData(String key) {

        // String temp = (String) redisDBOperation.redisTemplate.opsForValue().get(key);
        String temp = redisDBOperation.codisService.get(key);
        return JSONObject.parseObject(temp, HuntingMatchNowData.class);
        // throw new RuntimeException("功能取消");
    }

    /**
     * 删除HuntingMatchNowData
     *
     * @param key
     */
    public static void deleteHuntingMatchNowData(String key) {

        // if (!checkKeyExist(key)) {
        //     log.error("HuntingMatchNowData" + key + "不存在");
        // }
        // redisDBOperation.redisTemplate.delete(key);

        redisDBOperation.codisService.del(key);
        // throw new RuntimeException("功能取消");
    }

    /*
        ----------
        HuntingMatchHistoryData
        历史比赛数据
        ----------
     */

    /**
     * key示例: HuntingMatchHistory:"gameVersion":"matchUid" 压缩存储
     *
     * @param key
     * @param zipString
     */
    public static void insertHuntingMatchHistoryData(String key, String zipString) {

        // redisDBOperation.redisTemplate.opsForValue().set(key, zipString);

        throw new RuntimeException("功能取消");

    }

    public static HuntingMatchHistoryData selectHuntingMatchHistoryData(String key) throws IOException {

        // String zip = (String) redisDBOperation.redisTemplate.opsForValue().get(key);
        // String unzipString = DeflaterUtils.unzipString(zip);
        // return JSONObject.parseObject(unzipString, HuntingMatchHistoryData.class);
        throw new RuntimeException("功能取消");
    }

    /*
        ----------
        userData相关
        ----------
     */
    public static ExecutorService threadPool = Executors.newFixedThreadPool(100);

    /**
     * 保存玩家信息
     *
     * @param userData
     */
    public static void insertUserData(UserData userData) {
        // String key = "User:" + userData.getUuid();
        //
        // redisDBOperation.redisTemplate.opsForValue().set(key, JSONObject.toJSONString(userData));
        //
        // //todo 优化落库操作，定时刷新在线
        // threadPool.execute(() -> {
        //     if (userData.getUserId() == null) {
        //         userData.setUserId(redisDBOperation.getIncr("userAutoIncrementId"));
        //     }
        //     UserDataVOService voService = ApplicationContextUtil.getBean(UserDataVOService.class);
        //     // voService.updateUserData(userData);// TODO 取消用户数据的更新
        // });

        redisDBOperation.userFeignService.update(UserDataUpdateQuery.builder().userId(userData.getUuid()).userData(userData).build());
    }

    public static Boolean deleteUserData(String userUid) {

        // String key = "User:" + userUid;
        //
        // Boolean delete = redisDBOperation.redisTemplate.delete(key);
        // if (delete) {
        //     threadPool.execute(() -> {
        //         UserDataVOService voService = ApplicationContextUtil.getBean(UserDataVOService.class);
        //         voService.deleteUserData(userUid);
        //     });
        // }
        // return delete;
        throw new RuntimeException("功能取消");
    }

    /**
     * 返回数据库中的玩家信息
     *
     * @param key
     * @return
     */
    public static UserData selectUserData(String key) {

        // String temp = (String) redisDBOperation.redisTemplate.opsForValue().get(key);
        Result<UserData> loadResult = redisDBOperation.userFeignService.load(UserDataLandQuery.builder().userId(key.substring("User:".length())).build());
        SkynetException.asserter(loadResult.getSuccess(), -1);

        // if (temp == null) {
        if (Objects.isNull(loadResult.getData())) {
            SystemPropertiesConfig systemPropertiesConfig = ApplicationContextUtil.getBean(SystemPropertiesConfig.class);
            log.warn("redis中没有该用户信息，从谷歌数据库查询");
//            throw new BusinessException("数据库中没有该玩家信息"+key,-1);
//            return null;
            String cloudUrl = systemPropertiesConfig.getCloudUrl();
            String uuid = key.substring(5);
//            String testUrl = cloudUrl + "/admin-downloadUserData";
            //https://us-central1-wildhunthuntingclash.cloudfunctions.net/admin-downloadUserData 正式服
            //https://asia-south1-huntingmasterrecord.cloudfunctions.net/admin-downloadUserData 印度服
            String url = "https://us-central1-wildhunthuntingclash.cloudfunctions.net/admin-downloadUserData";
//            log.warn("谷歌服务器链接：{}",testUrl);
//            https://us-central1-xxhuntingxx-2876d.cloudfunctions.net/admin-readUserData
            CloudUserDataDto dataDto = new CloudUserDataDto("huF9NVzVKRQ^F&Nb8Cgmnhtl#Nvolu", uuid);
            UserData userData = HttpUtil.getUserDataFromCloud(url, dataDto);
            if (userData == null) {
                log.warn("谷歌数据库中没有该用户信息，抛出异常");
                throw new BusinessException("数据库中没有该玩家信息" + key, -1);
            }
            // if (userData.getUserId() == null) {
            //     Long userId = redisDBOperation.getIncr("userAutoIncrementId");
            //     userData.setUserId(userId);
            // }

            if (null != userData.getLinkedAuthProviderData()) {
                if (!StringUtils.isBlank(userData.getLinkedAuthProviderData().getFacebookUserId())) {
                    AccountMapData facebookAccountMapData = new AccountMapData(userData.getLinkedAuthProviderData().getFacebookUserId(), userData.getUuid());
                    RedisDBOperation.insertFacebookAccountMapData(userData.getLinkedAuthProviderData().getFacebookUserId(), facebookAccountMapData);
                    log.warn("写入角色与Facebook账号的映射关系:{}", facebookAccountMapData);
                }

                if (!StringUtils.isBlank(userData.getLinkedAuthProviderData().getGooglePlayUserId())) {
                    AccountMapData googleAccountMapData = new AccountMapData(userData.getLinkedAuthProviderData().getGooglePlayUserId(), userData.getUuid());
                    RedisDBOperation.insertGoogleAccountMapData(userData.getLinkedAuthProviderData().getGooglePlayUserId(), googleAccountMapData);
                    log.warn("写入角色与Google账号的映射关系:{}", googleAccountMapData);
                }
            }

            insertUserData(userData);
            // temp = JSONObject.toJSONString(userData);
            return userData;
        }
        // return JSONObject.parseObject(temp, UserData.class);
        return loadResult.getData();
    }



    /*
        ----------
        token相关
        ----------
     */

    public static void insertUserToken(String userUid, String token, Long tokenExpiration) {

        String key = "Token:" + userUid;

        // redisDBOperation.redisTemplate.opsForValue().set(key, token, tokenExpiration, TimeUnit.DAYS);
        redisDBOperation.codisService.set(key, token, tokenExpiration * 24 * 60, TimeUnit.SECONDS);
    }

    public static void setCacheObject(final String key, final String value, final Integer timeout, final TimeUnit timeUnit) {
        // redisDBOperation.redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
        redisDBOperation.codisService.set(key, value, timeout, TimeUnit.MILLISECONDS);
    }
    public static String getCacheObject(final String key) {
        // return (String) redisDBOperation.redisTemplate.opsForValue().get(key);
        return redisDBOperation.codisService.get(key);
    }

    public static boolean deleteKey(final String key) {
        // return redisDBOperation.redisTemplate.delete(key);
        return redisDBOperation.codisService.del(key) > 0;
    }

    public static void deleteUserToken(String userUid) {

        String key = "Token:" + userUid;
        // redisDBOperation.redisTemplate.delete(key);
        redisDBOperation.codisService.del(key);
    }

    public static String selectUserToken(String userUid) {
        String key = "Token:" + userUid;
        // return (String) redisDBOperation.redisTemplate.opsForValue().get(key);
        return redisDBOperation.codisService.get(key);
    }


    /*
        ----------
        游戏静态数据
        ----------
     */
    public static String getDataTable(String key) {

        // return (String) redisDBOperation.redisTemplate.opsForValue().get(key);
        return redisDBOperation.codisService.get(key);
    }

    public static void insertDataTable(String key, String zipDataTable) {

        // redisDBOperation.redisTemplate.opsForValue().set(key, zipDataTable);
        redisDBOperation.codisService.set(key, zipDataTable);
    }

    //ai先手比赛规则
    public static void insertLocalPlayerFirstAiRecordChooseRules(String key, String zipDataTable) {

        // redisDBOperation.redisTemplate.opsForValue().set(key, zipDataTable);
        throw new RuntimeException("功能取消");
    }

    public static String selectLocalPlayerFirstAiRecordChooseRules(String key) {

        // return (String) redisDBOperation.redisTemplate.opsForValue().get(key);
        throw new RuntimeException("功能取消");
    }

    //玩家先手比赛规则
    public static void insertAiFirstAiRecordChooseRules(String key, String zipDataTable) {

        // redisDBOperation.redisTemplate.opsForValue().set(key, zipDataTable);
        throw new RuntimeException("功能取消");
    }

    public static String selectAiFirstAiRecordChooseRules(String key) {

        // return (String) redisDBOperation.redisTemplate.opsForValue().get(key);
        throw new RuntimeException("功能取消");
    }

    /*
        ----------
        订单相关
        ----------
     */

    /**
     * 获取待购买的消费订单号
     *
     * @param orderId
     * @return
     */
    public static PendingPurchaseOrder selectPendingCustomOrder(String orderId) {

        String path = Path.getPendingCustomOrderCollectionPath();
        // return (PendingPurchaseOrder) redisDBOperation.redisTemplate.opsForValue().get(path + ":" + orderId);
        String content = redisDBOperation.codisService.get(path + ":" + orderId);
        PendingPurchaseOrder pendingPurchaseOrder = JSONObject.parseObject(content, PendingPurchaseOrder.class);
        return pendingPurchaseOrder;
    }


    public static void insertPendingCustomOrders(String orderId, PendingPurchaseOrder pendingOrder) {

        String path = Path.getPendingCustomOrderCollectionPath();
        // redisDBOperation.redisTemplate.opsForValue().set(path + ":" + orderId, pendingOrder, 30, TimeUnit.DAYS);
        redisDBOperation.codisService.set(path + ":" + orderId, JSONObject.toJSONString(pendingOrder), 30 * 24 * 60 * 60 * 1000, TimeUnit.DAYS);
    }

    public static void insertArchiveCustomOrders(String orderId, PendingPurchaseOrder pendingOrder) {

        String path = Path.getArchiveCustomOrderCollectionPath();
        // redisDBOperation.redisTemplate.opsForValue().set(path + ":" + orderId, pendingOrder, 30, TimeUnit.DAYS);
        redisDBOperation.codisService.set(path + ":" + orderId, JSONObject.toJSONString(pendingOrder), 30 * 24 * 60 * 60 * 1000, TimeUnit.DAYS);
    }

    public static void deletePendingCustomOrder(String orderId) {

        String finalPath = Path.getPendingCustomOrderCollectionPath() + ":" + orderId;
        // redisDBOperation.redisTemplate.delete(finalPath);
        redisDBOperation.codisService.del(finalPath);
    }

    /**
     * 完成的订单保存订单存根
     * @param orderId
     * @param saveData
     */
//    public static void insertCompleteOrder(String orderId,CompletedOrder saveData){
//
//        String key = "completedIapOrders"+":"+orderId;
//        redisDBOperation.redisTemplate.opsForValue().set(key,saveData);
//    }
//
//    public static CompletedOrder selectCompleteOrder(String path){
//
//        return (CompletedOrder) redisDBOperation.redisTemplate.opsForValue().get(path);
//    }
//
//    public static void deleteCompleteOrder(String orderId){
//
//        String finalPath = "completedIapOrders"+":"+orderId;
//        redisDBOperation.redisTemplate.delete(finalPath);
//    }


      /*
        ----------
        开箱
        ----------
     */

    /**
     * 保存开箱奖励
     *
     * @param path
     * @param archiveResult
     */
    public static void insertChestOpenResult(String path, ArchiveChestOpenResult archiveResult) {

//        gameEnvironment.redisTemplate.opsForValue().set(path,archiveResult,60, TimeUnit.DAYS);
        String jsonString = JSONObject.toJSONString(archiveResult);
        // redisDBOperation.redisTemplate.opsForList().leftPush(path, jsonString);
        redisDBOperation.codisService.leftPushAll(path, jsonString);
    }


    /**
     * 获取最后一次开箱奖励
     *
     * @param uid
     * @return
     */
    public static ArchiveChestOpenResult selectLatestOpenedChestOpenResult(String uid) {

        // String jsonString = (String) redisDBOperation.redisTemplate.boundListOps(uid).index(0);

        String jsonString = redisDBOperation.codisService.lrange(uid, 0, 1).get(0);

        return JSONObject.parseObject(jsonString, ArchiveChestOpenResult.class);
    }


    /*
        ----------
        ai对手信息
        ----------
     */
    public static OpponentProfile selectOpponentProfile(String aiProfileId) {

        // if (!redisDBOperation.redisTemplate.hasKey(aiProfileId))
        //     log.error("redis中不存在OpponentProfile key" + aiProfileId);
        // return (OpponentProfile) redisDBOperation.redisTemplate.opsForValue().get(aiProfileId);

        throw new RuntimeException("功能取消");
    }

    public static void insertOpponentProfile(String key, OpponentProfile opponentProfile) {

        // redisDBOperation.redisTemplate.opsForValue().set(key, opponentProfile);
        throw new RuntimeException("功能取消");
    }


    /*
      ----------
      游客账号
      ----------
   */
    public static void insertGuestAccount(String deviceId, String userUid, AccountMapData accountMapData) {

        String key = Path.getGuestAccountMapDataCollectionPath() + ":" + userUid;

        if (!checkKeyExist(key)) {
//            throw new BusinessException("关联了多个游客账号");
//             redisDBOperation.redisTemplate.opsForHash().put(key, deviceId, JSONObject.toJSONString(accountMapData));
            redisDBOperation.codisService.hset(key, deviceId, JSONObject.toJSONString(accountMapData));
        }
    }

    public static AccountMapData selectGuestAccount(String deviceId, String userUid) {

        String key = Path.getGuestAccountMapDataCollectionPath() + ":" + userUid;
        // String jsonString = (String) redisDBOperation.redisTemplate.opsForHash().get(key, deviceId);
        String jsonString = redisDBOperation.codisService.hget(key, deviceId);

        if (jsonString != null)
            return JSONObject.parseObject(jsonString, AccountMapData.class);
        else
            return null;
    }

    public static void deleteGuestAccount(String deviceId, String userUid) {

        String key = Path.getGuestAccountMapDataCollectionPath() + ":" + userUid;
        // redisDBOperation.redisTemplate.delete(key);
        redisDBOperation.codisService.del(key);
    }

    /*
        ----------
        google绑定账号
        ----------
     */
    public static void insertGoogleAccountMapData(String providerUserId, AccountMapData accountMapData) {

        String key = Path.getGoogleAccountMapDataCollectionPath();
        String jsonString = JSONObject.toJSONString(accountMapData);

        // redisDBOperation.redisTemplate.opsForHash().put(key, providerUserId, jsonString);
        redisDBOperation.codisService.hset(key, providerUserId, jsonString);
    }

    public static AccountMapData selectGoogleAccountMapData(String providerUserId) {

        String key = Path.getGoogleAccountMapDataCollectionPath();
        // Object object = redisDBOperation.redisTemplate.opsForHash().get(key, providerUserId);
        Object object = redisDBOperation.codisService.hget(key, providerUserId);
        if (object == null)
            return null;
        return JSONObject.parseObject((String) object, AccountMapData.class);
    }

    /*
        ----------
        faceBook绑定账号
        ----------
     */
    public static void insertFacebookAccountMapData(String providerUserId, AccountMapData accountMapData) {

        String key = Path.getFacebookAccountMapDataCollectionPath();
        String jsonString = JSONObject.toJSONString(accountMapData);

        // redisDBOperation.redisTemplate.opsForHash().put(key, providerUserId, jsonString);
        redisDBOperation.codisService.hset(key, providerUserId, jsonString);
    }

    public static AccountMapData selectFacebookAccountMapData(String providerUserId) {

        String key = Path.getFacebookAccountMapDataCollectionPath();
        // String jsonString = (String) redisDBOperation.redisTemplate.opsForHash().get(key, providerUserId);
        String jsonString = redisDBOperation.codisService.hget(key, providerUserId);
        if (jsonString == null)
            return null;
        return JSONObject.parseObject(jsonString, AccountMapData.class);
    }

    /*
       ----------
       玩家头像
       ----------
    */
    public static void insertUserProfileImageCollection(String imageBase64, String userUid) {

        String key = Path.getUserProfileImageCollectionPath();
        // redisDBOperation.redisTemplate.opsForHash().put(key, userUid, imageBase64);
        redisDBOperation.codisService.hset(key, userUid, imageBase64);
    }

    /**
     * 将玩家请求储存，等到有相同请求则直接返回
     */
    public static void insertUserArchiveServerResponse(String userUid, ServerResponseArchiveData archiveData) {

        String key = Path.getUserServerResponseArchiveCollectionPath(userUid);
        // redisDBOperation.redisTemplate.opsForHash().put(key, archiveData.getRequestId().toString(), archiveData);
        throw new RuntimeException("功能取消");
    }

    public static ServerResponseArchiveData selectNewUserArchiveServerResponse(String userUid) {

        // String key = Path.getUserServerResponseArchiveCollectionPath(userUid);
        // Set keySet = redisDBOperation.redisTemplate.opsForHash().keys(key);
        // List keyList = Lists.newArrayList(keySet);
        // Collections.reverse(keyList);
        // if (keyList.size() == 0) {
        //     return null;
        // }
        //
        // String requestId = keyList.get(0).toString();
        //
        // String jsonString = (String) redisDBOperation.redisTemplate.opsForHash().get(key, requestId);
        // if (jsonString == null)
        //     return null;
        // return JSONObject.parseObject(jsonString, ServerResponseArchiveData.class);

        throw new RuntimeException("功能取消");
    }

    public static LoginSessionData selectUserLoginSessionDataDocPath(String userUid) {

        String key = Path.getUserLoginSessionDataDocPath(userUid);
        // String jsonString = (String) redisDBOperation.redisTemplate.opsForValue().get(key);
        String jsonString = redisDBOperation.codisService.get(key);
        if (jsonString == null)
            return null;
        return JSONObject.parseObject(jsonString, LoginSessionData.class);
    }

    public static void insertUserLoginSessionDataDocPath(String userUid, LoginSessionData loginSessionData) {

        String key = Path.getUserLoginSessionDataDocPath(userUid);
        // redisDBOperation.redisTemplate.opsForValue().set(key, JSON.toJSONString(loginSessionData));
        redisDBOperation.codisService.set(key, JSON.toJSONString(loginSessionData));
    }


    /*
       ----------
       玩家邮件
       ----------
    */
    public static void insertUserInboxMail(String userUid, MailData mailData) {

        //邮件索引
        String mailPath = Path.getInboxMailCollectionPath();
        String jsonString = JSONObject.toJSONString(mailData);
        // redisDBOperation.redisTemplate.opsForHash().put(mailPath, mailData.getUid(), jsonString);
        redisDBOperation.codisService.hset(mailPath, mailData.getUid(), jsonString);

        //用户索引
        String path = Path.getUserInboxMailCollectionPath();
        String key = path + ":" + userUid;
        // redisDBOperation.redisTemplate.opsForList().leftPush(key, mailData.getUid());
        redisDBOperation.codisService.leftPushAll(key, mailData.getUid());
    }


    /**
     * 删除旧邮件
     *
     * @param userUid
     * @return
     */
    public static MailData deleteRedundancyMail(String userUid) {

        String path = Path.getUserInboxMailCollectionPath();
        String key = path + ":" + userUid;
        // String mailKey = (String) redisDBOperation.redisTemplate.opsForList().rightPop(key);
        String mailKey = redisDBOperation.codisService.rpop(key);

        //邮件索引
        String mailPath = Path.getInboxMailCollectionPath();
        // String jsonString = (String) redisDBOperation.redisTemplate.opsForHash().get(mailPath, mailKey);
        String jsonString = redisDBOperation.codisService.hget(mailPath, mailKey);
        // redisDBOperation.redisTemplate.opsForHash().delete(mailPath, mailKey);
        redisDBOperation.codisService.hdel(mailPath, mailKey);
        MailData mailData = JSONObject.parseObject(jsonString, MailData.class);
        return mailData;
    }

    public static void deleteMail(String userUid, String mailUid) {

        //用户邮件索引删除
        String path = Path.getUserInboxMailCollectionPath();
        String key = path + ":" + userUid;
        // redisDBOperation.redisTemplate.opsForList().remove(key, 1, mailUid);
        redisDBOperation.codisService.lrem(key, 1, mailUid);

        //邮件索引删除
        String mailPath = Path.getInboxMailCollectionPath();
        // redisDBOperation.redisTemplate.opsForHash().delete(mailPath, mailUid);
        redisDBOperation.codisService.hdel(mailPath, mailUid);
    }


    public static MailData selectInboxMail(String mailUid) {

        String path = Path.getInboxMailCollectionPath();

        // String jsonString = (String) redisDBOperation.redisTemplate.opsForHash().get(path, mailUid);
        String jsonString = redisDBOperation.codisService.hget(path, mailUid);
        if (jsonString == null) {
            throw new BusinessException("邮件" + mailUid + "不存在");
        }

        return JSONObject.parseObject(jsonString, MailData.class);
    }

    public static Long selectInboxMailLength(String userUid) {

        String path = Path.getUserInboxMailCollectionPath();
        String key = path + ":" + userUid;
        if (!checkKeyExist(path)) {
            log.info("该玩家没有邮件" + userUid);
            return 0L;
        }
        // return redisDBOperation.redisTemplate.opsForList().size(key);
        return redisDBOperation.codisService.llen(key);
    }

    /**
     * 获取玩家当前所有的邮件
     *
     * @param userUid
     * @return
     */
    public static List<MailData> selectUserMailData(String userUid) {

        String path = Path.getUserInboxMailCollectionPath();
        String mailPath = Path.getInboxMailCollectionPath();
        String key = path + ":" + userUid;
        // List<Object> list = redisDBOperation.redisTemplate.opsForList().range(key, 0, -1);
        List<String> list = redisDBOperation.codisService.lrange(key, 0, -1);
        List<MailData> mailDataList = new ArrayList<>();
        if (list != null) {
            for (Object obj : list) {
                // Object o = redisDBOperation.redisTemplate.opsForHash().get(mailPath, obj.toString());
                Object o = redisDBOperation.codisService.hget(mailPath, obj.toString());
                if (o != null) {
                    MailData mailData = JSONObject.parseObject(o.toString(), MailData.class);
                    mailDataList.add(mailData);
                }

            }
        }

        return mailDataList;
    }

    /**
     * 邮件存档
     *
     * @param mailUid
     * @param archivedMailData
     */
    public static void insertArchivedMailData(String mailUid, ArchivedMailData archivedMailData) {

        String path = Path.getUserArchivedMailCollectionPath();

        String jsonString = JSONObject.toJSONString(archivedMailData);
        String zipString = DeflaterUtils.zipString(jsonString);
        // redisDBOperation.redisTemplate.opsForHash().put(path, mailUid, zipString);
        redisDBOperation.codisService.hset(path, mailUid, zipString);
    }
    /*
        ----------
        基础操作相关
        ----------
     */

    /**
     * 在redis中检查key是否存在
     *
     * @param key
     * @return
     */
    public static boolean checkKeyExist(String key) {
        // return redisDBOperation.redisTemplate.hasKey(key);
        return redisDBOperation.codisService.hasKey(key);
    }

    /**
     * 批量获取
     *
     * @param list
     * @return
     */
    public static List<String> MultipleGet(List<String> list) {

        // return redisDBOperation.redisTemplate.opsForValue().multiGet(list);
        return redisDBOperation.codisService.multiGet(list);
    }

    /**
     * 批量删除
     *
     * @param list
     */
    public static void MultipleDelete(Set<String> list) {

        // redisDBOperation.redisTemplate.delete(list);
        redisDBOperation.codisService.del(list);

    }

    public static void Delete(String key) {
        // redisDBOperation.redisTemplate.delete(key);
        redisDBOperation.codisService.del(key);
    }

    /**
     * 游标查找
     * "*" + matchKey + "*"
     *
     * @param matchKey
     * @return
     */
    public static Set<String> scan(String matchKey) {

        // Set<String> keys = redisDBOperation.redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
        //     Set<String> keysTmp = new HashSet<>();
        //     Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(matchKey).count(1000).build());
        //     while (cursor.hasNext()) {
        //         keysTmp.add(new String(cursor.next()));
        //     }
        //     //关闭scan
        //     cursor.close();
        //     return keysTmp;
        // });
        //
        // return keys;
        throw new RuntimeException("功能取消");
    }


    private RedisConnectionFactory connectionFactory;

    /**
     * 自增序列（有过期时间）
     *
     * @param key      自增key
     * @param liveTime 存活时间(秒)
     * @return
     */
    public Long incr(String key, long liveTime) {
        // if (null == connectionFactory) {
        //     connectionFactory = redisDBOperation.redisTemplate.getConnectionFactory();
        // }
        // assert connectionFactory != null;
        // RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, connectionFactory);
        // long increment = entityIdCounter.getAndIncrement();
        // if (increment == 0 && liveTime > 0) {
        //     //初始设置过期时间
        //     entityIdCounter.expire(liveTime, TimeUnit.SECONDS);
        // }
        // return increment;
        throw new RuntimeException("功能取消");
    }

    /**
     * 自增序列（可设置初始值）
     *
     * @param key   自增key
     * @param value 初始值
     * @return
     * @description 若自增key已存在，并且初始值必须要大于生成的值，以免造成序列冲突
     */
    public void setIncr(String key, Long value) {
        // if (value < 0) {
        //     value = 1L;
        // }
        // if (null == connectionFactory) {
        //     connectionFactory = redisDBOperation.redisTemplate.getConnectionFactory();
        // }
        // assert connectionFactory != null;
        // RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, connectionFactory);
        // entityIdCounter.set(value);

        throw new RuntimeException("功能取消");
    }

    public Long getIncr(String key) {
        // if (null == connectionFactory) {
        //     connectionFactory = redisDBOperation.redisTemplate.getConnectionFactory();
        // }
        // assert connectionFactory != null;
        // RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, connectionFactory);
        // return entityIdCounter.getAndIncrement();

        Long result = codisService.incrKey(key);
        return result;
    }


}
