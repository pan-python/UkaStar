package com.ukastar.common.trace;

/**
 * 使用 ThreadLocal 保存当前请求的 TraceId，用于日志与返回体携带。
 */
public final class TraceIdHolder {

    public static final String CONTEXT_KEY = "traceId";
    public static final String MDC_KEY = "traceId";
    private static final ThreadLocal<String> TRACE_ID_LOCAL = new ThreadLocal<>();

    private TraceIdHolder() {
    }

    public static void set(String traceId) {
        TRACE_ID_LOCAL.set(traceId);
    }

    public static String get() {
        return TRACE_ID_LOCAL.get();
    }

    public static void clear() {
        TRACE_ID_LOCAL.remove();
    }
}
