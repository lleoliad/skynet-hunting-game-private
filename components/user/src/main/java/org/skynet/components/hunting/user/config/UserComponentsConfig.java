package org.skynet.components.hunting.user.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@Configuration
@EnableTransactionManagement // 添加 MySQL 事件管理
// @MapperScan(basePackages = {
//         "org.skynet.components.cache.dao.mapper"
// })
@ComponentScan(basePackages = {
        "org.skynet.starter.config",
        "org.skynet.starter.logging.config",
        "org.skynet.starter.scheduler.executor",
        "org.skynet.commons.context",
})
@EnableFeignClients(basePackages = {
        "org.skynet.components.hunting.user.service"
})
@RefreshScope
@EnableCaching
// @EnableKafka
// @EnableKafkaStreams
public class UserComponentsConfig {
}
