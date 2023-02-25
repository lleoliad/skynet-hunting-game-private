package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CreateDataBaseDTO对象", description = "创建已有所有录像的分布数据库接收对象")
public class EquipBulletDTO extends BaseDTO {

    @ApiModelProperty("子弹id")
    private Integer bulletId;
}
