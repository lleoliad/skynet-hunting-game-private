package org.skynet.service.provider.hunting.obsolete.pojo.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "LocalPlayerInAiFilterConfigBO对象", description = "根据AiFilterConfig的配置,计算出AI的显示准确度区间和AI分数区间")
public class LocalPlayerInAiFilterConfigBO {

    @ApiModelProperty(value = "AI精准度范围")
    private Double[] aiShowPrecisionRange;

    @ApiModelProperty(value = "AI分数范围")
    private Integer[] aiScoreRange;
}
