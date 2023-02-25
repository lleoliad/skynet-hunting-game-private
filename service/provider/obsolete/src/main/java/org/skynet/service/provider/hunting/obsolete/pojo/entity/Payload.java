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
@ApiModel(value = "Payload对象", description = "支付细节")
public class Payload implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "包名")
    private String packageName;

    @ApiModelProperty(value = "商品id")
    private String productId;

    @ApiModelProperty(value = "支付token")
    private String purchaseToken;

    @ApiModelProperty(value = "订单id")
    private String orderId;

}