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
@ApiModel(value = "ChestBulletTableValue对象", description = "宝箱子弹数据库表")
public class ChestBulletTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "子弹库类型", notes = "1.Random,2.HighChance,3.LowChance")
    private Integer libraryType;

    @ApiModelProperty(value = "箱子等级")
    private Integer chestLevel;

    @ApiModelProperty(value = "子弹id")
    private List<Integer> rewardBulletIdsArray;

    @ApiModelProperty(value = "备用子弹id")
    private List<Integer> fallbackRewardBulletIdsArray;
}
