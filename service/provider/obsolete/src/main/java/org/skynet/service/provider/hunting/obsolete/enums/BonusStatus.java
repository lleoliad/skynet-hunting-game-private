package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
/**
 * 奖励发放状态
 */
public enum BonusStatus {

    COMPLETE(1, "已经发放"),
    INCOMPLETE(0, "未发放"),
    ;

    private final Integer type;
    private final String description;
}
