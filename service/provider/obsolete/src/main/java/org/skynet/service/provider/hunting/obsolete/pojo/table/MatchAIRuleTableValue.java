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
@ApiModel(value = "MatchAIRuleTableValue对象", description = "比赛规则据库表")
public class MatchAIRuleTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "对应章节")
    private Integer chapterID;

    @ApiModelProperty(value = "章节进入次数")
    private Integer chapterEnteredCount;

    @ApiModelProperty(value = "匹配胜负倾向", notes = "0:胜状态,1:负状态")
    private Integer winLoseProne;

    @ApiModelProperty(value = "AI规则编号")
    private List<Integer> ruleIndexArray;

    @ApiModelProperty(value = "参与循环")
    private Boolean allowLoopPick;

    @ApiModelProperty(value = "是否玩家先手")
    private Boolean isPlayerFirst;

    @ApiModelProperty(value = "AI可用枪械")
    private List<Integer> aiAvailableGunIdsArray;
}
