package com.huakai.indonesian.domain.entity;

import com.huakai.indonesian.domain.valueobject.Email;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * User 聚合根实体的属性测试
 * 验证用户创建、设置修改与签到连击逻辑
 */
class UserTest {

    // Feature: indonesian-learning-app, Property 21: 创建用户时默认值应正确
    @Example
    void newUser_hasCorrectDefaults() {
        Email email = new Email("user@example.com");
        User user = new User(1L, email, "hashedPassword123");

        assertThat(user.id()).isEqualTo(1L);
        assertThat(user.email()).isEqualTo(email);
        assertThat(user.passwordHash()).isEqualTo("hashedPassword123");
        assertThat(user.dailyGoal()).isEqualTo(20);
        assertThat(user.uiLanguage()).isEqualTo("zh-CN");
        assertThat(user.themeMode()).isEqualTo("light");
        assertThat(user.streak()).isEqualTo(0);
        assertThat(user.lastCheckinDate()).isNull();
    }

    // Feature: indonesian-learning-app, Property 22: 修改每日目标应生效
    @Property(tries = 100)
    void changeDailyGoal_updatesValue(@ForAll @IntRange(min = 1, max = 500) int newGoal) {
        User user = createDefaultUser();
        user.changeDailyGoal(newGoal);
        assertThat(user.dailyGoal()).isEqualTo(newGoal);
    }

    // Feature: indonesian-learning-app, Property 23: 每日目标不能小于 1
    @Property(tries = 50)
    void changeDailyGoal_toZeroOrNegative_throwsException(@ForAll @IntRange(min = -100, max = 0) int invalidGoal) {
        User user = createDefaultUser();
        assertThatThrownBy(() -> user.changeDailyGoal(invalidGoal))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("每日目标必须为正整数");
    }

    // Feature: indonesian-learning-app, Property 24: 切换 UI 语言应生效
    @Example
    void changeUiLanguage_updatesValue() {
        User user = createDefaultUser();
        user.changeUiLanguage("en-US");
        assertThat(user.uiLanguage()).isEqualTo("en-US");
    }

    // Feature: indonesian-learning-app, Property 25: 无效语言代码应抛出异常
    @Example
    void changeUiLanguage_toInvalid_throwsException() {
        User user = createDefaultUser();
        assertThatThrownBy(() -> user.changeUiLanguage("fr-FR"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("不支持的语言");
    }

    // Feature: indonesian-learning-app, Property 26: 切换主题模式应生效
    @Example
    void changeThemeMode_updatesValue() {
        User user = createDefaultUser();
        user.changeThemeMode("dark");
        assertThat(user.themeMode()).isEqualTo("dark");
    }

    // Feature: indonesian-learning-app, Property 27: 无效主题模式应抛出异常
    @Example
    void changeThemeMode_toInvalid_throwsException() {
        User user = createDefaultUser();
        assertThatThrownBy(() -> user.changeThemeMode("blue"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("不支持的主题模式");
    }

    // Feature: indonesian-learning-app, Property 28: 首次签到连击应为 1
    @Example
    void firstCheckin_setsStreakToOne() {
        User user = createDefaultUser();
        LocalDate today = LocalDate.of(2026, 4, 19);

        boolean checkedIn = user.recordCheckin(today);

        assertThat(checkedIn).isTrue();
        assertThat(user.streak()).isEqualTo(1);
        assertThat(user.lastCheckinDate()).isEqualTo(today);
    }

    // Feature: indonesian-learning-app, Property 29: 连续签到应增加连击数
    @Example
    void consecutiveCheckin_incrementsStreak() {
        User user = createDefaultUser();
        LocalDate yesterday = LocalDate.of(2026, 4, 18);
        LocalDate today = LocalDate.of(2026, 4, 19);

        user.recordCheckin(yesterday);
        boolean checkedIn = user.recordCheckin(today);

        assertThat(checkedIn).isTrue();
        assertThat(user.streak()).isEqualTo(2);
    }

    // Feature: indonesian-learning-app, Property 30: 断签后签到应重置连击为 1
    @Example
    void brokenStreak_resetsToOne() {
        User user = createDefaultUser();
        LocalDate twoDaysAgo = LocalDate.of(2026, 4, 17);
        LocalDate today = LocalDate.of(2026, 4, 19);

        user.recordCheckin(twoDaysAgo);
        boolean checkedIn = user.recordCheckin(today);

        assertThat(checkedIn).isTrue();
        assertThat(user.streak()).isEqualTo(1);
    }

    // Feature: indonesian-learning-app, Property 31: 同一天重复签到应被忽略
    @Example
    void duplicateCheckin_sameDay_isIgnored() {
        User user = createDefaultUser();
        LocalDate today = LocalDate.of(2026, 4, 19);

        user.recordCheckin(today);
        boolean second = user.recordCheckin(today);

        assertThat(second).isFalse();
        assertThat(user.streak()).isEqualTo(1);
    }

    private User createDefaultUser() {
        return new User(1L, new Email("user@example.com"), "hashedPassword123");
    }
}
