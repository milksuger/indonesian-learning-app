package com.huakai.indonesian.application.dto;

import java.time.LocalDateTime;

/**
 * 今日学习单词数据传输对象
 * 包含单词内容与首次学习时间，用于今日学习记录页
 */
public record TodayWordDto(
    Long wordId,
    String indonesian,
    String chinese,
    String english,
    String exampleIndonesian,
    String exampleChinese,
    String exampleEnglish,
    boolean isFavorited,
    boolean isMemorized
) {
}
