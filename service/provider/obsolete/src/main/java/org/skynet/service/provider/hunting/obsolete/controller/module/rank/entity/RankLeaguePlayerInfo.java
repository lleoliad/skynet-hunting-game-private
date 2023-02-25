package org.skynet.service.provider.hunting.obsolete.controller.module.rank.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "RankLeaguePlayerInfo对象", description = "段位服务器返回的玩家段位信息")
public class RankLeaguePlayerInfo {

    @ApiModelProperty(value = "分组id")
    private String groupId;

    @ApiModelProperty(value = "段位")
    private Integer rank;

    @ApiModelProperty(value = "上赛季奖励状态")
    private RewardStatusEnum rewardStatus;


}
