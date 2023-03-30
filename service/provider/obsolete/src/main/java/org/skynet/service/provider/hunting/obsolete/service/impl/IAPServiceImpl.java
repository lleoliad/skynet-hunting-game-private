package org.skynet.service.provider.hunting.obsolete.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Lists;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.skynet.components.hunting.game.data.ChestOpenResult;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.domain.*;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.NanoIdUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.config.*;
import org.skynet.service.provider.hunting.obsolete.dao.entity.TopUpOrder;
import org.skynet.service.provider.hunting.obsolete.dao.service.TopUpOrderService;
import org.skynet.service.provider.hunting.obsolete.enums.GunLibraryType;
import org.skynet.service.provider.hunting.obsolete.enums.OmitState;
import org.skynet.service.provider.hunting.obsolete.enums.OrderState;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.IapReceiptValidateDTO;
import org.skynet.components.hunting.game.data.GunReward;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.IAPPurchaseReward;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.ReceiptValidateResult;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.*;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
import org.skynet.service.provider.hunting.obsolete.service.IAPService;
import org.skynet.service.provider.hunting.obsolete.service.ObsoleteUserDataService;
import org.skynet.service.provider.hunting.obsolete.service.PromotionEventPackageDataService;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
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
//            if (topUpOrder == null){
//                throw new BusinessException("mysql数据库中不存在的订单");
//            }
            if (0 != purchase.getPurchaseState()) {
                log.warn("充值失败,谷歌返回订单状态:{}", purchase.getPurchaseState());
                //订单校验失败
                if (topUpOrder != null) {
                    topUpOrder.setPayMode("GooglePay");
                    topUpOrder.setOrderState(OrderState.VerifyFailed.getType());
                    topUpOrderService.updateById(topUpOrder);
                }
                throw new BusinessException("验证内购订单失败" + purchase.toString());
            } else {
                if (topUpOrder == null) {
                    //避免重复插入调单数据
                    QueryWrapper<TopUpOrder> queryWrapper2 = new QueryWrapper<>();
                    queryWrapper2.eq("platform_order_number", purchase.getOrderId());
                    TopUpOrder topUpOrder2 = topUpOrderService.getOne(queryWrapper2);
                    if (topUpOrder2 == null) {
                        String customOrderId = NanoIdUtils.randomNanoId(30);
                        topUpOrder = new TopUpOrder();
                        topUpOrder.setOrderNumber(customOrderId);
                        topUpOrder.setPlatformOrderNumber(purchase.getOrderId());
                        topUpOrder.setProductName(purchase.getProductId());
                        topUpOrder.setUserInfo(receiptValidateDTO.getUserUid());
                        topUpOrder.setOrderState(OrderState.Place.getType());
                        topUpOrder.setOrderDate(LocalDateTime.now());
                        topUpOrder.setGoodCount(1);
                        topUpOrder.setOrderType(0);
                        topUpOrder.setOrderOmitState(OmitState.Omit.getType());
                        topUpOrder.setPayMode("GooglePay");
                        topUpOrderService.save(topUpOrder);
                    }
                    throw new BusinessException("mysql数据库中不存在的订单");
                }
                topUpOrder.setPayMode("GooglePay");
                topUpOrder.setPlatformOrderNumber(purchase.getOrderId());
                topUpOrder.setOrderState(OrderState.VerifySuccess.getType());
                log.warn("验证内购订单完成" + purchase.toString());
                topUpOrderService.updateById(topUpOrder);
            }

            String resultOrderId = purchase.getOrderId();

            if (!orderId.equals(resultOrderId)) {
                log.warn("验证内购订单 订单号不一致. user give" + orderId + "validate result: " + resultOrderId + ", raw" + JSONObject.toJSONString(purchase));
                topUpOrder.setOrderState(OrderState.VerifyFailed.getType());
                topUpOrderService.updateById(topUpOrder);
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

    public static void main(String[] args) {
        String payload = "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";
        String sandboxUrl = "https://sandbox.itunes.apple.com/verifyReceipt";
        JSONObject data = new JSONObject();
        data.put("receipt-data", payload);
        String result = HttpUtil.post(sandboxUrl, data.toJSONString());
        JSONObject jsondata = JSONObject.parseObject(result);
        log.warn("ios returnStr:{}", jsondata.toString());
    }

    @Override
    public ReceiptValidateResult appStoreReceiptValidate(IapReceiptValidateDTO receiptValidateDTO) {
        String receipt = receiptValidateDTO.getReceipt();
        //这是按照H5服务器端写的
        log.warn("已经进入验证内购订单service");

        JSONObject params = JSONObject.parseObject(receipt);
        if (params == null) {
            log.warn("验证内购订单时，传入的参数null");
            throw new BusinessException("验证内购订单时，传入的参数nul");
        } else {
            log.warn("传入的参数不是null，传入的参数为{}", JSONUtil.toJsonStr(params));
        }
        String payload = params.getString("Payload");
        if (payload == null) {
            log.warn("验证内购订单时，appstoreParam为null");
            throw new BusinessException("验证内购订单时，传入的参数nul");
        } else {
            log.warn("appstoreParam参数不是null，appstoreParam的参数为{}", payload);
        }

        String orderId = params.getString("customOrderId");
        //在已完成订单中寻找
        TopUpOrder completedOrder = checkIsOrderExist(orderId);

        if (completedOrder != null) {
            log.info("验证内购订单,该订单号已经存在且完成, order id:" + orderId);

            return new ReceiptValidateResult(
                    true,
                    false,
                    "",
                    orderId,
                    null
            );
        }

        try {

            String sandboxUrl = "https://sandbox.itunes.apple.com/verifyReceipt";
            String productUrl = "https://buy.itunes.apple.com/verifyReceipt";

//            if(isSandbox) {
//                log.warn("[沙盒模式下]!!!!!充值开始!");
//            }

            JSONObject data = new JSONObject();
            data.put("receipt-data", payload);

            log.warn("【正在向AppStore API发请求，请稍后...】");
            String result = HttpUtil.post(productUrl, data.toJSONString());
            log.warn("ios returnStr:{}", result);
            JSONObject resultData = JSONObject.parseObject(result);

            String status = resultData.getString("status");
            if (status.equals("21007")) {
                log.warn("[沙盒模式下]!!!!!充值开始!");
                result = HttpUtil.post(sandboxUrl, data.toJSONString());
                log.warn("ios returnStr:{}", result);
                resultData = JSONObject.parseObject(result);
                status = resultData.getString("status");
            }

            if (!resultData.containsKey("receipt")) {
                log.warn("验证ios内购订单失败");
                throw new BusinessException("验证ios内购订单失败");
            }
            String bid = resultData.getJSONObject("receipt").getString("bundle_id");
            log.warn("验证ios bid：{}", bid);
            log.warn("验证ios AppStoreConfig：{}", String.join(",", AppStoreConfig.BUNDLE_IDS));
            if (!Arrays.asList(AppStoreConfig.BUNDLE_IDS).contains(bid)) {
                log.warn("验证ios内购订单失败，应用不合法");
                throw new BusinessException("验证ios内购订单失败，应用不合法");
            }

            String productId = resultData.getJSONObject("receipt").getJSONArray("in_app").getJSONObject(0).getString("product_id");
            String platformOrderId = resultData.getJSONObject("receipt").getJSONArray("in_app").getJSONObject(0).getString("original_transaction_id");

            {
                QueryWrapper<TopUpOrder> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("platform_order_number", platformOrderId);
                TopUpOrder topUpOrder = topUpOrderService.getOne(queryWrapper);
                if (Objects.nonNull(topUpOrder) && topUpOrder.getOrderState() == OrderState.Completed.getType()) {
                    log.warn("ios内购订单已发货");
                    return new ReceiptValidateResult(
                            true,
                            false,
                            productId,
                            orderId,
                            null
                    );
                }
            }

            QueryWrapper<TopUpOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_number", receiptValidateDTO.getCustomOrderId());
            TopUpOrder topUpOrder = topUpOrderService.getOne(queryWrapper);
            if (!status.equals("0")) {
                log.warn("充值失败,appstore返回订单状态:{}", status);
                //订单校验失败
                if (Objects.nonNull(topUpOrder)) {
                    topUpOrder.setOrderState(OrderState.VerifyFailed.getType());
                    topUpOrder.setPayMode("AppStore");
                    topUpOrderService.updateById(topUpOrder);
                }
                throw new BusinessException("验证内购订单失败");
            } else {

                if (topUpOrder == null) {
                    //避免重复插入调单数据
                    QueryWrapper<TopUpOrder> queryWrapper2 = new QueryWrapper<>();
                    queryWrapper2.eq("platform_order_number", platformOrderId);
                    TopUpOrder topUpOrder2 = topUpOrderService.getOne(queryWrapper2);
                    if (topUpOrder2 == null) {
                        String customOrderId = NanoIdUtils.randomNanoId(30);
                        topUpOrder = new TopUpOrder();
                        topUpOrder.setOrderNumber(customOrderId);
                        topUpOrder.setPlatformOrderNumber(platformOrderId);
                        topUpOrder.setProductName(productId);
                        topUpOrder.setUserInfo(receiptValidateDTO.getUserUid());
                        topUpOrder.setOrderState(OrderState.Place.getType());
                        topUpOrder.setOrderDate(LocalDateTime.now());
                        topUpOrder.setGoodCount(1);
                        topUpOrder.setOrderType(0);
                        topUpOrder.setOrderOmitState(OmitState.Omit.getType());
                        topUpOrder.setPayMode("AppStore");
                        topUpOrderService.save(topUpOrder);
                    }
                    throw new BusinessException("mysql数据库中不存在的订单");
                }

                topUpOrder.setPayMode("AppStore");
                topUpOrder.setPlatformOrderNumber(platformOrderId);
                topUpOrder.setOrderState(OrderState.VerifySuccess.getType());
                log.warn("验证内购订单完成" + resultData.toString());
            }

            topUpOrderService.updateById(topUpOrder);

            return new ReceiptValidateResult(
                    false,
                    true,
                    productId,
                    orderId,
                    result
            );
        } catch (Exception e) {
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
    public IAPPurchaseReward iapPurchaseContentDelivery(String uuid, String productName, String additionalParametersJSON, String gameVersion, float additionValue) {

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
                ChestOpenResult chestOpenResult = purchasePromotionEventPackage(uuid, promotionPackageData, gameVersion, additionValue);
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
                            chestOpenResult = purchasePromotionEventPackageV2(userData, promotionEventPackageV2, purchaseKey, gameVersion, 0.0f);

                            result.setChestOpenResult(chestOpenResult);

                            //这里update必须+1
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
                    // ChestOpenResult chestOpenResult = purchaseChapterBonusPackage(uuid, tableValue.getId(), gameVersion, additionValue);
                    ChestOpenResult chestOpenResult = purchaseChapterBonusPackage(uuid, tableValue.getId(), gameVersion, 0.0f);
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
                        IAPPurchaseReward iapPurchaseReward = chapterGunPackageRewardDelivery(uuid, tableValue.getId(), gameVersion, additionValue);
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
                double vipPrice = obsoleteUserDataService.purchaseVip(userData, productName, gameVersion);
                increaseUserIapProductPurchaseCount(uuid, productName);
                isPurchaseComplete = true;
                productPrice = vipPrice;
            }
        }

        if (!isPurchaseComplete) {
            //内购第二版vip/svip
            if (productName.equals(VipV2Config.vipProductName) || productName.equals(VipV2Config.svipProductName)) {
                double vipPrice = obsoleteUserDataService.purchaseVipV2(userData, productName, gameVersion);
                increaseUserIapProductPurchaseCount(uuid, productName);
                isPurchaseComplete = true;
                productPrice = vipPrice;
            }
        }

        if (!isPurchaseComplete) {
            //内购第三版vip/svip
            if (productName.equals(VipV3Config.vipProductName) || productName.equals(VipV3Config.svipProductName)) {
                double vipPrice = obsoleteUserDataService.purchaseVipV3(userData, productName, gameVersion);
                increaseUserIapProductPurchaseCount(uuid, productName);
                isPurchaseComplete = true;
                productPrice = vipPrice;
            }
        }


        if (!isPurchaseComplete) {
            //子弹礼包
            int bulletGiftPackageId = additionalParameter.getIntValue("bulletGiftPackageId");
            if (bulletGiftPackageId > 0) {
                IAPPurchaseReward iapPurchaseReward = bulletPackageRewardDelivery(uuid, bulletGiftPackageId, gameVersion, additionValue);
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
                IAPPurchaseReward iapPurchaseReward = fifthDayGunGiftPackageRewardDelivery(uuid, fifthDayGunGiftPackageId, gameVersion, additionValue);
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
                IAPPurchaseReward iapPurchaseReward = gunPackageRewardDelivery(uuid, gunGiftPackageId, gameVersion, additionValue);
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

    private ChestOpenResult promotionGunPackageRewardDelivery(PromotionGunGiftPackageV2TableValue packageTableValue, UserData userData, String gameVersion, String purchaseKey, float additionValue) {
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
        if (Objects.nonNull(packageTableValue.getRewardBulletId()) && packageTableValue.getRewardBulletId().size() > 0) {
            if (packageTableValue.getRewardBulletId().size() != packageTableValue.getRewardBulletCount().size()) {
                throw new BusinessException("活动枪械礼包id" + packageTableValue.getId() + "rewardBulletIdArray 和 rewardBulletCountArray 长度不一致");
            }

            obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
            obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
        }

        //枪械奖励是直接填表
        if (packageTableValue.getRewardGunId().size() != packageTableValue.getRewardGunCount().size()) {
            throw new BusinessException("活动枪械礼包id" + packageTableValue.getId() + "rewardGunIdArray 和 rewardGunCountArray 长度不一致");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), newUnlockGunIds, gameVersion, 0f);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        obsoleteUserDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion, 0f);

        if (additionValue > 0) {
            gunRewardAddition(userData, rewardGunCountMap, chestOpenResult.getNewUnlockedGunIDs(), packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), packageTableValue.getRewardGunLibraryTypes(), gameVersion, additionValue);
            // int playerHighestUnlockedChapterID = obsoleteUserDataService.getPlayerHighestUnlockedChapterID(userData);
            // Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
            // Map<Integer, Integer> swapRewardGunCountMap = new HashMap<>(rewardGunCountMap);
            // for (Map.Entry<Integer, Integer> entry : swapRewardGunCountMap.entrySet()) {
            //     Integer gunId = entry.getKey();
            //     Integer gunCount = entry.getValue();
            //     // gunCount = (int) Math.ceil(gunCount * (1 + additionValue));
            //
            //     GunTableValue gunTableValue = gunTable.get(gunId.toString());
            //     GunQuality quality = GunQuality.values()[gunTableValue.getQuality() - 1];
            //
            //     Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
            //     int rewardCount = (int) Math.ceil(gunCount * additionValue);
            //     // switch (quality) {
            //     //     case White:
            //     //         //不应该会给白色品质的枪
            //     //         throw new BusinessException("不应该会给白色品质的枪。 id " + gunId);
            //     //     case Blue:
            //     //         //所有蓝色品质都算common，也就是random库不会增加
            //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Common, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //     //         break;
            //     //     case Orange:
            //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //     //         break;
            //     //     case Red:
            //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Epic, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //     //         break;
            //     // }
            //     gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Random, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0.0f);
            //     List<Integer> tempNewUnlockGunIds = com.google.common.collect.Lists.newArrayList();
            //     List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
            //     obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, tempNewUnlockGunIds, gameVersion);
            //
            //     for (Integer value : tempNewUnlockGunIds) {
            //         if (!newUnlockGunIds.contains(value)) {
            //             newUnlockGunIds.add(value);
            //         }
            //     }
            //
            //     for (Map.Entry<Integer, Integer> rgentry : gunRewardMap.entrySet()) {
            //         Integer integer = rewardGunCountMap.get(rgentry.getKey());
            //         if (null == integer) {
            //             integer = rgentry.getValue();
            //         } else {
            //             integer += rgentry.getValue();
            //         }
            //         rewardGunCountMap.put(rgentry.getKey(), integer);
            //     }
            // }
        }
        obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, 0.0f);

        //记录购买的礼包
        eventPackagesV2Keys.add(purchaseKey);
        userData.getServerOnly().setPurchasedPromotionEventPackagesV2Keys(eventPackagesV2Keys);

        //刷新礼包数据
        packageDataService.refreshPromotionEventPackageV2Now(userData, gameVersion);
        log.info("购买活动礼包完成,package data:" + packageTableValue);

        return chestOpenResult;
    }

    private ChestOpenResult purchasePromotionEventPackageV2(UserData userData, PromotionGiftPackageV2TableValue promotionEventPackageV2, String purchaseKey, String gameVersion, float additionValue) {

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
        if (promotionEventPackageV2.getRewardGunCounts() != null) {

            if (chestOpenResult.getNewUnlockedGunIDs() == null) {
                chestOpenResult.setNewUnlockedGunIDs(new ArrayList<>());
            }

            Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(promotionEventPackageV2.getRewardGunIDs(), promotionEventPackageV2.getRewardGunCounts());
            obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, promotionEventPackageV2.getRewardGunIDs(), promotionEventPackageV2.getRewardGunCounts(), chestOpenResult.getNewUnlockedGunIDs(), gameVersion, 0.0f);
            obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, additionValue);
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
            //         //         //不应该会给白色品质的枪
            //         //         throw new BusinessException("不应该会给白色品质的枪。 id " + gunId);
            //         //     case Blue:
            //         //         //所有蓝色品质都算common，也就是random库不会增加
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

        //子弹
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
        //刷新礼包数据
        packageDataService.refreshPromotionEventPackageV2Now(userData, gameVersion);
        log.info("购买活动礼包完成,package data:" + promotionEventPackageV2);

        return chestOpenResult;
    }

    private ChestOpenResult promotionGunPackageRewardDelivery(String uuid, PromotionEventPackageData purchasePackageData, String gameVersion, float additionValue) {
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
        if (Objects.nonNull(packageTableValue.getRewardBulletId()) && packageTableValue.getRewardBulletId().size() > 0) {
            if (packageTableValue.getRewardBulletId().size() != packageTableValue.getRewardBulletCount().size()) {
                throw new BusinessException("活动枪械礼包id" + packageTableValue.getId() + "rewardBulletIdArray 和 rewardBulletCountArray 长度不一致");
            }

            obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
            obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
        }

        //枪械奖励是直接填表
        if (packageTableValue.getRewardGunId().size() != packageTableValue.getRewardGunCount().size()) {
            throw new BusinessException("活动枪械礼包id" + packageTableValue.getId() + "rewardGunIdArray 和 rewardGunCountArray 长度不一致");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), newUnlockGunIds, gameVersion, additionValue);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        obsoleteUserDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion, additionValue);
        obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, additionValue);

        //记录购买的礼包
        purchasedPromotionEventPackagesKeys.add(purchasePackageData.getServer_only_purchaseKey());

        //刷新礼包数据
        packageDataService.refreshPromotionEventPackageV2Now(userData, gameVersion);
        log.info("购买活动礼包完成,package data:" + packageTableValue);

        return chestOpenResult;
    }


    @Override
    public ChestOpenResult purchasePromotionEventPackage(String uuid, PromotionEventPackageData purchasePackageData, String gameVersion, float additionValue) {

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
        if (tableValue.getRewardGunCounts() != null) {

            if (chestOpenResult.getNewUnlockedGunIDs() == null) {
                chestOpenResult.setNewUnlockedGunIDs(new ArrayList<>());
            }

            Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(tableValue.getRewardGunIDs(), tableValue.getRewardGunCounts());
            obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, tableValue.getRewardGunIDs(), tableValue.getRewardGunCounts(), chestOpenResult.getNewUnlockedGunIDs(), gameVersion, 0.0f);
            // obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, CommonUtils.combineGunIdAndCountArrayToGunCountMap(tableValue.getRewardGunIDs(), tableValue.getRewardGunCounts()), additionValue);
            obsoleteUserDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, CommonUtils.combineGunIdAndCountArrayToGunCountMap(tableValue.getRewardGunIDs(), tableValue.getRewardGunCounts()), gameVersion, 0.0f);
            if (additionValue > 0) {
                gunRewardAddition(userData, rewardGunCountMap, chestOpenResult.getNewUnlockedGunIDs(), tableValue.getRewardGunIDs(), tableValue.getRewardGunCounts(), tableValue.getRewardGunLibraryTypes(), gameVersion, additionValue);
                // List<Integer> newUnlockedGunIDs = chestOpenResult.getNewUnlockedGunIDs();
                // int playerHighestUnlockedChapterID = obsoleteUserDataService.getPlayerHighestUnlockedChapterID(userData);
                // Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
                // Map<Integer, Integer> swapRewardGunCountMap = new HashMap<>(rewardGunCountMap);
                // for (Map.Entry<Integer, Integer> entry : swapRewardGunCountMap.entrySet()) {
                //     Integer gunId = entry.getKey();
                //     Integer gunCount = entry.getValue();
                //     // gunCount = (int) Math.ceil(gunCount * (1 + additionValue));
                //
                //     GunTableValue gunTableValue = gunTable.get(gunId.toString());
                //     GunQuality quality = GunQuality.values()[gunTableValue.getQuality() - 1];
                //
                //     Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
                //     int rewardCount = (int) Math.ceil(gunCount * additionValue);
                //     // switch (quality) {
                //     //     case White:
                //     //         //不应该会给白色品质的枪
                //     //         throw new BusinessException("不应该会给白色品质的枪。 id " + gunId);
                //     //     case Blue:
                //     //         //所有蓝色品质都算common，也就是random库不会增加
                //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Common, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                //     //         break;
                //     //     case Orange:
                //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                //     //         break;
                //     //     case Red:
                //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Epic, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                //     //         break;
                //     // }
                //     gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Random, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0.0f);
                //     List<Integer> tempNewUnlockGunIds = com.google.common.collect.Lists.newArrayList();
                //     List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
                //     obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, tempNewUnlockGunIds, gameVersion);
                //
                //     for (Integer value : tempNewUnlockGunIds) {
                //         if (!newUnlockedGunIDs.contains(value)) {
                //             newUnlockedGunIDs.add(value);
                //         }
                //     }
                //
                //     for (Map.Entry<Integer, Integer> rgentry : gunRewardMap.entrySet()) {
                //         Integer integer = rewardGunCountMap.get(rgentry.getKey());
                //         if (null == integer) {
                //             integer = rgentry.getValue();
                //         } else {
                //             integer += rgentry.getValue();
                //         }
                //         rewardGunCountMap.put(rgentry.getKey(), integer);
                //     }
                // }
            }
            obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, 0.0f);
        }

        //子弹
        if (tableValue.getRewardBulletIDs() != null) {

            obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                    tableValue.getRewardBulletIDs(),
                    tableValue.getRewardBulletCount());
            obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    tableValue.getRewardBulletIDs(),
                    tableValue.getRewardBulletCount());
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
    public ChestOpenResult purchaseChapterBonusPackage(String uuid, Integer packageId, String gameVersion, float additionValue) {

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
    public IAPPurchaseReward bulletPackageRewardDelivery(String uuid, int bulletGiftPackageId, String gameVersion, float additionValue) {
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

        ChestOpenResult chestOpenResult = chestService.openChest(userData, chestData, gameVersion, additionValue);
        if (tableValue.getRewardBulletId().size() != tableValue.getRewardBulletCount().size()) {
            throw new BusinessException("子弹礼包id" + tableValue.getId() + "rewardBulletIdArray 和 rewardBulletCountArray 长度不一致");
        }

        obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                tableValue.getRewardBulletId(),
                tableValue.getRewardBulletCount());
        obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                tableValue.getRewardBulletId(),
                tableValue.getRewardBulletCount());

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
    public IAPPurchaseReward fifthDayGunGiftPackageRewardDelivery(String uuid, int packageId, String gameVersion, float additionValue) {
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
        if (Objects.nonNull(packageTableValue.getRewardBulletId()) && packageTableValue.getRewardBulletId().size() > 0) {
            if (packageTableValue.getRewardBulletId().size() != packageTableValue.getRewardBulletCount().size()) {
                throw new BusinessException("5日枪械礼包id" + packageTableValue.getId() + "rewardBulletIdArray 和 rewardBulletCountArray 长度不一致");
            }

            obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
            obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
        }

        //枪械奖励是直接填表
        if (packageTableValue.getRewardGunId().size() != packageTableValue.getRewardGunCount().size()) {
            throw new BusinessException("5日枪械礼包id" + packageTableValue.getId() + "rewardGunIdArray 和 rewardGunCountArray 长度不一致");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), newUnlockGunIds, gameVersion, 0.0f);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        obsoleteUserDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion, 0.0f);
        if (additionValue > 0) {
            gunRewardAddition(userData, rewardGunCountMap, newUnlockGunIds, packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), packageTableValue.getRewardGunLibraryTypes(), gameVersion, additionValue);
        }
        obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, 0.0f);

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

    public class AdditionReward implements Serializable {
        public List<Integer> gunLibraryTypes = new ArrayList<>();
        public List<Integer> gunCounts = new ArrayList<>();
        public Integer totalGunCount = 0;
    }

    /**
     * 处理枪械宝箱加成
     */
    public void gunRewardAddition(UserData userData, Map<Integer, Integer> rewardGunCountMap, List<Integer> newUnlockGunIds, List<Integer> gunIds, List<Integer> gunCounts, List<Integer> rewardGunLibraryTypes, String gameVersion, float additionValue) {
        if (additionValue > 0) {
            int playerHighestUnlockedChapterID = obsoleteUserDataService.getPlayerHighestUnlockedChapterID(userData);
            Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
            Map<Integer, AdditionReward> gunQualityCount = new HashMap<>();
            for (int i = 0; i < gunIds.size(); i++) {
                Integer gunId = gunIds.get(i);
                Integer gunCount = gunCounts.get(i);

                GunTableValue gunTableValue = gunTable.get(gunId.toString());

                AdditionReward additionReward = gunQualityCount.get(gunTableValue.getQuality());
                if (null == additionReward) {
                    additionReward = new AdditionReward();
                    gunQualityCount.put(gunTableValue.getQuality(), additionReward);
                }

                additionReward.gunCounts.add(gunCount);
                additionReward.gunLibraryTypes.add(rewardGunLibraryTypes.get(i));

                additionReward.totalGunCount += gunCount;
            }

            for (Map.Entry<Integer, AdditionReward> entry : gunQualityCount.entrySet()) {
                AdditionReward additionReward = entry.getValue();
                int totalGunCount = (int) Math.floor(entry.getValue().totalGunCount * additionValue + 0.5);

                int limit = additionReward.gunLibraryTypes.size() - 1;
                for (int i = 0; i <= limit; i++) {
                    Integer gunCount = gunCounts.get(i);
                    Integer gunLibraryType = additionReward.gunLibraryTypes.get(i);
                    Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
                    int rewardCount = 0;
                    if (i == limit) {
                        rewardCount = totalGunCount;
                    } else {
                        rewardCount = (int) Math.floor(gunCount * additionValue + 0.5); //(int) Math.ceil(gunCount * additionValue);
                        totalGunCount -= rewardCount;
                    }
                    if (rewardCount < 1) {
                        continue;
                    }
                    // switch (quality) {
                    //     case White:
                    //         //不应该会给白色品质的枪
                    //         throw new BusinessException("不应该会给白色品质的枪。 id " + gunId);
                    //     case Blue:
                    //         //所有蓝色品质都算common，也就是random库不会增加
                    //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Common, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                    //         break;
                    //     case Orange:
                    //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                    //         break;
                    //     case Red:
                    //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Epic, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
                    //         break;
                    // }
                    gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.values()[gunLibraryType - 1], playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0.0f);
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

            // Map<Integer, Integer> swapRewardGunCountMap = new HashMap<>(rewardGunCountMap);
            // int limit = gunIds.size() - 1;
            // for (int i = 0; i <= limit; i++) {
            //     Integer gunCount = gunCounts.get(i);
            //     Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
            //     int rewardCount = 0;
            //     if (i == limit) {
            //         rewardCount = totalGunCount;
            //     } else {
            //         rewardCount = (int) Math.floor(gunCount * additionValue + 0.5); //(int) Math.ceil(gunCount * additionValue);
            //         totalGunCount -= rewardCount;
            //     }
            //     if (rewardCount < 1) {
            //         continue;
            //     }
            //     // switch (quality) {
            //     //     case White:
            //     //         //不应该会给白色品质的枪
            //     //         throw new BusinessException("不应该会给白色品质的枪。 id " + gunId);
            //     //     case Blue:
            //     //         //所有蓝色品质都算common，也就是random库不会增加
            //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Common, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //     //         break;
            //     //     case Orange:
            //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //     //         break;
            //     //     case Red:
            //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Epic, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //     //         break;
            //     // }
            //     Integer gunLibraryType = rewardGunLibraryTypes.get(i);
            //     gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.values()[gunLibraryType - 1], playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0.0f);
            //     List<Integer> tempNewUnlockGunIds = com.google.common.collect.Lists.newArrayList();
            //     List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
            //     obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, tempNewUnlockGunIds, gameVersion);
            //
            //     for (Integer value : tempNewUnlockGunIds) {
            //         if (!newUnlockGunIds.contains(value)) {
            //             newUnlockGunIds.add(value);
            //         }
            //     }
            //
            //     for (Map.Entry<Integer, Integer> rgentry : gunRewardMap.entrySet()) {
            //         Integer integer = rewardGunCountMap.get(rgentry.getKey());
            //         if (null == integer) {
            //             integer = rgentry.getValue();
            //         } else {
            //             integer += rgentry.getValue();
            //         }
            //         rewardGunCountMap.put(rgentry.getKey(), integer);
            //     }
            // }
        }
    }


    @Override
    public IAPPurchaseReward chapterGunPackageRewardDelivery(String uuid, int packageId, String gameVersion, float additionValue) {
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
        if (Objects.nonNull(packageTableValue.getRewardBulletId()) && packageTableValue.getRewardBulletId().size() > 0) {
            if (packageTableValue.getRewardBulletId().size() != packageTableValue.getRewardBulletCount().size()) {
                throw new BusinessException("章节枪械礼包id" + packageTableValue.getId() + "rewardBulletIdArray 和 rewardBulletCountArray 长度不一致");
            }

            obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
            obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
        }

        //枪械奖励是直接填表
        if (packageTableValue.getRewardGunId().size() != packageTableValue.getRewardGunCount().size()) {
            throw new BusinessException("章节枪械礼包id" + packageTableValue.getId() + "rewardGunIdArray 和 rewardGunCountArray 长度不一致");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), newUnlockGunIds, gameVersion, 0.0f);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        obsoleteUserDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion, 0.0f);
        if (additionValue > 0) {
            gunRewardAddition(userData, rewardGunCountMap, newUnlockGunIds, packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), packageTableValue.getRewardGunLibraryTypes(), gameVersion, additionValue);
            // int playerHighestUnlockedChapterID = obsoleteUserDataService.getPlayerHighestUnlockedChapterID(userData);
            // Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
            // Map<Integer, Integer> swapRewardGunCountMap = new HashMap<>(rewardGunCountMap);
            // for (Map.Entry<Integer, Integer> entry : swapRewardGunCountMap.entrySet()) {
            //     Integer gunId = entry.getKey();
            //     Integer gunCount = entry.getValue();
            //     // gunCount = (int) Math.ceil(gunCount * (1 + additionValue));
            //
            //     GunTableValue gunTableValue = gunTable.get(gunId.toString());
            //     GunQuality quality = GunQuality.values()[gunTableValue.getQuality() - 1];
            //
            //     Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
            //     int rewardCount = (int) Math.ceil(gunCount * additionValue);
            //     // switch (quality) {
            //     //     case White:
            //     //         //不应该会给白色品质的枪
            //     //         throw new BusinessException("不应该会给白色品质的枪。 id " + gunId);
            //     //     case Blue:
            //     //         //所有蓝色品质都算common，也就是random库不会增加
            //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Common, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //     //         break;
            //     //     case Orange:
            //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //     //         break;
            //     //     case Red:
            //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Epic, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //     //         break;
            //     // }
            //     gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Random, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0.0f);
            //     List<Integer> tempNewUnlockGunIds = com.google.common.collect.Lists.newArrayList();
            //     List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
            //     obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, tempNewUnlockGunIds, gameVersion);
            //
            //     for (Integer value : tempNewUnlockGunIds) {
            //         if (!newUnlockGunIds.contains(value)) {
            //             newUnlockGunIds.add(value);
            //         }
            //     }
            //
            //     for (Map.Entry<Integer, Integer> rgentry : gunRewardMap.entrySet()) {
            //         Integer integer = rewardGunCountMap.get(rgentry.getKey());
            //         if (null == integer) {
            //             integer = rgentry.getValue();
            //         } else {
            //             integer += rgentry.getValue();
            //         }
            //         rewardGunCountMap.put(rgentry.getKey(), integer);
            //     }
            // }
        }

        obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, 0.0f);

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
    public IAPPurchaseReward gunPackageRewardDelivery(String uuid, int packageId, String gameVersion, float additionValue) {
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
        if (Objects.nonNull(packageTableValue.getRewardBulletId()) && packageTableValue.getRewardBulletId().size() > 0) {
            if (packageTableValue.getRewardBulletId().size() != packageTableValue.getRewardBulletCount().size()) {
                throw new BusinessException("枪械礼包id" + packageTableValue.getId() + "rewardBulletIdArray 和 rewardBulletCountArray 长度不一致");
            }

            obsoleteUserDataService.addBulletToUserDataByIdAndCountArray(userData,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
            obsoleteUserDataService.mergeRewardBulletsToChestOpenResult(chestOpenResult,
                    packageTableValue.getRewardBulletId(),
                    packageTableValue.getRewardBulletCount());
        }

        //枪械奖励是直接填表
        if (packageTableValue.getRewardGunId().size() != packageTableValue.getRewardGunCount().size()) {
            throw new BusinessException("枪械礼包id" + packageTableValue.getId() + "rewardGunIdArray 和 rewardGunCountArray 长度不一致");
        }

        Map<Integer, Integer> rewardGunCountMap = CommonUtils.combineGunIdAndCountArrayToGunCountMap(packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount());
        List<Integer> newUnlockGunIds = new ArrayList<>();
        obsoleteUserDataService.addGunToUserDataByIdAndCountArray(userData, packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), newUnlockGunIds, gameVersion, 0.0f);
        chestOpenResult.setNewUnlockedGunIDs(newUnlockGunIds);
        obsoleteUserDataService.recordDirectlyGunRewardsCountToGunLibraryDrawCountMap(userData, rewardGunCountMap, gameVersion, 0.0f);
        if (additionValue > 0) {
            gunRewardAddition(userData, rewardGunCountMap, chestOpenResult.getNewUnlockedGunIDs(), packageTableValue.getRewardGunId(), packageTableValue.getRewardGunCount(), packageTableValue.getRewardGunLibraryTypes(), gameVersion, additionValue);
            // int playerHighestUnlockedChapterID = obsoleteUserDataService.getPlayerHighestUnlockedChapterID(userData);
            // Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
            // Map<Integer, Integer> swapRewardGunCountMap = new HashMap<>(rewardGunCountMap);
            // for (Map.Entry<Integer, Integer> entry : swapRewardGunCountMap.entrySet()) {
            //     Integer gunId = entry.getKey();
            //     Integer gunCount = entry.getValue();
            //     // gunCount = (int) Math.ceil(gunCount * (1 + additionValue));
            //
            //     GunTableValue gunTableValue = gunTable.get(gunId.toString());
            //     GunQuality quality = GunQuality.values()[gunTableValue.getQuality() - 1];
            //
            //     Map<Integer, Integer> gunRewardMap = Maps.newHashMap();
            //     int rewardCount = (int) Math.ceil(gunCount * additionValue);
            //     // switch (quality) {
            //     //     case White:
            //     //         //不应该会给白色品质的枪
            //     //         throw new BusinessException("不应该会给白色品质的枪。 id " + gunId);
            //     //     case Blue:
            //     //         //所有蓝色品质都算common，也就是random库不会增加
            //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Common, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //     //         break;
            //     //     case Orange:
            //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Rare, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //     //         break;
            //     //     case Red:
            //     //         gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Epic, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0f);
            //     //         break;
            //     // }
            //     gunRewardMap = chestService.extractGunRewardsFromGunLibraryAsync(userData, GunLibraryType.Random, playerHighestUnlockedChapterID, rewardCount, false, gunRewardMap, gameVersion, 0.0f);
            //     List<Integer> tempNewUnlockGunIds = com.google.common.collect.Lists.newArrayList();
            //     List<GunReward> gunRewards = CommonUtils.convertGunCountMapToGunCountArray(gunRewardMap);
            //     obsoleteUserDataService.addGunToUserDataByGunIdCountData(userData, gunRewards, tempNewUnlockGunIds, gameVersion);
            //
            //     for (Integer value : tempNewUnlockGunIds) {
            //         if (!newUnlockGunIds.contains(value)) {
            //             newUnlockGunIds.add(value);
            //         }
            //     }
            //
            //     for (Map.Entry<Integer, Integer> rgentry : gunRewardMap.entrySet()) {
            //         Integer integer = rewardGunCountMap.get(rgentry.getKey());
            //         if (null == integer) {
            //             integer = rgentry.getValue();
            //         } else {
            //             integer += rgentry.getValue();
            //         }
            //         rewardGunCountMap.put(rgentry.getKey(), integer);
            //     }
            // }
        }
        obsoleteUserDataService.mergeGunCountMapToChestOpenResult(chestOpenResult, rewardGunCountMap, 0.0f);

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
