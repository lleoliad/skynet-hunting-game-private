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
@ApiModel(value = "PlayerControlRecordArchiveData对象", description = "播放器控制记录存档数据")
public class PlayerControlRecordArchiveData {

    @ApiModelProperty(value = "存档id")
    private String archiveUid;

    @ApiModelProperty(value = "比赛id")
    private String huntingMatchUid;

    @ApiModelProperty(value = "玩家uid")
    private String playerUid;

    @ApiModelProperty(value = "玩家奖杯计数")
    private Integer playerTrophyCount;

    @ApiModelProperty(value = "获得最高奖杯的玩家")
    private Integer playerHighestTrophyCount;

    @ApiModelProperty(value = "章节id")
    private Integer chapterId;

    @ApiModelProperty(value = "章节中玩家的奖杯数量")
    private Integer playerTrophyCountInChapter;

    @ApiModelProperty(value = "玩家枪id")
    private Integer playerGunId;

    @ApiModelProperty(value = "玩家枪等级id")
    private Integer playerGunLevel;

    @ApiModelProperty(value = "玩家子弹id")
    private Integer playerBulletId;

    @ApiModelProperty(value = "匹配id")
    private Integer matchId;

    @ApiModelProperty(value = "玩家最终总分是")
    private Integer playerTotalScore;

    @ApiModelProperty(value = "更新时间")
    private Long uploadTime;

    @ApiModelProperty(value = "编码字节")
    private String[] encodedBytes_Base64;
}

