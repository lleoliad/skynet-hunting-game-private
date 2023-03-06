package org.skynet.service.provider.hunting.obsolete.module.entity;

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
@ApiModel(value = "MatchDto对象", description = "玩家章节战斗信息")
@EqualsAndHashCode(callSuper = false)
public class ChapterBattleInfo {

    @ApiModelProperty(value = "章节战斗次数")
    private Integer battleCount;

    @ApiModelProperty(value = "章节最近五场得分，用户计算中位数")
    private List<Integer> lastFiveScores;


}
