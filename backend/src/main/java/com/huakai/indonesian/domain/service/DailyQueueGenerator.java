package com.huakai.indonesian.domain.service;

import com.huakai.indonesian.domain.valueobject.WordProgress;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 每日学习队列生成器领域服务
 * 负责生成用户每日的新词学习队列和复习队列
 */
public class DailyQueueGenerator {

    /**
     * 生成每日新词学习队列
     * 规则：
     * 1. 排除已掌握（memorized）的单词
     * 2. 排除今日已学习的单词（firstLearnedAt == today）
     * 3. 按 sort_order 升序排列
     * 4. 数量上限 = dailyGoal - learnedToday
     *
     * @param allWords     用户激活词书中的所有单词进度
     * @param today        今天的日期
     * @param dailyGoal    每日学习目标
     * @param learnedToday 今日已学新词数量
     * @return 新词队列（单词 ID 列表）
     */
    public List<Long> generateNewQueue(List<WordProgress> allWords, LocalDate today, int dailyGoal, int learnedToday) {
        int remaining = dailyGoal - learnedToday;
        if (remaining <= 0) {
            return List.of();
        }

        return allWords.stream()
                .filter(w -> !w.isMemorized())
                .filter(w -> w.firstLearnedAt() == null || !w.firstLearnedAt().equals(today))
                .sorted(Comparator.comparingInt(WordProgress::sortOrder))
                .limit(remaining)
                .map(WordProgress::wordId)
                .collect(Collectors.toList());
    }

    /**
     * 生成每日复习队列
     * 规则：
     * 1. 只包含状态为 reviewing 且未掌握的单词
     * 2. next_review_date <= today（已到复习日期）
     * 3. 按 next_review_date 升序排列（先到期的先复习）
     *
     * @param allWords 用户激活词书中的所有单词进度
     * @param today    今天的日期
     * @return 复习队列（单词 ID 列表）
     */
    public List<Long> generateReviewQueue(List<WordProgress> allWords, LocalDate today) {
        return allWords.stream()
                .filter(w -> !w.isMemorized())
                .filter(w -> w.nextReviewDate() != null && !w.nextReviewDate().isAfter(today))
                .sorted(Comparator.comparing(WordProgress::nextReviewDate))
                .map(WordProgress::wordId)
                .collect(Collectors.toList());
    }
}
