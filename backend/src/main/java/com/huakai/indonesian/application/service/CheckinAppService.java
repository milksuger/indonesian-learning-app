package com.huakai.indonesian.application.service;

import com.huakai.indonesian.domain.entity.User;
import com.huakai.indonesian.domain.repository.CheckinRecordRepository;
import com.huakai.indonesian.domain.repository.UserRepository;
import com.huakai.indonesian.domain.repository.UserWordProgressRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 签到应用服务
 * 封装每日签到判定与记录逻辑
 */
@Service
public class CheckinAppService {

    private final UserRepository userRepository;
    private final UserWordProgressRepository progressRepository;
    private final CheckinRecordRepository checkinRecordRepository;

    public CheckinAppService(UserRepository userRepository,
                             UserWordProgressRepository progressRepository,
                             CheckinRecordRepository checkinRecordRepository) {
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.checkinRecordRepository = checkinRecordRepository;
    }

    /**
     * 尝试为用户执行今日签到
     * 当满足条件（今日已学数量 >= 每日目标 且 无待复习单词）且今日未签到时执行
     *
     * @param userId 用户 ID
     * @return 实际签到成功返回 true，否则返回 false
     */
    public boolean tryCheckin(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (user.lastCheckinDate() != null && user.lastCheckinDate().equals(today)) {
            return false;
        }
        int learnedToday = progressRepository.countLearnedToday(userId, today);
        int reviewsDue = progressRepository.countReviewsDueToday(userId, today);
        if (learnedToday >= user.dailyGoal() && reviewsDue == 0) {
            user.recordCheckin(today);
            userRepository.save(user);
            checkinRecordRepository.save(userId, today);
            return true;
        }
        return false;
    }
}
