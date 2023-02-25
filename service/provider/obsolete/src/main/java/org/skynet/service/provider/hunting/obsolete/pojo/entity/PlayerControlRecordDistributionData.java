package org.skynet.service.provider.hunting.obsolete.pojo.entity;

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
@ApiModel(value = "PlayerControlRecordDistributionData对象", description = "播放器控制记录分配数据")
public class PlayerControlRecordDistributionData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "uid")
    private String uid;

    @ApiModelProperty(value = "路由id")
    private Long routeUid;

    @ApiModelProperty(value = "怪物id")
    private Integer animalId;

    @ApiModelProperty(value = "枪id")
    private Integer gunId;

    @ApiModelProperty(value = "枪等级")
    private Integer gunLevel;

    @ApiModelProperty(value = "子弹id")
    private Integer bulletId;

    @ApiModelProperty(value = "武器的分值")
    private Integer weaponScore;

    @ApiModelProperty(value = "精度范围", notes = "从0.00到1.00一共有101种可能")
    private List<Double> precisionRangeDistribution;

    @ApiModelProperty(value = "记录每一种精度的录像个数")
    private Integer totalRecordDataCount;

    @ApiModelProperty(value = "是否可用", notes = "代表着该路线武器组合,有足够且合适的录像文件分布,ai可以选取")
    private Boolean available;
}
