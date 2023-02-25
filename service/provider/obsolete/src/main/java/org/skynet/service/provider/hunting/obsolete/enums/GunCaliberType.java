package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GunCaliberType {

    White(1, "白色"),
    Blue(2, "蓝色"),
    Orange(3, "橙色"),
    Red(4, "红色"),
    ;

    private final Integer type;
    private final String message;
}
