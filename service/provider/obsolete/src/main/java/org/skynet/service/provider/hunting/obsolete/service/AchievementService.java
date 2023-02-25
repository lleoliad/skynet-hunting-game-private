package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.service.provider.hunting.obsolete.enums.AchievementType;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.AchievementData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerControlRecordData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerFireDetails;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.table.AchievementTableValue;

import java.util.List;

public interface AchievementService {

    /**
     * 奖杯成就
     *
     * @param userData              用户
     * @param trophyChangeInChapter 还未获得奖杯
     * @param chapterTableValueId   章节id
     * @param gameVersion
     */
    void updateTrophyAchievementData(UserData userData, Integer trophyChangeInChapter, Integer chapterTableValueId, String gameVersion);

    /**
     * 获取玩家的任务数据
     *
     * @param userData        玩家
     * @param achievementType 成就类型
     * @param gameVersion
     * @return AchievementData
     */
    AchievementData getAchievementDataFromUserData(UserData userData, AchievementType achievementType, String gameVersion);

    /**
     * 看某个任务是否完全完成了
     *
     * @param achievementData
     * @return
     */
    Boolean isAchievementFullyCompleted(AchievementData achievementData);

    /**
     * 完成某个章节以上比赛成就记录
     *
     * @param uuid
     * @param chapterId
     * @param gameVersion
     */
    void updateMatchDoneAchievementData(String uuid, Integer chapterId, String gameVersion);

    /**
     * 更新动物击杀
     *
     * @param uuid
     * @param chapterId
     * @param
     * @param playerFireDetails
     * @param gameVersion
     */
    void updateAnimalKillAchievementData(String uuid, Integer chapterId, List<PlayerFireDetails> playerFireDetails, String gameVersion, List<PlayerControlRecordData> playerControlRecordDataList);

    /**
     * 更新在某个章节及以上连胜成就
     *
     * @param userData
     * @param chapterId
     * @param isWinStreak
     * @param gameVersion
     */
    void updateMatchWinStreakInChapterOrAbove(UserData userData, Integer chapterId, Boolean isWinStreak, String gameVersion);

    /**
     * 获得下一个成就内容
     *
     * @param achievementId
     * @param gameVersion
     */
    AchievementTableValue getNextLevelAchievementTableValue(Integer achievementId, String gameVersion);
}
