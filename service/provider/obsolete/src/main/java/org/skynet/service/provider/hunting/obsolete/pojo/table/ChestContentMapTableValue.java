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
@ApiModel(value = "ChestContentMapTableValue对象", description = "宝箱内容数据库表")
public class ChestContentMapTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "宝箱类型", notes = "1:银,2:金,3.猩红,4:白金,5:king")
    private Integer chestType;

    @ApiModelProperty(value = "箱子等级")
    private Integer chestLevel;

    @ApiModelProperty(value = "Epic枪数量")
    private Integer epicGunCount;

    @ApiModelProperty(value = "Rare枪数量")
    private Integer rareGunCount;

    @ApiModelProperty(value = "Random枪数量")
    private Integer randomGunCount;

    @ApiModelProperty(value = "Random子弹数量")
    private Integer randomBulletCount;

    @ApiModelProperty(value = "高机会子弹数量")
    private Integer highChanceBulletCount;

    @ApiModelProperty(value = "低机会子弹数量")
    private Integer lowChanceBulletCount;
}
