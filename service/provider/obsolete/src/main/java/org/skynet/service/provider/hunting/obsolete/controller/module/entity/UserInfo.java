package org.skynet.service.provider.hunting.obsolete.controller.module.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UserInfo对象", description = "存储用户战斗信息")
@EqualsAndHashCode(callSuper = false)
public class UserInfo {

    @ApiModelProperty(value = "章节战斗信息")
    private ChapterInfo chapterInfo;

    @ApiModelProperty(value = "用户战斗信息")
    private BattleInfo battleInfo;

    @ApiModelProperty(value = "是AI战斗录制的玩家，需要传输此信息")
    private RecordInfo recordInfo;


}
