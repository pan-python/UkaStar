package com.ukastar.api.system.vo;

import java.time.Instant;

/**
 * 对外暴露的系统信息响应体。
 */
public record SystemInfoResponse(String applicationName, String version, Instant buildTime) {
}
