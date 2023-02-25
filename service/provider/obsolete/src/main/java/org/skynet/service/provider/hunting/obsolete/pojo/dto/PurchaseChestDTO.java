package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@ApiModel(value = "PurchaseBulletDTO对象", description = "记录玩家发起购买子弹前端返回对象")
public class PurchaseChestDTO extends BaseDTO {

    @ApiModelProperty(value = "商店宝箱id")
    private Integer shopChestTableId;
}
