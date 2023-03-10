package org.skynet.service.provider.hunting.obsolete.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.domain.*;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.NanoIdUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.config.IAPProductPrefix;
import org.skynet.service.provider.hunting.obsolete.config.VipConfig;
import org.skynet.service.provider.hunting.obsolete.config.VipV2Config;
import org.skynet.service.provider.hunting.obsolete.config.VipV3Config;
import org.skynet.service.provider.hunting.obsolete.dao.entity.TopUpOrder;
import org.skynet.service.provider.hunting.obsolete.dao.service.TopUpOrderService;
import org.skynet.service.provider.hunting.obsolete.enums.GunLibraryType;
import org.skynet.service.provider.hunting.obsolete.enums.GunQuality;
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
    private ObsoleteUserDataService obsoleteUserDataService;

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
        log.info("????????????????????? " + productName + ", count " + purchasedCount);
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

        log.info("?????????????????????" + pendingOrder);
    }

    @Override
    public PendingPurchaseOrder getPendingCustomOrder(String customOrderId) {

        return RedisDBOperation.selectPendingCustomOrder(customOrderId);
    }

    @Override
    public ReceiptValidateResult googlePlayReceiptValidate(IapReceiptValidateDTO receiptValidateDTO) {
        String receipt = receiptValidateDTO.getReceipt();
        //????????????98???????????????????????????json?????????????????????
        log.warn("??????????????????????????????service");
        JSONObject params = JSONObject.parseObject(receipt);
        if (params == null) {
            log.warn("???????????????????????????????????????null");
            throw new BusinessException("???????????????????????????????????????nul");
        } else {
            log.warn("?????????????????????null?????????????????????{}", JSONUtil.toJsonStr(params));
        }
//        String[] purchaseInfos = params.getString("Payload").split("\\|");
//        JSONObject googleParam = JSONObject.parseObject(purchaseInfos[3]);
        JSONObject googleParam = JSONObject.parseObject(params.getString("Payload"));
        if (googleParam == null) {
            log.warn("????????????????????????googleParam???null");
            throw new BusinessException("???????????????????????????????????????nul");
        } else {
            log.warn("googleParam????????????null???googleParam????????????{}", googleParam);
        }
        JSONObject jsonData = JSONObject.parseObject(googleParam.getString("json"));
        if (jsonData == null) {
            log.warn("????????????????????????jsonData???null");
            throw new BusinessException("???????????????????????????????????????nul");
        } else {
            log.warn("jsonData????????????null???jsonData????????????{}", jsonData);
        }
        String purchaseToken = jsonData.getString("purchaseToken");
        String productId = jsonData.getString("productId");
//        String gameUrl = params.getString("gameServer");
        String packageName = jsonData.getString("packageName");
//        String cpOrderId = purchaseInfos[2].split("_")[1];
        String orderId = jsonData.getString("orderId");
        //???????????????????????????
        TopUpOrder completedOrder = checkIsOrderExist(orderId);

        if (completedOrder != null) {
            log.info("??????????????????,?????????????????????????????????, order id:" + orderId);

            return new ReceiptValidateResult(
                    true,
                    false,
                    productId,
                    orderId,
                    null
            );
        }

        try {
            log.info("????????????????????????????????????");

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
            log.warn("?????????credential?????????{}", JSONUtil.toJsonStr(credential));
            //??????????????????????????????????????????????????????
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
//                    .setServiceAccountId(NettyHttpServerHandler.getAccountEmailListProperties().getProperty(packageName)) // ?????????serviceAccountEmail
//                    .setServiceAccountScopes(AndroidPublisherScopes.all())
//                    .setServiceAccountPrivateKey(privateKey).build();
//
//            AndroidPublisher publisher = new AndroidPublisher.Builder(transport, JacksonFactory.getDefaultInstance(), credential).build();
//            AndroidPublisher.Purchases.Products products = publisher.purchases().products();
//            AndroidPublisher.Purchases.Products.Get product = products.get(packageName, productId, purchaseToken);

            log.warn("????????????GOOGLE??????????????? API?????????????????????...???");
            ProductPurchase purchase = request.execute();
            log.warn(">>>purchase {}", purchase.toString());

            QueryWrapper<TopUpOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_number", receiptValidateDTO.getCustomOrderId());
            TopUpOrder topUpOrder = topUpOrderService.getOne(queryWrapper);
            if (topUpOrder == null) {
                throw new BusinessException("mysql??????????????????????????????");
            }
            topUpOrder.setPayMode("GooglePay");
            if (0 != purchase.getPurchaseState()) {
                log.warn("????????????,????????????????????????:{}", purchase.getPurchaseState());
                //??????????????????
                topUpOrder.setOrderState(OrderState.VerifyFailed.getType());
                throw new BusinessException("????????????????????????" + purchase.toString());
            } else {
                topUpOrder.setOrderState(OrderState.VerifySuccess.getType());
                log.warn("????????????????????????" + purchase.toString());
            }

            String resultOrderId = purchase.getOrderId();

            if (!orderId.equals(resultOrderId)) {
                log.warn("?????????????????? ??????????????????. user give" + orderId + "validate result: " + resultOrderId + ", raw" + JSONObject.toJSONString(purchase));
                topUpOrder.setOrderState(OrderState.VerifyFailed.getType());
                throw new BusinessException("?????????????????? ??????????????????");
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
            log.warn("????????????????????????===================================");
            e.printStackTrace();
            log.warn("===================================????????????????????????");
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
    public IAPPurchaseReward iapPurchaseContentDelivery(String uuid, String productName, String additionalParametersJSON, String gameVersion, float additionValue) {

        JSONObject additionalParameter = Objects.isNull(additionalParametersJSON) ? new JSONObject() : JSONObject.parseObject(additionalParametersJSON);
        UserData userData = GameEnvironment.userDataMap.get(uuid);

        boolean isPurchaseComplete = false;
        IAPPurchaseReward result = new IAPPurchaseReward();
        double productPrice = 0d;
        Map<String, ShopDiamondTableValue> shopDiamondTable = GameEnvironment.shopDiamondTableMap.get(gameVersion);

        //????????????
        Map<String, ShopCoinTableValue> shopCoinTable = GameEnvironment.shopCoinTableMap.get(gameVersion);
        Set<String> coinKeys = shopCoinTable.keySet();
        for (String key : coinKeys) {

            ShopCoinTableValue tableValue = shopCoinTable.get(key);
            if (tableValue.getProductId().equals(productName)) {

                //??????????????????????????????????????????
                boolean isFirstPurchase = getUserIapProductPurchasedCount(tableValue.getProductId(), uuid) == 0;

                int rewardCoin = tableValue.getCoinAmount();

                rewardCoin = isFirstPurchase ? rewardCoin * 2 : rewardCoin;
                long coin = userData.getCoin();
                coin += rewardCoin;
                userData.setCoin(coin);
                Long totalEarnedCoin = userData.getHistory().getTotalEarnedCoin();
                userData.getHistory().setTotalEarnedCoin(totalEarnedCoin + rewardCoin);
                increaseUserIapProductPurchaseCount(uuid, tableValue.getProductId());


                log.info("????????????,id" + tableValue.getId() + ", ??????????????????" + isFirstPurchase + ", reward coin" + rewardCoin);

                result.setCoin(rewardCoin);
                productPrice = tableValue.getPrice();
                isPurchaseComplete = true;
                break;
            }
        }

        //????????????
        if (!isPurchaseComplete) {

            Set<String> keySet = shopDiamondTable.keySet();
            for (String key : keySet) {

                ShopDiamondTableValue tableValue = shopDiamondTable.get(key);
                if (tableValue.getProductId().equals(productName)) {

                    //??????????????????????????????????????????
                    boolean isFirstPurchase = getUserIapProductPurchasedCount(tableValue.getProductId(), uuid) == 0;

                    int rewardDiamond = tableValue.getDiamondAmount();

                    rewardDiamond = isFirstPurchase ? rewardDiamond * 2 : rewardDiamond;

                    long diamond = userData.getDiamond() + rewardDiamond;
                    increaseUserIapProductPurchaseCount(uuid, tableValue.getProductId());

                    userData.setDiamond(diamond);
                    //????????????
                    Long totalEarnedDiamond = userData.getHistory().getTotalEarnedDiamond();
                    userData.getHistory().setTotalEarnedDiamond(totalEarnedDiamond + rewardDiamond);
                    log.info("???????????????id" + tableValue.getId() + isFirstPurchase + ", reward diamond" + rewardDiamond);

                    result.setDiamond(rewardDiamond);

                    productPrice = tableValue.getPrice();

                    isPurchaseComplete = true;

                    break;
                }
            }
        }

        if (!isPurchaseComplete) {

            //??????????????????
            PromotionEventPackageData promotionPackageData = packageDataService.findUserPromotionEventPackageDataByPackageId(userData, productName);
            if (promotionPackageData != null) {

                log.info("??????????????????,??????: " + promotionPackageData);
                ChestOpenResult chestOpenResult = purchasePromotionEventPackage(uuid, promotionPackageData, gameVersion, additionValue);
                result.setChestOpenResult(chestOpenResult);

                //??????update??????+1
                chestService.saveChestOpenResult(chestOpenResult, uuid, userData.getUpdateCount() + 1);
                increaseUserIapProductPurchaseCount(uuid, String.format("%s_%s_%s", IAPProductPrefix.promotionGiftPackage, productName, promotionPackageData.getPackageId()));

                isPurchaseComplete = true;
                productPrice = promotionPackageData.getPrice();
            }
//            ??????????????????V2
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
                            chestOpenResult = purchasePromotionEventPackageV2(userData, promotionEventPackageV2, purchaseKey, gameVersion, 0.0f);

                            result.setChestOpenResult(chestOpenResult);

                            //??????update??????+1
                            chestService.saveChestOpenResult(chestOpenResult, uuid, userData.getUpdateCount() + 1);
                            increaseUserIapProductPurchaseCount(uuid, String.format("%s_%s_%s", IAPProductPrefix.promotionGiftPackageV2, productName, promotionEventPackageV2.getId()));

                            isPurchaseComplete = true;
                            productPrice = promotionEventPackageGroupV2.getPrice();
                        } else {
                            String purchaseKey = promotionEventPackageGroupV2.getId() + "_" + promotionEventPackageGroupV2.getPackageTypes().get(0) + "_" + giftPackagesV2Datum.getPackageId();
                            Map<String, PromotionGunGiftPackageV2TableValue> gunGiftPackageV2TableValueMap = GameEnvironment.promotionGunGiftPackageV2TableMap.get(gameVersion);
                            PromotionGunGiftPackageV2TableValue gunGiftPackageV2 = gunGiftPackageV2TableValueMap.get(giftPackagesV2Datum.getPackageId().toString());
                            chestOpenResult = promotionGunPackageRewardDelivery(gunGiftPackageV2, userData, gameVersion, purchaseKey, additionValue);
                            result.setChestOpenResult(chestOpenResult);

                            //??????update??????+1
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
//                log.info("??????????????????,??????: "+promotionPackageData);
//                ChestOpenResult chestOpenResult = null;
//                if (promotionPackageData.getPackageType() == 1){
//                    chestOpenResult = purchasePromotionEventPackage(uuid, promotionPackageData,gameVersion);
//                }else {
//                    chestOpenResult = promotionGunPackageRewardDelivery(uuid, promotionPackageData, gameVersion);
//                }
//
//                result.setChestOpenResult(chestOpenResult);
//
//                //??????update??????+1
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
            //2,3,4??????????????????
            Set<String> bonusKeys = chapterBonusPackageTable.keySet();

            for (String key : bonusKeys) {
                ChapterBonusPackageTableValue tableValue = chapterBonusPackageTable.get(key);

                if (tableValue.getProductId().equals(productName)) {

                    log.info("??????????????????" + tableValue.getProductId() + ",id" + tableValue.getId());
                    // ChestOpenResult chestOpenResult = purchaseChapterBonusPackage(uuid, tableValue.getId(), gameVersion, additionValue);
                    ChestOpenResult chestOpenResult = purchaseChapterBonusPackage(uuid, tableValue.getId(), gameVersion, 0.0f);
                    result.setChestOpenResult(chestOpenResult);

                    //??????update??????+1
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
                //5-12??????????????????
                Set<String> chapterGunGiftPackageIdSet = chapterGunGiftPackageTable.keySet();

                for (String key : chapterGunGiftPackageIdSet) {
                    ChapterGunGiftPackageTableValue tableValue = chapterGunGiftPackageTable.get(key);
                    if (tableValue.getProductId().equals(productName)) {
                        log.info("??????????????????" + tableValue.getProductId() + ",??????id" + tableValue.getId());
                        IAPPurchaseReward iapPurchaseReward = chapterGunPackageRewardDelivery(uuid, tableValue.getId(), gameVersion, additionValue);
                        ChestOpenResult chestOpenResult = iapPurchaseReward.getChestOpenResult();
                        result.setChestOpenResult(chestOpenResult);

                        //??????update??????+1
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
            //??????vip/svip
            if (productName.equals(VipConfig.vipProductName) || productName.equals(VipConfig.svipProductName)) {
                double vipPrice = obsoleteUserDataService.purchaseVip(userData, productName, gameVersion);
                increaseUserIapProductPurchaseCount(uuid, productName);
                isPurchaseComplete = true;
                productPrice = vipPrice;
            }
        }

        if (!isPurchaseComplete) {
            //???????????????vip/svip
            if (productName.equals(VipV2Config.vipProductName) || productName.equals(VipV2Config.svipProductName)) {
                double vipPrice = obsoleteUserDataService.purchaseVipV2(userData, productName, gameVersion);
                increaseUserIapProductPurchaseCount(uuid, productName);
                isPurchaseComplete = true;
                productPrice = vipPrice;
            }
        }

        if (!isPurchaseComplete) {
            //???????????????vip/svip
            if (productName.equals(VipV3Config.vipProductName) || productName.equals(VipV3Config.svipProductName)) {
                double vipPrice = obsoleteUserDataService.purchaseVipV3(userData, productName, gameVersion);
                increaseUserIapProductPurchaseCount(uuid, productName);
                isPurchaseComplete = true;
                productPrice = vipPrice;
            }
        }


        if (!isPurchaseComplete) {
            //????????????
            int bulletGiftPackageId = additionalParameter.getIntValue("bulletGiftPackageId");
            if (bulletGiftPackageId > 0) {
                IAPPurchaseReward iapPurchaseReward = bulletPackageRewardDelivery(uuid, bulletGiftPackageId, gameVersion, additionValue);
                result.setChestOpenResult(iapPurchaseReward.getChestOpenResult());
                increaseUserIapProductPurchaseCount(uuid, String.format("%s_%s_%s", IAPProductPrefix.bulletGiftPackage, productName, bulletGiftPackageId));
                //??????update??????+1
                chestService.saveChestOpenResult(result.getChestOpenResult(), uuid, userData.getUpdateCount() + 1);
                isPurchaseComplete = true;
                productPrice = iapPurchaseReward.getPrice();
            }
        }

        if (!isPurchaseComplete) {
            //??????????????????
            int fifthDayGunGiftPackageId = additionalParameter.getIntValue("fifthDayGunGiftPackageId");
            if (fifthDayGunGiftPackageId > 0) {
                IAPPurchaseReward iapPurchaseReward = fifthDayGunGiftPackageRewardDelivery(uuid, fifthDayGunGiftPackageId, gameVersion, additionValue);
                result.setChestOpenResult(iapPurchaseReward.getChestOpenResult());
                //??????update??????+1
                chestService.saveChestOpenResult(result.getChestOpenResult(), uuid, userData.getUpdateCount() + 1);
                increaseUserIapProductPurchaseCount(uuid, String.format("%s_%s_%s", IAPProductPrefix.fifthDayGunGiftPackage, productName, fifthDayGunGiftPackageId));
                isPurchaseComplete = true;
                productPrice = iapPurchaseReward.getPrice();
            }
        }

        if (!isPurchaseComplete) {
            int gunGiftPackageId = additionalParameter.getIntValue("gunGiftPackageId");
            if (gunGiftPackageId > 0) {
                //????????????
                IAPPurchaseReward iapPurchaseReward = gunPackageRewardDelivery(uuid, gunGiftPackageId, gameVersion, additionValue);
                result.setChestOpenResult(iapPurchaseReward.getChestOpenResult());
                //??????update??????+1
                chestService.saveChestOpenResult(result.getChestOpenResult(), uuid, userData.getUpdateCount() + 1);
                increaseUserIapProductPurchaseCount(uuid, String.format("%s_%s_%s", IAPProductPrefix.gunGiftPackage, productName, gunGiftPackageId));
                isPurchaseComplete = true;
                productPrice = iapPurchaseReward.getPrice();
            }
        }

        if (!isPurchaseComplete) {
            throw new BusinessException("??????" + productName + "????????????????????????");
        }
        double price = userData.getHistory().getAccumulateMoneyPaid() + productPrice;
        userData.getHistory().setAccumulateMoneyPaid(price);
        int count = userData.getHistory().getMoneyPaidCount() + 1;
        userData.getHistory().setMoneyPaidCount(count);

        log.info("????????????????????????:" + userData.getHistory().getAccumulateMoneyPaid() + ",??????" + userData.getHistory().getMoneyPaidCount());

        return result;
    }

    private ChestOpenResult promotionGunPackageRewardDelivery(PromotionGunGiftPackageV2TableValue packageTableValue, UserData userData, String gameVersion, String purchaseKey, float additionValue) {
        List<String> eventPackagesV2Keys = userData.getServerOnly().getPurchasedPromotionEventPackagesV2Keys();

        if (eventPackagesV2Keys.contains(purchaseKey)) {
            throw new BusinessException("??????" + userData.getUuid() + "??????????????????,???????????????????????? package purchase key" + purchaseKey);
        }


        ChestOpenResult chestOpenResult = new ChestOpenResult();
        chestOpenResult.setChestData(new ChestData(NanoIdUtils.randomNanoId(30),
                packageTableValue.getRewardChestType(),
                packageTableValue.getRewardChestLevel(),
                TimeUtils.getUnixTimeSecond()));
        chestOpenResult.setCoin(0);
        chestOpenResult.setDiamond(0);

        //????????????
        if (packageTableValue.getRewardCoinCount() > 0) {
            userData.setCoin(userData.getCoin() + packageTableValue.getRewardCoinCount());
            chestOpenResult.setCoin(chestOpenResult.getCoin() + packageTableValue.getRewardCoinCount());
        }
        //????????????
        if (packageTableValue.getRewardDiamondCount() > 0) {
            userData.setDiamond(userData.getDiamond() + packageTableValue.getRewardDiamondCount());
            chestOpenResult.setDiamond(chestOpenResult.getDiamond() + packageTableValue.getRewardDiamondCount());
        }
        //????????????
        if (Objects.nonNull(packageTableValue.getRewardBulletId()) && packageTableValue.getRewardBulletId().size() > 0) {
            if (packageTableValue.getRewardBulletId().size() != packageTableValue.getRewardBulletCount().size()) {
                throw new BusinessException("??????????????????id" + packageTableValue.getId() + "rewardBulletIdArray ??? rewardBulletCountArray ???????????????");
            }

            obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
            obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
        }

        //???????????????????????????
        if (packageTableValue.getRewardGunId().size() != packageTableValue.getRewardGunCount().size()) {
            throw new BusinessException("??????????????????id" + packageTableValue.getId() + "rewardGunIdArray ??? rewardGunCountArray ???????????????");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), newUnlockGunIds, gameVersion, 0f);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        obsoleteUserDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion, 0f);

        if (additionValue > 0) {
            int playerHighestUnlockedChapterID = obsoleteUserDataService.getPlayerHighestUnlockedChapterID(userData);
            Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
            Map<Integer, Integer> swapRewardGunCountMap = new HashMap<>(rewardGunCountMap);
            for (Map.Entry<Integer, Integer> entry : swapRewardGunCountMap.entrySet()) {
                Integer gunId = entry.getKey();
                Integer gunCount = entry.getValue();
                // gunCount = (int) Math.ceil(gunCount * (1 + additionValue));

                GunTableValue gunTableValue = gunTable.get(gunId.toString());
                GunQuality quality = GunQuality.values()[gunTableValue.getQuality() - 1];

                Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
                int rewardCount = (int) Math.ceil(gunCount * additionValue);
                // switch (quality) {
                //     case White:
                //         //?????????????????????????????????
                //         throw new BusinessException("???????????????????????????????????? id " + gunId);
                //     case Blue:
                //         //????????????????????????common????????????random???????????????
                //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Common, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                //         break;
                //     case Orange:
                //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                //         break;
                //     case Red:
                //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Epic, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                //         break;
                // }
                gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Random, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0.0f);
                List<Integer> tempNewUnlockGunIds = com.google.common.collect.Lists.newArrayList();
                List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
                obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, tempNewUnlockGunIds, gameVersion);

                for (Integer value : tempNewUnlockGunIds) {
                    if (!newUnlockGunIds.contains(value)) {
                        newUnlockGunIds.add(value);
                    }
                }

                for (Map.Entry<Integer, Integer> rgentry : gunRewardMap.entrySet()) {
                    Integer integer = rewardGunCountMap.get(rgentry.getKey());
                    if (null == integer) {
                        integer = rgentry.getValue();
                    } else {
                        integer += rgentry.getValue();
                    }
                    rewardGunCountMap.put(rgentry.getKey(), integer);
                }
            }
        }
        obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, 0.0f);

        //?????????????????????
        eventPackagesV2Keys.add(purchaseKey);
        userData.getServerOnly().setPurchasedPromotionEventPackagesV2Keys(eventPackagesV2Keys);

        //??????????????????
        packageDataService.refreshPromotionEventPackageV2Now(userData, gameVersion);
        log.info("????????????????????????,package data:" + packageTableValue);

        return chestOpenResult;
    }

    private ChestOpenResult purchasePromotionEventPackageV2(UserData userData, PromotionGiftPackageV2TableValue promotionEventPackageV2, String purchaseKey, String gameVersion, float additionValue) {

        List<String> packagesV2Keys = userData.getServerOnly().getPurchasedPromotionEventPackagesV2Keys();
        if (packagesV2Keys.contains(purchaseKey)) {
            throw new BusinessException("?????????????????????????????????" + packagesV2Keys);
        }


        ChestOpenResult chestOpenResult = new ChestOpenResult(new ChestData(NanoIdUtils.randomNanoId(30), promotionEventPackageV2.getChestType(), promotionEventPackageV2.getPackageLevel(), TimeUtils.getUnixTimeSecond()), 0, 0, null, null, null);


        //??????
        if (promotionEventPackageV2.getDiamond() > 0) {

            long diamond = userData.getDiamond() + promotionEventPackageV2.getDiamond();
            userData.setDiamond(diamond);

            int chestDiamond = chestOpenResult.getDiamond() + promotionEventPackageV2.getDiamond();
            chestOpenResult.setDiamond(chestDiamond);
        }

        //??????
        if (promotionEventPackageV2.getRewardGunCounts() != null) {

            if (chestOpenResult.getNewUnlockedGunIDs() == null) {
                chestOpenResult.setNewUnlockedGunIDs(new ArrayList<>());
            }

            Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(promotionEventPackageV2.getRewardGunIDs(), promotionEventPackageV2.getRewardGunCounts());
            obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, promotionEventPackageV2.getRewardGunIDs(), promotionEventPackageV2.getRewardGunCounts(), chestOpenResult.getNewUnlockedGunIDs(), gameVersion, 0.0f);
            obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult,rewardGunCountMap, additionValue);
            obsoleteUserDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion, 0.0f);

            // if (additionValue > 0) {
            //     List<Integer> newUnlockedGunIDs = chestOpenResult.getNewUnlockedGunIDs();
            //     int playerHighestUnlockedChapterID = obsoleteUserDataService.getPlayerHighestUnlockedChapterID(userData);
            //     Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
            //     Map<Integer, Integer> swapRewardGunCountMap = new HashMap<>(rewardGunCountMap);
            //     for (Map.Entry<Integer, Integer> entry : swapRewardGunCountMap.entrySet()) {
            //         Integer gunId = entry.getKey();
            //         Integer gunCount = entry.getValue();
            //         // gunCount = (int) Math.ceil(gunCount * (1 + additionValue));
            //
            //         GunTableValue gunTableValue = gunTable.get(gunId.toString());
            //         GunQuality quality = GunQuality.values()[gunTableValue.getQuality() - 1];
            //
            //         Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
            //         int rewardCount = (int) Math.ceil(gunCount * additionValue);
            //         // switch (quality) {
            //         //     case White:
            //         //         //?????????????????????????????????
            //         //         throw new BusinessException("???????????????????????????????????? id " + gunId);
            //         //     case Blue:
            //         //         //????????????????????????common????????????random???????????????
            //         //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Common, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //         //         break;
            //         //     case Orange:
            //         //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //         //         break;
            //         //     case Red:
            //         //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Epic, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //         //         break;
            //         // }
            //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Random, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0.0f);
            //         List<Integer> tempNewUnlockGunIds = com.google.common.collect.Lists.newArrayList();
            //         List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
            //         obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, tempNewUnlockGunIds, gameVersion);
            //
            //         for (Integer value : tempNewUnlockGunIds) {
            //             if (!newUnlockedGunIDs.contains(value)) {
            //                 newUnlockedGunIDs.add(value);
            //             }
            //         }
            //
            //         for (Map.Entry<Integer, Integer> rgentry : gunRewardMap.entrySet()) {
            //             Integer integer = rewardGunCountMap.get(rgentry.getKey());
            //             if (null == integer) {
            //                 integer = rgentry.getValue();
            //             } else {
            //                 integer += rgentry.getValue();
            //             }
            //             rewardGunCountMap.put(rgentry.getKey(), integer);
            //         }
            //     }
            // }
            // obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, 0.0f);
        }

        //??????
        if (promotionEventPackageV2.getRewardBulletIDs() != null) {

            obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                    promotionEventPackageV2.getRewardBulletIDs(),
                    promotionEventPackageV2.getRewardBulletCount());
            obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    promotionEventPackageV2.getRewardBulletIDs(),
                    promotionEventPackageV2.getRewardBulletCount());
        }


        packagesV2Keys.add(purchaseKey);
        userData.getServerOnly().setPurchasedPromotionEventPackagesV2Keys(packagesV2Keys);
        //??????????????????
        packageDataService.refreshPromotionEventPackageV2Now(userData, gameVersion);
        log.info("????????????????????????,package data:" + promotionEventPackageV2);

        return chestOpenResult;
    }

    private ChestOpenResult promotionGunPackageRewardDelivery(String uuid, PromotionEventPackageData purchasePackageData, String gameVersion, float additionValue) {
        UserData userData = GameEnvironment.userDataMap.get(uuid);
        List<PromotionGiftPackageV2Data> promotionGiftPackagesV2Data = userData.getPromotionGiftPackagesV2Data();
        if (promotionGiftPackagesV2Data == null || promotionGiftPackagesV2Data.size() == 0) {
            throw new BusinessException("??????" + userData.getUuid() + "???????????????????????????????????????");
        }

        List<String> purchasedPromotionEventPackagesKeys = userData.getServerOnly().getPurchasedPromotionEventPackagesKeys();

        if (purchasedPromotionEventPackagesKeys.contains(purchasePackageData.getServer_only_purchaseKey())) {

            throw new BusinessException("??????" + userData.getUuid() + "??????????????????,???????????????????????? package purchase key" + purchasePackageData.getServer_only_purchaseKey());

        }

        Map<String, PromotionGunGiftPackageV2TableValue> promotionEventGunGiftPackageTable = GameEnvironment.promotionGunGiftPackageV2TableMap.get(gameVersion);
        //??????
        PromotionGunGiftPackageV2TableValue packageTableValue = promotionEventGunGiftPackageTable.get(String.valueOf(purchasePackageData.getPackageId()));


        ChestOpenResult chestOpenResult = new ChestOpenResult();
        chestOpenResult.setChestData(new ChestData(NanoIdUtils.randomNanoId(30),
                packageTableValue.getRewardChestType(),
                packageTableValue.getRewardChestLevel(),
                TimeUtils.getUnixTimeSecond()));
        chestOpenResult.setCoin(0);
        chestOpenResult.setDiamond(0);

        //????????????
        if (packageTableValue.getRewardCoinCount() > 0) {
            userData.setCoin(userData.getCoin() + packageTableValue.getRewardCoinCount());
            chestOpenResult.setCoin(chestOpenResult.getCoin() + packageTableValue.getRewardCoinCount());
        }
        //????????????
        if (packageTableValue.getRewardDiamondCount() > 0) {
            userData.setDiamond(userData.getDiamond() + packageTableValue.getRewardDiamondCount());
            chestOpenResult.setDiamond(chestOpenResult.getDiamond() + packageTableValue.getRewardDiamondCount());
        }
        //????????????
        if (Objects.nonNull(packageTableValue.getRewardBulletId()) && packageTableValue.getRewardBulletId().size() > 0) {
            if (packageTableValue.getRewardBulletId().size() != packageTableValue.getRewardBulletCount().size()) {
                throw new BusinessException("??????????????????id" + packageTableValue.getId() + "rewardBulletIdArray ??? rewardBulletCountArray ???????????????");
            }

            obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
            obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
        }

        //???????????????????????????
        if (packageTableValue.getRewardGunId().size() != packageTableValue.getRewardGunCount().size()) {
            throw new BusinessException("??????????????????id" + packageTableValue.getId() + "rewardGunIdArray ??? rewardGunCountArray ???????????????");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), newUnlockGunIds, gameVersion, additionValue);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        obsoleteUserDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion, additionValue);
        obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, additionValue);

        //?????????????????????
        purchasedPromotionEventPackagesKeys.add(purchasePackageData.getServer_only_purchaseKey());

        //??????????????????
        packageDataService.refreshPromotionEventPackageV2Now(userData, gameVersion);
        log.info("????????????????????????,package data:" + packageTableValue);

        return chestOpenResult;
    }


    @Override
    public ChestOpenResult purchasePromotionEventPackage(String uuid, PromotionEventPackageData purchasePackageData, String gameVersion, float additionValue) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);
        List<PromotionEventPackageData> promotionEventPackagesData = userData.getPromotionEventPackagesData();
        if (promotionEventPackagesData == null || promotionEventPackagesData.size() == 0) {
            throw new BusinessException("??????" + userData.getUuid() + "???????????????????????????????????????");
        }

        List<String> purchasedPromotionEventPackagesKeys = userData.getServerOnly().getPurchasedPromotionEventPackagesKeys();

        if (purchasedPromotionEventPackagesKeys.contains(purchasePackageData.getServer_only_purchaseKey())) {

            throw new BusinessException("??????" + userData.getUuid() + "??????????????????,???????????????????????? package purchase key" + purchasePackageData.getServer_only_purchaseKey());

        }
        ChestOpenResult chestOpenResult = new ChestOpenResult(purchasePackageData.getChestData(), 0, 0, null, null, null);

        Map<String, PromotionEventPackageTableValue> promotionEventPackageTable = GameEnvironment.promotionEventPackageTableMap.get(gameVersion);

        //??????
        PromotionEventPackageTableValue tableValue = promotionEventPackageTable.get(String.valueOf(purchasePackageData.getPackageId()));

        //??????
        if (tableValue.getDiamond() > 0) {

            long diamond = userData.getDiamond() + tableValue.getDiamond();
            userData.setDiamond(diamond);

            int chestDiamond = chestOpenResult.getDiamond() + tableValue.getDiamond();
            chestOpenResult.setDiamond(chestDiamond);
        }

        //??????
        if (tableValue.getRewardGunCounts() != null) {

            if (chestOpenResult.getNewUnlockedGunIDs() == null) {
                chestOpenResult.setNewUnlockedGunIDs(new ArrayList<>());
            }

            Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(tableValue.getRewardGunIDs(), tableValue.getRewardGunCounts());
            obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, tableValue.getRewardGunIDs(), tableValue.getRewardGunCounts(), chestOpenResult.getNewUnlockedGunIDs(), gameVersion, additionValue);
            // obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, CommonUtils.combineGunIdAndCountArrayToGunCountMap(tableValue.getRewardGunIDs(), tableValue.getRewardGunCounts()), additionValue);
            obsoleteUserDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, CommonUtils.combineGunIdAndCountArrayToGunCountMap(tableValue.getRewardGunIDs(), tableValue.getRewardGunCounts()), gameVersion, additionValue);
            if (additionValue > 0) {
                List<Integer> newUnlockedGunIDs = chestOpenResult.getNewUnlockedGunIDs();
                int playerHighestUnlockedChapterID = obsoleteUserDataService.getPlayerHighestUnlockedChapterID(userData);
                Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
                Map<Integer, Integer> swapRewardGunCountMap = new HashMap<>(rewardGunCountMap);
                for (Map.Entry<Integer, Integer> entry : swapRewardGunCountMap.entrySet()) {
                    Integer gunId = entry.getKey();
                    Integer gunCount = entry.getValue();
                    // gunCount = (int) Math.ceil(gunCount * (1 + additionValue));

                    GunTableValue gunTableValue = gunTable.get(gunId.toString());
                    GunQuality quality = GunQuality.values()[gunTableValue.getQuality() - 1];

                    Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
                    int rewardCount = (int) Math.ceil(gunCount * additionValue);
                    // switch (quality) {
                    //     case White:
                    //         //?????????????????????????????????
                    //         throw new BusinessException("???????????????????????????????????? id " + gunId);
                    //     case Blue:
                    //         //????????????????????????common????????????random???????????????
                    //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Common, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                    //         break;
                    //     case Orange:
                    //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                    //         break;
                    //     case Red:
                    //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Epic, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                    //         break;
                    // }
                    gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Random, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0.0f);
                    List<Integer> tempNewUnlockGunIds = com.google.common.collect.Lists.newArrayList();
                    List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
                    obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, tempNewUnlockGunIds, gameVersion);

                    for (Integer value : tempNewUnlockGunIds) {
                        if (!newUnlockedGunIDs.contains(value)) {
                            newUnlockedGunIDs.add(value);
                        }
                    }

                    for (Map.Entry<Integer, Integer> rgentry : gunRewardMap.entrySet()) {
                        Integer integer = rewardGunCountMap.get(rgentry.getKey());
                        if (null == integer) {
                            integer = rgentry.getValue();
                        } else {
                            integer += rgentry.getValue();
                        }
                        rewardGunCountMap.put(rgentry.getKey(), integer);
                    }
                }
            }
            obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, 0.0f);
        }

        //??????
        if (tableValue.getRewardBulletIDs() != null) {

            obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                    tableValue.getRewardBulletIDs(),
                    tableValue.getRewardBulletCount());
            obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    tableValue.getRewardBulletIDs(),
                    tableValue.getRewardBulletCount());
        }

//        populateSendToClientUserDataWithChestOpenResult(chestOpenResult);

        //?????????????????????
        purchasedPromotionEventPackagesKeys.add(purchasePackageData.getServer_only_purchaseKey());

        //??????????????????
        packageDataService.refreshPromotionEventPackageNow(userData, gameVersion);
        log.info("????????????????????????,package data:" + promotionEventPackagesData);

        return chestOpenResult;

    }

    @Override
    public ChestOpenResult purchaseChapterBonusPackage(String uuid, Integer packageId, String gameVersion, float additionValue) {

        UserData userData = GameEnvironment.userDataMap.get(uuid);

        List<ChapterBonusPackageData> chapterBonusPackagesData = userData.getChapterBonusPackagesData();

        if (chapterBonusPackagesData == null || chapterBonusPackagesData.size() == 0) {
            throw new BusinessException("??????" + userData.getUuid() + "??????????????????,??????????????????");
        }

        ChapterBonusPackageData targetPackageData = null;

        for (ChapterBonusPackageData packageData : chapterBonusPackagesData) {

            if (packageData.getChapterId().equals(packageId)) {
                targetPackageData = packageData;
                break;
            }
        }

        if (targetPackageData == null) {
            throw new BusinessException("??????" + uuid + "??????????????????,????????????????????????" + packageId);
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
        if (tableValue.getRewardGunIDs().size() > 0) {

            obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, tableValue.getRewardGunIDs(), tableValue.getRewardGunCounts(), chestOpenResult.getNewUnlockedGunIDs(), gameVersion, additionValue);

            obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, CommonUtils.combineGunIdAndCountArrayToGunCountMap(tableValue.getRewardGunIDs(), tableValue.getRewardGunCounts()), additionValue);
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

        log.info("??????Pending??????" + customOrderId + ",result" + pendingPurchaseProductNames);
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
        // TODO: 2023/1/3 ??????????????????
        QueryWrapper<TopUpOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_number", customOrderId);
        TopUpOrder topUpOrder = topUpOrderService.getOne(queryWrapper);
        topUpOrder.setOrderEndDate(LocalDateTime.now());
        topUpOrder.setOrderState(OrderState.Completed.getType());
        topUpOrder.setReceiptValidateResult(JSONObject.toJSONString(validateResult));
        topUpOrderService.updateById(topUpOrder);
//        RedisDBOperation.insertCompleteOrder(validateResult.getOrderId(),savaData);
        log.info("???????????????" + topUpOrder);
    }

    @Override
    public void archivePendingCustomOrder(String customOrderId, String platformOrderId) {

        PendingPurchaseOrder pendingPurchaseOrder = RedisDBOperation.selectPendingCustomOrder(customOrderId);
        pendingPurchaseOrder.setPlatformOrderId(platformOrderId);

        RedisDBOperation.insertArchiveCustomOrders(pendingPurchaseOrder.getCustomOrderId(), pendingPurchaseOrder);

        RedisDBOperation.deletePendingCustomOrder(customOrderId);

        log.info("????????????????????? " + pendingPurchaseOrder.toString());
    }

    @Override
    public IAPPurchaseReward bulletPackageRewardDelivery(String uuid, int bulletGiftPackageId, String gameVersion, float additionValue) {
        log.info("?????????????????????" + bulletGiftPackageId);
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
            throw new BusinessException("????????????????????????" + Strings.join(availableBulletGiftPackageData, ',') + "??????id??? " + bulletGiftPackageId + "?????????");
        }

        Map<String, BulletGiftPackageTableValue> bulletGiftPackageTable = GameEnvironment.bulletGiftPackageTableMap.get(gameVersion);
        BulletGiftPackageTableValue tableValue = bulletGiftPackageTable.get(String.valueOf(purchaseBulletGiftPackageData.getPackageId()));

        ChestData chestData = new ChestData(NanoIdUtils.randomNanoId(30),
                tableValue.getRewardChestType(),
                purchaseBulletGiftPackageData.getChestLevel(),
                TimeUtils.getUnixTimeSecond());

        ChestOpenResult chestOpenResult = chestService.openChest(userData, chestData, gameVersion, additionValue);
        if (tableValue.getRewardBulletId().size() != tableValue.getRewardBulletCount().size()) {
            throw new BusinessException("????????????id" + tableValue.getId() + "rewardBulletIdArray ??? rewardBulletCountArray ???????????????");
        }

        obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                tableValue.getRewardBulletId(),
                tableValue.getRewardBulletCount());
        obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                tableValue.getRewardBulletId(),
                tableValue.getRewardBulletCount());

        //????????????
        if (tableValue.getRewardDiamondCount() > 0) {
            userData.setDiamond(userData.getDiamond() + tableValue.getRewardDiamondCount());
            chestOpenResult.setDiamond(Objects.isNull(chestOpenResult.getDiamond()) ? 0 : chestOpenResult.getDiamond());
            chestOpenResult.setDiamond(chestOpenResult.getDiamond() + tableValue.getRewardDiamondCount());
        }

        // TODO: 2023/1/2 ???????????????????????????????????????????????????
        final PlayerBulletGiftPackageData playerBulletGiftPackageData = purchaseBulletGiftPackageData;
        List<PlayerBulletGiftPackageData> collect = availableBulletGiftPackageData.stream().filter(value -> value != playerBulletGiftPackageData).collect(Collectors.toList());
        userData.setAvailableBulletGiftPackageData(collect);

        IAPPurchaseReward result = new IAPPurchaseReward();
        result.setChestOpenResult(chestOpenResult);
        result.setPrice(tableValue.getPrice());

        return result;
    }

    @Override
    public IAPPurchaseReward fifthDayGunGiftPackageRewardDelivery(String uuid, int packageId, String gameVersion, float additionValue) {
        log.info("?????????????????????" + packageId);
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
            throw new BusinessException("????????????package id " + packageId + "?????????????????????????????? " + Strings.join(availableFifthDayGunGiftPackageData, ','));
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

        //????????????
        if (packageTableValue.getRewardCoinCount() > 0) {
            chestOpenResult.setCoin(chestOpenResult.getCoin() + packageTableValue.getRewardCoinCount());
            userData.setCoin(userData.getCoin() + packageTableValue.getRewardCoinCount());
        }
        //????????????
        if (packageTableValue.getRewardDiamondCount() > 0) {
            chestOpenResult.setDiamond(chestOpenResult.getDiamond() + packageTableValue.getRewardDiamondCount());
            userData.setDiamond(userData.getDiamond() + packageTableValue.getRewardDiamondCount());
        }
        //????????????
        if (Objects.nonNull(packageTableValue.getRewardBulletId()) && packageTableValue.getRewardBulletId().size() > 0) {
            if (packageTableValue.getRewardBulletId().size() != packageTableValue.getRewardBulletCount().size()) {
                throw new BusinessException("5???????????????id" + packageTableValue.getId() + "rewardBulletIdArray ??? rewardBulletCountArray ???????????????");
            }

            obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
            obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
        }

        //???????????????????????????
        if (packageTableValue.getRewardGunId().size() != packageTableValue.getRewardGunCount().size()) {
            throw new BusinessException("5???????????????id" + packageTableValue.getId() + "rewardGunIdArray ??? rewardGunCountArray ???????????????");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), newUnlockGunIds, gameVersion, additionValue);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        obsoleteUserDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion, additionValue);
        obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, additionValue);

        // TODO: 2023/1/2 ????????????????????????????????????
        final PlayerFifthDayGunGiftPackageData playerFifthDayGunGiftPackageData = targetPackageData;
        List<PlayerFifthDayGunGiftPackageData> collect = availableFifthDayGunGiftPackageData.stream().filter(value -> value != playerFifthDayGunGiftPackageData).collect(Collectors.toList());
        userData.setAvailableFifthDayGunGiftPackageData(collect);

        log.info("??????5???????????????????????? " + chestOpenResult.toString());

        IAPPurchaseReward result = new IAPPurchaseReward();
        result.setChestOpenResult(chestOpenResult);
        result.setPrice(groupTableValue.getPrice());

        return result;
    }


    @Override
    public IAPPurchaseReward chapterGunPackageRewardDelivery(String uuid, int packageId, String gameVersion, float additionValue) {
        log.info("?????????????????????" + packageId);
        UserData userData = GameEnvironment.userDataMap.get(uuid);

        List<ChapterBonusPackageData> chapterBonusPackagesData = userData.getChapterBonusPackagesData();

        if (chapterBonusPackagesData == null || chapterBonusPackagesData.size() == 0) {
            throw new BusinessException("??????" + userData.getUuid() + "??????????????????,??????????????????");
        }

        ChapterBonusPackageData targetPackageData = null;

        for (ChapterBonusPackageData packageData : chapterBonusPackagesData) {

            if (packageData.getChapterId().equals(packageId)) {
                targetPackageData = packageData;
                break;
            }
        }

        if (targetPackageData == null) {
            throw new BusinessException("??????" + uuid + "??????????????????,????????????????????????" + packageId);
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

        //????????????
        if (packageTableValue.getRewardCoinCount() > 0) {
            userData.setCoin(userData.getCoin() + packageTableValue.getRewardCoinCount());
            chestOpenResult.setCoin(chestOpenResult.getCoin() + packageTableValue.getRewardCoinCount());
        }
        //????????????
        if (packageTableValue.getRewardDiamondCount() > 0) {
            userData.setDiamond(userData.getDiamond() + packageTableValue.getRewardDiamondCount());
            chestOpenResult.setDiamond(chestOpenResult.getDiamond() + packageTableValue.getRewardDiamondCount());
        }
        //????????????
        if (Objects.nonNull(packageTableValue.getRewardBulletId()) && packageTableValue.getRewardBulletId().size() > 0) {
            if (packageTableValue.getRewardBulletId().size() != packageTableValue.getRewardBulletCount().size()) {
                throw new BusinessException("??????????????????id" + packageTableValue.getId() + "rewardBulletIdArray ??? rewardBulletCountArray ???????????????");
            }

            obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
            obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
        }

        //???????????????????????????
        if (packageTableValue.getRewardGunId().size() != packageTableValue.getRewardGunCount().size()) {
            throw new BusinessException("??????????????????id" + packageTableValue.getId() + "rewardGunIdArray ??? rewardGunCountArray ???????????????");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), newUnlockGunIds, gameVersion, additionValue);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        obsoleteUserDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion, additionValue);
        if (additionValue > 0) {
            int playerHighestUnlockedChapterID = obsoleteUserDataService.getPlayerHighestUnlockedChapterID(userData);
            Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
            Map<Integer, Integer> swapRewardGunCountMap = new HashMap<>(rewardGunCountMap);
            for (Map.Entry<Integer, Integer> entry : swapRewardGunCountMap.entrySet()) {
                Integer gunId = entry.getKey();
                Integer gunCount = entry.getValue();
                // gunCount = (int) Math.ceil(gunCount * (1 + additionValue));

                GunTableValue gunTableValue = gunTable.get(gunId.toString());
                GunQuality quality = GunQuality.values()[gunTableValue.getQuality() - 1];

                Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
                int rewardCount = (int) Math.ceil(gunCount * additionValue);
                // switch (quality) {
                //     case White:
                //         //?????????????????????????????????
                //         throw new BusinessException("???????????????????????????????????? id " + gunId);
                //     case Blue:
                //         //????????????????????????common????????????random???????????????
                //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Common, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                //         break;
                //     case Orange:
                //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                //         break;
                //     case Red:
                //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Epic, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                //         break;
                // }
                gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Random, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0.0f);
                List<Integer> tempNewUnlockGunIds = com.google.common.collect.Lists.newArrayList();
                List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
                obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, tempNewUnlockGunIds, gameVersion);

                for (Integer value : tempNewUnlockGunIds) {
                    if (!newUnlockGunIds.contains(value)) {
                        newUnlockGunIds.add(value);
                    }
                }

                for (Map.Entry<Integer, Integer> rgentry : gunRewardMap.entrySet()) {
                    Integer integer = rewardGunCountMap.get(rgentry.getKey());
                    if (null == integer) {
                        integer = rgentry.getValue();
                    } else {
                        integer += rgentry.getValue();
                    }
                    rewardGunCountMap.put(rgentry.getKey(), integer);
                }
            }
        }
        obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, additionValue);

        List<ChapterBonusPackageData> bonusPackagesData = userData.getChapterBonusPackagesData();
        ChapterBonusPackageData finalTargetPackageData = targetPackageData;
        List<ChapterBonusPackageData> collect = bonusPackagesData.stream().filter(value -> value != finalTargetPackageData).collect(Collectors.toList());
        userData.setChapterBonusPackagesData(collect);

        log.info("????????????????????????????????? " + chestOpenResult);

        IAPPurchaseReward result = new IAPPurchaseReward();
        result.setChestOpenResult(chestOpenResult);
        result.setPrice(packageTableValue.getPrice());

        return result;
    }

    @Override
    public IAPPurchaseReward gunPackageRewardDelivery(String uuid, int packageId, String gameVersion, float additionValue) {
        log.info("???????????????" + packageId);
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
            throw new BusinessException("??????????????????????????? " + Strings.join(availableGunGiftPackageData, ',') + ", ??????id???  " + packageId + "?????????");
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

        //????????????
        if (packageTableValue.getRewardCoinCount() > 0) {
            userData.setCoin(userData.getCoin() + packageTableValue.getRewardCoinCount());
            chestOpenResult.setCoin(chestOpenResult.getCoin() + packageTableValue.getRewardCoinCount());
        }
        //????????????
        if (packageTableValue.getRewardDiamondCount() > 0) {
            userData.setDiamond(userData.getDiamond() + packageTableValue.getRewardDiamondCount());
            chestOpenResult.setDiamond(chestOpenResult.getDiamond() + packageTableValue.getRewardDiamondCount());
        }
        //????????????
        if (Objects.nonNull(packageTableValue.getRewardBulletId()) && packageTableValue.getRewardBulletId().size() > 0) {
            if (packageTableValue.getRewardBulletId().size() != packageTableValue.getRewardBulletCount().size()) {
                throw new BusinessException("????????????id" + packageTableValue.getId() + "rewardBulletIdArray ??? rewardBulletCountArray ???????????????");
            }

            obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
            obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
        }

        //???????????????????????????
        if (packageTableValue.getRewardGunId().size() != packageTableValue.getRewardGunCount().size()) {
            throw new BusinessException("????????????id" + packageTableValue.getId() + "rewardGunIdArray ??? rewardGunCountArray ???????????????");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), newUnlockGunIds, gameVersion, 0.0f);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        obsoleteUserDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion, 0.0f);
        if (additionValue > 0) {
            int playerHighestUnlockedChapterID = obsoleteUserDataService.getPlayerHighestUnlockedChapterID(userData);
            Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
            Map<Integer, Integer> swapRewardGunCountMap = new HashMap<>(rewardGunCountMap);
            for (Map.Entry<Integer, Integer> entry : swapRewardGunCountMap.entrySet()) {
                Integer gunId = entry.getKey();
                Integer gunCount = entry.getValue();
                // gunCount = (int) Math.ceil(gunCount * (1 + additionValue));

                GunTableValue gunTableValue = gunTable.get(gunId.toString());
                GunQuality quality = GunQuality.values()[gunTableValue.getQuality() - 1];

                Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
                int rewardCount = (int) Math.ceil(gunCount * additionValue);
                // switch (quality) {
                //     case White:
                //         //?????????????????????????????????
                //         throw new BusinessException("???????????????????????????????????? id " + gunId);
                //     case Blue:
                //         //????????????????????????common????????????random???????????????
                //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Common, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                //         break;
                //     case Orange:
                //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                //         break;
                //     case Red:
                //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Epic, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                //         break;
                // }
                gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Random, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0.0f);
                List<Integer> tempNewUnlockGunIds = com.google.common.collect.Lists.newArrayList();
                List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
                obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, tempNewUnlockGunIds, gameVersion);

                for (Integer value : tempNewUnlockGunIds) {
                    if (!newUnlockGunIds.contains(value)) {
                        newUnlockGunIds.add(value);
                    }
                }

                for (Map.Entry<Integer, Integer> rgentry : gunRewardMap.entrySet()) {
                    Integer integer = rewardGunCountMap.get(rgentry.getKey());
                    if (null == integer) {
                        integer = rgentry.getValue();
                    } else {
                        integer += rgentry.getValue();
                    }
                    rewardGunCountMap.put(rgentry.getKey(), integer);
                }
            }
        }
        obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, 0.0f);

        // TODO: 2023/1/2 ????????????????????????????????????
        final PlayerGunGiftPackageData playerGunGiftPackageData = targetPackageData;
        List<PlayerGunGiftPackageData> collect = availableGunGiftPackageData.stream().filter(value -> value != playerGunGiftPackageData).collect(Collectors.toList());
        userData.setAvailableGunGiftPackageData(collect);

        log.info("??????????????????????????? " + chestOpenResult.toString());

        IAPPurchaseReward result = new IAPPurchaseReward();
        result.setChestOpenResult(chestOpenResult);
        result.setPrice(groupTableValue.getPrice());

        return result;
    }

    @Override
    public double getGiftPackagePopUpPriceRecommendPrice(UserData userData) {

        Map<String, Integer> iapProductPurchasedCountMap = userData.getIapProductPurchasedCountMap();
        //??????????????????????????????
        boolean purchasedChapter3GiftPackage = iapProductPurchasedCountMap.get("hs_tour3valuebundle") != null;
        //???????????????beast ????????????
        boolean purchasedBeastGiftPackage = false;
        //??????????????????????????????????????????hs_supersalebundle???????????????????????????????????????????????????id??????
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
        //????????????????????????????????????????????????29.99??????????????????beast
        if (!purchasedBeastGiftPackage) {
            purchasedBeastGiftPackage = iapProductPurchasedCountMap.get("hs_supersalebundle") != null;
        }

        /**
         * 1.????????????????????????????????????????????????29.99 beast??????????????????$4.99
         * 2.?????????????????????????????????????????????????????????$9.99
         * 3.????????????????????????29.99 beast??????????????????$29.99
         * 4.??????????????????????????????????????????29.99 beast??????????????????$99.99
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
//            throw new BusinessException(newOrder.getCustomOrderId()+"???????????????");
//        }
//
//        RedisDBOperation.insertCompleteOrder(newOrder.getCustomOrderId(),newOrder);
//
//        return "??????????????????";
//    }

//    @Override
//    public String deleteCompleteOrder(String orderId) {
//
//        boolean exist = RedisDBOperation.checkKeyExist("completedIapOrders:" + orderId);
//
//        if (!exist){
//
//            throw new BusinessException(orderId+"???????????????");
//        }
//        RedisDBOperation.deleteCompleteOrder(orderId);
//
//        return "??????????????????";
//
//    }

//    @Override
//    public List<CompletedOrder> getCompletedOrder(OrderDTO dto) {
//
//        Set<String> set = RedisDBOperation.scan("completedIapOrders");
//
//        //?????????????????????????????????
//        List<CompletedOrder> completedOrderList = new ArrayList<>();
//
//        //??????????????????
//        List<CompletedOrder> resultList = new ArrayList<>();
//        for (String orderKey : set) {
//            CompletedOrder completedOrder = RedisDBOperation.selectCompleteOrder(orderKey);
//            completedOrderList.add(completedOrder);
//        }
//
//        for (CompletedOrder completedOrder : completedOrderList) {
//
//            //????????????
//            if (!StringUtils.isEmpty(dto.getProductName())&&!completedOrder.getProductName().equals(dto.getProductName())){
//                continue;
//            }
//
////            //????????????
////            if (!StringUtils.isEmpty(dto.getCount())&&!completedOrder.getCount().equals(dto.getCount())){
////                continue;
////            }
//
//            //??????ID
//            if (!StringUtils.isEmpty(dto.getCustomOrderId())&&!completedOrder.getCustomOrderId().equals(dto.getCustomOrderId())){
//                continue;
//            }
//
//            //??????
//            if (!StringUtils.isEmpty(dto.getUserUid())&&!completedOrder.getPlayerUid().equals(dto.getUserUid())){
//                continue;
//            }
//
////            //????????????
////            if (!StringUtils.isEmpty(dto.getOrderType())&&!completedOrder.getOrderOmitState().getType().equals(dto.getOrderType())){
////                continue;
////            }
//
////            //??????????????????
////            if (!StringUtils.isEmpty(dto.getCreateSection())){
////
////                String[] createSection = dto.getCreateSection().split("-");
////                if (!(completedOrder.getStartDate()>=Long.parseLong(createSection[0]))&&!(completedOrder.getStartDate()<Long.parseLong(createSection[1]))){
////                    continue;
////                }
////            }
//
////            //??????????????????
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
