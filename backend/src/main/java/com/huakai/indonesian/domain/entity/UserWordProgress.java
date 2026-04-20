package com.huakai.indonesian.domain.entity;

import com.huakai.indonesian.domain.valueobject.SrsInterval;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 用户单词学习进度实体
 * 封装用户对单个单词的学习状态、SRS 参数与交互标记
 */
public class UserWordProgress {

    private Long id;
    private final Long userId;
    private final Long wordId;
    private String status;
    private boolean favorited;
    private boolean memorized;
    private boolean mistake;
    private int srsInterval;
    private int srsRepetitions;
    private LocalDate nextReviewDate;
    private LocalDate firstLearnedAt;
    private LocalDate lastReviewedAt;

    /**
     * 创建全新学习进度（用户首次遇到该单词）
     */
    public UserWordProgress(Long userId, Long wordId) {
        this.userId = userId;
        this.wordId = wordId;
        this.status = "new";
        this.srsInterval = SrsInterval.MIN_INTERVAL;
        this.srsRepetitions = 0;
    }

    /**
     * 从持久化层重建完整实体
     */
    public UserWordProgress(Long id, Long userId, Long wordId, String status,
                            boolean favorited, boolean memorized, boolean mistake,
                            int srsInterval, int srsRepetitions,
                            LocalDate nextReviewDate, LocalDate firstLearnedAt,
                            LocalDate lastReviewedAt) {
        this.id = id;
        this.userId = userId;
        this.wordId = wordId;
        this.status = status;
        this.favorited = favorited;
        this.memorized = memorized;
        this.mistake = mistake;
        this.srsInterval = srsInterval;
        this.srsRepetitions = srsRepetitions;
        this.nextReviewDate = nextReviewDate;
        this.firstLearnedAt = firstLearnedAt;
        this.lastReviewedAt = lastReviewedAt;
    }

    public Long id() {
        return id;
    }

    public Long userId() {
        return userId;
    }

    public Long wordId() {
        return wordId;
    }

    public String status() {
        return status;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public boolean isMemorized() {
        return memorized;
    }

    public boolean isMistake() {
        return mistake;
    }

    public int srsInterval() {
        return srsInterval;
    }

    public int srsRepetitions() {
        return srsRepetitions;
    }

    public LocalDate nextReviewDate() {
        return nextReviewDate;
    }

    public LocalDate firstLearnedAt() {
        return firstLearnedAt;
    }

    public LocalDate lastReviewedAt() {
        return lastReviewedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 首次学习该单词，状态变为 learning
     *
     * @param today 学习当天的日期
     */
    public void startLearning(LocalDate today) {
        this.status = "learning";
        this.firstLearnedAt = today;
        this.lastReviewedAt = today;
        this.srsInterval = 1;
        this.srsRepetitions = 1;
        this.nextReviewDate = today;
    }

    /**
     * 记录一次复习反馈，更新 SRS 参数与状态
     * 若是当天新学的单词，保持 nextReviewDate 为当天，支持无限次当日复习
     *
     * @param newInterval 计算后的新 SRS 间隔
     * @param today       复习当天的日期
     */
    public void recordReview(SrsInterval newInterval, LocalDate today) {
        this.srsInterval = newInterval.interval();
        this.srsRepetitions = newInterval.repetitions();
        if (isFirstLearnedToday(today)) {
            this.nextReviewDate = today;
        } else {
            this.nextReviewDate = newInterval.nextReviewDate(today);
        }
        this.lastReviewedAt = today;
        if (this.srsInterval >= SrsInterval.MAX_INTERVAL) {
            this.status = "memorized";
            this.memorized = true;
        } else {
            this.status = "reviewing";
        }
    }

    /**
     * 记录"不认识"反馈，重置间隔并标记错题
     * 若是当天新学的单词，保持 nextReviewDate 为当天，支持无限次当日复习
     *
     * @param newInterval 重置后的 SRS 间隔
     * @param today       复习当天的日期
     */
    public void recordUnknown(SrsInterval newInterval, LocalDate today) {
        this.srsInterval = newInterval.interval();
        this.srsRepetitions = newInterval.repetitions();
        if (isFirstLearnedToday(today)) {
            this.nextReviewDate = today;
        } else {
            this.nextReviewDate = newInterval.nextReviewDate(today);
        }
        this.lastReviewedAt = today;
        this.status = "learning";
        this.mistake = true;
    }

    /**
     * 判断该单词是否为当天首次学习
     */
    private boolean isFirstLearnedToday(LocalDate today) {
        return this.firstLearnedAt != null && this.firstLearnedAt.equals(today);
    }

    /**
     * 切换收藏状态
     */
    public void toggleFavorite() {
        this.favorited = !this.favorited;
    }

    /**
     * 手动标记为已掌握
     */
    public void markMemorized() {
        this.memorized = true;
        this.status = "memorized";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserWordProgress)) return false;
        UserWordProgress other = (UserWordProgress) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserWordProgress{userId=" + userId + ", wordId=" + wordId + ", status=" + status + "}";
    }
}
