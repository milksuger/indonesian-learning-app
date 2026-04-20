package com.huakai.indonesian.domain.service;

import com.huakai.indonesian.domain.valueobject.WordProgress;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DailyQueueGenerator 领域服务测试
 * 验证每日新词学习队列与复习队列的生成逻辑
 */
class DailyQueueGeneratorTest {

    private final DailyQueueGenerator generator = new DailyQueueGenerator();
    private final LocalDate today = LocalDate.of(2026, 4, 19);

    // Feature: indonesian-learning-app, Property 80: 未学习且未掌握的单词应进入新词队列
    @Test
    void generateNewQueue_includesOnlyUnlearnedAndUnmemorizedWords() {
        List<WordProgress> allWords = List.of(
            new WordProgress(1L, "new", false, false, null, null, 1),
            new WordProgress(2L, "learning", false, false, today, today.plusDays(1), 2),
            new WordProgress(3L, "memorized", false, true, today.minusDays(3), null, 3),
            new WordProgress(4L, "new", false, false, null, null, 4)
        );

        List<Long> result = generator.generateNewQueue(allWords, today, 20, 0);

        assertThat(result).containsExactly(1L, 4L);
    }

    // Feature: indonesian-learning-app, Property 81: 今日已学的单词不应再次进入新词队列
    @Test
    void generateNewQueue_excludesWordsLearnedToday() {
        List<WordProgress> allWords = List.of(
            new WordProgress(1L, "new", false, false, null, null, 1),
            new WordProgress(2L, "learning", false, false, today, today.plusDays(1), 2)
        );

        List<Long> result = generator.generateNewQueue(allWords, today, 20, 0);

        assertThat(result).containsExactly(1L);
    }

    // Feature: indonesian-learning-app, Property 82: 新词队列数量 = 每日目标 - 今日已学数量
    @Test
    void generateNewQueue_respectsRemainingGoal() {
        List<WordProgress> allWords = createWords(10);

        List<Long> result = generator.generateNewQueue(allWords, today, 20, 15);

        assertThat(result).hasSize(5);
    }

    // Feature: indonesian-learning-app, Property 83: 新词队列按 sort_order 排序
    @Test
    void generateNewQueue_ordersBySortOrder() {
        List<WordProgress> allWords = List.of(
            new WordProgress(1L, "new", false, false, null, null, 5),
            new WordProgress(2L, "new", false, false, null, null, 2),
            new WordProgress(3L, "new", false, false, null, null, 10)
        );

        List<Long> result = generator.generateNewQueue(allWords, today, 20, 0);

        assertThat(result).containsExactly(2L, 1L, 3L);
    }

    // Feature: indonesian-learning-app, Property 84: 每日目标已满时新词队列为空
    @Test
    void generateNewQueue_whenGoalReached_returnsEmpty() {
        List<WordProgress> allWords = createWords(10);

        List<Long> result = generator.generateNewQueue(allWords, today, 20, 20);

        assertThat(result).isEmpty();
    }

    // Feature: indonesian-learning-app, Property 85: 复习队列只包含 next_review_date <= today 且未掌握的单词
    @Test
    void generateReviewQueue_includesOnlyDueAndUnmemorizedWords() {
        List<WordProgress> allWords = List.of(
            new WordProgress(1L, "reviewing", false, false, today.minusDays(5), today.minusDays(1), 1),
            new WordProgress(2L, "reviewing", false, false, today.minusDays(5), today.plusDays(1), 2),
            new WordProgress(3L, "memorized", false, true, today.minusDays(5), today.minusDays(1), 3),
            new WordProgress(4L, "reviewing", false, false, today.minusDays(5), today, 4)
        );

        List<Long> result = generator.generateReviewQueue(allWords, today);

        assertThat(result).containsExactly(1L, 4L);
    }

    // Feature: indonesian-learning-app, Property 86: 复习队列按 next_review_date 升序排列
    @Test
    void generateReviewQueue_ordersByNextReviewDate() {
        List<WordProgress> allWords = List.of(
            new WordProgress(1L, "reviewing", false, false, today.minusDays(5), today.minusDays(1), 1),
            new WordProgress(2L, "reviewing", false, false, today.minusDays(5), today.minusDays(3), 2),
            new WordProgress(3L, "reviewing", false, false, today.minusDays(5), today, 3)
        );

        List<Long> result = generator.generateReviewQueue(allWords, today);

        assertThat(result).containsExactly(2L, 1L, 3L);
    }

    // Feature: indonesian-learning-app, Property 87: 无到期待复习单词时返回空队列
    @Test
    void generateReviewQueue_whenNoDueWords_returnsEmpty() {
        List<WordProgress> allWords = List.of(
            new WordProgress(1L, "reviewing", false, false, today.minusDays(5), today.plusDays(1), 1)
        );

        List<Long> result = generator.generateReviewQueue(allWords, today);

        assertThat(result).isEmpty();
    }

    private List<WordProgress> createWords(int count) {
        List<WordProgress> words = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            words.add(new WordProgress((long) i, "new", false, false, null, null, i));
        }
        return words;
    }
}
