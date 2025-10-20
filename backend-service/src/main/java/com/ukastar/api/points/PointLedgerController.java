package com.ukastar.api.points;

import com.ukastar.api.points.vo.PointBalanceResponse;
import com.ukastar.api.points.vo.PointOperationRequest;
import com.ukastar.api.points.vo.PointRecordResponse;
import com.ukastar.api.points.vo.PointStatisticsResponse;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.domain.points.PointBalance;
import com.ukastar.domain.points.PointRecord;
import com.ukastar.service.points.PointLedgerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 积分操作接口。
 */
@RestController
@RequestMapping("/api/points")
public class PointLedgerController {

    private final PointLedgerService pointLedgerService;

    public PointLedgerController(PointLedgerService pointLedgerService) {
        this.pointLedgerService = pointLedgerService;
    }

    @PostMapping("/award")
    @PreAuthorize("hasAuthority('PERM_POINTS_OPERATE')")
    public Mono<ApiResponse<PointBalanceResponse>> award(@RequestBody Mono<@Valid PointOperationRequest> requestMono) {
        return requestMono.flatMap(request -> pointLedgerService.award(request.childId(), request.amount(), request.operatorAccountId(), request.reason()))
                .map(this::mapBalance)
                .map(ApiResponse::success);
    }

    @PostMapping("/deduct")
    @PreAuthorize("hasAuthority('PERM_POINTS_OPERATE')")
    public Mono<ApiResponse<PointBalanceResponse>> deduct(@RequestBody Mono<@Valid PointOperationRequest> requestMono) {
        return requestMono.flatMap(request -> pointLedgerService.deduct(request.childId(), request.amount(), request.operatorAccountId(), request.reason()))
                .map(this::mapBalance)
                .map(ApiResponse::success);
    }

    @PostMapping("/redeem")
    @PreAuthorize("hasAuthority('PERM_POINTS_OPERATE')")
    public Mono<ApiResponse<PointBalanceResponse>> redeem(@RequestBody Mono<@Valid PointOperationRequest> requestMono) {
        return requestMono.flatMap(request -> pointLedgerService.redeem(request.childId(), request.amount(), request.operatorAccountId(), request.reason()))
                .map(this::mapBalance)
                .map(ApiResponse::success);
    }

    @GetMapping("/records")
    @PreAuthorize("hasAuthority('PERM_POINTS_VIEW')")
    public Flux<PointRecordResponse> listRecords(@RequestParam(required = false) Long tenantId, @RequestParam(required = false) Long childId) {
        if (childId != null) {
            return pointLedgerService.listRecordsByChild(childId).map(this::mapRecord);
        }
        if (tenantId == null) {
            return Flux.empty();
        }
        return pointLedgerService.listRecordsByTenant(tenantId).map(this::mapRecord);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('PERM_POINTS_VIEW')")
    public Mono<PointStatisticsResponse> statistics(@RequestParam Long tenantId) {
        return pointLedgerService.statistics(tenantId)
                .map(stats -> new PointStatisticsResponse(stats.todayCount(), stats.todayNetScore(), stats.totalChildren(), stats.totalPoints(), stats.weeklyNetScore()));
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAuthority('PERM_POINTS_VIEW')")
    public Mono<ApiResponse<PointBalanceResponse>> balance(@RequestParam Long childId) {
        return pointLedgerService
                .listRecordsByChild(childId) // 若仓储提供 balance 方法更佳；此处先通过已有服务组合
                .collectList()
                .map(list -> {
                    if (list.isEmpty()) {
                        return new PointBalanceResponse(childId, null, 0);
                    }
                    var last = list.get(list.size() - 1);
                    return new PointBalanceResponse(last.childId(), last.tenantId(), last.balanceAfter());
                })
                .map(ApiResponse::success);
    }

    private PointBalanceResponse mapBalance(PointBalance balance) {
        return new PointBalanceResponse(balance.childId(), balance.tenantId(), balance.balance());
    }

    private PointRecordResponse mapRecord(PointRecord record) {
        return new PointRecordResponse(record.id(), record.tenantId(), record.childId(), record.operatorAccountId(), record.actionType(), record.amount(), record.balanceAfter(), record.reason(), record.occurredAt());
    }
}
