package org.skynet.service.provider.hunting.obsolete.module.dto;

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
@ApiModel(value = "MatchDto对象", description = "用来做匹配")
@EqualsAndHashCode(callSuper = false)
public class BattleMatchDto extends BaseDTO {

    @ApiModelProperty(value = "章节id")
    private Integer chapterId;

    @ApiModelProperty(value = "该章节较低胜率/保底胜率的养成胜率加成")
    private Double cultivateWinRateAddition;


    @ApiModelProperty(value = "玩家当前装备的武器信息")
    private PlayerWeaponInfo playerWeaponInfo;

    @ApiModelProperty(value = "是否是AI录制模式")
    private Boolean isRecordMode;

    @ApiModelProperty(value = "奖杯数目")
    private Integer trophyCount;


}
