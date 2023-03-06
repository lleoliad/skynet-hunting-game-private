package org.skynet.server.hunting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
public class GameServer {
    public static void main(String[] args) {
        SpringApplication.run(GameServer.class, args);
    }

    // @PostConstruct
    // void started() {
    //     TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    // }
}