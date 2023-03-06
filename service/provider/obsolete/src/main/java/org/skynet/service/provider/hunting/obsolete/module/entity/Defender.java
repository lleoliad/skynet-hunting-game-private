package org.skynet.service.provider.hunting.obsolete.module.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Defender implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "枪id")
    private Integer gunId;

    @ApiModelProperty(value = "枪等级")
    private Integer gunLevel;

    @ApiModelProperty(value = "子弹id")
    private Integer bulletId;


    @ApiModelProperty(value = "玩家杯数")
    private Integer trophyCount;


}
