package org.skynet.service.provider.hunting.obsolete.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.skynet.components.hunting.user.domain.AchievementData;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.enums.AchievementType;
import org.skynet.service.provider.hunting.obsolete.enums.AnimalSizeType;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerControlRecordData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerFireDetails;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.AchievementTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.AnimalTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.ChapterTableValue;
import org.skynet.service.provider.hunting.obsolete.service.AchievementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AchievementServiceImpl implements AchievementService {


    /**
     * 奖杯成就
     *
     * @param userData              用户
     * @param trophyChangeInChapter 还未获得奖杯
     * @param chapterTableValueId   章节id
     * @param gameVersion
     */
    @Override
    public void updateTrophyAchievementData(UserData userData, Integer trophyChangeInChapter, Integer chapterTableValueId, String gameVersion) {

        //加载成就数据
        Map<String, AchievementTableValue> achievementTable = GameEnvironment.achievementTableMap.get(gameVersion);

        //获得奖杯
        {
            AchievementData achievementData = getAchievementDataFromUserData(userData, AchievementType.wonTrophy, gameVersion);
            if (!isAchievementFullyCompleted(achievementData)) {
                achievementData.setCurrentProgress(achievementData.getCurrentProgress() + trophyChangeInChapter);
            }
        }

        //在某个章节以上获得奖杯
        {
            AchievementData achievementData = getAchievementDataFromUserData(userData, AchievementType.wonTrophyInChapter, gameVersion);
            if (!isAchievementFullyCompleted(achievementData)) {
                Integer achievementId = achievementData.getAchievementId();
                AchievementTableValue achievementTableValue = achievementTable.get(String.valueOf(achievementId));
                Integer trophyCountInChapter = userData.getChapterWinTrophyCountMap().get(achievementTableValue.getCondition());
                achievementData.setCurrentProgress(trophyCountInChapter);
            }
        }
    }


    /**
     * 获取玩家的任务数据
     *
     * @param userData        玩家
     * @param achievementType 成就类型
     * @param gameVersion
     */
    @Override
    public AchievementData getAchievementDataFromUserData(UserData userData, AchievementType achievementType, String gameVersion) {

        Map<String, AchievementTableValue> achievementTable = GameEnvironment.achievementTableMap.get(gameVersion);

        for (AchievementData achievement : userData.getAchievements()) {

            Integer achievementId = achievement.getAchievementId();
            AchievementTableValue tableValue = achievementTable.get(String.valueOf(achievementId));

            if (tableValue.getAchievementType().equals(achievementType.getType())) {

                return achievement;
            }
        }

        throw new BusinessException("玩家" + userData.getUuid() + "没有任务" + achievementType + "的数据");
    }

    /**
     * 看某个任务是否完全完成了
     *
     * @param achievementData
     * @return
     */
    @Override
    public Boolean isAchievementFullyCompleted(AchievementData achievementData) {
        return achievementData.getFullyCompleted() != null && achievementData.getFullyCompleted();
    }

    @Override
    public void updateMatchDoneAchievementData(String uuid, Integer chapterId, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);

        updateMatchDoneAchievementData(userData, chapterId, gameVersion);
    }

    @Override
    public void updateMatchDoneAchievementData(UserData userData, Integer chapterId, String gameVersion) {

        Map<String, AchievementTableValue> achievementTable = GameEnvironment.achievementTableMap.get(gameVersion);

        AchievementData achievementData = getAchievementDataFromUserData(userData, AchievementType.completeChapterOrAboveMatch, gameVersion);

        if (isAchievementFullyCompleted(achievementData)) {
            return;
        }

        AchievementTableValue tableValue = achievementTable.get(String.valueOf(achievementData.getAchievementId()));

        if (chapterId >= tableValue.getCondition()) {
            achievementData.setCurrentProgress(achievementData.getCurrentProgress() + 1);
        }
    }

    @Override
    public void updateAnimalKillAchievementData(String uuid, Integer chapterId, List<PlayerFireDetails> playerFireDetails, String gameVersion, List<PlayerControlRecordData> allPlayerControlRecordData) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);
        updateAnimalKillAchievementData(userData, chapterId, playerFireDetails, gameVersion, allPlayerControlRecordData);
    }

    @Override
    public void updateAnimalKillAchievementData(UserData userData, Integer chapterId, List<PlayerFireDetails> playerFireDetails, String gameVersion, List<PlayerControlRecordData> allPlayerControlRecordData) {
        if (playerFireDetails == null || playerFireDetails.size() == 0) {
            return;
        }

        // Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(gameVersion);
        Map<String, AnimalTableValue> animalTable = GameEnvironment.animalTableMap.get(gameVersion);
        Map<String, AchievementTableValue> achievementTable = GameEnvironment.achievementTableMap.get(gameVersion);
        // ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(chapterId));


        for (PlayerFireDetails detail : playerFireDetails) {

            if (!detail.getIsKillAnimal()) {
                continue;
            }

            // int round = detail.getRound();
            // int animalIndex = round - 1;
            // if (animalIndex >= chapterTableValue.getMatchRouteAnimalSequence().size()) {
            //     throw new BusinessException("animalIndex >= matchRouteAnimalSequenceArray. animalIndex " + animalIndex + ", matchRouteAnimalSequenceArray:" +
            //             JSONObject.toJSONString(chapterTableValue.getMatchRouteAnimalSequence()));
            // }
            //
            // int animalId = chapterTableValue.getMatchRouteAnimalSequence().get(animalIndex);
            int animalId = detail.getAnimalId();
            AnimalTableValue animalTableValue = animalTable.get(String.valueOf(animalId));
            AnimalSizeType[] values = AnimalSizeType.values();
            AnimalSizeType animalSize = values[animalTableValue.getSizeType()];
            //动物类型击杀
            {
                AchievementData achievementData = null;
                switch (animalSize) {
                    case Undefined:
                        break;
                    case Small:
                        achievementData = getAchievementDataFromUserData(userData, AchievementType.killSmallAnimalInChapterOrAbove, gameVersion);
                        break;
                    case Medium:
                        achievementData = getAchievementDataFromUserData(userData, AchievementType.killMediumAnimalInChapterOrAbove, gameVersion);
                        break;
                    case Large:
                        achievementData = getAchievementDataFromUserData(userData, AchievementType.killLargeAnimalInChapterOrAbove, gameVersion);
                        break;
                }

                if (achievementData == null) {
                    throw new BusinessException("玩家" + userData.getUuid() + "无法找到成就数据,动物类型:" + animalSize);
                }

                if (!isAchievementFullyCompleted(achievementData)) {

                    AchievementTableValue achievementTableValue = achievementTable.get(String.valueOf(achievementData.getAchievementId()));
                    if (null != chapterId && chapterId >= achievementTableValue.getCondition()) {

                        achievementData.setCurrentProgress(achievementData.getCurrentProgress() + 1);
                        achievementData.setCurrentProgress(Math.min(achievementData.getCurrentProgress(), achievementData.getMaxProgress()));

//                        log.info("回合"+detail.getRound()+"kill animal size"+animalSize+"成就更新"+achievementData);
                    }
                }
            }

            //perfect击杀
            {
                if (detail.getIsPerfect()) {
                    AchievementData achievementData = getAchievementDataFromUserData(userData, AchievementType.perfectKillAnimalInChapterOrAbove, gameVersion);

                    if (!isAchievementFullyCompleted(achievementData)) {

                        AchievementTableValue achievementTableValue = achievementTable.get(String.valueOf(achievementData.getAchievementId()));
                        if (null != chapterId && chapterId >= achievementTableValue.getCondition()) {

                            achievementData.setCurrentProgress(achievementData.getCurrentProgress() + 1);
                            achievementData.setCurrentProgress(Math.min(achievementData.getCurrentProgress(), achievementData.getMaxProgress()));
                            log.info("回合:" + detail.getRound() + "perfect kill成就更新" + achievementData);
                        }
                    }
                }
            }
            //head shot击杀
            {
                if (detail.getIsHitHead()) {

                    AchievementData achievementData = getAchievementDataFromUserData(userData, AchievementType.killAnimalWithHeadshotInChapterOrAbove, gameVersion);

                    if (!isAchievementFullyCompleted(achievementData)) {

                        AchievementTableValue achievementTableValue = achievementTable.get(String.valueOf(achievementData.getAchievementId()));
                        if (null != chapterId && chapterId >= achievementTableValue.getCondition()) {
                            achievementData.setCurrentProgress(achievementData.getCurrentProgress() + 1);
                            achievementData.setCurrentProgress(Math.min(achievementData.getCurrentProgress(), achievementData.getMaxProgress()));
                            log.info("回合:" + detail.getRound() + "head shot 成就更新" + achievementData);
                        }
                    }
                }
            }
            //一场比赛5个以上perfect
            AchievementData achievementData = getAchievementDataFromUserData(userData, AchievementType.PerfectHuntingMatch, gameVersion);
            if (!isAchievementFullyCompleted(achievementData)) {

                int perfectShotCount = 0;
                for (PlayerControlRecordData playerControlRecordData : allPlayerControlRecordData) {
                    if (playerControlRecordData.getAverageShowPrecision() > 0.991) {
                        perfectShotCount++;
                    }
                }
//                for (PlayerFireDetails details : playerFireDetails) {
//                    if (!details.getIsPerfect()){
//                        perfectShotCount++;
//                        break;
//                    }
//                }
                if (perfectShotCount >= 5) {

                    achievementData.setCurrentProgress(achievementData.getCurrentProgress() + 1);
                    achievementData.setCurrentProgress(Math.min(achievementData.getCurrentProgress(), achievementData.getMaxProgress()));

//                    log.info("完美局数数据更新"+achievementData);
                }
            }


        }

    }

    @Override
    public void updateMatchWinStreakInChapterOrAbove(UserData userData, Integer chapterId, Boolean isWinStreak, String gameVersion) {
        if (null == chapterId || chapterId <= 0) {
            return;
        }

        Map<String, AchievementTableValue> achievementTable = GameEnvironment.achievementTableMap.get(gameVersion);
        AchievementData achievementData = getAchievementDataFromUserData(userData, AchievementType.completeChapterOrAboveWinStreak, gameVersion);

        if (isAchievementFullyCompleted(achievementData)) {
            return;
        }

        AchievementTableValue tableValue = achievementTable.get(String.valueOf(achievementData.getAchievementId()));

        if (isWinStreak) {

            if (chapterId >= tableValue.getCondition()) {

                int current = achievementData.getCurrentProgress() + 1;
                achievementData.setCurrentProgress(current);
                int count = Math.min(achievementData.getCurrentProgress(), achievementData.getMaxProgress());
                achievementData.setCurrentProgress(count);
            }
        } else {
            //如果这个成就已经完成了,就不重置
            if (achievementData.getCurrentProgress() < achievementData.getMaxProgress()) {

                achievementData.setCurrentProgress(0);
            }
        }

        log.info("章节以上连胜成就更新" + achievementData);


    }

    @Override
    public AchievementTableValue getNextLevelAchievementTableValue(Integer achievementId, String gameVersion) {

        Map<String, AchievementTableValue> achievementTable = GameEnvironment.achievementTableMap.get(gameVersion);
        AchievementTableValue achievementTableValue = achievementTable.get(String.valueOf(achievementId));
        AchievementType achievementType = AchievementType.values()[achievementTableValue.getAchievementType()];

        int currentLevel = achievementTableValue.getLevel();

        for (String key : achievementTable.keySet()) {

            AchievementTableValue tableValue = achievementTable.get(key);
            if (!tableValue.getAchievementType().equals(achievementType.getType())) {
                continue;
            }
            if (tableValue.getLevel() == currentLevel + 1) {
                return tableValue;
            }

        }
        return null;
    }
}
