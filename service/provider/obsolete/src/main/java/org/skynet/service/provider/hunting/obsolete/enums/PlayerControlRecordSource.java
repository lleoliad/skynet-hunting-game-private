package org.skynet.service.provider.hunting.obsolete.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.msgpack.annotation.MessagePackOrdinalEnum;

/**
 * 玩家控制记录源头
 */
@AllArgsConstructor
@Getter
@MessagePackOrdinalEnum
public enum PlayerControlRecordSource {

    //枚举类的下标需要从1开始，所以在最前面加了一个无意义的参数
    ToFrom1(0),
    Player(1),
    Admin_UnityEditor(2),
    ;

    private Integer type;

}
