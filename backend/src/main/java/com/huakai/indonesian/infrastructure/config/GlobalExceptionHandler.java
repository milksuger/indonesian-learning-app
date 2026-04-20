package com.huakai.indonesian.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * 全局异常处理器
 * 捕获所有控制器抛出的异常，转换为统一的 JSON 错误响应格式
 * 不暴露内部错误细节给客户端，但记录详细堆栈到服务端日志
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务参数不合法异常（400 Bad Request）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> error = Map.of(
            "code", "BAD_REQUEST",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 处理所有未预期的异常（500 Internal Server Error）
     * 返回通用错误消息，不暴露内部堆栈信息，但记录详细日志便于排查
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("未捕获的服务器异常", ex);
        Map<String, Object> error = Map.of(
            "code", "INTERNAL_ERROR",
            "message", "服务器内部错误",
            "timestamp", Instant.now().toString()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
