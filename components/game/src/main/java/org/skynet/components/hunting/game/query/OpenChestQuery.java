package org.skynet.components.hunting.game.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;

@ApiModel(value="OpenChestQuery 对象", description="")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class OpenChestQuery implements Serializable {

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "箱子id")
    String uid;

    @ApiModelProperty(value = "箱子类型")
    Integer chestType;

    @ApiModelProperty(value = "箱子等级")
    Integer level;

    @ApiModelProperty(value = "创建时间")
    Long createTime;

    @ApiModelProperty(value = "段位宝箱奖励加成-只向枪械")
    float rankAdditionValue;
}
