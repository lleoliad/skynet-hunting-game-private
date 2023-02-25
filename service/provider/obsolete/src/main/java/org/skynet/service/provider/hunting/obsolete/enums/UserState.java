package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态
 */
@AllArgsConstructor
@Getter
public enum UserState {

    Normal(0, "正常"),
    Block(-1, "封禁");

    private final Integer type;
    private final String description;
}
