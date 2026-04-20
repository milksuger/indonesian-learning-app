package com.huakai.indonesian.application.dto;

/**
 * 学习进度摘要数据传输对象
 * 用于首页或学习页面顶部展示今日学习概况
 */
public record ProgressSummaryDto(
    int dailyGoal,
    int learnedToday,
    int reviewsDueToday,
    int streak,
    boolean canCheckin
) {
}
