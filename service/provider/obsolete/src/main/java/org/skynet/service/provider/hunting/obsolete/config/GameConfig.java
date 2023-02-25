package org.skynet.service.provider.hunting.obsolete.config;

import org.skynet.service.provider.hunting.obsolete.common.util.RangeFloat;
import org.skynet.service.provider.hunting.obsolete.enums.ClientGameVersion;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(value = "GameConfig对象", description = "游戏的默认配置")
public class GameConfig {

    @ApiModelProperty(value = "最新的Android客户端版本")
    public static final ClientGameVersion latestClientGameVersion_Android = ClientGameVersion._1_1_0_;

    @ApiModelProperty(value = "最新的IOS客户端版本")
    public static final ClientGameVersion latestClientGameVersion_IOS = null;

    @ApiModelProperty(value = "初始金币数量")
    public static final Long defaultCoinAmount = 1000L;

    @ApiModelProperty(value = "初始钻石数量")
    public static final Long defaultDiamondAmount = 30L;

    @ApiModelProperty(value = "默认解锁章节")
    public static final Integer[] defaultUnlockChapterIDsArray = new Integer[]{1};

    @ApiModelProperty(value = "默认枪械ID")
    public static final Integer defaultGunID = 1;

    @ApiModelProperty(value = "默认子弹ID")
    public static final Integer defaultBulletID = 1;

    @ApiModelProperty(value = "购买子弹的数量")
    public static final Integer[] purchaseBulletCountArray = new Integer[]{9, 45, 180};

    @ApiModelProperty(value = "开启箱子时间允许的误差")
    public static final Integer chestOpenTimeDiffTolerance = 10;

    @ApiModelProperty(value = "匹配最短时长")
    public static final RangeFloat confirmHuntingMatchStartMinElapseTimeRangeArray = new RangeFloat(1d, 5d);

    @ApiModelProperty(value = "玩家名称长度限制")
    public static final Integer playerNameLengthLimit = 16;

    @ApiModelProperty(value = "活动礼包中宝箱最低等级")
    public static final Integer promotionEventPackageMinChestLevel = 3;

    @ApiModelProperty(value = "金币低于该数值,才可以通过观看广告来获取金币")
    public static final Integer canPlayAdsToGetCoinMaxAmount = 300;

    @ApiModelProperty(value = "最多不可重复匹配ai profile的个数")
    public static final Integer maxNotDuplicateMatchingAIProfileCount = 10;

    @ApiModelProperty(value = "匹配时随机名称ai的比例,范围是0-1", notes = "该ai名称是随机,且使用默认头像")
    public static final Double randomNameAiRatioInPlayerMatching = 0.1;

    @ApiModelProperty(value = "ai奖杯相对于玩家变化比例")
    public static final Double aiTrophyToPlayerChangeRatio = 0.2;

    @ApiModelProperty(value = "禁用客户端比赛打点")
    public static final Boolean disableClientHuntingMatchReport = false;

    @ApiModelProperty("标准0点时区偏移值")
    public static final Long standardTimeZoneOffset = -14400L;

    @ApiModelProperty(value = "免费箱子获取时间(第三章之前)")
    public static final Integer shortFreeChestUnlockSeconds = 7200;

    @ApiModelProperty(value = "免费箱子获取时间(第三章之后)")
    public static final Integer longFreeChestUnlockSeconds = 14400;

    @ApiModelProperty(value = "宝箱槽位数量")
    public static final Integer chestSlotAmount = 4;

    @ApiModelProperty(value = "免费宝箱解锁时长")
    public static final Integer freeChestUnlockSeconds = 5;

    @ApiModelProperty(value = "silver宝箱解锁时长")
    public static final Integer silverChestUnlockSeconds = 5;

    @ApiModelProperty(value = "gold宝箱解锁时长")
    public static final Integer goldChestUnlockSeconds = 14400;

    @ApiModelProperty(value = "ruby宝箱解锁时长")
    public static final Integer rubyChestUnlockSeconds = 28800;

    @ApiModelProperty(value = "platinum宝箱解锁时长")
    public static final Integer platinumChestUnlockSeconds = 43200;

    @ApiModelProperty(value = "rare宝箱解锁时长")
    public static final Integer rareChestUnlockSeconds = 57600;

    @ApiModelProperty(value = "epic宝箱解锁时长")
    public static final Integer epicChestUnlockSeconds = 86400;

    @ApiModelProperty(value = "低于该时长的章节胜利宝箱，解锁不需要钻石")
    public static final Integer chapterWinChestFreeUnlockMaxSeconds = 5;

    @ApiModelProperty("广告加速宝箱解锁时间")
    public static final Integer adAccelerateChapterWinChestUnlockSeconds = 3600;

    @ApiModelProperty(value = "录制模式免费的章节")
    public static final Integer[] recordModeFreeFeeChapterIdsArray = new Integer[]{1};

    @ApiModelProperty(value = "整局比赛库可以使用的最少整局数量")
    public static final Integer wholeMatchRecordPoolValidMinRecordCount = 500;

    @ApiModelProperty(value = "有效录像最少帧数要求")
    public static final Integer validRecordMinFrameRateRequires = 25;

    @ApiModelProperty("强制倾向胜利回合匹配连败次数")
    public static final Integer ForceWinProneRoundMatchLoseStreakCount = 3;

    @ApiModelProperty("强制低水平完整匹配连败次数")
    public static final Integer ForceLowLevelWholeMatchLoseStreakCount = 3;

    @ApiModelProperty("强制高水平完整匹配连胜次数")
    public static final Integer ForceHighLevelWholeMatchWinStreakCount = 5;

    @ApiModelProperty("强制倾向于失败的连胜次数")
    public static final Integer ForceLoseProneRoundMatchWinStreakCount = 5;


    @ApiModelProperty("邮箱容量")
    public static final Integer mailInboxCapacity = 50;


    public static final Integer playerHistoricalReportStorageLimit = 50;

    public static final Integer fullRoundMatchingLibraryStorageUpperLimit = 1000;

    public static final Integer fullRoundMatchingLibraryActivationLimits = 500;

    public static final Integer fullRoundMatchingLibraryActivationUpperLimit = 10;

    public static final Integer singleRoundMatchingLibraryStorageUpperLimit = 100;

    public static final Integer singleRoundMatchingLibraryActivationLimits = 10;

    public static final Double[] singleRoundMatchingPrecisionGroupsArray = new Double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};

    @ApiModelProperty("每天可以观看的广告次数上限")
    public static final Integer eachDayWatchRewardAdMaxCount = 20;

    @ApiModelProperty("子弹礼包最低等级")
    public static final Integer bulletGiftPackageMinChestLevel = 3;


}
