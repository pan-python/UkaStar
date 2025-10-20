package com.ukastar.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Sentinel 流量防护配置预留。
 */
@ConfigurationProperties(prefix = "sentinel")
public record SentinelProperties(
        @DefaultValue("false") boolean enabled,
        String dashboard,
        @DefaultValue("backend-service") String projectName,
        @DefaultValue("PT5S") Duration heartbeatInterval
) {
}
