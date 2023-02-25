package org.skynet.service.provider.hunting.obsolete.pojo.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CalculateAIFilterParametersBO对象", description = "返回AI过滤器参数")
public class CalculateAIFilterParametersBO {

    @ApiModelProperty(value = "AI精准度范围")
    private Double[] aiShowPrecisionRange;

    @ApiModelProperty(value = "安全的AI精准度范围")
    private Double[] secureAIShowPrecisionRange;
}
