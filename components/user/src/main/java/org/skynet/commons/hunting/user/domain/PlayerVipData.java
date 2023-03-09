package org.skynet.commons.hunting.user.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;

@ApiModel(value = "PlayerVipData对象", description = "玩家vip相关数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class PlayerVipData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("vip过期日")
    private Long vipExpiredStandardDay = -1L;

    @ApiModelProperty("svip过期日")
    private Long svipExpiredStandardDay = -1L;

    @ApiModelProperty("上一次获取vip奖励日")
    private Long lastClaimVipRewardsStandardDay = -1L;

    @ApiModelProperty("上一次获取svip奖励日")
    private Long lastClaimSVipRewardsStandardDay = -1L;

    @ApiModelProperty("上一次清除所有vip转盘次数的日期")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long server_only_lastClearLuckyWheelVipSpinCountStandardTimeDay;

    @ApiModelProperty("上一次刷新vip转盘次数的日期")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long server_only_lastRefreshLuckyWheelVipSpinStandardTimeDay = -1L;

    @ApiModelProperty("上一次刷新svip转盘次数的日期")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long server_only_lastRefreshLuckyWheelSVipSpinStandardTimeDay = -1L;
}
