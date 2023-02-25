package org.skynet.service.provider.hunting.obsolete.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "VipV3Config对象", description = "VipV3设置")
public class VipV3Config {

    @ApiModelProperty("vip功能解锁时间（注册后）")
    public static final Integer unlockVipFunctionAfterSignUpDayCount = 4;

    @ApiModelProperty("vip价格")
    public static final Double vipPrice = 9.99;

    @ApiModelProperty("vip持续时间(天)")
    public static final Integer vipDurationDays = 30;

    @ApiModelProperty("vip增加可同时解锁章节胜利宝箱 数量")
    public static final Integer vipAddUnlockingChapterWinChestSimultaneousCount = 0;

    @ApiModelProperty("vip商品名")
    public static final String vipProductName = "hs_vip_v3";

    @ApiModelProperty("每日钻石奖励")
    public static final Integer vipDailyDiamondRewardCount = 200;

    @ApiModelProperty("购买钻石奖励")
    public static final Integer vipPurchaseDiamondRewardCount = 1800;

    @ApiModelProperty("每日领取子弹id")
    public static final Integer vipDailyRewardBulletId = 3;

    @ApiModelProperty("每日领取子弹数量")
    public static final Integer vipDailyRewardBulletCount = 3;

    @ApiModelProperty("svip价格")
    public static final Double svipPrice = 19.99;

    @ApiModelProperty("svip持续时间(天)")
    public static final Integer svipDurationDays = 30;

    @ApiModelProperty("svip增加可同时解锁章节胜利宝箱 数量")
    public static final Integer svipAddUnlockingChapterWinChestSimultaneousCount = 1;

    @ApiModelProperty("svip商品名")
    public static final String svipProductName = "hs_svip_v3";

    @ApiModelProperty("每日钻石奖励")
    public static final Integer svipDailyDiamondRewardCount = 0;

    @ApiModelProperty("购买钻石奖励")
    public static final Integer svipPurchaseDiamondRewardCount = 4000;

    @ApiModelProperty("每日领取子弹id")
    public static final Integer svipDailyRewardBulletId = 4;

    @ApiModelProperty("每日领取子弹数量")
    public static final Integer svipDailyRewardBulletCount = 2;

    @ApiModelProperty("每日领取橙色枪卡数量")
    public static final Integer svipDailyRewardOrangeGunCount = 22;
}
