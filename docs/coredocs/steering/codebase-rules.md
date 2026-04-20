# Codebase Rules: 印尼语学习 Web App

## Language & Runtime

- 前端语言：JavaScript（Vue 3，不使用 TypeScript）
- 后端语言：Java 17+
- 包管理器（前端）：npm
- 构建工具（前端）：Vite
- 构建工具（后端）：Maven

## Framework & Libraries

- 前端框架：Vue 3 (Composition API)
- UI 组件库：Naive UI（优先使用，仅在无对应组件时自行实现）
- 状态管理：Pinia
- 路由：Vue Router
- HTTP 客户端：Axios
- 国际化：vue-i18n（zh-CN / en-US）
- 前端测试：Vitest + Vue Test Utils
- 后端框架：Spring Boot 3.2.x
- ORM：MyBatis-Plus 3.5.x
- 数据库：MySQL 8
- JWT：jjwt 0.12.x（HS256）
- 密码加密：spring-security-crypto（BCrypt，不引入完整 Spring Security）
- 邮件：spring-boot-starter-mail（163 SMTP）
- 属性测试：jqwik 1.8.x（兼容 JUnit 5）
- 接口文档：knife4j-openapi3-jakarta-spring-boot-starter 4.5.0（Spring Boot 3 专用，基于 OpenAPI 3 + springdoc-openapi）
- 实体/DTO 简化：Lombok（`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Getter`, `@Setter` 等）

## Directory Conventions

### 前端

- 页面视图：`frontend/src/views/`（命名：`XxxView.vue`）
- 通用组件：`frontend/src/components/`（命名：`XxxComponent.vue` 或 `Xxx.vue`）
- Pinia Store：`frontend/src/stores/`（命名：`useXxxStore.js`）
- Composables：`frontend/src/composables/`（命名：`useXxx.js`）
- API 封装：`frontend/src/api/`（按模块分文件，如 `auth.js`, `learning.js`）
- 国际化：`frontend/src/i18n/`（`zh-CN.js`, `en-US.js`）
- 工具函数：`frontend/src/utils/`

### 后端（DDD 六边形架构）

- 领域层：`domain/entity/`, `domain/valueobject/`, `domain/aggregate/`, `domain/repository/`, `domain/service/`, `domain/event/`
- 应用层：`application/service/`, `application/dto/`, `application/command/`, `application/query/`
- 基础设施层：`infrastructure/persistence/`, `infrastructure/config/`, `infrastructure/external/`
- 接口层：`interfaces/rest/`, `interfaces/dto/`
- 测试：`src/test/java/`（与 main 目录结构对应）

## Naming Conventions

### 前端

- 文件名（组件）：PascalCase（`FlashCard.vue`）
- 文件名（其他）：camelCase（`useFlashcard.js`, `auth.js`）
- 组件名：PascalCase
- 函数/变量：camelCase
- 常量：UPPER_SNAKE_CASE
- CSS 类名：Tailwind utilities 或 BEM（视情况）

### 后端

- 类名：PascalCase（`SrsInterval`, `LearningAppService`）
- 方法名：camelCase（`onKnown()`, `findByUserId()`）
- 常量：UPPER_SNAKE_CASE（`MAX_INTERVAL`）
- 包名：全小写（`com.huakai.indonesian.domain.entity`）
- 测试方法命名：`方法名_场景_期望结果`（`findById_whenWordExists_returnsWord`）

## Import Rules

- 前端使用 `@/` 别名作为 `src/` 的绝对路径
- 导入顺序：外部依赖 → 内部模块 → 相对路径

## Lombok 使用规范

- domain 层实体、值对象、DTO 均使用 Lombok 注解，禁止手写 getter/setter/constructor/toString
- 实体类（Entity）：使用 `@Data`（或 `@Getter` + `@Setter`）+ `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor`
- 值对象（ValueObject）：使用 Java `record` 优先；若需 Lombok，使用 `@Value`（不可变）
- DTO / Command / Query：使用 `@Data` + `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor`
- 禁止在 domain 层实体上使用 `@EqualsAndHashCode(callSuper = true)`（避免 JPA 代理问题）
- 使用 `@Slf4j` 替代手动声明 Logger

## Knife4j / 接口文档规范

- 依赖：`knife4j-openapi3-jakarta-spring-boot-starter:4.5.0`（Spring Boot 3 + Jakarta EE 专用）
- 访问地址：`/doc.html`（Knife4j 增强 UI）
- 配置类放在：`infrastructure/config/Knife4jConfig.java`
- 使用 OpenAPI 3 注解（`io.swagger.v3.oas.annotations.*`），禁止使用旧版 Swagger 2 注解（`io.swagger.annotations.*`）
- Controller 注解：`@Tag(name = "模块名", description = "描述")`
- 接口注解：`@Operation(summary = "接口名", description = "详细描述")`
- 参数注解：`@Parameter(description = "参数说明")`
- 请求体注解：`@Schema(description = "字段说明")` 加在 DTO 字段上
- 生产环境需关闭 Knife4j（`knife4j.enable: false`）

## Forbidden Patterns

- 禁止在 domain 层依赖 Spring、MyBatis 等任何框架
- 禁止在前端直接操作 DOM（使用 Vue 响应式）
- 禁止明文存储密码
- 禁止在登录失败时区分"邮箱不存在"和"密码错误"（防枚举攻击）
- 禁止引入 Redis（验证码用 MySQL 存储）
- 禁止引入完整 Spring Security（仅用 spring-security-crypto）
- 禁止使用 emoji 作为 UI 图标（使用 SVG 图标库）
- 禁止注释掉的死代码提交
- 禁止随意引入新依赖（需先确认）
- 前端禁止自行计算 SRS（由后端负责）

## Code Style

- 最大嵌套深度：3 层
- 单个方法：不超过 50 行
- 单个文件：不超过 1000 行
- 单个 Vue 组件：不超过 800 行
- 后端缩进：4 空格
- 前端缩进：2 空格
- 所有代码必须加中文注释（类、方法、复杂逻辑）

## API Conventions

- 所有接口前缀：`/api/v1`
- 统一错误响应格式：`{ "code": "ERROR_CODE", "message": "...", "timestamp": "..." }`
- 认证：请求头 `Authorization: Bearer <token>`
- JWT 存储：前端 localStorage

## Testing Requirements

- 后端 domain 层覆盖率 ≥ 80%，application 层 ≥ 70%
- 属性测试每个 property 最少运行 100 次迭代
- 属性测试注释格式：`// Feature: indonesian-learning-app, Property N: 描述`
- 发现 Bug 先写复现测试，再修复
- 前端重点测试：FlashCard 翻转逻辑、表单校验规则

## Performance Rules

- 所有设计需考虑 2核2G 低配服务器的资源限制
- 不引入重量级中间件（无 Redis、无消息队列）
- 验证码过期清理使用 Spring `@Scheduled`（每小时一次）
- 前端图片使用懒加载
