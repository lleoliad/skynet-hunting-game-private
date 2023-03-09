package org.skynet.commons.hunting.user.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@ApiModel(value = "ChestOpenIndexMap对象", description = "宝箱内容表格index")
@Data
// @AllArgsConstructor
// @NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ChestOpenIndexMap implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "枪奖励内容index")
    HashMap<String, Integer> gunRewardIndexMap = new LinkedHashMap<>();

    @ApiModelProperty(value = "种枪卡库抽取次数")
    Map<Integer, Integer> gunLibraryDrawCountMap = new LinkedHashMap<>();

    @ApiModelProperty(value = "子弹奖励内容index")
    HashMap<String, Integer> bulletRewardIndexMap = new LinkedHashMap<>();

    @ApiModelProperty(value = "金币和钻石奖励内容index")
    Integer coinAndDiamondRewardIndex = 0;
}
