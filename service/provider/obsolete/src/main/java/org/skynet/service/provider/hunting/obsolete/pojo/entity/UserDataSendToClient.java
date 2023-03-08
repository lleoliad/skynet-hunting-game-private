package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UserDataSendToClient对象", description = "返回客户端的用户数据")
public class UserDataSendToClient implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "更新次数")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer updateCount;

    @ApiModelProperty(value = "玩家姓名")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    @ApiModelProperty(value = "玩家的uuid")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uuid;

    @ApiModelProperty(value = "金币数量")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long coin;

    @ApiModelProperty(value = "钻石数量")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long diamond;

    @ApiModelProperty(value = "奖杯数目")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer trophy;

    @ApiModelProperty(value = "玩家引导数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PlayerTutorialData tutorialData;

    @ApiModelProperty(value = "对应章节获得的奖杯数目")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<Integer, Integer> chapterWinTrophyCountMap;

    @ApiModelProperty(value = "未解锁的章节")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Integer> unlockedChapterIds;

    @ApiModelProperty(value = "章节开始次数")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<Integer, Integer> chapterEnteredCountMap;

    @ApiModelProperty(value = "章节完成次数")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<Integer, Integer> chapterCompletedCountMap;

    @ApiModelProperty(value = "章节胜利次数")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<Integer, Integer> chapterWinCountMap;

    @ApiModelProperty(value = "装备的枪械ID")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer equippedGunId;

    @ApiModelProperty(value = "枪械的等级")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<Integer, Integer> gunLevelMap;

    @ApiModelProperty(value = "枪械的数量")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<Integer, Integer> gunCountMap;

    @ApiModelProperty(value = "装备的子弹ID")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer equippedBulletId;

    @ApiModelProperty(value = "子弹数量")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<Integer, Integer> bulletCountMap;

    @ApiModelProperty(value = "章节胜利宝箱")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ChapterWinChestData> chapterWinChestsData;

    @ApiModelProperty(value = "免费箱子数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FreeChestData[] freeChestsData;

    @ApiModelProperty(value = "章节礼包数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ChapterBonusPackageData> chapterBonusPackagesData;

    @ApiModelProperty(value = "活动礼包数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PromotionEventPackageData> promotionEventPackagesData;

    @ApiModelProperty(value = "活动礼包V2数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PromotionGiftPackageV2Data> promotionGiftPackagesV2Data;

    @ApiModelProperty(value = "正在购买的商品条目")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<UserPendingPurchaseData> iapPendingPurchaseProductsData;

    @ApiModelProperty(value = "上一次购买金币礼包的时间")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long lastPurchaseCoinBonusPackageTime;

    @ApiModelProperty(value = "需要播放解锁动画的章节id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer pendingUnlockAnimationChapterId;

    @ApiModelProperty(value = "每种内购完成的订单个数")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Integer> iapProductPurchasedCountMap;

    @ApiModelProperty(value = "历史数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private History history;

    @ApiModelProperty(value = "成就数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<AchievementData> achievements;

    @ApiModelProperty(value = "幸运转盘")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LuckyWheelData luckyWheelData;

    @ApiModelProperty(value = "幸运转盘V2")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LuckyWheelV2Data luckyWheelV2Data;

    @ApiModelProperty(value = "玩家广告数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PlayerAdvertisementData advertisementData;

    @ApiModelProperty(value = "用户vip数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PlayerVipData vipData;

    @ApiModelProperty(value = "用户vip v2数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PlayerVipV2Data vipV2Data;

    @ApiModelProperty(value = "用户vip v3数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PlayerVipV3Data vipV3Data;

    @ApiModelProperty(value = "玩家段位系统")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    // private PlayerRankData playerRankData;
    private Object playerRankData;

    @ApiModelProperty(value = "是否有未领取的段位奖励")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean haveNotObtainRankRewardChest;

    @ApiModelProperty(value = "未领取的段位宝箱类型")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer notObtainRankRewardChestType;

    @ApiModelProperty(value = "已经绑定的鉴权平台")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LinkedAuthProviderData linkedAuthProviderData;

    @ApiModelProperty(value = "玩家子弹礼包数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PlayerBulletGiftPackageData> availableBulletGiftPackageData;

    @ApiModelProperty(value = "玩家五日枪械礼包数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PlayerFifthDayGunGiftPackageData> availableFifthDayGunGiftPackageData;

    @ApiModelProperty(value = "玩家枪械礼包数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PlayerGunGiftPackageData> availableGunGiftPackageData;


    @ApiModelProperty(value = "注册时间")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long signUpTime;


}
