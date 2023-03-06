package org.skynet.service.provider.hunting.obsolete.pojo.environment;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.exception.Assert;
import org.skynet.service.provider.hunting.obsolete.common.result.ResponseEnum;
import org.skynet.service.provider.hunting.obsolete.common.util.DeflaterUtils;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.enums.Table;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import com.cn.huntingrivalserver.pojo.table.*;
import org.skynet.service.provider.hunting.obsolete.service.GameResourceService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.skynet.service.provider.hunting.obsolete.pojo.table.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
@ApiModel(value = "Environment对象", description = "系统环境信息")
@Slf4j
public class GameEnvironment {

    @ApiModelProperty("部分接口请求次数统计map")
    public static Map<String, List<Long>> timeMessage = new LinkedHashMap<>();

    @ApiModelProperty(value = "当前环境变量")
    private static GameEnvironment gameEnvironment;

    @ApiModelProperty(value = "需要发送给客户端的数据")
    public static UserDataSendToClient _sendToClientUserData = null;

    // @ApiModelProperty("所有的游戏版本")
    // public static final List<String> tableVersion = Arrays.asList("1.0.0","1.0.1","1.0.2","1.0.3","1.0.4","1.0.5","1.0.6","1.0.7","1.0.9","1.0.10","1.0.11","1.0.12","1.0.13");

    @ApiModelProperty("系统当前成就数据库")
    public static Map<String, Map<String, AchievementTableValue>> achievementTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前怪物血量数据库")
    public static Map<String, Map<String, AnimalTableValue>> animalTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前章节奖励数据库(2,3,4章)")
    public static Map<String, Map<String, ChapterBonusPackageTableValue>> chapterBonusPackageTableMap = new ConcurrentHashMap<>();


    @ApiModelProperty("系统当前章节奖励数据库(5-12章)")
    public static Map<String, Map<String, ChapterGunGiftPackageTableValue>> chapterGunGiftPackageTableMap = new ConcurrentHashMap<>();


    @ApiModelProperty("系统当前章节任务数据库")
    public static Map<String, Map<String, ChapterChestTaskTableValue>> chapterChestTaskTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前单个章节的属性信息数据库")
    public static Map<String, Map<String, ChapterTableValue>> chapterTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前宝箱内容数据库")
    public static Map<String, Map<String, ChestContentMapTableValue>> chestContentMapTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前宝箱枪数据库表")
    public static Map<String, Map<String, ChestGunTableValue>> chestGunTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前匹配表数据库")
    public static Map<String, Map<String, MatchTableValue>> matchTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前促销活动礼包组数据库")
    public static Map<String, Map<String, PromotionEventPackageGroupTableValue>> promotionEventPackageGroupTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前促销活动礼包数据库")
    public static Map<String, Map<String, PromotionEventPackageTableValue>> promotionEventPackageTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前促销活动V2礼包组数据库")
    public static Map<String, Map<String, PromotionEventPackageGroupV2TableValue>> promotionEventPackageGroupV2TableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前促销活动V2礼包数据库")
    public static Map<String, Map<String, PromotionEventPackageV2TableValue>> promotionEventPackageV2TableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前促销活动礼包V2武器礼包数据")
    public static Map<String, Map<String, PromotionEventGunGiftPackageV2TableValue>> promotionEventGunGiftPackageV2TableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前促销活动礼包数据库")
    public static Map<String, Map<String, SigninDiamondRewardTableValue>> signinDiamondRewardTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前的枪数据库")
    public static Map<String, Map<String, GunTableValue>> gunTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前的子弹数据库")
    public static Map<String, Map<String, BulletTableValue>> bulletTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("宝箱子弹数据库表")
    public static Map<String, Map<String, ChestBulletTableValue>> chestBulletTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("商店钻石数据库表内容")
    public static Map<String, Map<String, ShopDiamondTableValue>> shopDiamondTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("系统当前的子弹数据库")
    public static Map<String, Map<String, MatchWinRateTableValue>> matchWinRateTableValueMap = new ConcurrentHashMap<>();

    @ApiModelProperty("商店金币数据库表内容")
    public static Map<String, Map<String, ShopCoinTableValue>> shopCoinTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("宝箱钻石和金币数据库内容")
    public static Map<String, Map<String, ChestCoinDiamondTableValue>> chestCoinDiamondTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("商店宝箱数据库内容")
    public static Map<String, Map<String, ShopChestsTableValue>> shopChestsTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("商店金币礼包数据库")
    public static Map<String, Map<String, CoinBonusPackageTableValue>> coinBonusPackageTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("枪升级内容数据库表")
    public static Map<String, Map<String, GunUpgradeCountTableValue>> gunUpgradeCountTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("Ai武器组合数据库")
    public static Map<String, Map<String, AiWeaponCombinationTableValue>> aiWeaponCombinationTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("AI匹配局数规则数据库")
    public static Map<String, Map<String, MatchAIRoundRuleTableValue>> matchAIRoundRuleTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("记录模式匹配数据库表")
    public static Map<String, Map<String, RecordModeMatchTableValue>> recordModeMatchTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("幸运转盘数据库表")
    public static Map<String, LuckyWheelPropertyTable> luckyWheelPropertyTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("幸运转盘索引表")
    public static Map<String, Map<String, LuckyWheelRewardTableValue>> luckyWheelRewardTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("幸运转盘奖励表")
    public static Map<String, Map<String, LuckyWheelSectorContentTableValue>> luckyWheelSectorContentTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty(value = "系统当前所有玩家")
    public static final Map<String, UserData> userDataMap = new ConcurrentHashMap<>();

    @ApiModelProperty(value = "所有在线玩家，key是用户uid，value是用户上线时间")
    public static final Map<String, Date> onlineUser = new ConcurrentHashMap<>();

    @ApiModelProperty(value = "matchId和routeId的对应关系表")
    public static Map<String, Long[]> matchToRouteUidMap = new HashMap<>();

    @ApiModelProperty("子弹礼包数据库")
    public static Map<String, Map<String, BulletGiftPackageTableValue>> bulletGiftPackageTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("宝箱数据库")
    public static Map<String, Map<String, ChestTableValue>> chestTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("武器礼包数据库")
    public static Map<String, Map<String, GunGiftPackageTableValue>> gunGiftPackageTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("武器礼包Group数据库")
    public static Map<String, Map<String, GunGiftPackageGroupTableValue>> gunGiftPackageGroupTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("五日武器礼包数据库")
    public static Map<String, Map<String, FifthDayGunGiftPackageTableValue>> fifthDayGunGiftPackageTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("五日武器礼包Group数据库")
    public static Map<String, Map<String, FifthDayGunGiftPackageGroupTableValue>> fifthDayGunGiftPackageGroupTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("幸运转盘V2数据库表")
    public static Map<String, LuckyWheelV2PropertyTableValue> luckyWheelV2PropertyTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("幸运转盘V2索引表")
    public static Map<String, Map<String, LuckyWheelV2RewardTableValue>> luckyWheelV2RewardTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("幸运转盘V2奖励表")
    public static Map<String, Map<String, LuckyWheelV2SectorContentTableValue>> luckyWheelV2SectorContentTableMap = new ConcurrentHashMap<>();

    @ApiModelProperty("服务器时间偏移")
    public static long serverTimeOffset = 0;

    @Resource
    private GameResourceService gameResourceService;

    @PostConstruct
    public void init() {
        gameEnvironment = this;
        gameEnvironment.gameResourceService = this.gameResourceService;
    }

    public static UserDataSendToClient prepareSendToClientUserData() {


        _sendToClientUserData = new UserDataSendToClient(null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        return _sendToClientUserData;
    }


    /**
     * 初始化游戏环境
     *
     * @param _event
     * @param systemPropertiesConfig
     * @throws IOException
     */
    public static void init(ServletContextEvent _event, SystemPropertiesConfig systemPropertiesConfig) throws IOException {

        loadDataTable(systemPropertiesConfig.getClientGameVersion());


    }


    /**
     * 读取dataTable信息并插入
     *
     * @param key         数据库表名
     * @param inputStream file的输入流
     */
    public static void InsertDataTable(String key, InputStream inputStream) throws IOException {


        String dataTable = TransformerData(key, inputStream);

        String zipDataTable = DeflaterUtils.zipString(dataTable);

        RedisDBOperation.insertDataTable(key, zipDataTable);

    }

    /**
     * 从文件读取数据库的Json数据
     *
     * @param inputStream 文件内容
     * @return 数据库数据
     */
    public static String TransformerData(String versionNum, InputStream inputStream) throws IOException {

        //检查key是否存在，存在就不插入
        Assert.isTrue(!RedisDBOperation.checkKeyExist(versionNum), ResponseEnum.DATA_EXIST);

        //将inputStream转成String再压缩
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8.name());

        return writer.toString();
    }

    /**
     * 根据versionNum从redis中读取全部表数据
     *
     * @param versionNums 数据库表名
     * @return 数据库内容
     */
    public static void loadDataTable(String[] versionNums) throws IOException {

        for (String versionNum : versionNums) {
            //Table:1.0.9:MatchToRouteUid
            //Table:1.0.7_A:
            Table[] tables = Table.values();
            for (Table table : tables) {
                String osName = System.getProperty("os.name");
                if (osName.equals("Mac OS X")) {
                    String baseDir = "/Users/apple/Documents/storage/gitlab/huntingrival/source/datas";
                    String filename = baseDir + File.separator + versionNum + File.separator + table.getName();
                    if (FileUtil.exist(filename)) {
                        String content = FileUtil.readString(filename, StandardCharsets.UTF_8);
                        gameEnvironment.gameResourceService.inputContent(versionNum, content, table.getName());
                    }
                } else {
                    // /Users/apple/Documents/storage/gitlab/huntingrival/source/datas
                    // String baseDir = "/Users/apple/Documents/storage/gitlab/huntingrival/source/datas";
                    String baseDir = "/data/servers/hunt/moulds";
                    String filename = baseDir + File.separator + versionNum + File.separator + table.getName();
                    if (FileUtil.exist(filename)) {
                        String content = FileUtil.readString(filename, StandardCharsets.UTF_8);
                        gameEnvironment.gameResourceService.inputContent(versionNum, content, table.getName());
                    } else {
                        StringBuffer key = new StringBuffer("Table:" + versionNum);
                        key.append(":");
                        key.append(table.getName());
                        log.info("版本策划数据：{}", key);
                        String zipData = RedisDBOperation.getDataTable(key.toString());
                        if (!StrUtil.isEmptyIfStr(zipData)) {
                            gameEnvironment.gameResourceService.inputGameResource(versionNum, zipData, table.getName());
                        }
                    }
                }


                // key.append(table.getName());
                // log.info("规则表的key{}", key);
                // String zipData = RedisDBOperation.getDataTable(key.toString());
                // if ("1.1.0".equals(versionNum)) {
                //     key.delete(12, key.length());
                // } else {
                //     key.delete(13, key.length());
                // }
                // if (!StrUtil.isEmptyIfStr(zipData)) {
                //     gameEnvironment.gameResourceService.inputGameResource(versionNum, zipData, table.getName());
                // }

                // JSONObject query = new JSONObject();
                // query.put("versionCode", "1.1.0");
                // query.put("name", "CommonPropertyTable.json");
                // query.put("enabled", true);
                // Map<String, Object> mouldData = HttpUtil.getMouldData("http://localhost:17910/skynet/service/provider/hunting/repository/load", query);
                // log.info("{}", mouldData);
            }
        }
    }

    /**
     * 插入ai先手比赛规则
     *
     * @param rulesPath
     * @param inputStream
     * @throws IOException
     */
    public static void addAiFirstAiRecordChooseRules(String rulesPath, InputStream inputStream) throws IOException {

        String recordRules = TransformerData(rulesPath, inputStream);
        String zipDataTable = DeflaterUtils.zipString(recordRules);
        String key = "aiFirstAiRecordChooseRules:" + rulesPath;
        RedisDBOperation.insertAiFirstAiRecordChooseRules(key, zipDataTable);
    }

    /**
     * 向redis插入玩家先手比赛规则
     *
     * @param rulesPath
     * @param inputStream
     * @throws IOException
     */
    public static void addLocalPlayerFirstAiRecordChooseRules(String rulesPath, InputStream inputStream) throws IOException {

        String recordRules = TransformerData(rulesPath, inputStream);
        String zipDataTable = DeflaterUtils.zipString(recordRules);
        String key = "localPlayerFirstAiRecordChooseRules:" + rulesPath;
        RedisDBOperation.insertLocalPlayerFirstAiRecordChooseRules(key, zipDataTable);
    }

    /**
     * 加载玩家先手的ai规则数据
     *
     * @param rulesPath
     * @return
     */
    public static Map<String, LocalPlayerFirstAiRecordChooseRule> loadLocalPlayerFirstAiRecordChooseRules(String rulesPath) throws IOException {


        String zipRecordRules = RedisDBOperation.selectLocalPlayerFirstAiRecordChooseRules(rulesPath);

        //如何数据库里没有数据就抛出异常
        Assert.notEmpty(zipRecordRules, ResponseEnum.DATA_NOT_EXIST);

        //解压
        String unzipDataTable = DeflaterUtils.unzipString(zipRecordRules);


        return JSONObject.parseObject(unzipDataTable, new TypeReference<Map<String, LocalPlayerFirstAiRecordChooseRule>>() {
        });
    }

    public static Map<String, AiFirstAiRecordChooseRule> loadAiFirstAiRecordChooseRules(String rulesPath) throws IOException {

        String zipRecordRules = RedisDBOperation.selectAiFirstAiRecordChooseRules(rulesPath);

        //如何数据库里没有数据就抛出异常
        Assert.notEmpty(zipRecordRules, ResponseEnum.DATA_NOT_EXIST);

        //解压
        String unzipDataTable = DeflaterUtils.unzipString(zipRecordRules);

        return JSONObject.parseObject(unzipDataTable, new TypeReference<Map<String, AiFirstAiRecordChooseRule>>() {
        });
    }

}
