package org.skynet.service.provider.hunting.obsolete.controller.game;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.rank.league.query.GetRankAdditionQuery;
import org.skynet.components.hunting.rank.league.service.RankLeagueFeignService;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.domain.History;
import org.skynet.components.hunting.user.domain.UserPendingPurchaseData;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.NanoIdUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.dao.entity.TopUpOrder;
import org.skynet.service.provider.hunting.obsolete.enums.OrderState;
import org.skynet.service.provider.hunting.obsolete.enums.PlatformName;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.IapReceiptValidateDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.PreparePurchaseDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.RemoveFailureDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.SyncPendingDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.PendingPurchaseOrder;
import org.skynet.service.provider.hunting.obsolete.service.IAPService;
import org.skynet.service.provider.hunting.obsolete.dao.service.TopUpOrderService;
import org.skynet.service.provider.hunting.obsolete.service.ObsoleteUserDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "支付")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class IAPController {

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private IAPService iapService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private TopUpOrderService topUpOrderService;

    @Resource
    private RankLeagueFeignService rankLeagueFeignService;

    @PostMapping("iap-preparePurchase")
    @ApiOperation(value = "准备购买", notes = "记录玩家正在发起一次购买,玩家完成或者失败之后,清除该记录")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> preparePurchase(@RequestBody PreparePurchaseDTO request) {
        try {
            GameEnvironment.timeMessage.computeIfAbsent("preparePurchase", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] preparePurchase" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
            String customOrderId = NanoIdUtils.randomNanoId(30);
            UserData userData = null;

            //处理userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            userData = GameEnvironment.userDataMap.get(request.getUserUid());

            List<UserPendingPurchaseData> pendingPurchasesData = new ArrayList<>();
            if (userData.getIapPendingPurchaseProductsData() != null && userData.getIapPendingPurchaseProductsData().size() > 0) {

                pendingPurchasesData = userData.getIapPendingPurchaseProductsData();
            }

            UserPendingPurchaseData newPendingPurchaseData = new UserPendingPurchaseData(customOrderId, request.getProductName());

            boolean foundExistPendingData = false;

            for (int i = 0; i < pendingPurchasesData.size(); i++) {

                UserPendingPurchaseData currentData = pendingPurchasesData.get(i);
                if (currentData.getProductName().equals(request.getProductName())) {

                    pendingPurchasesData.set(i, newPendingPurchaseData);
                    foundExistPendingData = true;
                    break;
                }
            }

            if (!foundExistPendingData) {
                pendingPurchasesData.add(newPendingPurchaseData);
            }

            iapService.savePendingCustomOrder(userData, newPendingPurchaseData, request.getAdditionalParametersJSON());
            TopUpOrder topUpOrder = new TopUpOrder();
            topUpOrder.setOrderNumber(customOrderId);
            topUpOrder.setUserInfo(request.getUserUid());
            topUpOrder.setOrderState(OrderState.Place.getType());
            topUpOrder.setOrderDate(LocalDateTime.now());
            topUpOrder.setProductName(request.getProductName());
            topUpOrder.setGoodCount(1);
            topUpOrder.setOrderType(0);
            topUpOrderService.save(topUpOrder);

            userData.setIapPendingPurchaseProductsData(pendingPurchasesData);
            sendToClientData.setIapPendingPurchaseProductsData(pendingPurchasesData);
            log.info("正在发起购买. product: " + request.getProductName() + ",pending data" + JSONObject.toJSONString(pendingPurchasesData));

            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);

            map.put("userData", sendToClientData);
            map.put("customOrderId", customOrderId);

            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("preparePurchase").add(needTime);
            log.info("[cmd] completeAchievement finish need time" + (System.currentTimeMillis() - startTime));
            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }

    @PostMapping("iap-iapReceiptValidate")
    @ApiOperation(value = "验证内购订单,并发送商品")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> iapReceiptValidate(@RequestBody IapReceiptValidateDTO request) {
        try {
            GameEnvironment.timeMessage.computeIfAbsent("iapReceiptValidate", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] iapReceiptValidate" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            ReceiptValidateResult validateResult = null;

            PendingPurchaseOrder pendingCustomOrder = iapService.getPendingCustomOrder(request.getCustomOrderId());
            if (Objects.isNull(pendingCustomOrder)) {
                throw new BusinessException("获取内部订单号" + request.getCustomOrderId() + "不存在");
            }

            if (request.getPlatform().equals(PlatformName.UnityEditor.getPlatform())) {

//                if (request.getProductName()==null){
//
//                    throw new BusinessException("测试内购订单验证,但是没有上传productName");
//                }

                log.info("测试内购订单验证,直接通过");
                validateResult = new ReceiptValidateResult(
                        false,
                        true,
                        pendingCustomOrder.getProductName(),
                        "test_order",
                        "test_order"
                );
                QueryWrapper<TopUpOrder> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_number", request.getCustomOrderId());
                TopUpOrder topUpOrder = topUpOrderService.getOne(queryWrapper);
                topUpOrder.setOrderState(OrderState.Completed.getType());
                topUpOrder.setOrderEndDate(LocalDateTime.now());
                topUpOrderService.updateById(topUpOrder);

            } else {
                log.info("不是测试订单,需要验证");
                //先看一下这个订单是不是这个玩家的
//                pendingCustomOrder = iapService.getPendingCustomOrder(request.getCustomOrderId());
//                //订单过期
//                if (pendingCustomOrder == null){
//                    throw new BusinessException("获取内部订单号"+request.getCustomOrderId()+"不存在");
//                }
                if (!pendingCustomOrder.getPlayerUid().equals(request.getUserUid())) {
                    throw new BusinessException("获取内部订单号" + request.getCustomOrderId() + "不属于该玩家");
                }
                QueryWrapper<TopUpOrder> queryWrapper = new QueryWrapper<TopUpOrder>();
                queryWrapper.eq("order_number", request.getCustomOrderId());
                TopUpOrder topUpOrder = topUpOrderService.getOne(queryWrapper);
                topUpOrder.setReceiptValidateResult(request.getReceipt());
                topUpOrder.setOrderState(OrderState.Verifying.getType());
                topUpOrderService.updateById(topUpOrder);
                log.warn("订单更新到mysql成功");

                if (request.getCmd() == null) {
                    log.warn("request.getCmd()参数为空，应该通过其他方式获取");
                } else {
                    log.warn("request.getCmd()参数为不为，参数值为{}", JSONUtil.toJsonStr(request.getCmd()));
                }

                if (request.getPlatform().equals(PlatformName.Android.getPlatform())) {
                    validateResult = iapService.googlePlayReceiptValidate(request);

                } else if (request.getPlatform().equals(PlatformName.IOS.getPlatform())) {
                    //TODO ios订单验证
                    log.info("ios充值");
                }
            }

            if (validateResult == null) {
                throw new BusinessException("内购订单" + request.getReceipt() + "验证失败");
            }

            //如果已经完成,不做任何事
            if (validateResult.getIsAlreadyComplete()) {
                log.warn("该订单为已完成订单，不需要重复操作");
                Map<String, Object> map = CommonUtils.responsePrepare(null);
                map.put("userData", sendToClientData);
                return map;
            }
            //验证订单通过
            else if (validateResult.getIsValidPass()) {
                log.warn("订单验证通过,验证结果为{}", JSONUtil.toJsonStr(validateResult));
                IAPPurchaseReward purchaseReward = null;

                //处理userData
                obsoleteUserDataService.checkUserDataExist(request.getUserUid());
                UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

                Result<Float> rankAddition = rankLeagueFeignService.getRankAddition(GetRankAdditionQuery.builder().userId(request.getUserUid()).build());
                float additionValue = rankAddition.getData();

                purchaseReward = iapService.iapPurchaseContentDelivery(userData.getUuid(), validateResult.getProductName(), pendingCustomOrder.getAdditionalParametersJSON(), request.getGameVersion(), additionValue);

                iapService.clearPendingPurchaseInUserData(userData, pendingCustomOrder.getCustomOrderId());

                iapService.archivePendingCustomOrder(pendingCustomOrder.getCustomOrderId(), validateResult.getOrderId());

                log.warn("开始处理返回结果");
                //处理返回结果
                sendToClientData.setIapProductPurchasedCountMap(userData.getIapProductPurchasedCountMap());

                sendToClientData.setCoin(userData.getCoin());

                sendToClientData.setDiamond(userData.getDiamond());
                sendToClientData.setLuckyWheelData(userData.getLuckyWheelData());
                sendToClientData.setVipData(userData.getVipData());
                sendToClientData.setVipV2Data(userData.getVipV2Data());
                sendToClientData.setVipV3Data(userData.getVipV3Data());

                History history = new History();
                BeanUtils.copyProperties(userData.getHistory(), history);
                sendToClientData.setHistory(history);

                obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
                iapService.saveCompletedOrder(validateResult, request.getReceipt(), pendingCustomOrder.getCustomOrderId(), request.getUserUid());
                sendToClientData.setGunCountMap(userData.getGunCountMap());
                sendToClientData.setGunLevelMap(userData.getGunLevelMap());
                sendToClientData.setBulletCountMap(userData.getBulletCountMap());
                sendToClientData.setChapterBonusPackagesData(userData.getChapterBonusPackagesData());
                sendToClientData.setPromotionEventPackagesData(userData.getPromotionEventPackagesData());
                sendToClientData.setAvailableGunGiftPackageData(userData.getAvailableGunGiftPackageData());
                sendToClientData.setAvailableBulletGiftPackageData(userData.getAvailableBulletGiftPackageData());
                sendToClientData.setAvailableFifthDayGunGiftPackageData(userData.getAvailableFifthDayGunGiftPackageData());
                sendToClientData.setPromotionGiftPackagesV2Data(userData.getPromotionGiftPackagesV2Data());
                sendToClientData.setHistory(userData.getHistory());
                Map<String, Object> map = CommonUtils.responsePrepare(null);

                map.put("userData", sendToClientData);
                map.put("purchaseReward", purchaseReward);
                long needTime = System.currentTimeMillis() - startTime;
                GameEnvironment.timeMessage.get("iapReceiptValidate").add(needTime);
                log.info("[cmd] iapReceiptValidate finish need time" + (System.currentTimeMillis() - startTime));
                return map;
            }

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;

    }


    @PostMapping("iap-removeFailurePendingPurchase")
    @ApiOperation("内购如果失败的话,清除pending purchase记录")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> removeFailurePendingPurchase(@RequestBody RemoveFailureDTO dto) {

        try {
            ThreadLocalUtil.set(dto.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] removeFailurePendingPurchase" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(dto));
            CommonUtils.requestProcess(dto, null, systemPropertiesConfig.getSupportRecordModeClient());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
            UserData userData = null;

            //处理userData
            obsoleteUserDataService.checkUserDataExist(dto.getUserUid());
            userData = GameEnvironment.userDataMap.get(dto.getUserUid());

            if (userData.getIapPendingPurchaseProductsData() == null) {
                userData.setIapPendingPurchaseProductsData(new ArrayList<>());
            }

//            userData.setIapPendingPurchaseProductsData(userData.getIapPendingPurchaseProductsData().stream().
//                    filter(value -> !value.getProductName().equals(dto.getProductName())).collect(Collectors.toList()));

            Iterator<UserPendingPurchaseData> iterator = userData.getIapPendingPurchaseProductsData().iterator();
            //将数据库中的订单设置为过期
            while (iterator.hasNext()) {
                UserPendingPurchaseData purchaseData = iterator.next();
                if (purchaseData.getProductName().equals(dto.getProductName())) {
                    QueryWrapper<TopUpOrder> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("order_number", purchaseData.getCustomOrderId());
                    TopUpOrder topUpOrder = topUpOrderService.getOne(queryWrapper);
                    topUpOrder.setOrderState(OrderState.Overdue.getType());
                    topUpOrder.setOrderEndDate(LocalDateTime.now());
                    topUpOrderService.updateById(topUpOrder);
                    iterator.remove();
                }
            }
            log.info("内购失败,清除记录. product: " + dto.getProductName() + ",result: " + JSONObject.toJSONString(userData.getIapPendingPurchaseProductsData()));

            sendToClientData.setIapPendingPurchaseProductsData(userData.getIapPendingPurchaseProductsData());

            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, dto.getGameVersion());

            Map<String, Object> map = CommonUtils.responsePrepare(null);

            map.put("userData", sendToClientData);
            log.info("[cmd] removeFailurePendingPurchase finish need time" + (System.currentTimeMillis() - startTime));
            return map;

        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }


    @PostMapping("iap-syncPendingPurchaseProducts")
    @ApiOperation(value = "同步未购买订单", notes = "以客户端iap返回的未完成订单为准,反正订单都要验证,可以信任客户端")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> syncPendingPurchaseProducts(@RequestBody SyncPendingDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] syncPendingPurchaseProducts" + System.currentTimeMillis());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
            UserData userData = null;

            //处理userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            userData = GameEnvironment.userDataMap.get(request.getUserUid());

            List<UserPendingPurchaseData> pendingPurchaseProductsData = userData.getIapPendingPurchaseProductsData();
            //客户端发的是平台给的未完成订单,只处理这些订单
            pendingPurchaseProductsData = pendingPurchaseProductsData.stream().filter(value -> request.getPendingPurchaseProductsNames().contains(value.getProductName())).collect(Collectors.toList());
            userData.setIapPendingPurchaseProductsData(pendingPurchaseProductsData);
            sendToClientData.setIapPendingPurchaseProductsData(userData.getIapPendingPurchaseProductsData());

            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, false, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", sendToClientData);
            log.info("[cmd] syncPendingPurchaseProducts finish need time" + (System.currentTimeMillis() - startTime));
            return map;

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }

//    @PostMapping("iap-updateOrder")
//    @ApiOperation(value = "更新订单信息",notes = "比如漏单了，更新订单状态")
//    public Map<String,Object> updateOrder(@RequestBody UpdateOrderDTO dto){
//        try {
//            CommonUtils.processAdminRequest(dto.getAdminKey());
//
//            String[] customOrderIds = dto.getCustomOrderIds();
//            List<TopUpOrder> topUpOrderList = new ArrayList<>();
//            for (String customOrderId : customOrderIds) {
//                QueryWrapper<TopUpOrder>  queryWrapper = new QueryWrapper<>();
//                queryWrapper.eq("order_number",customOrderId);
//                TopUpOrder topUpOrder = topUpOrderService.getOne(queryWrapper);
//                topUpOrder.setOrderOmitState(OmitState.unOmit.getType());
//                topUpOrderList.add(topUpOrder);
//            }
//            topUpOrderService.saveBatch(topUpOrderList);
//
//            return CommonUtils.responsePrepare(null);
//        }catch (Exception e){
//            CommonUtils.responseException(dto,e,dto.getUserUid());
//        }
//        return null;
//    }


//    @PostMapping("iap-showNeedSupplement/{page}/{limit}")
//    @ApiOperation("展示需要补单的订单")
//    public Map<String,Object> showNeedSupplement(@ApiParam(value = "当前页码", required = true) @PathVariable Long page,
//                                                 @ApiParam(value = "每页记录数", required = true) @PathVariable Long limit){
//
//        try {
//            Page<TopUpOrder> pageParam = new Page<>(page, limit);
//            IPage<TopUpOrder> pageModel = topUpOrderService.showNeedSupplement(pageParam);
//            Map<String, Object> map = CommonUtils.responsePrepare(null);
//            map.put("pageModel",pageModel);
//            return map;
//        }catch (Exception e){
//            Map<String, Object> map = CommonUtils.responsePrepare(-1);
//            map.put("error",e.toString());
//        }
//        return null;
//
//    }
}
