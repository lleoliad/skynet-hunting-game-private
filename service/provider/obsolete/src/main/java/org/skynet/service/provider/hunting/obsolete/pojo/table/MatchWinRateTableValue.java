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
@ApiModel(value = "MatchWinRateTableValue对象", description = "比赛胜率数据表")
public class MatchWinRateTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "章节")
    private Integer chapterId;

    @ApiModelProperty(value = "玩家奖杯数范围min")
    private Integer playerTrophyCountRangeMin;

    @ApiModelProperty(value = "玩家奖杯数范围max")
    private Integer playerTrophyCountRangeMax;

    @ApiModelProperty(value = "武器匹配分数范围min")
    private Integer playerWeaponAIMatchScoreRangeMin;

    @ApiModelProperty(value = "武器匹配分数范围max")
    private Integer playerWeaponAIMatchScoreRangeMax;

    @ApiModelProperty(value = "胜率上限")
    private Integer winRateCeiling;

    @ApiModelProperty(value = "胜率下限")
    private Integer winRateFloor;

    @ApiModelProperty(value = "AI武器分数范围min")
    private Integer aiWeaponScoreRangeMin;

    @ApiModelProperty(value = "AI武器分数范围max")
    private Integer aiWeaponScoreRangeMax;
}
