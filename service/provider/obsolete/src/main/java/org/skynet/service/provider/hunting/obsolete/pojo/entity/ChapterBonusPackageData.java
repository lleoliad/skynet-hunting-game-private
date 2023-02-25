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
@ApiModel(value = "ChapterBonusPackageData对象", description = "章节礼包数据")
@AllArgsConstructor
@NoArgsConstructor
public class ChapterBonusPackageData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "章节id", notes = "chapter id同时也是package id")
    private Integer chapterId;

    @ApiModelProperty(value = "章节创建时间")
    private Long createTime;

    @ApiModelProperty(value = "过期时间")
    private Long expireTime;

    @ApiModelProperty(value = "章节是否激活", notes = "激活之后,expireTime才生成,玩家在看到礼包之后,才能Confirm")
    private Boolean isActive;
}
