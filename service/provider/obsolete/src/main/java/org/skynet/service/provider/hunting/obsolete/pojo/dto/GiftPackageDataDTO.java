package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "GiftPackageDataDTO对象", description = "玩家礼包")
public class GiftPackageDataDTO extends BaseDTO {

    @ApiModelProperty("新名字")
    private String resourceVersion;

//    @ApiModelProperty("新名字")
//    private String serverTimeOffset;

    @ApiModelProperty("新名字")
    private ClientBuildInAppInfo clientBuildInAppInfo;

}
