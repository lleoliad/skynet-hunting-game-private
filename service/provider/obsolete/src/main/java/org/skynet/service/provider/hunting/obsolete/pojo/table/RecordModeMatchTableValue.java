package org.skynet.service.provider.hunting.obsolete.pojo.table;

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
@ApiModel(value = "RecordModeMatchTableValue对象", description = "记录模式匹配数据库表值")
public class RecordModeMatchTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("章节")
    private Integer chapterId;

    @ApiModelProperty("match序列")
    private List<Integer> matchIdSequenceArray;

    @ApiModelProperty("玩家的奖杯区间")
    private List<Integer> playerTrophyRangeArray;

    @ApiModelProperty("玩家枪id")
    private Integer playerGunId;

    @ApiModelProperty("玩家枪等级")
    private Integer playerGunLevel;

    @ApiModelProperty("玩家子弹id")
    private Integer playerBulletId;

    @ApiModelProperty("权重")
    private Integer weight;
}
