package org.skynet.service.provider.hunting.obsolete.controller.game;

import com.alibaba.fastjson.JSONObject;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.enums.AchievementType;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.AchievementDTO;
import org.skynet.commons.hunting.user.domain.AchievementData;
import org.skynet.commons.hunting.user.domain.History;
import org.skynet.commons.hunting.user.dao.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.AchievementTableValue;
import org.skynet.service.provider.hunting.obsolete.service.AchievementService;
import org.skynet.service.provider.hunting.obsolete.service.UserDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Map;

@Api(tags = "成就相关操作")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class AchievementController {

    @Resource
    private UserDataService userDataService;

    @Resource
    private AchievementService achievementService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;


    @PostMapping("achievement-completeAchievement")
    @ApiOperation("完成某个任务")
    @RepeatSubmit(interval = 60000)
    public Map<String, Object> completeAchievement(@RequestBody AchievementDTO request) {
        GameEnvironment.timeMessage.computeIfAbsent("achievement", k -> new ArrayList<>());
        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            long startTime = System.currentTimeMillis();

            log.info("[cmd] completeAchievement" + System.currentTimeMillis());
            log.info(JSONObject.toJSONString(request));

            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();

            //处理userData
            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            if (!(request.getAchievementType() instanceof Integer)) {

                throw new BusinessException("上传的achievement type不是整型");
            }

            Map<String, AchievementTableValue> achievementTable = GameEnvironment.achievementTableMap.get(request.getGameVersion());
            AchievementData targetAchievementData = null;
            AchievementTableValue targetAchievementTableValue = null;
            for (AchievementData achievementData : userData.getAchievements()) {

                int achievementId = achievementData.getAchievementId();
                AchievementTableValue tableValue = achievementTable.get(String.valueOf(achievementId));
                if (tableValue.getAchievementType() == request.getAchievementType()) {
                    targetAchievementData = achievementData;
                    targetAchievementTableValue = tableValue;
                    break;
                }
            }

            AchievementType achievementType = AchievementType.values()[(int) request.getAchievementType()];

            if (targetAchievementData == null) {
                throw new BusinessException("没有任务类型" + achievementType.getMessage() + "的数据" + JSONObject.toJSONString(userData.getAchievements()));
            }
            if (targetAchievementData.getFullyCompleted()) {
                throw new BusinessException("任务" + JSONObject.toJSONString(userData.getAchievements()) + "已经做完了");
            }
            if (targetAchievementData.getCurrentProgress() < targetAchievementData.getMaxProgress()) {
                throw new BusinessException("任务" + JSONObject.toJSONString(targetAchievementData) + "没有完成");
            }

            long tempDiamond = userData.getDiamond() + targetAchievementTableValue.getDiamondReward();
            userData.setDiamond(tempDiamond);
            userData.getHistory().setTotalEarnedDiamond(userData.getHistory().getTotalEarnedDiamond() + targetAchievementTableValue.getDiamondReward());
            sendToClientData.setDiamond(userData.getDiamond());
            if (null == sendToClientData.getHistory()) {
                sendToClientData.setHistory(new History());
            }
            BeanUtils.copyProperties(userData.getHistory(), sendToClientData.getHistory());


            log.info("完成任务" + targetAchievementData);

            AchievementTableValue nextLevelAchievementTableValue = achievementService.getNextLevelAchievementTableValue(targetAchievementData.getAchievementId(), request.getGameVersion());

            if (nextLevelAchievementTableValue == null) {

                //没有下一个等级的任务了
                targetAchievementData.setFullyCompleted(true);

            } else {
                switch (achievementType) {

                    case undefined:
                        break;
                    case wonTrophy:
                    case PerfectHuntingMatch:
                        targetAchievementData.setCurrentProgress(targetAchievementData.getCurrentProgress() - targetAchievementData.getMaxProgress());
                        break;
                    case wonTrophyInChapter:
                        int nextLevelChapterTrophyCount;
                        nextLevelChapterTrophyCount = userData.getChapterWinTrophyCountMap().getOrDefault(nextLevelAchievementTableValue.getCondition(), 0);
                        targetAchievementData.setCurrentProgress(nextLevelChapterTrophyCount);
                        break;
                    case completeChapterOrAboveMatch:
                    case completeChapterOrAboveWinStreak:
                    case killSmallAnimalInChapterOrAbove:
                    case killMediumAnimalInChapterOrAbove:
                    case killLargeAnimalInChapterOrAbove:
                    case perfectKillAnimalInChapterOrAbove:
                    case killAnimalWithHeadshotInChapterOrAbove:
                        targetAchievementData.setCurrentProgress(0);
                        break;
                }

                targetAchievementData.setAchievementId(nextLevelAchievementTableValue.getId());
                targetAchievementData.setMaxProgress(nextLevelAchievementTableValue.getObjective());
            }

            sendToClientData.setAchievements(userData.getAchievements());
            sendToClientData.setHistory(userData.getHistory());

            //处理返回结果
            userDataService.userDataSettlement(userData, sendToClientData, true, request.getGameVersion());
//            sendToClientData.setHistory(userData.getHistory());
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", sendToClientData);
            long needTime = System.currentTimeMillis() - startTime;
            GameEnvironment.timeMessage.get("achievement").add(needTime);
            log.info("cmd completeAchievement finish need time" + needTime);
            return map;
        } catch (Exception e) {
            CommonUtils.responseException(request, e, request.getUserUid());
        } finally {
            ThreadLocalUtil.remove();
        }
        return null;
    }
}
