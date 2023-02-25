package org.skynet.service.provider.hunting.obsolete.pojo.bo;

import org.skynet.service.provider.hunting.obsolete.pojo.table.GunTableValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CheckGunContentBO对象", description = "返回需要枪的内容")
public class CheckGunContentBO {

    @ApiModelProperty(value = "枪的属性")
    GunTableValue gunTableValue;

    @ApiModelProperty("枪的等级")
    Integer gunLevel;
}
