package com.huakai.indonesian.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 全局异常处理单元测试
 * 验证异常转换为统一 JSON 响应格式
 */
@WebMvcTest(controllers = TestExceptionController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // Feature: indonesian-learning-app, Property 57: IllegalArgumentException 应返回 400 与统一错误格式
    @Test
    void illegalArgumentException_returnsBadRequestWithUnifiedError() throws Exception {
        mockMvc.perform(get("/test/illegal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("参数不合法"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // Feature: indonesian-learning-app, Property 58: 未预期的异常应返回 500 与统一错误格式
    @Test
    void runtimeException_returnsInternalServerError() throws Exception {
        mockMvc.perform(get("/test/runtime"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("服务器内部错误"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // Feature: indonesian-learning-app, Property 59: 空指针异常应返回 500 且不暴露细节
    @Test
    void nullPointerException_returnsInternalServerError() throws Exception {
        mockMvc.perform(get("/test/null"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }
}
