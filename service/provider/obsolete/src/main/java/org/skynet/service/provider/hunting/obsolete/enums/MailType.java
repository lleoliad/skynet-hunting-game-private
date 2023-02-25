package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MailType {

    SystemNotification("systemNotification", "系统公告"),
    DistributeItems("distributeItems", "物品发放"),
    ActivityAd("activityAd", "活动广告"),
    FreeCoins("freeCoins", "免费金币"),
    BattleReplay("battleReplay", "对局回放"),
    ;

    private final String type;
    private final String description;
}
