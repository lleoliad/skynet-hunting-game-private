package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "IAPPurchaseReward对象", description = "内购奖励内容")
public class IAPPurchaseReward implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("钻石")
    private int diamond;

    @ApiModelProperty("开箱奖励")
    private ChestOpenResult chestOpenResult;

    @ApiModelProperty("coin")
    private int coin;

    @ApiModelProperty("price")
    private Double price;
}
