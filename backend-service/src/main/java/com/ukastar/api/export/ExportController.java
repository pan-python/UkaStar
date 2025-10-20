package com.ukastar.api.export;

import com.ukastar.api.export.vo.ExportRequest;
import com.ukastar.api.export.vo.ExportResponse;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.service.points.PointLedgerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 导出接口示例，将积分流水导出为 CSV Base64。
 */
@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final PointLedgerService pointLedgerService;

    public ExportController(PointLedgerService pointLedgerService) {
        this.pointLedgerService = pointLedgerService;
    }

    @PostMapping("/points")
    @PreAuthorize("hasAuthority('PERM_EXPORT_EXECUTE')")
    public Mono<ApiResponse<ExportResponse>> exportPoints(@RequestBody Mono<@Valid ExportRequest> requestMono) {
        return requestMono.flatMap(request -> pointLedgerService.listRecordsByTenant(request.tenantId()).collectList()
                .map(records -> {
                    StringBuilder builder = new StringBuilder("id,family_id,action,amount,balance_after,occurred_at\n");
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
                    records.forEach(record -> builder.append(record.id()).append(',')
                            .append(record.familyId()).append(',')
                            .append(record.actionType()).append(',')
                            .append(record.amount()).append(',')
                            .append(record.balanceAfter()).append(',')
                            .append(formatter.format(record.occurredAt()))
                            .append('\n'));
                    String base64 = Base64.getEncoder().encodeToString(builder.toString().getBytes(StandardCharsets.UTF_8));
                    return new ExportResponse("points-export.csv", base64);
                }))
                .map(ApiResponse::success);
    }
}
