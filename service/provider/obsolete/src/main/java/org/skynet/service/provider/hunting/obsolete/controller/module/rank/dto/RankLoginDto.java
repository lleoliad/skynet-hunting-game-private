package org.skynet.service.provider.hunting.obsolete.controller.module.rank.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankLoginDto {

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "玩家昵称")
    private String nickname;

    @ApiModelProperty(value = "头像")
    private String headPic;

    @ApiModelProperty(value = "金币")
    private Long coin;


}
