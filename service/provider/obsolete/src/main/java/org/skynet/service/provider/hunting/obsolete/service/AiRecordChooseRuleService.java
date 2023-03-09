package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.commons.hunting.user.enums.ABTestGroup;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.CalculateAIFilterParametersBO;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.LocalPlayerCalculateAIFilterParametersBO;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.LocalPlayerInAiFilterConfigBO;
import org.skynet.service.provider.hunting.obsolete.pojo.table.AiFilterConfig;
import org.skynet.service.provider.hunting.obsolete.pojo.table.AiFirstAiRecordChooseRule;
import org.skynet.service.provider.hunting.obsolete.pojo.table.LocalPlayerFirstAiRecordChooseRule;

import java.util.Map;

public interface AiRecordChooseRuleService {

    /**
     * 获得玩家先手的ai规则
     *
     * @return
     */
    Map<String, LocalPlayerFirstAiRecordChooseRule> loadPlayerFirstAiRecordChooseRule(String gameVersion, ABTestGroup abTestGroup);

    /**
     * 获得AI先手的ai规则
     *
     * @param gameVersion
     * @param abTestGroup
     * @return
     */
    Map<String, AiFirstAiRecordChooseRule> loadAiFirstAiRecordChooseRule(String gameVersion, ABTestGroup abTestGroup);

    /**
     * AI只通过准确率筛选
     *
     * @return
     */
    CalculateAIFilterParametersBO calculateAIFilterParameters(AiFirstAiRecordChooseRule rule);

    /**
     * 计算AI过滤参数
     *
     * @param playerScore
     * @param playerShowPrecision
     * @return
     */
    LocalPlayerCalculateAIFilterParametersBO localPlayerCalculateAIFilterParameters(LocalPlayerFirstAiRecordChooseRule localPlay, Integer playerScore, Double playerShowPrecision);


    /**
     * 根据AiFilterConfig的配置,计算出AI的显示准确度区间和AI分数区间
     *
     * @param aiFilterConfig
     * @param playerScore
     * @param playerShowPrecision
     * @return
     */
    LocalPlayerInAiFilterConfigBO localPlayerCalculateAIFilterParametersInAiFilterConfig(AiFilterConfig aiFilterConfig, Integer playerScore, Double playerShowPrecision);
}
