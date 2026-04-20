package com.huakai.indonesian.interfaces.rest;

import com.huakai.indonesian.application.command.LoginCommand;
import com.huakai.indonesian.application.command.RegisterCommand;
import com.huakai.indonesian.application.service.AuthAppService;
import com.huakai.indonesian.domain.entity.User;
import com.huakai.indonesian.interfaces.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证 REST 控制器
 * 暴露用户注册与登录的 HTTP 接口
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthAppService authAppService;

    public AuthController(AuthAppService authAppService) {
        this.authAppService = authAppService;
    }

    /**
     * 用户登录
     *
     * @param command 登录请求参数
     * @return JWT 令牌
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginCommand command) {
        String token = authAppService.login(command);
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * 用户注册
     *
     * @param command 注册请求参数
     * @return 创建的用户信息
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterCommand command) {
        User user = authAppService.register(command);
        UserResponse response = new UserResponse(
            user.id(),
            user.email().value(),
            user.dailyGoal(),
            user.uiLanguage(),
            user.themeMode(),
            user.streak()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 发送注册验证码
     *
     * @param request 包含邮箱的请求
     * @return 空响应（204）
     */
    @PostMapping("/register/send-code")
    public ResponseEntity<Void> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authAppService.sendRegistrationCode(email);
        return ResponseEntity.noContent().build();
    }
}
