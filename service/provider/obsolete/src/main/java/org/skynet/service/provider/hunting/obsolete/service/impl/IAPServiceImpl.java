package org.skynet.service.provider.hunting.obsolete.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.NanoIdUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.config.IAPProductPrefix;
import org.skynet.service.provider.hunting.obsolete.config.VipConfig;
import org.skynet.service.provider.hunting.obsolete.config.VipV2Config;
import org.skynet.service.provider.hunting.obsolete.config.VipV3Config;
import org.skynet.service.provider.hunting.obsolete.enums.OrderState;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.IapReceiptValidateDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Lists;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.table.*;
import org.skynet.service.provider.hunting.obsolete.service.*;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IAPServiceImpl implements IAPService {

    @Resource
    private PromotionEventPackageDataService packageDataService;

    @Resource
    private UserDataService userDataService;

    @Resource
    private ChestService chestService;

    @Resource
    private TopUpOrderService topUpOrderService;


    @Override
    public void increaseUserIapProductPurchaseCount(String userUid, String productName) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);
        if (userData.getIapProductPurchasedCountMap() == null) {
            userData.setIapProductPurchasedCountMap(new HashMap<>());
        }
        int purchasedCount = userData.getIapProductPurchasedCountMap().getOrDefault(productName, 0);
        userData.getIapProductPurchasedCountMap().put(productName, ++purchasedCount);
        log.info("增加内购记录： " + productName + ", count " + purchasedCount);
    }

    @Override
    public int getUserIapProductPurchasedCount(String productName, String userUid) {

        UserData userData = GameEnvironment.userDataMap.get(userUid);
        if (userData.getIapProductPurchasedCountMap() == null || userData.getIapProductPurchasedCountMap().size() == 0) {
            return 0;
        }
        return userData.getIapProductPurchasedCountMap().getOrDefault(productName, 0);
    }

    @Override
    public void savePendingCustomOrder(UserData userData, UserPendingPurchaseData pendingPurchaseData, String additionalParametersJSON) {

        PendingPurchaseOrder pendingOrder = new PendingPurchaseOrder(pendingPurchaseData.getCustomOrderId(),
                userData.getUuid(),
                pendingPurchaseData.getProductName(),
                TimeUtils.getUnixTimeSecond(),
                "",
                additionalParametersJSON
        );

        RedisDBOperation.insertPendingCustomOrders(pendingPurchaseData.getCustomOrderId(), pendingOrder);

        log.info("存储内部订单号" + pendingOrder);
    }

    @Override
    public PendingPurchaseOrder getPendingCustomOrder(String customOrderId) {

        return RedisDBOperation.selectPendingCustomOrder(customOrderId);
    }

    @Override
    public ReceiptValidateResult googlePlayReceiptValidate(IapReceiptValidateDTO receiptValidateDTO) {
        String receipt = receiptValidateDTO.getReceipt();
        //这是按照98服务器端写的，要是json文件取值不相同
        log.warn("已经进入验证内购订单service");
        JSONObject params = JSONObject.parseObject(receipt);
        if (params == null) {
            log.warn("验证内购订单时，传入的参数null");
            throw new BusinessException("验证内购订单时，传入的参数nul");
        } else {
            log.warn("传入的参数不是null，传入的参数为{}", JSONUtil.toJsonStr(params));
        }
//        String[] purchaseInfos = params.getString("Payload").split("\\|");
//        JSONObject googleParam = JSONObject.parseObject(purchaseInfos[3]);
        JSONObject googleParam = JSONObject.parseObject(params.getString("Payload"));
        if (googleParam == null) {
            log.warn("验证内购订单时，googleParam为null");
            throw new BusinessException("验证内购订单时，传入的参数nul");
        } else {
            log.warn("googleParam参数不是null，googleParam的参数为{}", googleParam);
        }
        JSONObject jsonData = JSONObject.parseObject(googleParam.getString("json"));
        if (jsonData == null) {
            log.warn("验证内购订单时，jsonData为null");
            throw new BusinessException("验证内购订单时，传入的参数nul");
        } else {
            log.warn("jsonData参数不是null，jsonData的参数为{}", jsonData);
        }
        String purchaseToken = jsonData.getString("purchaseToken");
        String productId = jsonData.getString("productId");
//        String gameUrl = params.getString("gameServer");
        String packageName = jsonData.getString("packageName");
//        String cpOrderId = purchaseInfos[2].split("_")[1];
        String orderId = jsonData.getString("orderId");
        //在已完成订单中寻找
        TopUpOrder completedOrder = checkIsOrderExist(orderId);

        if (completedOrder != null) {
            log.info("验证内购订单,该订单号已经存在且完成, order id:" + orderId);

            return new ReceiptValidateResult(
                    true,
                    false,
                    productId,
                    orderId,
                    null
            );
        }

        try {
            log.info("该订单号不存在，需要验证");

            List<String> scopes = new ArrayList<>();
            scopes.add(AndroidPublisherScopes.ANDROIDPUBLISHER);
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:static/pc-api-7271145507751356136-749-1117d72d8d6c.json");
//            GoogleCredential credential = new GoogleCredential.Builder()
//                    .setTransport(httpTransport)
//                    .setJsonFactory(jsonFactory)
//                    .setServiceAccountId("iapvalidate@pc-api-7750150048754917476-162.iam.gserviceaccount.com")
//                    .setServiceAccountScopes(scopes)
//                    .setServiceAccountPrivateKeyFromP12File(resource.getFile())
//                    .build();


//            ResourceLoader resourceLoader = new DefaultResourceLoader();
//            org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:static/718144022281-ae30eafb2c5d.json");
            GoogleCredential credential = GoogleCredential.fromStream(resource.getInputStream()).createScoped(scopes);
            log.warn("生成的credential数据：{}", JSONUtil.toJsonStr(credential));
            //使用谷歌凭据和收据从谷歌获取购买信息
//            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            AndroidPublisher publisher = new AndroidPublisher.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(packageName).build();
            AndroidPublisher.Purchases purchases = publisher.purchases();
            final AndroidPublisher.Purchases.Products.Get request = purchases.products().get(packageName, productId, purchaseToken);
            System.out.println("===============" + request + "================");
//            final ProductPurchase purchase = request.execute();


//            InputStream input =  CommonUtils.readP12File(packageName);
//            HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
//            PrivateKey privateKey = SecurityUtils.loadPrivateKeyFromKeyStore(
//                    SecurityUtils.getPkcs12KeyStore(), input, "notasecret", "privatekey", "notasecret");
//            GoogleCredential credential = new GoogleCredential.Builder()
//                    .setTransport(transport).setJsonFactory(JacksonFactory.getDefaultInstance())
//                    .setServiceAccountId(NettyHttpServerHandler.getAccountEmailListProperties().getProperty(packageName)) // 替换掉serviceAccountEmail
//                    .setServiceAccountScopes(AndroidPublisherScopes.all())
//                    .setServiceAccountPrivateKey(privateKey).build();
//
//            AndroidPublisher publisher = new AndroidPublisher.Builder(transport, JacksonFactory.getDefaultInstance(), credential).build();
//            AndroidPublisher.Purchases.Products products = publisher.purchases().products();
//            AndroidPublisher.Purchases.Products.Get product = products.get(packageName, productId, purchaseToken);

            log.warn("【正在向GOOGLE消耗型商品 API发请求，请稍后...】");
            ProductPurchase purchase = request.execute();
            log.warn(">>>purchase {}", purchase.toString());

            QueryWrapper<TopUpOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_number", receiptValidateDTO.getCustomOrderId());
            TopUpOrder topUpOrder = topUpOrderService.getOne(queryWrapper);
            if (topUpOrder == null) {
                throw new BusinessException("mysql数据库中不存在的订单");
            }
            topUpOrder.setPayMode("GooglePay");
            if (0 != purchase.getPurchaseState()) {
                log.warn("充值失败,谷歌返回订单状态:{}", purchase.getPurchaseState());
                //订单校验失败
                topUpOrder.setOrderState(OrderState.VerifyFailed.getType());
                throw new BusinessException("验证内购订单失败" + purchase.toString());
            } else {
                topUpOrder.setOrderState(OrderState.VerifySuccess.getType());
                log.warn("验证内购订单完成" + purchase.toString());
            }

            String resultOrderId = purchase.getOrderId();

            if (!orderId.equals(resultOrderId)) {
                log.warn("验证内购订单 订单号不一致. user give" + orderId + "validate result: " + resultOrderId + ", raw" + JSONObject.toJSONString(purchase));
                topUpOrder.setOrderState(OrderState.VerifyFailed.getType());
                throw new BusinessException("验证内购订单 订单号不一致");
            }

            topUpOrderService.updateById(topUpOrder);

            return new ReceiptValidateResult(
                    false,
                    true,
                    productId,
                    orderId,
                    JSONObject.toJSONString(purchase)
            );

        } catch (IOException | GeneralSecurityException e) {
            log.warn("验证内购订单报错===================================");
            e.printStackTrace();
            log.warn("===================================验证内购订单报错");
        }

        return null;
    }


//    private static GoogleCredential getGoogleCredential() throws IOException {
//        List<String> scopes = new ArrayList<String>();
//        scopes.add(AndroidPublisherScopes.ANDROIDPUBLISHER);
//        ClassLoader classLoader = IAPServiceImpl.class.getClassLoader();
//        GoogleCredential credential = GoogleCredential.fromStream(classLoader.getResourceAsStream("classpath:static/718144022281-ae30eafb2c5d.json"))
//                .createScoped(scopes);
//        return credential;
//    }

//    private static ProductPurchase getPurchase(GoogleReceipt receipt, GoogleCredential credential)
//            throws GeneralSecurityException, IOException {
//        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        JsonFactory jsonFactory = new JacksonFactory();
//        AndroidPublisher publisher = new AndroidPublisher.Builder(httpTransport, jsonFactory, credential)
//                .setApplicationName(YOUR_APPLICATION_NAME).build();
//        AndroidPublisher.Purchases purchases = publisher.purchases();
//
//        final AndroidPublisher.Purchases.Products.Get request = purchases.products().get(receipt.getPackageName(), receipt.getProductId(),
//                receipt.getPurchaseToken());
//        final ProductPurchase purchase = request.execute();
//        return purchase;
//    }


    @Override
    public IAPPurchaseReward iapPurchaseContentDelivery(String uuid, String productName, String additionalParametersJSON, String gameVersion) {

        JSONObject additionalParameter = Objects.isNull(additionalParametersJSON) ? new JSONObject() : JSONObject.parseObject(additionalParametersJSON);
        UserData userData = GameEnvironment.userDataMap.get(uuid);

        boolean isPurchaseComplete = false;
        IAPPurchaseReward result = new IAPPurchaseReward();
        double productPrice = 0d;
        Map<String, ShopDiamondTableValue> shopDiamondTable = GameEnvironment.shopDiamondTableMap.get(gameVersion);

        //内购金币
        Map<String, ShopCoinTableValue> shopCoinTable = GameEnvironment.shopCoinTableMap.get(gameVersion);
        Set<String> coinKeys = shopCoinTable.keySet();
        for (String key : coinKeys) {

            ShopCoinTableValue tableValue = shopCoinTable.get(key);
            if (tableValue.getProductId().equals(productName)) {

                //如果是首次购买，那么数量翻倍
                boolean isFirstPurchase = getUserIapProductPurchasedCount(tableValue.getProductId(), uuid) == 0;

                int rewardCoin = tableValue.getCoinAmount();

                rewardCoin = isFirstPurchase ? rewardCoin * 2 : rewardCoin;
                long coin = userData.getCoin();
                coin += rewardCoin;
                userData.setCoin(coin);
                Long totalEarnedCoin = userData.getHistory().getTotalEarnedCoin();
                userData.getHistory().setTotalEarnedCoin(totalEarnedCoin + rewardCoin);
                increaseUserIapProductPurchaseCount(uuid, tableValue.getProductId());


                log.info("购买金币,id" + tableValue.getId() + ", 是否首次购买" + isFirstPurchase + ", reward coin" + rewardCoin);

                result.setCoin(rewardCoin);
                productPrice = tableValue.getPrice();
                isPurchaseComplete = true;
                break;
            }
        }

        //内购钻石
        if (!isPurchaseComplete) {

            Set<String> keySet = shopDiamondTable.keySet();
            for (String key : keySet) {

                ShopDiamondTableValue tableValue = shopDiamondTable.get(key);
                if (tableValue.getProductId().equals(productName)) {

                    //如果是首次购买，那么数量翻倍
                    boolean isFirstPurchase = getUserIapProductPurchasedCount(tableValue.getProductId(), uuid) == 0;

                    int rewardDiamond = tableValue.getDiamondAmount();

                    rewardDiamond = isFirstPurchase ? rewardDiamond * 2 : rewardDiamond;

                    long diamond = userData.getDiamond() + rewardDiamond;
                    increaseUserIapProductPurchaseCount(uuid, tableValue.getProductId());

                    userData.setDiamond(diamond);
                    //历史钻石
                    Long totalEarnedDiamond = userData.getHistory().getTotalEarnedDiamond();
                    userData.getHistory().setTotalEarnedDiamond(totalEarnedDiamond + rewardDiamond);
                    log.info("购买钻石，id" + tableValue.getId() + isFirstPurchase + ", reward diamond" + rewardDiamond);

                    result.setDiamond(rewardDiamond);

                    productPrice = tableValue.getPrice();

                    isPurchaseComplete = true;

                    break;
                }
            }
        }

        if (!isPurchaseComplete) {

            //内购活动礼包
            PromotionEventPackageData promotionPackageData = packageDataService.findUserPromotionEventPackageDataByPackageId(userData, productName);
            if (promotionPackageData != null) {

                log.info("内购活动礼包,数据: " + promotionPackageData);
                ChestOpenResult chestOpenResult = purchasePromotionEventPackage(uuid, promotionPackageData, gameVersion);
                result.setChestOpenResult(chestOpenResult);

                //这里update必须+1
                chestService.saveChestOpenResult(chestOpenResult, uuid, userData.getUpdateCount() + 1);
                increaseUserIapProductPurchaseCount(uuid, String.format("%s_%s_%s", IAPProductPrefix.promotionGiftPackage, productName, promotionPackageData.getPackageId()));

                isPurchaseComplete = true;
                productPrice = promotionPackageData.getPrice();
            }
//            内购活动礼包V2
            PromotionGiftPackageGroupV2TableValue promotionEventPackageGroupV2 = packageDataService.findUserPromotionEventPackageV2DataByPackageId(userData, gameVersion, productName);
            if (promotionEventPackageGroupV2 != null) {
                ChestOpenResult chestOpenResult = null;
                List<PromotionGiftPackageV2Data> giftPackagesV2Data = userData.getPromotionGiftPackagesV2Data();
                for (PromotionGiftPackageV2Data giftPackagesV2Datum : giftPackagesV2Data) {
                    if (Objects.equals(giftPackagesV2Datum.getPackageGroupId(), promotionEventPackageGroupV2.getId())) {
                        if (giftPackagesV2Datum.getPackageType() == 1) {
                            String purchaseKey = promotionEventPackageGroupV2.getId() + "_" + giftPackagesV2Datum.getPackageType() + "_" + giftPackagesV2Datum.getPackageId();
                            Map<String, PromotionGiftPackageV2TableValue> packageV2TableValueMap = GameEnvironment.promotionGiftPackageV2TableMap.get(gameVersion);
                            PromotionGiftPackageV2TableValue promotionEventPackageV2 = packageV2TableValueMap.get(giftPackagesV2Datum.getPackageId().toString());
                            chestOpenResult = purchasePromotionEventPackageV2(userData, promotionEventPackageV2, purchaseKey, gameVersion);

                            result.setChestOpenResult(chestOpenResult);

                            //这里update必须+1
                            chestService.saveChestOpenResult(chestOpenResult, uuid, userData.getUpdateCount() + 1);
                            increaseUserIapProductPurchaseCount(uuid, String.format("%s_%s_%s", IAPProductPrefix.promotionGiftPackageV2, productName, promotionEventPackageV2.getId()));

                            isPurchaseComplete = true;
                            productPrice = promotionEventPackageGroupV2.getPrice();
                        } else {
                            String purchaseKey = promotionEventPackageGroupV2.getId() + "_" + promotionEventPackageGroupV2.getPackageTypesArray().get(0) + "_" + giftPackagesV2Datum.getPackageId();
                            Map<String, PromotionGunGiftPackageV2TableValue> gunGiftPackageV2TableValueMap = GameEnvironment.promotionGunGiftPackageV2TableMap.get(gameVersion);
                            PromotionGunGiftPackageV2TableValue gunGiftPackageV2 = gunGiftPackageV2TableValueMap.get(giftPackagesV2Datum.getPackageId().toString());
                            chestOpenResult = promotionGunPackageRewardDelivery(gunGiftPackageV2, userData, gameVersion, purchaseKey);
                            result.setChestOpenResult(chestOpenResult);

                            //这里update必须+1
                            chestService.saveChestOpenResult(chestOpenResult, uuid, userData.getUpdateCount() + 1);
                            increaseUserIapProductPurchaseCount(uuid, String.format("%s_%s_%s", IAPProductPrefix.promotionGunGiftPackageV2, productName, gunGiftPackageV2.getId()));

                            isPurchaseComplete = true;
                            productPrice = promotionEventPackageGroupV2.getPrice();
                        }
                    }


                }


            }

//            PromotionEventPackageData promotionPackageData = packageDataService.findUserPromotionEventPackageDataByPackageId(userData, productName);
//            if (promotionPackageData !=null){
//
//                log.info("内购活动礼包,数据: "+promotionPackageData);
//                ChestOpenResult chestOpenResult = null;
//                if (promotionPackageData.getPackageType() == 1){
//                    chestOpenResult = purchasePromotionEventPackage(uuid, promotionPackageData,gameVersion);
//                }else {
//                    chestOpenResult = promotionGunPackageRewardDelivery(uuid, promotionPackageData, gameVersion);
//                }
//
//                result.setChestOpenResult(chestOpenResult);
//
//                //这里update必须+1
//                chestService.saveChestOpenResult(chestOpenResult,uuid, userData.getUpdateCount()+1);
//                increaseUserIapProductPurchaseCount(uuid, String.format("%s_%s_%s", IAPProductPrefix.promotionGiftPackage, productName, promotionPackageData.getPackageId()));
//
//                isPurchaseComplete = true;
//                productPrice = promotionPackageData.getPrice();
//            }
        }

        if (!isPurchaseComplete) {

            Map<String, ChapterBonusPackageTableValue> chapterBonusPackageTable = GameEnvironment.chapterBonusPackageTableMap.get(gameVersion);
            Map<String, ChapterGunGiftPackageTableValue> chapterGunGiftPackageTable = GameEnvironment.chapterGunGiftPackageTableMap.get(gameVersion);
            //2,3,4章的章节礼包
            Set<String> bonusKeys = chapterBonusPackageTable.keySet();

            for (String key : bonusKeys) {
                ChapterBonusPackageTableValue tableValue = chapterBonusPackageTable.get(key);

                if (tableValue.getProductId().equals(productName)) {

                    log.info("内购章节礼包" + tableValue.getProductId() + ",id" + tableValue.getId());
                    ChestOpenResult chestOpenResult = purchaseChapterBonusPackage(uuid, tableValue.getId(), gameVersion);
                    result.setChestOpenResult(chestOpenResult);

                    //这里update必须+1
                    chestService.saveChestOpenResult(chestOpenResult, uuid, userData.getUpdateCount() + 1);
                    increaseUserIapProductPurchaseCount(uuid, tableValue.getProductId());

                    isPurchaseComplete = true;
                    productPrice = tableValue.getPrice();
                    break;
                }
            }

            // int gameVersionNum = 0;
            // for (String s : gameVersion.split("\\.")) {
            //     gameVersionNum += Integer.parseInt(s);
            // }
            //
            // if (gameVersionNum >= 14) {
            if (gameVersion.compareTo("1.0.13") >= 0) {
                //5-12章的章节礼包
                Set<String> chapterGunGiftPackageIdSet = chapterGunGiftPackageTable.keySet();

                for (String key : chapterGunGiftPackageIdSet) {
                    ChapterGunGiftPackageTableValue tableValue = chapterGunGiftPackageTable.get(key);
                    if (tableValue.getProductId().equals(productName)) {
                        log.info("内购章节礼包" + tableValue.getProductId() + ",章节id" + tableValue.getId());
                        IAPPurchaseReward iapPurchaseReward = chapterGunPackageRewardDelivery(uuid, tableValue.getId(), gameVersion);
                        ChestOpenResult chestOpenResult = iapPurchaseReward.getChestOpenResult();
                        result.setChestOpenResult(chestOpenResult);

                        //这里update必须+1
                        chestService.saveChestOpenResult(chestOpenResult, uuid, userData.getUpdateCount() + 1);
                        increaseUserIapProductPurchaseCount(uuid, tableValue.getProductId());

                        isPurchaseComplete = true;
                        productPrice = iapPurchaseReward.getPrice();
                        break;
                    }

                }
            }


        }

        if (!isPurchaseComplete) {
            //内购vip/svip
            if (productName.equals(VipConfig.vipProductName) || productName.equals(VipConfig.svipProductName)) {
                double vipPrice = userDataService.purchaseVip(userData, productName, gameVersion);
                increaseUserIapProductPurchaseCount(uuid, productName);
                isPurchaseComplete = true;
                productPrice = vipPrice;
            }
        }

        if (!isPurchaseComplete) {
            //内购第二版vip/svip
            if (productName.equals(VipV2Config.vipProductName) || productName.equals(VipV2Config.svipProductName)) {
                double vipPrice = userDataService.purchaseVipV2(userData, productName, gameVersion);
                increaseUserIapProductPurchaseCount(uuid, productName);
                isPurchaseComplete = true;
                productPrice = vipPrice;
            }
        }

        if (!isPurchaseComplete) {
            //内购第三版vip/svip
            if (productName.equals(VipV3Config.vipProductName) || productName.equals(VipV3Config.svipProductName)) {
                double vipPrice = userDataService.purchaseVipV3(userData, productName, gameVersion);
                increaseUserIapProductPurchaseCount(uuid, productName);
                isPurchaseComplete = true;
                productPrice = vipPrice;
            }
        }


        if (!isPurchaseComplete) {
            //子弹礼包
            int bulletGiftPackageId = additionalParameter.getIntValue("bulletGiftPackageId");
            if (bulletGiftPackageId > 0) {
                IAPPurchaseReward iapPurchaseReward = bulletPackageRewardDelivery(uuid, bulletGiftPackageId, gameVersion);
                result.setChestOpenResult(iapPurchaseReward.getChestOpenResult());
                increaseUserIapProductPurchaseCount(uuid, String.format("%s_%s_%s", IAPProductPrefix.bulletGiftPackage, productName, bulletGiftPackageId));
                //这里update必须+1
                chestService.saveChestOpenResult(result.getChestOpenResult(), uuid, userData.getUpdateCount() + 1);
                isPurchaseComplete = true;
                productPrice = iapPurchaseReward.getPrice();
            }
        }

        if (!isPurchaseComplete) {
            //五日枪械礼包
            int fifthDayGunGiftPackageId = additionalParameter.getIntValue("fifthDayGunGiftPackageId");
            if (fifthDayGunGiftPackageId > 0) {
                IAPPurchaseReward iapPurchaseReward = fifthDayGunGiftPackageRewardDelivery(uuid, fifthDayGunGiftPackageId, gameVersion);
                result.setChestOpenResult(iapPurchaseReward.getChestOpenResult());
                //这里update必须+1
                chestService.saveChestOpenResult(result.getChestOpenResult(), uuid, userData.getUpdateCount() + 1);
                increaseUserIapProductPurchaseCount(uuid, String.format("%s_%s_%s", IAPProductPrefix.fifthDayGunGiftPackage, productName, fifthDayGunGiftPackageId));
                isPurchaseComplete = true;
                productPrice = iapPurchaseReward.getPrice();
            }
        }

        if (!isPurchaseComplete) {
            int gunGiftPackageId = additionalParameter.getIntValue("gunGiftPackageId");
            if (gunGiftPackageId > 0) {
                //枪械礼包
                IAPPurchaseReward iapPurchaseReward = gunPackageRewardDelivery(uuid, gunGiftPackageId, gameVersion);
                result.setChestOpenResult(iapPurchaseReward.getChestOpenResult());
                //这里update必须+1
                chestService.saveChestOpenResult(result.getChestOpenResult(), uuid, userData.getUpdateCount() + 1);
                increaseUserIapProductPurchaseCount(uuid, String.format("%s_%s_%s", IAPProductPrefix.gunGiftPackage, productName, gunGiftPackageId));
                isPurchaseComplete = true;
                productPrice = iapPurchaseReward.getPrice();
            }
        }

        if (!isPurchaseComplete) {
            throw new BusinessException("内购" + productName + "没有找到有效条目");
        }
        double price = userData.getHistory().getAccumulateMoneyPaid() + productPrice;
        userData.getHistory().setAccumulateMoneyPaid(price);
        int count = userData.getHistory().getMoneyPaidCount() + 1;
        userData.getHistory().setMoneyPaidCount(count);

        log.info("内购花费总计更新:" + userData.getHistory().getAccumulateMoneyPaid() + ",次数" + userData.getHistory().getMoneyPaidCount());

        return result;
    }

    private ChestOpenResult promotionGunPackageRewardDelivery(PromotionGunGiftPackageV2TableValue packageTableValue, UserData userData, String gameVersion, String purchaseKey) {
        List<String> eventPackagesV2Keys = userData.getServerOnly().getPurchasedPromotionEventPackagesV2Keys();

        if (eventPackagesV2Keys.contains(purchaseKey)) {
            throw new BusinessException("玩家" + userData.getUuid() + "购买活动礼包,该礼包已经购买过 package purchase key" + purchaseKey);
        }


        ChestOpenResult chestOpenResult = new ChestOpenResult();
        chestOpenResult.setChestData(new ChestData(NanoIdUtils.randomNanoId(30),
                packageTableValue.getRewardChestType(),
                packageTableValue.getRewardChestLevel(),
                TimeUtils.getUnixTimeSecond()));
        chestOpenResult.setCoin(0);
        chestOpenResult.setDiamond(0);

        //获得金币
        if (packageTableValue.getRewardCoinCount() > 0) {
            userData.setCoin(userData.getCoin() + packageTableValue.getRewardCoinCount());
            chestOpenResult.setCoin(chestOpenResult.getCoin() + packageTableValue.getRewardCoinCount());
        }
        //获得钻石
        if (packageTableValue.getRewardDiamondCount() > 0) {
            userData.setDiamond(userData.getDiamond() + packageTableValue.getRewardDiamondCount());
            chestOpenResult.setDiamond(chestOpenResult.getDiamond() + packageTableValue.getRewardDiamondCount());
        }
        //获得子弹
        if (Objects.nonNull(packageTableValue.getRewardBulletIdArray()) && packageTableValue.getRewardBulletIdArray().size() > 0) {
            if (packageTableValue.getRewardBulletIdArray().size() != packageTableValue.getRewardBulletCountArray().size()) {
                throw new BusinessException("活动枪械礼包id" + packageTableValue.getId() + "rewardBulletIdArray 和 rewardBulletCountArray 长度不一致");
            }

            userDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletIdArray(),
                    packageTableValue.getRewardBulletCountArray());
            userDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletIdArray(),
                    packageTableValue.getRewardBulletCountArray());
        }

        //枪械奖励是直接填表
        if (packageTableValue.getRewardGunIdArray().size() != packageTableValue.getRewardGunCountArray().size()) {
            throw new BusinessException("活动枪械礼包id" + packageTableValue.getId() + "rewardGunIdArray 和 rewardGunCountArray 长度不一致");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunIdArray(), packageTableValue.getRewardGunCountArray());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        userDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunIdArray(), packageTableValue.getRewardGunCountArray(), newUnlockGunIds, gameVersion);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        userDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion);
        userDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap);

        //记录购买的礼包
        eventPackagesV2Keys.add(purchaseKey);
        userData.getServerOnly().setPurchasedPromotionEventPackagesV2Keys(eventPackagesV2Keys);

        //刷新礼包数据
        packageDataService.refreshPromotionEventPackageV2Now(userData, gameVersion);
        log.info("购买活动礼包完成,package data:" + packageTableValue);

        return chestOpenResult;
    }

    private ChestOpenResult purchasePromotionEventPackageV2(UserData userData, PromotionGiftPackageV2TableValue promotionEventPackageV2, String purchaseKey, String gameVersion) {

        List<String> packagesV2Keys = userData.getServerOnly().getPurchasedPromotionEventPackagesV2Keys();
        if (packagesV2Keys.contains(purchaseKey)) {
            throw new BusinessException("用户已经购买过该礼包：" + packagesV2Keys);
        }


        ChestOpenResult chestOpenResult = new ChestOpenResult(new ChestData(NanoIdUtils.randomNanoId(30), promotionEventPackageV2.getChestType(), promotionEventPackageV2.getPackageLevel(), TimeUtils.getUnixTimeSecond()), 0, 0, null, null, null);


        //钻石
        if (promotionEventPackageV2.getDiamond() > 0) {

            long diamond = userData.getDiamond() + promotionEventPackageV2.getDiamond();
            userData.setDiamond(diamond);

            int chestDiamond = chestOpenResult.getDiamond() + promotionEventPackageV2.getDiamond();
            chestOpenResult.setDiamond(chestDiamond);
        }

        //枪械
        if (promotionEventPackageV2.getRewardGunCountsArray() != null) {

            if (chestOpenResult.getNewUnlockedGunIDs() == null) {
                chestOpenResult.setNewUnlockedGunIDs(new ArrayList<>());
            }

            userDataService.addGunToUserDataByIdAndCountArray(userData,
                    promotionEventPackageV2.getRewardGunIDsArray(),
                    promotionEventPackageV2.getRewardGunCountsArray(),
                    chestOpenResult.getNewUnlockedGunIDs(), gameVersion);
            userDataService.mergeGunCountMapToChestOpenResult(chestOpenResult,
                    CommonUtils.combineGunIdAndCountArrayToGunCountMap(promotionEventPackageV2.getRewardGunIDsArray(), promotionEventPackageV2.getRewardGunCountsArray()));
            userDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, CommonUtils.combineGunIdAndCountArrayToGunCountMap(promotionEventPackageV2.getRewardGunIDsArray(), promotionEventPackageV2.getRewardGunCountsArray()), gameVersion);
        }

        //子弹
        if (promotionEventPackageV2.getRewardBulletIDsArray() != null) {

            userDataService.addBulletToUserDataByIdAndCountArray(userData,
                    promotionEventPackageV2.getRewardBulletIDsArray(),
                    promotionEventPackageV2.getRewardBulletCountArray());
            userDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    promotionEventPackageV2.getRewardBulletIDsArray(),
                    promotionEventPackageV2.getRewardBulletCountArray());
        }


        packagesV2Keys.add(purchaseKey);
        userData.getServerOnly().setPurchasedPromotionEventPackagesV2Keys(packagesV2Keys);
        //刷新礼包数据
        packageDataService.refreshPromotionEventPackageV2Now(userData, gameVersion);
        log.info("购买活动礼包完成,package data:" + promotionEventPackageV2);

        return chestOpenResult;
    }

    private ChestOpenResult promotionGunPackageRewardDelivery(String uuid, PromotionEventPackageData purchasePackageData, String gameVersion) {
        UserData userData = GameEnvironment.userDataMap.get(uuid);
        List<PromotionGiftPackageV2Data> promotionGiftPackagesV2Data = userData.getPromotionGiftPackagesV2Data();
        if (promotionGiftPackagesV2Data == null || promotionGiftPackagesV2Data.size() == 0) {
            throw new BusinessException("玩家" + userData.getUuid() + "购买活动礼包，礼包数据为空");
        }

        List<String> purchasedPromotionEventPackagesKeys = userData.getServerOnly().getPurchasedPromotionEventPackagesKeys();

        if (purchasedPromotionEventPackagesKeys.contains(purchasePackageData.getServer_only_purchaseKey())) {

            throw new BusinessException("玩家" + userData.getUuid() + "购买活动礼包,该礼包已经购买过 package purchase key" + purchasePackageData.getServer_only_purchaseKey());

        }

        Map<String, PromotionGunGiftPackageV2TableValue> promotionEventGunGiftPackageTable = GameEnvironment.promotionGunGiftPackageV2TableMap.get(gameVersion);
        //奖励
        PromotionGunGiftPackageV2TableValue packageTableValue = promotionEventGunGiftPackageTable.get(String.valueOf(purchasePackageData.getPackageId()));


        ChestOpenResult chestOpenResult = new ChestOpenResult();
        chestOpenResult.setChestData(new ChestData(NanoIdUtils.randomNanoId(30),
                packageTableValue.getRewardChestType(),
                packageTableValue.getRewardChestLevel(),
                TimeUtils.getUnixTimeSecond()));
        chestOpenResult.setCoin(0);
        chestOpenResult.setDiamond(0);

        //获得金币
        if (packageTableValue.getRewardCoinCount() > 0) {
            userData.setCoin(userData.getCoin() + packageTableValue.getRewardCoinCount());
            chestOpenResult.setCoin(chestOpenResult.getCoin() + packageTableValue.getRewardCoinCount());
        }
        //获得钻石
        if (packageTableValue.getRewardDiamondCount() > 0) {
            userData.setDiamond(userData.getDiamond() + packageTableValue.getRewardDiamondCount());
            chestOpenResult.setDiamond(chestOpenResult.getDiamond() + packageTableValue.getRewardDiamondCount());
        }
        //获得子弹
        if (Objects.nonNull(packageTableValue.getRewardBulletIdArray()) && packageTableValue.getRewardBulletIdArray().size() > 0) {
            if (packageTableValue.getRewardBulletIdArray().size() != packageTableValue.getRewardBulletCountArray().size()) {
                throw new BusinessException("活动枪械礼包id" + packageTableValue.getId() + "rewardBulletIdArray 和 rewardBulletCountArray 长度不一致");
            }

            userDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletIdArray(),
                    packageTableValue.getRewardBulletCountArray());
            userDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletIdArray(),
                    packageTableValue.getRewardBulletCountArray());
        }

        //枪械奖励是直接填表
        if (packageTableValue.getRewardGunIdArray().size() != packageTableValue.getRewardGunCountArray().size()) {
            throw new BusinessException("活动枪械礼包id" + packageTableValue.getId() + "rewardGunIdArray 和 rewardGunCountArray 长度不一致");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunIdArray(), packageTableValue.getRewardGunCountArray());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        userDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunIdArray(), packageTableValue.getRewardGunCountArray(), newUnlockGunIds, gameVersion);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        userDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion);
        userDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap);

        //记录购买的礼包
        purchasedPromotionEventPackagesKeys.add(purchasePackageData.getServer_only_purchaseKey());

        //刷新礼包数据
        packageDataService.refreshPromotionEventPackageV2Now(userData, gameVersion);
        log.info("购买活动礼包完成,package data:" + packageTableValue);

        return chestOpenResult;
    }


    @Override
    public ChestOpenResult purchasePromotionEventPackage(String uuid, PromotionEventPackageData purchasePackageData, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);
        List<PromotionEventPackageData> promotionEventPackagesData = userData.getPromotionEventPackagesData();
        if (promotionEventPackagesData == null || promotionEventPackagesData.size() == 0) {
            throw new BusinessException("玩家" + userData.getUuid() + "购买活动礼包，礼包数据为空");
        }

        List<String> purchasedPromotionEventPackagesKeys = userData.getServerOnly().getPurchasedPromotionEventPackagesKeys();

        if (purchasedPromotionEventPackagesKeys.contains(purchasePackageData.getServer_only_purchaseKey())) {

            throw new BusinessException("玩家" + userData.getUuid() + "购买活动礼包,该礼包已经购买过 package purchase key" + purchasePackageData.getServer_only_purchaseKey());

        }
        ChestOpenResult chestOpenResult = new ChestOpenResult(purchasePackageData.getChestData(), 0, 0, null, null, null);

        Map<String, PromotionEventPackageTableValue> promotionEventPackageTable = GameEnvironment.promotionEventPackageTableMap.get(gameVersion);

        //奖励
        PromotionEventPackageTableValue tableValue = promotionEventPackageTable.get(String.valueOf(purchasePackageData.getPackageId()));

        //钻石
        if (tableValue.getDiamond() > 0) {

            long diamond = userData.getDiamond() + tableValue.getDiamond();
            userData.setDiamond(diamond);

            int chestDiamond = chestOpenResult.getDiamond() + tableValue.getDiamond();
            chestOpenResult.setDiamond(chestDiamond);
        }

        //枪械
        if (tableValue.getRewardGunCountsArray() != null) {

            if (chestOpenResult.getNewUnlockedGunIDs() == null) {
                chestOpenResult.setNewUnlockedGunIDs(new ArrayList<>());
            }

            userDataService.addGunToUserDataByIdAndCountArray(userData,
                    tableValue.getRewardGunIDsArray(),
                    tableValue.getRewardGunCountsArray(),
                    chestOpenResult.getNewUnlockedGunIDs(), gameVersion);
            userDataService.mergeGunCountMapToChestOpenResult(chestOpenResult,
                    CommonUtils.combineGunIdAndCountArrayToGunCountMap(tableValue.getRewardGunIDsArray(), tableValue.getRewardGunCountsArray()));
            userDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, CommonUtils.combineGunIdAndCountArrayToGunCountMap(tableValue.getRewardGunIDsArray(), tableValue.getRewardGunCountsArray()), gameVersion);
        }

        //子弹
        if (tableValue.getRewardBulletIDsArray() != null) {

            userDataService.addBulletToUserDataByIdAndCountArray(userData,
                    tableValue.getRewardBulletIDsArray(),
                    tableValue.getRewardBulletCountArray());
            userDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    tableValue.getRewardBulletIDsArray(),
                    tableValue.getRewardBulletCountArray());
        }

//        populateSendToClientUserDataWithChestOpenResult(chestOpenResult);

        //记录购买的礼包
        purchasedPromotionEventPackagesKeys.add(purchasePackageData.getServer_only_purchaseKey());

        //刷新礼包数据
        packageDataService.refreshPromotionEventPackageNow(userData, gameVersion);
        log.info("购买活动礼包完成,package data:" + promotionEventPackagesData);

        return chestOpenResult;

    }

    @Override
    public ChestOpenResult purchaseChapterBonusPackage(String uuid, Integer packageId, String gameVersion) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);

        List<ChapterBonusPackageData> chapterBonusPackagesData = userData.getChapterBonusPackagesData();

        if (chapterBonusPackagesData == null || chapterBonusPackagesData.size() == 0) {
            throw new BusinessException("玩家" + userData.getUuid() + "购买章节礼包,礼包数据为空");
        }

        ChapterBonusPackageData targetPackageData = null;

        for (ChapterBonusPackageData packageData : chapterBonusPackagesData) {

            if (packageData.getChapterId().equals(packageId)) {
                targetPackageData = packageData;
                break;
            }
        }

        if (targetPackageData == null) {
            throw new BusinessException("玩家" + uuid + "购买章节礼包,无法找到礼包数据" + packageId);
        }

        Map<String, ChapterBonusPackageTableValue> chapterBonusPackageTable = GameEnvironment.chapterBonusPackageTableMap.get(gameVersion);
        ChapterBonusPackageTableValue tableValue = chapterBonusPackageTable.get(String.valueOf(targetPackageData.getChapterId()));

        ChestData chestData = new ChestData(null, tableValue.getChestType(), tableValue.getChestLevel(), TimeUtils.getUnixTimeSecond());

        ChestOpenResult chestOpenResult = new ChestOpenResult(chestData, 0, 0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        long coin = userData.getCoin() + tableValue.getCoin();
        userData.setCoin(coin);
        userData.getHistory().setTotalEarnedCoin(userData.getHistory().getTotalEarnedCoin() + tableValue.getCoin());

        long diamond = userData.getDiamond() + tableValue.getDiamond();
        userData.setDiamond(diamond);
        userData.getHistory().setTotalEarnedDiamond(userData.getHistory().getTotalEarnedDiamond() + tableValue.getDiamond());

        chestOpenResult.setCoin(tableValue.getCoin());
        chestOpenResult.setDiamond(tableValue.getDiamond());
        if (tableValue.getRewardGunIDsArray().size() > 0) {

            userDataService.addGunToUserDataByIdAndCountArray(userData, tableValue.getRewardGunIDsArray(), tableValue.getRewardGunCountsArray(), chestOpenResult.getNewUnlockedGunIDs(), gameVersion);

            userDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, CommonUtils.combineGunIdAndCountArrayToGunCountMap(tableValue.getRewardGunIDsArray(), tableValue.getRewardGunCountsArray()));
        }


        List<ChapterBonusPackageData> bonusPackagesData = userData.getChapterBonusPackagesData();
        ChapterBonusPackageData finalTargetPackageData = targetPackageData;
        List<ChapterBonusPackageData> collect = bonusPackagesData.stream().filter(value -> value != finalTargetPackageData).collect(Collectors.toList());
        userData.setChapterBonusPackagesData(collect);

        return chestOpenResult;
    }

    @Override
    public void clearPendingPurchaseInUserData(UserData userData, String customOrderId) {

        if (userData.getIapPendingPurchaseProductsData() == null) {

            userData.setIapPendingPurchaseProductsData(new ArrayList<>());
        }

        List<UserPendingPurchaseData> pendingPurchaseProductNames = userData.getIapPendingPurchaseProductsData();

        pendingPurchaseProductNames = pendingPurchaseProductNames.stream().
                filter(value -> !value.getCustomOrderId().equals(customOrderId)).collect(Collectors.toList());
        userData.setIapPendingPurchaseProductsData(pendingPurchaseProductNames);

        log.info("完成Pending内购" + customOrderId + ",result" + pendingPurchaseProductNames);
    }


    @Override
    public void saveCompletedOrder(ReceiptValidateResult validateResult, String receipt, String customOrderId, String playerUid) {

//        CompletedOrder savaData = new CompletedOrder(
//                validateResult.getValidateRawRsp(),
//                validateResult.getProductName(),
//                playerUid,
//                customOrderId,
////                validateResult.getStartDate()
//                TimeUtils.getUnixTimeSecond()
////                validateResult.getOrderOmitState(),
////                validateResult.getPayMode()
//        );
        // TODO: 2023/1/3 这里有待商榷
        QueryWrapper<TopUpOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_number", customOrderId);
        TopUpOrder topUpOrder = topUpOrderService.getOne(queryWrapper);
        topUpOrder.setOrderEndDate(LocalDateTime.now());
        topUpOrder.setOrderState(OrderState.Completed.getType());
        topUpOrder.setReceiptValidateResult(JSONObject.toJSONString(validateResult));
        topUpOrderService.updateById(topUpOrder);
//        RedisDBOperation.insertCompleteOrder(validateResult.getOrderId(),savaData);
        log.info("保存订单号" + topUpOrder);
    }

    @Override
    public void archivePendingCustomOrder(String customOrderId, String platformOrderId) {

        PendingPurchaseOrder pendingPurchaseOrder = RedisDBOperation.selectPendingCustomOrder(customOrderId);
        pendingPurchaseOrder.setPlatformOrderId(platformOrderId);

        RedisDBOperation.insertArchiveCustomOrders(pendingPurchaseOrder.getCustomOrderId(), pendingPurchaseOrder);

        RedisDBOperation.deletePendingCustomOrder(customOrderId);

        log.info("归档内部订单号 " + pendingPurchaseOrder.toString());
    }

    @Override
    public IAPPurchaseReward bulletPackageRewardDelivery(String uuid, int bulletGiftPackageId, String gameVersion) {
        log.info("购买子弹礼包：" + bulletGiftPackageId);
        UserData userData = GameEnvironment.userDataMap.get(uuid);
        List<PlayerBulletGiftPackageData> availableBulletGiftPackageData = userData.getAvailableBulletGiftPackageData();
        PlayerBulletGiftPackageData purchaseBulletGiftPackageData = null;
        for (int i = 0; i < availableBulletGiftPackageData.size(); i++) {
            PlayerBulletGiftPackageData packageData = availableBulletGiftPackageData.get(i);
            if (packageData.getPackageId() == bulletGiftPackageId) {
                purchaseBulletGiftPackageData = packageData;
                break;
            }
        }
        if (Objects.isNull(purchaseBulletGiftPackageData)) {
            throw new BusinessException("当前所有子弹礼包" + Strings.join(availableBulletGiftPackageData, ',') + "没有id为 " + bulletGiftPackageId + "的礼包");
        }

        Map<String, BulletGiftPackageTableValue> bulletGiftPackageTable = GameEnvironment.bulletGiftPackageTableMap.get(gameVersion);
        BulletGiftPackageTableValue tableValue = bulletGiftPackageTable.get(String.valueOf(purchaseBulletGiftPackageData.getPackageId()));

        ChestData chestData = new ChestData(NanoIdUtils.randomNanoId(30),
                tableValue.getRewardChestType(),
                purchaseBulletGiftPackageData.getChestLevel(),
                TimeUtils.getUnixTimeSecond());

        ChestOpenResult chestOpenResult = chestService.openChest(userData, chestData, gameVersion);
        if (tableValue.getRewardBulletIdArray().size() != tableValue.getRewardBulletCountArray().size()) {
            throw new BusinessException("子弹礼包id" + tableValue.getId() + "rewardBulletIdArray 和 rewardBulletCountArray 长度不一致");
        }

        userDataService.addBulletToUserDataByIdAndCountArray(userData,
                tableValue.getRewardBulletIdArray(),
                tableValue.getRewardBulletCountArray());
        userDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                tableValue.getRewardBulletIdArray(),
                tableValue.getRewardBulletCountArray());

        //钻石奖励
        if (tableValue.getRewardDiamondCount() > 0) {
            userData.setDiamond(userData.getDiamond() + tableValue.getRewardDiamondCount());
            chestOpenResult.setDiamond(Objects.isNull(chestOpenResult.getDiamond()) ? 0 : chestOpenResult.getDiamond());
            chestOpenResult.setDiamond(chestOpenResult.getDiamond() + tableValue.getRewardDiamondCount());
        }

        // TODO: 2023/1/2 这个循环没弄清楚是干啥的，需要核实
        final PlayerBulletGiftPackageData playerBulletGiftPackageData = purchaseBulletGiftPackageData;
        List<PlayerBulletGiftPackageData> collect = availableBulletGiftPackageData.stream().filter(value -> value != playerBulletGiftPackageData).collect(Collectors.toList());
        userData.setAvailableBulletGiftPackageData(collect);

        IAPPurchaseReward result = new IAPPurchaseReward();
        result.setChestOpenResult(chestOpenResult);
        result.setPrice(tableValue.getPrice());

        return result;
    }

    @Override
    public IAPPurchaseReward fifthDayGunGiftPackageRewardDelivery(String uuid, int packageId, String gameVersion) {
        log.info("五日枪械礼包：" + packageId);
        UserData userData = GameEnvironment.userDataMap.get(uuid);

        Map<String, FifthDayGunGiftPackageTableValue> fifthDayGunGiftPackageTable = GameEnvironment.fifthDayGunGiftPackageTableMap.get(gameVersion);
        FifthDayGunGiftPackageTableValue packageTableValue = fifthDayGunGiftPackageTable.get(String.valueOf(packageId));

        List<PlayerFifthDayGunGiftPackageData> availableFifthDayGunGiftPackageData = userData.getAvailableFifthDayGunGiftPackageData();
        PlayerFifthDayGunGiftPackageData targetPackageData = null;
        for (int i = 0; i < availableFifthDayGunGiftPackageData.size(); i++) {
            PlayerFifthDayGunGiftPackageData packageData = availableFifthDayGunGiftPackageData.get(i);
            if (packageData.getPackageId() == packageId) {
                targetPackageData = packageData;
                break;
            }
        }
        if (Objects.isNull(targetPackageData)) {
            throw new BusinessException("无法找到package id " + packageId + "的五日枪械礼包数据。 " + Strings.join(availableFifthDayGunGiftPackageData, ','));
        }

        Map<String, FifthDayGunGiftPackageGroupTableValue> fifthDayGunGiftPackageGroupTable = GameEnvironment.fifthDayGunGiftPackageGroupTableMap.get(gameVersion);
        FifthDayGunGiftPackageGroupTableValue groupTableValue = fifthDayGunGiftPackageGroupTable.get(String.valueOf(targetPackageData.getGroupId()));

        ChestOpenResult chestOpenResult = new ChestOpenResult();
        chestOpenResult.setChestData(new ChestData(NanoIdUtils.randomNanoId(30),
                packageTableValue.getRewardChestType(),
                packageTableValue.getRewardChestLevel(),
                TimeUtils.getUnixTimeSecond()));
        chestOpenResult.setCoin(0);
        chestOpenResult.setDiamond(0);

        //获得金币
        if (packageTableValue.getRewardCoinCount() > 0) {
            chestOpenResult.setCoin(chestOpenResult.getCoin() + packageTableValue.getRewardCoinCount());
            userData.setCoin(userData.getCoin() + packageTableValue.getRewardCoinCount());
        }
        //获得钻石
        if (packageTableValue.getRewardDiamondCount() > 0) {
            chestOpenResult.setDiamond(chestOpenResult.getDiamond() + packageTableValue.getRewardDiamondCount());
            userData.setDiamond(userData.getDiamond() + packageTableValue.getRewardDiamondCount());
        }
        //获得子弹
        if (Objects.nonNull(packageTableValue.getRewardBulletIdArray()) && packageTableValue.getRewardBulletIdArray().size() > 0) {
            if (packageTableValue.getRewardBulletIdArray().size() != packageTableValue.getRewardBulletCountArray().size()) {
                throw new BusinessException("5日枪械礼包id" + packageTableValue.getId() + "rewardBulletIdArray 和 rewardBulletCountArray 长度不一致");
            }

            userDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletIdArray(),
                    packageTableValue.getRewardBulletCountArray());
            userDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletIdArray(),
                    packageTableValue.getRewardBulletCountArray());
        }

        //枪械奖励是直接填表
        if (packageTableValue.getRewardGunIdArray().size() != packageTableValue.getRewardGunCountArray().size()) {
            throw new BusinessException("5日枪械礼包id" + packageTableValue.getId() + "rewardGunIdArray 和 rewardGunCountArray 长度不一致");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunIdArray(), packageTableValue.getRewardGunCountArray());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        userDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunIdArray(), packageTableValue.getRewardGunCountArray(), newUnlockGunIds, gameVersion);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        userDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion);
        userDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap);

        // TODO: 2023/1/2 同上面方法一样，需要核实
        final PlayerFifthDayGunGiftPackageData playerFifthDayGunGiftPackageData = targetPackageData;
        List<PlayerFifthDayGunGiftPackageData> collect = availableFifthDayGunGiftPackageData.stream().filter(value -> value != playerFifthDayGunGiftPackageData).collect(Collectors.toList());
        userData.setAvailableFifthDayGunGiftPackageData(collect);

        log.info("购买5日枪械礼包完成， " + chestOpenResult.toString());

        IAPPurchaseReward result = new IAPPurchaseReward();
        result.setChestOpenResult(chestOpenResult);
        result.setPrice(groupTableValue.getPrice());

        return result;
    }


    @Override
    public IAPPurchaseReward chapterGunPackageRewardDelivery(String uuid, int packageId, String gameVersion) {
        log.info("章节枪械礼包：" + packageId);
        UserData userData = GameEnvironment.userDataMap.get(uuid);

        List<ChapterBonusPackageData> chapterBonusPackagesData = userData.getChapterBonusPackagesData();

        if (chapterBonusPackagesData == null || chapterBonusPackagesData.size() == 0) {
            throw new BusinessException("玩家" + userData.getUuid() + "购买章节礼包,礼包数据为空");
        }

        ChapterBonusPackageData targetPackageData = null;

        for (ChapterBonusPackageData packageData : chapterBonusPackagesData) {

            if (packageData.getChapterId().equals(packageId)) {
                targetPackageData = packageData;
                break;
            }
        }

        if (targetPackageData == null) {
            throw new BusinessException("玩家" + uuid + "购买章节礼包,无法找到礼包数据" + packageId);
        }

        Map<String, ChapterGunGiftPackageTableValue> chapterGunGiftPackageTable = GameEnvironment.chapterGunGiftPackageTableMap.get(gameVersion);

        ChapterGunGiftPackageTableValue packageTableValue = chapterGunGiftPackageTable.get(String.valueOf(targetPackageData.getChapterId()));

        ChestOpenResult chestOpenResult = new ChestOpenResult();
        chestOpenResult.setChestData(new ChestData(NanoIdUtils.randomNanoId(30),
                packageTableValue.getRewardChestType(),
                packageTableValue.getRewardChestLevel(),
                TimeUtils.getUnixTimeSecond()));
        chestOpenResult.setCoin(0);
        chestOpenResult.setDiamond(0);

        //获得金币
        if (packageTableValue.getRewardCoinCount() > 0) {
            userData.setCoin(userData.getCoin() + packageTableValue.getRewardCoinCount());
            chestOpenResult.setCoin(chestOpenResult.getCoin() + packageTableValue.getRewardCoinCount());
        }
        //获得钻石
        if (packageTableValue.getRewardDiamondCount() > 0) {
            userData.setDiamond(userData.getDiamond() + packageTableValue.getRewardDiamondCount());
            chestOpenResult.setDiamond(chestOpenResult.getDiamond() + packageTableValue.getRewardDiamondCount());
        }
        //获得子弹
        if (Objects.nonNull(packageTableValue.getRewardBulletIdArray()) && packageTableValue.getRewardBulletIdArray().size() > 0) {
            if (packageTableValue.getRewardBulletIdArray().size() != packageTableValue.getRewardBulletCountArray().size()) {
                throw new BusinessException("章节枪械礼包id" + packageTableValue.getId() + "rewardBulletIdArray 和 rewardBulletCountArray 长度不一致");
            }

            userDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletIdArray(),
                    packageTableValue.getRewardBulletCountArray());
            userDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletIdArray(),
                    packageTableValue.getRewardBulletCountArray());
        }

        //枪械奖励是直接填表
        if (packageTableValue.getRewardGunIdArray().size() != packageTableValue.getRewardGunCountArray().size()) {
            throw new BusinessException("章节枪械礼包id" + packageTableValue.getId() + "rewardGunIdArray 和 rewardGunCountArray 长度不一致");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunIdArray(), packageTableValue.getRewardGunCountArray());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        userDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunIdArray(), packageTableValue.getRewardGunCountArray(), newUnlockGunIds, gameVersion);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        userDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion);
        userDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap);

        List<ChapterBonusPackageData> bonusPackagesData = userData.getChapterBonusPackagesData();
        ChapterBonusPackageData finalTargetPackageData = targetPackageData;
        List<ChapterBonusPackageData> collect = bonusPackagesData.stream().filter(value -> value != finalTargetPackageData).collect(Collectors.toList());
        userData.setChapterBonusPackagesData(collect);

        log.info("购买章节枪械礼包完成， " + chestOpenResult);

        IAPPurchaseReward result = new IAPPurchaseReward();
        result.setChestOpenResult(chestOpenResult);
        result.setPrice(packageTableValue.getPrice());

        return result;
    }

    @Override
    public IAPPurchaseReward gunPackageRewardDelivery(String uuid, int packageId, String gameVersion) {
        log.info("枪械礼包：" + packageId);
        UserData userData = GameEnvironment.userDataMap.get(uuid);

        Map<String, GunGiftPackageTableValue> gunGiftPackageTable = GameEnvironment.gunGiftPackageTableMap.get(gameVersion);

        GunGiftPackageTableValue packageTableValue = gunGiftPackageTable.get(String.valueOf(packageId));

        List<PlayerGunGiftPackageData> availableGunGiftPackageData = userData.getAvailableGunGiftPackageData();
        PlayerGunGiftPackageData targetPackageData = null;
        for (int i = 0; i < availableGunGiftPackageData.size(); i++) {
            PlayerGunGiftPackageData packageData = availableGunGiftPackageData.get(i);
            if (packageData.getPackageId() == packageId) {
                targetPackageData = packageData;
                break;
            }
        }
        if (Objects.isNull(targetPackageData)) {
            throw new BusinessException("玩家枪械礼包数据： " + Strings.join(availableGunGiftPackageData, ',') + ", 没有id为  " + packageId + "的礼包");
        }

        Map<String, GunGiftPackageGroupTableValue> gunGiftPackageGroupTable = GameEnvironment.gunGiftPackageGroupTableMap.get(gameVersion);
        GunGiftPackageGroupTableValue groupTableValue = gunGiftPackageGroupTable.get(String.valueOf(targetPackageData.getGroupId()));

        ChestOpenResult chestOpenResult = new ChestOpenResult();
        chestOpenResult.setChestData(new ChestData(NanoIdUtils.randomNanoId(30),
                packageTableValue.getRewardChestType(),
                packageTableValue.getRewardChestLevel(),
                TimeUtils.getUnixTimeSecond()));
        chestOpenResult.setCoin(0);
        chestOpenResult.setDiamond(0);

        //获得金币
        if (packageTableValue.getRewardCoinCount() > 0) {
            userData.setCoin(userData.getCoin() + packageTableValue.getRewardCoinCount());
            chestOpenResult.setCoin(chestOpenResult.getCoin() + packageTableValue.getRewardCoinCount());
        }
        //获得钻石
        if (packageTableValue.getRewardDiamondCount() > 0) {
            userData.setDiamond(userData.getDiamond() + packageTableValue.getRewardDiamondCount());
            chestOpenResult.setDiamond(chestOpenResult.getDiamond() + packageTableValue.getRewardDiamondCount());
        }
        //获得子弹
        if (Objects.nonNull(packageTableValue.getRewardBulletIdArray()) && packageTableValue.getRewardBulletIdArray().size() > 0) {
            if (packageTableValue.getRewardBulletIdArray().size() != packageTableValue.getRewardBulletCountArray().size()) {
                throw new BusinessException("枪械礼包id" + packageTableValue.getId() + "rewardBulletIdArray 和 rewardBulletCountArray 长度不一致");
            }

            userDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletIdArray(),
                    packageTableValue.getRewardBulletCountArray());
            userDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletIdArray(),
                    packageTableValue.getRewardBulletCountArray());
        }

        //枪械奖励是直接填表
        if (packageTableValue.getRewardGunIdArray().size() != packageTableValue.getRewardGunCountArray().size()) {
            throw new BusinessException("枪械礼包id" + packageTableValue.getId() + "rewardGunIdArray 和 rewardGunCountArray 长度不一致");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunIdArray(), packageTableValue.getRewardGunCountArray());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        userDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunIdArray(), packageTableValue.getRewardGunCountArray(), newUnlockGunIds, gameVersion);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        userDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion);
        userDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap);

        // TODO: 2023/1/2 同上面方法一样，需要核实
        final PlayerGunGiftPackageData playerGunGiftPackageData = targetPackageData;
        List<PlayerGunGiftPackageData> collect = availableGunGiftPackageData.stream().filter(value -> value != playerGunGiftPackageData).collect(Collectors.toList());
        userData.setAvailableGunGiftPackageData(collect);

        log.info("购买枪械礼包完成， " + chestOpenResult.toString());

        IAPPurchaseReward result = new IAPPurchaseReward();
        result.setChestOpenResult(chestOpenResult);
        result.setPrice(groupTableValue.getPrice());

        return result;
    }

    @Override
    public double getGiftPackagePopUpPriceRecommendPrice(UserData userData) {

        Map<String, Integer> iapProductPurchasedCountMap = userData.getIapProductPurchasedCountMap();
        //是否购买过第三章礼包
        boolean purchasedChapter3GiftPackage = iapProductPurchasedCountMap.get("hs_tour3valuebundle") != null;
        //是否购买过beast 活动礼包
        boolean purchasedBeastGiftPackage = false;
        //活动礼包老的记录中，只记录了hs_supersalebundle名称，新的规则下，会带有对应前缀和id后缀
        if (!purchasedBeastGiftPackage) {
            List<String> beastGiftPackageProductName = Lists.newArrayList();
            beastGiftPackageProductName.add(IAPProductPrefix.promotionGiftPackage + "_hs_supersalebundle_1");
            beastGiftPackageProductName.add(IAPProductPrefix.promotionGiftPackage + "_hs_supersalebundle_2");
            beastGiftPackageProductName.add(IAPProductPrefix.promotionGiftPackage + "_hs_supersalebundle_3");
            beastGiftPackageProductName.add(IAPProductPrefix.promotionGiftPackage + "_hs_supersalebundle_4");
            beastGiftPackageProductName.add(IAPProductPrefix.promotionGiftPackage + "_hs_supersalebundle_5");
            for (String key : beastGiftPackageProductName) {
                purchasedBeastGiftPackage = iapProductPurchasedCountMap.get(key) != null;
                if (purchasedBeastGiftPackage) {
                    break;
                }
            }
        }
        //如果没有新规则下的记录，之前所有29.99的礼包都算是beast
        if (!purchasedBeastGiftPackage) {
            purchasedBeastGiftPackage = iapProductPurchasedCountMap.get("hs_supersalebundle") != null;
        }

        /**
         * 1.若玩家均未购买过第三章章节礼包和29.99 beast礼包，则推送$4.99
         * 2.若玩家均仅购买过第三章章节礼包，则推送$9.99
         * 3.若玩家均仅购买过29.99 beast礼包，则推送$29.99
         * 4.若玩家购买过第三章章节礼包和29.99 beast礼包，则推送$99.99
         */
        if (!purchasedChapter3GiftPackage && !purchasedBeastGiftPackage) {
            return 4.99D;
        } else if (purchasedChapter3GiftPackage && !purchasedBeastGiftPackage) {
            return 9.99D;
        } else if (purchasedBeastGiftPackage && !purchasedChapter3GiftPackage) {
            return 29.99D;
        } else {
            return 99.99D;
        }
    }

//    @Override
//    public String updateCompleteOrder(CompletedOrder newOrder) {
//
//        String key = "completedIapOrders"+":"+newOrder.getCustomOrderId();
//        boolean exist = RedisDBOperation.checkKeyExist(key);
//
//        if (!exist){
//            throw new BusinessException(newOrder.getCustomOrderId()+"订单不存在");
//        }
//
//        RedisDBOperation.insertCompleteOrder(newOrder.getCustomOrderId(),newOrder);
//
//        return "订单更新成功";
//    }

//    @Override
//    public String deleteCompleteOrder(String orderId) {
//
//        boolean exist = RedisDBOperation.checkKeyExist("completedIapOrders:" + orderId);
//
//        if (!exist){
//
//            throw new BusinessException(orderId+"订单不存在");
//        }
//        RedisDBOperation.deleteCompleteOrder(orderId);
//
//        return "订单删除成功";
//
//    }

//    @Override
//    public List<CompletedOrder> getCompletedOrder(OrderDTO dto) {
//
//        Set<String> set = RedisDBOperation.scan("completedIapOrders");
//
//        //数据库中所有的完成订单
//        List<CompletedOrder> completedOrderList = new ArrayList<>();
//
//        //处理后的订单
//        List<CompletedOrder> resultList = new ArrayList<>();
//        for (String orderKey : set) {
//            CompletedOrder completedOrder = RedisDBOperation.selectCompleteOrder(orderKey);
//            completedOrderList.add(completedOrder);
//        }
//
//        for (CompletedOrder completedOrder : completedOrderList) {
//
//            //订单名称
//            if (!StringUtils.isEmpty(dto.getProductName())&&!completedOrder.getProductName().equals(dto.getProductName())){
//                continue;
//            }
//
////            //商品数量
////            if (!StringUtils.isEmpty(dto.getCount())&&!completedOrder.getCount().equals(dto.getCount())){
////                continue;
////            }
//
//            //订单ID
//            if (!StringUtils.isEmpty(dto.getCustomOrderId())&&!completedOrder.getCustomOrderId().equals(dto.getCustomOrderId())){
//                continue;
//            }
//
//            //玩家
//            if (!StringUtils.isEmpty(dto.getUserUid())&&!completedOrder.getPlayerUid().equals(dto.getUserUid())){
//                continue;
//            }
//
////            //订单类型
////            if (!StringUtils.isEmpty(dto.getOrderType())&&!completedOrder.getOrderOmitState().getType().equals(dto.getOrderType())){
////                continue;
////            }
//
////            //订单发起时间
////            if (!StringUtils.isEmpty(dto.getCreateSection())){
////
////                String[] createSection = dto.getCreateSection().split("-");
////                if (!(completedOrder.getStartDate()>=Long.parseLong(createSection[0]))&&!(completedOrder.getStartDate()<Long.parseLong(createSection[1]))){
////                    continue;
////                }
////            }
//
////            //订单结束时间
////            if (!StringUtils.isEmpty(dto.getEndSection())){
////
////                String[] endSection = dto.getEndSection().split("-");
////                if (!(completedOrder.getCompleteTime()>=Long.parseLong(endSection[0]))&&!(completedOrder.getCompleteTime()<Long.parseLong(endSection[1]))){
////                    continue;
////                }
////            }
//
//            resultList.add(completedOrder);
//
//        }
//        return resultList;
//    }

    public TopUpOrder checkIsOrderExist(String orderId) {

//        String key = "completedIapOrders:"+orderId;
        QueryWrapper<TopUpOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_number", orderId);
        TopUpOrder order = topUpOrderService.getOne(queryWrapper);
        if (order == null) {
            return null;
        }

        if (order.getOrderState().equals(OrderState.Completed.getType())) {
            return order;
        }

        return null;
    }
}
