package com.ukastar.api.audit;

import com.ukastar.api.audit.vo.AuditEventResponse;
import com.ukastar.api.audit.vo.AuditRecordRequest;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.service.audit.AuditService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 审计日志接口。
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_AUDIT_VIEW')")
    public Mono<ApiResponse<AuditEventResponse>> record(@RequestBody Mono<@Valid AuditRecordRequest> requestMono) {
        return requestMono.flatMap(request -> auditService.recordEvent(request.tenantId(), request.eventType(), request.actor(), request.target(), request.summary()))
                .map(event -> new AuditEventResponse(event.id(), event.tenantId(), event.eventType(), event.actor(), event.target(), event.summary(), event.occurredAt()))
                .map(ApiResponse::success);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_AUDIT_VIEW')")
    public Flux<AuditEventResponse> listByTenant(@RequestParam Long tenantId) {
        return auditService.listByTenant(tenantId)
                .map(event -> new AuditEventResponse(event.id(), event.tenantId(), event.eventType(), event.actor(), event.target(), event.summary(), event.occurredAt()));
    }

    @GetMapping("/type/{eventType}")
    @PreAuthorize("hasAuthority('PERM_AUDIT_VIEW')")
    public Flux<AuditEventResponse> listByType(@PathVariable String eventType) {
        return auditService.listByEventType(eventType)
                .map(event -> new AuditEventResponse(event.id(), event.tenantId(), event.eventType(), event.actor(), event.target(), event.summary(), event.occurredAt()));
    }
}
