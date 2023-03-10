package org.skynet.components.hunting.user.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@ApiModel(value = "PlayerVipData对象", description = "玩家vip相关数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
// @Builder
@EqualsAndHashCode(callSuper = false)
public class PlayerVipDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("vip过期日")
    private Long vipExpiredStandardDay = -1L;

    @ApiModelProperty("svip过期日")
    private Long svipExpiredStandardDay = -1L;

    @ApiModelProperty("上一次获取vip奖励日")
    private Long lastClaimVipRewardsStandardDay = -1L;

    @ApiModelProperty("上一次获取svip奖励日")
    private Long lastClaimSVipRewardsStandardDay = -1L;
}
