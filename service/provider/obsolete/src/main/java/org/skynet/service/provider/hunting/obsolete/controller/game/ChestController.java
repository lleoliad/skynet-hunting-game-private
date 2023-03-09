package org.skynet.service.provider.hunting.obsolete.controller.game;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import org.skynet.commons.hunting.user.dao.entity.UserData;
import org.skynet.commons.hunting.user.domain.ChapterWinChestData;
import org.skynet.commons.hunting.user.domain.ChestData;
import org.skynet.commons.hunting.user.domain.FreeChestData;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.*;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.BaseDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.OpenChapterDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.StartUnlockDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
import org.skynet.service.provider.hunting.obsolete.service.UserDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.BackingStoreException;

@Api(tags = "章节箱子相关操作")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class ChestController {

    @Resource
    private UserDataService userDataService;

    @Resource
    private ChestService chestService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

//    @GetMapping("open/progress")
//    @ApiOperation("打开章节箱子")
//    public Map<String, Object> openChapterProgressChest(@RequestBody BaseDTO dto) {
//
//        try {
//            CommonUtils.requestProcess(dto, null,systemPropertiesConfig.getSupportRecordModeClient());
//
//            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
//            UserData userData = null;
//
//            //处理userData
//            userDataService.checkUserDataExist(dto.getUserUid());
//            userData = GameEnvironment.userDataMap.get(dto.getUserUid());
//
//            ChestOpenResult openResult = new ChestOpenResult();
//            ChapterProgressChestData chapterChestData = null;
//            if (userData.getChapterProgressChestsData() == null) {
//                throw new BusinessException("玩家没有章节箱子数据");
//            } else {
//                chapterChestData = userData.getChapterProgressChestsData();
//            }
//
//            if (chapterChestData.getCurrentProgress() < chapterChestData.getProgressMax()) {
//
//                throw new BusinessException("玩家" + dto.getUserUid() + "章节箱子进度不够，无法打开" + chapterChestData);
//            }
//
//            openResult = chestService.openChest(userData, chapterChestData);
//
//            //创建一个新的章节箱子
//            userData.setChapterProgressChestsData(null);
//            chestService.refreshChapterProgressChestData(userData.getUuid());
//            sendToClientData.setChapterProgressChestsData(userData.getChapterProgressChestsData());
//            sendToClientData.setCoin(userData.getCoin());
//            sendToClientData.setDiamond(userData.getDiamond());
//            sendToClientData.setGunCountMap(userData.getGunCountMap());
//            sendToClientData.setGunLevelMap(userData.getGunLevelMap());
//            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
//            log.info("打开章节箱子," + openResult);
//
//            //处理返回结果
//            userDataService.userDataSettlement(userData, sendToClientData);
//            Map<String, Object> map = CommonUtils.responsePrepare(null);
//            chestService.saveChestOpenResult(openResult, dto.getUserUid(), sendToClientData.getUpdateCount());
//
//            map.put("userData", sendToClientData);
//            map.put("openResult", openResult);
//
//            return map;
//        } catch (Exception e) {
//            CommonUtils.responseException(dto, e.toString());
//        }
//
//        return null;
//    }


    @PostMapping("chest-startUnlockChapterWinChest")
    @ApiOperation("开始解锁章节胜利宝箱")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> startUnlockChapterWinChest(@RequestBody StartUnlockDTO request) {

        GameEnvironment.timeMessage.computeIfAbsent("startUnlockChapterWinChest", k -> new ArrayList<>());

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] startUnlockChapterWinChest" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            //处理userData
            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            List<ChapterWinChestData> chapterWinChestsData = userData.getChapterWinChestsData();

            if (chapterWinChestsData == null) {
                throw new BusinessException("玩家" + userData.getUuid() + "没有章节胜利宝箱数据");
            }

            ChapterWinChestData targetChestData = null;
            if (chapterWinChestsData.size() < request.getSlotIndex() + 1 || chapterWinChestsData.get(request.getSlotIndex()) == null) {

                throw new BusinessException("player" + request.getUserUid() + "chapter win chest at index " + request.getSlotIndex() + " is empty");
            } else {
                targetChestData = chapterWinChestsData.get(request.getSlotIndex());
            }

            if (targetChestData.getAvailableUnixTime() >= 0) {
                throw new BusinessException("player" + request.getUserUid() + "chapter win chest at index" + request.getSlotIndex() + " is already start unlocking");
            }

            boolean[] playerVipStatus = userDataService.getPlayerVipStatus(userData);
            boolean isVip = playerVipStatus[0];
            boolean isSVip = playerVipStatus[1];
            boolean isVipV2 = playerVipStatus[2];
            boolean isSVipV2 = playerVipStatus[3];
            boolean isVipV3 = playerVipStatus[4];
            boolean isSVipV3 = playerVipStatus[5];

            long unixTimeNow = TimeUtils.getUnixTimeSecond();
            int unlockingChestCount = 0;
            //如果有另外的宝箱在解锁,无法开始
            for (ChapterWinChestData chestData : chapterWinChestsData) {

                if (chestData == null || chestData == targetChestData) {
                    continue;
                }
                if (chestData.getAvailableUnixTime() >= 0 || chestData.getAvailableUnixTime() > unixTimeNow) {
//                    throw new BusinessException("玩家" + userData.getUuid() + "有另外一个箱子在解锁，无法解锁新的胜利宝箱");
                    unlockingChestCount++;
                }
            }

            //可以同时解锁的数量
            int canUnlockingChestSimultaneousCount = 1;
            if (isVip) {
                canUnlockingChestSimultaneousCount += VipConfig.vipAddUnlockingChapterWinChestSimultaneousCount;
            }
            if (isSVip) {
                canUnlockingChestSimultaneousCount += VipConfig.svipAddUnlockingChapterWinChestSimultaneousCount;
            }
            if (isVipV2) {
                canUnlockingChestSimultaneousCount += VipV2Config.vipAddUnlockingChapterWinChestSimultaneousCount;
            }
            if (isSVipV2) {
                canUnlockingChestSimultaneousCount += VipV2Config.svipAddUnlockingChapterWinChestSimultaneousCount;
            }
            if (isVipV3) {
                canUnlockingChestSimultaneousCount += VipV3Config.vipAddUnlockingChapterWinChestSimultaneousCount;
            }
            if (isSVipV3) {
                canUnlockingChestSimultaneousCount += VipV3Config.svipAddUnlockingChapterWinChestSimultaneousCount;
            }

            log.info("可以同时解锁章节胜利宝箱数量:" + canUnlockingChestSimultaneousCount);
            if (unlockingChestCount >= canUnlockingChestSimultaneousCount) {
                throw new BusinessException("当前已经在解锁中的章节胜利宝箱" + unlockingChestCount + "个，最大能同时解锁" + canUnlockingChestSimultaneousCount + "个，无法解锁新的");
            }

            //计算解锁时间
            if (targetChestData.getCreateTime() + targetChestData.getUnlockSecondsRequires() <= request.getChestAvailableTime()) {

                targetChestData.setAvailableUnixTime(request.getChestAvailableTime());
            } else {
                throw new BusinessException("玩家" + userData.getUuid() + "要解锁宝箱,提供的解锁时间" + request.getChestAvailableTime()
                        + ",比在创建的时候直接解锁时间" + targetChestData.getCreateTime() + targetChestData.getUnlockSecondsRequires() + "还早");
            }

            sendToClientData.setChapterWinChestsData(userData.getChapterWinChestsData());
            log.info("开始解锁章节胜利宝箱,index" + request.getSlotIndex() + ",chest data" + JSONObject.toJSONString(targetChestData));
            //处理返回结果
            userDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
            sendToClientData.setCoin(userData.getCoin());
            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setHistory(userData.getHistory());
            sendToClientData.setChapterWinChestsData(userData.getChapterWinChestsData());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", sendToClientData);
            long needTime = System.currentTimeMillis() - startTime;

            GameEnvironment.timeMessage.get("startUnlockChapterWinChest").add(needTime);
            log.info("[cmd] startUnlockChapterWinChest finish need time" + (System.currentTimeMillis() - startTime));
            return map;

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }


    @PostMapping("chest-openChapterWinChestNow")
    @ApiOperation("立即解锁章节胜利宝箱")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> openChapterWinChestNow(@RequestBody OpenChapterDTO request) {
        GameEnvironment.timeMessage.computeIfAbsent("openChapterWinChestNow", k -> new ArrayList<>());
        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] openChapterWinChestNow" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            //处理userData
            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            ChestOpenResult openResult = new ChestOpenResult();

            List<ChapterWinChestData> chapterWinChestsData = userData.getChapterWinChestsData();
            if (chapterWinChestsData == null) {
                throw new BusinessException("玩家" + userData.getUuid() + "没有章节胜利宝箱数据");
            }

            ChapterWinChestData chestData = null;
            if (chapterWinChestsData.size() < request.getSlotIndex() + 1 || chapterWinChestsData.get(request.getSlotIndex()) == null) {
                throw new BusinessException("player" + request.getUserUid() + "chapter win chest at index " + request.getSlotIndex() + "is empty");
            } else {
                chestData = chapterWinChestsData.get(request.getSlotIndex());
            }

            long unixTimeNow = TimeUtils.getUnixTimeSecond();
            //计算需要多少钻石
            int diamondPrice = 0;
            //已经完成
            if (!(chestData.getAvailableUnixTime() >= 0 && chestData.getAvailableUnixTime() <= unixTimeNow)) {

                //没有开始
                if (chestData.getAvailableUnixTime() < 0) {

                    if (chestData.getUnlockSecondsRequires() <= GameConfig.chapterWinChestFreeUnlockMaxSeconds) {
                        diamondPrice = 0;
                    } else {
                        diamondPrice = (int) Math.round(chestData.getUnlockSecondsRequires() * 5.0 / 3600);
                        diamondPrice = Math.max(1, diamondPrice);
                    }

                } else {
                    //开始但是没有完成
                    long remainedSeconds = chestData.getAvailableUnixTime() - unixTimeNow;
                    if (remainedSeconds <= GameConfig.chapterWinChestFreeUnlockMaxSeconds) {
                        diamondPrice = 0;
                    } else {
                        diamondPrice = (int) Math.round(remainedSeconds * 5.0 / 3600);
                        diamondPrice = Math.max(1, diamondPrice);
                    }

                }
            }

            if (userData.getDiamond() < diamondPrice) {
                throw new BusinessException("玩家" + userData.getUuid() + "解锁箱子的钻石不足. need" + diamondPrice + ", now" + userData.getDiamond());
            }

            //打开箱子
            long tempDiamond = userData.getDiamond() - diamondPrice;
            userData.setDiamond(tempDiamond);

            openResult = chestService.openChest(userData, BeanUtil.copyProperties(chestData, ChestData.class), request.getGameVersion());
            userData.getChapterWinChestsData().set(request.getSlotIndex(), null);

            userData.getChapterWinChestsData().removeIf(Objects::isNull);
            sendToClientData.setChapterWinChestsData(userData.getChapterWinChestsData());
            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
            sendToClientData.setCoin(userData.getCoin());
            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setGunLevelMap(userData.getGunLevelMap());
            sendToClientData.setGunCountMap(userData.getGunCountMap());
            log.info("打开章节胜利宝箱,index" + request.getSlotIndex() + "，chest data" + JSONObject.toJSONString(chestData));
            //处理返回结果

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            chestService.saveChestOpenResult(openResult, request.getUserUid(), userData.getUpdateCount());

            sendToClientData.setHistory(userData.getHistory());
            userDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
            map.put("userData", sendToClientData);
            map.put("openResult", openResult);
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("openChapterWinChestNow").add(needTime);
            log.info("[cmd] openChapterWinChestNow finish need time" + (System.currentTimeMillis() - startTime));
            return map;

        } catch (Exception e) {

            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }

    @PostMapping("chest-openFreeChest")
    @ApiOperation("打开免费箱子")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> openFreeChest(@RequestBody BaseDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] openFreeChest" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            //处理userData
            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            FreeChestData[] freeChestsData = userData.getFreeChestsData();
            if (freeChestsData == null || freeChestsData.length == 0 || freeChestsData[0] == null) {
                throw new BusinessException("玩家" + userData.getUuid() + "没有免费箱子数据");
            }

            FreeChestData firstChestData = freeChestsData[0];
            long unixTimeNow = TimeUtils.getUnixTimeSecond();

            if (!chestService.isFreeChestAvailable(firstChestData)) {
                throw new BusinessException("玩家" + userData.getUuid() + "打开免费箱子,但是时间不满足.箱子" + firstChestData.getAvailableUnixTime() + ",now" + unixTimeNow);
            }

            ChestOpenResult openResult = chestService.openChest(userData, BeanUtil.copyProperties(firstChestData, ChestData.class), request.getGameVersion());
            log.info("打开免费箱子," + JSONObject.toJSONString(openResult));

            freeChestsData[0] = null;

            chestService.refreshFreeChestsData(userData.getUuid());
            sendToClientData.setFreeChestsData(userData.getFreeChestsData());
            sendToClientData.setHistory(userData.getHistory());
            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
            sendToClientData.setCoin(userData.getCoin());
            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setGunLevelMap(userData.getGunLevelMap());
            sendToClientData.setGunCountMap(userData.getGunCountMap());
            //处理返回结果
            userDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            chestService.saveChestOpenResult(openResult, request.getUserUid(), userData.getUpdateCount());
            map.put("userData", sendToClientData);
            map.put("openResult", openResult);
            log.info("[cmd] openFreeChest finish need time" + (System.currentTimeMillis() - startTime));
            return map;

        } catch (Exception e) {

            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }


    @PostMapping("chest-getLatestOpenedChestOpenResult")
    @ApiOperation("获取最近一次开箱的开箱结果")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> getLatestOpenedChestOpenResult(@RequestBody BaseDTO request) {
        GameEnvironment.timeMessage.computeIfAbsent("getLatestOpenedChestOpenResult", k -> new ArrayList<>());
        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] getLatestOpenedChestOpenResult" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
            UserData userData = null;

            //处理userData
            userDataService.checkUserDataExist(request.getUserUid());
            userData = GameEnvironment.userDataMap.get(request.getUserUid());

            String collectionPath = Path.getPlayerChestOpenResultDocPath(request.getUserUid());

            ArchiveChestOpenResult archiveChestOpenResult = chestService.getLatestOpenedChestOpenResult(collectionPath);
            if (archiveChestOpenResult == null) {
                throw new BusinessException("没有任何开箱信息");
            }

            userData.getChapterWinChestsData().removeIf(Objects::isNull);
            log.info("获取最近一次宝箱开启结果");
            userDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);

            map.put("chestOpenResult", archiveChestOpenResult.getOpenResult());
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("getLatestOpenedChestOpenResult").add(needTime);
            log.info("cmd getLatestOpenedChestOpenResult finish need time" + (System.currentTimeMillis() - startTime));
            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }

    @PostMapping("chest-accelerateChapterWinChestUnlock")
    @ApiOperation("章节胜利宝箱加速")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> accelerateChapterWinChestUnlock(@RequestBody OpenChapterDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            if (userData.getAdvertisementData().getRemainedRewardAdCountToday() <= 0) {
                throw new BackingStoreException("今日广告次数不足");
            }

            Integer rewardAdCountToday = userData.getAdvertisementData().getRemainedRewardAdCountToday();
            userData.getAdvertisementData().setRemainedRewardAdCountToday(--rewardAdCountToday);
            userDataSendToClient.setAdvertisementData(userData.getAdvertisementData());

            List<ChapterWinChestData> chapterWinChestsData = userData.getChapterWinChestsData();
            ChapterWinChestData targetChestData = chapterWinChestsData.get(request.getSlotIndex());
            if (targetChestData == null) {
                throw new BusinessException("没有章节胜利宝箱在slot" + request.getSlotIndex());
            }

            if (targetChestData.getAvailableUnixTime() < 0) {

                throw new BusinessException("章节胜利宝箱slot" + request.getSlotIndex() + "还没开始解锁");
            }

            Long unixTimeNow = TimeUtils.getUnixTimeSecond();
            Long serverAvailableUnixTimeAfterAccelerate = Math.max(unixTimeNow, targetChestData.getAvailableUnixTime() - GameConfig.adAccelerateChapterWinChestUnlockSeconds);

            if (Math.abs(serverAvailableUnixTimeAfterAccelerate - request.getAvailableUnixTimeAfterAccelerate()) > 60) {
                throw new BusinessException("服务器计算出的解锁时间" + serverAvailableUnixTimeAfterAccelerate + "和客户端计算的解锁时间相差过大" + request.getAvailableUnixTimeAfterAccelerate());
            }

            targetChestData.setAvailableUnixTime(request.getAvailableUnixTimeAfterAccelerate());
            log.info("加速章节胜利宝箱。slot" + request.getSlotIndex() + "," + JSONObject.toJSONString(targetChestData));

            userDataSendToClient.setChapterWinChestsData(userData.getChapterWinChestsData());
            userDataService.userDataSettlement(userData, userDataSendToClient, true, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", userDataSendToClient);

            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());

        } finally {
            ThreadLocalUtil.remove();
        }

        return null;
    }
}
