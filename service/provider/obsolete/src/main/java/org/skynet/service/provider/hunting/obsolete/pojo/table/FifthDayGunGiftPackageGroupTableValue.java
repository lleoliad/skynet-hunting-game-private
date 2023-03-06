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
@ApiModel(value = "FifthDayGunGiftPackageGroupTable对象", description = "五日武器礼包Group")
public class FifthDayGunGiftPackageGroupTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "价格")
    private Double price;
    @ApiModelProperty(value = "开启时间(注册后天数)")
    private Integer enableStandardDaysAfterSignUp;
    @ApiModelProperty(value = "持续时间")
    private Long enableDurationSeconds;
    @ApiModelProperty(value = "可启用最高章节")
    private List<Integer> activeHighestChapters;
    @ApiModelProperty(value = "商品名")
    private String productName;
    @ApiModelProperty(value = "可用礼包id")
    private List<Integer> availablePackageId;


}
