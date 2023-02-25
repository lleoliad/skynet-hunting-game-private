package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "RemoveFailureDTO对象", description = "清除内购失败记录接收对象")
public class RemoveFailureDTO extends BaseDTO {

    @ApiModelProperty(value = "产品名称")
    private String productName;
}
