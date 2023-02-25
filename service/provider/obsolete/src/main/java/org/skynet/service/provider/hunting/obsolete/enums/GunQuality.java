package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GunQuality {

    White(1, "白色"),
    Blue(2, "蓝色"),
    Green(3, "绿色"),
    Red(4, "红色"),
    Orange(5, "橙色"),
    ;
    private final Integer type;
    private final String message;
}
