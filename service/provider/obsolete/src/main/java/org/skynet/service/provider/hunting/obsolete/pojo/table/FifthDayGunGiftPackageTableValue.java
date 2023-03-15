package org.skynet.service.provider.hunting.obsolete.pojo.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "FifthDayGunGiftPackageGroupTable对象", description = "五日武器礼包")
public class FifthDayGunGiftPackageTableValue implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("奖励宝箱类型")
    private Integer rewardChestType;
    @ApiModelProperty("宝箱等级")
    private Integer rewardChestLevel;
    @ApiModelProperty("奖励金币(如果为0，则显示子弹)")
    private Integer rewardCoinCount;
    @ApiModelProperty("奖励钻石")
    private Integer rewardDiamondCount;
    @ApiModelProperty("奖励子弹id")
    private List<Integer> rewardBulletId;
    @ApiModelProperty("奖励子弹数量")
    private List<Integer> rewardBulletCount;
    @ApiModelProperty("奖励枪械id")
    private List<Integer> rewardGunId;

    @ApiModelProperty("奖励枪械库类型")
    private List<Integer> rewardGunLibraryTypes;
    @ApiModelProperty("奖励枪械数量")
    private List<Integer> rewardGunCount;
    @ApiModelProperty("红色Epic枪卡数量")
    private Integer epicGunCardCount;
    @ApiModelProperty("橙色Rare枪卡数量")
    private Integer rareGunCardCount;
    @ApiModelProperty("随机Random枪卡数量")
    private Integer randomGunCardCount;


}
