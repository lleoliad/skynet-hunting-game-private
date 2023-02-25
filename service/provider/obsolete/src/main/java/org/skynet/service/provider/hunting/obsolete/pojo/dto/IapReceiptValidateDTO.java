package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@ApiModel(value = "IapReceiptValidateDTO对象", description = "验证内购订单接收对象")
public class IapReceiptValidateDTO extends BaseDTO {


    @ApiModelProperty(value = "收据")
    private String receipt;

    @ApiModelProperty(value = "订单id")
    private String customOrderId;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty("支付相关信息")
    Map<String, Object> cmd;

}
