package com.huakai.indonesian.application.service;

import com.huakai.indonesian.application.command.ReviewFeedbackCommand;
import com.huakai.indonesian.application.dto.FlashCardDto;
import com.huakai.indonesian.domain.entity.UserWordProgress;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 复习应用服务
 * 封装每日复习队列生成与反馈处理
 */
@Service
public class ReviewAppService {

    private final UserWordProgressRepository progressRepository;
    private final UserWordbookRepository userWordbookRepository;
    private final WordbookRepository wordbookRepository;
    private final DailyQueueGenerator queueGenerator;
    private final CheckinAppService checkinAppService;

    public ReviewAppService(UserWordProgressRepository progressRepository,
                            UserWordbookRepository userWordbookRepository,
                            WordbookRepository wordbookRepository,
                            DailyQueueGenerator queueGenerator,
                            CheckinAppService checkinAppService) {
        this.progressRepository = progressRepository;
        this.userWordbookRepository = userWordbookRepository;
        this.wordbookRepository = wordbookRepository;
        this.queueGenerator = queueGenerator;
        this.checkinAppService = checkinAppService;
    }

    /**
     * 获取用户今日复习队列（仅包含激活词书中已到复习时间的单词）
     *
     * @param userId 用户 ID
     * @return 闪卡内容列表
     */
    public List<FlashCardDto> getReviewQueue(Long userId) {
        List<Long> activeWordbookIds = userWordbookRepository.findActiveWordbookIdsByUserId(userId);
        if (activeWordbookIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> activeWordIds = activeWordbookIds.stream()
            .flatMap(wbId -> wordbookRepository.findWordItemsByWordbookId(wbId).stream())
            .map(WordbookRepository.WordItem::id)
            .collect(Collectors.toSet());

        List<WordProgress> allProgress = progressRepository.findAllByUserId(userId);
        List<WordProgress> filteredProgress = allProgress.stream()
            .filter(p -> activeWordIds.contains(p.wordId()))
            .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        List<Long> queue = queueGenerator.generateReviewQueue(filteredProgress, today);

        Map<Long, WordProgress> progressMap = filteredProgress.stream()
            .collect(Collectors.toMap(WordProgress::wordId, java.util.function.Function.identity()));

        return queue.stream()
            .map(wordId -> toFlashCard(wordId, progressMap.get(wordId)))
            .collect(Collectors.toList());
    }

    /**
     * 提交复习反馈
     *
     * @param userId  用户 ID
     * @param command 反馈命令
     */
    public void submitFeedback(Long userId, ReviewFeedbackCommand command) {
        UserWordProgress progress = progressRepository.findByUserIdAndWordId(userId, command.wordId());
        if (progress == null) {
            throw new IllegalArgumentException("单词不在学习进度中，无法复习: wordId=" + command.wordId());
        }

        LocalDate today = LocalDate.now();
        SrsInterval current = new SrsInterval(progress.srsInterval(), progress.srsRepetitions());
        if (command.known()) {
            progress.recordReview(current.onKnown(), today);
        } else {
            progress.recordUnknown(current.onUnknown(), today);
        }

        progressRepository.save(progress);
        checkinAppService.tryCheckin(userId);
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
}
