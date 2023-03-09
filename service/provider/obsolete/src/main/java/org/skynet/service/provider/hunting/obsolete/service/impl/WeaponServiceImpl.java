package org.skynet.service.provider.hunting.obsolete.service.impl;

import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.BulletReward;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.GunReward;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.GunTableValue;
import org.skynet.service.provider.hunting.obsolete.service.WeaponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WeaponServiceImpl implements WeaponService {


//    @Override
//    public Integer calculateWeaponCombinationScore(Integer gunId, Integer gunLevel, Integer bulletId) {
//
//        Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap;
//
//        GunTableValue gunTableValue = gunTable.get(String.valueOf(gunId));
//
//        Map<String, BulletTableValue> bulletTable = GameEnvironment.bulletTableValueMap;
//        Integer gunScore = gunTableValue.getAiMatchScoreArray().get(gunLevel - 1);
//
//        BulletTableValue bulletTableValue = bulletTable.get(String.valueOf(bulletId));
//
//        return Math.round(gunScore * bulletTableValue.getAiMatchScoreFactor());
//
//    }


    public boolean isGunUnlocked(UserData userData, Integer gunId) {

        return userData.getGunLevelMap().containsKey(gunId);
    }

    @Override
    public void addGunToUserDataByGunIdCountData(UserData userData, List<GunReward> gunIdCountData, List<Integer> newUnlockGunIdsResult, String gameVersion) {

        for (GunReward reward : gunIdCountData) {
            int current = userData.getGunCountMap().getOrDefault(reward.getGunId(), 0);
            userData.getGunCountMap().put(reward.getGunId(), current + reward.getCount());
        }

        //检查是否是新获得的枪械
        if (newUnlockGunIdsResult != null) {
            for (GunReward data : gunIdCountData) {

                if (!newUnlockGunIdsResult.contains(data.getGunId()) && !isGunUnlocked(userData, data.getGunId())) {

                    Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
                    GunTableValue gunTableValue = gunTable.get(String.valueOf(data.getGunId()));
                    if (gunTableValue == null) {
                        throw new BusinessException("没有找到枪械" + data.getGunId());
                    }

                    Integer gunCount = userData.getGunCountMap().get(data.getGunId());
                    if (gunCount >= gunTableValue.getUnlockPiecesRequires()) {

                        log.info("解锁枪械" + data.getGunId());
                        userData.getGunCountMap().put(data.getGunId(), gunCount - gunTableValue.getUnlockPiecesRequires());
                        userData.getGunLevelMap().put(data.getGunId(), 1);
                        newUnlockGunIdsResult.add(data.getGunId());
                    }
                }
            }
        }
    }

    @Override
    public void addBulletToUserDataByIdCountData(UserData userData, List<BulletReward> bulletRewards, String gameVersion) {

        for (BulletReward data : bulletRewards) {

            Integer current = userData.getBulletCountMap().getOrDefault(data.getBulletId(), 0);

            userData.getBulletCountMap().put(data.getBulletId(), current + data.getCount());
        }
    }
}
