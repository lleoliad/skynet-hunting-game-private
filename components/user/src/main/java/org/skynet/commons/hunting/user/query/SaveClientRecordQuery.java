package org.skynet.commons.hunting.user.query;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;

@ApiModel(value="SaveClientRecordQuery 对象", description="")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class SaveClientRecordQuery implements Serializable {

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    // @ApiModelProperty(value = "", notes = "上周结算排行界面是否展示（客户端请求修改，默认是 false）")
    // private Boolean lastWeekEvaluationRankListShown;
    //
    // @ApiModelProperty(value = "", notes = "上周结算段位变化界面是否展示（客户端请求修改，默认是 false）")
    // private Boolean lastWeekEvaluationRankChangeShown;

    @ApiModelProperty(value = "客户端上报的状态记录")
    private JSONObject clientRecord;

}
