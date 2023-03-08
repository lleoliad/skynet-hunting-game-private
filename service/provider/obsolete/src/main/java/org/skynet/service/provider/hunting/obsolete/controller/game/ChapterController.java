package org.skynet.service.provider.hunting.obsolete.controller.game;

import com.alibaba.fastjson.JSONObject;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.ActiveChapterBonusDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.BaseDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.ChapterBonusPackageData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
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

@Api(tags = "章节相关操作")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class ChapterController {

    @Resource
    private UserDataService userDataService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    List<Long> confirmChapterUnlockAnimationComplete = new ArrayList<>();

    List<Long> activeChapterBonusPackage = new ArrayList<>();

    @PostMapping("chapter-confirmChapterUnlockAnimationComplete")
    @ApiOperation("确认玩家章节解锁动画播放完成")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> confirmChapterUnlockAnimationComplete(@RequestBody BaseDTO request) {
        GameEnvironment.timeMessage.computeIfAbsent("confirmChapterUnlockAnimationComplete", k -> new ArrayList<>());
        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] confirmChapterUnlockAnimationComplete" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            //处理userData
            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            userData.setPendingUnlockAnimationChapterId(-1);
            sendToClientData.setPendingUnlockAnimationChapterId(-1);

            //处理返回结果
            userDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", sendToClientData);
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("confirmChapterUnlockAnimationComplete").add(needTime);
            log.info("[cmd] confirmChapterUnlockAnimationComplete finish need time" + (System.currentTimeMillis() - startTime));
            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }


    @PostMapping("chapterBonusPackage-activeChapterBonusPackage")
    @ApiOperation(value = "激活章节礼包", notes = "玩家看到章节礼包之后,才开始计时")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> activeChapterBonusPackage(@RequestBody ActiveChapterBonusDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();
            log.info("[cmd] activeChapterBonusPackage" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            //处理userData
            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            List<ChapterBonusPackageData> chapterBonusPackagesData = userData.getChapterBonusPackagesData();
            ChapterBonusPackageData packageData = null;

            for (ChapterBonusPackageData chapterBonusPackageData : chapterBonusPackagesData) {

                if (chapterBonusPackageData.getChapterId().equals(request.getChapterId())) {
                    packageData = chapterBonusPackageData;
                    break;
                }
            }

            if (packageData == null) {
                throw new BusinessException("玩家" + request.getUserUid() + "确认章节礼包,但是无法找到礼包数据.chapter id" + request.getChapterId());
            }

            packageData.setExpireTime(request.getExpireTime());
            packageData.setIsActive(true);

            sendToClientData.setHistory(userData.getHistory());
            //处理返回结果
            userDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
            sendToClientData.setChapterBonusPackagesData(chapterBonusPackagesData);
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", sendToClientData);
            log.info("[cmd] activeChapterBonusPackage finish need time" + (System.currentTimeMillis() - startTime));

            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }
}
