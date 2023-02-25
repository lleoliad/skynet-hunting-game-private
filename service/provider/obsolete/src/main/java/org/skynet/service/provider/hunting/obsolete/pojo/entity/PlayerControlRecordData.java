package org.skynet.service.provider.hunting.obsolete.pojo.entity;


import org.skynet.service.provider.hunting.obsolete.enums.PlayerControlRecordSource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.msgpack.annotation.Message;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "PlayerControlRecordData对象", description = "玩家控制记录数据")
@Message
public class PlayerControlRecordData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "玩家uid")
    private String playerUid;

    @ApiModelProperty(value = "游戏版本")
    private String gameVersion;

    @ApiModelProperty(value = "控制记录版本")
    private Integer recordVersion;

    @ApiModelProperty(value = "控制记录上传方式")
    private PlayerControlRecordSource recordDataSource;

    @ApiModelProperty(value = "控制记录id")
    private String recordUid;

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

    @ApiModelProperty(value = "风力id")
    private Integer windId;

    @ApiModelProperty(value = "是否怪物击杀")
    private Boolean isAnimalKill;

    @ApiModelProperty(value = "最终的分数")
    private Integer finalScore;

    @ApiModelProperty(value = "平均射击精准度")
    private Float averageShowPrecision;

    @ApiModelProperty(value = "所有的操作数据")
    private ControlSegmentRecordData[] allControlSegmentRecordsData;

    public Long randomSeed;
}

