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
@ApiModel(value = "AiControlRecordFilterParameters对象", description = "AI操作录制文件过滤参数,使用该参数向服务器查询过滤需要的录制文件")
public class AiControlRecordFilterParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "匹配AI操作查询")
    private MatchAiControlQuery originQuery;

    @ApiModelProperty(value = "AI分数范围")
    private Integer[] aiScoreRange;

    @ApiModelProperty(value = "AI精准度范围")
    private Double[] aiShowPrecisionRange;

    @ApiModelProperty(value = "安全AI分数范围")
    private Integer[] secureAIScoreRange;

    @ApiModelProperty(value = "安全AI分数精准度范围")
    private Double[] secureAIShowPrecisionRange;
}
