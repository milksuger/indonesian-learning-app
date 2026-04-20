package com.huakai.indonesian.application.service;

import com.huakai.indonesian.application.command.LoginCommand;
import com.huakai.indonesian.application.command.RegisterCommand;
import com.huakai.indonesian.domain.entity.User;
import com.huakai.indonesian.domain.repository.UserRepository;
import com.huakai.indonesian.domain.service.PasswordEncoder;
import com.huakai.indonesian.domain.service.TokenProvider;
import com.huakai.indonesian.domain.valueobject.Email;
import org.springframework.stereotype.Service;

/**
 * 认证应用服务
 * 协调用户注册与登录用例，编排领域对象与外部服务
 */
@Service
public class AuthAppService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final EmailVerificationService emailVerificationService;

    /**
     * 构造认证应用服务
     */
    public AuthAppService(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          TokenProvider tokenProvider,
                          EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * 用户登录
     * 校验邮箱与密码，生成 JWT 令牌
     * 无论邮箱不存在还是密码错误，均返回统一错误消息以防止邮箱枚举攻击
     *
     * @param command 登录命令
     * @return JWT 访问令牌
     * @throws IllegalArgumentException 认证失败时抛出
     */
    public String login(LoginCommand command) {
        Email email = new Email(command.email());
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("邮箱或密码错误");
        }

        boolean passwordMatches = passwordEncoder.matches(command.password(), user.passwordHash());
        if (!passwordMatches) {
            throw new IllegalArgumentException("邮箱或密码错误");
        }

        return tokenProvider.generateToken(user.id(), user.email().value(), "user");
    }

    /**
     * 用户注册
     * 校验验证码有效性，检查邮箱是否已被注册，创建新用户并持久化
     *
     * @param command 注册命令（包含邮箱、密码、验证码）
     * @return 创建成功的用户实体
     * @throws IllegalArgumentException 验证码无效或邮箱已存在时抛出
     */
    public User register(RegisterCommand command) {
        Email email = new Email(command.email());

        boolean codeValid = emailVerificationService.verifyCode(email, command.verificationCode());
        if (!codeValid) {
            throw new IllegalArgumentException("验证码无效或已过期");
        }

        User existing = userRepository.findByEmail(email);
        if (existing != null) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        String encodedPassword = passwordEncoder.encode(command.password());
        User newUser = new User(null, email, encodedPassword);
        return userRepository.save(newUser);
    }

    /**
     * 向指定邮箱发送注册验证码
     *
     * @param email 目标邮箱
     */
    public void sendRegistrationCode(String email) {
        emailVerificationService.sendVerificationCode(new Email(email));
    }
}
