package com.huakai.indonesian.domain.valueobject;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Email 值对象的属性测试
 * 验证邮箱格式校验与不可变性
 */
class EmailTest {

    // Feature: indonesian-learning-app, Property 8: 有效邮箱格式应被正确接受并保存
    @Property(tries = 200)
    void validEmail_isAccepted(@ForAll("validEmails") String rawEmail) {
        Email email = new Email(rawEmail);
        assertThat(email.value()).isEqualTo(rawEmail.toLowerCase());
    }

    // Feature: indonesian-learning-app, Property 9: 无效邮箱格式应抛出 IllegalArgumentException
    @Property(tries = 200)
    void invalidEmail_throwsException(@ForAll("invalidEmails") String rawEmail) {
        assertThatThrownBy(() -> new Email(rawEmail))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("邮箱格式不正确");
    }

    // Feature: indonesian-learning-app, Property 10: 邮箱地址应统一转换为小写存储
    @Example
    void email_isStoredInLowerCase() {
        Email email = new Email("TestUser@EXAMPLE.COM");
        assertThat(email.value()).isEqualTo("testuser@example.com");
    }

    // Feature: indonesian-learning-app, Property 11: 两个相同邮箱应相等（值对象语义）
    @Example
    void sameEmails_areEqual() {
        Email email1 = new Email("user@example.com");
        Email email2 = new Email("USER@EXAMPLE.COM");
        assertThat(email1).isEqualTo(email2);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }

    // Feature: indonesian-learning-app, Property 12: 空字符串邮箱应抛出异常
    @Example
    void blankEmail_throwsException() {
        assertThatThrownBy(() -> new Email(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("邮箱格式不正确");
    }

    // Feature: indonesian-learning-app, Property 13: null 邮箱应抛出异常
    @Example
    void nullEmail_throwsException() {
        assertThatThrownBy(() -> new Email(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("邮箱格式不正确");
    }

    /**
     * 提供有效邮箱格式的生成器
     */
    @Provide
    Arbitrary<String> validEmails() {
        return Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(1).ofMaxLength(10)
            .map(local -> local + "@example.com");
    }

    /**
     * 提供无效邮箱格式的生成器
     */
    @Provide
    Arbitrary<String> invalidEmails() {
        return Arbitraries.of(
            "not-an-email",
            "missing-at-sign.com",
            "@no-local-part.com",
            "spaces in@email.com",
            "double@@at.com"
        );
    }
}
