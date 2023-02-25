package org.skynet.service.provider.hunting.obsolete.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "VipConfig对象", description = "Vip设置")
public class VipConfig {

    @ApiModelProperty("vip价格")
    public static final Double vipPrice = 9.99;

    @ApiModelProperty("vip持续时间(天)")
    public static final Integer vipDurationDays = 30;

    @ApiModelProperty("vip增加可同时解锁章节胜利宝箱 数量")
    public static final Integer vipAddUnlockingChapterWinChestSimultaneousCount = 0;

    @ApiModelProperty("每日钻石奖励")
    public static final String vipProductName = "hs_vip";

    @ApiModelProperty("购买钻石奖励")
    public static final Integer vipEachDayDiamondRewardCount = 200;

    @ApiModelProperty("每日vip转盘次数")
    public static final Integer vipPurchaseDiamondRewardCount = 1800;

    @ApiModelProperty("宝箱金币加成")
    public static final Integer vipLuckyWheelVipSpinCount = 5;

    @ApiModelProperty("宝箱金币加成")
    public static final Integer vipChestCoinAmountAddition = 0;

    @ApiModelProperty("宝箱卡片数量加成")
    public static final Integer vipChestCardAmountAddition = 0;

    @ApiModelProperty("svip价格")
    public static final Double svipPrice = 29.99;

    @ApiModelProperty("svip持续时间(天)")
    public static final Integer svipDurationDays = 30;

    @ApiModelProperty("svip增加可同时解锁章节胜利宝箱 数量")
    public static final Integer svipAddUnlockingChapterWinChestSimultaneousCount = 1;

    @ApiModelProperty("svip商品名")
    public static final String svipProductName = "hs_svip";

    @ApiModelProperty("每日钻石奖励")
    public static final Integer svipEachDayDiamondRewardCount = 500;

    @ApiModelProperty("购买钻石奖励")
    public static final Integer svipPurchaseDiamondRewardCount = 6000;

    @ApiModelProperty("每日vip转盘次数")
    public static final Integer svipLuckyWheelVipSpinCount = 15;

    @ApiModelProperty("宝箱金币加成")
    public static final Integer svipChestCoinAmountAddition = 0;

    @ApiModelProperty("宝箱卡片数量加成")
    public static final Integer svipChestCardAmountAddition = 0;

}
