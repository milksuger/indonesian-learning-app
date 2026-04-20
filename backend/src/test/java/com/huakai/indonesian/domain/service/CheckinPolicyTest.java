package com.huakai.indonesian.domain.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CheckinPolicy 领域服务测试
 * 验证每日签到触发条件
 */
class CheckinPolicyTest {

    private final CheckinPolicy policy = new CheckinPolicy();

    // Feature: indonesian-learning-app, Property 74: 完成新词目标且复习队列为空时应触发签到
    @Test
    void canCheckin_whenGoalMetAndNoReviews_returnsTrue() {
        boolean result = policy.canCheckin(20, 20, 0);
        assertThat(result).isTrue();
    }

    // Feature: indonesian-learning-app, Property 75: 新词目标未完成时不应触发签到
    @Test
    void canCheckin_whenGoalNotMet_returnsFalse() {
        boolean result = policy.canCheckin(15, 20, 0);
        assertThat(result).isFalse();
    }

    // Feature: indonesian-learning-app, Property 76: 还有复习单词时不应触发签到
    @Test
    void canCheckin_whenReviewsRemaining_returnsFalse() {
        boolean result = policy.canCheckin(20, 20, 5);
        assertThat(result).isFalse();
    }

    // Feature: indonesian-learning-app, Property 77: 新词目标未完成且还有复习时不应触发签到
    @Test
    void canCheckin_whenBothNotMet_returnsFalse() {
        boolean result = policy.canCheckin(10, 20, 3);
        assertThat(result).isFalse();
    }

    // Feature: indonesian-learning-app, Property 78: 新词学 0 个且目标 0 个、复习 0 个时应触发签到
    @Test
    void canCheckin_whenGoalIsZeroAndNoReviews_returnsTrue() {
        boolean result = policy.canCheckin(0, 0, 0);
        assertThat(result).isTrue();
    }

    // Feature: indonesian-learning-app, Property 79: 超额完成目标且复习为空时应触发签到
    @Test
    void canCheckin_whenExceededGoalAndNoReviews_returnsTrue() {
        boolean result = policy.canCheckin(25, 20, 0);
        assertThat(result).isTrue();
    }
}
