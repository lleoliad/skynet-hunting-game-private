package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.service.provider.hunting.obsolete.pojo.entity.PromotionEventPackageData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.table.PromotionEventPackageGroupV2TableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.PromotionEventPackageTableValue;

public interface PromotionEventPackageDataService {

    /**
     * 激活活动礼包
     */
    void refreshPromotionEventPackagesData(String uuid, String gameVersion);

    PromotionEventPackageTableValue findPackageTableValueByPackageTypeAndLevel(Integer packageType, Integer packageLevel, String gameVersion);

    /**
     * 找到玩家某个id的活动礼包
     *
     * @param userData
     * @param productName
     * @return
     */
    PromotionEventPackageData findUserPromotionEventPackageDataByPackageId(UserData userData, String productName);

    void refreshPromotionEventPackageNow(UserData uuid, String gameVersion);

    void refreshBulletGiftPackageNow(UserData userData, Boolean canDeleteExpirePackages, String gameVersion);

    void refreshFifthDayGunGiftPackageDataNow(UserData userData, Boolean canDeleteExpirePackages, String gameVersion);

    void refreshGunGiftPackageDataNow(UserData userData, Boolean canDeleteExpirePackages, String gameVersionBoolean);

    void refreshPromotionEventPackageV2Now(UserData userData, String gameVersion);

    PromotionEventPackageGroupV2TableValue findUserPromotionEventPackageV2DataByPackageId(UserData userData, String gameVersion, String productName);
}
