package com.huakai.indonesian.domain.repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 签到记录仓储接口
 */
public interface CheckinRecordRepository {

    /**
     * 保存签到记录
     */
    void save(Long userId, LocalDate checkinDate);

    /**
     * 查询用户在日期范围内的签到记录
     */
    List<LocalDate> findByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end);

    /**
     * 判断用户某天是否已签到
     */
    boolean existsByUserIdAndDate(Long userId, LocalDate date);
}
