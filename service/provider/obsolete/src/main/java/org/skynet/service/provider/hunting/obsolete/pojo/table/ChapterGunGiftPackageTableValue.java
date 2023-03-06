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
@ApiModel(value = "ChapterGunGiftPackageTableValue对象", description = "章节奖励数据库表(5-12章)")
@AllArgsConstructor
@NoArgsConstructor
public class ChapterGunGiftPackageTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("礼包商品名称(内购productId)")
    private String productId;
    @ApiModelProperty("礼包商品价格")
    private Double price;
    @ApiModelProperty("有效时长")
    private Long expireTime;
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
    @ApiModelProperty("奖励枪械数量")
    private List<Integer> rewardGunCount;
    @ApiModelProperty("红色Epic枪卡数量（UI显示用）")
    private Integer epicGunCardCount;
    @ApiModelProperty("橙色Rare枪卡数量（UI显示用）")
    private Integer rareGunCardCount;
    @ApiModelProperty("随机Random枪卡数量（UI显示用）")
    private Integer randomGunCardCount;


}
