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
@ApiModel(value = "ReceiptValidateResult对象", description = "收据验证结果")
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptValidateResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "是否订单在验证前已经是已完成状态")
    private Boolean isAlreadyComplete;

    @ApiModelProperty(value = "是否本次验证通过")
    private Boolean isValidPass;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "订单id")
    private String orderId;

    @ApiModelProperty(value = "验证原始Rsp")
    private String validateRawRsp;

}
