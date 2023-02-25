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
@ApiModel(value = "RankOtherPlayerDetail对象", description = "段位列表中的其他玩家的详细信息")
public class RankOtherPlayerDetail {

    @ApiModelProperty("段位")
    private Integer rank;

    @ApiModelProperty("最高段位id")
    private Integer bestRank;

    @ApiModelProperty("昵称")
    private String nickName;

    @ApiModelProperty("头像")
    private String headPic;

    @ApiModelProperty("金币数量")
    private Long coin;

    @ApiModelProperty("总共获得的金币数量")
    private Long totalEarnedCoin;

    @ApiModelProperty("累计比赛次数")
    private Integer totalBattleCount;

    @ApiModelProperty("比赛胜利次数")
    private Integer totalWinCount;

    @ApiModelProperty("比赛胜率")
    private Double winningPercentage;

    @ApiModelProperty("比赛连胜次数")
    private Integer winningStreak;

    @ApiModelProperty("最高比赛连胜次数")
    private Integer bestWinningStreak;

    @ApiModelProperty("平均射击准确率")
    private Double averagePrecision;

    @ApiModelProperty("总共击杀次数")
    private Integer totalKill;

    @ApiModelProperty("完美击杀次数")
    private Integer perfectKill;

    @ApiModelProperty("完美击杀率")
    private Double perfectPrecision;

    @ApiModelProperty("击中头部次数")
    private Integer headShotTimes;

    @ApiModelProperty("击中心脏次数")
    private Integer heartShotTimes;

}
