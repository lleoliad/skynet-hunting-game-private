package org.skynet.service.provider.hunting.obsolete.pojo.bo;

import org.skynet.components.hunting.user.dao.entity.UserData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "InitUserDataBO对象", description = "返回处理好的UserData和Token")
public class InitUserDataBO {

    @ApiModelProperty(value = "userData对象")
    private UserData userData;

    @ApiModelProperty(value = "token值")
    private String token;
}
