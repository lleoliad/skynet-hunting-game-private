package org.skynet.service.provider.hunting.obsolete.pojo.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "AchievementTableValue对象", description = "默认的成就数据库表")
public class AchievementTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "任务类型")
    private Integer achievementType;

    @ApiModelProperty(value = "任务等级")
    private Integer level;

    @ApiModelProperty(value = "任务参数")
    private Integer condition;

    @ApiModelProperty(value = "任务目标")
    private Integer objective;

    @ApiModelProperty(value = "钻石奖励")
    private Integer diamondReward;

    @ApiModelProperty(value = "任务名")
    private String achievementName;

    @ApiModelProperty(value = "任务描述")
    private String achievementDetail;
}
