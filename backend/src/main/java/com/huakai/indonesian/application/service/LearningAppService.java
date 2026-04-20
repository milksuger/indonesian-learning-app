package com.huakai.indonesian.application.service;

import com.huakai.indonesian.application.command.LearningFeedbackCommand;
import com.huakai.indonesian.application.dto.FlashCardDto;
import com.huakai.indonesian.application.dto.ProgressSummaryDto;
import com.huakai.indonesian.domain.entity.User;
import com.huakai.indonesian.domain.entity.UserWordProgress;
import com.huakai.indonesian.domain.repository.UserRepository;
import com.huakai.indonesian.domain.repository.UserWordProgressRepository;
import com.huakai.indonesian.domain.repository.UserWordbookRepository;
import com.huakai.indonesian.domain.repository.WordbookRepository;
import com.huakai.indonesian.domain.service.DailyQueueGenerator;
import com.huakai.indonesian.domain.valueobject.SrsInterval;
import com.huakai.indonesian.domain.valueobject.WordContent;
import com.huakai.indonesian.domain.valueobject.WordProgress;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * 学习应用服务
 * 封装每日新词学习队列生成与反馈处理
 */
@Service
public class LearningAppService {

    private final UserRepository userRepository;
    private final UserWordbookRepository userWordbookRepository;
    private final UserWordProgressRepository progressRepository;
    private final WordbookRepository wordbookRepository;
    private final DailyQueueGenerator queueGenerator;
    private final CheckinAppService checkinAppService;

    public LearningAppService(UserRepository userRepository,
                              UserWordbookRepository userWordbookRepository,
                              UserWordProgressRepository progressRepository,
                              WordbookRepository wordbookRepository,
                              DailyQueueGenerator queueGenerator,
                              CheckinAppService checkinAppService) {
        this.userRepository = userRepository;
        this.userWordbookRepository = userWordbookRepository;
        this.progressRepository = progressRepository;
        this.wordbookRepository = wordbookRepository;
        this.queueGenerator = queueGenerator;
        this.checkinAppService = checkinAppService;
    }

    /**
     * 获取用户今日新词学习队列
     *
     * @param userId 用户 ID
     * @return 闪卡内容列表
     */
    public List<FlashCardDto> getLearningQueue(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return Collections.emptyList();
        }

        List<Long> activeWordbookIds = userWordbookRepository.findActiveWordbookIdsByUserId(userId);
        if (activeWordbookIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<WordbookRepository.WordItem> allWordItems = activeWordbookIds.stream()
                .flatMap(wbId -> wordbookRepository.findWordItemsByWordbookId(wbId).stream())
                .distinct()
                .collect(Collectors.toList());

        List<WordProgress> existingProgress = progressRepository.findAllByUserId(userId);
        Map<Long, WordProgress> progressMap = existingProgress.stream()
                .collect(Collectors.toMap(WordProgress::wordId, Function.identity()));

        List<WordProgress> allProgress = allWordItems.stream()
                .map(item -> {
                    WordProgress p = progressMap.get(item.id());
                    if (p != null) {
                        return p;
                    }
                    return new WordProgress(item.id(), "new", false, false, null, null, item.sortOrder());
                })
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        int learnedToday = progressRepository.countLearnedToday(userId, today);
        List<Long> queue = queueGenerator.generateNewQueue(allProgress, today, user.dailyGoal(), learnedToday);

        return queue.stream()
                .map(wordId -> toFlashCard(wordId, progressMap.get(wordId)))
                .collect(Collectors.toList());
    }

    /**
     * 提交学习反馈
     *
     * @param userId  用户 ID
     * @param command 反馈命令
     */
    public void submitFeedback(Long userId, LearningFeedbackCommand command) {
        LocalDate today = LocalDate.now();
        UserWordProgress progress = progressRepository.findByUserIdAndWordId(userId, command.wordId());
        if (progress == null) {
            progress = new UserWordProgress(userId, command.wordId());
        }

        if (command.known()) {
            progress.startLearning(today);
        } else {
            progress.startLearning(today);
            progress.recordUnknown(new SrsInterval(1, 0), today);
        }

        progressRepository.save(progress);
        checkinAppService.tryCheckin(userId);
    }

    /**
     * 获取用户今日学习进度摘要
     *
     * @param userId 用户 ID
     * @return 进度摘要
     */
    public ProgressSummaryDto getProgressSummary(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return new ProgressSummaryDto(20, 0, 0, 0, false);
        }
        LocalDate today = LocalDate.now();
        int learnedToday = progressRepository.countLearnedToday(userId, today);
        int reviewsDue = progressRepository.countReviewsDueToday(userId, today);
        boolean canCheckin = learnedToday >= user.dailyGoal() && reviewsDue == 0;
        return new ProgressSummaryDto(user.dailyGoal(), learnedToday, reviewsDue, user.streak(), canCheckin);
    }

    private FlashCardDto toFlashCard(Long wordId, WordProgress progress) {
        WordContent content = wordbookRepository.findWordById(wordId);
        if (content == null) {
            return new FlashCardDto(wordId, "", "", "", "", "", "", false, false);
        }
        boolean isFavorited = progress != null && progress.isFavorited();
        boolean isMemorized = progress != null && progress.isMemorized();
        return new FlashCardDto(
                wordId,
                content.indonesian(),
                content.chinese(),
                content.english(),
                content.exampleIndonesian(),
                content.exampleChinese(),
                content.exampleEnglish(),
                isFavorited,
                isMemorized
        );
    }

    /**
     * 获取随机探索队列
     * 从用户激活词书中随机抽取未掌握的单词，每次返回固定数量
     * 不计入 SRS，纯浏览探索
     *
     * @param userId 用户 ID
     * @param size   每批数量，默认 20
     * @return 随机闪卡列表
     */
    public List<FlashCardDto> getRandomQueue(Long userId, int size) {
        List<Long> activeWordbookIds = userWordbookRepository.findActiveWordbookIdsByUserId(userId);
        if (activeWordbookIds.isEmpty()) return Collections.emptyList();

        // 获取激活词书中所有单词 ID
        List<Long> allWordIds = activeWordbookIds.stream()
                .flatMap(wbId -> wordbookRepository.findWordItemsByWordbookId(wbId).stream())
                .map(WordbookRepository.WordItem::id)
                .distinct()
                .collect(Collectors.toList());

        // 排除已掌握的单词
        List<Long> memorizedIds = progressRepository.findUnmemorizedWordIdsByUserId(userId);
        // memorizedIds 是未掌握的，但名字容易混淆，这里重新查已掌握的来排除
        List<Long> memorizedSet = progressRepository.findMemorizedByUserId(userId);
        java.util.Set<Long> memorizedSetIds = new java.util.HashSet<>(memorizedSet);

        List<Long> candidates = allWordIds.stream()
                .filter(id -> !memorizedSetIds.contains(id))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) return Collections.emptyList();

        // 随机打乱，取前 size 条
        java.util.Collections.shuffle(candidates);
        List<Long> selected = candidates.subList(0, Math.min(size, candidates.size()));

        // 查进度状态
        List<WordProgress> allProgress = progressRepository.findAllByUserId(userId);
        Map<Long, WordProgress> progressMap = allProgress.stream()
                .collect(Collectors.toMap(WordProgress::wordId, p -> p));

        return selected.stream()
                .map(wordId -> toFlashCard(wordId, progressMap.get(wordId)))
                .collect(Collectors.toList());
    }
}