package com.ukastar.api.miniapp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.persistence.entity.ChatSessionEntity;
import com.ukastar.persistence.mapper.ChatSessionMapper;
import com.ukastar.security.jwt.JwtService;
import com.ukastar.security.jwt.TokenDetails;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat/sessions")
public class ChatSessionController {

    record NewSessionRequest(String title) {}

    private final ChatSessionMapper sessionMapper;
    private final JwtService jwtService;

    public ChatSessionController(ChatSessionMapper sessionMapper, JwtService jwtService) {
        this.sessionMapper = sessionMapper;
        this.jwtService = jwtService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> list(@RequestHeader("Authorization") String auth){
        TokenDetails td = parse(auth);
        List<ChatSessionEntity> sessions = sessionMapper.selectList(new QueryWrapper<ChatSessionEntity>()
                .eq("tenant_id", td.tenantId()).eq("account_id", td.accountId()).orderByDesc("id"));
        Map<String, Object> resp = new HashMap<>();
        resp.put("sessions", sessions.stream().map(se -> Map.of(
                "sessionCode", se.getSessionCode(),
                "title", se.getTitle()
        )).toList());
        return resp;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> create(@RequestHeader("Authorization") String auth, @RequestBody NewSessionRequest req){
        TokenDetails td = parse(auth);
        ChatSessionEntity se = new ChatSessionEntity();
        se.setTenantId(td.tenantId());
        se.setAccountId(td.accountId());
        se.setSessionCode(java.util.UUID.randomUUID().toString());
        se.setTitle(req.title()==null?"Chat":req.title());
        se.setMaxContext(20);
        se.setIsActive(1);
        sessionMapper.insert(se);
        return Map.of("sessionCode", se.getSessionCode());
    }

    private TokenDetails parse(String auth){
        String token = auth != null && auth.toLowerCase().startsWith("bearer ") ? auth.substring(7) : auth;
        return jwtService.parse(token);
    }
}

