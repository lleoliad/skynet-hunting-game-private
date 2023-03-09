// package org.skynet.service.provider.hunting.obsolete.pojo.entity;
//
// import com.fasterxml.jackson.annotation.JsonInclude;
// import io.swagger.annotations.ApiModel;
// import io.swagger.annotations.ApiModelProperty;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.EqualsAndHashCode;
// import lombok.NoArgsConstructor;
//
// import java.io.Serializable;
//
// @Data
// @EqualsAndHashCode(callSuper = false)
// @NoArgsConstructor
// @AllArgsConstructor
// @ApiModel(value = "PlayerVipData对象", description = "玩家vip相关数据")
// public class PlayerVipData implements Serializable {
//
//     private static final long serialVersionUID = 1L;
//
//     @ApiModelProperty("vip过期日")
//     private Long vipExpiredStandardDay;
//
//     @ApiModelProperty("svip过期日")
//     private Long svipExpiredStandardDay;
//
//     @ApiModelProperty("上一次获取vip奖励日")
//     private Long lastClaimVipRewardsStandardDay;
//
//     @ApiModelProperty("上一次获取svip奖励日")
//     private Long lastClaimSVipRewardsStandardDay;
//
//     @ApiModelProperty("上一次清除所有vip转盘次数的日期")
//     @JsonInclude(JsonInclude.Include.NON_NULL)
//     private Long server_only_lastClearLuckyWheelVipSpinCountStandardTimeDay;
//
//     @ApiModelProperty("上一次刷新vip转盘次数的日期")
//     @JsonInclude(JsonInclude.Include.NON_NULL)
//     private Long server_only_lastRefreshLuckyWheelVipSpinStandardTimeDay;
//
//     @ApiModelProperty("上一次刷新svip转盘次数的日期")
//     @JsonInclude(JsonInclude.Include.NON_NULL)
//     private Long server_only_lastRefreshLuckyWheelSVipSpinStandardTimeDay;
// }
