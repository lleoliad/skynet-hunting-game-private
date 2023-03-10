package org.skynet.components.hunting.user.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@ApiModel(value = "PlayerAdvertisementDataVO对象", description = "玩家广告数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlayerAdvertisementDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("今天还剩余的激励广告次数")
    private Integer remainedRewardAdCountToday;

}
