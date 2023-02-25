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
@ApiModel(value = "AiWeaponCombinationTableValue对象", description = "Ai武器组合表值")
public class AiWeaponCombinationTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "枪械")
    private Integer gunId;

    @ApiModelProperty(value = "枪械等级")
    private Integer gunLevel;

    @ApiModelProperty(value = "子弹id")
    private Integer bulletId;
}
