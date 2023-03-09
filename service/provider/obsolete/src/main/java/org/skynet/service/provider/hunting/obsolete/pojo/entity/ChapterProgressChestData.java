package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.skynet.components.hunting.user.domain.ChestData;

import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "AchievementData对象", description = "章节进度箱子数据,保底箱子,只要完成就增加一个进度")
@AllArgsConstructor
@NoArgsConstructor
public class ChapterProgressChestData extends ChestData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "当前进度")
    Integer currentProgress;

    @ApiModelProperty(value = "最大进度")
    Integer progressMax;


    public ChapterProgressChestData(String uid, Integer chestType, Integer level, Long createTime, Integer currentProgress, Integer progressMax) {
        super(uid, chestType, level, createTime);
        this.currentProgress = currentProgress;
        this.progressMax = progressMax;
    }
}
