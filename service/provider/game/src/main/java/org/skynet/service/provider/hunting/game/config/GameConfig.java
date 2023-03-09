package org.skynet.service.provider.hunting.game.config;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@Configuration
@EnableTransactionManagement // 添加 MySQL 事件管理
@MapperScan(basePackages = {
        "org.skynet.components.cache.dao.mapper",
        "org.skynet.service.provider.hunting.game.dao.mapper",
})
@ComponentScan(basePackages = {
        "org.skynet.starter.config",
        "org.skynet.starter.logging.config",
        "org.skynet.starter.scheduler.executor",
        "org.skynet.commons.context",
        "org.skynet.starter.quartz",
        "org.skynet.commons.db.mysql",
        "org.skynet.commons.db.mongodb",
        "org.skynet.commons.db.redis.service",
        "org.skynet.starter.codis",
})
@EnableMongoRepositories(basePackages = {
        "org.skynet.service.provider.hunting.game.dao.repository"
})
// @EnableFeignClients(basePackages = {
//         "org.skynet.components.hunting.rank.league.service",
//         "org.skynet.components.hunting.robot.service",
//         "org.skynet.service.provider.hunting.obsolete.service"
// })
@RefreshScope
@EnableCaching
// @EnableKafka
// @EnableKafkaStreams
public class GameConfig {
}
