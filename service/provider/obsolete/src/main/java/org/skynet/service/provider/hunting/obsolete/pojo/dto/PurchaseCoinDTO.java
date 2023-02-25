package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@ApiModel(value = "PurchaseCoinDTO对象", description = "记录玩家发起购买金币前端返回对象")
public class PurchaseCoinDTO extends BaseDTO {


    @ApiModelProperty(value = "金币商店id")
    private Integer shopCoinTableId;
}
