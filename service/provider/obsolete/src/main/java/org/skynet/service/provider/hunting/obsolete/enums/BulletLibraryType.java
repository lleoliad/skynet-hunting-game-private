package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 子弹库类型
 */
@AllArgsConstructor
@Getter
public enum BulletLibraryType {


    Random(1, "随机"),
    HighChance(2, "高级"),
    LowChance(3, "低级"),
    ;

    private final Integer type;
    private final String message;
}
