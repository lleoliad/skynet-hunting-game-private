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
@ApiModel(value = "GunUpgradeCountTableValue对象", description = "枪升级内容数据库表")
public class GunUpgradeCountTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("等级")
    private Integer id;

    @ApiModelProperty("蓝色品质")
    private Integer blueQuality;

    @ApiModelProperty("绿色品质")
    private Integer greenQuality;

    @ApiModelProperty("红色品质")
    private Integer redQuality;

    @ApiModelProperty("橙色品质")
    private Integer orangeQuality;
}
