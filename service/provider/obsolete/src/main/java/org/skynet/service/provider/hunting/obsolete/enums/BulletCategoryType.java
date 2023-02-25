package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 子弹的类别
 */
@AllArgsConstructor
@Getter
public enum BulletCategoryType {

    Standard(1, "标准"),
    Activities(2, "快速"),
    ;

    private final Integer type;
    private final String description;
}
