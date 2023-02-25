package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "EquipGunDTO对象", description = "关于某把枪接收对象")
public class GunDTO extends BaseDTO {

    @ApiModelProperty("枪的id")
    private Integer gunId;
}
