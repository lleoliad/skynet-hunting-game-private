package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 强制引导步骤名称（部分）
 */
@AllArgsConstructor
@Getter
public enum ForceTutorialStepNames {

    forceCompleteTutorialMatch("ForceCompleteTutorialMatch", "强制完成教程匹配"),
    forceCompleteFirstPvPMatch("ForceCompleteFirstPvPMatch", "强制完成第一个PvP匹配"),
    forceCompleteSecondPvPMatch("ForceCompleteSecondPvPMatch", "强制完成第一个PvP匹配"),
    ;

    private final String name;

    private final String description;
}
