package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@ApiModel(value = "PurchaseCoinPackageDTO对象", description = "记录玩家发起购买金币礼包前端返回对象")
public class PurchaseCoinPackageDTO extends BaseDTO {

    @ApiModelProperty(value = "礼包的id")
    private Integer packageId;

    @ApiModelProperty("通过广告购买")
    private Boolean getByRewardAd;
}
