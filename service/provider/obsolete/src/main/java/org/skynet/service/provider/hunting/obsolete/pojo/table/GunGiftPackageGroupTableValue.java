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
@ApiModel(value = "GunGiftPackageGroupTableValue对象", description = "武器礼包Group")
public class GunGiftPackageGroupTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Integer id;
    @ApiModelProperty("价格")
    private Double price;
    @ApiModelProperty("第几天可见")
    private Integer visibleDaysAfterSignUp;
    @ApiModelProperty("开启时间")
    private List<Long> enableStandardTimeArray;
    @ApiModelProperty("结束时间")
    private List<Long> disableStandardTimeArray;
    @ApiModelProperty("商品名")
    private String productName;
    @ApiModelProperty("可用礼包id")
    private List<Integer> availablePackageIdArray;


}
