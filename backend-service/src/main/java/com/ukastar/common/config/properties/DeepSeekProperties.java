package com.ukastar.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * DeepSeek AI 接入配置预留。
 */
@ConfigurationProperties(prefix = "ai.deepseek")
public record DeepSeekProperties(
        @DefaultValue("false") boolean enabled,
        String apiKey,
        @DefaultValue("https://dashscope.aliyuncs.com/compatible-mode/v1") String baseUrl,
        @DefaultValue("deepseek-chat") String model
) {
}
