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
@ApiModel(value = "ChapterBonusPackageTableValue对象", description = "章节奖励数据库表(2,3,4章)")
@AllArgsConstructor
@NoArgsConstructor
public class ChapterBonusPackageTableValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id(对应章节)")
    private Integer id;

    @ApiModelProperty("有效时长")
    private Long expireTime;

    @ApiModelProperty("礼包商品名称(内购productId)")
    private String productId;

    @ApiModelProperty("宝箱类型")
    private Integer chestType;

    @ApiModelProperty("宝箱等级")
    private Integer chestLevel;

    @ApiModelProperty("金币奖励")
    private Integer coin;

    @ApiModelProperty("钻石奖励")
    private Integer diamond;

    @ApiModelProperty("枪械奖励ID")
    private List<Integer> rewardGunIDs;

    @ApiModelProperty("枪械奖励数量")
    private List<Integer> rewardGunCounts;

    @ApiModelProperty("奖励枪械库类型")
    private List<Integer> rewardGunLibraryTypes;

    @ApiModelProperty("价格")
    private Double price;

}
