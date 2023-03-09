package org.skynet.service.provider.hunting.obsolete.controller.admin;

import com.alibaba.fastjson.JSONObject;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.HttpUtil;
import org.skynet.service.provider.hunting.obsolete.common.util.NumberUtils;
import org.skynet.service.provider.hunting.obsolete.config.GameConfig;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.BattleScriptDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.OpponentPlayerInfo;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.OpponentProfile;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.ChapterTableValue;
import org.skynet.service.provider.hunting.obsolete.service.ObsoleteUserDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

@Api(tags = "ai测试工具")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
@CrossOrigin
public class AiUtilController {

    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;


    @PostMapping("battleScript")
    @ApiOperation("对战脚本")
    public Map<String, Object> battleScript(@RequestBody BattleScriptDTO request) {

        String fightUrl = systemPropertiesConfig.getFightUrl();

        Integer trophy = request.getTrophy();
        OpponentPlayerInfo opponentPlayerInfo = generateOpponentPlayerInfo(request.getTrophy(),
                request.getChapterId(),
                request.getChapterEnteredCount(),
                trophy,
                request.getUserUid(),
                request.getGameVersion());

        Map<String, Object> fightMessageMap = new LinkedHashMap<>();
        Map<String, Object> returnMap = new LinkedHashMap<>();
        int aiScore = 0;

        String url = fightUrl + "/battle/matchDebug";
        log.info("最终的url地址：{}", url);
        Map<String, Object> fightInfo = HttpUtil.getFightInfo(url, request);
        System.out.println(fightInfo);
        if (fightInfo != null) {
            Object data = fightInfo.get("data");
            if (data != null) {
                JSONObject jsonObject = JSONObject.parseObject(data.toString());
                fightMessageMap.put("AITrophy", opponentPlayerInfo.getTrophy());
                fightMessageMap.put("aiRecordChooseMode", jsonObject.get("aiRecordChooseMode"));
                fightMessageMap.put("windId", jsonObject.get("windId"));

                fightMessageMap.put("1-score", jsonObject.get("1-score"));
                aiScore += Integer.parseInt(jsonObject.get("1-score").toString());
                fightMessageMap.put("1-precision", jsonObject.get("1-precision"));

                fightMessageMap.put("2-score", jsonObject.get("2-score"));
                aiScore += Integer.parseInt(jsonObject.get("2-score").toString());
                fightMessageMap.put("2-precision", jsonObject.get("2-precision"));

                fightMessageMap.put("3-score", jsonObject.get("3-score"));
                aiScore += Integer.parseInt(jsonObject.get("3-score").toString());
                fightMessageMap.put("3-precision", jsonObject.get("3-precision"));

                fightMessageMap.put("4-score", jsonObject.get("4-score"));
                aiScore += Integer.parseInt(jsonObject.get("4-score").toString());
                fightMessageMap.put("4-precision", jsonObject.get("4-precision"));

                fightMessageMap.put("5-score", jsonObject.get("5-score"));
                aiScore += Integer.parseInt(jsonObject.get("5-score").toString());
                fightMessageMap.put("5-precision", jsonObject.get("5-precision"));

                fightMessageMap.put("aiScore", aiScore);

                fightMessageMap.put("weaponId", jsonObject.get("weaponId"));
                fightMessageMap.put("gunId", jsonObject.get("gunId"));
                fightMessageMap.put("gunLevel", jsonObject.get("gunLevel"));
                fightMessageMap.put("bulletId", jsonObject.get("bulletId"));

                fightMessageMap.put("wholeMatch", jsonObject.get("wholeMatch") == null);
                if ((Boolean) fightMessageMap.get("wholeMatch")) {
                    fightMessageMap.put("AITrophy", jsonObject.get("AITrophy"));
                }

            }
        }


        returnMap.put("fightMessageMap", fightMessageMap);
        returnMap.put("log", JSONObject.toJSONString(fightMessageMap));
        return returnMap;
    }


    public OpponentPlayerInfo generateOpponentPlayerInfo(Integer trophy,
                                                         Integer chapterId,
                                                         Integer chapterEnteredCount,
                                                         Integer opponentTrophyCount,
                                                         String userUid,
                                                         String gameVersion) {

        final double aiProfileCount = 1071d;
        OpponentPlayerInfo opponentPlayerInfo = new OpponentPlayerInfo(null, null, null, null);

        Map<String, ChapterTableValue> chapterTable = GameEnvironment.chapterTableMap.get(gameVersion);
        //对手奖杯根据玩家变化
        ChapterTableValue chapterTableValue = chapterTable.get(String.valueOf(chapterId));

        if (opponentTrophyCount == null) {
            opponentPlayerInfo.setTrophy(NumberUtils.randomInt(trophy * (1 - GameConfig.aiTrophyToPlayerChangeRatio), trophy * (1 + GameConfig.aiTrophyToPlayerChangeRatio)));
            opponentPlayerInfo.setTrophy(Math.max(chapterTableValue.getUnlockRequiresTrophyCount(), opponentPlayerInfo.getTrophy()));
        } else {
            opponentPlayerInfo.setTrophy(opponentTrophyCount);
            log.info("直接设置对手奖杯数:" + opponentTrophyCount);
        }

        boolean useTotalRandomAiProfile = NumberUtils.randomFloat(0d, 1d) <= GameConfig.randomNameAiRatioInPlayerMatching;

        //如果是第一章前三局,那么都是随机玩家信息
        if (chapterId == 1) {

            if (chapterEnteredCount <= 3) {
                useTotalRandomAiProfile = true;
            }
        }
        if (useTotalRandomAiProfile) {
            opponentPlayerInfo.setName(obsoleteUserDataService.createGuestName(userUid));
            opponentPlayerInfo.setIcon_base64(null);
            opponentPlayerInfo.setUseDefaultIcon(true);
        } else {
            //从服务器中获得对手名称和头像
            Integer aiProfileId = NumberUtils.randomInt(1.0, aiProfileCount);

            String collectionPath = Path.getDefaultAiProfileCollectionPath();

            String redisAiProfileId = collectionPath + ":" + aiProfileId;
            OpponentProfile opponentProfile = RedisDBOperation.selectOpponentProfile(redisAiProfileId);

            if (opponentProfile == null) {
                throw new BusinessException("获取ai profile id" + aiProfileId + "不存在");
            }
            opponentPlayerInfo.setName(opponentProfile.getName());
            opponentPlayerInfo.setIcon_base64(opponentProfile.getIcon_base64());
            opponentPlayerInfo.setUseDefaultIcon(opponentProfile.getUseDefaultIcon());
        }


        return opponentPlayerInfo;
    }
}
