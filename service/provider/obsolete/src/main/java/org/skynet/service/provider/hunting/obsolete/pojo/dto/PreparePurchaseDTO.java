package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@ApiModel(value = "PreparePurchaseDTO对象", description = "记录玩家发起购买前端返回对象")
public class PreparePurchaseDTO extends BaseDTO {

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty("产品数量")
    private Integer count = 1;

    @ApiModelProperty("充值金额")
    private Integer money;

    @ApiModelProperty(value = "透传附加JSON参数")
    private String additionalParametersJSON;
}
