package org.skynet.service.provider.hunting.obsolete.controller.game;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.HttpUtil;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.GameConfig;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.config.VipV2Config;
import org.skynet.service.provider.hunting.obsolete.module.dto.BaseDTO;
import org.skynet.service.provider.hunting.obsolete.module.dto.UserInfoDto;
import org.skynet.service.provider.hunting.obsolete.enums.ABTestGroup;
import org.skynet.service.provider.hunting.obsolete.enums.ClientGameVersion;
import org.skynet.service.provider.hunting.obsolete.enums.PlatformName;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.module.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.InitUserDataBO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.ChangeNameDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.LoginDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.LuckyWheelV2PropertyTableValue;
import org.skynet.service.provider.hunting.obsolete.pojo.table.RecordModeMatchTableValue;
import org.skynet.service.provider.hunting.obsolete.service.HuntingMatchService;
import org.skynet.service.provider.hunting.obsolete.service.IAPService;
import org.skynet.service.provider.hunting.obsolete.service.SigninDiamondRewardTableService;
import org.skynet.service.provider.hunting.obsolete.service.UserDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Api(tags = "玩家相关")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
@CrossOrigin
public class UserController {

    @Resource
    private HuntingMatchService huntingMatchService;

    @Resource
    private UserDataService userDataService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private SigninDiamondRewardTableService signinDiamondRewardTableService;

    @Resource
    private IAPService iapService;

//    @PostMapping("/login")
//    @ApiOperation("玩家登录")
//    public Map<String,Object> login(@RequestBody LoginDTO loginDTO){
//
//        String loginUserUid = null;
//        try{
//            GameEnvironment.timeMessage.computeIfAbsent("login", k -> new ArrayList<>());
//            ThreadLocalUtil.set(loginDTO.getServerTimeOffset());
//            log.warn("客户端请求时间偏移单位：{}",loginDTO.getServerTimeOffset());
//            log.warn("threadLocal中的时间偏移单位：{}",ThreadLocalUtil.localVar.get());
//            long startTime = System.currentTimeMillis();
//            log.info("[cmd] login"+System.currentTimeMillis());
//
//            userDataService.createLoginSessionData(loginDTO);
//
//            CommonUtils.requestProcess(loginDTO,false,systemPropertiesConfig.getSupportRecordModeClient());
//
//
//
//
//            //获取请求信息中的内容
//            UserData newUserData = null;
//            String privateKey = loginDTO.getPrivateKey();
//            boolean isNewUser = false;
//
//            UserData loginUserData = null;
//            if (StringUtils.isEmpty(loginDTO.getUserUid())){
//                log.warn("开始创建新用户");
//                //新用户
//                isNewUser = true;
//                newUserData = userDataService.createNewPlayer(loginDTO.getGameVersion());
//                if (newUserData != null){
//                    log.warn("新用户创建成功，用户信息为：{}", JSONUtil.toJsonStr(newUserData));
//                }else {
//                    log.warn("创建的用户数据为空");
//                }
//                privateKey = newUserData.getServerOnly().getPrivateKey();
//                loginUserData = newUserData;
//                //加载到登录用户环境中
//                GameEnvironment.userDataMap.put(newUserData.getUuid(),newUserData);
//            }else {
//                log.warn("从redis中加载用户");
//                // 从redis中加载用户
//                //判断该玩家是否在线,在线则抛出异常，不执行登录操作
//                synchronized (GameEnvironment.userDataMap) {
//                    if (GameEnvironment.userDataMap.containsKey(loginDTO.getUserUid())){
//                        throw new BusinessException("该玩家已经在线");
//                    }
//                }
//                userDataService.checkUserDataExist(loginDTO.getUserUid());
//                loginUserData = GameEnvironment.userDataMap.get(loginDTO.getUserUid());
//
//            }
//
//            double giftPackagePopUpRecommendPrice = 0;
//
//            loginUserUid = newUserData == null? loginDTO.getUserUid():newUserData.getUuid();
//
//            String userToken = null;
//
//            InitUserDataBO initUserDataBO = userDataService.initUserData(loginUserData, privateKey, loginUserUid, loginDTO);
//
//            loginUserData = initUserDataBO.getUserData();
//
//            userToken = initUserDataBO.getToken();
//
//            System.out.println("userToken:"+userToken);
//
//            giftPackagePopUpRecommendPrice = iapService.getGiftPackagePopUpPriceRecommendPrice(loginUserData);
//
//            userDataService.userDataTransaction( loginUserData,false,loginDTO.getGameVersion());
//            loginUserData.getServerOnly().setLastLoginClientVersion(loginDTO.getGameVersion());
//            GameEnvironment.userDataMap.remove(loginUserUid);
//
//            boolean disableClientHuntingMatchReport = GameConfig.disableClientHuntingMatchReport;
//            log.info("用户"+loginUserUid+"登录,token："+userToken+", time"+ TimeUtils.getUnixTimeSecond());
//
//            //确认新版本
//            ClientGameVersion latestClientGameVersion = GameConfig.latestClientGameVersion_Android;
//
//            LuckyWheelV2PropertyTableValue luckyWheelV2PropertyTable = GameEnvironment.luckyWheelV2PropertyTableMap.get(loginDTO.getGameVersion());
//
//            Integer newRequestId = userDataService.getUserMaxRequestIdNow(loginUserUid);
//
//            if (loginDTO.getPlatform().equals(PlatformName.IOS.getPlatform())){
//
//                latestClientGameVersion = GameConfig.latestClientGameVersion_IOS;
//
//            }
//
//
////            if (loginUserData.getLuckyWheelV2Data().getFreeSpinCount() <= 0){
////                loginUserData.getLuckyWheelV2Data().setNextFreeSpinUnixTime(TimeUtils.getUnixTimeSecond() + 1000);
////            }
//            if (loginUserData.getLuckyWheelV2Data().getNextFreeSpinUnixTime() <= 0){
//                loginUserData.getLuckyWheelV2Data().setNextFreeSpinUnixTime(TimeUtils.getUnixTimeSecond() + 1000);
//            }
//
//            loginUserData.getChapterWinChestsData().removeIf(Objects::isNull);
//
//
//            //返回内容
//            String latestClientVersion = ClientGameVersion.clientGameVersionEnumToString(latestClientGameVersion);
//            ABTestGroup resultGroup = loginUserData.getServerOnly().getAbTestGroup();
//            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
//            userDataSendToClient.setAvailableFifthDayGunGiftPackageData(loginUserData.getAvailableFifthDayGunGiftPackageData());
//            userDataSendToClient.setAvailableGunGiftPackageData(loginUserData.getAvailableGunGiftPackageData());
//            userDataSendToClient.setAvailableBulletGiftPackageData(loginUserData.getAvailableBulletGiftPackageData());
//            BeanUtils.copyProperties(loginUserData,userDataSendToClient);
//            userDataSendToClient.setLuckyWheelV2Data(loginUserData.getLuckyWheelV2Data());
//            userDataSendToClient.setChapterWinChestsData(loginUserData.getChapterWinChestsData());
////            userDataSendToClient.getHistory().setServer_only_matchTotalShots(null);
////            userDataSendToClient.getHistory().setServer_only_matchAllShotsPrecisionAccumulation(null);
////            userDataSendToClient.getAdvertisementData().setServer_only_lastRefreshRewardAdCountUnixDay(null);
////            userDataSendToClient.getVipData().setServer_only_lastRefreshLuckyWheelSVipSpinStandardTimeDay(null);
////            userDataSendToClient.getVipData().setServer_only_lastClearLuckyWheelVipSpinCountStandardTimeDay(null);
////            userDataSendToClient.getVipData().setServer_only_lastRefreshLuckyWheelVipSpinStandardTimeDay(null);
//            History history = userDataSendToClient.getHistory();
//            PlayerAdvertisementData advertisementData = userDataSendToClient.getAdvertisementData();
//            PlayerVipData vipData = userDataSendToClient.getVipData();
//            CommonUtils.responseRemoveServer(history);
//            CommonUtils.responseRemoveServer(advertisementData);
//            CommonUtils.responseRemoveServer(vipData);
//
//
//
//
//            Map<String, Object> map = CommonUtils.responsePrepare(null);
//            log.warn("下发给服务器的时间：{}",map.get("serverTime"));
//            map.put("userData",userDataSendToClient);
//            map.put("userToken",userToken);
//            map.put("requestId",newRequestId);
//            map.put("abTestGroup",resultGroup.getStatus());
//            map.put("disableClientHuntingMatchReport",disableClientHuntingMatchReport);
//            map.put("latestClientGameVersion",latestClientVersion);
//            map.put("standardTimeOffset",GameConfig.standardTimeZoneOffset);
////            if (TimeUtils.getUnixTimeSecond()-loginUserData.getSignUpTime()>=345600){
////
////            }
//            map.put("vipV2FunctionUnlockDay",VipV2Config.unlockVipFunctionAfterSignUpDayCount);
//            map.put("luckyWheelV2FunctionUnlockDay",luckyWheelV2PropertyTable.getFunctionEnableDayFromSignUp());
//            //礼包弹出推荐价格
//            map.put("giftPackagePopUpRecommendPrice", giftPackagePopUpRecommendPrice);
//
//            if (isNewUser){
//                map.put("privateKey",privateKey);
//            }
//
//            userDataService.updateSessionToken(loginUserData, userToken, loginDTO.getRequestRandomId());
//
//            long needTime = System.currentTimeMillis() - startTime;
//            GameEnvironment.timeMessage.get("login").add(needTime);
//            log.info("[cmd] login finish need time"+(needTime));
//            return map;
//
//        } catch (Exception e){
//            e.printStackTrace();
//            log.error("登录出错：" + e);
//            CommonUtils.responseException(loginDTO,e,loginDTO.getUserUid());
//        }finally {
//            ThreadLocalUtil.remove();
//        }
//        return null;
//    }

    @PostMapping("/login")
    @ApiOperation("玩家登录")
    public Map<String, Object> login(@RequestBody LoginDTO loginDTO) {
        String loginUserUid = null;
        try {
            String fightUrl = systemPropertiesConfig.getFightUrl();
            GameEnvironment.timeMessage.computeIfAbsent("login", k -> new ArrayList<>());
            ThreadLocalUtil.set(loginDTO.getServerTimeOffset());
            log.warn("客户端请求时间偏移单位：{}", loginDTO.getServerTimeOffset());
            log.warn("threadLocal中的时间偏移单位：{}", ThreadLocalUtil.localVar.get());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] login" + System.currentTimeMillis());

            userDataService.createLoginSessionData(loginDTO);

            CommonUtils.requestProcess(loginDTO, false, systemPropertiesConfig.getSupportRecordModeClient());


            //获取请求信息中的内容
            UserData newUserData = null;
            String privateKey = loginDTO.getPrivateKey();
            boolean isNewUser = false;

            UserData loginUserData = null;
            if (StringUtils.isEmpty(loginDTO.getUserUid())) {
                log.warn("开始创建新用户");
                //新用户
                isNewUser = true;
                newUserData = userDataService.createNewPlayer(loginDTO.getGameVersion());
                if (newUserData != null) {
                    log.warn("新用户创建成功，用户信息为：{}", JSONUtil.toJsonStr(newUserData));
                } else {
                    log.warn("创建的用户数据为空");
                }
                privateKey = newUserData.getServerOnly().getPrivateKey();
                loginUserData = newUserData;
                //加载到登录用户环境中
                GameEnvironment.userDataMap.put(newUserData.getUuid(), newUserData);
            } else {
                log.warn("从redis中加载用户");
                // 从redis中加载用户
                //判断该玩家是否在线,在线则抛出异常，不执行登录操作
                // synchronized (GameEnvironment.userDataMap) {
                //     if (GameEnvironment.userDataMap.containsKey(loginDTO.getUserUid())) {
                //         throw new BusinessException("该玩家已经在线");
                //     }
                // }
                userDataService.checkUserDataExist(loginDTO.getUserUid());
                loginUserData = GameEnvironment.userDataMap.get(loginDTO.getUserUid());

            }

            double giftPackagePopUpRecommendPrice = 0;

            loginUserUid = newUserData == null ? loginDTO.getUserUid() : newUserData.getUuid();

            String userToken = null;

            InitUserDataBO initUserDataBO = userDataService.initUserData(loginUserData, privateKey, loginUserUid, loginDTO);

            loginUserData = initUserDataBO.getUserData();

            userToken = initUserDataBO.getToken();

            System.out.println("userToken:" + userToken);

            giftPackagePopUpRecommendPrice = iapService.getGiftPackagePopUpPriceRecommendPrice(loginUserData);

            userDataService.userDataTransaction(loginUserData, false, loginDTO.getGameVersion());
            loginUserData.getServerOnly().setLastLoginClientVersion(loginDTO.getGameVersion());
            GameEnvironment.userDataMap.remove(loginUserUid);

            boolean disableClientHuntingMatchReport = GameConfig.disableClientHuntingMatchReport;
            log.info("用户" + loginUserUid + "登录,token：" + userToken + ", time" + TimeUtils.getUnixTimeSecond());

            //确认新版本
            ClientGameVersion latestClientGameVersion = GameConfig.latestClientGameVersion_Android;

            LuckyWheelV2PropertyTableValue luckyWheelV2PropertyTable = GameEnvironment.luckyWheelV2PropertyTableMap.get(loginDTO.getGameVersion());

            Integer newRequestId = userDataService.getUserMaxRequestIdNow(loginUserUid);

            if (loginDTO.getPlatform().equals(PlatformName.IOS.getPlatform())) {

                latestClientGameVersion = GameConfig.latestClientGameVersion_IOS;

            }


//            if (loginUserData.getLuckyWheelV2Data().getFreeSpinCount() <= 0){
//                loginUserData.getLuckyWheelV2Data().setNextFreeSpinUnixTime(TimeUtils.getUnixTimeSecond() + 1000);
//            }
            if (loginUserData.getLuckyWheelV2Data().getNextFreeSpinUnixTime() <= 0) {
                loginUserData.getLuckyWheelV2Data().setNextFreeSpinUnixTime(TimeUtils.getUnixTimeSecond() + 1000);
            }

            loginUserData.getChapterWinChestsData().removeIf(Objects::isNull);

            GameEnvironment.onlineUser.put(loginUserData.getUuid(), new Date());
            //如果是老用户登录，可能不存在这个字段，当该字段为空的时候，设只false
            if (loginUserData.getIsCreateBattleInfo() == null) {
                loginUserData.setIsCreateBattleInfo(false);
            }

            //初始化vip3
            if (loginUserData.getVipV3Data() == null) {
                loginUserData.setVipV3Data(new PlayerVipV3Data(-1L, -1L, -1L, -1L));
                RedisDBOperation.insertUserData(loginUserData);
            }


//            //向战斗服发消息，创建用户战斗信息
            if (!loginUserData.getIsCreateBattleInfo()) {
                UserInfoDto createUserInfo = getCreateUserInfo(loginUserData, loginDTO.getGameVersion());
                log.info("战斗服地址：{}", fightUrl + "/user/create");
                Map<String, Object> fightInfo = HttpUtil.getFightInfo(fightUrl + "/user/create", createUserInfo);
                if (fightInfo == null || (int) fightInfo.get("code") != 0) {
                    log.error("创建用户战斗信息失败");
                } else {
                    loginUserData.setIsCreateBattleInfo(true);
                    RedisDBOperation.insertUserData(loginUserData);
                }
            }


//            http://192.168.2.199:9301/user/online   localhost
            log.info("战斗服地址：{}", fightUrl + "/user/online");
            HttpUtil.getFightInfo(fightUrl + "/user/online", new BaseDTO(loginDTO.getGameVersion(), loginUserData.getUuid()));
//
////
            log.info("登录时的用户数据：{}", loginUserData);

            //返回内容
            String latestClientVersion = ClientGameVersion.clientGameVersionEnumToString(latestClientGameVersion);
            ABTestGroup resultGroup = loginUserData.getServerOnly().getAbTestGroup();
            UserDataSendToClient userDataSendToClient = GameEnvironment.prepareSendToClientUserData();
            //todo 玩家段位系统
//            userDataSendToClient.setHaveNotObtainRankRewardChest(loginUserData.getHaveNotObtainRankRewardChest());

            userDataSendToClient.setAvailableFifthDayGunGiftPackageData(loginUserData.getAvailableFifthDayGunGiftPackageData());
            userDataSendToClient.setAvailableGunGiftPackageData(loginUserData.getAvailableGunGiftPackageData());
            userDataSendToClient.setAvailableBulletGiftPackageData(loginUserData.getAvailableBulletGiftPackageData());
            BeanUtils.copyProperties(loginUserData, userDataSendToClient);
            userDataSendToClient.setLuckyWheelV2Data(loginUserData.getLuckyWheelV2Data());
            userDataSendToClient.setChapterWinChestsData(loginUserData.getChapterWinChestsData());
            userDataSendToClient.setPromotionGiftPackagesV2Data(loginUserData.getPromotionGiftPackagesV2Data());
            userDataSendToClient.setPlayerRankData(loginUserData.getPlayerRankData());
//            userDataSendToClient.getHistory().setServer_only_matchTotalShots(null);
//            userDataSendToClient.getHistory().setServer_only_matchAllShotsPrecisionAccumulation(null);
//            userDataSendToClient.getAdvertisementData().setServer_only_lastRefreshRewardAdCountUnixDay(null);
//            userDataSendToClient.getVipData().setServer_only_lastRefreshLuckyWheelSVipSpinStandardTimeDay(null);
//            userDataSendToClient.getVipData().setServer_only_lastClearLuckyWheelVipSpinCountStandardTimeDay(null);
//            userDataSendToClient.getVipData().setServer_only_lastRefreshLuckyWheelVipSpinStandardTimeDay(null);
            History history = userDataSendToClient.getHistory();
            PlayerAdvertisementData advertisementData = userDataSendToClient.getAdvertisementData();
            PlayerVipData vipData = userDataSendToClient.getVipData();
            CommonUtils.responseRemoveServer(history);
            CommonUtils.responseRemoveServer(advertisementData);
            CommonUtils.responseRemoveServer(vipData);


            Map<String, Object> map = CommonUtils.responsePrepare(null);
            log.warn("下发给服务器的时间：{}", map.get("serverTime"));
            map.put("userData", userDataSendToClient);
            map.put("userToken", userToken);
            map.put("requestId", newRequestId);
            map.put("abTestGroup", resultGroup.getStatus());
            map.put("disableClientHuntingMatchReport", disableClientHuntingMatchReport);
            map.put("latestClientGameVersion", latestClientVersion);
            map.put("standardTimeOffset", GameConfig.standardTimeZoneOffset);
//            if (TimeUtils.getUnixTimeSecond()-loginUserData.getSignUpTime()>=345600){
//
//            }
            map.put("vipV2FunctionUnlockDay", VipV2Config.unlockVipFunctionAfterSignUpDayCount);
            map.put("luckyWheelV2FunctionUnlockDay", luckyWheelV2PropertyTable.getFunctionEnableDayFromSignUp());
            //礼包弹出推荐价格
            map.put("giftPackagePopUpRecommendPrice", giftPackagePopUpRecommendPrice);

            //todo 玩家段位系统,是否领取奖励
//            map.put("haveNotObtainRankRewardChest",loginUserData.getHaveNotObtainRankRewardChest());
//            if (rankInfo != null){
//                map.put("playerRankData",rankInfo.get("data"));
//            }


            if (isNewUser) {
                map.put("privateKey", privateKey);
            }

            userDataService.updateSessionToken(loginUserData, userToken, loginDTO.getRequestRandomId());

            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("login").add(needTime);
            log.info("[cmd] login finish need time" + (needTime));

            // TODO 处理缺省值的错误
            String sv = JSON.toJSONString(map, SerializerFeature.WriteMapNullValue);
            String siv = JSON.toJSONString(map);
            if (sv.length() != siv.length()) {
                map = JSON.parseObject(siv);
                log.warn("缺省值异常:{}", siv);
            }
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("登录出错：" + e);
            CommonUtils.responseException(loginDTO, e, loginDTO.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }


    public UserInfoDto getCreateUserInfo(UserData userData, String gameVersion) {
        UserInfoDto userInfoDto = new UserInfoDto();
        UserInfo userInfo = new UserInfo();


        //计算胜率，生成战斗信息
        int totalGameCount = 0;
        for (Map.Entry<Integer, Integer> integerIntegerEntry : userData.getChapterEnteredCountMap().entrySet()) {
            totalGameCount += integerIntegerEntry.getValue();
        }
        int winGameCount = 0;
        for (Map.Entry<Integer, Integer> integerIntegerEntry : userData.getChapterWinCountMap().entrySet()) {
            winGameCount += integerIntegerEntry.getValue();
        }
        BattleInfo battleInfo = new BattleInfo
                (userData.getTrophy(),
                        userData.getHistory().getCurrentMatchWinStreak(),
                        userData.getHistory().getCurrentMatchLoseStreak(),
                        winGameCount * 1.0 / totalGameCount,
                        userData.getHistory().getMatchAverageHitPrecision(),
                        0, winGameCount, totalGameCount - winGameCount, totalGameCount
                );
        userInfo.setBattleInfo(battleInfo);

        //生成chapterInfo
        ChapterInfo chapterInfo = new ChapterInfo();
        Map<String, ChapterBattleInfo> chapterBattleInfos = new HashMap<>();
        for (Map.Entry<Integer, Integer> integerIntegerEntry : userData.getChapterEnteredCountMap().entrySet()) {
            ChapterBattleInfo chapterBattleInfo = new ChapterBattleInfo();
            List<Integer> playerChapterLatestMatchScore = huntingMatchService.getPlayerChapterLatestMatchScore(integerIntegerEntry.getKey(), userData.getUuid(), gameVersion);
            chapterBattleInfo.setLastFiveScores(playerChapterLatestMatchScore);
            chapterBattleInfo.setBattleCount(integerIntegerEntry.getValue());
            chapterBattleInfos.put(integerIntegerEntry.getKey().toString(), chapterBattleInfo);
        }
        chapterInfo.setChapterBattleInfos(chapterBattleInfos);
        userInfo.setChapterInfo(chapterInfo);

        //生成recordInfo
        RecordInfo recordInfo = new RecordInfo();
        PlayerRecordModeData recordModeData = userData.getServerOnly().getRecordModeData();
        if (recordModeData != null) {
            Map<String, RecordModeMatchTableValue> recordModeMatchTable = GameEnvironment.recordModeMatchTableMap.get(gameVersion);
            RecordModeMatchTableValue recordModeMatchTableValue = recordModeMatchTable.get(String.valueOf(recordModeData.getRecordModeMatchTableId()));
            PlayerWeaponInfo playerWeaponInfo = new PlayerWeaponInfo(recordModeMatchTableValue.getPlayerGunId(), recordModeMatchTableValue.getPlayerGunLevel(), recordModeMatchTableValue.getPlayerBulletId());
            recordInfo.setRecordModeMatchId(recordModeData.getRecordModeMatchTableId());
            recordInfo.setPlayerWeaponInfo(playerWeaponInfo);
        }
        userInfo.setRecordInfo(recordInfo);

        userInfoDto.setVersion(gameVersion);
        userInfoDto.setUid(userData.getUuid());
        userInfoDto.setUserInfo(userInfo);
        return userInfoDto;
    }

    private static final String CHANGE_NAME_CONDITION = "^[a-z0-9A-Z ]+$";

    @PostMapping("changePlayerName")
    @ApiOperation("修改玩家名称")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> changePlayerName(@RequestBody ChangeNameDTO request) {
        try {
            GameEnvironment.timeMessage.computeIfAbsent("changePlayerName", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] changePlayerName" + System.currentTimeMillis());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());
            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            //处理userData
            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            Map<String, Object> changeNameResult = new HashMap<>();
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            changeNameResult.put("success", false);
            changeNameResult.put("rejectReason", "");
            sendToClientData.setName(userData.getName());
            if (StringUtils.isEmpty(request.getNewName())) {

                changeNameResult.put("rejectReason", "new_name_is_empty");
                log.error("玩家" + request.getUserUid() + "新名称为空");
                map.put("result", changeNameResult);
                userDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
                map.put("userData", sendToClientData);
                return map;
//                throw new BusinessException(map.toString());
            }

            //校验玩家名称
            if (!request.getNewName().matches(CHANGE_NAME_CONDITION)) {
                changeNameResult.put("rejectReason", "contain_illegal_character");
                log.error("修改名称" + request.getNewName() + "包含非法字符");
                map.put("result", changeNameResult);
                userDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
                map.put("userData", sendToClientData);
                return map;
//                throw new BusinessException(map.toString());
            }

            //bad word check
            log.info("check name" + request.getNewName());
            if (userDataService.checkContainBadWords(request.getNewName())) {
                changeNameResult.put("rejectReason", "bad_word_check_not_pass");
                log.error("修改名称" + request.getNewName() + "包含脏字");
                map.put("result", changeNameResult);
                userDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
                map.put("userData", sendToClientData);
                return map;
//                throw new BusinessException(map.toString());
            }

            log.info("更换名称:" + request.getNewName());
            userData.setName(request.getNewName());
            sendToClientData.setName(userData.getName());
            changeNameResult.put("success", true);
            map.put("result", changeNameResult);
            userDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());

            map.put("userData", sendToClientData);
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("changePlayerName").add(needTime);
            log.info("[cmd] changePlayerName finish need time" + (System.currentTimeMillis() - startTime));
            log.info("[cmd] changePlayerName result: " + JSON.toJSONString(map));
            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }


    @PostMapping("/keepAlive")
    @ApiOperation("玩家保活")
    public Map<String, Object> keepAlive(@RequestBody org.skynet.service.provider.hunting.obsolete.pojo.dto.BaseDTO baseDTO) {
        String fightUrl = systemPropertiesConfig.getFightUrl();
        log.info("玩家保活：{}", baseDTO.getUserUid());
        GameEnvironment.onlineUser.put(baseDTO.getUserUid(), new Date());
        log.info("玩家数据最新保活情况{}:", GameEnvironment.onlineUser.get(baseDTO.getUserUid()));
        BaseDTO dto = new BaseDTO(baseDTO.getGameVersion(), baseDTO.getUserUid());
        HttpUtil.getFightInfo(fightUrl + "/user/online", dto);
        return CommonUtils.responsePrepare(null);
    }


}
