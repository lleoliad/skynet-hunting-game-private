package org.skynet.commons.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@ApiModel(value = "PromotionEventPackageData对象", description = "活动礼包数据")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PromotionEventPackageData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "对应PromotionEventPackage表")
    Integer packageId;

    @ApiModelProperty(value = "内购名")
    String productName;

    @ApiModelProperty(value = "宝箱对象")
    ChestData chestData;

    @ApiModelProperty(value = "过期时间")
    Long expireTime;

    @ApiModelProperty(value = "价格")
    Double price;

    @ApiModelProperty(value = "不向客户端发送的数据")
    String server_only_purchaseKey;
}
