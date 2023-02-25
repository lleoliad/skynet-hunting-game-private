package org.skynet.service.provider.hunting.obsolete.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 枪的类别
 */
@AllArgsConstructor
@Getter
public enum GunCategoryType {

    Standard(1, "标准"),
    Surprise(2, "优秀"),
    Activities(3, "快速"),
    ;

    private final Integer type;
    private final String description;
}
