package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.msgpack.annotation.MessagePackOrdinalEnum;

/**
 * 数据库中可能存在的表名
 */
@AllArgsConstructor
@Getter
@MessagePackOrdinalEnum
public enum Table {

    Achievement("AchievementTable", ""),
    AiWeaponCombination("AiWeaponCombinationTable", ""),
    Animal("AnimalTable", ""),
    Bullet("BulletTable", ""),
    ChapterBonusPackage("ChapterBonusPackageTable", ""),
    ChapterChestTask("ChapterChestTaskTable", ""),
    Chapter("ChapterTable", ""),
    ChestBullet("ChestBulletTable", ""),
    ChestCoinDiamond("ChestCoinDiamondTable", ""),
    ChestContentMap("ChestContentMapTable", ""),
    ChestGun("ChestGunTable", ""),
    CoinBonusPackage("CoinBonusPackageTable", ""),
    Gun("GunTable", ""),
    GunUpgradeCount("GunUpgradeCountTable", ""),
    MatchAIRoundRule("MatchAIRoundRuleTable", ""),
    Match("MatchTable", ""),
    PromotionEventPackageGroup("PromotionEventPackageGroupTable", ""),
    PromotionEventPackage("PromotionEventPackageTable", ""),
    RecordModeMatch("RecordModeMatchTable", ""),
    ShopChests("ShopChestsTable", ""),
    ShopCoin("ShopCoinTable", ""),
    ShopDiamond("ShopDiamondTable", ""),
    SigninDiamondReward("SigninDiamondRewardTable", ""),
    LuckyWheelProperty("LuckyWheelPropertyTable", ""),
    LuckyWheelReward("LuckyWheelRewardTable", ""),
    LuckyWheelSectorContent("LuckyWheelSectorContentTable", ""),

    MatchToRouteUid("MatchToRouteUid", ""),
    VipProperty("VipPropertyTable", ""),
    VipV2Property("VipV2PropertyTable", ""),
    LuckyWheelV2Property("LuckyWheelV2PropertyTable", ""),
    LuckyWheelV2Reward("LuckyWheelV2RewardTable", ""),
    LuckyWheelV2SectorContent("LuckyWheelV2SectorContentTable", ""),
    CommonProperty("CommonPropertyTable", ""),
    FifthDayGunGiftPackageGroup("FifthDayGunGiftPackageGroupTable", ""),
    FifthDayGunGiftPackage("FifthDayGunGiftPackageTable", ""),
    GunGiftPackageGroupTable("GunGiftPackageGroupTable", ""),
    GunGiftPackageTable("GunGiftPackageTable", ""),
    Chest("ChestTable", ""),
    BulletGiftPackage("BulletGiftPackageTable", ""),
    chapterGunGiftPackage("ChapterGunGiftPackageTable", ""),

    PromotionGiftPackageGroupV2("PromotionGiftPackageGroupV2Table", ""),

    PromotionGiftPackageV2("PromotionGiftPackageV2Table", ""),

    PromotionGunGiftPackageV2("PromotionGunGiftPackageV2Table", ""),

    ;

    private final String name;
    private final String description;
}
