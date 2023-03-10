package org.skynet.service.provider.hunting.obsolete.controller.game;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.rank.league.query.GetRankAdditionQuery;
import org.skynet.components.hunting.rank.league.service.RankLeagueFeignService;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.domain.ChapterWinChestData;
import org.skynet.components.hunting.user.domain.ChestData;
import org.skynet.components.hunting.user.domain.FreeChestData;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.BackingStoreException;

@Api(tags = "????????????????????????")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class ChestController {

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private ChestService chestService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private RankLeagueFeignService rankLeagueFeignService;

//    @GetMapping("open/progress")
//    @ApiOperation("??????????????????")
//    public Map<String, Object> openChapterProgressChest(@RequestBody BaseDTO dto) {
//
//        try {
//            CommonUtils.requestProcess(dto, null,systemPropertiesConfig.getSupportRecordModeClient());
//
//            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
//            UserData userData = null;
//
//            //??????userData
//            userDataService.checkUserDataExist(dto.getUserUid());
//            userData = GameEnvironment.userDataMap.get(dto.getUserUid());
//
//            ChestOpenResult openResult = new ChestOpenResult();
//            ChapterProgressChestData chapterChestData = null;
//            if (userData.getChapterProgressChestsData() == null) {
//                throw new BusinessException("??????????????????????????????");
//            } else {
//                chapterChestData = userData.getChapterProgressChestsData();
//            }
//
//            if (chapterChestData.getCurrentProgress() < chapterChestData.getProgressMax()) {
//
//                throw new BusinessException("??????" + dto.getUserUid() + "???????????????????????????????????????" + chapterChestData);
//            }
//
//            openResult = chestService.openChest(userData, chapterChestData);
//
//            //??????????????????????????????
//            userData.setChapterProgressChestsData(null);
//            chestService.refreshChapterProgressChestData(userData.getUuid());
//            sendToClientData.setChapterProgressChestsData(userData.getChapterProgressChestsData());
//            sendToClientData.setCoin(userData.getCoin());
//            sendToClientData.setDiamond(userData.getDiamond());
//            sendToClientData.setGunCountMap(userData.getGunCountMap());
//            sendToClientData.setGunLevelMap(userData.getGunLevelMap());
//            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
//            log.info("??????????????????," + openResult);
//
//            //??????????????????
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
    @ApiOperation("??????????????????????????????")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> startUnlockChapterWinChest(@RequestBody StartUnlockDTO request) {

        GameEnvironment.timeMessage.computeIfAbsent("startUnlockChapterWinChest", k -> new ArrayList<>());

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] startUnlockChapterWinChest" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            //??????userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            List<ChapterWinChestData> chapterWinChestsData = userData.getChapterWinChestsData();

            if (chapterWinChestsData == null) {
                throw new BusinessException("??????" + userData.getUuid() + "??????????????????????????????");
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

            boolean[] playerVipStatus = obsoleteUserDataService.getPlayerVipStatus(userData);
            boolean isVip = playerVipStatus[0];
            boolean isSVip = playerVipStatus[1];
            boolean isVipV2 = playerVipStatus[2];
            boolean isSVipV2 = playerVipStatus[3];
            boolean isVipV3 = playerVipStatus[4];
            boolean isSVipV3 = playerVipStatus[5];

            long unixTimeNow = TimeUtils.getUnixTimeSecond();
            int unlockingChestCount = 0;
            //?????????????????????????????????,????????????
            for (ChapterWinChestData chestData : chapterWinChestsData) {

                if (chestData == null || chestData == targetChestData) {
                    continue;
                }
                if (chestData.getAvailableUnixTime() >= 0 || chestData.getAvailableUnixTime() > unixTimeNow) {
//                    throw new BusinessException("??????" + userData.getUuid() + "???????????????????????????????????????????????????????????????");
                    unlockingChestCount++;
                }
            }

            //???????????????????????????
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

            log.info("??????????????????????????????????????????:" + canUnlockingChestSimultaneousCount);
            if (unlockingChestCount >= canUnlockingChestSimultaneousCount) {
                throw new BusinessException("?????????????????????????????????????????????" + unlockingChestCount + "???????????????????????????" + canUnlockingChestSimultaneousCount + "????????????????????????");
            }

            //??????????????????
            if (targetChestData.getCreateTime() + targetChestData.getUnlockSecondsRequires() <= request.getChestAvailableTime()) {

                targetChestData.setAvailableUnixTime(request.getChestAvailableTime());
            } else {
                throw new BusinessException("??????" + userData.getUuid() + "???????????????,?????????????????????" + request.getChestAvailableTime()
                        + ",???????????????????????????????????????" + targetChestData.getCreateTime() + targetChestData.getUnlockSecondsRequires() + "??????");
            }

            sendToClientData.setChapterWinChestsData(userData.getChapterWinChestsData());
            log.info("??????????????????????????????,index" + request.getSlotIndex() + ",chest data" + JSONObject.toJSONString(targetChestData));
            //??????????????????
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
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
    @ApiOperation("??????????????????????????????")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> openChapterWinChestNow(@RequestBody OpenChapterDTO request) {
        GameEnvironment.timeMessage.computeIfAbsent("openChapterWinChestNow", k -> new ArrayList<>());
        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] openChapterWinChestNow" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            //??????userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            Result<Float> rankAddition = rankLeagueFeignService.getRankAddition(GetRankAdditionQuery.builder().userId(request.getUserUid()).build());
            float additionValue = rankAddition.getData();

            ChestOpenResult openResult = new ChestOpenResult();

            List<ChapterWinChestData> chapterWinChestsData = userData.getChapterWinChestsData();
            if (chapterWinChestsData == null) {
                throw new BusinessException("??????" + userData.getUuid() + "??????????????????????????????");
            }

            ChapterWinChestData chestData = null;
            if (chapterWinChestsData.size() < request.getSlotIndex() + 1 || chapterWinChestsData.get(request.getSlotIndex()) == null) {
                throw new BusinessException("player" + request.getUserUid() + "chapter win chest at index " + request.getSlotIndex() + "is empty");
            } else {
                chestData = chapterWinChestsData.get(request.getSlotIndex());
            }

            long unixTimeNow = TimeUtils.getUnixTimeSecond();
            //????????????????????????
            int diamondPrice = 0;
            //????????????
            if (!(chestData.getAvailableUnixTime() >= 0 && chestData.getAvailableUnixTime() <= unixTimeNow)) {

                //????????????
                if (chestData.getAvailableUnixTime() < 0) {

                    if (chestData.getUnlockSecondsRequires() <= GameConfig.chapterWinChestFreeUnlockMaxSeconds) {
                        diamondPrice = 0;
                    } else {
                        diamondPrice = (int) Math.round(chestData.getUnlockSecondsRequires() * 5.0 / 3600);
                        diamondPrice = Math.max(1, diamondPrice);
                    }

                } else {
                    //????????????????????????
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
                throw new BusinessException("??????" + userData.getUuid() + "???????????????????????????. need" + diamondPrice + ", now" + userData.getDiamond());
            }

            //????????????
            long tempDiamond = userData.getDiamond() - diamondPrice;
            userData.setDiamond(tempDiamond);

            openResult = chestService.openChest(userData, BeanUtil.copyProperties(chestData, ChestData.class), request.getGameVersion(), additionValue);
            userData.getChapterWinChestsData().set(request.getSlotIndex(), null);

            userData.getChapterWinChestsData().removeIf(Objects::isNull);
            sendToClientData.setChapterWinChestsData(userData.getChapterWinChestsData());
            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
            sendToClientData.setCoin(userData.getCoin());
            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setGunLevelMap(userData.getGunLevelMap());
            sendToClientData.setGunCountMap(userData.getGunCountMap());
            log.info("????????????????????????,index" + request.getSlotIndex() + "???chest data" + JSONObject.toJSONString(chestData));
            //??????????????????

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            chestService.saveChestOpenResult(openResult, request.getUserUid(), userData.getUpdateCount());

            sendToClientData.setHistory(userData.getHistory());
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
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
    @ApiOperation("??????????????????")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> openFreeChest(@RequestBody BaseDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] openFreeChest" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            //??????userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            Result<Float> rankAddition = rankLeagueFeignService.getRankAddition(GetRankAdditionQuery.builder().userId(request.getUserUid()).build());
            float additionValue = rankAddition.getData();

            FreeChestData[] freeChestsData = userData.getFreeChestsData();
            if (freeChestsData == null || freeChestsData.length == 0 || freeChestsData[0] == null) {
                throw new BusinessException("??????" + userData.getUuid() + "????????????????????????");
            }

            FreeChestData firstChestData = freeChestsData[0];
            long unixTimeNow = TimeUtils.getUnixTimeSecond();

            if (!chestService.isFreeChestAvailable(firstChestData)) {
                throw new BusinessException("??????" + userData.getUuid() + "??????????????????,?????????????????????.??????" + firstChestData.getAvailableUnixTime() + ",now" + unixTimeNow);
            }

            ChestOpenResult openResult = chestService.openChest(userData, BeanUtil.copyProperties(firstChestData, ChestData.class), request.getGameVersion(), additionValue);
            log.info("??????????????????," + JSONObject.toJSONString(openResult));

            freeChestsData[0] = null;

            chestService.refreshFreeChestsData(userData.getUuid());
            sendToClientData.setFreeChestsData(userData.getFreeChestsData());
            sendToClientData.setHistory(userData.getHistory());
            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
            sendToClientData.setCoin(userData.getCoin());
            sendToClientData.setDiamond(userData.getDiamond());
            sendToClientData.setGunLevelMap(userData.getGunLevelMap());
            sendToClientData.setGunCountMap(userData.getGunCountMap());
            //??????????????????
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
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
    @ApiOperation("???????????????????????????????????????")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> getLatestOpenedChestOpenResult(@RequestBody BaseDTO request) {
        GameEnvironment.timeMessage.computeIfAbsent("getLatestOpenedChestOpenResult", k -> new ArrayList<>());
        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] getLatestOpenedChestOpenResult" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
            UserData userData = null;

            //??????userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            userData = GameEnvironment.userDataMap.get(request.getUserUid());

            String collectionPath = Path.getPlayerChestOpenResultDocPath(request.getUserUid());

            ArchiveChestOpenResult archiveChestOpenResult = chestService.getLatestOpenedChestOpenResult(collectionPath);
            if (archiveChestOpenResult == null) {
                throw new BusinessException("????????????????????????");
            }

            userData.getChapterWinChestsData().removeIf(Objects::isNull);
            log.info("????????????????????????????????????");
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
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
    @ApiOperation("????????????????????????")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> accelerateChapterWinChestUnlock(@RequestBody OpenChapterDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            obsoleteUserDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            if (userData.getAdvertisementData().getRemainedRewardAdCountToday() <= 0) {
                throw new BackingStoreException("????????????????????????");
            }

            Integer rewardAdCountToday = userData.getAdvertisementData().getRemainedRewardAdCountToday();
            userData.getAdvertisementData().setRemainedRewardAdCountToday(--rewardAdCountToday);
            userDataSendToClient.setAdvertisementData(userData.getAdvertisementData());

            List<ChapterWinChestData> chapterWinChestsData = userData.getChapterWinChestsData();
            ChapterWinChestData targetChestData = chapterWinChestsData.get(request.getSlotIndex());
            if (targetChestData == null) {
                throw new BusinessException("???????????????????????????slot" + request.getSlotIndex());
            }

            if (targetChestData.getAvailableUnixTime() < 0) {

                throw new BusinessException("??????????????????slot" + request.getSlotIndex() + "??????????????????");
            }

            Long unixTimeNow = TimeUtils.getUnixTimeSecond();
            Long serverAvailableUnixTimeAfterAccelerate = Math.max(unixTimeNow, targetChestData.getAvailableUnixTime() - GameConfig.adAccelerateChapterWinChestUnlockSeconds);

            if (Math.abs(serverAvailableUnixTimeAfterAccelerate - request.getAvailableUnixTimeAfterAccelerate()) > 60) {
                throw new BusinessException("?????????????????????????????????" + serverAvailableUnixTimeAfterAccelerate + "?????????????????????????????????????????????" + request.getAvailableUnixTimeAfterAccelerate());
            }

            targetChestData.setAvailableUnixTime(request.getAvailableUnixTimeAfterAccelerate());
            log.info("???????????????????????????slot" + request.getSlotIndex() + "," + JSONObject.toJSONString(targetChestData));

            userDataSendToClient.setChapterWinChestsData(userData.getChapterWinChestsData());
            obsoleteUserDataService.userDataSettlement(userData, userDataSendToClient, true, request.getGameVersion());
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
