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
@ApiModel(value = "MatchAiRoundControlQuery对象", description = "回合选择录制文件情况下，查询参数")
public class MatchAiRoundControlQuery implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "匹配Ai规则表索引")
    private Integer matchAiRoundRuleTableId;

    @ApiModelProperty(value = "章节id")
    private Integer chapterId;

    @ApiModelProperty(value = "匹配id")
    private Integer matchId;

    @ApiModelProperty(value = "回合数")
    private Integer round;

    @ApiModelProperty(value = "路线id")
    private Long routeUid;

    @ApiModelProperty(value = "玩家平均射击精准度")
    private Double playerAveragePrecision;

    @ApiModelProperty(value = "玩家的最终分数")
    private Integer playerFinalScore;

    @ApiModelProperty("玩家Uid")
    private String playerUid;

    @ApiModelProperty(value = "ai的武器")
    private PlayerWeaponInfo aiWeaponInfo;
}
