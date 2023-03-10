package org.skynet.components.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@ApiModel(value = "PlayerFifthDayGunGiftPackageData对象", description = "玩家五日枪械礼包数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlayerFifthDayGunGiftPackageData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("对应 FifthDayGunGiftPackageGroupTable 表")
    private Integer groupId;

    @ApiModelProperty("对应 FifthDayGunGiftPackageTable 表")
    private Integer packageId;

    @ApiModelProperty("过期时间 unix time second")
    private Long expireTime;

}
