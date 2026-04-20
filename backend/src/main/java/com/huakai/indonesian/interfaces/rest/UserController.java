package com.huakai.indonesian.interfaces.rest;

import com.huakai.indonesian.application.command.UpdateSettingsCommand;
import com.huakai.indonesian.application.dto.CheckinCalendarDto;
import com.huakai.indonesian.application.dto.FlashCardDto;
import com.huakai.indonesian.application.dto.LearningStatsDto;
import com.huakai.indonesian.application.dto.PagedResultDto;
import com.huakai.indonesian.application.dto.TodayWordDto;
import com.huakai.indonesian.application.dto.UserProfileDto;
import com.huakai.indonesian.application.dto.WordSearchResultDto;
import com.huakai.indonesian.application.service.UserAppService;
import com.huakai.indonesian.interfaces.security.JwtAuthHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户 REST 控制器
 * 暴露用户资料、设置、收藏、错题本与签到日历接口
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserAppService userAppService;
    private final JwtAuthHelper jwtAuthHelper;

    public UserController(UserAppService userAppService, JwtAuthHelper jwtAuthHelper) {
        this.userAppService = userAppService;
        this.jwtAuthHelper = jwtAuthHelper;
    }

    /**
     * 获取当前登录用户资料
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getProfile(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        UserProfileDto profile = userAppService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * 更新用户设置
     */
    @PutMapping("/me/settings")
    public ResponseEntity<Void> updateSettings(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateSettingsCommand command) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        userAppService.updateSettings(userId, command);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取签到日历
     */
    @GetMapping("/me/checkin-calendar")
    public ResponseEntity<CheckinCalendarDto> getCheckinCalendar(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        CheckinCalendarDto calendar = userAppService.getCheckinCalendar(userId, year, month);
        return ResponseEntity.ok(calendar);
    }

    /**
     * 切换单词收藏状态
     */
    @PostMapping("/me/favorites/{wordId}")
    public ResponseEntity<Void> toggleFavorite(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long wordId) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        userAppService.toggleFavorite(userId, wordId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("/me/favorites")
    public ResponseEntity<List<FlashCardDto>> getFavorites(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        List<FlashCardDto> favorites = userAppService.getFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    /**
     * 获取已掌握单词列表
     */
    @GetMapping("/me/memorized")
    public ResponseEntity<List<FlashCardDto>> getMemorizedWords(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        List<FlashCardDto> words = userAppService.getMemorizedWords(userId);
        return ResponseEntity.ok(words);
    }

    /**
     * 获取错题本列表
     */
    @GetMapping("/me/mistakes")
    public ResponseEntity<List<FlashCardDto>> getMistakes(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        List<FlashCardDto> mistakes = userAppService.getMistakes(userId);
        return ResponseEntity.ok(mistakes);
    }

    /**
     * 手动标记单词为已掌握
     */
    @PostMapping("/me/memorized/{wordId}")
    public ResponseEntity<Void> markMemorized(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long wordId) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        userAppService.markMemorized(userId, wordId);
        return ResponseEntity.ok().build();
    }

    /**
     * 分页获取收藏列表（用于列表模式）
     */
    @GetMapping("/me/favorites/paged")
    public ResponseEntity<List<FlashCardDto>> getFavoritesPaged(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        List<FlashCardDto> favorites = userAppService.getFavoritesPaged(userId, page, size);
        return ResponseEntity.ok(favorites);
    }

    /**
     * 分页获取已掌握列表（用于列表模式）
     */
    @GetMapping("/me/memorized/paged")
    public ResponseEntity<List<FlashCardDto>> getMemorizedPaged(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        List<FlashCardDto> words = userAppService.getMemorizedWordsPaged(userId, page, size);
        return ResponseEntity.ok(words);
    }

    /**
     * 分页获取错题本（用于列表模式）
     */
    @GetMapping("/me/mistakes/paged")
    public ResponseEntity<List<FlashCardDto>> getMistakesPaged(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        List<FlashCardDto> mistakes = userAppService.getMistakesPaged(userId, page, size);
        return ResponseEntity.ok(mistakes);
    }

    /**
     * 获取今日学习的单词列表
     */
    @GetMapping("/me/today-words")
    public ResponseEntity<List<TodayWordDto>> getTodayWords(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        return ResponseEntity.ok(userAppService.getTodayWords(userId));
    }

    /**
     * 取消已掌握状态，将单词重新纳入复习队列
     */
    @DeleteMapping("/me/memorized/{wordId}")
    public ResponseEntity<Void> unmarkMemorized(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long wordId) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        userAppService.unmarkMemorized(userId, wordId);
        return ResponseEntity.ok().build();
    }

    /**
     * 在激活词书内搜索单词
     */
    @GetMapping("/me/words/search")
    public ResponseEntity<PagedResultDto<WordSearchResultDto>> searchWords(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        return ResponseEntity.ok(userAppService.searchWords(userId, q, page, size));
    }

    /**
     * 获取学习统计数据（近 30 天）
     */
    @GetMapping("/me/stats")
    public ResponseEntity<LearningStatsDto> getLearningStats(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        return ResponseEntity.ok(userAppService.getLearningStats(userId));
    }
}
