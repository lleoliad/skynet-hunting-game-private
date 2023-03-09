// package org.skynet.service.provider.hunting.obsolete.pojo.entity;
//
//
// import org.skynet.service.provider.hunting.obsolete.module.rank.entity.ClientRecord;
// import io.swagger.annotations.ApiModel;
// import io.swagger.annotations.ApiModelProperty;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.EqualsAndHashCode;
// import lombok.NoArgsConstructor;
//
// @Data
// @EqualsAndHashCode(callSuper = false)
// @NoArgsConstructor
// @AllArgsConstructor
// @ApiModel(value = "PlayerRankData对象", description = "玩家自己的段位信息")
// public class PlayerRankData {
//
//
//     @ApiModelProperty(value = "上一周段位赛组 uid")
//     private String lastWeekRankGroupUid;
//
//     @ApiModelProperty(value = "本周段位赛组 uid")
//     private String currentWeekRankGroupUid;
//
//     @ApiModelProperty(value = "本周段位的 id。可以通过 id，从表格获取对应段位信息")
//     private Integer currentWeekRankId;
//
//     @ApiModelProperty(value = "上周段位 id")
//     private Integer lastWeekRankId;
//
//     @ApiModelProperty(value = "上一周升段奖励宝箱类型，如果已经领取或者没有则为-1")
//     private Integer lastWeekRewardChestType;
//
//     @ApiModelProperty(value = "奖励宝箱等级")
//     private Integer lastWeekRewardChestLevel;
//
//     @ApiModelProperty(value = "客户端存的一些记录")
//     private ClientRecord clientRecord;
//
//     @ApiModelProperty(value = "上赛季奖励状态")
//     private Integer rewardStatus;
//
//     @ApiModelProperty(value = "段位结算的结束标准时间")
//     private Long endStandardTime;
//
//     @ApiModelProperty(value = "最高段位id")
//     private Integer bestRankId;
//
//
// }
