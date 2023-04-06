package org.skynet.service.provider.hunting.game.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.skynet.commons.lang.common.Result;
import org.skynet.commons.lang.common.SkynetObject;
import org.skynet.components.hunting.data.service.DataService;
import org.skynet.components.hunting.game.query.MatchCompleteQuery;
import org.skynet.components.hunting.game.query.MatchConsumeBulletQuery;
import org.skynet.components.hunting.user.dao.entity.UserData;
import org.skynet.components.hunting.user.data.ClientUserData;
import org.skynet.components.hunting.user.data.HistoryVO;
import org.skynet.components.hunting.user.domain.ChapterWinChestData;
import org.skynet.components.hunting.user.domain.History;
import org.skynet.components.hunting.user.query.UserDataLandQuery;
import org.skynet.components.hunting.user.query.UserDataUpdateQuery;
import org.skynet.components.hunting.user.service.UserFeignService;
import org.skynet.service.provider.hunting.game.service.ChestAssignmentService;
import org.skynet.service.provider.hunting.game.service.MatchAssignmentService;
import org.skynet.service.provider.hunting.obsolete.enums.ForceTutorialStepNames;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerControlRecordData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerFireDetails;
import org.skynet.service.provider.hunting.obsolete.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class MatchAssignmentServiceImpl implements MatchAssignmentService {

    @Resource
    private UserFeignService userFeignService;

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

    @Resource
    private ChestAssignmentService chestAssignmentService;

    @Override
    public Result<?> consumeBullet(MatchConsumeBulletQuery matchConsumeBulletQuery) {
        UserData userData = matchConsumeBulletQuery.getUserData();
        boolean updateUserData = false;
        if (Objects.isNull(userData)) {
            Result<UserData> userDataResult = userFeignService.load(UserDataLandQuery.builder().userId(matchConsumeBulletQuery.getUserId()).build());
            if (userDataResult.failed()) {
                return userDataResult.build();
            }

            userData = userDataResult.getData();
            updateUserData = true;
        }

        huntingMatchService.consumeBullet(userData, matchConsumeBulletQuery.getBulletId(), matchConsumeBulletQuery.getVersion());

        if (updateUserData) {
            userFeignService.update(UserDataUpdateQuery.builder()
                    .userId(matchConsumeBulletQuery.getUserId())
                    .update(SkynetObject.builder()
                            .push("bulletCountMap", userData.getBulletCountMap())
                            .push("equippedBulletId", userData.getEquippedBulletId())
                            .build())
                    .build());
        }

        // ClientUserData clientUserData = ClientUserData.builder()
        //         .bulletCountMap(userData.getBulletCountMap())
        //         .equippedBulletId(userData.getEquippedBulletId())
        //         .build();
        return Result.ok(userData);
    }

    @Override
    public Result<?> complete(MatchCompleteQuery matchCompleteQuery) {
        boolean isWin = matchCompleteQuery.getIsWin();
        boolean recordOnlyMode = BooleanUtil.isTrue(matchCompleteQuery.getRecordOnlyMode());
        UserData userData = matchCompleteQuery.getUserData();

        Result<Object> ok = Result.ok();

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
        if (isWin && !recordOnlyMode) {
            if (null != matchCompleteQuery.getChapterId()) {
                if (obsoleteUserDataService.isForceTutorialStepComplete(userData.getUuid(), ForceTutorialStepNames.forceCompleteFirstPvPMatch.getName())) {
                    newCreateChapterWinChestData = chestService.tryCreateChapterWinChest(userData.getUuid(), matchCompleteQuery.getChapterId(), matchCompleteQuery.getVersion());
                }
            } else {
                newCreateChapterWinChestData = chestAssignmentService.getChapterWinChestData(userData, matchCompleteQuery.getChestType(), matchCompleteQuery.getChestLevel());
            }

            if (null != newCreateChapterWinChestData) {
                ok.push("newCreateChapterWinChestData", newCreateChapterWinChestData);
            }
        }

        //刷新玩家历史数据
        huntingMatchService.refreshPlayerHistoryData(userData, matchCompleteQuery.getChapterId(), isWin, playerFireDetails, matchCompleteQuery.getVersion());

        return ok.push("userData", userData).build();
    }
}
