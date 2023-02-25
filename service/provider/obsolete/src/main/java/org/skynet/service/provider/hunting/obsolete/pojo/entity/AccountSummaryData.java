package org.skynet.service.provider.hunting.obsolete.pojo.entity;


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
@ApiModel(value = "AchievementData对象", description = "玩家的成就数据")
public class AccountSummaryData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "账号uid")
    private String uid;

    @ApiModelProperty(value = "玩家名称")
    private String playerName;

    @ApiModelProperty(value = "账号奖杯数量")
    private Integer trophy;


}
