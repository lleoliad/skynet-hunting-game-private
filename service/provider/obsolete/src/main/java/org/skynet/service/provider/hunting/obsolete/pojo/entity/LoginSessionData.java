package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.msgpack.annotation.Message;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "LoginSessionData对象", description = "登录session")
@AllArgsConstructor
@NoArgsConstructor
@Message
public class LoginSessionData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "开始登录时间")
    private Long startLoginTime;

    @ApiModelProperty(value = "当前token")
    private String currentToken;

    @ApiModelProperty(value = "返回随机id")
    private Integer requestRandomId;

    @ApiModelProperty(value = "重试次数")
    private Integer retry;
}
