package org.skynet.service.provider.hunting.obsolete.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(value = "IAPProductPrefix对象", description = "记录内购购买次数时，名称的前缀,因为内购有时会同样的product name对应多个礼包，所以需要加前缀和id来区分")
public class IAPProductPrefix {

    @ApiModelProperty(value = "promotionGiftPackage")
    public static final String promotionGiftPackage = "promotion_gift_package";

    @ApiModelProperty(value = "promotionGiftPackageV2")
    public static final String promotionGiftPackageV2 = "promotion_gift_package_v2";

    @ApiModelProperty(value = "promotionGiftPackageV2")
    public static final String promotionGunGiftPackageV2 = "promotion_gun_gift_package_v2";

    @ApiModelProperty(value = "bulletGiftPackage")
    public static final String bulletGiftPackage = "bullet_gift_package";

    @ApiModelProperty(value = "fifthDayGunGiftPackage")
    public static final String fifthDayGunGiftPackage = "fifth_day_gun_gift_package";

    @ApiModelProperty(value = "gunGiftPackage")
    public static final String gunGiftPackage = "gun_gift_package";

}
