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
@ApiModel(value = "BulletReward对象", description = "子弹奖励")
public class BulletReward implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "子弹id")
    private Integer bulletId;

    @ApiModelProperty(value = "数量")
    private Integer count;
}
