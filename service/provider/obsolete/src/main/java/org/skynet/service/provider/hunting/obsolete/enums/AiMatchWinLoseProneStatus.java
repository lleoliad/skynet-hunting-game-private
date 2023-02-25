package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 匹配AI时胜利或失败倾向状态
 */
@AllArgsConstructor
@Getter
public enum AiMatchWinLoseProneStatus {

    WinProne(0, "赢了"),
    LoseProne(1, "输了");

    private final Integer status;
    private final String message;
}
