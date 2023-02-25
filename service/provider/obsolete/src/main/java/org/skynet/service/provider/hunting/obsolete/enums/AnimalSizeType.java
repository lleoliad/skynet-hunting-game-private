package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 动物规模
 */
@AllArgsConstructor
@Getter
public enum AnimalSizeType {

    Undefined(0, "未定义"),
    Small(1, "小动物"),
    Medium(2, "中等体型"),
    Large(3, "大型动物");

    private final Integer status;
    private String message;

    AnimalSizeType(Integer status) {
        this.status = status;
    }
}
