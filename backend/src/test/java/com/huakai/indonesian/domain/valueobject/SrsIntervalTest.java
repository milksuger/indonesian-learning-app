package com.huakai.indonesian.domain.valueobject;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SrsInterval 值对象的属性测试
 * 验证 SRS 间隔重复算法的核心不变量
 */
class SrsIntervalTest {

    // Feature: indonesian-learning-app, Property 1: 已知反馈时间隔翻倍，但不超过最大间隔 30 天
    @Property(tries = 200)
    void onKnown_doublesIntervalButCappedAtMax(@ForAll @IntRange(min = 1, max = 30) int interval) {
        SrsInterval srs = new SrsInterval(interval, 0);
        SrsInterval result = srs.onKnown();

        int expectedInterval = Math.min(interval * 2, 30);
        assertThat(result.interval()).isEqualTo(expectedInterval);
        assertThat(result.repetitions()).isEqualTo(1);
    }

    // Feature: indonesian-learning-app, Property 2: 已知反馈时重复次数始终加 1
    @Property(tries = 200)
    void onKnown_alwaysIncrementsRepetitions(@ForAll @IntRange(min = 1, max = 30) int interval,
                                               @ForAll @IntRange(min = 0, max = 100) int repetitions) {
        SrsInterval srs = new SrsInterval(interval, repetitions);
        SrsInterval result = srs.onKnown();

        assertThat(result.repetitions()).isEqualTo(repetitions + 1);
    }

    // Feature: indonesian-learning-app, Property 3: 未知反馈时间隔始终重置为 1 天
    @Property(tries = 200)
    void onUnknown_alwaysResetsIntervalToOne(@ForAll @IntRange(min = 1, max = 30) int interval,
                                               @ForAll @IntRange(min = 0, max = 100) int repetitions) {
        SrsInterval srs = new SrsInterval(interval, repetitions);
        SrsInterval result = srs.onUnknown();

        assertThat(result.interval()).isEqualTo(1);
        assertThat(result.repetitions()).isEqualTo(0);
    }

    // Feature: indonesian-learning-app, Property 4: 下次复习日等于今天加上间隔天数
    @Property(tries = 200)
    void nextReviewDate_isTodayPlusIntervalDays(@ForAll @IntRange(min = 1, max = 30) int interval) {
        SrsInterval srs = new SrsInterval(interval, 0);
        LocalDate today = LocalDate.of(2026, 4, 19);

        LocalDate result = srs.nextReviewDate(today);

        assertThat(result).isEqualTo(today.plusDays(interval));
    }

    // Feature: indonesian-learning-app, Property 5: 值对象是不可变的，原对象不受影响
    @Property(tries = 200)
    void onKnown_doesNotMutateOriginal(@ForAll @IntRange(min = 1, max = 30) int interval) {
        SrsInterval original = new SrsInterval(interval, 0);
        original.onKnown();

        assertThat(original.interval()).isEqualTo(interval);
        assertThat(original.repetitions()).isEqualTo(0);
    }

    // Feature: indonesian-learning-app, Property 6: 边界值 30 已知反馈后仍为 30
    @Example
    void onKnown_atMaxInterval_staysAtMax() {
        SrsInterval srs = new SrsInterval(30, 5);
        SrsInterval result = srs.onKnown();

        assertThat(result.interval()).isEqualTo(30);
        assertThat(result.repetitions()).isEqualTo(6);
    }

    // Feature: indonesian-learning-app, Property 7: 边界值 1 未知反馈后仍为 1
    @Example
    void onUnknown_atMinInterval_staysAtMin() {
        SrsInterval srs = new SrsInterval(1, 0);
        SrsInterval result = srs.onUnknown();

        assertThat(result.interval()).isEqualTo(1);
        assertThat(result.repetitions()).isEqualTo(0);
    }
}
