package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.service.provider.hunting.obsolete.pojo.bo.LuckyWheelV2SpinRewardBO;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.LuckyWheelSpinReward;

public interface LuckyWheelService {


    /**
     * 获得转盘旋转一次的奖励
     *
     * @param userUid
     * @param gameVersion
     * @return
     */
    LuckyWheelSpinReward spinLuckyWheelOnceReward(String userUid, String gameVersion);

    /**
     * 尝试刷新转盘的内容和累计奖励
     *
     * @param userUid
     */
    void tryRefreshLuckyWheelContents(String userUid);

    /**
     * 刷新免费转盘次数
     *
     * @param userUid
     * @param gameVersion
     */
    void refreshFreeSpinCount(String userUid, String gameVersion);

    /**
     * 刷新下一次免费转盘获取时间
     *
     * @param userUid
     * @param gameVersion
     */
    void refreshNextFreeSpinTime(String userUid, String gameVersion);

    /**
     * 刷新免费转盘次数
     *
     * @param userUid
     * @param gameVersion
     */
    void refreshLuckyWheelV1FreeSpinCount(String userUid, String gameVersion);

    /**
     * 刷新转盘v2表盘内容
     *
     * @param userUid
     * @param gameVersion
     */
    void refreshLuckyWheelV2Content(String userUid, String gameVersion);

    /**
     * 随机一个表盘内容id
     *
     * @param gameVersion
     */
    Integer getRandomLuckyWheelV2SectorContentId(String gameVersion);

    /**
     * 获取转盘v2表盘内容
     *
     * @param userUid
     * @param gameVersion
     */
    LuckyWheelV2SpinRewardBO spinLuckyWheelV2Reward(String userUid, String gameVersion);
}
