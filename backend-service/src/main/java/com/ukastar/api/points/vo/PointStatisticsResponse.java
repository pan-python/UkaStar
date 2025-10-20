package com.ukastar.api.points.vo;

/**
 * 积分统计响应。
 */
public record PointStatisticsResponse(
        int todayCount,
        int todayNetScore,
        int totalChildren,
        int totalPoints,
        int weeklyNetScore
) {
}
