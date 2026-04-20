package com.huakai.indonesian.interfaces.rest;

import com.huakai.indonesian.domain.repository.WordbookRepository;
import com.huakai.indonesian.domain.repository.UserWordbookRepository;
import com.huakai.indonesian.interfaces.security.JwtAuthHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * 词书 REST 控制器
 * 暴露词书浏览与激活接口
 */
@RestController
@RequestMapping("/api/v1/wordbooks")
public class WordbookController {

    private final WordbookRepository wordbookRepository;
    private final UserWordbookRepository userWordbookRepository;
    private final JwtAuthHelper jwtAuthHelper;

    public WordbookController(WordbookRepository wordbookRepository,
                              UserWordbookRepository userWordbookRepository,
                              JwtAuthHelper jwtAuthHelper) {
        this.wordbookRepository = wordbookRepository;
        this.userWordbookRepository = userWordbookRepository;
        this.jwtAuthHelper = jwtAuthHelper;
    }

    /**
     * 获取所有词书列表
     */
    @GetMapping
    public ResponseEntity<List<WordbookRepository.WordbookSummary>> getAllWordbooks() {
        List<WordbookRepository.WordbookSummary> wordbooks = wordbookRepository.findAll();
        return ResponseEntity.ok(wordbooks);
    }

    /**
     * 获取当前用户已激活的词书 ID 列表
     */
    @GetMapping("/active")
    public ResponseEntity<List<Long>> getActiveWordbooks(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        List<Long> activeIds = userWordbookRepository.findActiveWordbookIdsByUserId(userId);
        return ResponseEntity.ok(activeIds);
    }

    /**
     * 激活词书
     */
    @PostMapping("/{wordbookId}/activate")
    public ResponseEntity<Void> activateWordbook(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long wordbookId) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        userWordbookRepository.activate(userId, wordbookId);
        return ResponseEntity.ok().build();
    }

    /**
     * 取消激活词书
     */
    @PostMapping("/{wordbookId}/deactivate")
    public ResponseEntity<Void> deactivateWordbook(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long wordbookId) {
        Long userId = jwtAuthHelper.extractUserId(authHeader);
        userWordbookRepository.deactivate(userId, wordbookId);
        return ResponseEntity.ok().build();
    }

    /**
     * 分页获取词书中的单词列表（用于列表浏览模式）
     *
     * @param wordbookId 词书 ID
     * @param page       页码（从 0 开始）
     * @param size       每页数量（默认 20）
     */
    @GetMapping("/{wordbookId}/words")
    public ResponseEntity<List<WordbookRepository.WordContentWithId>> getWordbookWords(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long wordbookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // 验证登录
        jwtAuthHelper.extractUserId(authHeader);
        int offset = page * size;
        List<WordbookRepository.WordContentWithId> words =
            wordbookRepository.findWordsByWordbookIdPaged(wordbookId, offset, size);
        return ResponseEntity.ok(words);
    }
}
