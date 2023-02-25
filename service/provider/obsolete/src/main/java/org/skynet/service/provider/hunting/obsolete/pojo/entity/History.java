package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "History对象", description = "玩家历史数据")
public class History implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "通过比赛获得的金币总数")
    private Long totalEarnedCoinByMatch;

    @ApiModelProperty(value = "总共获得的金币总数")
    private Long totalEarnedCoin;

    @ApiModelProperty(value = "总共获得的钻石总数")
    private Long totalEarnedDiamond;

    @ApiModelProperty(value = "历史最高奖杯数")
    private Integer highestTrophyCount;

    @ApiModelProperty(value = "当前连胜次数")
    private Integer currentMatchWinStreak;

    @ApiModelProperty(value = "最高连胜次数")
    private Integer bestMatchWinStreak;

    @ApiModelProperty(value = "当前连败次数")
    private Integer currentMatchLoseStreak;

    @ApiModelProperty(value = "平均准确率")
    private Double matchAverageHitPrecision;

    @ApiModelProperty(value = "累计击杀数")
    private Integer totalAnimalKillAmount;

    @ApiModelProperty(value = "perfect击杀次数")
    private Integer perfectAnimalKillAmount;

    @ApiModelProperty(value = "击中头部次数")
    private Integer headShotTimes;

    @ApiModelProperty(value = "击中心脏次数")
    private Integer heartShotTimes;

    @ApiModelProperty(value = "累计付费")
    private Double accumulateMoneyPaid;

    @ApiModelProperty(value = "付费次数")
    private Integer moneyPaidCount;

    @ApiModelProperty(value = "比赛一共射击次数")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer server_only_matchTotalShots;

    @ApiModelProperty(value = "比赛中所有射击的累计准确度")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double server_only_matchAllShotsPrecisionAccumulation;

//    @ApiModelProperty(value = "最高段位 id")
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    private Integer bestRankId;


}
