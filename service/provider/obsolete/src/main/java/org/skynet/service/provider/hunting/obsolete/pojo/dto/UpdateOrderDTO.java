package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UpdateGUNDTO对象", description = "更新订单接收对象")
public class UpdateOrderDTO extends BaseDTO {

    @ApiModelProperty("订单号")
    private String customOrderId;
}
