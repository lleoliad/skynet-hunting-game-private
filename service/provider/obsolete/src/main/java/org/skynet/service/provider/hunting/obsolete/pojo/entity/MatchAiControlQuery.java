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
@ApiModel(value = "MatchAiControlQuery对象", description = "匹配AI操作查询")
public class MatchAiControlQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "匹配Ai规则表索引")
    private Integer matchAiRoundRuleTableId;

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

    @ApiModelProperty(value = "ai的武器")
    private PlayerWeaponInfo aiWeaponInfo;
}
