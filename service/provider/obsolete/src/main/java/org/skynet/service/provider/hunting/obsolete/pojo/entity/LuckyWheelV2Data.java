package org.skynet.service.provider.hunting.obsolete.pojo.entity;

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
@ApiModel(value = "LuckyWheelV2Data", description = "第二版转盘玩家数据")
public class LuckyWheelV2Data implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "上一次刷新整个转盘的日期")
    private Long lastRefreshLuckyWheelStandardDay;

    @ApiModelProperty(value = "表盘内容id")
    private Integer sectorContentId;

    @ApiModelProperty(value = "免费次数")
    private Integer freeSpinCount;

    @ApiModelProperty(value = "下一个免费次数unix时间")
    private Long nextFreeSpinUnixTime;

    @ApiModelProperty(value = "一共使用过多少次转盘")
    private Integer useSpinCountInHistory;
}
