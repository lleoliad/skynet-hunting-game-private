package org.skynet.service.provider.hunting.obsolete.pojo.table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataTable implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, AchievementTableValue> achievementTable;

    private Map<String, AnimalTableValue> animalTable;

    private Map<String, ChapterBonusPackageTableValue> chapterBonusPackageTable;

    private Map<String, ChapterChestTaskTableValue> chapterChestTaskTable;

    private Map<String, ChapterTableValue> chapterTable;

    private Map<String, ChestContentMapTableValue> chestContentMapTable;

    private Map<String, ChestGunTableValue> chestGunTable;

    private Map<String, MatchAIRoundRuleTableValue> matchAIRoundRuleTable;

    private Map<String, MatchTableValue> matchTable;

    private Map<String, PromotionEventPackageGroupTableValue> promotionEventPackageGroupTable;

    private Map<String, PromotionEventPackageTableValue> promotionEventPackageTable;

    private Map<String, SigninDiamondRewardTableValue> signinDiamondRewardTable;

    private Map<String, GunTableValue> gunTable;

    private Map<String, BulletTableValue> bulletTable;

    private Map<String, ShopDiamondTableValue> shopDiamondTable;

    private Map<String, ShopCoinTableValue> shopCoinTable;

    private Map<String, ShopChestsTableValue> shopChestsTable;

    private Map<String, ChestBulletTableValue> chestBulletTable;

    private Map<String, ChestCoinDiamondTableValue> chestCoinDiamondTable;

    private Map<String, GunUpgradeCountTableValue> gunUpgradeCountTable;

    private Map<String, CoinBonusPackageTableValue> coinBonusPackageTable;

    private Map<String, AiWeaponCombinationTableValue> aiWeaponCombinationTable;

    private Map<String, RecordModeMatchTableValue> recordModeMatchTable;

}
