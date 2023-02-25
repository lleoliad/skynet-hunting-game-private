package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "AchievementDTO对象", description = "成就接收对象")
public class AchievementDTO extends BaseDTO {

    @ApiModelProperty("成就类型")
    private Object achievementType;
}
