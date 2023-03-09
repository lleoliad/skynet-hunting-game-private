package org.skynet.components.hunting.user.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.skynet.components.hunting.user.domain.*;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@ApiModel(value="ClientUserData 对象", description="DTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class ClientUserData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "玩家的全局唯一id")
    private Long uid;

    @ApiModelProperty(value = "更新次数")
    private Integer updateCount;

    @ApiModelProperty(value = "注册时间")
    private Long signUpTime;

    @ApiModelProperty(value = "玩家姓名")
    @Indexed(unique = false)
    private String name;

    @ApiModelProperty(value = "玩家的uuid")
    @Indexed(unique = false)
    private String uuid;

    @ApiModelProperty(value = "金币数量")
    private Long coin;

    @ApiModelProperty(value = "钻石数量")
    private Long diamond;

    @ApiModelProperty(value = "奖杯数目")
    private Integer trophy;

    @ApiModelProperty(value = "玩家引导数据")
    private PlayerTutorialData tutorialData;

    @ApiModelProperty(value = "对应章节获得的奖杯数目")
    private Map<Integer, Integer> chapterWinTrophyCountMap;

    @ApiModelProperty(value = "未解锁的章节")
    private List<Integer> unlockedChapterIds;

    @ApiModelProperty(value = "章节开始次数")
    private Map<Integer, Integer> chapterEnteredCountMap;

    @ApiModelProperty(value = "章节完成次数")
    private Map<Integer, Integer> chapterCompletedCountMap;

    @ApiModelProperty(value = "章节胜利次数")
    private Map<Integer, Integer> chapterWinCountMap;

    @ApiModelProperty(value = "装备的枪械ID")
    private Integer equippedGunId;

    @ApiModelProperty(value = "枪械的等级")
    private Map<Integer, Integer> gunLevelMap;

    @ApiModelProperty(value = "枪械的数量")
    private Map<Integer, Integer> gunCountMap;

    @ApiModelProperty(value = "装备的子弹ID")
    private Integer equippedBulletId;

    @ApiModelProperty(value = "子弹数量")
    private Map<Integer, Integer> bulletCountMap;

    @ApiModelProperty(value = "章节胜利宝箱")
    private List<ChapterWinChestData> chapterWinChestsData;

    @ApiModelProperty(value = "免费箱子数据")
    private FreeChestData[] freeChestsData;

    @ApiModelProperty(value = "章节礼包数据")
    private List<ChapterBonusPackageData> chapterBonusPackagesData;

    @ApiModelProperty(value = "活动礼包数据")
    private List<PromotionEventPackageData> promotionEventPackagesData;

    @ApiModelProperty(value = "活动礼包V2数据")
    private List<PromotionGiftPackageV2Data> promotionGiftPackagesV2Data;

    @ApiModelProperty(value = "正在购买的商品名称")
    private List<UserPendingPurchaseData> iapPendingPurchaseProductsData;

    @ApiModelProperty(value = "每种内购完成的订单个数")
    private Map<String, Integer> iapProductPurchasedCountMap;

    @ApiModelProperty(value = "需要播放解锁动画的章节id")
    private Integer pendingUnlockAnimationChapterId;

    @ApiModelProperty(value = "已经绑定的鉴权平台")
    private LinkedAuthProviderData linkedAuthProviderData;

    @ApiModelProperty(value = "不向客户端发送的数据")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ServerOnly serverOnly;

    @ApiModelProperty(value = "历史数据")
    private History history;

    @ApiModelProperty(value = "成就数据")
    List<AchievementData> achievements;

    @ApiModelProperty(value = "幸运转盘数据")
    private LuckyWheelData luckyWheelData;

    @ApiModelProperty(value = "第二版转盘玩家数据")
    private LuckyWheelV2Data luckyWheelV2Data;

    @ApiModelProperty(value = "广告数据")
    private PlayerAdvertisementData advertisementData;

    @ApiModelProperty(value = "vip数据, 1.0.10之后新注册用户不再有该值")
    private PlayerVipData vipData;

    @ApiModelProperty(value = "vipV2数据")
    private PlayerVipV2Data vipV2Data;

    @ApiModelProperty(value = "vipV3数据")
    private PlayerVipV3Data vipV3Data;

    @ApiModelProperty(value = "玩家段位系统")
    // private PlayerRankData playerRankData;
    private Object playerRankData;

    @ApiModelProperty(value = "是否有未领取的段位奖励")
    private Boolean haveNotObtainRankRewardChest;

    @ApiModelProperty(value = "可用的子弹礼包数据")
    List<PlayerBulletGiftPackageData> availableBulletGiftPackageData;

    @ApiModelProperty(value = "可用的五日枪械礼包数据")
    List<PlayerFifthDayGunGiftPackageData> availableFifthDayGunGiftPackageData;

    @ApiModelProperty(value = "可用的枪械礼包数据")
    List<PlayerGunGiftPackageData> availableGunGiftPackageData;

    @ApiModelProperty(value = "是否在战斗服创建信息")
    private Boolean isCreateBattleInfo;

    @ApiModelProperty(value = "最后一次请求时间")
    private long lastRequestTime;
}
