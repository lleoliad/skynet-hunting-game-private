package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 枪械库类型
 */
@AllArgsConstructor
@Getter
public enum GunLibraryType {

    Epic(1, "史诗级"),
    Rare(2, "稀有级"),
    Random(3, "随机"),
    Common(4, "普通"),
    ;

    private final Integer type;
    private final String message;
}
