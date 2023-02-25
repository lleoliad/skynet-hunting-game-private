package org.skynet.service.provider.hunting.obsolete.pojo.table;


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
@ApiModel(value = "PendingPurchaseOrder对象", description = "待定购买订单")
public class PendingPurchaseOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品订单id")
    private String customOrderId;

    @ApiModelProperty(value = "玩家uid")
    private String playerUid;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    @ApiModelProperty(value = "平台订单id")
    private String platformOrderId;

    @ApiModelProperty(value = "透传附加json参数")
    private String additionalParametersJSON;
}
