package org.skynet.service.provider.hunting.obsolete.pojo.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "LuckyWheelSectorContentTableValue对象", description = "幸运转盘奖励表")
public class LuckyWheelV2SectorContentTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id(随机库组id)")
    private Integer id;

    @ApiModelProperty("奖励类型")
    private List<Integer> rewardTypes;

    @ApiModelProperty("奖励类型")
    private List<Integer> rewardAmounts;


}
