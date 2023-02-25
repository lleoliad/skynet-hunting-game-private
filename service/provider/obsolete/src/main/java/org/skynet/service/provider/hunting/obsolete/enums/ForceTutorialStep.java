package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 强制引导步骤
 */
@AllArgsConstructor
@Getter
public enum ForceTutorialStep {

    NOT_START(0, "未开始"),
    CHAPTER_WIN_CHEST_OPEN(1, "章节已经胜利打开箱子"),
    SWITCH_GUN(2, "切换枪支");
    private final Integer status;
    private final String message;
}
