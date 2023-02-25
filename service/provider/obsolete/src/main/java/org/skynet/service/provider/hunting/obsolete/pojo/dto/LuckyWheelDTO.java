package org.skynet.service.provider.hunting.obsolete.pojo.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "LuckyWheelDTO对象", description = "处理LuckyWheel数据接收对象")
public class LuckyWheelDTO extends BaseDTO {

    @ApiModelProperty("是否是vip操作")
    private Boolean isVipSpin;

    @ApiModelProperty("是否是通过观看广告操作")
    private Boolean spinByWatchRewardAd;
}
