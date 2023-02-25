package org.skynet.server.hunting.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ComponentScan(basePackages = {
        "org.skynet.service.provider.hunting",
})
@RefreshScope
public class GameConfig {
}
