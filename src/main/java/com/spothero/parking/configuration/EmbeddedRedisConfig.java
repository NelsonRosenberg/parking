package com.spothero.parking.configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

@Slf4j
@Configuration
public class EmbeddedRedisConfig {

    @Value("${spring.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void init() {
        try {
            redisServer = RedisServer.builder()
                    .port(redisPort)
                    .build();
            redisServer.start();
            log.info("Started redis server on port " + String.valueOf(redisPort));
        } catch (Exception ex) {
            log.error("Could not start redis server, port is in use or server already started.");
        }
    }

    @PreDestroy
    public void destroy() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

}
