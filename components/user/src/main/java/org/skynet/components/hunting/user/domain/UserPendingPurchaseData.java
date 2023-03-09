package org.skynet.components.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@ApiModel(value = "UserPendingPurchaseData对象", description = "玩家待定购买数据")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserPendingPurchaseData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品订单id")
    private String customOrderId;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty("产品数量")
    private Integer count;

    @ApiModelProperty("产品金额")
    private Integer money;

    public UserPendingPurchaseData(String customOrderId, String productName) {
        this.customOrderId = customOrderId;
        this.productName = productName;
    }
}
