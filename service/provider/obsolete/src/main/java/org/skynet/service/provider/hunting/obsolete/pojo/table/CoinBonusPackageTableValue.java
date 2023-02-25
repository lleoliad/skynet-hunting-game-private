package org.skynet.service.provider.hunting.obsolete.pojo.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CoinBonusPackageTable对象", description = "金币礼包数据库表")
public class CoinBonusPackageTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("对应章节")
    private Integer id;

    @ApiModelProperty("金币数量")
    private Integer coinAmount;

    @ApiModelProperty("现价")
    private Integer diamondPrice;
}
