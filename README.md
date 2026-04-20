# 印尼语学习 Web 应用

一款面向中文母语者的印尼语词汇学习 Web 应用，采用闪卡（FlashCard）形式结合 SRS（间隔重复）算法，帮助用户高效记忆印尼语单词。支持每日学习队列、复习提醒、签到打卡、收藏与已掌握管理，并内置 AI 语音合成发音功能。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端（用户端） | Vue 3 + Vite + Naive UI + Pinia + Vue Router + Vue I18n |
| 前端（管理端） | Vue 3 + Vite + Naive UI |
| 后端 | Spring Boot 3.2.x + MyBatis-Plus + Java 17 |
| 数据库 | MySQL 8 |
| 认证 | JWT (HS256) + 邮箱验证码（163 SMTP） |
| 语音合成 | Web Speech API（浏览器原生，免费） |
| 部署 | Docker Compose + Nginx |

---

## 功能特性

### 用户端
- **用户认证**：邮箱注册（验证码）、登录、JWT Token 认证
- **闪卡学习**：每日新词队列，点击翻转查看释义和例句
- **SRS 复习系统**：基于间隔重复算法，自动计算下次复习日期
- **语音发音**：单词和例句支持 AI 语音朗读，可设置自动发音和语速
- **词书管理**：浏览系统词书，激活/停用学习词书
- **收藏与已掌握**：标记收藏单词，手动标记已掌握
- **错题本**：记录"不认识"的单词，便于针对性复习
- **每日签到**：完成每日学习目标后自动签到，连续签到统计
- **个人设置**：界面语言（中/英）、主题模式（浅色/深色）、每日学习目标
- **响应式设计**：PC 端顶部导航 + 移动端底部导航自适应

### 管理后台
- **批量导入**：通过 JSON 文件批量导入词书和单词，支持追加到已有词书
- **词书 CRUD**：创建、编辑、删除词书
- **单词 CRUD**：在词书内添加、编辑、删除单词
- **分页查看**：词书内单词支持分页展示，可调整每页条数
- **全局搜索**：跨词书搜索单词，支持印尼语/中文/英文模糊匹配
- **导出功能**：将词书及单词导出为 JSON 文件
- **用户统计**：查看注册用户列表及学习进度统计
- **系统概览**：查看总用户数、词书数、单词数、学习次数等统计数据

---

## 项目结构

```
MyApp/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/huakai/indonesian/
│   │       ├── domain/         # 领域层（实体、值对象、领域服务、仓储接口）
│   │       ├── application/    # 应用层（应用服务、DTO、Command）
│   │       ├── infrastructure/ # 基础设施层（MyBatis-Plus、JWT、配置）
│   │       └── interfaces/     # 接口层（REST Controller）
│   ├── src/test/java/          # 单元测试与集成测试
│   └── pom.xml
│
├── frontend/                   # Vue 3 用户端
│   ├── src/
│   │   ├── components/         # 公共组件（FlashCard、AppNav）
│   │   ├── composables/        # 组合式函数（useSpeech）
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── views/              # 页面视图
│   │   ├── i18n/               # 国际化（zh-CN / en-US）
│   │   └── utils/              # 工具函数（Axios 封装）
│   └── package.json
│
├── frontend-admin/             # Vue 3 管理后台
│   ├── src/
│   │   ├── views/              # 页面（ImportPage、WordbookPage）
│   │   └── App.vue
│   └── package.json
│
└── README.md
```

---

## 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+

### 1. 数据库配置

创建数据库并执行初始化脚本：

```sql
CREATE DATABASE indonesian_learning CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

 application.yml 中配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/indonesian_learning?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`

### 3. 启动用户端前端

```bash
cd frontend
npm install
npm run dev
```

用户端默认运行在 `http://localhost:5173`

### 4. 启动管理后台

```bash
cd frontend-admin
npm install
npm run dev
```

管理后台默认运行在 `http://localhost:5174`

---

## API 接口

后端提供 RESTful API，基础路径为 `/api/v1`：

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `POST /api/v1/auth/register` | 用户注册 |
| 认证 | `POST /api/v1/auth/login` | 用户登录 |
| 认证 | `POST /api/v1/auth/send-code` | 发送邮箱验证码 |
| 学习 | `GET /api/v1/learning/queue` | 获取今日新词队列 |
| 学习 | `POST /api/v1/learning/feedback` | 提交学习反馈 |
| 复习 | `GET /api/v1/review/queue` | 获取今日复习队列 |
| 复习 | `POST /api/v1/review/feedback` | 提交复习反馈 |
| 用户 | `GET /api/v1/users/me` | 获取用户资料 |
| 用户 | `PUT /api/v1/users/me/settings` | 更新用户设置 |
| 管理 | `POST /api/v1/admin/import` | 批量导入词书 |
| 管理 | `GET /api/v1/admin/wordbooks` | 查询所有词书 |
| 管理 | `GET /api/v1/admin/wordbooks/{id}/words` | 查询词书单词 |
| 管理 | `POST /api/v1/admin/wordbooks` | 创建词书 |
| 管理 | `PUT /api/v1/admin/wordbooks/{id}` | 更新词书 |
| 管理 | `DELETE /api/v1/admin/wordbooks/{id}` | 删除词书 |
| 管理 | `POST /api/v1/admin/wordbooks/{id}/words` | 添加单词 |
| 管理 | `PUT /api/v1/admin/words/{id}` | 更新单词 |
| 管理 | `DELETE /api/v1/admin/words/{id}` | 删除单词 |
| 管理 | `GET /api/v1/admin/words/search` | 全局搜索单词（分页） |
| 管理 | `GET /api/v1/admin/users` | 用户列表（含学习统计） |
| 管理 | `GET /api/v1/admin/stats` | 系统概览统计 |

启动后端后，可通过 Knife4j 查看完整 API 文档：`http://localhost:8080/doc.html`

---

## 语音功能说明

应用使用浏览器原生 **Web Speech API** 实现印尼语语音合成，**完全免费，无需申请任何 API Key**。

- 支持自动发音（卡片切换时自动朗读）
- 支持手动点击播放按钮朗读单词和例句
- 语速可调（0.5x ~ 1.5x）
- 自动选择印尼语发音人（`id-ID`），兼容 Chrome、Edge、Firefox、Safari

> 提示：首次使用时，浏览器可能需要联网下载语音包，建议在稳定的网络环境下使用。

---

## 部署

### Docker Compose 部署

项目支持通过 Docker Compose 一键部署：

```bash
docker-compose up --build
```

部署包含：
- MySQL 8 数据库容器
- Spring Boot 后端容器
- Nginx 反向代理容器（前端静态资源 + API 代理）

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 | - |
| `MYSQL_DATABASE` | 数据库名 | indonesian_learning |
| `JWT_SECRET` | JWT 签名密钥 | - |
| `MAIL_USERNAME` | 163 邮箱账号 | - |
| `MAIL_PASSWORD` | 163 邮箱授权码 | - |
| `ALLOWED_ORIGINS` | 跨域允许的来源（多个用逗号分隔） | `http://localhost:5173` |

---

## 开发规范

本项目遵循以下开发规范：

- **架构**：六边形架构（Domain / Application / Infrastructure / Interfaces）
- **测试**：TDD 测试驱动开发，单元测试覆盖率 >= 80%
- **代码质量**：函数 <= 50 行，文件 <= 800 行，嵌套 <= 3 层
- **安全**：密码 BCrypt 加密，JWT 7 天有效期，无硬编码密钥
- **国际化**：所有用户界面文本通过 Vue I18n 管理，支持中英文切换
- **主题**：支持浅色/深色模式，通过 Naive UI NConfigProvider 统一管理

---

## 测试

### 后端测试

```bash
cd backend
mvn test
```

### 前端测试

```bash
cd frontend
npm run test:unit:run
```

---

## 许可证

MIT License
