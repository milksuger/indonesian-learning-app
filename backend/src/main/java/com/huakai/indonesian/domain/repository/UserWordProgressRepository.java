package com.huakai.indonesian.domain.repository;

import com.huakai.indonesian.domain.entity.UserWordProgress;
import com.huakai.indonesian.domain.valueobject.WordProgress;

import java.time.LocalDate;
import java.util.List;
/**
 * 用户单词学习进度仓储接口
 */
public interface UserWordProgressRepository {

    /**
     * 获取用户激活词书中所有单词的进度快照
     */
    List<WordProgress> findAllByUserId(Long userId);

    /**
     * 获取指定单词的完整进度实体
     */
    UserWordProgress findByUserIdAndWordId(Long userId, Long wordId);

    /**
     * 创建或更新学习进度
     */
    void save(UserWordProgress progress);

    /**
     * 获取今日已学新词数量
     */
    int countLearnedToday(Long userId, LocalDate today);

    /**
     * 获取今日待复习单词数量
     */
    int countReviewsDueToday(Long userId, LocalDate today);

    /**
     * 获取用户收藏的所有单词 ID
     */
    List<Long> findFavoritesByUserId(Long userId);

    /**
     * 获取用户已掌握的所有单词 ID
     */
    List<Long> findMemorizedByUserId(Long userId);

    /**
     * 获取用户错题本中的所有单词 ID
     */
    List<Long> findMistakesByUserId(Long userId);

    /**
     * 分页获取用户收藏的单词 ID
     */
    List<Long> findFavoritesByUserIdPaged(Long userId, int offset, int limit);

    /**
     * 分页获取用户已掌握的单词 ID
     */
    List<Long> findMemorizedByUserIdPaged(Long userId, int offset, int limit);

    /**
     * 分页获取用户错题本中的单词 ID
     */
    List<Long> findMistakesByUserIdPaged(Long userId, int offset, int limit);

    /**
     * 获取用户今日首次学习的单词进度列表（按学习时间排序）
     */
    List<UserWordProgress> findLearnedTodayByUserId(Long userId, LocalDate today);

    /**
     * 获取用户在激活词书中未掌握的所有单词 ID（用于随机模式）
     */
    List<Long> findUnmemorizedWordIdsByUserId(Long userId);

    /**
     * 取消已掌握状态
     */
    void unmarkMemorized(Long userId, Long wordId);

    /**
     * 获取用户近 N 天每日学习数量
     */
    List<Object[]> findDailyLearnedCounts(Long userId, LocalDate from, LocalDate to);
}
