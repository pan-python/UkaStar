package com.ukastar.api.points.vo;

/**
 * 积分统计响应。
 */
public record PointStatisticsResponse(
        int todayCount,
        int todayNetScore,
        int totalFamilies,
        int totalPoints,
        int weeklyNetScore
) {
}
