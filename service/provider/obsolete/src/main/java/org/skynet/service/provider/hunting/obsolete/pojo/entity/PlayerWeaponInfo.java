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
@ApiModel(value = "PlayerWeaponInfo对象", description = "对手信息数据")
public class PlayerWeaponInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "枪id")
    private Integer gunId;

    @ApiModelProperty(value = "枪等级")
    private Integer gunLevel;

    @ApiModelProperty(value = "子弹id")
    private Integer bulletId;
}
