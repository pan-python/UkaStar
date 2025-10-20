package com.ukastar.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ocr.proxy")
public record OcrProxyProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("http://localhost:9000") String baseUrl,
        String token,
        @DefaultValue("X-Internal-Token") String tokenHeader
) {}

