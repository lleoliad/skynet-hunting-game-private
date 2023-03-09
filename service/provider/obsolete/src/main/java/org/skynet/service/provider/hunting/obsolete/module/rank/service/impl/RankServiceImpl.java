// package org.skynet.service.provider.hunting.obsolete.module.rank.service.impl;
//
// import cn.hutool.json.JSONUtil;
// import com.alibaba.fastjson.JSONObject;
// import org.skynet.components.hunting.rank.league.service.RankLeagueFeignService;
// import org.skynet.service.provider.hunting.obsolete.common.Path;
// import org.skynet.service.provider.hunting.obsolete.common.util.HttpUtil;
// import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
// import org.skynet.service.provider.hunting.obsolete.module.rank.dto.RankLoginDto;
// import org.skynet.service.provider.hunting.obsolete.module.rank.entity.ClientRecord;
// import org.skynet.service.provider.hunting.obsolete.module.rank.service.RankService;
// import org.skynet.service.provider.hunting.obsolete.pojo.entity.PlayerRankData;
// import org.skynet.commons.hunting.user.dao.entity.UserData;
// import org.skynet.service.provider.hunting.obsolete.service.UserDataService;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.commons.lang3.StringUtils;
// import org.skynet.starter.codis.service.CodisService;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
//
// import javax.annotation.Resource;
// import java.util.Map;
//
// @Slf4j
// @Service
// @Transactional
// public class RankServiceImpl implements RankService {
//
//     // @Resource
//     // private RedisTemplate<String, Object> redisTemplate;
//
//     @Resource
//     private CodisService codisService;
//
//     @Resource
//     private SystemPropertiesConfig systemPropertiesConfig;
//
//     @Resource
//     private UserDataService userDataService;
//
//
//     public void getRewardInfo(UserData userData, String gameVersion) {
//         String rankUrl = systemPropertiesConfig.getRankUrl();
//         log.info("段位赛地址{},", rankUrl);
//         if (StringUtils.isEmpty(rankUrl)) {
//             return;
//         }
//
//         RankLoginDto rankLoginDto = new RankLoginDto(userData.getUuid(), gameVersion, userData.getName(), null, 0L);
//         //从redis中获取玩家头像，如果没有直接为空
//         String key = Path.getUserProfileImageCollectionPath();
//         // Object imageBase64 = redisTemplate.opsForHash().get(key, userData.getUuid());
//         String imageBase64 = codisService.get(key);
//         if (imageBase64 != null) {
//             rankLoginDto.setHeadPic(imageBase64.toString());
//         }
//         PlayerRankData playerRankData = new PlayerRankData(null,
//                 null,
//                 -1,
//                 -1, -1, -1,
//                 new ClientRecord(false, false), -1, -1L, -1);
//
//         Map<String, Object> rankInfo = HttpUtil.getRankInfo(rankUrl + "/login", rankLoginDto);
//
//
//         if (rankInfo != null) {
//
//             JSONObject data = JSONObject.parseObject(rankInfo.get("data").toString());
//             if (data != null) {
//                 JSONObject userJsonData = JSONObject.parseObject(data.get("userData").toString());
//                 if (userJsonData != null) {
//                     JSONObject rankJsonData = JSONObject.parseObject(userJsonData.get("playerRankData").toString());
//                     if (rankJsonData.get("lastWeekRankGroupUid") != null) {
//                         playerRankData.setLastWeekRankGroupUid(rankJsonData.get("lastWeekRankGroupUid").toString());
//                     }
//                     if (rankJsonData.get("currentWeekRankGroupUid") != null) {
//                         playerRankData.setCurrentWeekRankGroupUid(rankJsonData.get("currentWeekRankGroupUid").toString());
//                     }
//                     if (rankJsonData.get("currentWeekRankId") != null) {
//                         playerRankData.setCurrentWeekRankId(Integer.parseInt(rankJsonData.get("currentWeekRankId").toString()));
//                     }
//                     if (rankJsonData.get("lastWeekRankId") != null) {
//                         playerRankData.setLastWeekRankId(Integer.parseInt(rankJsonData.get("lastWeekRankId").toString()));
//                     }
//                     if (rankJsonData.get("lastWeekRewardChestType") != null) {
//                         playerRankData.setLastWeekRewardChestType(Integer.parseInt(rankJsonData.get("lastWeekRewardChestType").toString()));
//                     }
//                     if (rankJsonData.get("lastWeekRewardChestLevel") != null) {
//                         playerRankData.setLastWeekRewardChestLevel(Integer.parseInt(rankJsonData.get("lastWeekRewardChestLevel").toString()));
//                     }
//                     if (rankJsonData.get("clientRecord") != null) {
//                         ClientRecord clientRecord = JSONUtil.toBean(rankJsonData.get("clientRecord").toString(), ClientRecord.class);
//                         playerRankData.setClientRecord(clientRecord);
//                     }
//                     if (rankJsonData.get("rewardStatus") != null) {
//                         playerRankData.setRewardStatus(Integer.parseInt(rankJsonData.get("rewardStatus").toString()));
//                     }
//                     if (rankJsonData.get("endStandardTime") != null) {
//                         playerRankData.setEndStandardTime(Long.parseLong(rankJsonData.get("endStandardTime").toString()));
//                     }
//                     if (rankJsonData.get("bestRankId") != null) {
//                         playerRankData.setBestRankId(Integer.parseInt(rankJsonData.get("bestRankId").toString()));
//                     }
//
//                 }
//             }
//
//             userData.setPlayerRankData(playerRankData);
//
//
//         }
//
//     }
//
//
// }
