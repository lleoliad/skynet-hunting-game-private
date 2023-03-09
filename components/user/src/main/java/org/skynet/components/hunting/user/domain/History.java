package org.skynet.components.hunting.user.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;


@ApiModel(value = "History对象", description = "玩家历史数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
// @Builder
// @EqualsAndHashCode(callSuper = false)
public class History implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "通过比赛获得的金币总数")
    private Long totalEarnedCoinByMatch = 0L;

    @ApiModelProperty(value = "总共获得的金币总数")
    private Long totalEarnedCoin = 0L;

    @ApiModelProperty(value = "总共获得的钻石总数")
    private Long totalEarnedDiamond = 0L;

    @ApiModelProperty(value = "历史最高奖杯数")
    private Integer highestTrophyCount = 0;

    @ApiModelProperty(value = "当前连胜次数")
    private Integer currentMatchWinStreak = 0;

    @ApiModelProperty(value = "最高连胜次数")
    private Integer bestMatchWinStreak = 0;

    @ApiModelProperty(value = "当前连败次数")
    private Integer currentMatchLoseStreak = 0;

    @ApiModelProperty(value = "平均准确率")
    private Double matchAverageHitPrecision = 0D;

    @ApiModelProperty(value = "累计击杀数")
    private Integer totalAnimalKillAmount = 0;

    @ApiModelProperty(value = "perfect击杀次数")
    private Integer perfectAnimalKillAmount = 0;

    @ApiModelProperty(value = "击中头部次数")
    private Integer headShotTimes = 0;

    @ApiModelProperty(value = "击中心脏次数")
    private Integer heartShotTimes = 0;

    @ApiModelProperty(value = "累计付费")
    private Double accumulateMoneyPaid = 0D;

    @ApiModelProperty(value = "付费次数")
    private Integer moneyPaidCount = 0;

    @ApiModelProperty(value = "比赛一共射击次数")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer server_only_matchTotalShots = 0;

    @ApiModelProperty(value = "比赛中所有射击的累计准确度")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double server_only_matchAllShotsPrecisionAccumulation = 0D;


}
