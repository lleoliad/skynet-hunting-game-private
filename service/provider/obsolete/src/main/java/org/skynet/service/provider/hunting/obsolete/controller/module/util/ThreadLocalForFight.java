package org.skynet.service.provider.hunting.obsolete.controller.module.util;

import org.skynet.service.provider.hunting.obsolete.controller.module.dto.BattleCompleteDto;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ThreadLocalForFight {

    public static ThreadLocal<Object> localVar = new ThreadLocal<>();


    public static void setBattleCompleteDto(BattleCompleteDto battleCompleteDto) {
        if (battleCompleteDto != null) {
            localVar.set(battleCompleteDto);
        }
    }


    public static BattleCompleteDto getBattleCompleteDto() {
        if (localVar.get() != null) {
//            log.warn("从ThreadLocal中取出的值：{}",localVar.get());
            return (BattleCompleteDto) localVar.get();
        }
//        log.warn("ThreadLocal中的值为空，返回0");
        return null;
    }

    public static void remove() {
        if (localVar.get() != null) {
            localVar.remove();
//            log.warn("移除ThreadLocal中的值");
        }
    }


}
