package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import org.skynet.service.provider.hunting.obsolete.enums.PlayerControlRecordSource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "PlayerControlRecordDocData对象", description = "玩家控制记录文档数据")
public class PlayerControlRecordDocData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "uid")
    private String uid;

    @ApiModelProperty(value = "玩家uid")
    private String playerUid;

    @ApiModelProperty(value = "游戏版本")
    private String gameVersion;

    @ApiModelProperty(value = "记录版本")
    private Integer recordVersion;

    @ApiModelProperty(value = "怪物路由id")
    private Long animalRouteUid;

    @ApiModelProperty(value = "怪物id")
    private Integer animalId;

    @ApiModelProperty(value = "子弹id")
    private Integer bulletId;

    @ApiModelProperty(value = "枪id")
    private Integer gunId;

    @ApiModelProperty(value = "枪等级")
    private Integer gunLevel;

    @ApiModelProperty(value = "")
    private Integer windId;

    @ApiModelProperty(value = "是否怪物击杀")
    private Boolean isAnimalKilled;

    @ApiModelProperty(value = "最终分数")
    private Integer finalScore;

    @ApiModelProperty(value = "平均射击精准度")
    private Double averageShowPrecision;

    @ApiModelProperty(value = "是否已经归档到精度分布数据库")
    private Boolean isArchivedToDistributionDatabase;

    @ApiModelProperty(value = "来源")
    private PlayerControlRecordSource source;

    @ApiModelProperty(value = "压缩文件")
    private String rawDataBase64;

    @ApiModelProperty(value = "加载时间")
    private Long uploadTime;
}
