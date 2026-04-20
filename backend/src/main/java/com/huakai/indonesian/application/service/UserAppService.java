package com.huakai.indonesian.application.service;

import com.huakai.indonesian.application.command.UpdateSettingsCommand;
import com.huakai.indonesian.application.dto.CheckinCalendarDto;
import com.huakai.indonesian.application.dto.FlashCardDto;
import com.huakai.indonesian.application.dto.LearningStatsDto;
import com.huakai.indonesian.application.dto.PagedResultDto;
import com.huakai.indonesian.application.dto.TodayWordDto;
import com.huakai.indonesian.application.dto.UserProfileDto;
import com.huakai.indonesian.application.dto.WordSearchResultDto;
import com.huakai.indonesian.domain.entity.User;
import com.huakai.indonesian.domain.entity.UserWordProgress;
import com.huakai.indonesian.domain.repository.CheckinRecordRepository;
import com.huakai.indonesian.domain.repository.UserRepository;
import com.huakai.indonesian.domain.repository.UserWordProgressRepository;
import com.huakai.indonesian.domain.repository.UserWordbookRepository;
import com.huakai.indonesian.domain.repository.WordbookRepository;
import com.huakai.indonesian.domain.valueobject.WordContent;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户应用服务
 * 封装用户资料、设置、收藏、错题本与签到日历查询
 */
@Service
public class UserAppService {

    private final UserRepository userRepository;
    private final UserWordProgressRepository progressRepository;
    private final CheckinRecordRepository checkinRecordRepository;
    private final WordbookRepository wordbookRepository;
    private final UserWordbookRepository userWordbookRepository;

    public UserAppService(UserRepository userRepository,
                          UserWordProgressRepository progressRepository,
                          CheckinRecordRepository checkinRecordRepository,
                          WordbookRepository wordbookRepository,
                          UserWordbookRepository userWordbookRepository) {
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.checkinRecordRepository = checkinRecordRepository;
        this.wordbookRepository = wordbookRepository;
        this.userWordbookRepository = userWordbookRepository;
    }

    /**
     * 获取用户资料
     *
     * @param userId 用户 ID
     * @return 用户资料
     */
    public UserProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return null;
        }
        return new UserProfileDto(
            user.id(),
            user.email().value(),
            user.dailyGoal(),
            user.uiLanguage(),
            user.themeMode(),
            user.streak(),
            user.lastCheckinDate()
        );
    }

    /**
     * 更新用户设置
     *
     * @param userId  用户 ID
     * @param command 设置命令
     */
    public void updateSettings(Long userId, UpdateSettingsCommand command) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (command.dailyGoal() != null) {
            user.changeDailyGoal(command.dailyGoal());
        }
        if (command.uiLanguage() != null) {
            user.changeUiLanguage(command.uiLanguage());
        }
        if (command.themeMode() != null) {
            user.changeThemeMode(command.themeMode());
        }
        userRepository.save(user);
    }

    /**
     * 获取指定月份的签到日历
     *
     * @param userId 用户 ID
     * @param year   年份
     * @param month  月份
     * @return 签到日历
     */
    public CheckinCalendarDto getCheckinCalendar(Long userId, int year, int month) {
        User user = userRepository.findById(userId);
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<LocalDate> dates = checkinRecordRepository.findByUserIdAndDateRange(userId, start, end);
        return new CheckinCalendarDto(year, month, dates, user != null ? user.streak() : 0);
    }

    /**
     * 切换单词收藏状态
     *
     * @param userId 用户 ID
     * @param wordId 单词 ID
     */
    public void toggleFavorite(Long userId, Long wordId) {
        UserWordProgress progress = progressRepository.findByUserIdAndWordId(userId, wordId);
        if (progress == null) {
            progress = new UserWordProgress(userId, wordId);
        }
        progress.toggleFavorite();
        progressRepository.save(progress);
    }

    /**
     * 获取用户收藏的单词列表
     *
     * @param userId 用户 ID
     * @return 闪卡内容列表
     */
    public List<FlashCardDto> getFavorites(Long userId) {
        List<Long> wordIds = progressRepository.findFavoritesByUserId(userId);
        return wordIds.stream()
            .map(this::toFlashCard)
            .collect(Collectors.toList());
    }

    /**
     * 获取用户已掌握的单词列表
     *
     * @param userId 用户 ID
     * @return 闪卡内容列表
     */
    public List<FlashCardDto> getMemorizedWords(Long userId) {
        List<Long> wordIds = progressRepository.findMemorizedByUserId(userId);
        return wordIds.stream()
            .map(this::toFlashCard)
            .collect(Collectors.toList());
    }

    /**
     * 获取用户错题本列表
     *
     * @param userId 用户 ID
     * @return 闪卡内容列表
     */
    public List<FlashCardDto> getMistakes(Long userId) {
        List<Long> wordIds = progressRepository.findMistakesByUserId(userId);
        return wordIds.stream()
            .map(this::toFlashCard)
            .collect(Collectors.toList());
    }

    /**
     * 手动标记单词为已掌握
     *
     * @param userId 用户 ID
     * @param wordId 单词 ID
     */
    public void markMemorized(Long userId, Long wordId) {
        UserWordProgress progress = progressRepository.findByUserIdAndWordId(userId, wordId);
        if (progress == null) {
            progress = new UserWordProgress(userId, wordId);
        }
        progress.markMemorized();
        progressRepository.save(progress);
    }

    /**
     * 分页获取收藏列表
     */
    public List<FlashCardDto> getFavoritesPaged(Long userId, int page, int size) {
        List<Long> wordIds = progressRepository.findFavoritesByUserIdPaged(userId, page * size, size);
        return wordIds.stream().map(this::toFlashCard).collect(Collectors.toList());
    }

    /**
     * 分页获取已掌握列表
     */
    public List<FlashCardDto> getMemorizedWordsPaged(Long userId, int page, int size) {
        List<Long> wordIds = progressRepository.findMemorizedByUserIdPaged(userId, page * size, size);
        return wordIds.stream().map(this::toFlashCard).collect(Collectors.toList());
    }

    /**
     * 分页获取错题本
     */
    public List<FlashCardDto> getMistakesPaged(Long userId, int page, int size) {
        List<Long> wordIds = progressRepository.findMistakesByUserIdPaged(userId, page * size, size);
        return wordIds.stream().map(this::toFlashCard).collect(Collectors.toList());
    }

    /**
     * 获取今日学习的单词列表
     */
    public List<TodayWordDto> getTodayWords(Long userId) {
        LocalDate today = LocalDate.now();
        List<UserWordProgress> progresses = progressRepository.findLearnedTodayByUserId(userId, today);
        return progresses.stream()
            .map(p -> {
                WordContent c = wordbookRepository.findWordById(p.wordId());
                if (c == null) return null;
                return new TodayWordDto(p.wordId(), c.indonesian(), c.chinese(), c.english(),
                    c.exampleIndonesian(), c.exampleChinese(), c.exampleEnglish(),
                    p.isFavorited(), p.isMemorized());
            })
            .filter(d -> d != null)
            .collect(Collectors.toList());
    }

    /**
     * 取消已掌握状态，将单词重新纳入复习队列
     */
    public void unmarkMemorized(Long userId, Long wordId) {
        progressRepository.unmarkMemorized(userId, wordId);
    }

    /**
     * 在用户激活词书内搜索单词
     */
    public PagedResultDto<WordSearchResultDto> searchWords(Long userId, String keyword, int page, int size) {
        List<Long> activeWordbookIds = userWordbookRepository.findActiveWordbookIdsByUserId(userId);
        if (activeWordbookIds.isEmpty()) {
            return new PagedResultDto<>(0, page, size, List.of());
        }
        long total = wordbookRepository.countSearchWords(keyword, activeWordbookIds);
        List<WordbookRepository.WordContentWithId> words =
            wordbookRepository.searchWords(keyword, activeWordbookIds, page * size, size);

        Map<Long, String> wbNames = wordbookRepository.findWordbookNames(activeWordbookIds);

        // 批量查进度状态
        List<Long> wordIds = words.stream().map(WordbookRepository.WordContentWithId::wordId).collect(Collectors.toList());
        Map<Long, UserWordProgress> progressMap = wordIds.stream()
            .map(wid -> progressRepository.findByUserIdAndWordId(userId, wid))
            .filter(p -> p != null)
            .collect(Collectors.toMap(UserWordProgress::wordId, p -> p));

        List<WordSearchResultDto> items = words.stream().map(w -> {
            UserWordProgress p = progressMap.get(w.wordId());
            // 需要从 wordbook 找 wordbookId，通过 wordId 反查
            WordContent content = wordbookRepository.findWordById(w.wordId());
            return new WordSearchResultDto(
                w.wordId(), null, "",
                w.indonesian(), w.chinese(), w.english(),
                w.exampleIndonesian(), w.exampleChinese(), w.exampleEnglish(),
                p != null && p.isFavorited(), p != null && p.isMemorized()
            );
        }).collect(Collectors.toList());

        return new PagedResultDto<>(total, page, size, items);
    }

    /**
     * 获取用户学习统计数据（近 30 天）
     */
    public LearningStatsDto getLearningStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(29);

        int totalLearned = (int) progressRepository.findAllByUserId(userId).stream()
            .filter(p -> p.firstLearnedAt() != null).count();
        int totalMemorized = (int) progressRepository.findMemorizedByUserId(userId).size();
        int totalMistakes = (int) progressRepository.findMistakesByUserId(userId).size();
        int totalFavorites = (int) progressRepository.findFavoritesByUserId(userId).size();

        // 近 30 天每日学习数量
        List<Object[]> rawCounts = progressRepository.findDailyLearnedCounts(userId, from, today);
        Map<LocalDate, Long> countMap = rawCounts.stream()
            .collect(Collectors.toMap(r -> (LocalDate) r[0], r -> (Long) r[1]));
        List<LearningStatsDto.DailyCountDto> dailyCounts = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            dailyCounts.add(new LearningStatsDto.DailyCountDto(d.toString(), countMap.getOrDefault(d, 0L).intValue()));
        }

        // 各词书完成进度
        List<Long> activeIds = userWordbookRepository.findActiveWordbookIdsByUserId(userId);
        List<LearningStatsDto.WordbookProgressDto> wbProgress = activeIds.stream().map(wbId -> {
            List<WordbookRepository.WordItem> items = wordbookRepository.findWordItemsByWordbookId(wbId);
            int total = items.size();
            long learned = items.stream()
                .filter(item -> {
                    UserWordProgress p = progressRepository.findByUserIdAndWordId(userId, item.id());
                    return p != null && p.firstLearnedAt() != null;
                }).count();
            WordbookRepository.WordbookSummary summary = wordbookRepository.findAll().stream()
                .filter(s -> s.id().equals(wbId)).findFirst().orElse(null);
            String name = summary != null ? summary.name() : "词书" + wbId;
            return new LearningStatsDto.WordbookProgressDto(name, (int) learned, total);
        }).collect(Collectors.toList());

        return new LearningStatsDto(totalLearned, totalMemorized, totalMistakes, totalFavorites, dailyCounts, wbProgress);
    }

    private FlashCardDto toFlashCard(Long wordId) {
        WordContent content = wordbookRepository.findWordById(wordId);
        if (content == null) {
            return new FlashCardDto(wordId, "", "", "", "", "", "", false, false);
        }
        return new FlashCardDto(
            wordId,
            content.indonesian(),
            content.chinese(),
            content.english(),
            content.exampleIndonesian(),
            content.exampleChinese(),
            content.exampleEnglish(),
            false,
            false
        );
    }
}
