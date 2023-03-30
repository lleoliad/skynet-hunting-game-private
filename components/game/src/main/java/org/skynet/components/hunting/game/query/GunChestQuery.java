package org.skynet.components.hunting.game.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.skynet.components.hunting.user.dao.entity.UserData;

import java.io.Serializable;
import java.util.Map;

@ApiModel(value="OpenChestQuery 对象", description="")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class GunChestQuery implements Serializable {

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "箱子id")
    String uid;

    @ApiModelProperty(value = "金币")
    private Integer coin;

    @ApiModelProperty(value = "钻石")
    private Integer diamond;

    @ApiModelProperty(value = "箱子类型")
    Integer chestType;

    @ApiModelProperty(value = "箱子等级")
    Integer level;

    @ApiModelProperty(value = "段位宝箱奖励加成-只向枪械")
    float rankAdditionValue;

    @ApiModelProperty(value = "Epic枪数量", notes = "红")
    private Integer epicGunCount;

    @ApiModelProperty(value = "Rare枪数量", notes = "橙")
    private Integer rareGunCount;

    @ApiModelProperty(value = "Random枪数量", notes = "蓝")
    private Integer randomGunCount;

    @ApiModelProperty(value = "奖励子弹数", notes = "")
    private Map<Integer, Integer> rewardBulletCountMap;

    @ApiModelProperty(value = "玩家数据")
    private UserData userData;



}
