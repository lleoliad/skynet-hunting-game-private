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
@ApiModel(value = "LuckyWheelV2PropertyTableValue对象", description = "幸运转盘V2版属性表")
public class LuckyWheelV2PropertyTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("转盘功能第几天启用（从注册算起）")
    private Integer FunctionEnableDayFromSignUp;
    @ApiModelProperty("初始免费使用次数")
    private Integer DefaultFreeSpinCount;
    @ApiModelProperty("免费使用恢复1个需要的时间（秒）")
    private Long FreeSpinIncreaseOnceSeconds;
    @ApiModelProperty("转盘奖励类型6对应的子弹id")
    private Integer RewardType6BulletId;
    @ApiModelProperty("转盘奖励类型7对应的子弹id")
    private Integer RewardType7BulletId;
    @ApiModelProperty("转盘奖励类型8对应的子弹id")
    private Integer RewardType8BulletId;


}
