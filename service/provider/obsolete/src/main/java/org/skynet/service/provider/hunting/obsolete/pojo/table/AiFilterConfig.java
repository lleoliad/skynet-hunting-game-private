package org.skynet.service.provider.hunting.obsolete.pojo.table;

import org.skynet.service.provider.hunting.obsolete.common.util.RangeFloat;
import org.skynet.service.provider.hunting.obsolete.enums.ComparisonType;
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
@ApiModel(value = "AiFilterConfig对象", description = "AI过滤设置")
public class AiFilterConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ai相对于玩家得分的得分期望比例范围")
    private RangeFloat scoreRatioToPlayerExpectationRange;

    @ApiModelProperty(value = "显示精度范围")
    private RangeFloat showPrecisionRange;

    @ApiModelProperty(value = "显示精度和玩家精度对比方式")
    private ComparisonType showPrecisionComparisonTypeToPlayerShowPrecision;

    @ApiModelProperty(value = "该配置权重")
    private Integer weight;

}
