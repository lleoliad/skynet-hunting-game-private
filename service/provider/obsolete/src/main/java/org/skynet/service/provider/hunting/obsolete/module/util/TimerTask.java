package org.skynet.service.provider.hunting.obsolete.module.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import org.skynet.components.hunting.battle.query.OfflineQuery;
import org.skynet.components.hunting.battle.service.BattleFeignService;
import org.skynet.service.provider.hunting.obsolete.common.util.HttpUtil;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.module.dto.BaseDTO;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;


@Slf4j
@Component
public class TimerTask {

    @Resource
    private SystemPropertiesConfig systemPropertiesConfig;

    @Resource
    private BattleFeignService battleFeignService;

    /**
     * @Author: SHAOXINYU
     * @Description: 定时任务，每隔15分钟执行一次，从在线用户列表中移除离线用户（10分钟心跳检测）
     * @Date: 2023/1/31 11:26
     */
    @Scheduled(cron = "0 0/15 * * * ? ")
    public void checkOnlineUser() {
        // String fightUrl = systemPropertiesConfig.getFightUrl();
        GameEnvironment.onlineUser.forEach((userUid, lastLoginDate) -> {
            if (DateUtil.between(lastLoginDate, new Date(), DateUnit.MINUTE) > 10) {
                log.info("用户{}下线，下线时间为：{}，当前时间为：{}", userUid, lastLoginDate, new Date());
                GameEnvironment.onlineUser.remove(userUid);
                // HttpUtil.getFightInfo(fightUrl + "/user/offline", new BaseDTO(null, userUid));
                battleFeignService.userOffline(OfflineQuery.builder().userId(userUid).build());

            }
        });
    }


}
