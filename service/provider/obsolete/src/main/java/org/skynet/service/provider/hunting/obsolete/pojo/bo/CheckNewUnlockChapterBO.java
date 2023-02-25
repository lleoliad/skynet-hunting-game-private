package org.skynet.service.provider.hunting.obsolete.pojo.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "checkNewUnlockChapterBO对象", description = "看是否能够解锁新章节的返回对象")
public class CheckNewUnlockChapterBO {

    @ApiModelProperty(value = "是否有新的未解锁章节")
    private Boolean haveNewChapterUnlocked;

    @ApiModelProperty(value = "新的未解锁章节")
    private Integer newUnlockChapterId;
}
