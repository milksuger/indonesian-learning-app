package com.huakai.indonesian.interfaces.rest;

import com.huakai.indonesian.application.command.LearningFeedbackCommand;
import com.huakai.indonesian.application.dto.FlashCardDto;
import com.huakai.indonesian.application.dto.ProgressSummaryDto;
import com.huakai.indonesian.application.service.LearningAppService;
import com.huakai.indonesian.interfaces.security.JwtAuthHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 学习 REST 控制器
 * 暴露每日新词学习队列与反馈接口
 */
@RestController
@RequestMapping("/api/v1/learning")
public class LearningController {

    private final LearningAppService learningAppService;
    private final JwtAuthHelper jwtAuthHelper;

    public LearningController(LearningAppService learningAppService, JwtAuthHelper jwtAuthHelper) {
        this.learningAppService = learningAppService;
        this.jwtAuthHelper = jwtAuthHelper;
    }

    /**
     * 获取今日新词学习队列
     */
    @GetMapping("/queue")
    public ResponseEntity<List<FlashCardDto>> getLearningQueue(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        List<FlashCardDto> queue = learningAppService.getLearningQueue(userId);
        return ResponseEntity.ok(queue);
    }

    /**
     * 提交学习反馈
     */
    @PostMapping("/feedback")
    public ResponseEntity<Void> submitFeedback(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LearningFeedbackCommand command) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        learningAppService.submitFeedback(userId, command);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取今日学习进度摘要
     */
    @GetMapping("/summary")
    public ResponseEntity<ProgressSummaryDto> getProgressSummary(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        ProgressSummaryDto summary = learningAppService.getProgressSummary(userId);
        return ResponseEntity.ok(summary);
    }

    /**
     * 获取随机探索队列（从激活词书随机抽取未掌握单词，不计入 SRS）
     *
     * @param size 每批数量，默认 20
     */
    @GetMapping("/random")
    public ResponseEntity<List<FlashCardDto>> getRandomQueue(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        return ResponseEntity.ok(learningAppService.getRandomQueue(userId, size));
    }
}
