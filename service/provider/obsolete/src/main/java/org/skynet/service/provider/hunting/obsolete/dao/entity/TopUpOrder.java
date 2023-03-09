package org.skynet.service.provider.hunting.obsolete.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import org.skynet.service.provider.hunting.obsolete.enums.OmitState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author luo hairui
 * @since 2022-07-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "TopUpOrder对象", description = "")
public class TopUpOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("订单编号")
    private String orderNumber;

    @ApiModelProperty("平台订单编号")
    private String platformOrderNumber;

    @ApiModelProperty("下订用户")
    private String userInfo;

    @ApiModelProperty("下订时间")
    private LocalDateTime orderDate;

    @ApiModelProperty("充值产品")
    private String productName;

    @ApiModelProperty("产品数量")
    private Integer goodCount = 1;

    @ApiModelProperty("充值金额")
    private Integer money;

    @ApiModelProperty(value = "订单状态", notes = "0为下订，1为付费完成，2为订单正在校验,3订单校验失败,4为订单校验成功,5为订单已经过期")
    private Integer orderState;

    @ApiModelProperty(value = "订单遗漏状态", notes = "0:遗漏 1:未遗漏,2:已补单")
    private Integer orderOmitState = OmitState.unOmit.getType();

    @ApiModelProperty("校验通知地址")
    private String httpsVerifyUrl;

    @ApiModelProperty(value = "订单类型", notes = "0为普通订单")
    private Integer orderType = 0;

    @ApiModelProperty("有效剩余天数")
    private Integer validRemainDays;

    @ApiModelProperty("订单结束时间")
    private LocalDateTime orderEndDate;

    @ApiModelProperty("首充状态")
    private Integer firstTopupState;

    @ApiModelProperty("实际支付金额")
    private String realMoney;

    @ApiModelProperty("支付方式")
    private String payMode;

    @ApiModelProperty("收据")
    private String receiptValidateResult;
}
