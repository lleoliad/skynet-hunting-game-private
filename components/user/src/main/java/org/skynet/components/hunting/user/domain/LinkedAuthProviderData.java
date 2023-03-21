package org.skynet.components.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@ApiModel(value = "LinkedAuthProviderData对象", description = "已绑定的鉴权平台id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LinkedAuthProviderData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("谷歌玩家ID")
    private String googlePlayUserId;

    @ApiModelProperty("facebook玩家ID")
    private String facebookUserId;

    @ApiModelProperty("gameCenter玩家ID")
    private String gameCenterUserId;
}
