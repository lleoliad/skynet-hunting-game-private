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
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ChapterWinChestProgressData对象", description = "章节胜利宝箱进度数据")
public class ChapterWinChestProgressData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "当前进度")
    Integer currentProgress;

    @ApiModelProperty(value = "最大进度")
    Integer progressMax;
}
