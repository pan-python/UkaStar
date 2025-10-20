package com.ukastar.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "wechat.miniapp")
public record WechatMiniappProperties(
        @DefaultValue("false") boolean enabled,
        String appId,
        String secret
) {}

