package com.huakai.indonesian.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huakai.indonesian.domain.repository.WordbookRepository;
import com.huakai.indonesian.domain.valueobject.WordContent;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 词书仓储实现
 * 基于 MyBatis-Plus 实现词书与单词的只读查询
 */
@Repository
public class WordbookRepositoryImpl implements WordbookRepository {

    private final WordbookMapper wordbookMapper;
    private final WordMapper wordMapper;

    public WordbookRepositoryImpl(WordbookMapper wordbookMapper, WordMapper wordMapper) {
        this.wordbookMapper = wordbookMapper;
        this.wordMapper = wordMapper;
    }

    @Override
    public List<WordbookSummary> findAll() {
        List<WordbookPo> pos = wordbookMapper.selectList(null);
        return pos.stream()
            .map(this::toSummary)
            .collect(Collectors.toList());
    }

    @Override
    public List<WordContent> findWordsByWordbookId(Long wordbookId) {
        LambdaQueryWrapper<WordPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WordPo::getWordbookId, wordbookId)
               .orderByAsc(WordPo::getSortOrder);
        List<WordPo> pos = wordMapper.selectList(wrapper);
        return pos.stream()
            .map(this::toWordContent)
            .collect(Collectors.toList());
    }

    @Override
    public WordContent findWordById(Long wordId) {
        WordPo po = wordMapper.selectById(wordId);
        return po == null ? null : toWordContent(po);
    }

    @Override
    public List<WordItem> findWordItemsByWordbookId(Long wordbookId) {
        LambdaQueryWrapper<WordPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WordPo::getWordbookId, wordbookId)
               .orderByAsc(WordPo::getSortOrder)
               .select(WordPo::getId, WordPo::getSortOrder);
        List<WordPo> pos = wordMapper.selectList(wrapper);
        return pos.stream()
            .map(po -> new WordItem(po.getId(), po.getSortOrder()))
            .collect(Collectors.toList());
    }

    @Override
    public List<WordContentWithId> findWordsByWordbookIdPaged(Long wordbookId, int offset, int limit) {
        LambdaQueryWrapper<WordPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WordPo::getWordbookId, wordbookId)
               .orderByAsc(WordPo::getSortOrder)
               .last("LIMIT " + limit + " OFFSET " + offset);
        List<WordPo> pos = wordMapper.selectList(wrapper);
        return pos.stream()
            .map(po -> new WordContentWithId(
                po.getId(),
                po.getIndonesian(),
                po.getChinese(),
                po.getEnglish(),
                po.getExampleIndonesian(),
                po.getExampleZh(),
                po.getExampleEn()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public List<WordContentWithId> searchWords(String keyword, List<Long> wordbookIds, int offset, int limit) {
        LambdaQueryWrapper<WordPo> wrapper = new LambdaQueryWrapper<>();
        if (wordbookIds != null && !wordbookIds.isEmpty()) {
            wrapper.in(WordPo::getWordbookId, wordbookIds);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(WordPo::getIndonesian, keyword)
                              .or().like(WordPo::getChinese, keyword)
                              .or().like(WordPo::getEnglish, keyword));
        }
        wrapper.orderByAsc(WordPo::getWordbookId).orderByAsc(WordPo::getSortOrder)
               .last("LIMIT " + limit + " OFFSET " + offset);
        return wordMapper.selectList(wrapper).stream()
            .map(po -> new WordContentWithId(po.getId(), po.getIndonesian(), po.getChinese(),
                po.getEnglish(), po.getExampleIndonesian(), po.getExampleZh(), po.getExampleEn()))
            .collect(Collectors.toList());
    }

    @Override
    public long countSearchWords(String keyword, List<Long> wordbookIds) {
        LambdaQueryWrapper<WordPo> wrapper = new LambdaQueryWrapper<>();
        if (wordbookIds != null && !wordbookIds.isEmpty()) {
            wrapper.in(WordPo::getWordbookId, wordbookIds);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(WordPo::getIndonesian, keyword)
                              .or().like(WordPo::getChinese, keyword)
                              .or().like(WordPo::getEnglish, keyword));
        }
        return wordMapper.selectCount(wrapper);
    }

    @Override
    public Map<Long, String> findWordbookNames(List<Long> wordbookIds) {
        if (wordbookIds == null || wordbookIds.isEmpty()) return Map.of();
        return wordbookMapper.selectBatchIds(wordbookIds).stream()
            .collect(Collectors.toMap(WordbookPo::getId, WordbookPo::getName));
    }

    private WordbookSummary toSummary(WordbookPo po) {
        return new WordbookSummary(
            po.getId(),
            po.getName(),
            po.getNameEn(),
            po.getLevel(),
            po.getWordCount()
        );
    }

    private WordContent toWordContent(WordPo po) {
        return new WordContent(
            po.getIndonesian(),
            po.getChinese(),
            po.getEnglish(),
            po.getExampleIndonesian(),
            po.getExampleZh(),
            po.getExampleEn()
        );
    }
}
