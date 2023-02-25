package org.skynet.service.provider.hunting.obsolete.pojo.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "GunReward对象", description = "枪支奖励")
public class RewardGunInfo {

    @ApiModelProperty(value = "枪支奖励")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<GunReward> gunRewards;

    @ApiModelProperty(value = "新的未解锁枪支id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Integer> newUnlockedGunIDs;

}
