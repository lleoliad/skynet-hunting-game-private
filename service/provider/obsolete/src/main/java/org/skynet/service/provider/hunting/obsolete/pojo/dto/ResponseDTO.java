package org.skynet.service.provider.hunting.obsolete.pojo.dto;


import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ResponseDTO对象", description = "通常返回客户端的信息")
public class ResponseDTO {

    @ApiModelProperty(value = "返回客户端的User对象")
    UserDataSendToClient userDataSendToClient;

    @ApiModelProperty(value = "code")
    private Integer code;

    @ApiModelProperty(value = "目前的unix时间(秒)")
    private Long serverTime;
}
