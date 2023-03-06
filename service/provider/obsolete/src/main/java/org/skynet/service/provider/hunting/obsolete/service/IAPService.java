package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.service.provider.hunting.obsolete.pojo.dto.IapReceiptValidateDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.table.PendingPurchaseOrder;

/**
 * 购物相关
 */
public interface IAPService {


    /**
     * 增加用户某个内购商品的购买次数
     *
     * @param userUid
     * @param productName
     */
    void increaseUserIapProductPurchaseCount(String userUid, String productName);

    /**
     * 获取用户某个内购商品购买次数
     *
     * @param productName
     * @param userUid
     * @return
     */
    int getUserIapProductPurchasedCount(String productName, String userUid);

    /**
     * 保存待购买的客户订单
     *
     * @param userData
     * @param pendingPurchaseData
     */
    void savePendingCustomOrder(UserData userData, UserPendingPurchaseData pendingPurchaseData, String additionalParametersJSON);

    /**
     * 获取未付款订单
     *
     * @param customOrderId
     * @return
     */
    PendingPurchaseOrder getPendingCustomOrder(String customOrderId);

    /**
     * 验证google play订单
     *
     * @param params @return
     */
    ReceiptValidateResult googlePlayReceiptValidate(IapReceiptValidateDTO request);

    /**
     * 内购完成后,发放物品
     *
     * @param uuid
     * @param productName
     * @param gameVersion
     * @return
     */
    IAPPurchaseReward iapPurchaseContentDelivery(String uuid, String productName, String additionalParametersJSON, String gameVersion);

    /**
     * 购买活动礼包
     *
     * @param purchasePackageData
     * @param gameVersion
     * @return
     */
    ChestOpenResult purchasePromotionEventPackage(String uuid, PromotionEventPackageData purchasePackageData, String gameVersion);

    /**
     * 购买章节礼包
     *
     * @param uuid
     * @param packageId
     * @param gameVersion
     * @return
     */
    ChestOpenResult purchaseChapterBonusPackage(String uuid, Integer packageId, String gameVersion);

    /**
     * 清除掉商品的开始购买记录
     *
     * @param userData
     * @param customOrderId
     */
    void clearPendingPurchaseInUserData(UserData userData, String customOrderId);

    /**
     * 保存订单ID
     *
     * @param validateResult
     * @param customOrderId
     */
    void saveCompletedOrder(ReceiptValidateResult validateResult, String receipt, String customOrderId, String playerUid);

    /**
     * 将已完成的订单归档
     *
     * @param customOrderId
     */
    void archivePendingCustomOrder(String customOrderId, String platformOrderId);

    /**
     * 发放子弹礼包奖励
     *
     * @param uuid
     * @param bulletGiftPackageId
     * @param gameVersion
     * @return
     */
    IAPPurchaseReward bulletPackageRewardDelivery(String uuid, int bulletGiftPackageId, String gameVersion);

    /**
     * 五日枪械礼包内购奖励发放
     *
     * @param uuid
     * @param packageId
     * @param gameVersion
     * @return
     */
    IAPPurchaseReward fifthDayGunGiftPackageRewardDelivery(String uuid, int packageId, String gameVersion);


    /**
     * 5-12章枪章节礼包内购奖励发放
     *
     * @param uuid
     * @param packageId
     * @param gameVersion
     * @return
     */
    IAPPurchaseReward chapterGunPackageRewardDelivery(String uuid, int packageId, String gameVersion);

    /**
     * 枪械礼包内购奖励发放
     *
     * @param uuid
     * @param packageId
     * @param gameVersion
     * @return
     */
    IAPPurchaseReward gunPackageRewardDelivery(String uuid, int packageId, String gameVersion);

    /**
     * 根据玩家过往内购数据，推荐礼包弹出的时候，应该选择哪个价位的
     *
     * @param userData
     */
    double getGiftPackagePopUpPriceRecommendPrice(UserData userData);

//    /**
//     * 根据条件获取订单
//     * @param dto
//     * @return
//     */
//    List<CompletedOrder> getCompletedOrder(OrderDTO dto);
//
//
//    /**
//     * 更新订单信息
//     * @param newOrder
//     */
//    String updateCompleteOrder(CompletedOrder newOrder);
//
//   /**
//    * 删除订单
//    * @param orderId
//    */
//   String deleteCompleteOrder(String orderId);
}
