package org.skynet.service.provider.hunting.obsolete.pojo.table;

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
@ApiModel(value = "AiFirstAiRecordChooseRule对象", description = "AI先手规则")
public class AiFirstAiRecordChooseRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "AI过滤设置集合")
    private AiFilterConfig[] aiFilterConfigs;

    @ApiModelProperty(value = "安全的AI过滤设置")
    private AiFilterConfig secureAIFilterConfig;
}
