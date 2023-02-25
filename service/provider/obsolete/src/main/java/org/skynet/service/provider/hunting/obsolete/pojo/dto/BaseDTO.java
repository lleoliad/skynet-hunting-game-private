package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "BaseDTO对象", description = "基础接收对象")
@EqualsAndHashCode(callSuper = false)
public class BaseDTO {

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

    @ApiModelProperty(value = "段位赛分组Id")
    private String groupId;

}
