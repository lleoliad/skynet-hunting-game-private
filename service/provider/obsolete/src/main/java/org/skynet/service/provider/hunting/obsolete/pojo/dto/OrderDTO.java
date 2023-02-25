package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "OrderDTO对象", description = "订单处理接收对象")
public class OrderDTO extends BaseDTO {

    @ApiModelProperty("命令类型")
    private String controlType;

    @ApiModelProperty("玩家Uid")
    private String userUid;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty("订单类型")
    private Integer orderType;

    @ApiModelProperty(value = "订单id")
    private String customOrderId;

    @ApiModelProperty("产品数量")
    private Integer count;

    @ApiModelProperty("产品金额")
    private Integer money;

    @ApiModelProperty("创建区间")
    private String createSection;

    @ApiModelProperty("订单结束区间")
    private String endSection;

    @ApiModelProperty("修改订单时传递的压缩文件")
    private String encodeData;
}
