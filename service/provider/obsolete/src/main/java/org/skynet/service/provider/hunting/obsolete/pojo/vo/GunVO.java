package org.skynet.service.provider.hunting.obsolete.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "GunVO对象", description = "展示枪信息对象")
public class GunVO {

    @ApiModelProperty(value = "装备的枪械ID")
    private Integer equippedGunId = null;

    @ApiModelProperty(value = "枪械的等级")
    private Map<Integer, Integer> gunLevelMap = null;

    @ApiModelProperty(value = "枪械的数量")
    private Map<Integer, Integer> gunCountMap = null;

    @ApiModelProperty(value = "玩家的uuid")
    private String uuid;
}
