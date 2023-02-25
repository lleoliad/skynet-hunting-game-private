package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ConfirmHuntingMatchCompleteDTO对象", description = "确认比赛完成的接接收对象")
public class ConfirmHuntingMatchStartDTO extends BaseDTO {

    @ApiModelProperty(value = "章节id")
    private Integer chapterId;

    @ApiModelProperty(value = "用户更新次数")
    private Integer userDataUpdateCount;
}
