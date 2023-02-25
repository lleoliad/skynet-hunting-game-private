package org.skynet.service.provider.hunting.obsolete.pojo.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "PlayerUploadWholeMatchControlRecordData对象", description = "玩家上传整局比赛控制记录数据")
public class PlayerUploadWholeMatchControlRecordData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("uid")
    private String uid;

    @ApiModelProperty("游戏版本")
    private String gameVersion;

    @ApiModelProperty("比赛匹配id")
    private String huntingMatchUid;

    @ApiModelProperty("玩家uid")
    private String playerUid;

    @ApiModelProperty("玩家杯数")
    private Integer playerTrophyCount;

    @ApiModelProperty(value = "安全的杯数范围", notes = "查找整局比赛录制数据时，需要确保玩家的杯数是在对应的杯数区间内。该杯数区间来自ChapterTable.RecordsTrophySegment,具体逻辑，查看方法savePlayerUploadControlRecordsAsync")
    private Integer safePlayerTrophyCount;

    @ApiModelProperty("章节id")
    private Integer chapterId;

    @ApiModelProperty("玩家枪id")
    private Integer playerGunId;

    @ApiModelProperty("玩家枪等级")
    private Integer playerGunLevel;

    @ApiModelProperty("玩家子弹id")
    private Integer playerBulletId;

    @ApiModelProperty("匹配id")
    private Integer matchId;

    @ApiModelProperty("玩家最终分数")
    private Integer playerTotalScore;

    @ApiModelProperty("更新时间")
    private Long uploadTime;

    @ApiModelProperty("比赛数据压缩文件")
    private List<String> encodedBytes_Base64;

    @ApiModelProperty(value = "是否可用", notes = "如果玩家帧率过低，该次录像不会被匹配给其他玩家，仅存档")
    private Boolean usable;

}
