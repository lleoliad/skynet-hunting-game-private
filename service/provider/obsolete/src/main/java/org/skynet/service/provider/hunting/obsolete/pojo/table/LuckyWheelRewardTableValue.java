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
@ApiModel(value = "LuckyWheelRewardTableValue对象", description = "幸运转盘索引表")
public class LuckyWheelRewardTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("旋转次数")
    private Integer spinCount;

    @ApiModelProperty("允许循环")
    private Boolean loop;

    @ApiModelProperty("奖励索引")
    private List<Integer> rewardIndices;

    @ApiModelProperty("奖励索引权重")
    private List<Integer> rewardIndexWeights;
}
