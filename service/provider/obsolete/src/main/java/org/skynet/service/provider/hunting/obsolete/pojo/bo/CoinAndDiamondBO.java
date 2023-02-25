package org.skynet.service.provider.hunting.obsolete.pojo.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CoinAndDiamondBO对象", description = "返回需要金币和钻石内容")
public class CoinAndDiamondBO {

    @ApiModelProperty("金币")
    private int coin;

    @ApiModelProperty("钻石")
    private int diamond;
}
