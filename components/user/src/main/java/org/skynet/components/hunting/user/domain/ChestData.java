package org.skynet.components.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@ApiModel(value = "ChestData对象", description = "箱子基础数据")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChestData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "箱子id")
    String uid;

    @ApiModelProperty(value = "箱子类型")
    Integer chestType;

    @ApiModelProperty(value = "箱子等级")
    Integer level;

    @ApiModelProperty(value = "创建时间")
    Long createTime;
}
