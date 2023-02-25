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
@ApiModel(value = "ChapterTableValue对象", description = "单个章节的属性信息")
public class ChapterTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "解锁需要的奖杯数")
    private Integer unlockRequiresTrophyCount;

    @ApiModelProperty(value = "胜利获得奖杯数")
    private Integer winTrophyIncreaseCount;

    @ApiModelProperty(value = "失败减少奖杯数")
    private Integer loseTrophyDecreaseCount;

    @ApiModelProperty(value = "可获得奖杯上限数")
    private Integer maxAvailableTrophyCount;

    @ApiModelProperty(value = "入场费用")
    private Integer entryFee;

    @ApiModelProperty(value = "胜利奖励")
    private Integer reward;

    @ApiModelProperty(value = "局数")
    private Integer round;

    @ApiModelProperty(value = "赛事id")
    private List<Integer> matchIdArray;

    @ApiModelProperty(value = "循环赛事id")
    private List<Integer> loopMatchIdsArray;

    @ApiModelProperty(value = "比赛路线动物顺序")
    private List<Integer> matchRouteAnimalSequenceArray;

    @ApiModelProperty(value = "胜利获得宝箱类型", notes = "1:木,2:银,3:金,4:白金,5:king")
    private List<Integer> winRewardChestTypeArray;

    @ApiModelProperty(value = "胜利获得宝箱类型循环", notes = "1:木,2:银,3:金,4:白金,5:king")
    private List<Integer> fallbackWinRewardChestTypeArray;

    @ApiModelProperty(value = "强制使用偏向胜利回合匹配的章节进入次数（用于AI匹配）")
    private Integer forceWinProneRoundMatchChapterEnterCount;

    @ApiModelProperty(value = "强制使用回合匹配的章节进入次数（用于AI匹配）")
    private Integer forceRoundMatchChapterEnterCount;

    @ApiModelProperty(value = "保底胜率（用于AI匹配）")
    private Double minGuaranteeWinRate;

    @ApiModelProperty(value = "较低胜率（用于AI匹配）")
    private Double lowWinRate;

    @ApiModelProperty(value = "较高胜率（用于AI匹配）")
    private Double highWinRate;

    @ApiModelProperty(value = "最大养成胜率加成（用于AI匹配）")
    private Double maxCultivateWinRateAddition;

    @ApiModelProperty(value = "最小养成分数（用于AI匹配）")
    private Double minCultivateScore;

    @ApiModelProperty(value = "最大养成分数（用于AI匹配）")
    private Double maxCultivateScore;

    @ApiModelProperty(value = "录像奖杯分段（用于AI匹配）")
    private List<Integer> recordsTrophySegmentArray;
}
