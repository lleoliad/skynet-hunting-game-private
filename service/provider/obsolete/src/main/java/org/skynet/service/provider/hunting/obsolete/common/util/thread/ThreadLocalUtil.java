package org.skynet.service.provider.hunting.obsolete.common.util.thread;

import org.skynet.service.provider.hunting.obsolete.common.util.ApplicationContextUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadLocalUtil {

    public static ThreadLocal<Long> localVar = new ThreadLocal<>();

    public static void set(Long time) {
        SystemPropertiesConfig bean = ApplicationContextUtil.getBean(SystemPropertiesConfig.class);
        if (!bean.getProduction()) {
            if (time != null) {
                // log.warn("非生产环境，向ThreadLocal中添加值：{}", time);
                localVar.set(time);
            }
        } else {
//            log.warn("生产环境，直接置0");
            localVar.set(0L);
        }
    }


    public static Long get() {
        if (localVar.get() != null) {
//            log.warn("从ThreadLocal中取出的值：{}",localVar.get());
            return localVar.get();
        }
//        log.warn("ThreadLocal中的值为空，返回0");
        return 0L;
    }

    public static void remove() {
        if (localVar.get() != null) {
            localVar.remove();
//            log.warn("移除ThreadLocal中的值");
        }
    }

}
