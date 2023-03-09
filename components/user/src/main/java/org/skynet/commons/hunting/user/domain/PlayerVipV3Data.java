package org.skynet.commons.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@ApiModel(value = "PlayerVipV2Data对象", description = "玩家vip v3相关数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlayerVipV3Data implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("vip过期日")
    private Long vipExpiredStandardDay;

    @ApiModelProperty("svip过期日")
    private Long svipExpiredStandardDay;

    @ApiModelProperty("上一次获取vip奖励日")
    private Long lastClaimVipRewardsStandardDay;

    @ApiModelProperty("上一次获取svip奖励日")
    private Long lastClaimSVipRewardsStandardDay;

}
