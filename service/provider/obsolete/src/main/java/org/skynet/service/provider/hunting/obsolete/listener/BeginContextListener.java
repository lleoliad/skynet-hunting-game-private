package org.skynet.service.provider.hunting.obsolete.listener;

import org.skynet.service.provider.hunting.obsolete.DBOperation.RedisDBOperation;
import org.skynet.service.provider.hunting.obsolete.config.SystemPropertiesConfig;
import org.skynet.service.provider.hunting.obsolete.pojo.environment.GameEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * 上下文监听器
 */
@WebListener
@Slf4j
public class BeginContextListener implements ServletContextListener {

    @Autowired
    private GameEnvironment gameEnvironment;

    @Autowired
    private RedisDBOperation redisDBOperation;

    @Autowired
    private SystemPropertiesConfig systemPropertiesConfig;

    /**
     * 加载上下文
     */
    public void contextInitialized(ServletContextEvent event) {

        try {
            gameEnvironment.init(event, systemPropertiesConfig);
        } catch (Exception e) {

            log.error("服务器启动异常");
            e.printStackTrace();
        }
    }
}
