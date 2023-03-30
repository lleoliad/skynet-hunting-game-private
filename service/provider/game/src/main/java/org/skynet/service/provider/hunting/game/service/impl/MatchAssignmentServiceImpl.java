package org.skynet.service.provider.hunting.game.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.data.service.DataService;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.domain.ChapterWinChestData;
import org.skynet.service.provider.hunting.game.query.MatchCompleteQuery;
import org.skynet.service.provider.hunting.game.service.MatchAssignmentService;
import org.skynet.service.provider.hunting.obsolete.enums.ForceTutorialStepNames;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerControlRecordData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerFireDetails;
import org.skynet.service.provider.hunting.obsolete.pojo.table.ChapterTableValue;
import org.skynet.service.provider.hunting.obsolete.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MatchAssignmentServiceImpl implements MatchAssignmentService {
    @Resource
    private ObsoleteUserDataService obsoleteUserDataService;

    @Resource
    private HuntingMatchService huntingMatchService;

    @Resource
    private AchievementService achievementService;

    @Resource
    private PlayerControlRecordDataService playerControlRecordDataService;

    @Resource
    private ChestService chestService;

    @Resource
    private DataService dataService;

    @Override
    public Result<?> complete(MatchCompleteQuery matchCompleteQuery) {
        boolean isWin = matchCompleteQuery.getIsWin();
        boolean recordOnlyMode = BooleanUtil.isTrue(matchCompleteQuery.getRecordOnlyMode());
        UserData userData = matchCompleteQuery.getUserData();

        List<PlayerControlRecordData> allPlayerControlRecordsData = new ArrayList<>();

        for (String roundEncodedData : matchCompleteQuery.getAllEncodedControlRecordsData()) {
            String temp = playerControlRecordDataService.decodeControlRecordData(roundEncodedData);
            PlayerControlRecordData controlRecordData = JSONObject.parseObject(temp, PlayerControlRecordData.class);
            allPlayerControlRecordsData.add(controlRecordData);
        }

        List<PlayerFireDetails> playerFireDetails = huntingMatchService.generateFireDetailsFromControlRecordData(allPlayerControlRecordsData);

        // ChapterTableValue chapterTableValue = null;
        // if (null != matchCompleteQuery.getChapterId()) {
        //     chapterTableValue = dataService.map(matchCompleteQuery.getVersion(), ChapterTableValue.class).get(String.valueOf(matchCompleteQuery.getChapterId()));
        // }
        //
        // //记录玩家完成章节比赛次数
        // huntingMatchService.recordChapterComplete(userData,
        //         chapterTableValue,
        //         matchCompleteQuery.getMatchId(),
        //         playerFireDetails,
        //         isWin,
        //         matchCompleteQuery.getVersion(),
        //         allPlayerControlRecordsData);

        //记录动物击杀成就
        achievementService.updateAnimalKillAchievementData(userData,
                matchCompleteQuery.getChapterId(),
//                    huntingMatchNowData.getMatchId(),
                playerFireDetails,
//                    isPlayerWin,
                matchCompleteQuery.getVersion(),
                allPlayerControlRecordsData);


        ChapterWinChestData newCreateChapterWinChestData = null;
        //如果胜利,且已经完成了第一次PVP匹配教学,获得一个章节胜利宝箱
        if (isWin && obsoleteUserDataService.isForceTutorialStepComplete(userData.getUuid(), ForceTutorialStepNames.forceCompleteFirstPvPMatch.getName()) && !recordOnlyMode) {
            newCreateChapterWinChestData = chestService.tryCreateChapterWinChest(userData.getUuid(), matchCompleteQuery.getChapterId(), matchCompleteQuery.getVersion());
        }

        //刷新玩家历史数据
        huntingMatchService.refreshPlayerHistoryData(userData, matchCompleteQuery.getChapterId(), isWin, playerFireDetails, matchCompleteQuery.getVersion());

        return Result.ok(userData);
    }
}
