package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@ApiModel(value = "ResponseDTO对象", description = "通常返回客户端的信息")
public class StartUnlockDTO extends BaseDTO {

    @ApiModelProperty("位置索引")
    private Integer slotIndex;

    @ApiModelProperty("箱子可开启的时间")
    private Long chestAvailableTime;
}
