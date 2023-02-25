package org.skynet.service.provider.hunting.obsolete.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 成就类型
 */
@AllArgsConstructor
@Getter
public enum AchievementType {


    undefined(0, "未定义"),
    wonTrophy(1, "赢得奖杯"),
    wonTrophyInChapter(2, "在某个章节赢奖杯"),
    completeChapterOrAboveMatch(3, "完成某个章节及以上比赛"),
    completeChapterOrAboveWinStreak(4, "完成某章节及以上连胜"),
    killSmallAnimalInChapterOrAbove(5, "某章节及以上击杀小型动物"),
    killMediumAnimalInChapterOrAbove(6, "某章节及以上击杀中型动物"),
    killLargeAnimalInChapterOrAbove(7, "某章节及以上击杀大型动物"),
    perfectKillAnimalInChapterOrAbove(8, "某章节及以上Perfect击杀"),
    killAnimalWithHeadshotInChapterOrAbove(9, "某章节及以上Headshot击杀"),
    PerfectHuntingMatch(10, "一场比赛5个以上perfect"),
    ;

    private final Integer type;
    private final String message;
}
