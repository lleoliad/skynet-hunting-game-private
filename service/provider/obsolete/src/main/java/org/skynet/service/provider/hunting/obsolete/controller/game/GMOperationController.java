package org.skynet.service.provider.hunting.obsolete.controller.game;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.rank.league.query.GetRankAdditionQuery;
import org.skynet.components.hunting.rank.league.service.RankLeagueFeignService;
import org.skynet.service.provider.hunting.obsolete.common.exception.Assert;
import org.skynet.service.provider.hunting.obsolete.common.result.R;
import org.skynet.service.provider.hunting.obsolete.common.result.ResponseEnum;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.DeflaterUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.enums.OmitState;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.IAPPurchaseReward;
import org.skynet.service.provider.hunting.obsolete.dao.entity.TopUpOrder;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.service.IAPService;
import org.skynet.service.provider.hunting.obsolete.dao.service.TopUpOrderService;
import org.skynet.service.provider.hunting.obsolete.service.ObsoleteUserDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.*;
import org.skynet.service.provider.hunting.obsolete.pojo.table.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@Api(tags = "GM操作")
@RestController
@RequestMapping("/huntingrival/gm")
@Slf4j
public class GMOperationController {

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private TopUpOrderService topUpOrderService;

    @Resource
    private IAPService iapService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private RankLeagueFeignService rankLeagueFeignService;


//    @PostMapping("check/integrity")
//    @ApiOperation("检查app完整性")
//    public Map<String,Object> checkAppIntegrity(@RequestBody CheckAppIntegrityDTO dto){
//
//        try {
//            CommonUtils.requestProcess(dto, null,false);
//
//            log.info("上报版本"+dto.getGameVersion()+"code hash"+dto.getCodeHash());
//            //todo 需要判断codehash是否有效
//
//            Map<String,Object> map = new LinkedHashMap<>();
//
//            map.put("integrity",true);
//
//            return map;
//        }catch (Exception e){
//            CommonUtils.responseException(dto, e,dto.getUserUid());
//        }
//
//        return null;
//    }


    @PostMapping("getAll/userData")
    @ApiOperation("获取所有的用户数据")
    public R getAllUserData(@RequestBody GMDTO dto) {

        try {
            CommonUtils.processAdminRequest(dto.getAdminKey());

            if (dto.getControlType().equals("getAllUserData")) {

                List<UserData> allUserData = obsoleteUserDataService.getAllUserData();


                return R.ok().serverTime(TimeUtils.getUnixTimeSecond()).data("allUserData", allUserData);
            }
        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
            return null;
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;
    }


    @PostMapping("get/user")
    @ApiOperation("获取某个用户的所有数据")
    public R readUserData(@RequestBody BaseDTO dto) {

        try {
            CommonUtils.processAdminRequest(dto.getAdminKey());
            UserData userData = obsoleteUserDataService.getUserData("User:" + dto.getUserUid());

            return R.ok().serverTime(TimeUtils.getUnixTimeSecond()).data("userData", userData);
        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
        }
        return null;
    }

    @PostMapping("save/user")
    @ApiOperation("保存某个用户数据，用于单个修改")
    public R saveUserData(@RequestBody GMDTO dto) {
        try {
            CommonUtils.processAdminRequest(dto.getAdminKey());
            UserData userData = obsoleteUserDataService.getUserData("User:" + dto.getUserUid());

            return R.ok().serverTime(TimeUtils.getUnixTimeSecond()).data("userData", userData);
        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;

    }


    @PostMapping("remove/user")
    @ApiOperation("删除用户")
    public R removeUser(@RequestBody GMDTO dto) {

        try {

            CommonUtils.processAdminRequest(dto.getAdminKey());

            Assert.isTrue(dto.getControlType().equals("removeUserData"), ResponseEnum.CMD_ERROR);
            String result = obsoleteUserDataService.removeUserData(dto.getUserUid());

            return R.ok().data("result", result).serverTime(TimeUtils.getUnixTimeSecond());


        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;
    }

    @PostMapping("update/user")
    @ApiOperation("更新用户")
    public R updateUser(@RequestBody GMDTO dto) {

        try {
            CommonUtils.processAdminRequest(dto.getAdminKey());
            Assert.isTrue(dto.getControlType().equals("updateUserData"), ResponseEnum.CMD_ERROR);

            String zipString = dto.getEncodeUserData();

            String unzipString = DeflaterUtils.unzipString(zipString);

            UserData userData = JSONObject.parseObject(unzipString, UserData.class);
            String result = obsoleteUserDataService.updateUserData(userData);

            return R.ok().data("result", result).serverTime(TimeUtils.getUnixTimeSecond());

        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;
    }


    @PostMapping("logout/user")
    @ApiOperation("踢人下线")
    public R logoutUser(@RequestBody GMDTO dto) {

        try {
            CommonUtils.processAdminRequest(dto.getAdminKey());

            Assert.isTrue(dto.getControlType().equals("logoutUser"), ResponseEnum.CMD_ERROR);

            String result = obsoleteUserDataService.logoutUserData(dto.getUserUid());

            return R.ok().data("result", result).serverTime(TimeUtils.getUnixTimeSecond());


        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;
    }

    @PostMapping("block/user")
    @ApiOperation("封禁用户")
    public R blockUser(@RequestBody GMDTO dto) {
        try {
            CommonUtils.processAdminRequest(dto.getAdminKey());

            Assert.isTrue(dto.getControlType().equals("blockUser"), ResponseEnum.CMD_ERROR);

            String result = obsoleteUserDataService.blockUserData(dto.getUserUid(), dto.getBlockTime());

            return R.ok().data("result", result).serverTime(TimeUtils.getUnixTimeSecond());


        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;
    }

    @PostMapping("/update/gun")
    @ApiOperation("更新用户枪械数据")
    public R updateGun(@RequestBody UpdateGUNDTO dto) {

        try {
            CommonUtils.processAdminRequest(dto.getAdminKey());
            Assert.isTrue(dto.getControlType().equals("updateGun"), ResponseEnum.CMD_ERROR);
            String result = obsoleteUserDataService.updateUserGun(dto);

            return R.ok().serverTime(TimeUtils.getUnixTimeSecond()).data("result", result);
        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;
    }

    @PostMapping("/remove/gun")
    @ApiOperation("删除用户枪械数据")
    public R removeGun(@RequestBody DeleteGUNDTO dto) {

        try {
            CommonUtils.processAdminRequest(dto.getAdminKey());
            Assert.isTrue(dto.getControlType().equals("deleteGun"), ResponseEnum.CMD_ERROR);
            String result = obsoleteUserDataService.deleteUserGun(dto);

            return R.ok().serverTime(TimeUtils.getUnixTimeSecond()).data("result", result);
        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;
    }

    @PostMapping("/update/prop")
    @ApiOperation("更新养成数据")
    public R updateProp(@RequestBody UpdatePropDTO dto) {

        try {
            CommonUtils.processAdminRequest(dto.getAdminKey());
            Assert.isTrue(dto.getControlType().equals("updateProp"), ResponseEnum.CMD_ERROR);
            String result = obsoleteUserDataService.updateProp(dto);

            return R.ok().serverTime(TimeUtils.getUnixTimeSecond()).data("result", result);
        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;
    }

//    @PostMapping("/get/order")
//    @ApiOperation("获取订单数据")
//    public R getOrder(@RequestBody OrderDTO dto){
//
//        try {
//            CommonUtils.processAdminRequest(dto.getAdminKey());
//            Assert.isTrue(dto.getControlType().equals("getOrder"), ResponseEnum.CMD_ERROR);
////            List<CompletedOrder> completedOrder = iapService.getCompletedOrder(dto);
//
//            List<TopUpOrder> list = topUpOrderService.list();
//            return R.ok().serverTime(TimeUtils.getUnixTimeSecond()).data("result",list);
//        }catch (Exception e){
//            CommonUtils.responseException(dto, e,dto.getUserUid());
//        }
//        return null;
//    }

    @PostMapping("/update/order")
    @ApiOperation("修改订单")
    public R updateOrder(@RequestBody OrderDTO dto) {

        try {
            CommonUtils.processAdminRequest(dto.getAdminKey());
            Assert.isTrue(dto.getControlType().equals("updateOrder"), ResponseEnum.CMD_ERROR);
            String encodeData = dto.getEncodeData();
            String unzipString = DeflaterUtils.unzipString(encodeData);
            TopUpOrder newOrder = JSONObject.parseObject(unzipString, TopUpOrder.class);
            topUpOrderService.updateById(newOrder);
            return R.ok().serverTime(TimeUtils.getUnixTimeSecond());
        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;
    }

    @PostMapping("/remove/order")
    @ApiOperation("删除订单")
    public R removeOrder(@RequestBody OrderDTO dto) {

        try {
            CommonUtils.processAdminRequest(dto.getAdminKey());
            Assert.isTrue(dto.getControlType().equals("deleteOrder"), ResponseEnum.CMD_ERROR);
            QueryWrapper<TopUpOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_number", dto.getCustomOrderId());
            topUpOrderService.remove(queryWrapper);

            return R.ok().serverTime(TimeUtils.getUnixTimeSecond());
        } catch (Exception e) {
            CommonUtils.responseException(dto, e, dto.getUserUid());
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;
    }

    @PostMapping("supplement/order")
    @ApiOperation("补单")
    public R supplement(@RequestBody UpdateOrderDTO request) {

        try {
            CommonUtils.processAdminRequest(request.getAdminKey());
            String customOrderId = request.getCustomOrderId();

            Map<String, Object> map = CommonUtils.responsePrepare(null);

            QueryWrapper<TopUpOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_number", customOrderId);
            TopUpOrder topUpOrder = topUpOrderService.getOne(queryWrapper);
            String userInfo = topUpOrder.getUserInfo();
            String productName = topUpOrder.getProductName();
            IAPPurchaseReward purchaseReward = null;

            //帮用户登录一下
            //补单调用该接口时，用户没有登录
            obsoleteUserDataService.checkUserDataExist(userInfo);
            UserData userData = GameEnvironment.userDataMap.get(userInfo);
            String gameVersion = userData.getServerOnly().getLastLoginClientVersion();

            Result<Float> rankAddition = rankLeagueFeignService.getRankAddition(GetRankAdditionQuery.builder().userId(request.getUserUid()).build());
            float additionValue = rankAddition.getData();

            PendingPurchaseOrder pendingCustomOrder = iapService.getPendingCustomOrder(customOrderId);

            purchaseReward = iapService.iapPurchaseContentDelivery(userInfo, productName, pendingCustomOrder.getAdditionalParametersJSON(), gameVersion, additionValue);
            map.put(userInfo, purchaseReward);
            topUpOrder.setOrderOmitState(OmitState.Supplement.getType());

            topUpOrderService.updateById(topUpOrder);
            obsoleteUserDataService.saveUserData(userData);
            return R.ok().serverTime(TimeUtils.getUnixTimeSecond()).data("purchaseRewardList", map);
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;
    }

    @PostMapping("/product")
    @ApiOperation("获取所有的商品名称")
    public R getProduct(@RequestBody GMDTO request) {
        try {
            CommonUtils.processAdminRequest(request.getAdminKey());
            Set<String> products = new HashSet<>();
            String[] clientGameVersion = systemPropertiesConfig.getClientGameVersion();
            for (String gameVersion : clientGameVersion) {
                Map<String, ChapterBonusPackageTableValue> packageTableMap = GameEnvironment.chapterBonusPackageTableMap.get(gameVersion);
                if (packageTableMap != null && packageTableMap.size() != 0) {
                    for (ChapterBonusPackageTableValue tableValue : packageTableMap.values()) {
                        products.add(tableValue.getProductId());
                    }
                }
                Map<String, ShopCoinTableValue> shopCoinTable = GameEnvironment.shopCoinTableMap.get(gameVersion);
                if (shopCoinTable != null && shopCoinTable.size() != 0) {
                    for (ShopCoinTableValue shopCoinTableValue : shopCoinTable.values()) {
                        products.add(shopCoinTableValue.getProductId());
                    }
                }
                Map<String, ShopDiamondTableValue> shopDiamondTable = GameEnvironment.shopDiamondTableMap.get(gameVersion);
                if (shopDiamondTable != null && shopDiamondTable.size() != 0) {
                    for (ShopDiamondTableValue shopDiamondTableValue : shopDiamondTable.values()) {
                        products.add(shopDiamondTableValue.getProductId());
                    }
                }
                Map<String, PromotionEventPackageGroupTableValue> promotionEventPackageGroupTable = GameEnvironment.promotionEventPackageGroupTableMap.get(gameVersion);
                if (promotionEventPackageGroupTable != null && promotionEventPackageGroupTable.size() != 0) {
                    for (PromotionEventPackageGroupTableValue packageGroupTableValue : promotionEventPackageGroupTable.values()) {
                        products.add(packageGroupTableValue.getProductName());
                    }
                }

            }

            return R.ok().serverTime(TimeUtils.getUnixTimeSecond()).data("productNameList", products);

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;
    }

    @PostMapping("/weapon")
    @ApiOperation("获得所有的枪和子弹的种类")
    public R getWeapon(@RequestBody GMDTO request) {
        try {
            CommonUtils.processAdminRequest(request.getAdminKey());
            Map<String, Set<Integer>> map = new HashMap<>();
            Set<Integer> gunSet = new HashSet<>();
            Set<Integer> bulletSet = new HashSet<>();
            String[] clientGameVersion = systemPropertiesConfig.getClientGameVersion();
            for (String gameVersion : clientGameVersion) {
                Map<String, GunTableValue> gunTable = GameEnvironment.gunTableMap.get(gameVersion);
                if (gunTable != null && gunTable.size() != 0) {
                    for (GunTableValue gunTableValue : gunTable.values()) {
                        gunSet.add(gunTableValue.getId());
                    }
                }
                Map<String, BulletTableValue> bulletTableValueMap = GameEnvironment.bulletTableMap.get(gameVersion);
                if (bulletTableValueMap != null && bulletTableValueMap.size() != 0) {
                    for (BulletTableValue bulletTableValue : bulletTableValueMap.values()) {
                        bulletSet.add(bulletTableValue.getId());
                    }
                }

            }

            map.put("gun", gunSet);
            map.put("bullet", bulletSet);
            return R.ok().serverTime(TimeUtils.getUnixTimeSecond()).data("weapon", map);

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            if (ThreadLocalUtil.localVar.get() != null) {
                ThreadLocalUtil.localVar.remove();
            }
        }
        return null;
    }
}
