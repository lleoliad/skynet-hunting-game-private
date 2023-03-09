package org.skynet.commons.hunting.user.domain;

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
@ApiModel(value = "AchievementData对象", description = "玩家的成就数据")
public class AchievementData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务ID")
    private Integer achievementId;

    @ApiModelProperty(value = "任务进度")
    private Integer currentProgress;

    @ApiModelProperty(value = "最大进度")
    private Integer maxProgress;

    @ApiModelProperty(value = "是否全部完成")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean fullyCompleted = false;
}
