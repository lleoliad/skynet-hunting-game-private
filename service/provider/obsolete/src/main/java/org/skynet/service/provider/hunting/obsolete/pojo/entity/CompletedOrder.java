package org.skynet.service.provider.hunting.obsolete.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "CompletedOrder对象", description = "已完成订单")
public class CompletedOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "rawData")
    private String rawData;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "玩家Uid")
    private String playerUid;

    @ApiModelProperty(value = "订单id")
    private String customOrderId;

//    @ApiModelProperty(value = "订单开始时间")
//    private Long startDate;

    @ApiModelProperty(value = "完成时间")
    private Long completeTime;
//
//    @ApiModelProperty(value = "是否已经发放奖励",notes = "0为未发放,1为已发放")
//    private BonusStatus orderOmitState = BonusStatus.INCOMPLETE;
//
//    @ApiModelProperty("支付模式")
//    private String payMode;


}
