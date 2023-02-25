package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "AIControlRecordDataQueryDTO对象", description = "人工智能控制记录数据查询接收对象")
public class ActiveChapterBonusDTO extends BaseDTO {

    @ApiModelProperty("章节id")
    private Integer chapterId;

    @ApiModelProperty("过期时间")
    private Long expireTime;
}
