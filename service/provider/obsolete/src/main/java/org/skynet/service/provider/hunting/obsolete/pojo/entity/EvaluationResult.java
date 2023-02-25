package org.skynet.service.provider.hunting.obsolete.pojo.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EvaluationResult对象", description = "段位结算数据的组成部分")
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationResult {

    @ApiModelProperty("上周的段位结算是否完成，如果是false，不发送其他结果")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isEvaluateComplete;

    @ApiModelProperty("新段位id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<RankPlayerData> rankPlayersData;

    @ApiModelProperty("新段位id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer newRankId;


}
