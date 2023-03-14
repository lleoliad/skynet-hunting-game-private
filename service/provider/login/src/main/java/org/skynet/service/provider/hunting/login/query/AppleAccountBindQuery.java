package org.skynet.service.provider.hunting.login.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.ClientBuildInAppInfo;

import java.io.Serializable;

@ApiModel(value="AppleAccountBindQuery 对象", description="")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppleAccountBindQuery implements Serializable {

    @ApiModelProperty(value = "游戏版本")
    private String gameVersion;

    @ApiModelProperty(value = "用户uid")
    private String userUid;

    @ApiModelProperty(value = "用户token")
    private String userToken;

    @ApiModelProperty(value = "客户端时间")
    private Long clientTime;

    @ApiModelProperty(value = "平台")
    private String platform;

    @ApiModelProperty(value = "管理员钥匙")
    private String adminKey;

    @ApiModelProperty(value = "是否是快速转发", notes = "玩家登陆的时候,会集中处理之前没有执行完的所有服务器方法,这时候可能需要特殊处理")
    private Boolean isFastForwarding;

    @ApiModelProperty(value = "客户端app信息")
    private ClientBuildInAppInfo clientBuildInAppInfo;

    @ApiModelProperty("设备ID")
    private String deviceId;

    @ApiModelProperty("用户更新次数")
    private Integer userDataUpdateCount;

    @ApiModelProperty("请求id")
    private Integer requestId;

    @ApiModelProperty("请求id")
    private Integer requestRandomId;

    @ApiModelProperty(value = "重试次数")
    private Integer retry;

    @ApiModelProperty(value = "系统便宜时间")
    private Long serverTimeOffset = 0L;

    @ApiModelProperty(value = "Identity Token")
    private String identityToken;
}
