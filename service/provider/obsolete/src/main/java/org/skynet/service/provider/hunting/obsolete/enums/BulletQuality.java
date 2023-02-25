package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 子弹质量种类
 */
@AllArgsConstructor
@Getter
public enum BulletQuality {

    White(1, "白色"),
    Blue(2, "蓝色"),
    Orange(3, "橙色"),
    // Green(3,"绿色"),
    Red(4, "红色"),
    Purple(5, "紫色"),
    ;

    private final Integer type;
    private final String message;
}
