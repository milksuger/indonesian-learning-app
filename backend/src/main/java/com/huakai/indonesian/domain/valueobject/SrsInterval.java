package com.huakai.indonesian.domain.valueobject;

import java.time.LocalDate;

/**
 * SRS 间隔值对象
 * 封装间隔重复算法的核心参数：复习间隔天数与重复次数
 * 不可变，所有操作返回新实例
 */
public record SrsInterval(int interval, int repetitions) {

    /** 最大复习间隔：30 天 */
    public static final int MAX_INTERVAL = 30;
    /** 最小复习间隔：1 天 */
    public static final int MIN_INTERVAL = 1;

    /**
     * 用户点击"认识"后的状态转换
     * 间隔翻倍，但不超过最大间隔；重复次数加 1
     */
    public SrsInterval onKnown() {
        return new SrsInterval(Math.min(interval * 2, MAX_INTERVAL), repetitions + 1);
    }

    /**
     * 用户点击"不认识"后的状态转换
     * 间隔重置为最小值；重复次数清零
     */
    public SrsInterval onUnknown() {
        return new SrsInterval(MIN_INTERVAL, 0);
    }

    /**
     * 基于今天的日期计算下次复习日期
     */
    public LocalDate nextReviewDate(LocalDate today) {
        return today.plusDays(interval);
    }
}
