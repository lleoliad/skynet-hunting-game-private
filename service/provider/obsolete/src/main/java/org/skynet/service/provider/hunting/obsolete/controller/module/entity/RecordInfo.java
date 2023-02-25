package org.skynet.service.provider.hunting.obsolete.controller.module.entity;


import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerWeaponInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "RecordInfo对象", description = "用来做匹配")
@EqualsAndHashCode(callSuper = false)
public class RecordInfo {

    @ApiModelProperty(value = "策划表RecordModeMatch的ID")
    private Integer recordModeMatchId;

    @ApiModelProperty(value = "枪械信息？")
    private PlayerWeaponInfo playerWeaponInfo;


}
