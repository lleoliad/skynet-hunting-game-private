package org.skynet.service.provider.hunting.obsolete.pojo.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "LocalPlayerCalculateAIFilterParametersBO对象", description = "玩家先手返回AI过滤器参数")
public class LocalPlayerCalculateAIFilterParametersBO {

    @ApiModelProperty(value = "AI精准度范围")
    private Double[] aiShowPrecisionRange;

    @ApiModelProperty(value = "AI分数范围")
    private Integer[] aiScoreRange;

    @ApiModelProperty(value = "安全的AI精准度范围")
    private Double[] secureAIShowPrecisionRange;

    @ApiModelProperty(value = "安全的AI分数范围")
    private Integer[] secureAIScoreRange;
}
