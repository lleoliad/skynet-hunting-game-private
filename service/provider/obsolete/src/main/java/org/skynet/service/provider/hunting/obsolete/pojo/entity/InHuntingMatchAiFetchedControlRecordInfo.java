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
@ApiModel(value = "InHuntingMatchAiFetchedControlRecordInfo对象", description = "比赛过程中,ai获取的录像文件信息")
public class InHuntingMatchAiFetchedControlRecordInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "uid")
    private String uid;

    @ApiModelProperty(value = "recordGameVersion")
    private String recordGameVersion;

    @ApiModelProperty(value = "内部版本")
    private Integer internalVersion;

    @ApiModelProperty(value = "最终分数")
    private Integer finalScore;

    @ApiModelProperty(value = "fetchedTime")
    private Long fetchedTime;
}
