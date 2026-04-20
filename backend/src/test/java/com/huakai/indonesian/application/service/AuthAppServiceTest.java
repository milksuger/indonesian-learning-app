package com.huakai.indonesian.application.service;

import com.huakai.indonesian.application.command.LoginCommand;
import com.huakai.indonesian.application.command.RegisterCommand;
import com.huakai.indonesian.domain.entity.User;
import com.huakai.indonesian.domain.repository.UserRepository;
import com.huakai.indonesian.domain.service.PasswordEncoder;
import com.huakai.indonesian.domain.service.TokenProvider;
import com.huakai.indonesian.domain.valueobject.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthAppService 应用服务单元测试
 * 使用 Mockito 隔离仓储与外部服务依赖
 */
class AuthAppServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private TokenProvider tokenProvider;
    private EmailVerificationService emailVerificationService;
    private AuthAppService authAppService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        tokenProvider = mock(TokenProvider.class);
        emailVerificationService = mock(EmailVerificationService.class);
        authAppService = new AuthAppService(userRepository, passwordEncoder, tokenProvider, emailVerificationService);
    }

    // Feature: indonesian-learning-app, Property 32: 正确邮箱密码登录应返回 JWT
    @Test
    void login_withValidCredentials_returnsToken() {
        Email email = new Email("user@example.com");
        User user = new User(1L, email, "encodedPassword");
        when(userRepository.findByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);
        when(tokenProvider.generateToken(1L, "user@example.com", "user")).thenReturn("jwt-token-123");

        String token = authAppService.login(new LoginCommand("user@example.com", "rawPassword"));

        assertThat(token).isEqualTo("jwt-token-123");
    }

    // Feature: indonesian-learning-app, Property 33: 不存在的邮箱登录应抛出异常（统一错误消息防枚举）
    @Test
    void login_withNonexistentEmail_throwsException() {
        Email email = new Email("nobody@example.com");
        when(userRepository.findByEmail(email)).thenReturn(null);

        assertThatThrownBy(() -> authAppService.login(new LoginCommand("nobody@example.com", "anyPassword")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("邮箱或密码错误");
    }

    // Feature: indonesian-learning-app, Property 34: 密码错误登录应抛出异常（统一错误消息防枚举）
    @Test
    void login_withWrongPassword_throwsException() {
        Email email = new Email("user@example.com");
        User user = new User(1L, email, "encodedPassword");
        when(userRepository.findByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authAppService.login(new LoginCommand("user@example.com", "wrongPassword")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("邮箱或密码错误");
    }

    // Feature: indonesian-learning-app, Property 35: 新邮箱注册应创建用户并返回实体
    @Test
    void register_withValidCode_createsUser() {
        Email email = new Email("new@example.com");
        when(emailVerificationService.verifyCode(email, "123456")).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(null);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new User(99L, u.email(), u.passwordHash());
        });

        User result = authAppService.register(new RegisterCommand("new@example.com", "rawPassword", "123456"));

        assertThat(result.id()).isEqualTo(99L);
        assertThat(result.email().value()).isEqualTo("new@example.com");
        assertThat(result.passwordHash()).isEqualTo("encodedPassword");
    }

    // Feature: indonesian-learning-app, Property 36: 已存在邮箱注册应抛出异常
    @Test
    void register_withExistingEmail_throwsException() {
        Email email = new Email("exists@example.com");
        when(emailVerificationService.verifyCode(email, "123456")).thenReturn(true);
        User existing = new User(1L, email, "encodedPassword");
        when(userRepository.findByEmail(email)).thenReturn(existing);

        assertThatThrownBy(() -> authAppService.register(new RegisterCommand("exists@example.com", "rawPassword", "123456")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("邮箱已被注册");
    }

    // Feature: indonesian-learning-app, Property 66: 验证码无效时注册应抛出异常
    @Test
    void register_withInvalidCode_throwsException() {
        Email email = new Email("new@example.com");
        when(emailVerificationService.verifyCode(email, "000000")).thenReturn(false);

        assertThatThrownBy(() -> authAppService.register(new RegisterCommand("new@example.com", "rawPassword", "000000")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("验证码无效或已过期");
    }
}
