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
@ApiModel(value = "OpponentPlayerInfo对象", description = "对手玩家信息")
public class OpponentPlayerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "姓名")
    String name;

    @ApiModelProperty(value = "照片")
    String icon_base64;

    @ApiModelProperty(value = "默认照片")
    Boolean useDefaultIcon;

    @ApiModelProperty(value = "奖杯")
    Integer trophy;
}
