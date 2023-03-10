package org.skynet.components.hunting.user.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@ApiModel(value = "PlayerAdvertisementData对象", description = "玩家广告数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlayerAdvertisementData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("今天还剩余的激励广告次数")
    private Integer remainedRewardAdCountToday;

    @ApiModelProperty("上一次刷新今日剩余激励广告次数的unix天")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long server_only_lastRefreshRewardAdCountUnixDay;

}
