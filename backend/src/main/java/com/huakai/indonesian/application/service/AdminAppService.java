package com.huakai.indonesian.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huakai.indonesian.infrastructure.persistence.UserMapper;
import com.huakai.indonesian.infrastructure.persistence.UserPo;
import com.huakai.indonesian.infrastructure.persistence.UserWordProgressMapper;
import com.huakai.indonesian.infrastructure.persistence.UserWordProgressPo;
import com.huakai.indonesian.infrastructure.persistence.WordMapper;
import com.huakai.indonesian.infrastructure.persistence.WordPo;
import com.huakai.indonesian.infrastructure.persistence.WordbookMapper;
import com.huakai.indonesian.infrastructure.persistence.WordbookPo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员应用服务
 * 封装 JSON 批量导入、查看、导出词书与单词的后台操作
 */
@Service
public class AdminAppService {

    private final WordbookMapper wordbookMapper;
    private final WordMapper wordMapper;
    private final UserMapper userMapper;
    private final UserWordProgressMapper progressMapper;
    private final ObjectMapper objectMapper;

    public AdminAppService(WordbookMapper wordbookMapper, WordMapper wordMapper,
                           UserMapper userMapper, UserWordProgressMapper progressMapper) {
        this.wordbookMapper = wordbookMapper;
        this.wordMapper = wordMapper;
        this.userMapper = userMapper;
        this.progressMapper = progressMapper;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 从 JSON 文件批量导入词书与单词
     *
     * @param file JSON 文件，格式包含 name, nameEn, level, words 数组
     */
    @Transactional
    public int importWordbook(MultipartFile file, Long wordbookId) {
        try {
            JsonNode root = objectMapper.readTree(file.getInputStream());

            JsonNode wordsNode = root.get("words");
            if (wordsNode == null || !wordsNode.isArray()) {
                throw new IllegalArgumentException("JSON 中缺少 words 数组");
            }

            WordbookPo wordbook;
            if (wordbookId == null) {
                wordbook = new WordbookPo();
                wordbook.setName(root.get("name").asText());
                wordbook.setNameEn(root.has("nameEn") ? root.get("nameEn").asText() : null);
                wordbook.setLevel(root.get("level").asText());
                wordbook.setWordCount(0);
                wordbook.setSortOrder(0);
                wordbook.setCreatedAt(LocalDateTime.now());
                wordbookMapper.insert(wordbook);
            } else {
                wordbook = wordbookMapper.selectById(wordbookId);
                if (wordbook == null) {
                    throw new IllegalArgumentException("词书不存在：id=" + wordbookId);
                }
            }

            int sortOrder = 1;
            if (wordbookId != null) {
                List<WordPo> existing = wordMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WordPo>()
                                .eq(WordPo::getWordbookId, wordbookId)
                                .orderByDesc(WordPo::getSortOrder)
                                .last("LIMIT 1")
                );
                if (!existing.isEmpty()) {
                    sortOrder = existing.get(0).getSortOrder() + 1;
                }
            }

            for (JsonNode wordNode : wordsNode) {
                WordPo word = new WordPo();
                word.setWordbookId(wordbook.getId());
                word.setIndonesian(wordNode.get("indonesian").asText());
                word.setChinese(wordNode.get("chinese").asText());
                word.setEnglish(wordNode.has("english") ? wordNode.get("english").asText() : null);
                word.setExampleIndonesian(wordNode.has("exampleIndonesian") ? wordNode.get("exampleIndonesian").asText() : null);
                word.setExampleZh(wordNode.has("exampleZh") ? wordNode.get("exampleZh").asText() : null);
                word.setExampleEn(wordNode.has("exampleEn") ? wordNode.get("exampleEn").asText() : null);
                word.setSortOrder(sortOrder++);
                word.setCreatedAt(LocalDateTime.now());
                wordMapper.insert(word);
            }

            wordbook.setWordCount(wordbook.getWordCount() + wordsNode.size());
            wordbookMapper.updateById(wordbook);

            return wordsNode.size();
        } catch (Exception e) {
            throw new IllegalArgumentException("导入失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有词书列表
     *
     * @return 词书简要信息列表
     */
    public List<Map<String, Object>> listWordbooks() {
        List<WordbookPo> wordbooks = wordbookMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        for (WordbookPo wb : wordbooks) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", wb.getId());
            map.put("name", wb.getName());
            map.put("nameEn", wb.getNameEn());
            map.put("level", wb.getLevel());
            map.put("wordCount", wb.getWordCount());
            result.add(map);
        }
        return result;
    }

    /**
     * 导出指定词书为 JSON（包含单词列表）
     *
     * @param wordbookId 词书 ID
     * @return 符合导入格式的 JSON 数据
     */
    public Map<String, Object> exportWordbook(Long wordbookId) {
        WordbookPo wordbook = wordbookMapper.selectById(wordbookId);
        if (wordbook == null) {
            throw new IllegalArgumentException("词书不存在：id=" + wordbookId);
        }

        List<WordPo> words = wordMapper.selectList(
                new LambdaQueryWrapper<WordPo>()
                        .eq(WordPo::getWordbookId, wordbookId)
                        .orderByAsc(WordPo::getSortOrder)
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", wordbook.getName());
        result.put("nameEn", wordbook.getNameEn());
        result.put("level", wordbook.getLevel());

        List<Map<String, Object>> wordList = new ArrayList<>();
        for (WordPo w : words) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("indonesian", w.getIndonesian());
            map.put("chinese", w.getChinese());
            map.put("english", w.getEnglish());
            map.put("exampleIndonesian", w.getExampleIndonesian());
            map.put("exampleZh", w.getExampleZh());
            map.put("exampleEn", w.getExampleEn());
            wordList.add(map);
        }
        result.put("words", wordList);
        return result;
    }

    /**
     * 创建新词书
     */
    public WordbookPo createWordbook(String name, String nameEn, String level) {
        WordbookPo wordbook = new WordbookPo();
        wordbook.setName(name);
        wordbook.setNameEn(nameEn);
        wordbook.setLevel(level);
        wordbook.setWordCount(0);
        wordbook.setSortOrder(0);
        wordbook.setCreatedAt(LocalDateTime.now());
        wordbookMapper.insert(wordbook);
        return wordbook;
    }

    /**
     * 更新词书信息
     */
    public WordbookPo updateWordbook(Long wordbookId, String name, String nameEn, String level) {
        WordbookPo wordbook = wordbookMapper.selectById(wordbookId);
        if (wordbook == null) {
            throw new IllegalArgumentException("词书不存在：id=" + wordbookId);
        }
        wordbook.setName(name);
        wordbook.setNameEn(nameEn);
        wordbook.setLevel(level);
        wordbookMapper.updateById(wordbook);
        return wordbook;
    }

    /**
     * 删除词书（级联删除单词）
     */
    @Transactional
    public void deleteWordbook(Long wordbookId) {
        WordbookPo wordbook = wordbookMapper.selectById(wordbookId);
        if (wordbook == null) {
            throw new IllegalArgumentException("词书不存在：id=" + wordbookId);
        }
        wordMapper.delete(
                new LambdaQueryWrapper<WordPo>()
                        .eq(WordPo::getWordbookId, wordbookId)
        );
        wordbookMapper.deleteById(wordbookId);
    }

    /**
     * 获取词书的单词列表（支持分页）
     *
     * @param wordbookId 词书 ID
     * @param page       页码（从 0 开始，可选）
     * @param size       每页数量（可选，不提供则返回全部）
     * @return 包含 words 列表和 total 总数的映射
     */
    public Map<String, Object> listWords(Long wordbookId, Integer page, Integer size) {
        LambdaQueryWrapper<WordPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WordPo::getWordbookId, wordbookId)
                .orderByAsc(WordPo::getSortOrder);

        // 如果提供了分页参数，则执行分页查询
        if (page != null && size != null) {
            long total = wordMapper.selectCount(wrapper);
            wrapper.last("LIMIT " + size + " OFFSET " + (page * size));
            List<WordPo> words = wordMapper.selectList(wrapper);

            List<Map<String, Object>> result = new ArrayList<>();
            for (WordPo w : words) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", w.getId());
                map.put("indonesian", w.getIndonesian());
                map.put("chinese", w.getChinese());
                map.put("english", w.getEnglish());
                map.put("exampleIndonesian", w.getExampleIndonesian());
                map.put("exampleZh", w.getExampleZh());
                map.put("exampleEn", w.getExampleEn());
                map.put("sortOrder", w.getSortOrder());
                result.add(map);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("words", result);
            response.put("total", total);
            return response;
        } else {
            // 向后兼容：如果没有提供分页参数，返回全部结果
            List<WordPo> words = wordMapper.selectList(wrapper);
            List<Map<String, Object>> result = new ArrayList<>();
            for (WordPo w : words) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", w.getId());
                map.put("indonesian", w.getIndonesian());
                map.put("chinese", w.getChinese());
                map.put("english", w.getEnglish());
                map.put("exampleIndonesian", w.getExampleIndonesian());
                map.put("exampleZh", w.getExampleZh());
                map.put("exampleEn", w.getExampleEn());
                map.put("sortOrder", w.getSortOrder());
                result.add(map);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("words", result);
            response.put("total", result.size());
            return response;
        }
    }

    /**
     * 添加单词到词书
     */
    public WordPo addWord(Long wordbookId, String indonesian, String chinese,
                          String english, String exampleIndonesian,
                          String exampleZh, String exampleEn) {
        WordbookPo wordbook = wordbookMapper.selectById(wordbookId);
        if (wordbook == null) {
            throw new IllegalArgumentException("词书不存在：id=" + wordbookId);
        }

        List<WordPo> existing = wordMapper.selectList(
                new LambdaQueryWrapper<WordPo>()
                        .eq(WordPo::getWordbookId, wordbookId)
                        .orderByDesc(WordPo::getSortOrder)
                        .last("LIMIT 1")
        );
        int sortOrder = existing.isEmpty() ? 1 : existing.get(0).getSortOrder() + 1;

        WordPo word = new WordPo();
        word.setWordbookId(wordbookId);
        word.setIndonesian(indonesian);
        word.setChinese(chinese);
        word.setEnglish(english);
        word.setExampleIndonesian(exampleIndonesian);
        word.setExampleZh(exampleZh);
        word.setExampleEn(exampleEn);
        word.setSortOrder(sortOrder);
        word.setCreatedAt(LocalDateTime.now());
        wordMapper.insert(word);

        wordbook.setWordCount(wordbook.getWordCount() + 1);
        wordbookMapper.updateById(wordbook);

        return word;
    }

    /**
     * 更新单词
     */
    public WordPo updateWord(Long wordId, String indonesian, String chinese,
                             String english, String exampleIndonesian,
                             String exampleZh, String exampleEn) {
        WordPo word = wordMapper.selectById(wordId);
        if (word == null) {
            throw new IllegalArgumentException("单词不存在：id=" + wordId);
        }
        word.setIndonesian(indonesian);
        word.setChinese(chinese);
        word.setEnglish(english);
        word.setExampleIndonesian(exampleIndonesian);
        word.setExampleZh(exampleZh);
        word.setExampleEn(exampleEn);
        wordMapper.updateById(word);
        return word;
    }

    /**
     * 删除单词
     */
    @Transactional
    public void deleteWord(Long wordId) {
        WordPo word = wordMapper.selectById(wordId);
        if (word == null) {
            throw new IllegalArgumentException("单词不存在：id=" + wordId);
        }
        wordMapper.deleteById(wordId);

        WordbookPo wordbook = wordbookMapper.selectById(word.getWordbookId());
        if (wordbook != null) {
            wordbook.setWordCount(Math.max(0, wordbook.getWordCount() - 1));
            wordbookMapper.updateById(wordbook);
        }
    }

    /**
     * 获取所有用户列表（含学习统计）
     */
    public List<Map<String, Object>> listUsers() {
        List<UserPo> users = userMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserPo u : users) {
            // 统计该用户的学习进度数量
            long learnedCount = progressMapper.selectCount(
                new LambdaQueryWrapper<UserWordProgressPo>()
                    .eq(UserWordProgressPo::getUserId, u.getId())
                    .isNotNull(UserWordProgressPo::getFirstLearnedAt)
            );
            long memorizedCount = progressMapper.selectCount(
                new LambdaQueryWrapper<UserWordProgressPo>()
                    .eq(UserWordProgressPo::getUserId, u.getId())
                    .eq(UserWordProgressPo::getIsMemorized, 1)
            );
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("email", u.getEmail());
            map.put("dailyGoal", u.getDailyGoal());
            map.put("uiLanguage", u.getUiLanguage());
            map.put("streak", u.getStreak());
            map.put("lastCheckinDate", u.getLastCheckinDate());
            map.put("learnedCount", learnedCount);
            map.put("memorizedCount", memorizedCount);
            result.add(map);
        }
        return result;
    }

    /**
     * 获取系统概览统计数据
     */
    public Map<String, Object> getStats() {
        long totalUsers = userMapper.selectCount(null);
        long totalWordbooks = wordbookMapper.selectCount(null);
        long totalWords = wordMapper.selectCount(null);
        long totalProgress = progressMapper.selectCount(
            new LambdaQueryWrapper<UserWordProgressPo>()
                .isNotNull(UserWordProgressPo::getFirstLearnedAt)
        );

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalWordbooks", totalWordbooks);
        stats.put("totalWords", totalWords);
        stats.put("totalLearned", totalProgress);
        return stats;
    }

    /**
     * 全局单词搜索
     * 支持印尼语、中文、英文模糊匹配，可按词书过滤，分页返回
     *
     * @param keyword    搜索关键词（为空时返回全部）
     * @param wordbookId 可选词书过滤
     * @param page       页码（从 0 开始）
     * @param size       每页数量
     * @return 包含 total、words 的结果 Map
     */
    public Map<String, Object> searchWords(String keyword, Long wordbookId, int page, int size) {
        // 构建查询条件
        LambdaQueryWrapper<WordPo> wrapper = new LambdaQueryWrapper<>();

        if (wordbookId != null) {
            wrapper.eq(WordPo::getWordbookId, wordbookId);
        }

        String kw = keyword == null ? "" : keyword.trim();
        if (!kw.isEmpty()) {
            wrapper.and(w -> w
                .like(WordPo::getIndonesian, kw)
                .or().like(WordPo::getChinese, kw)
                .or().like(WordPo::getEnglish, kw)
            );
        }

        wrapper.orderByAsc(WordPo::getWordbookId).orderByAsc(WordPo::getSortOrder);

        // 查总数
        long total = wordMapper.selectCount(wrapper);

        // 分页查询
        wrapper.last("LIMIT " + size + " OFFSET " + (page * size));
        List<WordPo> words = wordMapper.selectList(wrapper);

        // 批量获取词书名称
        List<Long> wbIds = words.stream()
            .map(WordPo::getWordbookId)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
        Map<Long, String> wbNameMap = new java.util.HashMap<>();
        if (!wbIds.isEmpty()) {
            wordbookMapper.selectBatchIds(wbIds).forEach(wb -> wbNameMap.put(wb.getId(), wb.getName()));
        }

        // 组装结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (WordPo w : words) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", w.getId());
            map.put("wordbookId", w.getWordbookId());
            map.put("wordbookName", wbNameMap.getOrDefault(w.getWordbookId(), ""));
            map.put("indonesian", w.getIndonesian());
            map.put("chinese", w.getChinese());
            map.put("english", w.getEnglish());
            map.put("exampleIndonesian", w.getExampleIndonesian());
            map.put("exampleZh", w.getExampleZh());
            map.put("exampleEn", w.getExampleEn());
            map.put("sortOrder", w.getSortOrder());
            result.add(map);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("total", total);
        response.put("page", page);
        response.put("size", size);
        response.put("words", result);
        return response;
    }
}
