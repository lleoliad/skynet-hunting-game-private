package org.skynet.service.provider.hunting.obsolete.module.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ChapterInfo对象", description = "玩家章节信息")
@EqualsAndHashCode(callSuper = false)
public class ChapterInfo {

    @ApiModelProperty(value = "章节战斗信息，key是章节，val是战斗信息")
    private Map<String, ChapterBattleInfo> chapterBattleInfos;

}
