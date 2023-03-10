package org.skynet.service.provider.hunting.login.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.skynet.components.hunting.user.data.ClientUserData;

import java.io.Serializable;

@ApiModel(value = "LoginBO 对象", description = "BO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "客户端用户数据")
    private ClientUserData userData;

    @ApiModelProperty(value = "Token")
    private String userToken;

    @ApiModelProperty(value = "通讯序列号")
    private Long requestId;

    @ApiModelProperty(value = "A/B包")
    private Integer abTestGroup;

    @ApiModelProperty(value = "禁用客户端比赛打点")
    private Boolean disableClientHuntingMatchReport;

    @ApiModelProperty(value = "最新的版本号")
    private String latestClientGameVersion;

    @ApiModelProperty(value = "标准0点时区偏移值")
    private Long standardTimeOffset;

    @ApiModelProperty(value = "vip功能解锁时间（注册后）")
    private Integer vipV2FunctionUnlockDay;

    @ApiModelProperty(value = "转盘功能第几天启用（从注册算起）")
    private Integer luckyWheelV2FunctionUnlockDay;

    @ApiModelProperty(value = "根据玩家过往内购数据，推荐礼包弹出的时候，应该选择哪个价位的")
    private Double giftPackagePopUpRecommendPrice;

    @ApiModelProperty(value = "私钥")
    private String privateKey;

    @ApiModelProperty(value = "服务器时间")
    private Long serverTime;
}
