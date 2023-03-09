package org.skynet.commons.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@ApiModel(value = "PlayerBulletGiftPackageData对象", description = "玩家子弹礼包数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlayerBulletGiftPackageData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("礼包id")
    private Integer packageId;

    @ApiModelProperty("宝箱等级")
    private Integer chestLevel;

    @ApiModelProperty("开始时间")
    private Long startStandardTimeSecond;

    @ApiModelProperty("结束时间")
    private Long endStandardTimeSecond;

}
