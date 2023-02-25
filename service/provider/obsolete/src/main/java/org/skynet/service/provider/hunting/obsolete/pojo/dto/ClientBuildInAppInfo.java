package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ClientBuildInAppInfo对象", description = "客户端App消息")
public class ClientBuildInAppInfo {

    @ApiModelProperty(value = "当前服务器是否支持录制模式")
    private Boolean recordOnlyMode;
}
