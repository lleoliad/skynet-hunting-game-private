package org.skynet.components.hunting.user.query;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.skynet.components.hunting.user.dao.entity.UserData;

import java.io.Serializable;

@ApiModel(value="PlayerCreateQuery 对象", description="")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class PlayerCreateQuery implements Serializable {

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "用户数据")
    private UserData userData;

}
