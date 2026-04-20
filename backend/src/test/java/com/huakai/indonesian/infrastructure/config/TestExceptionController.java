package com.huakai.indonesian.infrastructure.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仅用于全局异常处理测试的临时控制器
 */
@RestController
public class TestExceptionController {

    @GetMapping("/test/illegal")
    public String illegal() {
        throw new IllegalArgumentException("参数不合法");
    }

    @GetMapping("/test/runtime")
    public String runtime() {
        throw new RuntimeException("不应该暴露给客户端的错误详情");
    }

    @GetMapping("/test/null")
    public String nullPointer() {
        String s = null;
        return s.toUpperCase();
    }
}
