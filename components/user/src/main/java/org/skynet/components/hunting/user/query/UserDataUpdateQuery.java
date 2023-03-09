package org.skynet.components.hunting.user.query;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.skynet.components.hunting.user.dao.entity.UserData;

import java.io.Serializable;

@ApiModel(value="UserDataLandQuery 对象", description="")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class UserDataUpdateQuery implements Serializable {

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "用户数据")
    private UserData userData;

    @ApiModelProperty(value = "更新数据")
    private JSONObject update;

}
