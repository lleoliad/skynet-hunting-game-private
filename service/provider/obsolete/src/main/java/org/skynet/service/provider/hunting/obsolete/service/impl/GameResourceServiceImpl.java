package org.skynet.service.provider.hunting.obsolete.service.impl;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.DeflaterUtils;
import org.skynet.service.provider.hunting.obsolete.enums.Table;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.service.GameResourceService;
import org.skynet.service.provider.hunting.obsolete.pojo.table.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class GameResourceServiceImpl implements GameResourceService {

    @Override
    public void inputGameResource(String version, String zipData, String tableName) throws IOException {

        String unzipDataTable = null;
        //解压
        if (zipData != null) {
            unzipDataTable = DeflaterUtils.unzipString(zipData);

            // String filename = "./" + version + "/" + tableName;
            // if (!FileUtil.exist("/data/servers/hunt/110/" + version + "/" + tableName)) {
            //     FileUtil.writeString(unzipDataTable, filename, StandardCharsets.UTF_8);
            // }

            String baseDir = "/data/servers/hunt/moulds";
            String filename = baseDir + File.separator + version + File.separator + tableName;
            if (!FileUtil.exist(filename)) {
                FileUtil.writeString(unzipDataTable, filename, StandardCharsets.UTF_8);
            }
        }

        inputContent(version, unzipDataTable, tableName);
    }

    @Override
    public void inputContent(String version, String content, String tableName) throws IOException {
        if (tableName.equals(Table.Achievement.getName())) {
            Map<String, AchievementTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, AchievementTableValue>>() {
            });
            GameEnvironment.achievementTableMap.put(version, map);

        } else if (tableName.equals(Table.AiWeaponCombination.getName())) {
            Map<String, AiWeaponCombinationTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, AiWeaponCombinationTableValue>>() {
            });
            GameEnvironment.aiWeaponCombinationTableMap.put(version, map);
        } else if (tableName.equals(Table.Animal.getName())) {
            Map<String, AnimalTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, AnimalTableValue>>() {
            });
            GameEnvironment.animalTableMap.put(version, map);
        } else if (tableName.equals(Table.Bullet.getName())) {
            Map<String, BulletTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, BulletTableValue>>() {
            });
            GameEnvironment.bulletTableMap.put(version, map);
        } else if (tableName.equals(Table.ChapterBonusPackage.getName())) {
            Map<String, ChapterBonusPackageTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, ChapterBonusPackageTableValue>>() {
            });
            GameEnvironment.chapterBonusPackageTableMap.put(version, map);
        } else if (tableName.equals(Table.ChapterChestTask.getName())) {
            Map<String, ChapterChestTaskTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, ChapterChestTaskTableValue>>() {
            });
            GameEnvironment.chapterChestTaskTableMap.put(version, map);
        } else if (tableName.equals(Table.Chapter.getName())) {
            Map<String, ChapterTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, ChapterTableValue>>() {
            });
            GameEnvironment.chapterTableMap.put(version, map);
        } else if (tableName.equals(Table.Match.getName())) {
            Map<String, MatchTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, MatchTableValue>>() {
            });
            GameEnvironment.matchTableMap.put(version, map);
        } else if (tableName.equals(Table.ChestBullet.getName())) {
            Map<String, ChestBulletTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, ChestBulletTableValue>>() {
            });
            GameEnvironment.chestBulletTableMap.put(version, map);
        } else if (tableName.equals(Table.ChestCoinDiamond.getName())) {
            Map<String, ChestCoinDiamondTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, ChestCoinDiamondTableValue>>() {
            });
            GameEnvironment.chestCoinDiamondTableMap.put(version, map);
        } else if (tableName.equals(Table.ChestContentMap.getName())) {
            Map<String, ChestContentMapTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, ChestContentMapTableValue>>() {
            });
            GameEnvironment.chestContentMapTableMap.put(version, map);
        } else if (tableName.equals(Table.ChestGun.getName())) {
            Map<String, ChestGunTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, ChestGunTableValue>>() {
            });
            GameEnvironment.chestGunTableMap.put(version, map);
        } else if (tableName.equals(Table.CoinBonusPackage.getName())) {
            Map<String, CoinBonusPackageTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, CoinBonusPackageTableValue>>() {
            });
            GameEnvironment.coinBonusPackageTableMap.put(version, map);
        } else if (tableName.equals(Table.Gun.getName())) {
            Map<String, GunTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, GunTableValue>>() {
            });
            GameEnvironment.gunTableMap.put(version, map);
        } else if (tableName.equals(Table.GunUpgradeCount.getName())) {
            Map<String, GunUpgradeCountTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, GunUpgradeCountTableValue>>() {
            });
            GameEnvironment.gunUpgradeCountTableMap.put(version, map);
        } else if (tableName.equals(Table.MatchAIRoundRule.getName())) {
            Map<String, MatchAIRoundRuleTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, MatchAIRoundRuleTableValue>>() {
            });
            GameEnvironment.matchAIRoundRuleTableMap.put(version, map);
        } else if (tableName.equals(Table.PromotionEventPackageGroup.getName())) {
            Map<String, PromotionEventPackageGroupTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, PromotionEventPackageGroupTableValue>>() {
            });
            GameEnvironment.promotionEventPackageGroupTableMap.put(version, map);
        } else if (tableName.equals(Table.PromotionEventPackage.getName())) {
            Map<String, PromotionEventPackageTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, PromotionEventPackageTableValue>>() {
            });
            GameEnvironment.promotionEventPackageTableMap.put(version, map);
        } else if (tableName.equals(Table.ShopChests.getName())) {
            Map<String, ShopChestsTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, ShopChestsTableValue>>() {
            });
            GameEnvironment.shopChestsTableMap.put(version, map);
        } else if (tableName.equals(Table.ShopCoin.getName())) {
            Map<String, ShopCoinTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, ShopCoinTableValue>>() {
            });
            GameEnvironment.shopCoinTableMap.put(version, map);
        } else if (tableName.equals(Table.ShopDiamond.getName())) {
            Map<String, ShopDiamondTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, ShopDiamondTableValue>>() {
            });
            GameEnvironment.shopDiamondTableMap.put(version, map);
        } else if (tableName.equals(Table.SigninDiamondReward.getName()) && version.compareTo("1.0.07") <= 0) {
            Map<String, SigninDiamondRewardTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, SigninDiamondRewardTableValue>>() {
            });
            GameEnvironment.signinDiamondRewardTableMap.put(version, map);
        } else if (tableName.equals(Table.RecordModeMatch.getName())) {
            Map<String, RecordModeMatchTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, RecordModeMatchTableValue>>() {
            });
            GameEnvironment.recordModeMatchTableMap.put(version, map);
        } else if (tableName.equals(Table.LuckyWheelProperty.getName()) && CommonUtils.compareVersion(version)) {
            LuckyWheelPropertyTable luckyWheelPropertyTable = JSONObject.parseObject(content, LuckyWheelPropertyTable.class);
            GameEnvironment.luckyWheelPropertyTableMap.put(version, luckyWheelPropertyTable);
        } else if (tableName.equals(Table.LuckyWheelReward.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, LuckyWheelRewardTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, LuckyWheelRewardTableValue>>() {
            });
            GameEnvironment.luckyWheelRewardTableMap.put(version, map);
        } else if (tableName.equals(Table.LuckyWheelSectorContent.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, LuckyWheelSectorContentTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, LuckyWheelSectorContentTableValue>>() {
            });
            GameEnvironment.luckyWheelSectorContentTableMap.put(version, map);
        } else if (tableName.equals(Table.MatchToRouteUid.getName()) && CommonUtils.compareVersion(version)) {
            GameEnvironment.matchToRouteUidMap = JSONObject.parseObject(content, new TypeReference<Map<String, Long[]>>() {
            });
        } else if (tableName.equals(Table.BulletGiftPackage.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, BulletGiftPackageTableValue> bulletGiftPackageTableValueMap = JSONObject.parseObject(content, new TypeReference<Map<String, BulletGiftPackageTableValue>>() {
            });
            GameEnvironment.bulletGiftPackageTableMap.put(version, bulletGiftPackageTableValueMap);
        } else if (tableName.equals(Table.Chest.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, ChestTableValue> chestTableValueMap = JSONObject.parseObject(content, new TypeReference<Map<String, ChestTableValue>>() {
            });
            GameEnvironment.chestTableMap.put(version, chestTableValueMap);
        } else if (tableName.equals(Table.GunGiftPackageTable.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, GunGiftPackageTableValue> gunGiftPackageTableValueMap = JSONObject.parseObject(content, new TypeReference<Map<String, GunGiftPackageTableValue>>() {
            });
            GameEnvironment.gunGiftPackageTableMap.put(version, gunGiftPackageTableValueMap);
        } else if (tableName.equals(Table.GunGiftPackageGroupTable.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, GunGiftPackageGroupTableValue> gunGiftPackageGroupTableValueMap = JSONObject.parseObject(content, new TypeReference<Map<String, GunGiftPackageGroupTableValue>>() {
            });
            GameEnvironment.gunGiftPackageGroupTableMap.put(version, gunGiftPackageGroupTableValueMap);
        } else if (tableName.equals(Table.FifthDayGunGiftPackage.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, FifthDayGunGiftPackageTableValue> fifthDayGunGiftPackageTableValueMap = JSONObject.parseObject(content, new TypeReference<Map<String, FifthDayGunGiftPackageTableValue>>() {
            });
            GameEnvironment.fifthDayGunGiftPackageTableMap.put(version, fifthDayGunGiftPackageTableValueMap);
        } else if (tableName.equals(Table.FifthDayGunGiftPackageGroup.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, FifthDayGunGiftPackageGroupTableValue> fifthDayGunGiftPackageGroupTableValueMap = JSONObject.parseObject(content, new TypeReference<Map<String, FifthDayGunGiftPackageGroupTableValue>>() {
            });
            GameEnvironment.fifthDayGunGiftPackageGroupTableMap.put(version, fifthDayGunGiftPackageGroupTableValueMap);
        } else if (tableName.equals(Table.LuckyWheelV2Property.getName()) && CommonUtils.compareVersion(version)) {
            LuckyWheelV2PropertyTableValue luckyWheelV2PropertyTable = JSONObject.parseObject(content, LuckyWheelV2PropertyTableValue.class);
            GameEnvironment.luckyWheelV2PropertyTableMap.put(version, luckyWheelV2PropertyTable);
        } else if (tableName.equals(Table.LuckyWheelV2Reward.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, LuckyWheelV2RewardTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, LuckyWheelV2RewardTableValue>>() {
            });
            GameEnvironment.luckyWheelV2RewardTableMap.put(version, map);
        } else if (tableName.equals(Table.LuckyWheelV2SectorContent.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, LuckyWheelV2SectorContentTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, LuckyWheelV2SectorContentTableValue>>() {
            });
            GameEnvironment.luckyWheelV2SectorContentTableMap.put(version, map);
        } else if (tableName.equals(Table.chapterGunGiftPackage.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, ChapterGunGiftPackageTableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, ChapterGunGiftPackageTableValue>>() {
            });
            GameEnvironment.chapterGunGiftPackageTableMap.put(version, map);
        } else if (tableName.equals(Table.PromotionGiftPackageGroupV2.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, PromotionEventPackageGroupV2TableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, PromotionEventPackageGroupV2TableValue>>() {
            });
            GameEnvironment.promotionEventPackageGroupV2TableMap.put(version, map);
        } else if (tableName.equals(Table.PromotionGiftPackageV2.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, PromotionEventPackageV2TableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, PromotionEventPackageV2TableValue>>() {
            });
            GameEnvironment.promotionEventPackageV2TableMap.put(version, map);
        } else if (tableName.equals(Table.PromotionGunGiftPackageV2.getName()) && CommonUtils.compareVersion(version)) {
            Map<String, PromotionEventGunGiftPackageV2TableValue> map = JSONObject.parseObject(content, new TypeReference<Map<String, PromotionEventGunGiftPackageV2TableValue>>() {
            });
            GameEnvironment.promotionEventGunGiftPackageV2TableMap.put(version, map);
        }

    }
}
