package org.skynet.service.provider.hunting.obsolete.pojo.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "GunTableValue对象", description = "枪数据库表")
public class GunTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty("解锁需要碎片数量")
    private Integer unlockPiecesRequires;

    @ApiModelProperty(value = "是否是初始枪")
    private Boolean isStarterGun;

    @ApiModelProperty(value = "品质", notes = "1:白色,2:蓝色,3:橙色,4:红色")
    private Integer quality;

    @ApiModelProperty(value = "等级上限")
    private Integer maxLevel;

    @ApiModelProperty(value = "升级所需金币")
    private List<Integer> upgradeCoinRequiresArray;

    @ApiModelProperty(value = "养成分数(对应等级)")
    private List<Integer> levelCultivateScoresArray;
}
