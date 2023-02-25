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
@ApiModel(value = "MatchAiRoundRecordFilterParameters对象", description = "AI操作录制文件过滤参数,使用该参数向服务器查询过滤需要的录制文件")
public class MatchAiRoundRecordFilterParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("animalId")
    private Integer animalId;

    @ApiModelProperty("回合选择录制文件情况下，查询参数")
    private MatchAiRoundControlQuery originQuery;

    @ApiModelProperty("AI分数范围")
    private Integer[] aiScoreRange;

    @ApiModelProperty("AI射击精度范围")
    private Double[] aiShowPrecisionRange;

    @ApiModelProperty("安全的AI分数范围")
    private Integer[] secureAIScoreRange;

    @ApiModelProperty("安全的AI射击精度范围")
    private Double[] secureAIShowPrecisionRange;

    @ApiModelProperty("安全失败的原因")
    private String safeFailedReason;
}
