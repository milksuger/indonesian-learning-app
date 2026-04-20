package com.huakai.indonesian.interfaces.rest;

import com.huakai.indonesian.application.command.ReviewFeedbackCommand;
import com.huakai.indonesian.application.dto.FlashCardDto;
import com.huakai.indonesian.application.service.ReviewAppService;
import com.huakai.indonesian.interfaces.security.JwtAuthHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 复习 REST 控制器
 * 暴露每日复习队列与反馈接口
 */
@RestController
@RequestMapping("/api/v1/review")
public class ReviewController {

    private final ReviewAppService reviewAppService;
    private final JwtAuthHelper jwtAuthHelper;

    public ReviewController(ReviewAppService reviewAppService, JwtAuthHelper jwtAuthHelper) {
        this.reviewAppService = reviewAppService;
        this.jwtAuthHelper = jwtAuthHelper;
    }

    /**
     * 获取今日复习队列
     */
    @GetMapping("/queue")
    public ResponseEntity<List<FlashCardDto>> getReviewQueue(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        List<FlashCardDto> queue = reviewAppService.getReviewQueue(userId);
        return ResponseEntity.ok(queue);
    }

    /**
     * 提交复习反馈
     */
    @PostMapping("/feedback")
    public ResponseEntity<Void> submitFeedback(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ReviewFeedbackCommand command) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        reviewAppService.submitFeedback(userId, command);
        return ResponseEntity.ok().build();
    }
}
