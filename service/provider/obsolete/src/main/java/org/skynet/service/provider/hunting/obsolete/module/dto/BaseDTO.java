package org.skynet.service.provider.hunting.obsolete.module.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "BaseDTO对象", description = "基础接收对象")
@EqualsAndHashCode(callSuper = false)
public class BaseDTO {

    @ApiModelProperty(value = "游戏版本")
    private String version;

    @ApiModelProperty(value = "用户uid")
    private String uid;


}
