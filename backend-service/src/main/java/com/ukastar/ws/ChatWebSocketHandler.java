package com.ukastar.ws;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.common.config.properties.DeepSeekProperties;
import com.ukastar.persistence.entity.ChatMessageEntity;
import com.ukastar.persistence.entity.ChatSessionEntity;
import com.ukastar.persistence.mapper.ChatMessageMapper;
import com.ukastar.persistence.mapper.ChatSessionMapper;
import com.ukastar.security.jwt.JwtService;
import com.ukastar.security.jwt.TokenDetails;
import com.ukastar.security.jwt.TokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/**
 * 初版聊天 WS 处理器：校验 token（query 参数 token=...），通过后回显消息。
 * 后续将接入 DeepSeek 并持久化会话/消息。
 */
@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final JwtService jwtService;
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final WebClient webClient;
    private final DeepSeekProperties deepSeek;

    public ChatWebSocketHandler(JwtService jwtService,
                                ChatSessionMapper sessionMapper,
                                ChatMessageMapper messageMapper,
                                WebClient.Builder webClientBuilder,
                                DeepSeekProperties deepSeek) {
        this.jwtService = jwtService;
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.deepSeek = deepSeek;
        this.webClient = webClientBuilder.baseUrl(deepSeek.baseUrl())
                .defaultHeader("Authorization", "Bearer " + (deepSeek.apiKey() == null ? "" : deepSeek.apiKey()))
                .build();
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // 简单鉴权：从 query 参数读取 token
        if (!isAuthorized(session)) {
            return session.close(CloseStatus.POLICY_VIOLATION);
        }

        // 处理：拉取/创建会话，接收用户消息 -> 调用 DeepSeek -> 返回应答并落库
        Flux<WebSocketMessage> flow = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(msg -> handleMessage(session, msg))
                .onErrorResume(ex -> {
                    log.warn("ws error: {}", ex.getMessage());
                    return Mono.just(session.textMessage("[error] " + ex.getMessage()));
                });

        return session.send(flow);
    }

    private boolean isAuthorized(WebSocketSession session) {
        try {
            URI uri = session.getHandshakeInfo().getUri();
            MultiValueMap<String, String> queryParams = org.springframework.web.util.UriComponentsBuilder.fromUri(uri).build().getQueryParams();
            String token = queryParams.getFirst("token");
            if (token == null || token.isBlank()) {
                log.warn("ws auth failed: missing token");
                return false;
            }
            TokenDetails details = jwtService.parse(token);
            if (details.tokenType() != TokenType.ACCESS) {
                log.warn("ws auth failed: not access token");
                return false;
            }
            // 简要过期校验由 parse 完成；后续可补充租户/账号状态校验
            log.info("ws auth ok: subject={}, tenant={}", details.subject(), Objects.toString(details.tenantId(), "-"));
            return true;
        } catch (Exception e) {
            log.warn("ws auth exception: {}", e.getMessage());
            return false;
        }
    }

    private Mono<WebSocketMessage> handleMessage(WebSocketSession ws, String userContent) {
        URI uri = ws.getHandshakeInfo().getUri();
        var params = org.springframework.web.util.UriComponentsBuilder.fromUri(uri).build().getQueryParams();
        String token = params.getFirst("token");
        String sessionCode = params.getFirst("session");
        if (sessionCode == null || sessionCode.isBlank()) {
            sessionCode = java.util.UUID.randomUUID().toString();
        }
        TokenDetails details = jwtService.parse(token);
        Long tenantId = details.tenantId();
        Long accountId = details.accountId();

        // 创建/获取会话
        String finalSessionCode = sessionCode;
        ChatSessionEntity sessionEntity = sessionMapper.selectOne(new QueryWrapper<ChatSessionEntity>()
                .eq("tenant_id", tenantId)
                .eq("account_id", accountId)
                .eq("session_code", finalSessionCode));
        if (sessionEntity == null) {
            sessionEntity = new ChatSessionEntity();
            sessionEntity.setTenantId(tenantId);
            sessionEntity.setAccountId(accountId);
            sessionEntity.setSessionCode(finalSessionCode);
            sessionEntity.setTitle("Chat");
            sessionEntity.setMaxContext(20);
            sessionEntity.setIsActive(1);
            sessionMapper.insert(sessionEntity);
        }
        Long sessionId = sessionEntity.getId();

        // 保存用户消息
        ChatMessageEntity userMsg = new ChatMessageEntity();
        userMsg.setTenantId(tenantId);
        userMsg.setSessionId(sessionId);
        userMsg.setMessageRole("USER");
        userMsg.setContent(userContent);
        messageMapper.insert(userMsg);

        // 组装上下文
        int max = sessionEntity.getMaxContext() == null ? 20 : sessionEntity.getMaxContext();
        List<ChatMessageEntity> recent = messageMapper.selectList(new QueryWrapper<ChatMessageEntity>()
                .eq("session_id", sessionId)
                .orderByDesc("id")
                .last("limit " + max));
        java.util.Collections.reverse(recent);
        var messages = new java.util.ArrayList<java.util.Map<String, Object>>();
        for (ChatMessageEntity m : recent) {
            String role = switch (m.getMessageRole()) {
                case "USER" -> "user";
                case "ASSISTANT" -> "assistant";
                default -> "system";
            };
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("role", role);
            item.put("content", m.getContent());
            messages.add(item);
        }

        // 调用 DeepSeek（非流式，简化集成）
        if (!deepSeek.enabled()) {
            String reply = "[DeepSeek disabled] " + userContent;
            persistAssistant(tenantId, sessionId, reply);
            return Mono.just(ws.textMessage(reply));
        }
        var payload = java.util.Map.of(
                "model", deepSeek.model(),
                "messages", messages,
                "stream", false
        );
        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .map(body -> extractContent(body))
                .doOnNext(reply -> persistAssistant(tenantId, sessionId, reply))
                .map(ws::textMessage);
    }

    private void persistAssistant(Long tenantId, Long sessionId, String content) {
        ChatMessageEntity aiMsg = new ChatMessageEntity();
        aiMsg.setTenantId(tenantId);
        aiMsg.setSessionId(sessionId);
        aiMsg.setMessageRole("ASSISTANT");
        aiMsg.setContent(content);
        messageMapper.insert(aiMsg);
    }

    private String extractContent(String json) {
        // 朴素提取（避免引入额外 JSON 依赖）：寻找 "content":"..."
        try {
            int idx = json.indexOf("\"content\":");
            if (idx < 0) return json;
            int start = json.indexOf('"', idx + 10) + 1;
            int end = json.indexOf('"', start);
            if (start > 0 && end > start) {
                return json.substring(start, end);
            }
            return json;
        } catch (Exception e) {
            return json;
        }
    }
}
