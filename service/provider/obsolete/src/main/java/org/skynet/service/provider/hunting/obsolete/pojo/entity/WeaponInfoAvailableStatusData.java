package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "WeaponInfoAvailableStatusData对象", description = "武器信息可用状态数据")
public class WeaponInfoAvailableStatusData {

    @ApiModelProperty(value = "武器")
    private PlayerWeaponInfo weaponInfo;

    @ApiModelProperty(value = "路由集合")
    private List<Long> routesIds;

    @ApiModelProperty(value = "可用分布计数")
    private Integer availableDistributionCount;

    @ApiModelProperty(value = "控制记录数据计数")
    private Integer controlRecordDataCount;
}
