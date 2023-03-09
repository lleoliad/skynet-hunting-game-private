package org.skynet.service.provider.hunting.obsolete.controller.game;

import com.alibaba.fastjson.JSONObject;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.rank.league.query.GetRankAdditionQuery;
import org.skynet.components.hunting.rank.league.service.RankLeagueFeignService;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.NanoIdUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.GameConfig;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.PurchaseBulletDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.PurchaseChestDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.PurchaseCoinPackageDTO;
import org.skynet.components.hunting.user.domain.ChestData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.ChestOpenResult;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.BulletTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.CoinBonusPackageTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.ShopChestsTableValue;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
import org.skynet.service.provider.hunting.obsolete.service.ObsoleteUserDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@Api(tags = "商店购买")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class ShopController {

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private ChestService chestService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private RankLeagueFeignService rankLeagueFeignService;

//    @GetMapping("coin")
//    @ApiOperation("购买金币")
//    public Map<String,Object> purchaseCoin(@RequestBody PurchaseCoinDTO dto){
//
//        try{
//            CommonUtils.requestProcess(dto,null);
//
//            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
//            UserData userData = null;
//
//            //处理userData
//            userDataService.checkUserDataExist(dto.getUserUid());
//            userData = GameEnvironment.userDataMap.get(dto.getUserUid());
//
//            Map<String, ShopCoinTableValue> shopCoinTable = GameEnvironment.shopCoinTableMap;
//
//            ShopCoinTableValue tableValue = shopCoinTable.get(String.valueOf(dto.getShopCoinTableId()));
//            if (userData.getDiamond()<tableValue.getDiamondPrice()){
//
//                throw new BusinessException("玩家"+userData.getUuid()+"商店购买金币id"+dto.getShopCoinTableId()+",玩家钻石不足,无法购买");
//            }
//
//            double coin = userData.getCoin() + userData.getCoin();
//            userData.setCoin(coin);
//            double diamond = userData.getDiamond() - tableValue.getDiamondPrice();
//            userData.setDiamond(diamond);
//
//            sendToClientData.setCoin(userData.getCoin());
//            sendToClientData.setDiamond(userData.getDiamond());
//
//            //处理返回结果
//            userDataService.userDataSettlement(userData,sendToClientData);
//            Map<String, Object> map = CommonUtils.responsePrepare(null);
//
//            map.put("userData",sendToClientData);
//
//            return map;
//        }catch (Exception e){
//            CommonUtils.responseException(dto,e.toString());
//        }
//        return null;
//    }

    @PostMapping("shop-purchaseChest")
    @ApiOperation("购买宝箱")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> purchaseChest(@RequestBody PurchaseChestDTO request) {
        try {
            GameEnvironment.timeMessage.computeIfAbsent("purchaseChest", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] purchaseChest" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
            UserData userData = null;

            //处理userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            userData = GameEnvironment.userDataMap.get(request.getUserUid());

            Result<Float> rankAddition = rankLeagueFeignService.getRankAddition(GetRankAdditionQuery.builder().userId(request.getUserUid()).build());
            float additionValue = rankAddition.getData();

            Integer chestLevel = obsoleteUserDataService.playerHighestUnlockedChapterID(userData);
            Map<String, ShopChestsTableValue> shopChestsTable = GameEnvironment.shopChestsTableMap.get(request.getGameVersion());
            ShopChestsTableValue tableValue = shopChestsTable.get(String.valueOf(request.getShopChestTableId()));

            if (chestLevel - 1 >= tableValue.getDiamondPrice().size()) {
                throw new BusinessException("玩家" + userData.getUuid() + "商店购买的箱子id" + request.getShopChestTableId() + ",箱子等级" + (chestLevel - 1) + "超过价格数组上限" + JSONObject.toJSONString(tableValue.getDiamondPrice()));
            }

            int price = tableValue.getDiamondPrice().get(chestLevel - 1);
            if (userData.getDiamond() < price) {

                throw new BusinessException("玩家" + userData.getUuid() + "商店购买箱子id" + request.getShopChestTableId() + ",玩家钻石不足,无法购买");
            }

            long tempPrice = userData.getDiamond() - price;
            userData.setDiamond(tempPrice);

            ChestData chestData = new ChestData(
                    NanoIdUtils.randomNanoId(30),
                    tableValue.getChestType(),
                    obsoleteUserDataService.playerHighestUnlockedChapterID(userData),
                    TimeUtils.getUnixTimeSecond()
            );

            ChestOpenResult chestOpenResult = chestService.openChest(userData, chestData, request.getGameVersion(), additionValue);

            sendToClientData.setCoin(userData.getCoin());
            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setGunCountMap(userData.getGunCountMap());
            sendToClientData.setGunLevelMap(userData.getGunLevelMap());
            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
            sendToClientData.setHistory(userData.getHistory());
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());

            chestService.saveChestOpenResult(chestOpenResult, request.getUserUid(), sendToClientData.getUpdateCount());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            //处理返回结果
            map.put("userData", sendToClientData);
            map.put("openResult", chestOpenResult);
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("purchaseChest").add(needTime);
            log.info("[cmd] purchaseChest finish need time" + (System.currentTimeMillis() - startTime));
            return map;

        } catch (Exception e) {

            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }


    @PostMapping("shop-purchaseCoinBonusPackage")
    @ApiOperation("购买金币礼包")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> purchaseCoinBonusPackage(@RequestBody PurchaseCoinPackageDTO request) {
        try {
            GameEnvironment.timeMessage.computeIfAbsent("purchaseCoinBonusPackage", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] purchaseCoinBonusPackage" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            Map<String, CoinBonusPackageTableValue> coinBonusPackageTable = GameEnvironment.coinBonusPackageTableMap.get(request.getGameVersion());
            CoinBonusPackageTableValue tableValue = null;

            if (!coinBonusPackageTable.containsKey(String.valueOf(request.getPackageId()))) {
                throw new BusinessException("玩家" + request.getUserUid() + "购买金币礼包,没有在 CoinBonusPackageTable 中找到" + request.getPackageId() + "的金币礼包条目");
            } else {
                tableValue = coinBonusPackageTable.get(String.valueOf(request.getPackageId()));
            }

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
            UserData userData = null;

            //处理userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            userData = GameEnvironment.userDataMap.get(request.getUserUid());


            //通过广告购买
            if (request.getGetByRewardAd()) {
                if (userData.getCoin() > GameConfig.canPlayAdsToGetCoinMaxAmount) {
                    throw new BusinessException("玩家金币数量" + userData.getCoin() + "超过允许通过广告购买金币礼包的上限" + GameConfig.canPlayAdsToGetCoinMaxAmount);
                }

                if (userData.getAdvertisementData().getRemainedRewardAdCountToday() <= 0) {
                    throw new BusinessException("玩家今日已经没有剩余的激励广告次数了");
                }

                if (tableValue != coinBonusPackageTable.get("1")) {
                    throw new BusinessException("通过广告购买的金币礼包" + JSONObject.toJSONString(tableValue) + "不是第一个礼包");
                }

                userData.getAdvertisementData().setRemainedRewardAdCountToday(userData.getAdvertisementData().getRemainedRewardAdCountToday() - 1);
            }
            //通过钻石购买
            else {

                if (userData.getDiamond() < tableValue.getDiamondPrice()) {

                    throw new BusinessException("玩家" + request.getUserUid() + "购买金币礼包,钻石不足. now " + userData.getDiamond() + ", need" + tableValue.getDiamondPrice());
                }
                long tempDiamond = userData.getDiamond() - tableValue.getDiamondPrice();
                userData.setDiamond(tempDiamond);
            }

            long tempCoin = userData.getCoin() + tableValue.getCoinAmount();
            userData.setCoin(tempCoin);
            userData.getHistory().setTotalEarnedCoin(userData.getHistory().getTotalEarnedCoin() + tableValue.getCoinAmount());
            log.info("购买金币礼包" + tableValue.getId() + "完成，coin:" + userData.getCoin() + ", diamond:" + userData.getDiamond() + ", getByRewardAd:" + request.getGetByRewardAd());

            sendToClientData.setCoin(userData.getCoin());
            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setHistory(userData.getHistory());
            sendToClientData.setAdvertisementData(userData.getAdvertisementData());
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", sendToClientData);
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("purchaseCoinBonusPackage").add(needTime);
            log.info("[cmd] purchaseCoinBonusPackage finish need time" + (System.currentTimeMillis() - startTime));
            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }

    @PostMapping("weapon-purchaseBullet")
    @ApiOperation("购买子弹")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> purchaseBullet(@RequestBody PurchaseBulletDTO request) {
        try {
            GameEnvironment.timeMessage.computeIfAbsent("purchaseBullet", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] purchaseBullet" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            //处理userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());
            String playerUUID = userData.getUuid();

            Map<String, BulletTableValue> bulletTable = GameEnvironment.bulletTableMap.get(request.getGameVersion());

            BulletTableValue bulletValue = null;

            if (!bulletTable.containsKey(String.valueOf(request.getBulletId()))) {
                throw new BusinessException("用户" + playerUUID + "想要购买子弹 BulletTable没有找到内容 bullet id:" + request.getBulletId());
            }

            bulletValue = bulletTable.get(String.valueOf(request.getBulletId()));

            int price = 0;

            if (bulletValue.getPurchaseDiamondPrice().size() < request.getPurchaseIndex() + 1) {

                throw new BusinessException("用户" + playerUUID + " 想要购买子弹 bullet id:" + request.getBulletId() + ",无法查询到价格 purchase index:"
                        + request.getPurchaseIndex() + ",price array" + bulletValue.getPurchaseDiamondPrice());
            }

            price = bulletValue.getPurchaseDiamondPrice().get(request.getPurchaseIndex());

            if (userData.getDiamond() < price) {
                throw new BusinessException("用户" + playerUUID + " 想要购买子弹 bullet id:" + request.getBulletId() + ",钻石不足,now: "
                        + userData.getDiamond() + ",price" + price);
            }

            if (request.getPurchaseIndex() < 0 || request.getPurchaseIndex() > 2) {
                throw new BusinessException("用户" + playerUUID + " 想要购买子弹 bullet id:" + request.getBulletId() + ",无法查询到购买数量 purchase index:"
                        + request.getPurchaseIndex() + ",PurchaseBulletCount" + Arrays.toString(GameConfig.purchaseBulletCountArray));
            }

            int purchaseCount = GameConfig.purchaseBulletCountArray[request.getPurchaseIndex()];

            //开始购买
            long tempPrice = userData.getDiamond() - price;
            userData.setDiamond(tempPrice);
            int bulletCount = userData.getBulletCountMap().getOrDefault(request.getBulletId(), 0);
            bulletCount += purchaseCount;
            userData.getBulletCountMap().put(request.getBulletId(), bulletCount);

            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
            sendToClientData.setHistory(userData.getHistory());

            //处理返回结果
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());

            Map<String, Object> map = CommonUtils.responsePrepare(null);

            map.put("userData", sendToClientData);
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("purchaseBullet").add(needTime);
            log.info("[cmd] purchaseBullet finish need time" + (System.currentTimeMillis() - startTime));
            return map;
        } catch (Exception e) {

            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }
}
