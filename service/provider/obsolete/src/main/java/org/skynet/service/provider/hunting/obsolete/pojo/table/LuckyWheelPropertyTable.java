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
@ApiModel(value = "LuckyWheelPropertyTable对象", description = "幸运转盘属性表")
public class LuckyWheelPropertyTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("转盘功能启动章节")
    private Integer functionEnableChapterId;

    @ApiModelProperty("初始免费使用次数")
    private Integer defaultFreeSpinCount;

    @ApiModelProperty("免费使用恢复1个需要的时间（秒）")
    private Integer freeSpinIncreaseOnceSeconds;

    @ApiModelProperty("累计奖励旋转次数")
    private List<Integer> cumulativeRewardSpinCountArray;

    @ApiModelProperty("累计奖励对应的钻石奖励")
    private List<Integer> cumulativeRewardDiamondCountArray;
}
