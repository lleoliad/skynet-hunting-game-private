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
@ApiModel(value = "PlayerFireDetails对象", description = "玩家每一枪的数据")
public class PlayerFireDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "回合数")
    private Integer round;

    @ApiModelProperty(value = "射击精准度")
    private Float showPrecision;

    @ApiModelProperty(value = "是否完美击杀")
    private Boolean isPerfect;

    @ApiModelProperty(value = "怪物id")
    private Integer animalId;

    @ApiModelProperty(value = "是否杀的怪物")
    private Boolean isKillAnimal;

    @ApiModelProperty(value = "是否打中头")
    private Boolean isHitHead;

    @ApiModelProperty(value = "是否打中心脏")
    private Boolean isHitHeart;
}
