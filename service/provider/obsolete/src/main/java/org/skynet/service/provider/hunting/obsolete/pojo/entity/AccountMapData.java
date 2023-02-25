package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "AccountMapData对象", description = "游客账号绑定情况")
public class AccountMapData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("玩家Id")
    private String userId;

    @ApiModelProperty("关联游戏ID")
    private String linkedPlayerUid;
}
