package com.ukastar.api.miniapp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.persistence.entity.ChatMessageEntity;
import com.ukastar.persistence.entity.ChatSessionEntity;
import com.ukastar.persistence.mapper.ChatMessageMapper;
import com.ukastar.persistence.mapper.ChatSessionMapper;
import com.ukastar.security.jwt.TokenDetails;
import com.ukastar.security.jwt.JwtService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final JwtService jwtService;

    public ChatHistoryController(ChatSessionMapper sessionMapper, ChatMessageMapper messageMapper, JwtService jwtService) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.jwtService = jwtService;
    }

    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> history(@RequestHeader("Authorization") String auth,
                                       @RequestParam String session,
                                       @RequestParam(defaultValue = "50") int limit) {
        String token = auth != null && auth.toLowerCase().startsWith("bearer ") ? auth.substring(7) : auth;
        TokenDetails td = jwtService.parse(token);
        ChatSessionEntity se = sessionMapper.selectOne(new QueryWrapper<ChatSessionEntity>()
                .eq("tenant_id", td.tenantId()).eq("account_id", td.accountId()).eq("session_code", session));
        Map<String, Object> resp = new HashMap<>();
        if (se == null) { resp.put("messages", List.of()); resp.put("maxContext", 0); return resp; }
        List<ChatMessageEntity> msgs = messageMapper.selectList(new QueryWrapper<ChatMessageEntity>()
                .eq("session_id", se.getId())
                .orderByAsc("id")
                .last("limit " + Math.max(1, Math.min(limit, 200))));
        List<Map<String, Object>> out = new java.util.ArrayList<>();
        for (ChatMessageEntity m : msgs) {
            Map<String, Object> x = new HashMap<>();
            x.put("role", m.getMessageRole());
            x.put("content", m.getContent());
            out.add(x);
        }
        resp.put("messages", out);
        resp.put("maxContext", se.getMaxContext());
        return resp;
    }
}
