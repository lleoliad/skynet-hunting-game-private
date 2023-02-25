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
@ApiModel(value = "PromotionEventPackageTableValue对象", description = "促销活动礼包数据库表")
public class PromotionEventPackageTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "礼包类型")
    private Integer packageType;

    @ApiModelProperty(value = "礼包级别")
    private Integer packageLevel;

    @ApiModelProperty(value = "宝箱类型")
    private Integer chestType;

    @ApiModelProperty(value = "钻石奖励")
    private Integer diamond;

    @ApiModelProperty(value = "枪械奖励ID")
    private List<Integer> rewardGunIDsArray;

    @ApiModelProperty(value = "枪械奖励数量")
    private List<Integer> rewardGunCountsArray;

    @ApiModelProperty(value = "子弹奖励ID")
    private List<Integer> rewardBulletIDsArray;

    @ApiModelProperty(value = "子弹奖励数量")
    private List<Integer> rewardBulletCountArray;
}
