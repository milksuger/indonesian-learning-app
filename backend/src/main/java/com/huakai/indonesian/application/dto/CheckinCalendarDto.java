package com.huakai.indonesian.application.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 签到日历数据传输对象
 */
public record CheckinCalendarDto(
    int year,
    int month,
    List<LocalDate> checkinDates,
    int streak
) {
}
