package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "AIControlRecordDataQueryDTO对象", description = "人工智能控制记录数据查询接收对象")
public class AIControlRecordDataQueryDTO extends BaseDTO {

    @ApiModelProperty(value = "比赛uid")
    private String huntingMatchNowUid;

    @ApiModelProperty(value = "玩家最终分数")
    private Integer playerFinalScore;

    @ApiModelProperty(value = "玩家平均精准度")
    private Double playerAverageShowPrecision;

    @ApiModelProperty(value = "回合")
    private Integer round;

    @ApiModelProperty(value = "怪物routeId")
    private Long animalRouteUid;

    @ApiModelProperty(value = "记录版本")
    private Integer recordVersion;
}
