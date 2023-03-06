package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.msgpack.annotation.MessagePackOrdinalEnum;
import org.skynet.service.provider.hunting.obsolete.pojo.table.*;

/**
 * 数据库中可能存在的表名
 */
@AllArgsConstructor
@Getter
@MessagePackOrdinalEnum
public enum Table {
    Achievement("AchievementTable", false, AchievementTableValue.class, ""),
    AiWeaponCombination("AiWeaponCombinationTable", false, AiWeaponCombinationTableValue.class, ""),
    Animal("AnimalTable", false, AnimalTableValue.class, ""),
    Bullet("BulletTable", false, BulletTableValue.class, ""),
    ChapterBonusPackage("ChapterBonusPackageTable", false, ChapterBonusPackageTableValue.class, ""),
    ChapterChestTask("ChapterChestTaskTable", false, ChapterChestTaskTableValue.class, ""),
    Chapter("ChapterTable", false, ChapterTableValue.class, ""),
    ChestBullet("ChestBulletTable", false, ChestBulletTableValue.class, ""),
    ChestCoinDiamond("ChestCoinDiamondTable", false, ChestCoinDiamondTableValue.class, ""),
    ChestContentMap("ChestContentMapTable", false, ChestContentMapTableValue.class, ""),
    ChestGun("ChestGunTable", false, ChestGunTableValue.class, ""),
    CoinBonusPackage("CoinBonusPackageTable", false, CoinBonusPackageTableValue.class, ""),
    Gun("GunTable", false, GunTableValue.class, ""),
    GunUpgradeCount("GunUpgradeCountTable", false, GunUpgradeCountTableValue.class, ""),
    MatchAIRoundRule("MatchAIRoundRuleTable", false, MatchAIRoundRuleTableValue.class, ""),
    Match("MatchTable", false, MatchTableValue.class, ""),
    PromotionEventPackageGroup("PromotionEventPackageGroupTable", false, PromotionEventPackageGroupTableValue.class, ""),
    PromotionEventPackage("PromotionEventPackageTable", false, PromotionEventPackageTableValue.class, ""),
    RecordModeMatch("RecordModeMatchTable", false, RecordModeMatchTableValue.class, ""),
    ShopChests("ShopChestsTable", false, ShopChestsTableValue.class, ""),
    ShopCoin("ShopCoinTable", false, ShopCoinTableValue.class, ""),
    ShopDiamond("ShopDiamondTable", false, ShopDiamondTableValue.class, ""),
    SigninDiamondReward("SigninDiamondRewardTable", false, SigninDiamondRewardTableValue.class, ""),
    LuckyWheelProperty("LuckyWheelPropertyTable", true, LuckyWheelPropertyTable.class, ""),
    LuckyWheelReward("LuckyWheelRewardTable", false, LuckyWheelRewardTableValue.class, ""),
    LuckyWheelSectorContent("LuckyWheelSectorContentTable", false, LuckyWheelSectorContentTableValue.class, ""),
    MatchToRouteUid("MatchToRouteUid", false, null, ""),
    VipProperty("VipPropertyTable", true, null, ""),
    VipV2Property("VipV2PropertyTable", true, null, ""),
    LuckyWheelV2Property("LuckyWheelV2PropertyTable", true, LuckyWheelV2PropertyTableValue.class, ""),
    LuckyWheelV2Reward("LuckyWheelV2RewardTable", false, LuckyWheelV2RewardTableValue.class, ""),
    LuckyWheelV2SectorContent("LuckyWheelV2SectorContentTable", false, LuckyWheelV2SectorContentTableValue.class, ""),
    CommonProperty("CommonPropertyTable", true, null, ""),
    FifthDayGunGiftPackageGroup("FifthDayGunGiftPackageGroupTable", false, FifthDayGunGiftPackageGroupTableValue.class, ""),
    FifthDayGunGiftPackage("FifthDayGunGiftPackageTable", false, FifthDayGunGiftPackageTableValue.class, ""),
    GunGiftPackageGroup("GunGiftPackageGroupTable", false, GunGiftPackageGroupTableValue.class, ""),
    GunGiftPackage("GunGiftPackageTable", false, GunGiftPackageTableValue.class, ""),
    Chest("ChestTable", false, ChestTableValue.class, ""),
    BulletGiftPackage("BulletGiftPackageTable", false, BulletGiftPackageTableValue.class, ""),
    ChapterGunGiftPackage("ChapterGunGiftPackageTable", false, ChapterGunGiftPackageTableValue.class, ""),
    PromotionGiftPackageGroupV2("PromotionGiftPackageGroupV2Table", false, PromotionGiftPackageGroupV2TableValue.class, ""),
    PromotionGiftPackageV2("PromotionGiftPackageV2Table", false, PromotionGiftPackageV2TableValue.class, ""),
    PromotionGunGiftPackageV2("PromotionGunGiftPackageV2Table", false, PromotionGunGiftPackageV2TableValue.class, ""),
    ;

    private final String name;
    private final boolean config;
    private final Class<?> classes;
    private final String description;
}
