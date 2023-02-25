package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "IncreaseChapterWinChestProgressResult对象", description = "章节胜利宝箱进度增长结果")
public class IncreaseChapterWinChestProgressResult {

    @ApiModelProperty(value = "章节id")
    private Integer chapterId;

    @ApiModelProperty(value = "之前的进展")
    private Integer progressBefore;

    @ApiModelProperty(value = "之后的进展")
    private Integer progressAfter;

    @ApiModelProperty(value = "获胜的章节勋章数据")
    private ChapterWinChestData obtainChapterWinChestData;
}
