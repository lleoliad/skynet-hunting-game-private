package org.skynet.service.provider.hunting.game.service.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.game.data.BulletReward;
import org.skynet.components.hunting.game.data.ChestOpenResult;
import org.skynet.components.hunting.game.data.GunReward;
import org.skynet.components.hunting.game.data.OpenChestBO;
import org.skynet.components.hunting.game.query.GunChestQuery;
import org.skynet.components.hunting.game.query.OpenChestQuery;
import org.skynet.components.hunting.game.query.WinChestQuery;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.data.ClientUserData;
import org.skynet.components.hunting.user.data.HistoryVO;
import org.skynet.components.hunting.user.domain.ChapterWinChestData;
import org.skynet.components.hunting.user.domain.ChestData;
import org.skynet.components.hunting.user.query.UserDataLandQuery;
import org.skynet.components.hunting.user.query.UserDataUpdateQuery;
import org.skynet.components.hunting.user.service.UserFeignService;
import org.skynet.service.provider.hunting.game.service.ChestAssignmentService;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.NanoIdUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.enums.ChestType;
import org.skynet.service.provider.hunting.obsolete.enums.GunLibraryType;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.ChapterWinChestConfig;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
import org.skynet.service.provider.hunting.obsolete.service.ObsoleteUserDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
public class ChestAssignmentServiceImpl implements ChestAssignmentService {

    @Resource
    private UserFeignService userFeignService;

    @Resource
    private ChestService chestService;

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Override
    public Result<OpenChestBO> open(OpenChestQuery openChestQuery) {
        Result<UserData> userDataResult = userFeignService.load(UserDataLandQuery.builder().userId(openChestQuery.getUserId()).build());
        if (userDataResult.failed()) {
            return userDataResult.build();
        }

        UserData userData = userDataResult.getData();

        ChestOpenResult chestOpenResult = chestService.openChest(userDataResult.getData(), ChestData.builder()
                .chestType(openChestQuery.getChestType())
                .level(openChestQuery.getLevel())
                .build(), openChestQuery.getVersion(), openChestQuery.getRankAdditionValue());

        userFeignService.update(UserDataUpdateQuery.builder().userId(openChestQuery.getUserId()).userData(userData).build());
        ClientUserData clientUserData = ClientUserData.builder().build();
        if (chestOpenResult != null) {
            clientUserData.setBulletCountMap(userData.getBulletCountMap());
            clientUserData.setCoin(userData.getCoin());
            clientUserData.setDiamond(userData.getDiamond());
            clientUserData.setGunLevelMap(userData.getGunLevelMap());
            clientUserData.setGunCountMap(userData.getGunCountMap());
            clientUserData.setHistory(BeanUtil.copyProperties(userData.getHistory(), HistoryVO.class));
        }

        return Result.ok(OpenChestBO.builder().userData(clientUserData).openResult(chestOpenResult).build());
    }

    @Override
    public Result<?> winChest(WinChestQuery winChestQuery) {
        Result<UserData> loadUserDataResult = userFeignService.load(UserDataLandQuery.builder().version(winChestQuery.getVersion()).userId(winChestQuery.getUserId()).build());
        if (loadUserDataResult.failed()) {
            return loadUserDataResult.build();
        }

        UserData userData = loadUserDataResult.getData();

        Integer emptySlotIndex = chestService.getChapterWinChestSlotInfo(userData);

        if (emptySlotIndex < 0) {
            return Result.ok();
        }
        ChestType winChestType = null;
        for (ChestType cType : ChestType.values()) {
            if (cType.getType() == winChestQuery.getChestType().intValue()) {
                winChestType = cType;
            }
        }

        int chestUnlockSeconds = ChapterWinChestConfig.getChestUnlockSeconds(winChestType);

        ChapterWinChestData chapterWinChestData = new ChapterWinChestData();
        chapterWinChestData.setUid(NanoIdUtils.randomNanoId(30));
        chapterWinChestData.setChestType(winChestType.getType());
        chapterWinChestData.setLevel(winChestQuery.getChestLevel());
        chapterWinChestData.setCreateTime(TimeUtils.getUnixTimeSecond());
        chapterWinChestData.setAvailableUnixTime(-1L);
        chapterWinChestData.setUnlockSecondsRequires((long) chestUnlockSeconds);

        userData.getChapterWinChestsData().add(emptySlotIndex, chapterWinChestData);
        userFeignService.update(UserDataUpdateQuery.builder().userId(winChestQuery.getUserId()).userData(userData).build());
        // map.put("newCreateChapterWinChestData", newCreateChapterWinChestData);
        // return Result.ok().put(chapterWinChestData);
        return Result.ok().push("newCreateChapterWinChestData", chapterWinChestData).move("userData").push("chapterWinChestsData", userData.getChapterWinChestsData());
    }

    @Override
    public Result<?> gunChest(GunChestQuery gunChestQuery) {
        UserData userData = gunChestQuery.getUserData();

        if (Objects.isNull(userData)) {
            Result<UserData> userDataResult = userFeignService.load(UserDataLandQuery.builder().version(gunChestQuery.getVersion()).userId(gunChestQuery.getUserId()).build());
            if (userDataResult.failed()) {
                return userDataResult.build();
            }

            userData = userDataResult.getData();
        }

        ChestOpenResult chestOpenResult = new ChestOpenResult();
        chestOpenResult.setChestData(ChestData.builder()
                .chestType(gunChestQuery.getChestType())
                .level(gunChestQuery.getLevel())
                .build());

        if (null != gunChestQuery.getCoin()) {
            userData.setCoin(userData.getCoin() + gunChestQuery.getCoin());
            chestOpenResult.setCoin(gunChestQuery.getCoin());
        }

        if (null != gunChestQuery.getDiamond()) {
            userData.setDiamond(userData.getDiamond() + gunChestQuery.getDiamond());
            chestOpenResult.setDiamond(gunChestQuery.getDiamond());
        }

        Boolean enableDrawCountRequires = false;
        float additionValue = gunChestQuery.getRankAdditionValue();

        //gun
        Map<Integer, Integer> gunRewardMap = new HashMap<>();
        if (null != gunChestQuery.getEpicGunCount() && gunChestQuery.getEpicGunCount() > 0) {
            gunRewardMap = chestService.extractGunRewardsFromGunLibrary(userData, GunLibraryType.Epic, gunChestQuery.getLevel(), gunChestQuery.getEpicGunCount(), gunChestQuery.getVersion(), enableDrawCountRequires, gunRewardMap, additionValue);
        }

        if (null != gunChestQuery.getRareGunCount() && gunChestQuery.getRareGunCount() > 0) {
            gunRewardMap = chestService.extractGunRewardsFromGunLibrary(userData, GunLibraryType.Rare, gunChestQuery.getLevel(), gunChestQuery.getRareGunCount(), gunChestQuery.getVersion(), enableDrawCountRequires, gunRewardMap, additionValue);
        }
        if (null != gunChestQuery.getRandomGunCount() && gunChestQuery.getRandomGunCount() > 0) {
            gunRewardMap = chestService.extractGunRewardsFromGunLibrary(userData, GunLibraryType.Random, gunChestQuery.getLevel(), gunChestQuery.getRandomGunCount(), gunChestQuery.getVersion(), enableDrawCountRequires, gunRewardMap, additionValue);
        }

        if (gunRewardMap.size() > 0) {
            chestOpenResult.setGunRewards(new ArrayList<>());
            Set<Integer> keySet1 = gunRewardMap.keySet();
            for (Integer key : keySet1) {
                chestOpenResult.getGunRewards().add(new GunReward(key, gunRewardMap.get(key)));
            }

            //将枪数据加到角色数据中
            List<Integer> newUnlockGunIds = new ArrayList<>();
            obsoleteUserDataService.addGunToUserData(userData, CommonUtils.convertGunCountArrayToGunCountMap(chestOpenResult.getGunRewards()), newUnlockGunIds, gunChestQuery.getVersion());
            chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        }

        //bullet
        Map<Integer, Integer> rewardBulletCountMap = gunChestQuery.getRewardBulletCountMap();
        if (ObjectUtils.isNotEmpty(rewardBulletCountMap)) {
            chestOpenResult.setBulletRewards(new ArrayList<>());
            rewardBulletCountMap.forEach((key, value) -> chestOpenResult.getBulletRewards().add(new BulletReward(key, rewardBulletCountMap.get(key))));

            obsoleteUserDataService.addBulletToUserData(userData, rewardBulletCountMap);
        }

        // return Result.ok(chestOpenResult);
        return Result.ok().push("openResult", chestOpenResult)
                .move("userData")
                .push("coin", userData.getCoin())
                .push("diamond", userData.getDiamond())
                .push("bulletCountMap", userData.getBulletCountMap())
                .push("serverOnly", userData.getServerOnly())
                .push("gunCountMap", userData.getGunCountMap())
                .push("gunLevelMap", userData.getGunLevelMap())
                .build();
    }
}
