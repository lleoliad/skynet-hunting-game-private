package org.skynet.commons.hunting.user.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@ApiModel(value="PlayerDetailInfoDTO 对象", description="DTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class PlayerDetailInfoDTO implements Serializable {

    @ApiModelProperty(value = "用户ID", notes = "创建的时候，为机器人生成一个UUID")
    private String userId;

    @ApiModelProperty(value = "历史最高段位")
    private Integer bestRank;

    @ApiModelProperty(value = "段位")
    private Integer rank;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "头像")
    private String headPic;

    @ApiModelProperty(value = "金币")
    private Long coin;

    @ApiModelProperty(value = "奖杯数目")
    private Integer trophy;

    @ApiModelProperty(value = "累计赢取金币总数")
    private Long totalEarnedCoin;

    @ApiModelProperty(value = "累计比赛总次数")
    private Integer totalBattleCount;

    @ApiModelProperty(value = "累计获胜比赛总次数")
    private Integer totalWinCount;

    @ApiModelProperty(value = "平均胜率（累计获胜比赛总次数/累计比赛总次数）")
    private Float winningPercentage;

    @ApiModelProperty(value = "当前连胜次数，一旦失败则变为0")
    private Integer winningStreak;

    @ApiModelProperty(value = "最高连胜次数，最大的连胜次数")
    private Integer bestWinningStreak;

    @ApiModelProperty(value = "平均准确率（击中的每一枪的准确率之和/击中动物的总枪数）")
    private Float averagePrecision;

    @ApiModelProperty(value = "累计击杀总次数")
    private Integer totalKill;

    @ApiModelProperty(value = "累计Perfect总次数（每次击杀最多获得一次Perfect）")
    private Integer perfectKill;

    @ApiModelProperty(value = "累计Perfect百分比（累计Perfect总次数/击杀动物总次数）")
    private Float perfectPrecision;

    @ApiModelProperty(value = "累计击中大脑总次数（每次击杀最多获得一次击中大脑）")
    private Integer headShotTimes;

    @ApiModelProperty(value = "累计击中心脏总次数（每次击杀最多获得一次击中大脑）")
    private Integer heartShotTimes;
}
