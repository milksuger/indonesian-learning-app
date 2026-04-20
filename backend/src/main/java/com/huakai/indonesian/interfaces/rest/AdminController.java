package com.huakai.indonesian.interfaces.rest;

import com.huakai.indonesian.application.service.AdminAppService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 管理员 REST 控制器
 * 暴露后台管理接口：批量导入、查看词书、导出 JSON
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminAppService adminAppService;

    public AdminController(AdminAppService adminAppService) {
        this.adminAppService = adminAppService;
    }

    /**
     * 批量导入词书（JSON 文件）
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importWordbook(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "wordbookId", required = false) Long wordbookId) {
        try {
            int count = adminAppService.importWordbook(file, wordbookId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "imported", count,
                    "message", "导入成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 查询所有词书列表
     */
    @GetMapping("/wordbooks")
    public ResponseEntity<List<Map<String, Object>>> listWordbooks() {
        return ResponseEntity.ok(adminAppService.listWordbooks());
    }

    /**
     * 导出指定词书为 JSON（包含单词列表）
     */
    @GetMapping(value = "/wordbooks/{id}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> exportWordbook(@PathVariable("id") Long id) {
        try {
            Map<String, Object> data = adminAppService.exportWordbook(id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 创建词书
     */
    @PostMapping("/wordbooks")
    public ResponseEntity<Map<String, Object>> createWordbook(@RequestBody Map<String, String> body) {
        try {
            var wordbook = adminAppService.createWordbook(
                    body.get("name"), body.get("nameEn"), body.get("level"));
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", wordbook.getId(),
                    "name", wordbook.getName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 更新词书
     */
    @PutMapping("/wordbooks/{id}")
    public ResponseEntity<Map<String, Object>> updateWordbook(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        try {
            var wordbook = adminAppService.updateWordbook(
                    id, body.get("name"), body.get("nameEn"), body.get("level"));
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", wordbook.getId(),
                    "name", wordbook.getName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 删除词书
     */
    @DeleteMapping("/wordbooks/{id}")
    public ResponseEntity<Map<String, Object>> deleteWordbook(@PathVariable("id") Long id) {
        try {
            adminAppService.deleteWordbook(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取词书单词列表（支持分页）
     *
     * @param id         词书 ID
     * @param page       页码（从 0 开始，默认 0）
     * @param size       每页数量（默认 20）
     */
    @GetMapping("/wordbooks/{id}/words")
    public ResponseEntity<Map<String, Object>> listWords(
            @PathVariable("id") Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminAppService.listWords(id, page, size));
    }

    /**
     * 添加单词到词书
     */
    @PostMapping("/wordbooks/{id}/words")
    public ResponseEntity<Map<String, Object>> addWord(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        try {
            var word = adminAppService.addWord(
                    id,
                    body.get("indonesian"),
                    body.get("chinese"),
                    body.get("english"),
                    body.get("exampleIndonesian"),
                    body.get("exampleZh"),
                    body.get("exampleEn"));
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", word.getId(),
                    "indonesian", word.getIndonesian()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 更新单词
     */
    @PutMapping("/words/{id}")
    public ResponseEntity<Map<String, Object>> updateWord(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        try {
            var word = adminAppService.updateWord(
                    id,
                    body.get("indonesian"),
                    body.get("chinese"),
                    body.get("english"),
                    body.get("exampleIndonesian"),
                    body.get("exampleZh"),
                    body.get("exampleEn"));
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", word.getId(),
                    "indonesian", word.getIndonesian()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 删除单词
     */
    @DeleteMapping("/words/{id}")
    public ResponseEntity<Map<String, Object>> deleteWord(@PathVariable("id") Long id) {
        try {
            adminAppService.deleteWord(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取所有用户列表（含学习统计）
     */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> listUsers() {
        return ResponseEntity.ok(adminAppService.listUsers());
    }

    /**
     * 获取系统概览统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(adminAppService.getStats());
    }

    /**
     * 全局单词搜索（跨词书，支持印尼语/中文/英文模糊匹配）
     *
     * @param q          搜索关键词
     * @param wordbookId 可选，限定词书
     * @param page       页码（从 0 开始）
     * @param size       每页数量
     */
    @GetMapping("/words/search")
    public ResponseEntity<Map<String, Object>> searchWords(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Long wordbookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminAppService.searchWords(q, wordbookId, page, size));
    }
}
