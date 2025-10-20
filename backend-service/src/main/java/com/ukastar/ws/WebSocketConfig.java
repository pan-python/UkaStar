package com.ukastar.ws;

import com.ukastar.common.config.properties.WebSocketProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties({WebSocketProperties.class, com.ukastar.common.config.properties.DeepSeekProperties.class})
@ConditionalOnProperty(prefix = "ws", name = "enabled", havingValue = "true")
public class WebSocketConfig {

    @Bean
    public HandlerMapping wsHandlerMapping(WebSocketProperties props, WebSocketHandler chatWebSocketHandler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put(props.path(), chatWebSocketHandler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        mapping.setOrder(-1); // before annotated controllers
        return mapping;
    }
}
