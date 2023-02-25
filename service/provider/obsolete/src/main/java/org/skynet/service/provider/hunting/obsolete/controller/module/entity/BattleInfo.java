package org.skynet.service.provider.hunting.obsolete.controller.module.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "BattleInfo对象", description = "存储用户战斗信息")
@EqualsAndHashCode(callSuper = false)
public class BattleInfo {

    @ApiModelProperty(value = "玩家奖杯数")
    private Integer trophyCount;
    @ApiModelProperty(value = "玩家连胜次数")
    private Integer winningStreak;
    @ApiModelProperty(value = "玩家连败次数")
    private Integer losingStreak;
    @ApiModelProperty(value = "玩家胜率")
    private Double winningPercentage;
    @ApiModelProperty(value = "玩家精度")
    private Double playerAveragePrecision;
    @ApiModelProperty(value = "玩家得分")
    private Integer playerScore;
    @ApiModelProperty(value = "总战斗胜利次数")
    private Integer totalWinCount;
    @ApiModelProperty(value = "总战斗失败次数")
    private Integer totalLoseCount;
    @ApiModelProperty(value = "总战斗次数")
    private Integer totalBattleCount;

}
