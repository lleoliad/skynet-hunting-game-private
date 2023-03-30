package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.components.hunting.game.data.BulletReward;
import org.skynet.components.hunting.game.data.GunReward;
import org.skynet.components.hunting.user.dao.entity.UserData;

import java.util.List;

/**
 * 武器，子弹相关
 */
public interface WeaponService {


//
//    /**
//     * 计算武器组合的分数
//     * @param gunId
//     * @param gunLevel
//     * @param bulletId
//     * @return
//     */
//    Integer calculateWeaponCombinationScore(Integer gunId, Integer gunLevel, Integer bulletId);

    /**
     * 判断枪是否解锁
     *
     * @param userData
     * @param gunId
     * @return
     */
    boolean isGunUnlocked(UserData userData, Integer gunId);

    /**
     * 更新用户枪支信息
     *
     * @param userData
     * @param rewards
     * @param newUnlockGunIdsResult
     * @param gameVersion
     */
    void addGunToUserDataByGunIdCountData(UserData userData, List<GunReward> rewards, List<Integer> newUnlockGunIdsResult, String gameVersion);

    /**
     * 更新用户子弹信息
     *
     * @param userData
     * @param bulletRewards
     * @param gameVersion
     */
    void addBulletToUserDataByIdCountData(UserData userData, List<BulletReward> bulletRewards, String gameVersion);
}
