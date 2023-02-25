package org.skynet.service.provider.hunting.obsolete.pojo.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "BulletTableValue对象", description = "子弹数据库表")
public class BulletTableValue {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "品质", notes = "1:白色,2:蓝色,3:橙色,4:红色")
    private Integer quality;

    @ApiModelProperty(value = "口径类型", notes = "Standard:1,Activities:2")
    private Integer categoryType;

    @ApiModelProperty(value = "是否默认子弹")
    private Boolean defaultBullet;

    @ApiModelProperty(value = "购买价格")
    private List<Integer> purchaseDiamondPriceArray;

    @ApiModelProperty(value = "养成分数加成")
    private Integer cultivateScoreAddition;
}
