package org.skynet.service.provider.hunting.obsolete.service.impl;

import org.skynet.service.provider.hunting.obsolete.common.exception.BusinessException;
import org.skynet.service.provider.hunting.obsolete.enums.ABTestGroup;
import org.skynet.service.provider.hunting.obsolete.enums.ComparisonType;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.CalculateAIFilterParametersBO;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.LocalPlayerCalculateAIFilterParametersBO;
import org.skynet.service.provider.hunting.obsolete.pojo.bo.LocalPlayerInAiFilterConfigBO;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import org.skynet.service.provider.hunting.obsolete.pojo.table.AiFilterConfig;
import org.skynet.service.provider.hunting.obsolete.pojo.table.AiFirstAiRecordChooseRule;
import org.skynet.service.provider.hunting.obsolete.pojo.table.LocalPlayerFirstAiRecordChooseRule;
import org.skynet.service.provider.hunting.obsolete.pojo.table.Rule;
import org.skynet.service.provider.hunting.obsolete.service.AiRecordChooseRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiRecordChooseRuleServiceImpl implements AiRecordChooseRuleService {


    @Override
    public Map<String, LocalPlayerFirstAiRecordChooseRule> loadPlayerFirstAiRecordChooseRule(String gameVersion, ABTestGroup abTestGroup) {

        if (abTestGroup == null) {
            abTestGroup = ABTestGroup.A;
        }

        Map<String, LocalPlayerFirstAiRecordChooseRule> rulesModule = null;

        try {
            String rulesPath = "Table:" + gameVersion + ":LocalPlayerFirstAiRecordChooseRules";
            rulesModule = GameEnvironment.loadLocalPlayerFirstAiRecordChooseRules(rulesPath);

        } catch (Exception e) {
            //没有该版本配置文件,载入默认配置
            String rulesPath = "Table:default:LocalPlayerFirstAiRecordChooseRules";
            try {
                rulesModule = GameEnvironment.loadLocalPlayerFirstAiRecordChooseRules(rulesPath);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        if (rulesModule == null) {

            throw new BusinessException("无法载入Player first ai record choose rule file. game version" + gameVersion + ", ab test group:" + abTestGroup.getMessage());
        }
        return rulesModule;
    }

    /**
     * 获得AI先手的ai规则
     *
     * @param gameVersion
     * @param abTestGroup
     * @return
     */
    @Override
    public Map<String, AiFirstAiRecordChooseRule> loadAiFirstAiRecordChooseRule(String gameVersion, ABTestGroup abTestGroup) {

        if (abTestGroup == null) {
            abTestGroup = ABTestGroup.A;
        }

        Map<String, AiFirstAiRecordChooseRule> rulesModule = null;

        try {
            String rulesPath = "Table:" + gameVersion + ":AiFirstAiRecordChooseRules";
            rulesModule = GameEnvironment.loadAiFirstAiRecordChooseRules(rulesPath);

        } catch (Exception e) {
            //没有该版本配置文件,载入默认配置
            String rulesPath = "Table:default:AiFirstAiRecordChooseRules";
            try {
                rulesModule = GameEnvironment.loadAiFirstAiRecordChooseRules(rulesPath);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        if (rulesModule == null) {

            throw new BusinessException("无法载入Ai first ai record choose rule file. game version" + gameVersion + ", ab test group:" + abTestGroup.getMessage());
        }

        return rulesModule;

    }

    /**
     * AI通过准确率筛选
     *
     * @param rule
     * @return
     */
    @Override
    public CalculateAIFilterParametersBO calculateAIFilterParameters(AiFirstAiRecordChooseRule rule) {

        if (rule.getAiFilterConfigs() == null || rule.getAiFilterConfigs().length == 0) {
            throw new BusinessException("AI先开枪配置AiFirstAiRecordChooseRule没有配置任何AiFilterConfig");
        }

        Double[] aiShowPrecisionRange = new Double[]{0.0, 1.0};
        List<AiFilterConfig> list = Arrays.stream(rule.getAiFilterConfigs()).collect(Collectors.toList());
        //将AiFilterConfig里面的weight求和
        int totalWeight = list.stream().mapToInt(AiFilterConfig::getWeight).sum();

        double dice = Math.random() * totalWeight;
        for (AiFilterConfig filterConfig : rule.getAiFilterConfigs()) {

            if (dice <= filterConfig.getWeight()) {
                aiShowPrecisionRange[0] = filterConfig.getShowPrecisionRange().get_min();
                aiShowPrecisionRange[1] = filterConfig.getShowPrecisionRange().get_max();
                break;
            }
            dice -= filterConfig.getWeight();
        }
        Double[] secureAIShowPrecisionRange = new Double[]{rule.getSecureAIFilterConfig().getShowPrecisionRange().get_min(), rule.getSecureAIFilterConfig().getShowPrecisionRange().get_max()};

        return new CalculateAIFilterParametersBO(aiShowPrecisionRange, secureAIShowPrecisionRange);
    }

    /**
     * 计算AI过滤参数
     *
     * @param localPlay
     * @param playerScore
     * @param playerShowPrecision
     * @return
     */
    @Override
    public LocalPlayerCalculateAIFilterParametersBO localPlayerCalculateAIFilterParameters(LocalPlayerFirstAiRecordChooseRule localPlay, Integer playerScore, Double playerShowPrecision) {

        Double[] aiShowPrecisionRange = new Double[]{0.0, 1.0};
        Integer[] aiScoreRange = new Integer[]{0, Integer.MAX_VALUE};
        Double[] secureAIShowPrecisionRange = new Double[]{0.0, 1.0};
        Integer[] secureAIScoreRange = new Integer[]{0, Integer.MAX_VALUE};
        boolean findResult = false;

        for (int i = 0; i < localPlay.getRules().length; i++) {

            Rule subRule = localPlay.getRules()[i];
            //该rule符合玩家精度
            if (subRule.getPlayerShowPrecision().get_min() <= playerShowPrecision && subRule.getPlayerShowPrecision().get_max() >= playerShowPrecision) {

                findResult = true;

                List<AiFilterConfig> list = Arrays.stream(subRule.getAiFilterConfigs()).collect(Collectors.toList());
                if (subRule.getAiFilterConfigs() != null && list.size() > 0) {

                    //subRule的weight求和
                    int totalWeight = list.stream().mapToInt(AiFilterConfig::getWeight).sum();
//                    log.warn("totalWeight:{}",totalWeight);

                    double dice = Math.random() * totalWeight;
//                    log.warn("dice:{}",dice);

                    for (AiFilterConfig filterConfig : subRule.getAiFilterConfigs()) {

                        if (dice <= filterConfig.getWeight()) {

                            LocalPlayerInAiFilterConfigBO filterConfigBO = localPlayerCalculateAIFilterParametersInAiFilterConfig(filterConfig, playerScore, playerShowPrecision);
                            aiShowPrecisionRange = filterConfigBO.getAiShowPrecisionRange();
                            aiScoreRange = filterConfigBO.getAiScoreRange();
                            break;
                        }
                        dice -= filterConfig.getWeight();
                    }

                    //计算保底AI匹配规则
                    LocalPlayerInAiFilterConfigBO secureFilterResult = localPlayerCalculateAIFilterParametersInAiFilterConfig(subRule.getSecureAIFilterConfig(), playerScore, playerShowPrecision);

                    secureAIShowPrecisionRange = secureFilterResult.getAiShowPrecisionRange();
                    secureAIScoreRange = secureFilterResult.getAiScoreRange();
                } else {

                    throw new BusinessException("AI操作过滤, " + localPlay.getDescription() + "的第" + i + "个子规则,没有配置任何AiFilterConfig");
                }

                break;

            }
        }
        LocalPlayerCalculateAIFilterParametersBO result = new LocalPlayerCalculateAIFilterParametersBO(aiShowPrecisionRange, aiScoreRange, secureAIShowPrecisionRange, secureAIScoreRange);

        return findResult ? result : null;
    }

    /**
     * 根据AiFilterConfig的配置,计算出AI的显示准确度区间和AI分数区间
     *
     * @param aiFilterConfig
     * @param playerScore
     * @param playerShowPrecision
     * @return
     */
    @Override
    public LocalPlayerInAiFilterConfigBO localPlayerCalculateAIFilterParametersInAiFilterConfig(AiFilterConfig aiFilterConfig, Integer playerScore, Double playerShowPrecision) {

        Integer[] aiScoreRange;

        //玩家如果是0分,则AI可以匹配任意分数
        if (playerScore == 0) {
            aiScoreRange = new Integer[]{0, 100000};
        } else {
            int min = (int) Math.floor(aiFilterConfig.getScoreRatioToPlayerExpectationRange().get_min() * playerScore);
            int max = (int) Math.floor(aiFilterConfig.getScoreRatioToPlayerExpectationRange().get_max() * playerScore);
            aiScoreRange = new Integer[]{min, max};
        }

        Double[] aiShowPrecisionRange = new Double[]{aiFilterConfig.getShowPrecisionRange().get_min(), aiFilterConfig.getShowPrecisionRange().get_max()};
        //精度需要额外和玩家精度对比
        if (aiFilterConfig.getShowPrecisionComparisonTypeToPlayerShowPrecision() != ComparisonType.Any) {

            switch (aiFilterConfig.getShowPrecisionComparisonTypeToPlayerShowPrecision()) {

                case Less:
                    aiShowPrecisionRange[1] = Math.min(aiShowPrecisionRange[1], playerShowPrecision - 0.1);
                    break;
                case LessEqual:
                    aiShowPrecisionRange[1] = Math.min(aiShowPrecisionRange[1], playerShowPrecision);
                    break;
                case GreaterEqual:
                    aiShowPrecisionRange[0] = Math.max(aiShowPrecisionRange[0], playerShowPrecision);
                case Greater:
                    aiShowPrecisionRange[0] = Math.max(aiShowPrecisionRange[0], playerShowPrecision + 0.1);
                    break;
            }
            //clamp in 0-1
            aiShowPrecisionRange[0] = Math.max(0, aiShowPrecisionRange[0]);
            aiShowPrecisionRange[1] = Math.min(1, aiShowPrecisionRange[1]);

        }

        return new LocalPlayerInAiFilterConfigBO(aiShowPrecisionRange, aiScoreRange);
    }


}
