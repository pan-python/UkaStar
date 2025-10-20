package com.ukastar.domain.points;

/**
 * 积分统计汇总。
 */
public record PointStatistics(
        int todayCount,
        int todayNetScore,
        int totalFamilies,
        int totalPoints,
        int weeklyNetScore
) {
}
