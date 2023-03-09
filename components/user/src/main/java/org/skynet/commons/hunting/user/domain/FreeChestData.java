package org.skynet.commons.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;


@ApiModel(value = "FreeChestData对象", description = "免费箱子数据")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class FreeChestData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "箱子id")
    String uid;

    @ApiModelProperty(value = "箱子类型")
    Integer chestType;

    @ApiModelProperty(value = "箱子等级")
    Integer level;

    @ApiModelProperty(value = "创建时间")
    Long createTime;

    @ApiModelProperty("剩余可用时间")
    Long availableUnixTime;

}
