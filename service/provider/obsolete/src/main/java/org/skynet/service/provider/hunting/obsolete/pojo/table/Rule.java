package org.skynet.service.provider.hunting.obsolete.pojo.table;

import org.skynet.service.provider.hunting.obsolete.common.util.RangeFloat;
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
@ApiModel(value = "Rule对象", description = "AI操作选择匹配规则")
public class Rule implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "玩家表现精准度")
    private RangeFloat playerShowPrecision;

    @ApiModelProperty(value = "AI过滤设置集合")
    private AiFilterConfig[] aiFilterConfigs;

    @ApiModelProperty(value = "安全的AI过滤设置")
    private AiFilterConfig secureAIFilterConfig;
}
