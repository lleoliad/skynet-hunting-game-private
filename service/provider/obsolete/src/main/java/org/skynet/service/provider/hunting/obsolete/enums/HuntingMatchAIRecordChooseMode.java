package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 比赛中，AI选择录像的模式
 */
@AllArgsConstructor
@Getter
public enum HuntingMatchAIRecordChooseMode {

    Unknown("未知"),
    WinProneRound("倾向胜利的回合选择"),
    LoseProneRound("倾向失败的回合选择"),
    drawProneRound("普通,总体上倾向于平局的回合选择"),
    LowLevelWholeMatch("低水平完整局"),
    MediumLevelWholeMatch("中水平完整局"),
    HighLevelWholeMatch("高水平完整局"),
    ;

    private final String description;
}
