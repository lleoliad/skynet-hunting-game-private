package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ConfirmHuntingMatchCompleteDTO对象", description = "确认比赛完成的接接收对象")
public class ConfirmHuntingMatchCompleteDTO extends BaseDTO {


    @ApiModelProperty(value = "匹配的uuid")
    private String matchUUID;

    @ApiModelProperty(value = "平均帧率")
    private Integer averageFrameRate;

    @ApiModelProperty(value = "玩家最终的分数")
    private Integer playerFinalScore;

    @ApiModelProperty(value = "ai最终的分数")
    private Integer aiFinalScore;

    @ApiModelProperty(value = "所有编码控制记录数据")
    private List<String> allEncodedControlRecordsData;

    @ApiModelProperty(value = "直接赢")
    private Boolean directlyWin;

    @ApiModelProperty(value = "客户直接进行战斗结算，不处理战报")
    private Boolean playerDirectChangeResult;

    @ApiModelProperty(value = "直接输")
    private Boolean directlyLose;

    @ApiModelProperty(value = "用户更新次数")
    private Integer userDataUpdateCount;

    @ApiModelProperty(value = "回合数")
    private Integer roundCount;

}
