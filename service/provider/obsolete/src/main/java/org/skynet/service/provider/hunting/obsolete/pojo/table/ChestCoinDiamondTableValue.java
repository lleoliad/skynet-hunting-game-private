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
@ApiModel(value = "ChestCoinDiamondTableValue对象", description = "宝箱金币和钻石数据库表")
public class ChestCoinDiamondTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty(value = "宝箱类型", notes = "1:银,2:金,3.猩红,4:白金,5:king")
    private Integer chestType;

    @ApiModelProperty("箱子等级")
    private Integer chestLevel;

    @ApiModelProperty("金币奖励")
    private List<Integer> rewardCoinCountArray;

    @ApiModelProperty("备用金币奖励")
    private List<Integer> fallbackRewardCoinCountArray;

    @ApiModelProperty("钻石奖励")
    private List<Integer> rewardDiamondCountArray;

    @ApiModelProperty("备用钻石奖励")
    private List<Integer> fallbackRewardDiamondCountArray;
}
