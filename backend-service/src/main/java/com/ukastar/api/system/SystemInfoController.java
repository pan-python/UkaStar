package com.ukastar.api.system;

import com.ukastar.api.system.vo.SystemEchoRequest;
import com.ukastar.api.system.vo.SystemEchoResponse;
import com.ukastar.api.system.vo.SystemInfoResponse;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.service.system.SystemInfoService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 系统信息基础控制器，用于验证脚手架链路。
 */
@RestController
@RequestMapping("/api/system")
public class SystemInfoController {

    private final SystemInfoService systemInfoService;

    public SystemInfoController(SystemInfoService systemInfoService) {
        this.systemInfoService = systemInfoService;
    }

    @GetMapping("/info")
    @PreAuthorize("hasAuthority('PERM_SYSTEM_INFO_VIEW')")
    public Mono<ApiResponse<SystemInfoResponse>> systemInfo() {
        return systemInfoService.current()
                .map(info -> new SystemInfoResponse(info.applicationName(), info.version(), info.buildTime()))
                .map(ApiResponse::success);
    }

    @PostMapping("/echo")
    @PreAuthorize("hasAuthority('PERM_SYSTEM_INFO_ECHO')")
    public Mono<ApiResponse<SystemEchoResponse>> echo(@RequestBody Mono<@Valid SystemEchoRequest> requestMono) {
        return requestMono
                .map(request -> new SystemEchoResponse(request.message()))
                .map(ApiResponse::success);
    }
}
