package org.skynet.commons.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;


@ApiModel(value = "ChapterWinChestData对象", description = "章节胜利箱子数据,必须在该章节胜利才能获得")
@Data
// @NoArgsConstructor
// @AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ChapterWinChestData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "箱子id")
    String uid;

    @ApiModelProperty(value = "箱子类型")
    Integer chestType;

    @ApiModelProperty(value = "箱子等级")
    Integer level;

    @ApiModelProperty(value = "创建时间")
    Long createTime;

    @ApiModelProperty(value = "剩余的解锁时间")
    Long availableUnixTime;

    @ApiModelProperty(value = "解锁该宝箱需要多少秒")
    Long unlockSecondsRequires;

}
