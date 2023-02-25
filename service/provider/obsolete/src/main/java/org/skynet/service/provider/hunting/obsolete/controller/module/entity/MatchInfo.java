package org.skynet.service.provider.hunting.obsolete.controller.module.entity;


import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerWeaponInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "MatchInfo对象", description = "battleMatching方法的返回对象")
@EqualsAndHashCode(callSuper = false)
public class MatchInfo {

    @ApiModelProperty(value = "攻击方玩家信息")
    private PlayerWeaponInfo attacker;

    @ApiModelProperty(value = "防守方玩家信息")
    private PlayerWeaponInfo defender;

    @ApiModelProperty(value = "章节id")
    private Integer chapterId;

    @ApiModelProperty(value = "是否是玩家先手")
    private Boolean isPlayerFirst;

    @ApiModelProperty(value = "匹配id")
    private Integer matchId;


}
