package org.skynet.service.provider.hunting.obsolete.module.dto;

import org.skynet.service.provider.hunting.obsolete.module.entity.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UserDto对象", description = "用来创建用户战斗信息")
@EqualsAndHashCode(callSuper = false)
public class UserInfoDto extends BaseDTO {

    @ApiModelProperty(value = "用户信息")
    private UserInfo userInfo;

}
