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
@ApiModel(value = "RankPlayerData对象", description = "段位列表中的玩家信息")
public class RankPlayerData {

    @ApiModelProperty(value = "玩家uid")
    private String uid;

    @ApiModelProperty(value = "玩家名称")
    private String playerName;

    @ApiModelProperty(value = "玩家金币数")
    private Long playerCoin;

    @ApiModelProperty(value = "玩家头像下载地址")
    private String playerPortraitUrl;


}
