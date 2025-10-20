package com.ukastar.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * MinIO 相关配置预留。
 */
@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
        @DefaultValue("false") boolean enabled,
        String endpoint,
        String accessKey,
        String secretKey,
        @DefaultValue("ukastar") String bucket,
        String region
) {
}
