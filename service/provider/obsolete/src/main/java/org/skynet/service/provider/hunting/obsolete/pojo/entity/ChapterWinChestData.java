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
@ApiModel(value = "ChapterWinChestData对象", description = "章节胜利箱子数据,必须在该章节胜利才能获得")
@NoArgsConstructor
@AllArgsConstructor
public class ChapterWinChestData extends ChestData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "剩余的解锁时间")
    Long availableUnixTime;

    @ApiModelProperty(value = "解锁该宝箱需要多少秒")
    Long unlockSecondsRequires;

    public ChapterWinChestData(String uid, Integer chestType, Integer level, Long createTime, Long availableUnixTime, Long unlockSecondsRequires) {
        super(uid, chestType, level, createTime);
        this.availableUnixTime = availableUnixTime;
        this.unlockSecondsRequires = unlockSecondsRequires;
    }


}
