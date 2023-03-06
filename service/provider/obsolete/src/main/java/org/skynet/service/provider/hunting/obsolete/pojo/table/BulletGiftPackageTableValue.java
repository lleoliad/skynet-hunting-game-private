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
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "BulletGiftPackageTableValue对象", description = "子弹礼包表")
public class BulletGiftPackageTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "价格", notes = "1:小型 2:中型 3:大型")
    private Double price;
    @ApiModelProperty(value = "奖励子弹id")
    private List<Integer> rewardBulletId;
    @ApiModelProperty(value = "奖励子弹数量")
    private List<Integer> rewardBulletCount;
    @ApiModelProperty(value = "奖励宝箱类型")
    private Integer rewardChestType;
    @ApiModelProperty(value = "奖励钻石")
    private Integer rewardDiamondCount;
    @ApiModelProperty(value = "第几天可见")
    private Integer visibleDaysAfterSignUp;
    @ApiModelProperty(value = "开启时间")
    private List<Long> enableStandardTime;
    @ApiModelProperty(value = "结束时间")
    private List<Long> disableStandardTime;
    @ApiModelProperty(value = "商品名")
    private String productName;


}
