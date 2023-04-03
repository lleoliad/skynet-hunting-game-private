package org.skynet.components.hunting.game.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.skynet.components.hunting.user.dao.entity.UserData;

import java.io.Serializable;

@ApiModel(value="ConsumeBulletQuery 对象", description="")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class MatchConsumeBulletQuery implements Serializable {

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "枪id")
    private Integer gunId;

    @ApiModelProperty(value = "枪等级")
    private Integer gunLevel;

    @ApiModelProperty(value = "子弹id")
    private Integer bulletId;

    @ApiModelProperty(value = "玩家数据")
    private UserData userData;

}
