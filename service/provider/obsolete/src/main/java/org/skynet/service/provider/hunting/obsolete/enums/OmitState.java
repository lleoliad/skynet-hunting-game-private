package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单遗漏装状态
 */
@AllArgsConstructor
@Getter
public enum OmitState {

    unOmit(0, "未遗漏"),
    Omit(1, "遗漏"),
    Supplement(2, "已补单"),
    ;
    private final Integer type;
    private final String description;
}
