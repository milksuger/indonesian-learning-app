package com.huakai.indonesian.domain.service;

/**
 * 签到判定领域服务
 * 封装每日签到触发的业务规则
 */
public class CheckinPolicy {

    /**
     * 判断用户是否满足签到条件
     * 条件：今日学习的新词数量 >= 每日目标，且今日待复习单词数量为 0
     *
     * @param learnedToday    今日已学新词数量
     * @param dailyGoal       每日学习目标
     * @param reviewsDueToday 今日待复习单词数量
     * @return 满足签到条件返回 true
     */
    public boolean canCheckin(int learnedToday, int dailyGoal, int reviewsDueToday) {
        return learnedToday >= dailyGoal && reviewsDueToday == 0;
    }
}
