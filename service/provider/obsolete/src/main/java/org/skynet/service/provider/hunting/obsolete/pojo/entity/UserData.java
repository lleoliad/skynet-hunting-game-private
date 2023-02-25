package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
@ApiModel(value = "UserData对象", description = "玩家信息")
public class UserData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "行数据id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "玩家的全局唯一id")
    private Long userId;

    @ApiModelProperty(value = "更新次数")
    @TableField(exist = false)
    private Integer updateCount;

    @ApiModelProperty(value = "注册时间")
    @TableField("sign_up_time")
    private Long signUpTime;

    @ApiModelProperty(value = "玩家姓名")
    private String name;

    @ApiModelProperty(value = "玩家的uuid")
    private String uuid;

    @ApiModelProperty(value = "金币数量")
    @TableField(exist = false)
    private Long coin;

    @ApiModelProperty(value = "钻石数量")
    @TableField(exist = false)
    private Long diamond;

    @ApiModelProperty(value = "奖杯数目")
    @TableField(exist = false)
    private Integer trophy;

    @ApiModelProperty(value = "玩家引导数据")
    @TableField(exist = false)
    private PlayerTutorialData tutorialData;

    @ApiModelProperty(value = "对应章节获得的奖杯数目")
    @TableField(exist = false)
    private Map<Integer, Integer> chapterWinTrophyCountMap;

    @ApiModelProperty(value = "未解锁的章节")
    @TableField(exist = false)
    private List<Integer> unlockedChapterIds;

    @ApiModelProperty(value = "章节开始次数")
    @TableField(exist = false)
    private Map<Integer, Integer> chapterEnteredCountMap;

    @ApiModelProperty(value = "章节完成次数")
    @TableField(exist = false)
    private Map<Integer, Integer> chapterCompletedCountMap;

    @ApiModelProperty(value = "章节胜利次数")
    @TableField(exist = false)
    private Map<Integer, Integer> chapterWinCountMap;

    @ApiModelProperty(value = "装备的枪械ID")
    @TableField(exist = false)
    private Integer equippedGunId;

    @ApiModelProperty(value = "枪械的等级")
    @TableField(exist = false)
    private Map<Integer, Integer> gunLevelMap;

    @ApiModelProperty(value = "枪械的数量")
    @TableField(exist = false)
    private Map<Integer, Integer> gunCountMap;

    @ApiModelProperty(value = "装备的子弹ID")
    @TableField(exist = false)
    private Integer equippedBulletId;

    @ApiModelProperty(value = "子弹数量")
    @TableField(exist = false)
    private Map<Integer, Integer> bulletCountMap;

    @ApiModelProperty(value = "章节胜利宝箱")
    @TableField(exist = false)
    private List<ChapterWinChestData> chapterWinChestsData;

    @ApiModelProperty(value = "免费箱子数据")
    @TableField(exist = false)
    private FreeChestData[] freeChestsData;

    @ApiModelProperty(value = "章节礼包数据")
    @TableField(exist = false)
    private List<ChapterBonusPackageData> chapterBonusPackagesData;

    @ApiModelProperty(value = "活动礼包数据")
    @TableField(exist = false)
    private List<PromotionEventPackageData> promotionEventPackagesData;

    @ApiModelProperty(value = "活动礼包V2数据")
    @TableField(exist = false)
    private List<PromotionGiftPackageV2Data> promotionGiftPackagesV2Data;

    @ApiModelProperty(value = "正在购买的商品名称")
    @TableField(exist = false)
    private List<UserPendingPurchaseData> iapPendingPurchaseProductsData;

    @ApiModelProperty(value = "每种内购完成的订单个数")
    @TableField(exist = false)
    private Map<String, Integer> iapProductPurchasedCountMap;

    @ApiModelProperty(value = "需要播放解锁动画的章节id")
    @TableField(exist = false)
    private Integer pendingUnlockAnimationChapterId;

    @ApiModelProperty(value = "已经绑定的鉴权平台")
    @TableField(exist = false)
    private LinkedAuthProviderData linkedAuthProviderData;

    @ApiModelProperty(value = "不向客户端发送的数据")
    @TableField(exist = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ServerOnly serverOnly = new ServerOnly(null,
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
            -1,
            -1);


    @ApiModelProperty(value = "历史数据")
    @TableField(exist = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private History history = new History(null,
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

    @ApiModelProperty(value = "成就数据")
    @TableField(exist = false)
    List<AchievementData> achievements;

    @ApiModelProperty(value = "幸运转盘数据")
    @TableField(exist = false)
    private LuckyWheelData luckyWheelData;

    @ApiModelProperty(value = "第二版转盘玩家数据")
    @TableField(exist = false)
    private LuckyWheelV2Data luckyWheelV2Data;

    @ApiModelProperty(value = "广告数据")
    @TableField(exist = false)
    private PlayerAdvertisementData advertisementData;

    @ApiModelProperty(value = "vip数据, 1.0.10之后新注册用户不再有该值")
    @TableField(exist = false)
    private PlayerVipData vipData;

    @ApiModelProperty(value = "vipV2数据")
    @TableField(exist = false)
    private PlayerVipV2Data vipV2Data;

    @ApiModelProperty(value = "vipV3数据")
    @TableField(exist = false)
    private PlayerVipV3Data vipV3Data;

    @ApiModelProperty(value = "玩家段位系统")
    @TableField(exist = false)
    private PlayerRankData playerRankData;

    @ApiModelProperty(value = "是否有未领取的段位奖励")
    @TableField(exist = false)
    private Boolean haveNotObtainRankRewardChest;

    @ApiModelProperty(value = "可用的子弹礼包数据")
    @TableField(exist = false)
    List<PlayerBulletGiftPackageData> availableBulletGiftPackageData;

    @ApiModelProperty(value = "可用的五日枪械礼包数据")
    @TableField(exist = false)
    List<PlayerFifthDayGunGiftPackageData> availableFifthDayGunGiftPackageData;

    @ApiModelProperty(value = "可用的枪械礼包数据")
    @TableField(exist = false)
    List<PlayerGunGiftPackageData> availableGunGiftPackageData;

    @ApiModelProperty(value = "是否在战斗服创建信息")
    @TableField(exist = false)
    private Boolean isCreateBattleInfo;

}
