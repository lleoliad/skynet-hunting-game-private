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
@ApiModel(value = "MatchControlRecordsPoolMetaData对象", description = "匹配控制记录池数据")
public class MatchControlRecordsPoolMetaData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("最低分数")
    private Integer lowestFinalScore;

    @ApiModelProperty("最高分数")
    private Integer highestFinalScore;

    @ApiModelProperty(value = "是否可用", notes = "可用的录像数据超过一定数量，该池才认为是可用的，否则走回合匹配")
    private Boolean isUsable;
}
