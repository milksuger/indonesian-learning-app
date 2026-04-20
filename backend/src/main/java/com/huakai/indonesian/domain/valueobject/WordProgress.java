package com.huakai.indonesian.domain.valueobject;

import java.time.LocalDate;

/**
 * 单词学习进度快照
 * 用于队列生成时的只读查询数据，避免加载完整聚合根
 */
public record WordProgress(
    Long wordId,
    String status,
    boolean isFavorited,
    boolean isMemorized,
    LocalDate firstLearnedAt,
    LocalDate nextReviewDate,
    int sortOrder
) {
}
