package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态
 */
@AllArgsConstructor
@Getter
public enum OrderState {

    Place(0, "下订单"),
    Completed(1, "已完成"),
    Verifying(2, "订单正在校验"),
    VerifyFailed(3, "校验失败"),
    VerifySuccess(4, "校验成功"),
    Overdue(5, "已过期"),
    ;
    private final Integer type;
    private final String description;
}
