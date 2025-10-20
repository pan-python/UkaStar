package com.ukastar.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 提供项目级通用基础 Bean。
 */
@Configuration
public class CommonBeansConfiguration {

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}
