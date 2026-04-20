package com.huakai.indonesian.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huakai.indonesian.domain.entity.UserWordProgress;
import com.huakai.indonesian.domain.repository.UserWordProgressRepository;
import com.huakai.indonesian.domain.valueobject.WordProgress;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * 用户单词学习进度仓储实现
 * 基于 MyBatis-Plus 实现学习进度的查询与持久化
 */
@Repository
public class UserWordProgressRepositoryImpl implements UserWordProgressRepository {

    private final UserWordProgressMapper progressMapper;
    private final WordMapper wordMapper;

    public UserWordProgressRepositoryImpl(UserWordProgressMapper progressMapper, WordMapper wordMapper) {
        this.progressMapper = progressMapper;
        this.wordMapper = wordMapper;
    }

    @Override
    public List<WordProgress> findAllByUserId(Long userId) {
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId);
        List<UserWordProgressPo> progressPos = progressMapper.selectList(wrapper);
        if (progressPos.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> wordIds = progressPos.stream()
            .map(UserWordProgressPo::getWordId)
            .distinct()
            .collect(Collectors.toList());
        List<WordPo> wordPos = wordMapper.selectBatchIds(wordIds);
        Map<Long, Integer> sortOrderMap = wordPos.stream()
            .collect(Collectors.toMap(WordPo::getId, WordPo::getSortOrder));

        return progressPos.stream()
            .map(po -> toWordProgress(po, sortOrderMap.getOrDefault(po.getWordId(), 0)))
            .collect(Collectors.toList());
    }

    @Override
    public UserWordProgress findByUserIdAndWordId(Long userId, Long wordId) {
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId)
               .eq(UserWordProgressPo::getWordId, wordId);
        UserWordProgressPo po = progressMapper.selectOne(wrapper);
        return po == null ? null : toEntity(po);
    }

    @Override
    public void save(UserWordProgress progress) {
        UserWordProgressPo po = toPo(progress);
        if (progress.id() == null) {
            LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserWordProgressPo::getUserId, progress.userId())
                   .eq(UserWordProgressPo::getWordId, progress.wordId());
            UserWordProgressPo existing = progressMapper.selectOne(wrapper);
            if (existing != null) {
                po.setId(existing.getId());
                progressMapper.updateById(po);
            } else {
                progressMapper.insert(po);
            }
        } else {
            progressMapper.updateById(po);
        }
    }

    @Override
    public int countLearnedToday(Long userId, LocalDate today) {
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId)
               .eq(UserWordProgressPo::getFirstLearnedAt, today);
        return Math.toIntExact(progressMapper.selectCount(wrapper));
    }

    @Override
    public int countReviewsDueToday(Long userId, LocalDate today) {
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId)
               .le(UserWordProgressPo::getNextReviewDate, today)
               .eq(UserWordProgressPo::getIsMemorized, 0);
        return Math.toIntExact(progressMapper.selectCount(wrapper));
    }

    @Override
    public List<Long> findFavoritesByUserId(Long userId) {
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId)
               .eq(UserWordProgressPo::getIsFavorited, 1)
               .select(UserWordProgressPo::getWordId);
        return progressMapper.selectList(wrapper).stream()
            .map(UserWordProgressPo::getWordId)
            .collect(Collectors.toList());
    }

    @Override
    public List<Long> findMemorizedByUserId(Long userId) {
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId)
               .eq(UserWordProgressPo::getIsMemorized, 1)
               .select(UserWordProgressPo::getWordId);
        return progressMapper.selectList(wrapper).stream()
            .map(UserWordProgressPo::getWordId)
            .collect(Collectors.toList());
    }

    @Override
    public List<Long> findMistakesByUserId(Long userId) {
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId)
               .eq(UserWordProgressPo::getIsMistake, 1)
               .select(UserWordProgressPo::getWordId);
        return progressMapper.selectList(wrapper).stream()
            .map(UserWordProgressPo::getWordId)
            .collect(Collectors.toList());
    }

    @Override
    public List<Long> findFavoritesByUserIdPaged(Long userId, int offset, int limit) {
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId)
               .eq(UserWordProgressPo::getIsFavorited, 1)
               .select(UserWordProgressPo::getWordId)
               .last("LIMIT " + limit + " OFFSET " + offset);
        return progressMapper.selectList(wrapper).stream()
            .map(UserWordProgressPo::getWordId)
            .collect(Collectors.toList());
    }

    @Override
    public List<Long> findMemorizedByUserIdPaged(Long userId, int offset, int limit) {
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId)
               .eq(UserWordProgressPo::getIsMemorized, 1)
               .select(UserWordProgressPo::getWordId)
               .last("LIMIT " + limit + " OFFSET " + offset);
        return progressMapper.selectList(wrapper).stream()
            .map(UserWordProgressPo::getWordId)
            .collect(Collectors.toList());
    }

    @Override
    public List<Long> findMistakesByUserIdPaged(Long userId, int offset, int limit) {
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId)
                .eq(UserWordProgressPo::getIsMistake, 1)
                .select(UserWordProgressPo::getWordId)
                .last("LIMIT " + limit + " OFFSET " + offset);
        return progressMapper.selectList(wrapper).stream()
            .map(UserWordProgressPo::getWordId)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserWordProgress> findLearnedTodayByUserId(Long userId, LocalDate today) {
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId)
               .eq(UserWordProgressPo::getFirstLearnedAt, today)
               .orderByAsc(UserWordProgressPo::getFirstLearnedAt);
        return progressMapper.selectList(wrapper).stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<Long> findUnmemorizedWordIdsByUserId(Long userId) {
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId)
               .eq(UserWordProgressPo::getIsMemorized, 0)
               .select(UserWordProgressPo::getWordId);
        return progressMapper.selectList(wrapper).stream()
            .map(UserWordProgressPo::getWordId)
            .collect(Collectors.toList());
    }

    @Override
    public void unmarkMemorized(Long userId, Long wordId) {
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId)
               .eq(UserWordProgressPo::getWordId, wordId);
        UserWordProgressPo po = progressMapper.selectOne(wrapper);
        if (po != null) {
            po.setIsMemorized(0);
            po.setStatus("reviewing");
            progressMapper.updateById(po);
        }
    }

    @Override
    public List<Object[]> findDailyLearnedCounts(Long userId, LocalDate from, LocalDate to) {
        // 用 MyBatis-Plus 查询，按 first_learned_at 分组统计
        LambdaQueryWrapper<UserWordProgressPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordProgressPo::getUserId, userId)
               .between(UserWordProgressPo::getFirstLearnedAt, from, to)
               .isNotNull(UserWordProgressPo::getFirstLearnedAt);
        List<UserWordProgressPo> pos = progressMapper.selectList(wrapper);
        // 在内存中按日期分组（数据量小，避免复杂 SQL）
        Map<LocalDate, Long> grouped = pos.stream()
            .collect(Collectors.groupingBy(UserWordProgressPo::getFirstLearnedAt, Collectors.counting()));
        return grouped.entrySet().stream()
            .map(e -> new Object[]{e.getKey(), e.getValue()})
            .collect(Collectors.toList());
    }

    private WordProgress toWordProgress(UserWordProgressPo po, int sortOrder) {
        return new WordProgress(
            po.getWordId(),
            po.getStatus(),
            po.getIsFavorited() != null && po.getIsFavorited() == 1,
            po.getIsMemorized() != null && po.getIsMemorized() == 1,
            po.getFirstLearnedAt(),
            po.getNextReviewDate(),
            sortOrder
        );
    }

    private UserWordProgress toEntity(UserWordProgressPo po) {
        return new UserWordProgress(
            po.getId(),
            po.getUserId(),
            po.getWordId(),
            po.getStatus(),
            po.getIsFavorited() != null && po.getIsFavorited() == 1,
            po.getIsMemorized() != null && po.getIsMemorized() == 1,
            po.getIsMistake() != null && po.getIsMistake() == 1,
            po.getSrsInterval() != null ? po.getSrsInterval() : 1,
            po.getSrsRepetitions() != null ? po.getSrsRepetitions() : 0,
            po.getNextReviewDate(),
            po.getFirstLearnedAt(),
            po.getLastReviewedAt()
        );
    }

    private UserWordProgressPo toPo(UserWordProgress progress) {
        UserWordProgressPo po = new UserWordProgressPo();
        po.setId(progress.id());
        po.setUserId(progress.userId());
        po.setWordId(progress.wordId());
        po.setStatus(progress.status());
        po.setIsFavorited(progress.isFavorited() ? 1 : 0);
        po.setIsMemorized(progress.isMemorized() ? 1 : 0);
        po.setIsMistake(progress.isMistake() ? 1 : 0);
        po.setSrsInterval(progress.srsInterval());
        po.setSrsRepetitions(progress.srsRepetitions());
        po.setNextReviewDate(progress.nextReviewDate());
        po.setFirstLearnedAt(progress.firstLearnedAt());
        po.setLastReviewedAt(progress.lastReviewedAt());
        return po;
    }
}
