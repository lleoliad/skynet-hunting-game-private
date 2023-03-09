package org.skynet.components.hunting.user.domain;

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
@ApiModel(value = "PlayerGunGiftPackageData对象", description = "玩家枪械礼包数据")
public class PlayerGunGiftPackageData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("对应 GunGiftPackageGroupTable 表")
    private Integer groupId;

    @ApiModelProperty("对应 GunGiftPackageTable 表")
    private Integer packageId;

    @ApiModelProperty("过期时间 unix time second")
    private Long expireTime;

}
