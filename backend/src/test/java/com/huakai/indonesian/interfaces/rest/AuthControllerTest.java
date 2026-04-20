package com.huakai.indonesian.interfaces.rest;

import com.huakai.indonesian.application.command.LoginCommand;
import com.huakai.indonesian.application.command.RegisterCommand;
import com.huakai.indonesian.application.service.AuthAppService;
import com.huakai.indonesian.domain.entity.User;
import com.huakai.indonesian.domain.valueobject.Email;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController 接口层单元测试
 * 使用 @WebMvcTest 隔离 Spring MVC 层，Mock 应用服务依赖
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthAppService authAppService;

    // Feature: indonesian-learning-app, Property 45: 登录接口应返回 JWT 令牌
    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        when(authAppService.login(any(LoginCommand.class))).thenReturn("jwt-token-123");

        LoginCommand command = new LoginCommand("user@example.com", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"));
    }

    // Feature: indonesian-learning-app, Property 46: 注册接口应返回创建的用户信息
    @Test
    void register_withNewEmail_returnsUser() throws Exception {
        User user = new User(1L, new Email("new@example.com"), "encodedPassword");
        when(authAppService.register(any(RegisterCommand.class))).thenReturn(user);

        RegisterCommand command = new RegisterCommand("new@example.com", "password123", "123456");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }
}
