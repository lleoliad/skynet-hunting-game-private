package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import org.skynet.components.hunting.user.dao.entity.UserData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@ApiModel(value = "ResponseDTO对象", description = "通常返回客户端的信息")
public class SetUserDataDTO extends BaseDTO {

    @ApiModelProperty("准备设置的用户数据")
    private UserData userData;

    @ApiModelProperty("是否全部覆盖")
    private Boolean overrideUserUid;
}
