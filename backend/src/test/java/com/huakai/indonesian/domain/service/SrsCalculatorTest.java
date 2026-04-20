package com.huakai.indonesian.domain.service;

import com.huakai.indonesian.domain.valueobject.SrsInterval;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SrsCalculator 领域服务测试
 * 验证 SRS 算法的核心计算逻辑
 */
class SrsCalculatorTest {

    private final SrsCalculator calculator = new SrsCalculator();

    // Feature: indonesian-learning-app, Property 67: 新单词首次学习后的首次复习间隔应为 1 天
    @Example
    void calculateFirstReview_forNewWord_returnsIntervalOne() {
        SrsInterval result = calculator.calculateFirstReview();
        assertThat(result.interval()).isEqualTo(1);
        assertThat(result.repetitions()).isEqualTo(0);
    }

    // Feature: indonesian-learning-app, Property 68: 认识反馈后的下次复习日期 = 今天 + 翻倍后间隔（最大 30）
    @Property(tries = 200)
    void calculateNextReview_onKnown_returnsCorrectDate(@ForAll @IntRange(min = 1, max = 30) int interval,
                                                         @ForAll @IntRange(min = 0, max = 100) int repetitions) {
        SrsInterval current = new SrsInterval(interval, repetitions);
        LocalDate today = LocalDate.of(2026, 4, 19);

        LocalDate result = calculator.calculateNextReview(current, true, today);

        int expectedInterval = Math.min(interval * 2, 30);
        assertThat(result).isEqualTo(today.plusDays(expectedInterval));
    }

    // Feature: indonesian-learning-app, Property 69: 不认识反馈后的下次复习日期 = 明天（间隔重置为 1）
    @Property(tries = 200)
    void calculateNextReview_onUnknown_returnsTomorrow(@ForAll @IntRange(min = 1, max = 30) int interval,
                                                        @ForAll @IntRange(min = 0, max = 100) int repetitions) {
        SrsInterval current = new SrsInterval(interval, repetitions);
        LocalDate today = LocalDate.of(2026, 4, 19);

        LocalDate result = calculator.calculateNextReview(current, false, today);

        assertThat(result).isEqualTo(today.plusDays(1));
    }

    // Feature: indonesian-learning-app, Property 70: 已知反馈应返回新的 SrsInterval（翻倍且次数加 1）
    @Example
    void calculateNextReview_onKnown_updatesInterval() {
        SrsInterval current = new SrsInterval(5, 2);
        LocalDate today = LocalDate.of(2026, 4, 19);

        LocalDate nextDate = calculator.calculateNextReview(current, true, today);

        assertThat(nextDate).isEqualTo(LocalDate.of(2026, 4, 29)); // 5 * 2 = 10 天后
    }

    // Feature: indonesian-learning-app, Property 71: 在最大间隔 30 时已知反馈后仍为 30
    @Example
    void calculateNextReview_onKnownAtMax_staysAtMax() {
        SrsInterval current = new SrsInterval(30, 10);
        LocalDate today = LocalDate.of(2026, 4, 19);

        LocalDate nextDate = calculator.calculateNextReview(current, true, today);

        assertThat(nextDate).isEqualTo(LocalDate.of(2026, 5, 19)); // 30 天后
    }

    // Feature: indonesian-learning-app, Property 72: 判断单词是否到复习日期
    @Example
    void isDueForReview_whenTodayOrBefore_returnsTrue() {
        LocalDate today = LocalDate.of(2026, 4, 19);

        assertThat(calculator.isDueForReview(today, today)).isTrue();
        assertThat(calculator.isDueForReview(today.minusDays(1), today)).isTrue();
    }

    // Feature: indonesian-learning-app, Property 73: 判断单词是否未到复习日期
    @Example
    void isDueForReview_whenFuture_returnsFalse() {
        SrsInterval current = new SrsInterval(3, 1);
        LocalDate today = LocalDate.of(2026, 4, 19);
        LocalDate reviewDate = today.plusDays(5);

        assertThat(calculator.isDueForReview(reviewDate, today)).isFalse();
    }
}
