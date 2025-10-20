package com.ukastar.infra.web;

import com.ukastar.common.trace.TraceIdHolder;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 统一的 TraceId 过滤器，负责生成/透传 TraceId 并注入日志 MDC。
 */
@Component
public class TraceIdWebFilter implements WebFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String incomingTraceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
        String traceId = StringUtils.hasText(incomingTraceId) ? incomingTraceId : UUID.randomUUID().toString();
        exchange.getResponse().getHeaders().set(TRACE_ID_HEADER, traceId);
        MDC.put(TraceIdHolder.MDC_KEY, traceId);
        TraceIdHolder.set(traceId);

        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(TraceIdHolder.CONTEXT_KEY, traceId))
                .doOnEach(signal -> {
                    if (signal.isOnComplete()) {
                        return;
                    }
                    try {
                        String currentTraceId = signal.getContextView().getOrDefault(TraceIdHolder.CONTEXT_KEY, traceId);
                        MDC.put(TraceIdHolder.MDC_KEY, currentTraceId);
                        TraceIdHolder.set(currentTraceId);
                    } catch (Exception ignored) {
                        MDC.put(TraceIdHolder.MDC_KEY, traceId);
                        TraceIdHolder.set(traceId);
                    }
                })
                .doFinally(signalType -> {
                    MDC.remove(TraceIdHolder.MDC_KEY);
                    TraceIdHolder.clear();
                });
    }
}
