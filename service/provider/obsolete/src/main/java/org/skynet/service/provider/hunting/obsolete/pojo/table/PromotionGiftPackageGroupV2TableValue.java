package org.skynet.service.provider.hunting.obsolete.pojo.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "PromotionGiftPackageGroupV2对象", description = "第二版促销活动礼包组数据表")
public class PromotionGiftPackageGroupV2TableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "礼包商品名称(内购productId)")
    private String productName;

    @ApiModelProperty(value = "礼包类型")
    private List<Integer> packageTypes;

    @ApiModelProperty(value = "开启时间(相对于玩家注册事件)")
    private List<Integer> activeTime;

    @ApiModelProperty(value = "过期时间(相对于玩家注册事件)")
    private List<Integer> expireTime;

    @ApiModelProperty(value = "价格")
    private Double price;
}
