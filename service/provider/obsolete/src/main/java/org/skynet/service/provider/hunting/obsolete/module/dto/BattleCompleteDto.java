package org.skynet.service.provider.hunting.obsolete.module.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "BattleCompleteDto对象", description = "战斗结束后，前端上传整场战斗的战报信息")
@EqualsAndHashCode(callSuper = false)
public class BattleCompleteDto extends BaseDTO {

    @ApiModelProperty(value = "玩家最终的积分")
    private Integer playerFinalScore;

    @ApiModelProperty(value = "AI最终的积分")
    private Integer aiFinalScore;

    @ApiModelProperty(value = "总回合数")
    private Integer roundCount;

    @ApiModelProperty(value = "所有回合战报数据")
    private List<String> roundReportData;

    @ApiModelProperty(value = "直接赢")
    private Boolean directlyWin;

    @ApiModelProperty(value = "客户直接进行战斗结算，不处理战报")
    private Boolean playerDirectChangeResult;


}
