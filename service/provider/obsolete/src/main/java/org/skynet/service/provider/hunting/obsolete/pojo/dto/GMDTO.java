package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "GMDTO对象", description = "GM服务器接收对象")
public class GMDTO extends BaseDTO {

    @ApiModelProperty("命令类型")
    private String controlType;

    @ApiModelProperty(value = "需要处理的用户数据", notes = "base64")
    private String encodeUserData;

    @ApiModelProperty("封禁时间")
    private Long[] blockTime;

}
