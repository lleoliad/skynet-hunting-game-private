package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@ApiModel(value = "LuckyWheelSpinReward对象", description = "幸运转盘奖励")
public class LuckyWheelSpinReward implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("奖励下标")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer rewardIndex;

    @ApiModelProperty("奖励的金币")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer rewardCoin;

    @ApiModelProperty("开箱结果")
    private ChestOpenResult chestOpenResult;

}
