package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "OpenChapterDTO对象", description = "打开宝箱接收对象")
public class OpenChapterDTO extends BaseDTO {

    @ApiModelProperty("位置索引")
    private Integer slotIndex;

    @ApiModelProperty("加速后的可获得时间")
    private Long availableUnixTimeAfterAccelerate;
}
