package org.skynet.service.provider.hunting.obsolete.module.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "SearchAiDto对象", description = "为游戏服查找ai")
@EqualsAndHashCode(callSuper = false)
public class SearchAiDto implements Serializable {

    @ApiModelProperty(value = "游戏版本")
    private String gameVersion;
    @ApiModelProperty(value = "动物Id")
    private Integer animalId;
    @ApiModelProperty(value = "线路Id")
    private Long animalRouteUid;
    @ApiModelProperty(value = "武器Id")
    private Integer gunId;
    @ApiModelProperty(value = "武器等级")
    private Integer gunLevel;
    @ApiModelProperty(value = "子弹Id")
    private Integer bulletId;
    @ApiModelProperty(value = "风力Id")
    private Integer windId;
    @ApiModelProperty(value = "准确率等级")
    private Integer AveragePrecisionLevel;


}
