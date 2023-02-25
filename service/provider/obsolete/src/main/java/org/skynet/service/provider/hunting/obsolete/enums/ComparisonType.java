package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 比较类型
 */
@AllArgsConstructor
@Getter
public enum ComparisonType {

    Less("Less", "小于"),
    LessEqual("LessEqual", "小于等于"),
    GreaterEqual("GreaterEqual", "大于等于"),
    Greater("Greater", "大于"),
    Any("Any", "任意情况"),
    ;

    private final String type;
    private final String message;
}
