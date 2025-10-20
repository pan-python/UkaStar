package com.ukastar.api.miniapp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.common.config.properties.WechatMiniappProperties;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.domain.account.Account;
import com.ukastar.infra.rbac.InMemoryRbacStore;
import com.ukastar.persistence.entity.AccountEntity;
import com.ukastar.persistence.entity.AccountRoleEntity;
import com.ukastar.persistence.entity.RoleEntity;
import com.ukastar.persistence.mapper.AccountMapper;
import com.ukastar.persistence.mapper.AccountRoleMapper;
import com.ukastar.persistence.mapper.RoleMapper;
import com.ukastar.security.jwt.JwtService;
import com.ukastar.security.jwt.TokenPair;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/miniapp")
@ConditionalOnProperty(prefix = "wechat.miniapp", name = "enabled", havingValue = "true")
public class MiniappAuthController {

    record LoginRequest(@NotBlank String code, Long tenantId) {}
    record LoginResponse(String openid, String sessionKey, String accessToken, String refreshToken) {}

    private final WechatMiniappProperties props;
    private final WebClient.Builder webClientBuilder;
    private final AccountMapper accountMapper;
    private final RoleMapper roleMapper;
    private final AccountRoleMapper accountRoleMapper;
    private final InMemoryRbacStore rbacStore;
    private final JwtService jwtService;

    public MiniappAuthController(WechatMiniappProperties props,
                                 WebClient.Builder webClientBuilder,
                                 AccountMapper accountMapper,
                                 RoleMapper roleMapper,
                                 AccountRoleMapper accountRoleMapper,
                                 InMemoryRbacStore rbacStore,
                                 JwtService jwtService) {
        this.props = props;
        this.webClientBuilder = webClientBuilder;
        this.accountMapper = accountMapper;
        this.roleMapper = roleMapper;
        this.accountRoleMapper = accountRoleMapper;
        this.rbacStore = rbacStore;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ApiResponse<LoginResponse>> login(@RequestBody Mono<LoginRequest> reqMono) {
        return reqMono.flatMap(req -> {
            Long tenantId = req.tenantId() != null ? req.tenantId() : 1L;
            return code2Session(req.code()).flatMap(cx -> ensureAccountAndToken(tenantId, cx.openid(), cx.sessionKey()))
                    .map(ApiResponse::success);
        });
    }

    private record WxSession(String openid, String sessionKey) {}

    private Mono<WxSession> code2Session(String code) {
        String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                props.appId(), props.secret(), code);
        return webClientBuilder.build().get().uri(url).retrieve()
                .bodyToMono(Map.class)
                .flatMap(map -> {
                    Object err = map.get("errcode");
                    if (err != null && ((Number)err).intValue() != 0) {
                        String msg = String.valueOf(map.get("errmsg"));
                        return Mono.error(new IllegalStateException("wx error: " + msg));
                    }
                    String openid = String.valueOf(map.get("openid"));
                    String sessionKey = String.valueOf(map.get("session_key"));
                    return Mono.just(new WxSession(openid, sessionKey));
                });
    }

    private Mono<LoginResponse> ensureAccountAndToken(Long tenantId, String openid, String sessionKey) {
        return Mono.fromCallable(() -> {
            String username = "wx_" + openid;
            AccountEntity entity = accountMapper.selectOne(new QueryWrapper<AccountEntity>()
                    .eq("tenant_id", tenantId)
                    .eq("username", username));
            if (entity == null) {
                entity = new AccountEntity();
                entity.setTenantId(tenantId);
                entity.setUsername(username);
                entity.setPasswordHash("");
                entity.setStatus(1);
                accountMapper.insert(entity);
                // 绑定只读角色（TENANT_VIEWER）
                RoleEntity viewer = roleMapper.selectOne(new QueryWrapper<RoleEntity>().eq("tenant_id", tenantId).eq("code", "TENANT_VIEWER"));
                if (viewer != null) {
                    AccountRoleEntity ar = new AccountRoleEntity();
                    ar.setTenantId(tenantId);
                    ar.setAccountId(entity.getId());
                    ar.setRoleId(viewer.getId());
                    try { accountRoleMapper.insert(ar); } catch (Exception ignored) {}
                }
            }
            Set<String> roleCodes = Set.of("TENANT_VIEWER");
            Set<String> permCodes = rbacStore.permissionCodesForRoles(roleCodes);
            Set<String> authorities = rbacStore.authoritiesFor(roleCodes, permCodes);
            Account domain = new Account(entity.getId(), tenantId, entity.getUsername(), entity.getPasswordHash(), roleCodes, permCodes, authorities, com.ukastar.domain.rbac.DataScope.SELF, true, 0L);
            TokenPair pair = jwtService.generateTokenPair(domain);
            return new LoginResponse(openid, sessionKey, pair.accessToken(), pair.refreshToken());
        });
    }
}

