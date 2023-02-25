package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "LoginDTO对象", description = "登录接收对象")
public class LoginDTO extends BaseDTO {

    @ApiModelProperty(value = "私钥")
    private String privateKey;

    @ApiModelProperty(value = "设备id")
    private String deviceId;

    @ApiModelProperty(value = "请求随机数")
    private Integer requestRandomId;
}
