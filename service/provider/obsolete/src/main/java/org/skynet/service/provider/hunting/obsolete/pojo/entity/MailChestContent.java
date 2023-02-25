package org.skynet.service.provider.hunting.obsolete.pojo.entity;

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
@ApiModel(value = "MailChestContent对象", description = "邮件中的宝箱数据")
public class MailChestContent implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("宝箱")
    private ChestData chestData;

    @ApiModelProperty("金币")
    private Long coin;

    @ApiModelProperty("钻石")
    private Long diamond;

    @ApiModelProperty("枪械")
    private List<GunReward> gunRewards;

    @ApiModelProperty("子弹")
    private List<BulletReward> bulletRewards;
}
