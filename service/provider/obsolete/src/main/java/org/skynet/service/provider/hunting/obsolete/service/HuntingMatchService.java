package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.commons.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.enums.HuntingMatchAIRecordChooseMode;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.CheckNewUnlockChapterBO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.table.ChapterTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.MatchAIRoundRuleTableValue;

import java.util.List;

public interface HuntingMatchService {


    /**
     * 保存huntingMatch
     *
     * @param path
     * @param huntingMatchUUID
     * @param huntingMatchNowData
     */
    void saveHuntingMatchNowData(String path, String huntingMatchUUID, String userUid, HuntingMatchNowData huntingMatchNowData);

    /**
     * 按回合匹配重新保存HuntingMatchNowData
     *
     * @param path
     * @param huntingMatchUUID
     * @param userUid
     * @param huntingMatchNowData
     */
    void reSaveHuntingMatchNowData(String path, String huntingMatchUUID, String userUid, HuntingMatchNowData huntingMatchNowData);

    /**
     * 删除HuntingMatchNowData
     *
     * @param matchPath
     * @param huntingMatchNowUid
     */
    void removeHuntingMatchNowData(String matchPath, String userUid, String huntingMatchNowUid);

    /**
     * 删除HuntingMatchNowData
     *
     * @param gameVersion
     * @param matchUid
     */
    HuntingMatchHistoryData getHuntingMatchHistoryData(String gameVersion, String matchUid);

    /**
     * 获取玩家正在进行的比赛
     *
     * @param userUid
     */
    List<String> getPlayerHuntingMatchNowData(String userUid);

    /**
     * 保存HuntingMatchNowData
     *
     * @param matchPath
     * @param huntingMatchNowUid
     * @return
     */
    HuntingMatchNowData getHuntingMatchNowData(String matchPath, String huntingMatchNowUid);

    /**
     * 根据比赛结果,获得或失去奖杯
     *
     * @param uuid              玩家id
     * @param chapterTableValue 章节信息
     * @param isWin             是否赢
     * @param gameVersion
     */
    void winOrLoseTrophyInHuntingMatch(String uuid, ChapterTableValue chapterTableValue, boolean isWin, String gameVersion);

    /**
     * 看是进入哪场比赛
     *
     * @param userUid
     * @param chapterId
     * @param chapterEnteredCount
     * @param gameVersion
     * @return
     */
    int determineEnterWhichMatch(String userUid, Integer chapterId, Integer chapterEnteredCount, String gameVersion);


    /**
     * 生成使用的枪械信息
     *
     * @param userData
     * @return
     */
    PlayerWeaponInfo generateLocalPlayerWeaponInfo(UserData userData, String gameVersion);


//
//    /**
//     * 计算武器的AI匹配分数
//     * @param weaponInfo
//     * @return
//     */
//    Integer calculateWeaponScore(PlayerWeaponInfo weaponInfo);


    /**
     * 生成本次比赛对手信息
     *
     * @param userData
     * @param chapterId
     * @param opponentTrophyCount
     * @param gameVersion
     * @return
     */
    OpponentPlayerInfo generateOpponentPlayerInfo(UserData userData, Integer chapterId, Integer opponentTrophyCount, String gameVersion);

    /**
     * 计算消费的子弹
     *
     * @param userData
     * @param useBulletId
     * @param gameVersion
     */
    void consumeBullet(UserData userData, Integer useBulletId, String gameVersion);

    /**
     * 根据控制记录数据生成开火详细信息
     *
     * @param controlRecordData
     * @return
     */
    List<PlayerFireDetails> generateFireDetailsFromControlRecordData(List<PlayerControlRecordData> controlRecordData);

    /**
     * 看是否能够解锁新章节
     *
     * @param userData
     * @param gameVersion
     * @return
     */
    CheckNewUnlockChapterBO checkNewUnlockChapter(UserData userData, String gameVersion);

    /**
     * 记录玩家完成章节比赛次数
     *
     * @param uuid
     * @param chapterTableValue
     * @param isWin
     * @param gameVersion
     * @param allPlayerControlRecordsData
     */
    void recordChapterComplete(String uuid, ChapterTableValue chapterTableValue, Integer matchId, List<PlayerFireDetails> playerFireDetails, Boolean isWin, String gameVersion, List<PlayerControlRecordData> allPlayerControlRecordsData);

    /**
     * 刷新玩家历史数据
     *
     * @param uuid
     * @param chapterTableValue
     * @param isWin
     * @param playerFireDetails
     * @param gameVersion
     */
    void refreshPlayerHistoryData(String uuid, ChapterTableValue chapterTableValue, Boolean isWin, List<PlayerFireDetails> playerFireDetails, String gameVersion);

    /**
     * 确定AI录像查找模式
     *
     * @param userUid
     * @param chapterId
     * @param gameVersion
     * @return
     */
    HuntingMatchAIRecordChooseMode determineAIRecordChooseMode(String userUid, String chapterId, String gameVersion);

    /**
     * 获得玩家在某个章节最近几场比赛中位数
     *
     * @param chapterId
     * @param userUid
     * @param gameVersion
     * @return
     */
    Integer getPlayerChapterLatestMatchMedianScore(Integer chapterId, String userUid, String gameVersion);


    /**
     * 获得玩家在某个章节最近几场比赛得分
     *
     * @param chapterId
     * @param userUid
     * @param gameVersion
     * @return
     */
    List<Integer> getPlayerChapterLatestMatchScore(Integer chapterId, String userUid, String gameVersion);

    /**
     * 保存玩家在某个章节最近几场比赛的分数
     *
     * @param userUid
     * @param chapterId
     * @param score
     * @param gameVersion
     */
    void savePlayerChapterLatestMatchScoreCollection(String userUid, Integer chapterId, Integer score, String gameVersion);

    /**
     * 确定使用哪一个MatchAIRoundRule
     * MatchAIRoundRule只针对回合匹配
     *
     * @param chapterId
     * @param chapterEnteredCount
     * @param aiRecordChooseMode
     * @param gameVersion
     * @return
     */
    MatchAIRoundRuleTableValue determineMatchAiRoundRuleTableValue(Integer chapterId,
                                                                   Integer chapterEnteredCount,
                                                                   HuntingMatchAIRecordChooseMode aiRecordChooseMode,
                                                                   String gameVersion);


    /**
     * @param path
     * @param uuid
     * @param historyData
     */
    void saveHuntingHistoryMatch(String path, String uuid, HuntingMatchHistoryData historyData);

}
