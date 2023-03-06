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
@ApiModel(value = "ChestGunTableValue对象", description = "宝箱枪数据库表")
public class ChestGunTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "枪械库类型", notes = "1.Epic,2.Rare,3.Random")
    private Integer libraryType;

    @ApiModelProperty(value = "箱子等级")
    private Integer chestLevel;

    @ApiModelProperty(value = "抽取次数要求")
    private Integer drawCountRequires;

    @ApiModelProperty(value = "枪id")
    private List<Integer> rewardGunIds;

    @ApiModelProperty(value = "备用枪id")
    private List<Integer> fallbackRewardGunIds;
}
