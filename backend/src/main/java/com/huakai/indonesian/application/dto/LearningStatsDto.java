package com.huakai.indonesian.application.dto;

import java.util.List;

/**
 * 学习统计数据传输对象
 */
public record LearningStatsDto(
    int totalLearned,
    int totalMemorized,
    int totalMistakes,
    int totalFavorites,
    List<DailyCountDto> dailyCounts,
    List<WordbookProgressDto> wordbookProgress
) {
    /** 每日学习数量 */
    public record DailyCountDto(String date, int count) {}

    /** 词书完成进度 */
    public record WordbookProgressDto(String name, int learned, int total) {}
}
