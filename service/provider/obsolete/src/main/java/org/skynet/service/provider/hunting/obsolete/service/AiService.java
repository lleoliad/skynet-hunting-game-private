package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.service.provider.hunting.obsolete.enums.ABTestGroup;
import org.skynet.service.provider.hunting.obsolete.enums.HuntingMatchAIRecordChooseMode;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.RecordDataAndBase64;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.*;
import org.skynet.service.provider.hunting.obsolete.pojo.table.MatchAIRoundRuleTableValue;

import java.util.List;

/**
 * Ai相关设置
 */
public interface AiService {

    /**
     * 将完整匹配AI选择模式从完整匹配模式类型转换为回合匹配模式类型
     *
     * @param aiRecordChooseMode
     * @return
     */

    HuntingMatchAIRecordChooseMode convertAiRecordChooseModeFromWholeMatchToRound(HuntingMatchAIRecordChooseMode aiRecordChooseMode);

    /**
     * 确保某种武器组合,在所有路线下都有分布数据
     *
     * @param distributionDataArray
     * @param requiresRoutesCount
     * @return
     */
    List<WeaponInfoAvailableStatusData> filterWeaponInfoAvailableForAllRoutes(List<PlayerControlRecordDistributionData> distributionDataArray, Integer requiresRoutesCount);

    /**
     * 为可用武器排序，并随机返回一把可用武器
     *
     * @param weaponInfoAvailableStatusData
     * @return
     */
    WeaponInfoAvailableStatusData sortWeaponInfoAvailableStatusData(List<WeaponInfoAvailableStatusData> weaponInfoAvailableStatusData);


    /**
     * 加载Ai控制记录数据
     *
     * @param filterParameters
     * @param gameVersion
     * @return
     */
    RecordDataAndBase64 loadAiControlRecordData(MatchAiRoundRecordFilterParameters filterParameters, String gameVersion);

    /**
     * 以分数和精度搜索记录
     *
     * @param list
     * @param scoreRange
     * @param averageShowPrecisionRange
     * @return
     */
    List<PlayerControlRecordData> searchRecordsWithScoreAndPrecisionInDatabase(List<PlayerControlRecordData> list, Integer[] scoreRange, Double[] averageShowPrecisionRange, String userUid);

    /**
     * 根据精度来搜索记录
     *
     * @param filterList
     * @param averageShowPrecisionRange
     * @param userUid
     * @return
     */
    List<PlayerControlRecordData> searchRecordsWithPrecisionInDatabase(List<PlayerControlRecordData> filterList, Double[] averageShowPrecisionRange, String userUid);

    /**
     * 为AI查找整场比赛录像数据
     *
     * @param userUid
     * @param aiRecordChooseMode
     * @param chapterId
     * @param matchId
     * @param gameVersion
     * @return
     */
    PlayerUploadWholeMatchControlRecordData queryWholeMatchControlRecords(String userUid, HuntingMatchAIRecordChooseMode aiRecordChooseMode, Integer chapterId, Integer matchId, String gameVersion);

    PlayerUploadWholeMatchControlRecordData testQueryWholeMatchControlRecords(String userUid, Integer chapterId, Integer matchId, String gameVersion, RangeInt rangeInt);


    /**
     * 根据玩家上传的整场比赛产生对战AI信息
     *
     * @param wholeMatchRecordsData
     * @return
     */
    PlayerWeaponInfo generateAiWeaponInfoByWholeMatchRecordsData(PlayerUploadWholeMatchControlRecordData wholeMatchRecordsData);

    /**
     * 根据match ai round rule表格，生成ai武器信息
     *
     * @param matchAiRoundRuleTableValue
     * @param gameVersion
     * @return
     */
    PlayerWeaponInfo generateAiWeaponInfoByMatchAiRoundRule(MatchAIRoundRuleTableValue matchAiRoundRuleTableValue, String gameVersion);

    /**
     * 生成比赛中AI回合选择模式录像过滤参数
     *
     * @param aiQuery
     * @return
     */
    MatchAiRoundRecordFilterParameters generateMatchAiRoundControlRecordFilterParameters(MatchAiRoundControlQuery aiQuery, String gameVersion, ABTestGroup abTestGroup);
}
