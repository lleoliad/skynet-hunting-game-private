package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.domain.ChapterWinChestData;
import org.skynet.components.hunting.user.domain.ChestData;
import org.skynet.components.hunting.user.domain.FreeChestData;
import org.skynet.service.provider.hunting.obsolete.enums.BulletLibraryType;
import org.skynet.service.provider.hunting.obsolete.enums.GunLibraryType;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;

import java.util.List;
import java.util.Map;

public interface ChestService {

    /**
     * 打开邮件中宝箱
     *
     * @param mailChestContent
     * @param unlockNewGunIds
     * @param gameVersion
     */

    void openMailChest(String userUid, MailChestContent mailChestContent, List<Integer> unlockNewGunIds, String gameVersion);

    /**
     * 刷新免费箱子数据
     */
    void refreshFreeChestsData(String uuid);

    /**
     * 创建免费箱子
     *
     * @param userData
     * @param availableTime
     * @return
     */
    FreeChestData createNewFreeChest(UserData userData, Long availableTime);

//    /**
//     * 刷新章节进度宝箱数据
//     */
//    void refreshChapterProgressChestData(String uuid);

    /**
     * 生成一个章节胜利箱子
     *
     * @param chapterId
     * @param uuid
     * @return
     */
    ChapterWinChestData tryCreateChapterWinChestAsync(Integer chapterId, String uuid);

    /**
     * 获取章节赢取宝箱位置信息
     *
     * @param uuid
     * @return
     */
    Integer getChapterWinChestSlotInfo(String uuid);

    /**
     * 创建一个章节胜利宝箱
     *
     * @param chapterId
     * @param uuid
     * @param gameVersion
     * @return
     */
    ChapterWinChestData createChapterWinChest(Integer chapterId, String uuid, String gameVersion);

    /**
     * 生成一个章节胜利箱子
     *
     * @param uuid
     * @param chapterId
     * @param gameVersion
     * @return
     */
    ChapterWinChestData tryCreateChapterWinChest(String uuid, Integer chapterId, String gameVersion);

//    /**
//     *尝试增加当前章节胜利宝箱进度
//     * @param uuid
//     * @param chapterId
//     * @return
//     */
//    IncreaseChapterWinChestProgressResult tryIncreaseChapterWinChestProgress(String uuid,Integer chapterId);


//    /**
//     * 将开箱结果写到发给客户端的数据中
//     * @param uuid
//     * @param chestOpenResult
//     */
//    void populateSendToClientUserDataWithChestOpenResult(String uuid,ChestOpenResult chestOpenResult);

    /**
     * 保存玩家开箱结果
     * 注意userUpdateCount是开箱后,改变了userData.updateCount的数字
     *
     * @param chestOpenResult
     * @param userUid
     * @param userUpdateCount
     */
    void saveChestOpenResult(ChestOpenResult chestOpenResult, String userUid, Integer userUpdateCount);

    /**
     * 打开箱子,返回奖励内容
     *
     * @param userData
     * @param chestData
     * @param gameVersion
     * @return
     */
    ChestOpenResult openChest(UserData userData, ChestData chestData, String gameVersion);

    /**
     * 获取枪奖励
     *
     * @param userData
     * @param gunLibraryType
     * @param chestLevel
     * @param getGunCount
     * @param gameVersion
     * @return
     */
    List<Integer> getGunRewards(UserData userData, GunLibraryType gunLibraryType, Integer chestLevel, Integer getGunCount, String gameVersion);

    /**
     * 获取子弹奖励
     *
     * @param userData
     * @param bulletLibraryType
     * @param chestLevel
     * @param getBulletCount
     * @param gameVersion
     * @return
     */
    List<Integer> getBulletRewards(UserData userData, BulletLibraryType bulletLibraryType, Integer chestLevel, Integer getBulletCount, String gameVersion, Map<Integer, Integer> resultBulletCountMap);

    /**
     * 判断免费箱子是否可开
     *
     * @param freeChestData
     * @return
     */
    Boolean isFreeChestAvailable(FreeChestData freeChestData);


    /**
     * 获得最后一次开箱结果
     *
     * @param uid
     * @return
     */
    ArchiveChestOpenResult getLatestOpenedChestOpenResult(String uid);

    /**
     * 从枪械类型库中，抽出对应数量的枪械奖励，并将枪械id顺序放入结果中
     *
     * @param userData
     * @param gunLibraryType
     * @param chestLevel
     * @param getGunCount
     * @param enableDrawCountRequires
     * @param resultMap
     */
    Map<Integer, Integer> extractGunRewardsFromGunLibraryAsync(UserData userData, GunLibraryType gunLibraryType, int chestLevel, int getGunCount, boolean enableDrawCountRequires, Map<Integer, Integer> resultMap, String gameVersion);

    Map<Integer, Integer> extractGunRewardsFromGunLibrary(UserData userData, GunLibraryType gunLibraryType, Integer chestLevel, Integer getGunCount, String gameVersion, Boolean enableDrawCountRequires, Map<Integer, Integer> resultMap);
}
