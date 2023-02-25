package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ForceStepDTO对象", description = "确认完成步骤接收对象")
public class ForceStepDTO extends BaseDTO {

    @ApiModelProperty("强制完成步骤的名称")
    private String forceTutorialStepName;
}
