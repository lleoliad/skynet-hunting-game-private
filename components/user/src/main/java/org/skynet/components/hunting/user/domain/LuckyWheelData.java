package org.skynet.components.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@ApiModel(value = "LuckyWheelData对象", description = "玩家幸运转盘数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LuckyWheelData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "上一次刷新整个转盘的日期")
    private Long lastRefreshLuckyWheelStandardDay;

    @ApiModelProperty(value = "当前转盘对应的章节，累计奖励刷新时才改变")
    private Integer currentChapterId;

    @ApiModelProperty(value = "免费次数")
    private Integer freeSpinCount;

    @ApiModelProperty(value = "下一个免费次数unix时间")
    private Long nextFreeSpinUnixTime;

    @ApiModelProperty(value = "累计奖励累计的旋转次数")
    private Integer cumulativeRewardSpinCount;

    @ApiModelProperty(value = "vip转盘次数")
    private Integer vipSpinCount;

    @ApiModelProperty(value = "一共使用过多少次转盘")
    private Integer useSpinCountInHistory;
}
