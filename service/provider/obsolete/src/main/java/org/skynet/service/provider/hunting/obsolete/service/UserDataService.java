package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.commons.hunting.user.dao.entity.UserData;
import org.skynet.commons.hunting.user.domain.*;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.InitUserDataBO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.BaseDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.DeleteGUNDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.UpdateGUNDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.UpdatePropDTO;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;


public interface UserDataService {


    /**
     * 删除关联游客账号
     *
     * @param userData
     */
    void deleteGuestAccountData(UserData userData, String deviceId);

    void saveUserProfileImage(String imageUrl, String userUid);

    /**
     * faceBook 验证
     *
     * @param userUid
     * @param accessToken
     * @return
     */
    String facebookAuthenticationValidate(String userUid, String accessToken);

    /**
     * google第三方验证
     *
     * @param userId
     * @param idToken
     * @return
     */
    GoogleIdToken.Payload googleAuthenticationValidate(String userId, String idToken) throws GeneralSecurityException, IOException;

    /**
     * 通过时间获取指定范围得的邮件
     *
     * @param userUid
     * @param pullMailCount
     * @param cursorReceiveTime
     * @param cursorMailUid
     * @return
     */
    List<MailData> getAllInboxMails(String userUid, int pullMailCount, Long cursorReceiveTime, String cursorMailUid);

    /**
     * 检查是否包含脏字
     *
     * @param newName
     * @return
     */
    boolean checkContainBadWords(String newName);

    /**
     * 获得玩家宝箱内容加成
     *
     * @param userUid
     * @return
     */
    int[] getPlayerChestRewardAddition(String userUid);

    /**
     * 获取vip的状态
     *
     * @param userData
     * @return
     */
    boolean[] getPlayerVipStatus(UserData userData);

    /**
     * 获取vip v2的状态
     *
     * @param userData
     * @return
     */
    boolean[] getPlayerVipV2Status(UserData userData);


    /**
     * 获取vip v3的状态
     *
     * @param userData
     * @return
     */
    boolean[] getPlayerVipV3Status(UserData userData);

    /**
     * 确保user data的幂等性，同一个客户端操作不要在服务器上执行两次
     *
     * @param userUid
     * @param clientUserDataUpdateCount
     * @param gameVersion
     */
    void ensureUserDataIdempotence(String userUid, Integer clientUserDataUpdateCount, String gameVersion);

    /**
     * 刷新玩家广告数据
     *
     * @param userUid
     */
    void refreshPlayerAdvertisementData(String userUid);

    /**
     * 清理玩家邮箱
     *
     * @param userUid
     */
    void cleanUpPlayerInboxMails(String userUid);

    /**
     * 刷新幸运转盘
     *
     * @param userUid
     */
    void refreshVipLuckyWheelCount(String userUid);

    /**
     * 尝试以游客身份登录
     *
     * @param userUid
     * @param deviceId
     */
    void tryLoginAsGuest(String userUid, String deviceId);

    /**
     * 创建默认的vip数据
     *
     * @return
     */
    PlayerVipData createDefaultPlayerVipData();

    /**
     * 创建默认的vip v2数据
     *
     * @return
     */
    PlayerVipV2Data createDefaultPlayerVipV2Data();


    /**
     * 创建默认的vip v3数据
     *
     * @return
     */
    PlayerVipV3Data createDefaultPlayerVipV3Data();


    /**
     * 创建默认的广告数据
     *
     * @return
     */
    PlayerAdvertisementData createDefaultPlayerAdvertisementData();

    /**
     * 创建默认的幸运转盘
     *
     * @param gameVersion
     * @return
     */
    LuckyWheelData createDefaultLuckyWheelData(String gameVersion);

    /**
     * 创建默认的幸运转盘
     *
     * @param gameVersion
     * @return
     */
    LuckyWheelV2Data createDefaultLuckyWheelV2Data(String gameVersion);

    /**
     * 游戏结算
     *
     * @param userData
     * @param sendToClientData
     * @param incrementUpdateCount
     * @param gameVersion
     * @return
     */
    Integer userDataSettlement(UserData userData, UserDataSendToClient sendToClientData, Boolean incrementUpdateCount, String gameVersion) throws IllegalAccessException;

    /**
     * 创建默认的玩家
     *
     * @param uuid        uuid
     * @param gameVersion
     * @return 默认玩家
     */
    UserData createDefaultUserData(Long userId, String uuid, String gameVersion);


    /**
     * 保存玩家信息
     *
     * @param userData
     */
    void saveUserData(UserData userData);

    /**
     * 让用户数据中没有初始化的数据初始化
     *
     * @param userData    玩家
     * @param gameVersion
     */
    UserData upgradeUser(UserData userData, String gameVersion);

    /**
     * 创建新玩家
     *
     * @param gameVersion
     * @return UserData
     */
    UserData createNewPlayer(String gameVersion);

    /**
     * 从redis中按照uuid加载用户
     *
     * @param uuid 用户的uuid
     * @return UserData
     */
    Boolean checkUserData(String uuid);


    /**
     * 给玩家到A组或者B组
     *
     * @param userData
     */
    UserData assignUserToABTestGroup(UserData userData);

    /**
     * 如果玩家数据不存在某些任务,则添加上
     *
     * @param userData    玩家
     * @param gameVersion
     * @return
     */
    UserData upgradeUserAchievementData(UserData userData, String gameVersion);

//    /**
//     * 为每个章节创建章节胜利宝箱进度数据
//     */
//    void createAllChapterWinChestProgressDataAsync(String uuid);

    /**
     * 删除某个玩家的所有正在比赛信息
     */
    void resolveUnCompleteHuntingMatchAsync(String uuid, String gameVersion);

    /**
     * 检查Userdata是否存在
     *
     * @param uuid
     */
    void checkUserDataExist(String uuid);


    /**
     * 玩家最高已解锁的章节ID
     *
     * @param userData
     * @return
     */
    Integer playerHighestUnlockedChapterID(UserData userData);

    /**
     * 载入修改并存储玩家数据,并且返回userData updateCount
     *
     * @param incrementUpdateCount
     * @param userData
     * @param gameVersion
     * @return
     */
    Integer userDataTransaction(UserData userData, Boolean incrementUpdateCount, String gameVersion);

    /**
     * 封装处理过程
     *
     * @param userData
     * @param gameVersion
     * @return
     */
    void operationUserData(UserData userData, String gameVersion);

    /**
     * 处理客户端的UserData信息
     *
     * @param userData
     * @param privateKey
     * @param loginUserUid
     * @param request
     * @return
     */
    InitUserDataBO initUserData(UserData userData, String privateKey, String loginUserUid, BaseDTO request);


//    /**
//     * 根据动物信息获取枪的信息
//     * @param userData
//     * @param animalSizeType
//     * @return
//     */
//    CheckGunContentBO getPlayerEquippedGunTableValueByAnimalSize(UserData userData, AnimalSizeType animalSizeType);

//    /**
//     * 根据动物信息获取子弹信息
//     * @param userData
//     * @param animalSizeType
//     * @return
//     */
//    BulletTableValue getPlayerEquippedBulletTableValueByAnimalSize(UserData userData, AnimalSizeType animalSizeType);

    /**
     * 计算玩家某个章节的胜率
     *
     * @param userData
     * @param chapterId
     * @return
     */
    Double calculatePlayerChapterWinRate(UserData userData, Integer chapterId);

    /**
     * 创建游客名字
     *
     * @param exceptName
     * @return
     */
    String createGuestName(String exceptName);

    /**
     * 购买后为用户添加枪械
     *
     * @param userData
     * @param rewardGunIDsArray
     * @param rewardGunCountsArray
     * @param newUnlockedGunIDs
     * @param gameVersion
     */
    void addGunToUserDataByIdAndCountArray(UserData userData, List<Integer> rewardGunIDsArray, List<Integer> rewardGunCountsArray, List<Integer> newUnlockedGunIDs, String gameVersion);

    /**
     * 购买后为用户添加子弹
     *
     * @param userData
     * @param rewardBulletIDsArray
     * @param rewardBulletCountArray
     */
    void addBulletToUserDataByIdAndCountArray(UserData userData, List<Integer> rewardBulletIDsArray, List<Integer> rewardBulletCountArray);

    /**
     * 合并枪械奖励
     *
     * @param chestOpenResult
     * @param gunsId
     * @param gunsCount
     */
    void mergeRewardGunsToChestOpenResult(ChestOpenResult chestOpenResult, List<Integer> gunsId, List<Integer> gunsCount);

    /**
     * 合并枪械奖励
     *
     * @param chestOpenResult
     * @param gunCountMap
     */
    void mergeGunCountMapToChestOpenResult(ChestOpenResult chestOpenResult, Map<Integer, Integer> gunCountMap);

    /**
     * 合并子弹奖励
     *
     * @param chestOpenResult
     * @param bulletsId
     * @param bulletsCount
     */
    void mergeRewardBulletsToChestOpenResult(ChestOpenResult chestOpenResult, List<Integer> bulletsId, List<Integer> bulletsCount);

    /**
     * 通过枪Id计数数据将枪添加到用户数据
     *
     * @param userData
     * @param gunIdCountData
     * @param newUnlockGunIdsResult
     * @param gameVersion
     */
    void addGunToUserDataByGunIdCountData(UserData userData, List<GunReward> gunIdCountData, List<Integer> newUnlockGunIdsResult, String gameVersion);

    void addBulletToUserDataByIdCountData(UserData userData, List<BulletReward> bulletIdCountData);

    /**
     * 更新用户枪支信息
     *
     * @param userData
     * @param gunCountMap
     * @param newUnlockGunIdsResult
     * @param gameVersion
     */
    void addGunToUserData(UserData userData, Map<Integer, Integer> gunCountMap, List<Integer> newUnlockGunIdsResult, String gameVersion);

    void addBulletToUserData(UserData userData, Map<Integer, Integer> bulletCountMap);

    /**
     * 通过uid获得用户信息
     *
     * @param uid
     * @return
     */
    UserData getUserData(String uid);

    /**
     * 检查用户是否被封禁
     *
     * @param uid
     * @return
     */
    boolean checkUserIfBlocking(String uid);

    /**
     * 获取所有的用户
     *
     * @return
     */
    List<UserData> getAllUserData();

    /**
     * 根据ID删除用户
     *
     * @param userUid
     */
    String removeUserData(String userUid);

    /**
     * 更新用户
     *
     * @param userData
     * @return
     */
    String updateUserData(UserData userData);

    /**
     * 踢人下线
     *
     * @param userUid
     * @return
     */
    String logoutUserData(String userUid);

    /**
     * 封禁用户
     *
     * @param userUid
     * @param blockTime
     * @return
     */
    String blockUserData(String userUid, Long[] blockTime);

    /**
     * 更新用户枪械
     *
     * @param dto
     * @return
     */
    String updateUserGun(UpdateGUNDTO dto);

    /**
     * 删除用户拥有的枪支
     *
     * @param dto
     * @return
     */
    String deleteUserGun(DeleteGUNDTO dto);

    /**
     * 更新养成数据
     *
     * @param dto
     * @return
     */
    String updateProp(UpdatePropDTO dto);

    /**
     * 将玩家分配到某个录制模式比赛表ID
     *
     * @param userUid
     * @param gameVersion
     */
    void assignPlayerRecordModeMatch(String userUid, String gameVersion);

    /**
     * 某个强制引导步骤是否完成
     *
     * @param userUid
     * @param forceStepName
     * @return
     */
    boolean isForceTutorialStepComplete(String userUid, String forceStepName);

    double calculatePlayerCultivateScore(String userUid, String gameVersion);

    void recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(UserData userData, Map<Integer, Integer> gunCountMap, String gameVersion);

    /**
     * 玩家购买vip商品
     *
     * @param userData
     * @param gameVersion
     */
    double purchaseVip(UserData userData, String productName, String gameVersion);

    /**
     * 玩家购买vipV2商品
     *
     * @param userData
     * @param gameVersion
     */
    double purchaseVipV2(UserData userData, String productName, String gameVersion);


    /**
     * 玩家购买vipV3商品
     *
     * @param userData
     * @param gameVersion
     */
    double purchaseVipV3(UserData userData, String productName, String gameVersion);

    /**
     * 刷新转盘的vip次数
     *
     * @param userData
     */
    void refreshVipV1LuckyWheelCount(UserData userData);

    /**
     * 玩家最高已解锁的章节ID
     *
     * @param userData
     */
    int getPlayerHighestUnlockedChapterID(UserData userData);

    LoginSessionData createLoginSessionData(BaseDTO request);

    void updateSessionToken(UserData userData, String token, Integer requestRandomId);

    /**
     * @param userData
     */
    void upgradePlayerChestOpenIndexMapData(UserData userData);

    void archiveServerResponse(BaseDTO responseBody, String functionName);

    Integer getUserMaxRequestIdNow(String userUid);
}
