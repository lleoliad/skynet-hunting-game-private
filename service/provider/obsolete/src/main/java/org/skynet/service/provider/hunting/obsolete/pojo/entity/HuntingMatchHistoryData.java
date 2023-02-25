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
@ApiModel(value = "HuntingMatchHistoryData对象", description = "比赛历史数据")
public class HuntingMatchHistoryData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "游戏版本")
    private String gameVersion;

    @ApiModelProperty(value = "章节id")
    private Integer chapterId;

    @ApiModelProperty(value = "匹配id")
    private Integer matchId;

    @ApiModelProperty(value = "匹配uid")
    private String matchUid;

    @ApiModelProperty(value = "回合数")
    private Integer roundCount;

    @ApiModelProperty(value = "玩家的最终得分")
    private Integer playerFinalScore;

    @ApiModelProperty(value = "ai最终得分")
    private Integer aiFinalScore;

    @ApiModelProperty(value = "玩家开火细节")
    private List<PlayerFireDetails> playerFireDetails;

    @ApiModelProperty(value = "正在进行的比赛的档案文件")
    private HuntingMatchNowData HuntingMatchNowDataArchive;

    @ApiModelProperty(value = "开始时间")
    private Long startTime;

    @ApiModelProperty(value = "结束时间")
    private Long endTime;


}
