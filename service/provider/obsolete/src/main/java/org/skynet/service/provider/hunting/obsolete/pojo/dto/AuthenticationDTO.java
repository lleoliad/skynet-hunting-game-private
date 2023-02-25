package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import org.skynet.service.provider.hunting.obsolete.enums.AuthenticationProvider;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "AuthenticationDTO对象", description = "处理第三方验证数据接收对象")
public class AuthenticationDTO extends BaseDTO {

    @ApiModelProperty("认证来源")
    private AuthenticationProvider authenticationProvider;

    @ApiModelProperty("请求的userId")
    private String providerUserId;

    @ApiModelProperty("token")
    private String idToken;

}
