package com.huakai.indonesian.infrastructure.persistence;

import com.huakai.indonesian.domain.entity.User;
import com.huakai.indonesian.domain.repository.UserRepository;
import com.huakai.indonesian.domain.valueobject.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepositoryImpl 集成测试
 * 使用 H2 内存数据库验证 MyBatis-Plus 持久化操作
 */
@SpringBootTest
@ActiveProfiles("test")
class UserRepositoryImplTest {

    @Autowired
    private UserRepository userRepository;

    // Feature: indonesian-learning-app, Property 53: 保存新用户后应能通过 ID 查询
    @Test
    void save_newUser_thenFindById_returnsUser() {
        Email email = new Email("test1@example.com");
        User user = new User(null, email, "encodedPassword");
        user.changeDailyGoal(15);

        User saved = userRepository.save(user);

        assertThat(saved.id()).isNotNull();
        User found = userRepository.findById(saved.id());
        assertThat(found).isNotNull();
        assertThat(found.email().value()).isEqualTo("test1@example.com");
        assertThat(found.dailyGoal()).isEqualTo(15);
    }

    // Feature: indonesian-learning-app, Property 54: 保存用户后应能通过邮箱查询
    @Test
    void save_thenFindByEmail_returnsUser() {
        Email email = new Email("test2@example.com");
        User user = new User(null, email, "encodedPassword");

        userRepository.save(user);
        User found = userRepository.findByEmail(email);

        assertThat(found).isNotNull();
        assertThat(found.email().value()).isEqualTo("test2@example.com");
    }

    // Feature: indonesian-learning-app, Property 55: 不存在的邮箱应返回 null
    @Test
    void findByEmail_nonExistent_returnsNull() {
        Email email = new Email("nobody@example.com");
        User found = userRepository.findByEmail(email);
        assertThat(found).isNull();
    }

    // Feature: indonesian-learning-app, Property 56: 更新已存在用户应生效
    @Test
    void save_existingUser_updatesFields() {
        Email email = new Email("update@example.com");
        User user = new User(null, email, "encodedPassword");
        User saved = userRepository.save(user);

        saved.changeDailyGoal(50);
        saved.changeUiLanguage("en-US");
        userRepository.save(saved);

        User updated = userRepository.findById(saved.id());
        assertThat(updated.dailyGoal()).isEqualTo(50);
        assertThat(updated.uiLanguage()).isEqualTo("en-US");
    }
}
