package org.skynet.service.provider.hunting.obsolete.pojo.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.skynet.commons.hunting.user.domain.ChestData;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ChestOpenResult对象", description = "箱子打开结果")
public class ChestOpenResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "箱子")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ChestData chestData;

    @ApiModelProperty(value = "金币")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer coin;

//    @ApiModelProperty(value = "vip加成的金币数量",notes = "coin中已经包含了该部分")
//    private Integer vipAdditionCoin;

    @ApiModelProperty(value = "钻石")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer diamond;

    @ApiModelProperty(value = "枪支奖励")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<GunReward> gunRewards;

    @ApiModelProperty(value = "新的未解锁枪支id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Integer> newUnlockedGunIDs;

    @ApiModelProperty(value = "子弹奖励")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<BulletReward> bulletRewards;


}


