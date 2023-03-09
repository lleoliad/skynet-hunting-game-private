// package org.skynet.commons.hunting.user.domain;
//
//
// import io.swagger.annotations.ApiModel;
// import io.swagger.annotations.ApiModelProperty;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.EqualsAndHashCode;
// import lombok.NoArgsConstructor;
//
// @ApiModel(value = "PlayerRankData对象", description = "玩家自己的段位信息")
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// @EqualsAndHashCode(callSuper = false)
// public class PlayerRankData {
//
//     @ApiModelProperty(value = "上周段位 id")
//     private Integer lastWeekRankId;
//
//
//     @ApiModelProperty(value = "本周段位的 id。可以通过 id，从表格获取对应段位信息")
//     private Integer currentWeekRankId;
//
//
//     @ApiModelProperty(value = "段位结算的结束标准时间")
//     private Long endStandardTime;
//
//     @ApiModelProperty(value = "当前是段位周赛的第几周（当服务器段位结算完成后，变化）")
//     private Integer currentRankWeekIndex;
//
//
// }
