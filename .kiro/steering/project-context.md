# Project Context: 印尼语学习 Web App

## Purpose

面向汉语母语者的印尼语单词学习 Web App，填补市场上缺少中文界面印尼语学习工具的空白。用户通过单词卡翻卡、间隔重复复习（SRS）、每日打卡等功能系统地学习印尼语词汇。第一版目标词汇量 500~1000 个基础词汇。

## Architecture Overview

前后端分离架构，后端采用 DDD 六边形架构。前端 Vue 3 SPA 通过 REST API 与 Spring Boot 后端通信，Nginx 作反向代理，MySQL 8 持久化数据，Docker Compose 一键部署。

```
Vue 3 (SPA)  →  Nginx (反向代理)  →  Spring Boot 3 (DDD)  →  MySQL 8
```

## Key Directories

| Path | Purpose |
|------|---------|
| `frontend/src/api/` | Axios 封装，按模块分文件 |
| `frontend/src/components/` | 通用组件（FlashCard, ProgressBar, CalendarView） |
| `frontend/src/views/` | 页面视图（Login, Learn, Review, Profile 等） |
| `frontend/src/stores/` | Pinia 状态管理（user, learning, review, settings） |
| `frontend/src/router/` | Vue Router 路由配置 |
| `frontend/src/i18n/` | 国际化（zh-CN, en-US） |
| `frontend/src/composables/` | 可复用逻辑（useFlashcard, useSrs, useTheme） |
| `backend/src/main/java/com/huakai/indonesian/domain/` | 领域层（实体、值对象、聚合根、仓储接口、领域服务） |
| `backend/src/main/java/com/huakai/indonesian/application/` | 应用层（用例编排、DTO、Command、Query） |
| `backend/src/main/java/com/huakai/indonesian/infrastructure/` | 基础设施层（MyBatis-Plus、邮件、配置） |
| `backend/src/main/java/com/huakai/indonesian/interfaces/` | 接口层（REST Controller、请求/响应 DTO） |
| `nginx/` | Nginx 配置文件 |
| `docker-compose.yml` | Docker Compose 部署配置 |

## Entry Points

- 前端：`frontend/src/main.js`（Vue 3 应用入口）
- 后端：`backend/src/main/java/com/huakai/indonesian/IndonesianApplication.java`
- 部署：`docker-compose.yml`

## Core Modules / Components

- `Learning_System` — 单词卡展示、翻卡交互、今日新词队列生成、学习进度记录
- `Review_System` — 间隔重复算法（SRS）、复习队列管理、错题本维护
- `User_System` — 注册/登录（邮箱验证码）、JWT 认证、每日目标、打卡记录、设置
- `SrsInterval` — 核心值对象，类 SM-2 轻量 SRS 算法（认识→间隔翻倍上限30天，不认识→重置为1天）
- `FlashCard` — 前端核心组件，支持 CSS 3D 翻转动画
- `Email_Service` — 163 SMTP 发送注册验证码（6位数字，10分钟有效期）

## Business Logic Summary

1. **SRS 算法**：认识 → `min(interval × 2, 30)`，不认识 → `interval = 1, repetitions = 0`
2. **今日新词队列**：激活单词书中未学过且未标记已记住的单词，按 sort_order 或随机排列，取 `daily_goal - 今日已学` 条
3. **打卡触发**：`今日已学新词 >= daily_goal` 且 `今日复习队列全部完成` → 自动打卡，更新连续打卡天数
4. **单词状态流转**：`new → learning → reviewing → memorized`（可逆）
5. **错题本**：点击"不认识"时 `is_mistake = 1`，在错题本中点击"认识"时 `is_mistake = 0`

## External Dependencies & Integrations

- Naive UI — 前端组件库（优先使用，仅在无对应组件时自行实现）
- MyBatis-Plus — ORM 框架
- jjwt (0.12.x) — JWT 签发与验证（HS256，7天有效期）
- spring-security-crypto — BCrypt 密码加密（不引入完整 Spring Security）
- spring-boot-starter-mail — 163 SMTP 邮件发送
- jqwik (1.8.x) — 后端属性测试框架（兼容 JUnit 5）
- Lombok — 实体/DTO 注解简化（`@Data`, `@Builder`, `@Slf4j` 等）
- knife4j-openapi3-jakarta-spring-boot-starter (4.5.0) — 接口文档（Spring Boot 3 + OpenAPI 3，访问 `/doc.html`）
- Vitest + Vue Test Utils — 前端测试
- Docker Compose + Nginx + Let's Encrypt — 部署（阿里云 2核2G，域名 huakai.wang）

## Current Status

Active development — 规范文档（需求 + 设计）已完成，待实现。

## Open Questions / Known Issues

- 验证码存储在 MySQL（不引入 Redis），定时清理过期记录（每小时 `@Scheduled`）
- 服务器资源有限（2核2G），所有设计需考虑轻量化
- 前端不计算 SRS，仅展示后端返回的队列
