package org.skynet.service.provider.hunting.obsolete.pojo.bo;

import org.skynet.components.hunting.game.data.BulletReward;
import org.skynet.components.hunting.game.data.ChestOpenResult;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.RewardGunInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "LuckyWheelV2SpinRewardBO对象", description = "转盘v2奖励")
public class LuckyWheelV2SpinRewardBO {

    @ApiModelProperty(value = "奖励下标")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer rewardIndex;

    @ApiModelProperty(value = "奖励金币")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer rewardCoin;

    @ApiModelProperty(value = "宝箱结果")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ChestOpenResult chestOpenResult;

    @ApiModelProperty(value = "子弹奖励")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<BulletReward> bulletRewards;


    @ApiModelProperty(value = "枪支奖励")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RewardGunInfo rewardGunInfo;

}
