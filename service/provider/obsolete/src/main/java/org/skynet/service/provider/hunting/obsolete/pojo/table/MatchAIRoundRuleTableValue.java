package org.skynet.service.provider.hunting.obsolete.pojo.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "MatchAIRoundRuleTableValue对象", description = "AI匹配局数规则表")
public class MatchAIRoundRuleTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("对应章节")
    private Integer chapterID;

    @ApiModelProperty("章节进入次数")
    private Integer chapterEnteredCount;

    @ApiModelProperty(value = "匹配胜负倾向", notes = "0:胜状态,1:负状态,2:倾向于平局的回合选择")
    private Integer winLoseProne;

    @ApiModelProperty("AI规则编号")
    private List<Integer> ruleIndex;

    @ApiModelProperty("参与循环")
    private Boolean allowLoopPick;

    @ApiModelProperty("是否玩家先手")
    private Boolean isPlayerFirst;

    @ApiModelProperty("AI可用武器组合")
    private List<Integer> aiWeaponCombinationId;
}
