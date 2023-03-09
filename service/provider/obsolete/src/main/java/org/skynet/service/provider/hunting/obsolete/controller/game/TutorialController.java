package org.skynet.service.provider.hunting.obsolete.controller.game;

import com.alibaba.fastjson.JSONObject;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.enums.ClientGameVersion;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.ForceStepDTO;
import org.skynet.components.hunting.user.domain.PlayerTutorialData;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
import org.skynet.service.provider.hunting.obsolete.service.PromotionEventPackageDataService;
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
import java.util.Map;


@Api(tags = "新手关卡")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class TutorialController {

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private PromotionEventPackageDataService promotionEventPackageDataService;

    @Resource
    private ChestService chestService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;


//    @GetMapping("/confirmMatchComplete")
//    @ApiOperation("确认完成教学关卡")
//    public ResponseDTO ConfirmTutorialHuntingMatchComplete(@RequestBody BaseDTO requestData){
//
//        try {
//            CommonUtils.requestProcess(requestData,null);
//            UserDataSendToClient sendToClientUserData = GameEnvironment.prepareSendToClientUserData();
//
//            String userUid = requestData.getUserUid();
//            userDataService.checkUserDataExist(userUid);
//
//            UserData userData = GameEnvironment.userDataMap.get(userUid);
//            PlayerTutorialData tutorialStatus = userData.getTutorialData();
//            tutorialStatus.setIsTutorialHuntingMatchComplete(true);
//            tutorialStatus.setServer_only_tutorialHuntingMatchCompleteTime(TimeUtils.getUnixTimeSecond());
//
//            sendToClientUserData.setTutorialData(tutorialStatus);
//
//            //解锁活动礼包
//            promotionEventPackageDataService.refreshPromotionEventPackagesData(userUid);
//
//            if ((int) GameEnvironment._clientGameVersion.getVersion().charAt(5) <= (int) ClientGameVersion._1_0_4_.getVersion().charAt(5)){
//
//                //新手完成直接赠送一个胜利宝箱
//                chestService.tryCreateChapterWinChestAsync(GameConfig.tutorialHuntingMatchChapterId,userUid);
//            }
//
//            sendToClientUserData.setChapterWinChestsData(userData.getChapterWinChestsData());
//            //promotionEventPackagesData返回
//            sendToClientUserData.setPromotionEventPackagesData(userData.getPromotionEventPackagesData());
//            userDataService.userDataSettlement(userData, sendToClientUserData);
//
//
//            return new ResponseDTO(sendToClientUserData,ResponseEnum.SUCCESS.getCode(),TimeUtils.getUnixTimeSecond());
//
//        }catch (Exception e){
//
//            CommonUtils.responseException(requestData,e.toString());
//
//        }
//        return null;
//    }

    @PostMapping("tutorial-completeForceTutorialStep")
    @ApiOperation("确认某个引导步骤完成")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> completeForceTutorialStep(@RequestBody ForceStepDTO request) {

        try {
            GameEnvironment.timeMessage.computeIfAbsent("completeForceTutorialStep", k -> new ArrayList<>());
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] completeForceTutorialStep" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
//            userDataService.ensureUserDataIdempotence(request.getUserUid(),request.getUserDataUpdateCount(),request.getGameVersion());

            //处理userData
            obsoleteUserDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            PlayerTutorialData tutorialData = userData.getTutorialData();

            tutorialData.getForceTutorialStepStatusMap().put(request.getForceTutorialStepName(), true);
            log.info("完成强制引导步骤" + request.getForceTutorialStepName());

            ClientGameVersion clientGameVersion = ClientGameVersion.toClientGameVersionEnum(request.getGameVersion());

            //解锁活动礼包
//            promotionEventPackageDataService.refreshPromotionEventPackageNow(userData, request.getGameVersion());
//            if (clientGameVersion.getVersion().compareTo(ClientGameVersion._1_0_10_.getVersion()) <= 0){
//                if (request.getForceTutorialStepName()!=null&&request.getForceTutorialStepName().equals(ForceTutorialStepNames.forceCompleteTutorialMatch.getName())){
//
//                }
//            }

            sendToClientData.setTutorialData(userData.getTutorialData());
            obsoleteUserDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
            sendToClientData.setGunLevelMap(userData.getGunLevelMap());
            sendToClientData.setGunCountMap(userData.getGunCountMap());
//            sendToClientData.setBulletCountMap(userData.getBulletCountMap());
            sendToClientData.setHistory(userData.getHistory());
//            sendToClientData.setPromotionEventPackagesData(userData.getPromotionEventPackagesData());
            sendToClientData.setChapterBonusPackagesData(userData.getChapterBonusPackagesData());
            Map<String, Object> map = CommonUtils.responsePrepare(null);

            //处理返回结果
            map.put("userData", sendToClientData);
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("completeForceTutorialStep").add(needTime);
            log.info("[cmd] completeForceTutorialStep finish need time" + (System.currentTimeMillis() - startTime));
            return map;

        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }

}
