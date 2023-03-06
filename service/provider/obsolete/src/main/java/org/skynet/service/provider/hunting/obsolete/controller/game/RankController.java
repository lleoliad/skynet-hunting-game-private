package org.skynet.service.provider.hunting.obsolete.controller.game;


import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.common.Path;
import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.common.util.CommonUtils;
import org.skynet.service.provider.hunting.obsolete.common.util.HttpUtil;
import org.skynet.service.provider.hunting.obsolete.common.util.thread.ThreadLocalUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.controller.module.rank.dto.PreviewPlayerDto;
import org.skynet.service.provider.hunting.obsolete.controller.module.rank.service.RankService;
import org.skynet.service.provider.hunting.obsolete.idempotence.RepeatSubmit;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.BaseDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.DownloadPlayersIconDto;
import org.skynet.service.provider.hunting.obsolete.pojo.dto.RankOtherPlayerDetailDto;
import com.cn.huntingrivalserver.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.service.ChestService;
import org.skynet.service.provider.hunting.obsolete.service.UserDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api(tags = "段位")
@RestController
@RequestMapping("/huntingrival")
@Slf4j
public class RankController {


    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private UserDataService userDataService;

    @Resource
    private RankService rankService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ChestService chestService;


    @PostMapping("/rank-getAllPlayersData")
    @ApiOperation("获取段位中所有玩家的信息")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> getAllPlayersData() {

        try {
            List<RankPlayerData> rankPlayersData = new ArrayList<>();
            RankPlayerData rankPlayerData = new RankPlayerData("123131", "test", 1000L, "url");
            rankPlayersData.add(rankPlayerData);

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("rankPlayersData", rankPlayersData);

            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @PostMapping("/rank-downloadPlayersIcon")
    @ApiOperation("获取其他玩家头像数据")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> downloadPlayersIcon(@RequestBody DownloadPlayersIconDto request) {

        try {
            List<String> icons = new ArrayList<>();
            icons.add("icons..........");
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("icons", icons);

            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @PostMapping("/rank-getPlayerDetailData")
    @ApiOperation("获取其他玩家详细信息")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> getPlayerDetailData(@RequestBody RankOtherPlayerDetailDto request) {

        try {
            UserData userData = RedisDBOperation.selectUserData("User:" + request.getUserUid());

            PreviewPlayerDto previewPlayerDto = new PreviewPlayerDto(request.getOtherPlayerUid(), request.getGameVersion(), request.getGroupId());
            Map<String, Object> rankInfo = HttpUtil.getRankInfo("http://192.168.2.199:10010/huntingrival/rank/league/player/preview", previewPlayerDto);
            Map<String, Object> map = CommonUtils.responsePrepare(null);

            RankOtherPlayerDetail playerDetailData = new RankOtherPlayerDetail();

            if (userData == null) {
                if (rankInfo != null) {
                    JSONObject data = JSONObject.parseObject(rankInfo.get("data").toString());
                    if (data != null) {
                        JSONObject playerDetailInfo = JSONObject.parseObject(data.get("playerDetailInfo").toString());
                        if (playerDetailInfo != null) {
                            playerDetailData.setRank(Integer.parseInt(playerDetailInfo.get("rank").toString()));
                            playerDetailData.setBestRank(Integer.parseInt(playerDetailInfo.get("bestRank").toString()));
                            playerDetailData.setNickName(playerDetailInfo.get("nickname").toString());
                            playerDetailData.setHeadPic(playerDetailInfo.get("headPic").toString());
                            playerDetailData.setCoin(Long.parseLong(playerDetailInfo.get("coin").toString()));
                            playerDetailData.setTotalBattleCount(Integer.parseInt(playerDetailInfo.get("totalBattleCount").toString()));
                            playerDetailData.setTotalEarnedCoin(Long.parseLong(playerDetailInfo.get("totalEarnedCoin").toString()));
                            playerDetailData.setTotalWinCount(Integer.parseInt(playerDetailInfo.get("totalWinCount").toString()));
                            playerDetailData.setWinningPercentage(Double.parseDouble(playerDetailInfo.get("winningPercentage").toString()));
                            playerDetailData.setWinningStreak(Integer.parseInt(playerDetailInfo.get("winningStreak").toString()));
                            playerDetailData.setBestWinningStreak(Integer.parseInt(playerDetailInfo.get("bestWinningStreak").toString()));
                            playerDetailData.setAveragePrecision(Double.parseDouble(playerDetailInfo.get("averagePrecision").toString()));
                            playerDetailData.setTotalKill(Integer.parseInt(playerDetailInfo.get("totalKill").toString()));
                            playerDetailData.setPerfectPrecision(Double.parseDouble(playerDetailInfo.get("perfectPrecision").toString()));
                            playerDetailData.setPerfectKill(Integer.parseInt(playerDetailInfo.get("perfectKill").toString()));
                            playerDetailData.setHeadShotTimes(Integer.parseInt(playerDetailInfo.get("headShotTimes").toString()));
                            playerDetailData.setHeartShotTimes(Integer.parseInt(playerDetailInfo.get("heartShotTimes").toString()));
                        }
                    }
                }
            } else {
                if (rankInfo != null) {
                    JSONObject data = JSONObject.parseObject(rankInfo.get("data").toString());
                    if (data != null) {
                        JSONObject playerDetailInfo = JSONObject.parseObject(data.get("playerDetailInfo").toString());
                        if (playerDetailInfo != null) {
                            playerDetailData.setRank(Integer.parseInt(playerDetailInfo.get("rank").toString()));
                            playerDetailData.setBestRank(Integer.parseInt(playerDetailInfo.get("bestRank").toString()));
                            playerDetailData.setHeadPic(playerDetailInfo.get("headPic").toString());
                        }
                    }
                }
                playerDetailData.setNickName(userData.getName());
                //获取玩家头像
                String key = Path.getUserProfileImageCollectionPath();
                Object imageBase64 = redisTemplate.opsForHash().get(key, userData.getUuid());
                if (imageBase64 != null) {
                    playerDetailData.setHeadPic(imageBase64.toString());
                }
                //计算玩家总战斗次数
                int totalBattleCount = 0;
                Map<Integer, Integer> chapterCompletedCountMap = userData.getChapterCompletedCountMap();
                for (Integer integer : chapterCompletedCountMap.keySet()) {
                    totalBattleCount += chapterCompletedCountMap.get(integer);
                }
                playerDetailData.setTotalBattleCount(totalBattleCount);
                playerDetailData.setTotalEarnedCoin(userData.getHistory().getTotalEarnedCoin());
                //计算玩家总胜利次数
                int totalWinCount = 0;
                Map<Integer, Integer> chapterWinCountMap = userData.getChapterWinCountMap();
                for (Integer integer : chapterWinCountMap.keySet()) {
                    totalWinCount += chapterWinCountMap.get(integer);
                }
                playerDetailData.setTotalWinCount(totalWinCount);
                playerDetailData.setWinningPercentage(totalWinCount * 1.0 / totalBattleCount);
                playerDetailData.setWinningStreak(userData.getHistory().getCurrentMatchWinStreak());
                playerDetailData.setBestWinningStreak(userData.getHistory().getBestMatchWinStreak());
                playerDetailData.setAveragePrecision(userData.getHistory().getMatchAverageHitPrecision());
                playerDetailData.setTotalKill(userData.getHistory().getTotalAnimalKillAmount());
                playerDetailData.setPerfectPrecision(userData.getHistory().getPerfectAnimalKillAmount() * 1.0 / userData.getHistory().getTotalAnimalKillAmount());
                playerDetailData.setPerfectKill(userData.getHistory().getPerfectAnimalKillAmount());
                playerDetailData.setHeadShotTimes(userData.getHistory().getHeadShotTimes());
                playerDetailData.setHeartShotTimes(userData.getHistory().getHeartShotTimes());
            }

            map.put("playerDetailData", playerDetailData);

            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @PostMapping("/rank-getEvaluationResult")
    @ApiOperation("获取上周段位结算数据")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> getEvaluationResult(@RequestBody BaseDTO baseDTO) {

        try {
            UserDataSendToClient sendToClientUserData = GameEnvironment.prepareSendToClientUserData();
            EvaluationResult evaluationResult = new EvaluationResult();
            evaluationResult.setIsEvaluateComplete(RandomUtil.randomBoolean());
            if (!evaluationResult.getIsEvaluateComplete()) {
                evaluationResult.setNewRankId(1);
                evaluationResult.setRankPlayersData(new ArrayList<>());
            }
            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("evaluationResult", evaluationResult);
            if (!evaluationResult.getIsEvaluateComplete()) {
                return map;
            }
            //userData 中更新的数据也下发下来
//            userData: {
//                //新段位的数据
//                playerRankData: {
//                    lastWeekRankId: number
//                    currentWeekRankId: number
//                    endStandardTime: number
//                }
//            }
//            sendToClientUserData.setPlayerRankData(new PlayerRankData(null,
//                    null,
//                    -1,
//                    -1,-1,
//                    new ClientRecord(false,false),-1,-1L));
            map.put("userData", sendToClientUserData);

            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @PostMapping("/rank-openRankRewardChest")
    @ApiOperation("打开段位宝箱")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> openPromotedRewardChest(@RequestBody BaseDTO request) {

        try {
            ThreadLocalUtil.set(request.getServerTimeOffset());
            CommonUtils.requestProcess(request, null, systemPropertiesConfig.getSupportRecordModeClient());
            userDataService.ensureUserDataIdempotence(request.getUserUid(), request.getUserDataUpdateCount(), request.getGameVersion());

            UserDataSendToClient sendToClientData = GameEnvironment.prepareSendToClientUserData();
            //处理userData
            userDataService.checkUserDataExist(request.getUserUid());
            UserData userData = GameEnvironment.userDataMap.get(request.getUserUid());

            String rankUrl = systemPropertiesConfig.getRankUrl();
            ChestData chestData = new ChestData();
            //获取用户宝箱奖励
            PreviewPlayerDto previewPlayerDto = new PreviewPlayerDto(userData.getUuid(), request.getGameVersion(), request.getGroupId());
            Map<String, Object> rankInfo = HttpUtil.getRankInfo(rankUrl + "/getReward", previewPlayerDto);

            // if (rankInfo != null) {
            //     JSONObject data = JSONObject.parseObject(rankInfo.get("data").toString());
            //     if (data != null) {
            //         JSONObject userJsonData = JSONObject.parseObject(data.get("userData").toString());
            //         if (userJsonData != null) {
            //             JSONObject rankJsonData = JSONObject.parseObject(userJsonData.get("playerRankData").toString());
            //             if (rankJsonData.get("lastWeekRewardChestType") != null) {
            //                 chestData.setChestType(Integer.parseInt(rankJsonData.get("lastWeekRewardChestType").toString()));
            //             }
            //             if (rankJsonData.get("lastWeekRewardChestLevel") != null) {
            //                 chestData.setLevel(Integer.parseInt(rankJsonData.get("lastWeekRewardChestLevel").toString()));
            //             }
            //
            //         }
            //     }
            // }

            JSONObject data = JSONObject.parseObject(rankInfo.get("data").toString());
            JSONObject userJsonData = JSONObject.parseObject(data.get("userData").toString());
            JSONObject rankJsonData = JSONObject.parseObject(userJsonData.get("playerRankData").toString());
            if (rankJsonData.get("lastWeekRewardChestType") != null) {
                chestData.setChestType(Integer.parseInt(rankJsonData.get("lastWeekRewardChestType").toString()));
            }
            if (rankJsonData.get("lastWeekRewardChestLevel") != null) {
                chestData.setLevel(Integer.parseInt(rankJsonData.get("lastWeekRewardChestLevel").toString()));
            }

            ChestOpenResult chestOpenResult = null;
            if (chestData.getChestType() != null && chestData.getLevel() != null) {
                chestOpenResult = chestService.openChest(userData, chestData, request.getGameVersion());
            }


            //todo 开完宝箱之后刷新段位信息

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            if (chestOpenResult != null) {
                sendToClientData.setBulletCountMap(userData.getBulletCountMap());
                sendToClientData.setCoin(userData.getCoin());
                sendToClientData.setDiamond(userData.getDiamond());
                sendToClientData.setGunLevelMap(userData.getGunLevelMap());
                sendToClientData.setGunCountMap(userData.getGunCountMap());

                JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(sendToClientData));
                jsonObject.put("playerRankData", rankJsonData);
                map.put("userData", jsonObject);
                map.put("openResult", chestOpenResult);
            }


            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ThreadLocalUtil.remove();
        }

    }


    @PostMapping("/rank-confirmLastWeekEvaluationRankListShow")
    @ApiOperation("确认上周结算排行界面展示")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> confirmLastWeekEvaluationRankListShow(@RequestBody BaseDTO baseDTO) {

        try {

            UserData userData = RedisDBOperation.selectUserData("UserId:" + baseDTO.getUserUid());
            if (userData == null) {
                throw new BusinessException("用户信息不存在：" + "UserId:" + baseDTO.getUserUid());
            }

//            userData.getPlayerRankData().setLastWeekEvaluationRankListShown(true);
            UserDataSendToClient sendToClientUserData = GameEnvironment.prepareSendToClientUserData();
            sendToClientUserData.setPlayerRankData(userData.getPlayerRankData());

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", sendToClientUserData);

            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @PostMapping("/rank-confirmLastWeekEvaluationRankChangeShow")
    @ApiOperation("确认上周结算段位变化界面展示")
    @RepeatSubmit(interval = 120000)
    public Map<String, Object> confirmLastWeekEvaluationRankChangeShow(@RequestBody BaseDTO baseDTO) {

        try {

            UserData userData = RedisDBOperation.selectUserData("UserId:" + baseDTO.getUserUid());
            if (userData == null) {
                throw new BusinessException("用户信息不存在：" + "UserId:" + baseDTO.getUserUid());
            }

//            userData.getPlayerRankData().setLastWeekEvaluationRankChangeShown(true);
            UserDataSendToClient sendToClientUserData = GameEnvironment.prepareSendToClientUserData();
            sendToClientUserData.setPlayerRankData(userData.getPlayerRankData());

            Map<String, Object> map = CommonUtils.responsePrepare(null);
            map.put("userData", sendToClientUserData);

            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
