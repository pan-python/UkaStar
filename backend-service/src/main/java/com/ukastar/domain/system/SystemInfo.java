package com.ukastar.domain.system;

import java.time.Instant;

/**
 * 系统层面的基础信息，用于健康检查与脚手架验证。
 */
public record SystemInfo(String applicationName, String version, Instant buildTime) {
}
