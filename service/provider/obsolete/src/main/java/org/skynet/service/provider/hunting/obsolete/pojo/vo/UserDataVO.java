package org.skynet.service.provider.hunting.obsolete.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UserDataVO对象", description = "主界面用户展示对象")
@TableName("user_data")
public class UserDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "行数据id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "玩家的全局唯一id")
    private Long userId;

    @ApiModelProperty(value = "玩家的uuid")
    private String uuid;

    @ApiModelProperty(value = "玩家注册时间")
    @TableField("sign_up_time")
    private Long signUpTime;

    @ApiModelProperty(value = "玩家姓名")
    private String name;

    @ApiModelProperty(value = "账户状态", notes = "0是正常，1是封禁")
    private Byte accountStatus;

    @ApiModelProperty(value = "封禁时间")
    @TableField(exist = false)
    private Long[] blockTime = new Long[]{-1L, -1L};

    //封禁开始时间
    private Date blockStartTime;

    //封禁结束时间
    private Date blockEndTime;

    @ApiModelProperty(value = "金币数量")
    private Long coin;

    @ApiModelProperty(value = "钻石数量")
    private Long diamond;

    @ApiModelProperty(value = "总共获得的金币总数")
    @TableField(exist = false)
    private Long totalEarnedCoin;

    @ApiModelProperty(value = "总共获得的钻石总数")
    @TableField(exist = false)
    private Long totalEarnedDiamond;

    @ApiModelProperty(value = "奖杯数目")
    private Integer trophy;

    @ApiModelProperty(value = "历史最高奖杯数")
    private Integer highestTrophyCount;

    @ApiModelProperty(value = "累计付费")
    private Double accumulateMoneyPaid;

    @ApiModelProperty(value = "对局总数")
    private Integer totalGames;

    @ApiModelProperty(value = "胜利局数")
    private Integer winGames;

    @ApiModelProperty(value = "胜率")
    private Double winningProbability;

    @ApiModelProperty(value = "平均准确率")
    private Double matchAverageHitPrecision;

    @ApiModelProperty(value = "最高未解锁章节")
    private Integer highestUnlockChapterId;

    @ApiModelProperty(value = "服务器ID")
    @TableField(exist = false)
    private String serverInfoNum;
}
