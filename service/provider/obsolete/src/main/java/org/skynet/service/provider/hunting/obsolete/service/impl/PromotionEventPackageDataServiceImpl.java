package org.skynet.service.provider.hunting.obsolete.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.NanoIdUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.config.GameConfig;
import org.skynet.service.provider.hunting.obsolete.config.IAPProductPrefix;
import org.skynet.service.provider.hunting.obsolete.enums.ForceTutorialStepNames;
import com.cn.huntingrivalserver.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import com.cn.huntingrivalserver.pojo.table.*;
import org.skynet.service.provider.hunting.obsolete.service.PromotionEventPackageDataService;
import org.skynet.service.provider.hunting.obsolete.service.UserDataService;
import lombok.extern.slf4j.Slf4j;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.table.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PromotionEventPackageDataServiceImpl implements PromotionEventPackageDataService {

    @Resource
    private UserDataService userDataService;

    /**
     * 激活活动礼包
     */
    @Override
    public void refreshPromotionEventPackagesData(String uuid, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);

        List<String> purchasedPromotionEventPackagesKeys = userData.getServerOnly().getPurchasedPromotionEventPackagesKeys();

        Long unixTimeNow = TimeUtils.getUnixTimeSecond();

        //完成教学关卡后，立即刷新，所以是0
        int elapseTimeSinceTutorialLevelComplete = 0;

        List<PromotionEventPackageData> promotionEventPackagesData = new ArrayList<>();

        //玩家最高解锁章节,就是礼包等级,同时也是箱子等级
        Integer packageLevel = userDataService.playerHighestUnlockedChapterID(userData);

        Map<String, PromotionEventPackageGroupTableValue> promotionEventPackageGroupTable = GameEnvironment.promotionEventPackageGroupTableMap.get(gameVersion);
        Set<String> keySet = promotionEventPackageGroupTable.keySet();
        for (String key : keySet) {

            PromotionEventPackageGroupTableValue packageGroupTableValue = promotionEventPackageGroupTable.get(key);

            //检查数据
            if (packageGroupTableValue.getPackageTypesArray().size() != packageGroupTableValue.getActiveTimeArray().size() ||
                    packageGroupTableValue.getPackageTypesArray().size() != packageGroupTableValue.getExpireTimeArray().size()) {

                throw new BusinessException("PromotionEventPackageGroupTable表格 第 " + key + "行数据, packageTypes activeTime expireDuration 数组长度不一致 " + JSONObject.toJSONString(packageGroupTableValue));
            }

            for (Integer index : packageGroupTableValue.getPackageTypesArray()) {

                Integer packageType = packageGroupTableValue.getPackageTypesArray().get(index);
                String purchaseKey = packageGroupTableValue.getId() + "_" + index;

                //如果已经购买过了,跳过
                if (purchasedPromotionEventPackagesKeys.contains(purchaseKey)) {
                    continue;
                }

                PromotionEventPackageTableValue packageTableValue = findPackageTableValueByPackageTypeAndLevel(packageType, packageLevel, gameVersion);
                Integer activeTime = packageGroupTableValue.getActiveTimeArray().get(index);
                Integer expireTime = packageGroupTableValue.getExpireTimeArray().get(index);

                if (activeTime > expireTime) {

                    throw new BusinessException("PromotionEventPackageGroupTable表格 第 " + key + " 行数据， 有expire time < active time " + JSONObject.toJSONString(packageGroupTableValue));
                }

                //当前时间在这个package有效时间内
                if (elapseTimeSinceTutorialLevelComplete >= activeTime && elapseTimeSinceTutorialLevelComplete <= expireTime) {

                    ChestData chestData = new ChestData(NanoIdUtils.randomNanoId(30), packageTableValue.getChestType(), packageLevel, unixTimeNow);

                    PromotionEventPackageData packageData = new PromotionEventPackageData(packageTableValue.getId(),
                            packageGroupTableValue.getProductName(),
                            chestData,
                            unixTimeNow + expireTime,
                            packageGroupTableValue.getPrice(),
                            purchaseKey);

                    promotionEventPackagesData.add(packageData);
                    break;
                }
            }

            //检查数据
            List<String> productNames = new ArrayList<>();
            for (PromotionEventPackageData data : promotionEventPackagesData) {

                if (productNames.contains(data.getProductName())) {
                    throw new BusinessException("刷新活动礼包数据,有重复的product name " + data.getProductName() + ",活动礼包数据: " + JSONObject.toJSONString(promotionEventPackagesData));
                }
                productNames.add(data.getProductName());
            }

            userData.setPromotionEventPackagesData(promotionEventPackagesData);

            log.info("刷新活动礼包数据 " + JSONObject.toJSONString(promotionEventPackagesData));
        }
    }

    @Override
    public PromotionEventPackageTableValue findPackageTableValueByPackageTypeAndLevel(Integer packageType, Integer packageLevel, String gameVersion) {

        Map<String, PromotionEventPackageTableValue> promotionEventPackageTable = GameEnvironment.promotionEventPackageTableMap.get(gameVersion);

        PromotionEventPackageTableValue lowestLevelPackageTableValue = new PromotionEventPackageTableValue();
        lowestLevelPackageTableValue = null;
        Set<String> keySet = promotionEventPackageTable.keySet();
        for (String key : keySet) {

            PromotionEventPackageTableValue tableValue = promotionEventPackageTable.get(key);
            if (tableValue.getPackageType().equals(packageType)) {

                if (tableValue.getPackageLevel().equals(packageLevel)) {
                    return tableValue;
                }
                if (lowestLevelPackageTableValue == null) {
                    lowestLevelPackageTableValue = tableValue;
                } else if (lowestLevelPackageTableValue.getPackageLevel() > tableValue.getPackageLevel()) {
                    lowestLevelPackageTableValue = tableValue;
                }
            }

        }

        if (lowestLevelPackageTableValue != null) {
            return lowestLevelPackageTableValue;
        }
        throw new BusinessException("无法在PromotionEventPackage表中,找到活动礼包类型 " + packageType + ", 礼包等级" + packageLevel + "的条目");
    }

    @Override
    public PromotionEventPackageData findUserPromotionEventPackageDataByPackageId(UserData userData, String productName) {

        List<PromotionEventPackageData> promotionEventPackagesData = userData.getPromotionEventPackagesData();

        for (PromotionEventPackageData packageData : promotionEventPackagesData) {

            if (packageData.getProductName().equals(productName)) {
                return packageData;
            }
        }
        return null;
    }

    @Override
    public PromotionEventPackageGroupV2TableValue findUserPromotionEventPackageV2DataByPackageId(UserData userData, String gameVersion, String productName) {

        if (userData.getPromotionGiftPackagesV2Data() == null || userData.getPromotionGiftPackagesV2Data().size() == 0) {
            return null;
        }

        Map<String, PromotionEventPackageGroupV2TableValue> groupV2TableValueMap = GameEnvironment.promotionEventPackageGroupV2TableMap.get(gameVersion);


        List<PromotionEventPackageGroupV2TableValue> targetPackages = new ArrayList<>();
        groupV2TableValueMap.forEach(new BiConsumer<String, PromotionEventPackageGroupV2TableValue>() {
            @Override
            public void accept(String s, PromotionEventPackageGroupV2TableValue groupV2TableValue) {
                if (groupV2TableValue.getProductName().equals(productName)) {
                    targetPackages.add(groupV2TableValue);
                }
            }
        });

        if (targetPackages.size() != 0) {
            return targetPackages.get(0);
        }

        return null;

    }

    @Override
    public void refreshPromotionEventPackageNow(UserData userData, String gameVersion) {
        //如果教学关都没完成，不刷新
        if (userData.getTutorialData().getForceTutorialStepStatusMap().get(ForceTutorialStepNames.forceCompleteTutorialMatch.getName()) == null) {
            return;
        }

        List<String> purchasedPromotionEventPackagesKeys = userData.getServerOnly().getPurchasedPromotionEventPackagesKeys();
        Long unixTimeNow = TimeUtils.getUnixTimeSecond();

        Long signUpTime = userData.getSignUpTime();
        Long elapseSecondsSinceSignup = unixTimeNow - signUpTime;

        List<PromotionEventPackageData> promotionEventPackagesData = new ArrayList<>();

        //玩家最高解锁章节,就是礼包等级,同时也是箱子等级
        int packageLevel = userDataService.getPlayerHighestUnlockedChapterID(userData);

        //版本号数字之和，用来判断当前版本是否小于某一个版本  1.0.12
        // int gameVersionNum = 0;
        // for (String s : gameVersion.split("\\.")) {
        //     gameVersionNum += Integer.parseInt(s);
        // }

        List<PromotionEventPackageData> oldPromotionPackage = userData.getPromotionEventPackagesData();

        // @TOTO 版本号判断错误
        // if (gameVersionNum <= 13) {
        if (gameVersion.compareTo("1.0.12") <= 0) {
            Map<String, PromotionEventPackageGroupTableValue> promotionEventPackageGroupTable = GameEnvironment.promotionEventPackageGroupTableMap.get(gameVersion);
            for (String key : promotionEventPackageGroupTable.keySet()) {
                PromotionEventPackageGroupTableValue packageGroupTableValue = promotionEventPackageGroupTable.get(key);

                //检查数据
                if (packageGroupTableValue.getPackageTypesArray().size() != packageGroupTableValue.getActiveTimeArray().size() ||
                        packageGroupTableValue.getPackageTypesArray().size() != packageGroupTableValue.getExpireTimeArray().size()) {
                    throw new BusinessException("PromotionEventPackageGroupTable表格 第 " + key + " 行数据, packageTypes activeTime expireDuration 数组长度不一致 " + JSON.toJSONString(packageGroupTableValue));
                }

                for (int i = 0; i < packageGroupTableValue.getPackageTypesArray().size(); i++) {
                    Integer packageType = packageGroupTableValue.getPackageTypesArray().get(i);

                    String purchaseKey = packageGroupTableValue.getId() + "_" + i;
                    //如果已经购买过了,跳过
                    if (purchasedPromotionEventPackagesKeys.contains(purchaseKey)) {
                        continue;
                    }

                    PromotionEventPackageTableValue packageTableValue = findPackageTableValueByPackageTypeAndLevelAsync(packageType, packageLevel, gameVersion);
                    Integer activeTime = packageGroupTableValue.getActiveTimeArray().get(i);
                    Integer expireTime = packageGroupTableValue.getExpireTimeArray().get(i);

                    if (activeTime > expireTime) {
                        throw new BusinessException("`PromotionEventPackageGroupTable表格 第 " + key + " 行数据, 有expire time < active time " + JSON.toJSONString(packageGroupTableValue));
                    }

                    //当前时间在这个package有效时间内
                    if (elapseSecondsSinceSignup >= activeTime && elapseSecondsSinceSignup <= expireTime) {
                        PromotionEventPackageData packageData = new PromotionEventPackageData(
                                packageTableValue.getId(),
                                packageGroupTableValue.getProductName(),
                                new ChestData(NanoIdUtils.randomNanoId(30), packageTableValue.getChestType(), packageLevel, unixTimeNow),
                                unixTimeNow + expireTime,
                                packageGroupTableValue.getPrice(),
                                purchaseKey);
                        promotionEventPackagesData.add(packageData);
                        break;
                    }
                }
            }

            //检查数据
            List<String> productNames = new ArrayList<>();
            for (PromotionEventPackageData data : promotionEventPackagesData) {
                if (productNames.contains(data.getProductName())) {
                    throw new BusinessException("刷新活动礼包数据,有重复的product name " + data.getProductName() + ",活动礼包数据: " + JSON.toJSONString(promotionEventPackagesData));
                }
                productNames.add(data.getProductName());
            }

            userData.setPromotionEventPackagesData(promotionEventPackagesData);

            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
            if (userDataSendToClient != null) {
                userDataSendToClient.setPromotionEventPackagesData(promotionEventPackagesData);
            }
            log.info("刷新活动礼包数据 " + JSON.toJSONString(promotionEventPackagesData));
        } else {
            if (oldPromotionPackage != null && oldPromotionPackage.size() != 0) {
                Map<String, PromotionEventPackageGroupTableValue> promotionEventPackageGroupTable = GameEnvironment.promotionEventPackageGroupTableMap.get(gameVersion);
                for (String key : promotionEventPackageGroupTable.keySet()) {
                    PromotionEventPackageGroupTableValue packageGroupTableValue = promotionEventPackageGroupTable.get(key);

                    //检查数据
                    if (packageGroupTableValue.getPackageTypesArray().size() != packageGroupTableValue.getActiveTimeArray().size() ||
                            packageGroupTableValue.getPackageTypesArray().size() != packageGroupTableValue.getExpireTimeArray().size()) {
                        throw new BusinessException("PromotionEventPackageGroupTable表格 第 " + key + " 行数据, packageTypes activeTime expireDuration 数组长度不一致 " + JSON.toJSONString(packageGroupTableValue));
                    }

                    for (int i = 0; i < packageGroupTableValue.getPackageTypesArray().size(); i++) {
                        Integer packageType = packageGroupTableValue.getPackageTypesArray().get(i);

                        String purchaseKey = packageGroupTableValue.getId() + "_" + i;
                        //如果已经购买过了,跳过
                        if (purchasedPromotionEventPackagesKeys.contains(purchaseKey)) {
                            continue;
                        }

                        PromotionEventPackageTableValue packageTableValue = findPackageTableValueByPackageTypeAndLevelAsync(packageType, packageLevel, gameVersion);
                        Integer activeTime = packageGroupTableValue.getActiveTimeArray().get(i);
                        Integer expireTime = packageGroupTableValue.getExpireTimeArray().get(i);

                        if (activeTime > expireTime) {
                            throw new BusinessException("`PromotionEventPackageGroupTable表格 第 " + key + " 行数据, 有expire time < active time " + JSON.toJSONString(packageGroupTableValue));
                        }

                        //当前时间在这个package有效时间内
                        if (elapseSecondsSinceSignup >= activeTime && elapseSecondsSinceSignup <= expireTime) {
                            PromotionEventPackageData packageData = new PromotionEventPackageData(
                                    packageTableValue.getId(),
                                    packageGroupTableValue.getProductName(),
                                    new ChestData(NanoIdUtils.randomNanoId(30), packageTableValue.getChestType(), packageLevel, unixTimeNow),
                                    unixTimeNow + expireTime,
                                    packageGroupTableValue.getPrice(),
                                    purchaseKey);
                            promotionEventPackagesData.add(packageData);
                            break;
                        }
                    }
                }

                //检查数据
                List<String> productNames = new ArrayList<>();
                for (PromotionEventPackageData data : promotionEventPackagesData) {
                    if (productNames.contains(data.getProductName())) {
                        throw new BusinessException("刷新活动礼包数据,有重复的product name " + data.getProductName() + ",活动礼包数据: " + JSON.toJSONString(promotionEventPackagesData));
                    }
                    productNames.add(data.getProductName());
                }

                userData.setPromotionEventPackagesData(promotionEventPackagesData);

                UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
                if (userDataSendToClient != null) {
                    userDataSendToClient.setPromotionEventPackagesData(promotionEventPackagesData);
                }
                log.info("刷新活动礼包数据 " + JSON.toJSONString(promotionEventPackagesData));
            }
        }


        //如果当前用户数据中已经存在老的礼包结构，则继续按照老礼包的规则进行配置
//         || gameVersionNum<=(1 + 12)


//        Map<String, PromotionEventPackageGroupTableValue> promotionEventPackageGroupTable = GameEnvironment.promotionEventPackageGroupTableMap.get(gameVersion);
//        for (String key : promotionEventPackageGroupTable.keySet()) {
//            PromotionEventPackageGroupTableValue packageGroupTableValue = promotionEventPackageGroupTable.get(key);
//
//            //检查数据
//            if (packageGroupTableValue.getPackageTypesArray().size() != packageGroupTableValue.getActiveTimeArray().size() ||
//                    packageGroupTableValue.getPackageTypesArray().size() != packageGroupTableValue.getExpireTimeArray().size()) {
//                throw new BusinessException("PromotionEventPackageGroupTable表格 第 " + key + " 行数据, packageTypes activeTime expireDuration 数组长度不一致 " + JSON.toJSONString(packageGroupTableValue));
//            }
//
//            for (int i = 0; i < packageGroupTableValue.getPackageTypesArray().size(); i++) {
//                Integer packageType = packageGroupTableValue.getPackageTypesArray().get(i);
//
//                String purchaseKey = packageGroupTableValue.getId() + "_" + i;
//                //如果已经购买过了,跳过
//                if (purchasedPromotionEventPackagesKeys.contains(purchaseKey)) {
//                    continue;
//                }
//
//                PromotionEventGunGiftPackageV2TableValue promotionEventGunGiftPackage = null;
//                PromotionEventPackageTableValue packageTableValue = findPackageTableValueByPackageTypeAndLevelAsync(packageType, packageLevel,gameVersion);
//                //如果第一档的活动礼包找不到，再去另一张配置表找第二，第三档
////                if (packageTableValue == null){
////                    promotionEventGunGiftPackage = findPromotionEventGunGiftPackage(packageType, packageLevel, gameVersion);
////                    packageTableValue = new PromotionEventPackageTableValue();
////                    packageTableValue.setId(promotionEventGunGiftPackage.getId());
////                    packageTableValue.setPackageType(promotionEventGunGiftPackage.getPackageType());
////                    packageTableValue.setChestType(promotionEventGunGiftPackage.getRewardChestType());
////                }
//                Integer activeTime = packageGroupTableValue.getActiveTimeArray().get(i);
//                Integer expireTime = packageGroupTableValue.getExpireTimeArray().get(i);
//
//                if (activeTime > expireTime) {
//                    throw new BusinessException("`PromotionEventPackageGroupTable表格 第 " + key + " 行数据, 有expire time < active time " + JSON.toJSONString(packageGroupTableValue));
//                }
//
//                //当前时间在这个package有效时间内
//                if (elapseSecondsSinceSignup >= activeTime && elapseSecondsSinceSignup <= expireTime) {
//                    PromotionEventPackageData packageData = new PromotionEventPackageData(
//                            packageTableValue.getId(),
//                            packageGroupTableValue.getProductName(),
//                            new ChestData(NanoIdUtils.randomNanoId(30),packageTableValue.getChestType(),packageLevel,unixTimeNow),
//                            unixTimeNow + expireTime,
//                            packageGroupTableValue.getPrice(),
//                            purchaseKey);
//                    promotionEventPackagesData.add(packageData);
//                    break;
//                }
//            }
//        }
//
//        //检查数据
//        List<String> productNames = new ArrayList<>();
//        for (PromotionEventPackageData data : promotionEventPackagesData) {
//            if (productNames.contains(data.getProductName())) {
//                throw new BusinessException("刷新活动礼包数据,有重复的product name " + data.getProductName() + ",活动礼包数据: " + JSON.toJSONString(promotionEventPackagesData));
//            }
//            productNames.add(data.getProductName());
//        }
//
//        userData.setPromotionEventPackagesData(promotionEventPackagesData);
//
//        UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
//        if (userDataSendToClient != null ) {
//            userDataSendToClient.setPromotionEventPackagesData(promotionEventPackagesData);
//        }
//        log.info("刷新活动礼包数据 " + JSON.toJSONString(promotionEventPackagesData));
    }

    @Override
    public void refreshBulletGiftPackageNow(UserData userData, Boolean canDeleteExpirePackages, String gameVersion) {
        Long signUpTime = userData.getSignUpTime();
        Long signUpStandardDay = TimeUtils.convertUnixTimeSecondToStandardDay(signUpTime);
        //距离注册已经过去多少天
        long dayPassSinceSignUp = TimeUtils.getStandardTimeDay() - signUpStandardDay;
        long standardSecond = TimeUtils.getStandardTimeSecond();
        Map<String, BulletGiftPackageTableValue> bulletGiftPackageTable = GameEnvironment.bulletGiftPackageTableMap.get(gameVersion);
        int chestLevel = Math.max(userDataService.getPlayerHighestUnlockedChapterID(userData), GameConfig.bulletGiftPackageMinChestLevel);
        List<PlayerBulletGiftPackageData> availableBulletGiftPackageData = userData.getAvailableBulletGiftPackageData();
        Map<String, Integer> iapProductPurchasedCountMap = userData.getIapProductPurchasedCountMap();

        boolean packageDataChanged = false;
        if (canDeleteExpirePackages) {
            List<PlayerBulletGiftPackageData> expiredPackageDataArray = new ArrayList<>();
            for (PlayerBulletGiftPackageData packageData : availableBulletGiftPackageData) {
                if (standardSecond > packageData.getEndStandardTimeSecond()) {
                    expiredPackageDataArray.add(packageData);
                }
            }
            if (expiredPackageDataArray.size() > 0) {
                packageDataChanged = true;
                log.info("删除过期子弹礼包： " + JSON.toJSONString(expiredPackageDataArray));
                availableBulletGiftPackageData = availableBulletGiftPackageData.stream().filter(value -> !expiredPackageDataArray.contains(value)).collect(Collectors.toCollection(ArrayList::new));

                for (String key : bulletGiftPackageTable.keySet()) {
                    BulletGiftPackageTableValue giftPackageTableValue = bulletGiftPackageTable.get(key);
                    String purchaseRecordKey = IAPProductPrefix.bulletGiftPackage + "_" + giftPackageTableValue.getProductName() + "_" + giftPackageTableValue.getId();
                    iapProductPurchasedCountMap.remove(purchaseRecordKey);
                }
            }
        }

        for (String key : bulletGiftPackageTable.keySet()) {
            BulletGiftPackageTableValue giftPackageTableValue = bulletGiftPackageTable.get(key);
            //注册天数是否足够
            if (dayPassSinceSignUp < giftPackageTableValue.getVisibleDaysAfterSignUp()) {
                continue;
            }

            //是否在启用时间内
            // if (standardSecond < giftPackageTableValue.getEnableStandardTimeArray().get(0) || standardSecond > giftPackageTableValue.getDisableStandardTimeArray().get(0)) {
            //     continue;
            // }

            //是否已经启用
            if (isAlreadyInData(giftPackageTableValue.getId(), availableBulletGiftPackageData)) {
                continue;
            }

            //是否已经购买
            String purchaseRecordKey = IAPProductPrefix.bulletGiftPackage + "_" + giftPackageTableValue.getProductName() + "_" + giftPackageTableValue.getId();
            boolean isPurchased = iapProductPurchasedCountMap.get(purchaseRecordKey) != null;
            if (isPurchased) {
                continue;
            }

            List<Long> enableStandardTimeArray = giftPackageTableValue.getEnableStandardTimeArray();
            List<Long> disableStandardTimeArray = giftPackageTableValue.getDisableStandardTimeArray();
            for (int i = 0; i < enableStandardTimeArray.size(); i++) {
                if (standardSecond >= enableStandardTimeArray.get(i) && standardSecond <= disableStandardTimeArray.get(i)) {
                    packageDataChanged = true;

                    availableBulletGiftPackageData.add(new PlayerBulletGiftPackageData(giftPackageTableValue.getId(), chestLevel, enableStandardTimeArray.get(i), disableStandardTimeArray.get(i)));
                    break;
                }
            }

        }

        userData.setAvailableBulletGiftPackageData(availableBulletGiftPackageData);
        log.info("刷新子弹礼包：" + JSON.toJSONString(availableBulletGiftPackageData));

        UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
        if (userDataSendToClient != null && packageDataChanged) {
            userDataSendToClient.setAvailableBulletGiftPackageData(availableBulletGiftPackageData);
        }

    }

    @Override
    public void refreshFifthDayGunGiftPackageDataNow(UserData userData, Boolean canDeleteExpirePackages, String gameVersion) {
        List<PlayerFifthDayGunGiftPackageData> availableFifthDayGunGiftPackageData = userData.getAvailableFifthDayGunGiftPackageData();

        Map<String, FifthDayGunGiftPackageGroupTableValue> fifthDayGunGiftPackageGroupTable = GameEnvironment.fifthDayGunGiftPackageGroupTableMap.get(gameVersion);

        Long signUpTime = userData.getSignUpTime();
        Long signUpDay = TimeUtils.convertUnixTimeSecondToStandardDay(signUpTime);
        Long daysSinceSignUp = TimeUtils.getStandardTimeDay() - signUpDay;
        Long unixTimeSecond = TimeUtils.getUnixTimeSecond();

        boolean isPackageDataChanged = false;
        //检查过期礼包数据
        if (canDeleteExpirePackages) {
            List<PlayerFifthDayGunGiftPackageData> expiredPackageDataArray = new ArrayList<>();
            for (PlayerFifthDayGunGiftPackageData packageData : availableFifthDayGunGiftPackageData) {
                Integer groupId = packageData.getGroupId();
//                FifthDayGunGiftPackageGroupTableValue groupTableValue = fifthDayGunGiftPackageGroupTable.get("" + groupId);
                Long expireTime = packageData.getExpireTime();
                if (expireTime > 0 && unixTimeSecond > expireTime) {
                    expiredPackageDataArray.add(packageData);
                }
            }

            if (expiredPackageDataArray.size() > 0) {
                isPackageDataChanged = true;
                log.info("五日枪械礼包过期：" + JSON.toJSONString(expiredPackageDataArray));
                availableFifthDayGunGiftPackageData = availableFifthDayGunGiftPackageData.stream().filter(value -> !expiredPackageDataArray.contains(value)).collect(Collectors.toCollection(ArrayList::new));
            }
        }

        List<Integer> availableGroupIds = new ArrayList<>();
        for (PlayerFifthDayGunGiftPackageData packageData : availableFifthDayGunGiftPackageData) {
            if (availableGroupIds.contains(packageData.getGroupId())) {
                throw new BusinessException("不应该有重复的group id: " + JSON.toJSONString(availableFifthDayGunGiftPackageData));
            }

            availableGroupIds.add(packageData.getGroupId());
        }

        int playerHighestUnlockedChapterID = userDataService.getPlayerHighestUnlockedChapterID(userData);
        List<Integer> purchasedGroupIds = getPurchasedGroupIdsFifthDayGunGiftPackageGroupTableValue(userData, fifthDayGunGiftPackageGroupTable);
        //添加新的礼包
        for (String key : fifthDayGunGiftPackageGroupTable.keySet()) {
            FifthDayGunGiftPackageGroupTableValue groupTableValue = fifthDayGunGiftPackageGroupTable.get(key);
            //已经购买过了
            if (purchasedGroupIds.contains(groupTableValue.getId())) {
                continue;
            }

            //注册时长还不够
            if (daysSinceSignUp < groupTableValue.getEnableStandardDaysAfterSignUp()) {
                continue;
            }

            //已经过期的礼包组
            //当前时间-（持续时间+开启时间）> 0
            Long currentDay = TimeUtils.convertUnixTimeSecondToStandardDay(TimeUtils.getUnixTimeSecond()) - daysSinceSignUp;
            Integer startDay = groupTableValue.getEnableStandardDaysAfterSignUp();
            Long lastDay = TimeUtils.convertUnixTimeSecondToStandardDay(groupTableValue.getEnableDurationSeconds());

            if (daysSinceSignUp < startDay || daysSinceSignUp > (startDay + lastDay)) {
                continue;
            }

            //已经激活的礼包组
            if (availableGroupIds.contains(groupTableValue.getId())) {
                continue;
            }

            //玩家最高章节不在激活章节中
            if (!groupTableValue.getActiveHighestChaptersArray().contains(playerHighestUnlockedChapterID)) {
                continue;
            }

            isPackageDataChanged = true;
            PlayerFifthDayGunGiftPackageData fifthDayGunGiftPackageTableValue = createFifthDayGunGiftPackageDataFromGroup(groupTableValue, userData, gameVersion);
            availableFifthDayGunGiftPackageData.add(fifthDayGunGiftPackageTableValue);
        }

        log.info("刷新五日枪械礼包数据：" + JSON.toJSONString(availableFifthDayGunGiftPackageData));

        userData.setAvailableFifthDayGunGiftPackageData(availableFifthDayGunGiftPackageData);

        UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
        if (userDataSendToClient != null && isPackageDataChanged) {
            userDataSendToClient.setAvailableFifthDayGunGiftPackageData(availableFifthDayGunGiftPackageData);
        }
    }

    @Override
    public void refreshGunGiftPackageDataNow(UserData userData, Boolean canDeleteExpirePackages, String gameVersion) {
        List<PlayerGunGiftPackageData> availableGunGiftPackageData = userData.getAvailableGunGiftPackageData();
        Map<String, GunGiftPackageGroupTableValue> gunGiftPackageGroupTable = GameEnvironment.gunGiftPackageGroupTableMap.get(gameVersion);

        Long signUpTime = userData.getSignUpTime();
        Long signUpDay = TimeUtils.convertUnixTimeSecondToStandardDay(signUpTime);
        Long daysSinceSignUp = TimeUtils.getStandardTimeDay() - signUpDay;
        Long standardTimeSecond = TimeUtils.getStandardTimeSecond();
        boolean isPackageDataChanged = false;

        //检查过期礼包数据
        if (canDeleteExpirePackages) {
            List<PlayerGunGiftPackageData> expiredPackageDataArray = new ArrayList<>();
            for (PlayerGunGiftPackageData packageData : availableGunGiftPackageData) {
                Integer groupId = packageData.getGroupId();
                // GunGiftPackageGroupTableValue groupTableValue = gunGiftPackageGroupTable.get("" + groupId);
                Long expireTime = packageData.getExpireTime(); // groupTableValue.getDisableStandardTimeArray().get(0);
                if (standardTimeSecond > expireTime) {
                    log.info("枪械礼包 " + packageData.getPackageId() + " 过期");

                    // GunGiftPackageGroupTableValue gunGiftPackageGroupTableValue = gunGiftPackageGroupTable.get(packageData.getGroupId().toString());
                    // for (Integer packageId : gunGiftPackageGroupTableValue.getAvailablePackageIdArray()) {
                    //     String productName = String.format("%s_%s_%s", IAPProductPrefix.gunGiftPackage, gunGiftPackageGroupTableValue.getProductName(), packageId);
                    //     userData.getIapProductPurchasedCountMap().remove(productName);
                    // }

                    expiredPackageDataArray.add(packageData);
                }
            }

            if (expiredPackageDataArray.size() > 0) {
                isPackageDataChanged = true;
                log.info("删除过期的枪械礼包数据：" + JSON.toJSONString(expiredPackageDataArray));
                availableGunGiftPackageData = availableGunGiftPackageData.stream().filter(value -> !expiredPackageDataArray.contains(value)).collect(Collectors.toCollection(ArrayList::new));

                Map<String, Integer> iapProductPurchasedCountMap = userData.getIapProductPurchasedCountMap();
                for (String key : gunGiftPackageGroupTable.keySet()) {
                    GunGiftPackageGroupTableValue groupTableValue = gunGiftPackageGroupTable.get(key);
                    for (Integer packageId : groupTableValue.getAvailablePackageIdArray()) {
                        String purchaseKey = IAPProductPrefix.gunGiftPackage + "_" + groupTableValue.getProductName() + "_" + packageId;
                        iapProductPurchasedCountMap.remove(purchaseKey);
                    }
                }
            }
        }

        List<Integer> availableGroupIds = new ArrayList<>();
        for (PlayerGunGiftPackageData packageData : availableGunGiftPackageData) {
            if (availableGroupIds.contains(packageData.getGroupId())) {
                throw new BusinessException("不应该有重复的group id: " + JSON.toJSONString(availableGunGiftPackageData))
                        ;
            }
            availableGroupIds.add(packageData.getGroupId());
        }

        Integer playerHighestUnlockedChapterID = userDataService.getPlayerHighestUnlockedChapterID(userData);
        List<Integer> purchasedGroupIds = getPurchasedGroupIdsGunGiftPackageGroupTableValue(userData, gunGiftPackageGroupTable);

        //添加新的礼包
        for (String key : gunGiftPackageGroupTable.keySet()) {
            GunGiftPackageGroupTableValue groupTableValue = gunGiftPackageGroupTable.get(key);
            //已经购买过了
            if (purchasedGroupIds.contains(groupTableValue.getId())) {
                continue;
            }

            //注册时长还不够
            if (daysSinceSignUp < groupTableValue.getVisibleDaysAfterSignUp()) {
                continue;
            }

            //还没开始的礼包组
            // if (standardTimeSecond < groupTableValue.getEnableStandardTimeArray().get(0) || standardTimeSecond > groupTableValue.getDisableStandardTimeArray().get(0)) {
            //     continue;
            // }

            //已经激活的礼包组
            if (availableGroupIds.contains(groupTableValue.getId())) {
                continue;
            }

            List<Long> enableStandardTimeArray = groupTableValue.getEnableStandardTimeArray();
            List<Long> disableStandardTimeArray = groupTableValue.getDisableStandardTimeArray();
            for (int i = 0; i < enableStandardTimeArray.size(); i++) {
                if (standardTimeSecond >= enableStandardTimeArray.get(i) && standardTimeSecond <= disableStandardTimeArray.get(i)) {

                    isPackageDataChanged = true;
                    PlayerGunGiftPackageData gunGiftPackageData = createGunGiftPackageDataFromGroup(groupTableValue, userData, gameVersion, disableStandardTimeArray.get(i));
                    availableGunGiftPackageData.add(gunGiftPackageData);
                    break;
                }
            }
        }

        log.info("刷新枪械礼包数据：" + JSON.toJSONString(availableGunGiftPackageData));

        userData.setAvailableGunGiftPackageData(availableGunGiftPackageData);

        UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
        if (userDataSendToClient != null && isPackageDataChanged) {
            userDataSendToClient.setAvailableGunGiftPackageData(availableGunGiftPackageData);
        }
    }


    @Override
    public void refreshPromotionEventPackageV2Now(UserData userData, String gameVersion) {

        //如果教学关都没完成，不刷新
        if (userData.getTutorialData().getForceTutorialStepStatusMap().get(ForceTutorialStepNames.forceCompleteTutorialMatch.getName()) == null) {
            return;
        }

        List<PromotionEventPackageData> promotionEventPackagesData = userData.getPromotionEventPackagesData();
        List<PromotionGiftPackageV2Data> promotionGiftPackagesV2Data = new ArrayList<>();
        if (promotionEventPackagesData.size() != 0) {
//            userData.setPromotionGiftPackagesV2Data(promotionGiftPackagesV2Data);
            return;
        }

        // //版本号数字之和，用来判断当前版本是否小于某一个版本
        // int gameVersionNum = 0;
        // for (String s : gameVersion.split("\\.")) {
        //     gameVersionNum += Integer.parseInt(s);
        // }
        //1.0.13
        // if (gameVersionNum < 14) {
        if (gameVersion.compareTo("1.0.13") < 0) {
            return;
        }


        Long unixTimeNow = TimeUtils.getUnixTimeSecond();

        Long signUpTime = userData.getSignUpTime();
        long elapseSecondsSinceSignup = unixTimeNow - signUpTime;


        //玩家最高解锁章节,就是礼包等级,同时也是箱子等级
        int packageLevel = userDataService.getPlayerHighestUnlockedChapterID(userData);

        List<String> purchasedPromotionEventPackagesV2Keys = userData.getServerOnly().getPurchasedPromotionEventPackagesV2Keys();

        //拿到所有礼包信息
        Map<String, PromotionEventPackageGroupV2TableValue> groupMap = GameEnvironment.promotionEventPackageGroupV2TableMap.get(gameVersion);


        for (String key : groupMap.keySet()) {
            PromotionEventPackageGroupV2TableValue groupV2TableValue = groupMap.get(key);

            //检查数据
            if (groupV2TableValue.getPackageTypesArray().size() != groupV2TableValue.getActiveTimeArray().size() ||
                    groupV2TableValue.getPackageTypesArray().size() != groupV2TableValue.getExpireTimeArray().size()) {
                throw new BusinessException("PromotionEventPackageGroupTable表格 第 " + key + " 行数据, packageTypes activeTime expireDuration 数组长度不一致 " + JSON.toJSONString(groupV2TableValue));
            }

            for (int i = 0; i < groupV2TableValue.getPackageTypesArray().size(); i++) {
                Integer packageType = groupV2TableValue.getPackageTypesArray().get(i);

                PromotionGiftPackageV2Data packageData = new PromotionGiftPackageV2Data();
                PromotionEventPackageV2TableValue packageTableValue = findPackageV2TableValueByPackageTypeAndLevelAsync(packageType, packageLevel, gameVersion);
                packageData.setPackageType(1);
                if (packageTableValue == null) {
                    PromotionEventGunGiftPackageV2TableValue gunGiftPackageV2 = findPromotionEventGunGiftPackageV2(packageType, packageLevel, gameVersion);
                    packageTableValue = new PromotionEventPackageV2TableValue();
                    packageTableValue.setId(gunGiftPackageV2.getId());
                    packageTableValue.setPackageType(gunGiftPackageV2.getPackageType());
                    packageTableValue.setChestType(gunGiftPackageV2.getRewardChestType());
                    packageData.setPackageType(2);
                }

                //groupId _ packageType-1(groupId-1)
                String purchaseKey = groupV2TableValue.getId() + "_" + packageType + "_" + packageTableValue.getId();
                //如果已经购买过了,跳过
                if (purchasedPromotionEventPackagesV2Keys.contains(purchaseKey)) {
                    continue;
                }

                Integer activeTime = groupV2TableValue.getActiveTimeArray().get(i);
                Integer expireTime = groupV2TableValue.getExpireTimeArray().get(i);

                if (activeTime > expireTime) {
                    throw new BusinessException("`PromotionEventPackageGroupTable表格 第 " + key + " 行数据, 有expire time < active time " + JSON.toJSONString(groupV2TableValue));
                }

//                //当前时间在这个package有效时间内
                if (elapseSecondsSinceSignup >= activeTime && elapseSecondsSinceSignup <= expireTime) {
                    packageData.setPackageId(packageTableValue.getId());
                    packageData.setPackageGroupId(groupV2TableValue.getId());
                    packageData.setExpireTime(signUpTime + groupV2TableValue.getExpireTimeArray().get(0));
                    promotionGiftPackagesV2Data.add(packageData);
                    break;
                }
            }
        }


        //检查数据
        List<String> productNames = new ArrayList<>();
        for (PromotionGiftPackageV2Data data : promotionGiftPackagesV2Data) {
            String productName = data.getPackageId() + "_" + data.getPackageType();
            if (productNames.contains(productName)) {
                throw new BusinessException("刷新活动礼包数据,有重复的packageId:" + data.getPackageId() + "packageType:" + data.getPackageType() + ",活动礼包数据: " + JSON.toJSONString(promotionEventPackagesData));
            }
            productNames.add(productName);
        }

        userData.setPromotionGiftPackagesV2Data(promotionGiftPackagesV2Data);

        log.info("刷新活动礼包数据 " + JSON.toJSONString(promotionGiftPackagesV2Data));


    }

    private PromotionEventPackageTableValue findPackageTableValueByPackageTypeAndLevelAsync(Integer packageType, Integer packageLevel, String gameVersion) {
        Map<String, PromotionEventPackageTableValue> promotionEventPackageTable = GameEnvironment.promotionEventPackageTableMap.get(gameVersion);
        PromotionEventPackageTableValue lowestLevelPackageTableValue = null;
        for (String key : promotionEventPackageTable.keySet()) {
            PromotionEventPackageTableValue tableValue = promotionEventPackageTable.get(key);
            if (tableValue.getPackageType() == packageType) {
                if (tableValue.getPackageLevel() == packageLevel) {
                    return tableValue;
                }

                if (lowestLevelPackageTableValue == null) {
                    lowestLevelPackageTableValue = tableValue;
                } else if (lowestLevelPackageTableValue.getPackageLevel() > tableValue.getPackageLevel()) {
                    lowestLevelPackageTableValue = tableValue;
                }
            }
        }


        if (lowestLevelPackageTableValue != null) {
            return lowestLevelPackageTableValue;
        }

//        throw new BusinessException("无法在PromotionEventPackage表中,找到活动礼包类型 " + packageType + ", 礼包等级 " + packageLevel + " 的条目");
        //新增，第二，三档的章节礼包，单独配置了另一张表，所以需要二次查找,这里如果找不到，直接返回null
        return null;
    }


    private PromotionEventPackageV2TableValue findPackageV2TableValueByPackageTypeAndLevelAsync(Integer packageType, Integer packageLevel, String gameVersion) {
        Map<String, PromotionEventPackageV2TableValue> promotionEventPackageTable = GameEnvironment.promotionEventPackageV2TableMap.get(gameVersion);
        PromotionEventPackageV2TableValue lowestLevelPackageTableValue = null;
        for (String key : promotionEventPackageTable.keySet()) {
            PromotionEventPackageV2TableValue tableValue = promotionEventPackageTable.get(key);
            if (tableValue.getPackageType() == packageType) {
                if (tableValue.getPackageLevel() == packageLevel) {
                    return tableValue;
                }

                if (lowestLevelPackageTableValue == null) {
                    lowestLevelPackageTableValue = tableValue;
                } else if (lowestLevelPackageTableValue.getPackageLevel() > tableValue.getPackageLevel()) {
                    lowestLevelPackageTableValue = tableValue;
                }
            }
        }


        return lowestLevelPackageTableValue;
    }


    private PromotionEventGunGiftPackageV2TableValue findPromotionEventGunGiftPackageV2(Integer packageType, Integer packageLevel, String gameVersion) {
        Map<String, PromotionEventGunGiftPackageV2TableValue> promotionEventGunGiftPackageTable = GameEnvironment.promotionEventGunGiftPackageV2TableMap.get(gameVersion);
        PromotionEventGunGiftPackageV2TableValue lowestLevelPackageTableValue = null;

        //新增，第二，三档的章节礼包，单独配置了另一张表，所以需要二次查找
        for (String key : promotionEventGunGiftPackageTable.keySet()) {
            PromotionEventGunGiftPackageV2TableValue tableValue = promotionEventGunGiftPackageTable.get(key);
            if (tableValue.getPackageType() == packageType) {
                if (tableValue.getPackageLevel() == packageLevel) {
                    return tableValue;
                }

                if (lowestLevelPackageTableValue == null) {
                    lowestLevelPackageTableValue = tableValue;
                } else if (lowestLevelPackageTableValue.getPackageLevel() > tableValue.getPackageLevel()) {
                    lowestLevelPackageTableValue = tableValue;
                }
            }
        }


        if (lowestLevelPackageTableValue != null) {
            return lowestLevelPackageTableValue;
        }

        throw new BusinessException("无法在PromotionEventPackage表中,找到活动礼包类型 " + packageType + ", 礼包等级 " + packageLevel + " 的条目");

    }


    private List<Integer> getPurchasedGroupIdsGunGiftPackageGroupTableValue(UserData userData, Map<String, GunGiftPackageGroupTableValue> gunGiftPackageGroupTable) {
        Map<String, Integer> iapProductPurchasedCountMap = userData.getIapProductPurchasedCountMap();
        List<Integer> purchasedGroupIds = new ArrayList<>();
        for (String key : gunGiftPackageGroupTable.keySet()) {
            GunGiftPackageGroupTableValue groupTableValue = gunGiftPackageGroupTable.get(key);
            for (Integer packageId : groupTableValue.getAvailablePackageIdArray()) {
                String purchaseKey = IAPProductPrefix.gunGiftPackage + "_" + groupTableValue.getProductName() + "_" + packageId;
                if (iapProductPurchasedCountMap.get(purchaseKey) != null) {
                    purchasedGroupIds.add(groupTableValue.getId());
                    break;
                }
            }
        }
        return purchasedGroupIds;
    }

    private PlayerGunGiftPackageData createGunGiftPackageDataFromGroup(GunGiftPackageGroupTableValue packageGroupTableValue, UserData userData, String gameVersion, Long expireTime) {
        int playerHighestUnlockedChapterID = userDataService.getPlayerHighestUnlockedChapterID(userData);
        GunGiftPackageTableValue targetTableValue = null;

        Map<String, GunGiftPackageTableValue> gunGiftPackageTable = GameEnvironment.gunGiftPackageTableMap.get(gameVersion);

        for (Integer packageId : packageGroupTableValue.getAvailablePackageIdArray()) {
            GunGiftPackageTableValue tableValue = gunGiftPackageTable.get(packageId.toString());
            //至少是选择第一个
            if (targetTableValue == null) {
                targetTableValue = tableValue;
            } else {
                //倾向选择和最高章节相同的宝箱等级
                if (tableValue.getRewardChestLevel() == playerHighestUnlockedChapterID) {
                    targetTableValue = tableValue;
                    break;
                }
            }
        }
        if (targetTableValue == null) {
            throw new BusinessException("无法根据" + JSON.toJSONString(packageGroupTableValue) + "选择合适的枪械礼包");
        }
        Long signUpTime = userData.getSignUpTime();
        // Long expireTime = TimeUtils.standardTimeSecondToUnixTimeSecond(packageGroupTableValue.getDisableStandardTimeArray().get(0));
        return new PlayerGunGiftPackageData(packageGroupTableValue.getId(), targetTableValue.getId(), expireTime);
    }

    private boolean isAlreadyInData(int packageId, List<PlayerBulletGiftPackageData> availableBulletGiftPackageData) {
        for (PlayerBulletGiftPackageData packageData : availableBulletGiftPackageData) {
            if (packageData.getPackageId() == packageId) {
                return true;
            }
        }
        return false;
    }

    //获取购买过的group id
    private List<Integer> getPurchasedGroupIdsFifthDayGunGiftPackageGroupTableValue(UserData userData, Map<String, FifthDayGunGiftPackageGroupTableValue> fifthDayGunGiftPackageGroupTable) {
        Map<String, Integer> iapProductPurchasedCountMap = userData.getIapProductPurchasedCountMap();
        List<Integer> purchasedGroupIds = new ArrayList<>();
        for (String key : fifthDayGunGiftPackageGroupTable.keySet()) {
            FifthDayGunGiftPackageGroupTableValue groupTableValue = fifthDayGunGiftPackageGroupTable.get(key);
            for (Integer packageId : groupTableValue.getAvailablePackageIdArray()) {
                String purchaseKey = IAPProductPrefix.fifthDayGunGiftPackage + "_" + groupTableValue.getProductName() + "_" + packageId;
                if (iapProductPurchasedCountMap.get(purchaseKey) != null) {
                    purchasedGroupIds.add(groupTableValue.getId());
                    break;
                }
            }
        }
        return purchasedGroupIds;
    }

    //获取购买过的group id
    private PlayerFifthDayGunGiftPackageData createFifthDayGunGiftPackageDataFromGroup(FifthDayGunGiftPackageGroupTableValue packageGroupTableValue, UserData userData, String gameVersion) {
        int playerHighestUnlockedChapterID = userDataService.getPlayerHighestUnlockedChapterID(userData);
        FifthDayGunGiftPackageTableValue targetTableValue = null;

        Map<String, FifthDayGunGiftPackageTableValue> fifthDayGunGiftPackageTable = GameEnvironment.fifthDayGunGiftPackageTableMap.get(gameVersion);

        for (int packageId : packageGroupTableValue.getAvailablePackageIdArray()) {
            FifthDayGunGiftPackageTableValue tableValue = fifthDayGunGiftPackageTable.get("" + packageId);
            //至少是选择第一个
            if (targetTableValue == null) {
                targetTableValue = tableValue;
            } else {
                //倾向选择和最高章节相同的宝箱等级
                if (tableValue.getRewardChestLevel() == playerHighestUnlockedChapterID) {
                    targetTableValue = tableValue;
                    break;
                }
            }
        }
        if (targetTableValue == null) {
            throw new BusinessException("`无法根据" + JSON.toJSONString(packageGroupTableValue) + "选择合适的五日枪械礼包");
        }
//        Long signUpTime = userData.getSignUpTime();
        Long expireTime = packageGroupTableValue.getEnableDurationSeconds() + TimeUtils.getUnixTimeSecond();
        return new PlayerFifthDayGunGiftPackageData(packageGroupTableValue.getId(), targetTableValue.getId(), expireTime);
    }

}
