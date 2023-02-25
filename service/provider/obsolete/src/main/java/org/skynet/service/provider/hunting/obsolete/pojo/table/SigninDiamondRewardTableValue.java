package org.skynet.service.provider.hunting.obsolete.pojo.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "SigninDiamondRewardTableValue对象", description = "签到钻石奖励数据库表")
public class SigninDiamondRewardTableValue {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "钻石奖励")
    private Integer diamondReward;
}
