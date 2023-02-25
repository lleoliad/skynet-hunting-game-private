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
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "GunReward对象", description = "枪支奖励")
public class GunReward implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "枪id")
    private Integer gunId;

    @ApiModelProperty(value = "数目")
    private Integer count;


}
