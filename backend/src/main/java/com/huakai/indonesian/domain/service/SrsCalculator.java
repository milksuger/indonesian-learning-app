package com.huakai.indonesian.domain.service;

import com.huakai.indonesian.domain.valueobject.SrsInterval;

import java.time.LocalDate;

/**
 * SRS 计算器领域服务
 * 封装间隔重复算法的计算逻辑，协调 SrsInterval 值对象的状态转换
 */
public class SrsCalculator {

    /**
     * 计算新单词的首次复习参数
     * 新单词首次学习后，默认 1 天后复习
     */
    public SrsInterval calculateFirstReview() {
        return new SrsInterval(1, 0);
    }

    /**
     * 根据用户反馈计算下次复习日期
     *
     * @param current  当前的 SRS 参数
     * @param isKnown  是否认识（true = 认识，false = 不认识）
     * @param today    今天的日期
     * @return 下次复习日期
     */
    public LocalDate calculateNextReview(SrsInterval current, boolean isKnown, LocalDate today) {
        SrsInterval next = isKnown ? current.onKnown() : current.onUnknown();
        return next.nextReviewDate(today);
    }

    /**
     * 判断单词在指定日期是否已到复习时间
     *
     * @param nextReviewDate 下次复习日期
     * @param today          今天的日期
     * @return 已到或已过复习日期返回 true
     */
    public boolean isDueForReview(LocalDate nextReviewDate, LocalDate today) {
        return !nextReviewDate.isAfter(today);
    }
}
