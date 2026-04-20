package com.huakai.indonesian.domain.entity;

import com.huakai.indonesian.domain.valueobject.Email;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * 用户聚合根实体
 * 负责维护用户基本信息、学习设置与签到连击状态的一致性
 */
public class User {

    /** 支持的语言代码 */
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("zh-CN", "en-US", "id-ID");
    /** 支持的主题模式 */
    private static final Set<String> SUPPORTED_THEMES = Set.of("light", "dark");

    private final Long id;
    private final Email email;
    private String passwordHash;
    private int dailyGoal;
    private String uiLanguage;
    private String themeMode;
    private int streak;
    private LocalDate lastCheckinDate;

    /**
     * 创建新用户，使用默认设置
     *
     * @param id           用户唯一标识
     * @param email        邮箱值对象
     * @param passwordHash BCrypt 哈希后的密码
     */
    public User(Long id, Email email, String passwordHash) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.dailyGoal = 20;
        this.uiLanguage = "zh-CN";
        this.themeMode = "light";
        this.streak = 0;
        this.lastCheckinDate = null;
    }

    public Long id() {
        return id;
    }

    public Email email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public int dailyGoal() {
        return dailyGoal;
    }

    public String uiLanguage() {
        return uiLanguage;
    }

    public String themeMode() {
        return themeMode;
    }

    public int streak() {
        return streak;
    }

    public LocalDate lastCheckinDate() {
        return lastCheckinDate;
    }

    /**
     * 修改每日学习目标
     *
     * @param newGoal 新的目标值，必须为正整数
     */
    public void changeDailyGoal(int newGoal) {
        if (newGoal < 1) {
            throw new IllegalArgumentException("每日目标必须为正整数");
        }
        this.dailyGoal = newGoal;
    }

    /**
     * 切换界面语言
     *
     * @param language 语言代码（zh-CN 或 en-US）
     */
    public void changeUiLanguage(String language) {
        if (!SUPPORTED_LANGUAGES.contains(language)) {
            throw new IllegalArgumentException("不支持的语言: " + language);
        }
        this.uiLanguage = language;
    }

    /**
     * 切换主题模式
     *
     * @param mode 主题模式（light 或 dark）
     */
    public void changeThemeMode(String mode) {
        if (!SUPPORTED_THEMES.contains(mode)) {
            throw new IllegalArgumentException("不支持的主题模式: " + mode);
        }
        this.themeMode = mode;
    }

    /**
     * 记录每日签到
     * 如果昨天已签到则连击加 1，否则重置为 1；同一天重复签到会被忽略
     *
     * @param today 今天的日期
     * @return 实际签到成功返回 true，重复签到返回 false
     */
    public boolean recordCheckin(LocalDate today) {
        if (today.equals(lastCheckinDate)) {
            return false;
        }
        if (lastCheckinDate != null && lastCheckinDate.plusDays(1).equals(today)) {
            streak++;
        } else {
            streak = 1;
        }
        lastCheckinDate = today;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email=" + email + "}";
    }
}
