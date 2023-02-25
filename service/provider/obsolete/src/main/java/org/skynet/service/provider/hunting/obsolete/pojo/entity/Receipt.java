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
@ApiModel(value = "Receipt对象", description = "")
public class Receipt implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "支付细节")
    private Payload payload;

    @ApiModelProperty(value = "储存")
    private String store;

    @ApiModelProperty("交易id")
    private String transactionID;
}