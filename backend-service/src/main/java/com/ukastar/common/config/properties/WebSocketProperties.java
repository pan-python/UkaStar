package com.ukastar.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

/**
 * WebSocket 服务配置预留。
 */
@ConfigurationProperties(prefix = "ws")
public record WebSocketProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("/ws") String path,
        @DefaultValue("PT5M") Duration idleTimeout,
        @DefaultValue("64KB") DataSize maxTextMessageSize
) {
}
