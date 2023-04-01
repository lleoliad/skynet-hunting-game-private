package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 箱子类型
 */
@AllArgsConstructor
@Getter
public enum ChestType {

    BRONZE(1, "铜箱子"),
    SILVER(2, "银箱子"),
    GOLD(3, "金箱子"),
    PLATINUM(4, "白金箱子"),
    KING(5, "king箱子"),
    FREE(7, "free箱子"),
    ROOKIE(11, "ROOKIE"),
    PRO(12, "PRO"),
    EXPERT(13, "EXPERT"),
    MASTER(14, "MASTER");

    private final Integer type;
    private final String message;

}
