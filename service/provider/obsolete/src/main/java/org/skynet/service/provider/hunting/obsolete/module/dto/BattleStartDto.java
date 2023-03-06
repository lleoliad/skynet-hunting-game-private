package org.skynet.service.provider.hunting.obsolete.module.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "BattleStartDto对象", description = "每回合开始时，向服务器发起校验，并获取战斗AI数据")
@EqualsAndHashCode(callSuper = false)
public class BattleStartDto extends BaseDTO {

    @ApiModelProperty(value = "玩家准确率")
    private Double playerAveragePrecision;

    @ApiModelProperty(value = "玩家分数")
    private Integer playerScore;

    @ApiModelProperty(value = "回合数")
    private Integer round;

    @ApiModelProperty(value = "动物路线id")
    private Long animalRouteUid;


}
