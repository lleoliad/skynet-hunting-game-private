package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 子弹库类型
 */
@AllArgsConstructor
@Getter
public enum LuckyWheelV2RewardType {


    Coin(1, ""),
    RandomGunCard(2, ""),
    BlueGunCard(3, ""),
    OrangeGunCard(4, ""),
    RedGunCard(5, ""),
    FixBullet1(6, "固定子弹奖励1"),
    FixBullet2(7, "固定子弹奖励2"),
    FixBullet3(8, "固定子弹奖励3"),
    BronzeChest(9, ""),
    SilverChest(10, ""),
    ;

    private final Integer type;
    private final String message;
}
