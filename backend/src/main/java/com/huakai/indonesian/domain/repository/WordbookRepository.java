package com.huakai.indonesian.domain.repository;

import com.huakai.indonesian.domain.valueobject.WordContent;

import java.util.List;
import java.util.Map;

/**
 * 词书仓储接口
 * 定义词书与单词数据的只读查询操作
 */
public interface WordbookRepository {

    /**
     * 获取所有词书列表
     */
    List<WordbookSummary> findAll();

    /**
     * 根据词书 ID 获取单词列表
     */
    List<WordContent> findWordsByWordbookId(Long wordbookId);

    /**
     * 根据单词 ID 获取单词内容
     */
    WordContent findWordById(Long wordId);

    /**
     * 词书摘要信息
     */
    record WordbookSummary(Long id, String name, String nameEn, String level, Integer wordCount) {
    }

    /**
     * 单词条目（ID 与排序号）
     */
    record WordItem(Long id, Integer sortOrder) {
    }

    /**
     * 获取词书中所有单词的 ID 与排序号
     */
    List<WordItem> findWordItemsByWordbookId(Long wordbookId);

    /**
     * 分页获取词书中的单词内容
     *
     * @param wordbookId 词书 ID
     * @param offset     偏移量
     * @param limit      每页数量
     * @return 单词内容列表（含 wordId）
     */
    List<WordContentWithId> findWordsByWordbookIdPaged(Long wordbookId, int offset, int limit);

    /**
     * 带 wordId 的单词内容（用于列表模式）
     */
    record WordContentWithId(Long wordId, String indonesian, String chinese, String english,
                             String exampleIndonesian, String exampleChinese, String exampleEnglish) {
    }

    /**
     * 在指定词书范围内搜索单词（印尼语/中文/英文模糊匹配）
     *
     * @param keyword     搜索关键词
     * @param wordbookIds 限定词书 ID 列表
     * @param offset      偏移量
     * @param limit       数量
     */
    List<WordContentWithId> searchWords(String keyword, List<Long> wordbookIds, int offset, int limit);

    /**
     * 统计搜索结果总数
     */
    long countSearchWords(String keyword, List<Long> wordbookIds);

    /**
     * 根据词书 ID 获取词书名称映射
     */
    Map<Long, String> findWordbookNames(List<Long> wordbookIds);
}
