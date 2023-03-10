package org.skynet.components.hunting.user.domain;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@ApiModel(value = "PromotionGiftPackageV2Data对象", description = "第二版活动礼包数据")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PromotionGiftPackageV2Data {

    @ApiModelProperty("礼包id")
    private Integer packageId;

    @ApiModelProperty("礼包group id，对应 PromotionGiftPackageGroupV2Table 表")
    private Integer packageGroupId;

    @ApiModelProperty("礼包过期时间")
    private Long expireTime;

    @ApiModelProperty("礼包类型。1：普通活动礼包，2：枪械活动礼包")
    private Integer packageType;


}
