package org.skynet.service.provider.hunting.obsolete.pojo.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "DiamondRewardBO对象", description = "是否可以获得签到钻石奖励的消息返回值")
public class DiamondRewardBO {

    @ApiModelProperty(value = "是否可以获得")
    private Boolean canCollect;

    @ApiModelProperty(value = "钻石的数目")
    private Integer diamondReward;
}
