# 需求文档

## 简介

本项目是一款面向汉语母语者的印尼语单词学习 Web App，旨在填补市场上缺少中文界面印尼语学习工具的空白。
用户可通过单词卡翻卡、间隔重复复习、每日打卡等功能系统地学习印尼语词汇。
第一版目标词汇量为 500~1000 个基础词汇，后期可扩展至进阶词汇及收费功能。

技术栈：Vue 3 + Vite + Naive UI（前端）、Spring Boot 3 + MyBatis-Plus（后端）、MySQL 8（数据库）、Docker Compose 部署。

---

## 词汇表

- **学习系统（Learning_System）**：负责向用户展示单词卡、记录翻卡结果的核心模块
- **复习系统（Review_System）**：基于间隔重复算法管理复习队列的模块
- **用户系统（User_System）**：负责注册、登录、打卡、学习记录的模块
- **单词书（Wordbook）**：由管理员预置的单词集合，按难度分级（基础、进阶等）
- **单词卡（Flashcard）**：包含印尼语单词、中/英翻译、例句及例句翻译的学习单元
- **认识（Known）**：用户对单词卡的正向反馈，触发间隔拉长
- **不认识（Unknown）**：用户对单词卡的负向反馈，触发快速复现
- **收藏（Favorite）**：用户对单词的纯标记操作，不影响推荐逻辑
- **已记住（Memorized）**：用户标记后系统停止推荐该单词，可手动撤销
- **错题本（Mistake_Book）**：汇集所有标记为"不认识"的单词集合
- **每日目标（Daily_Goal）**：用户自定义的每日新词学习数量
- **打卡（Check_In）**：完成当日新词 + 复习词目标后触发的成就记录
- **间隔重复算法（SRS）**：类 SM-2 的轻量算法，根据反馈动态调整下次复习间隔
- **邮件服务（Email_Service）**：通过 163 SMTP 发送验证码的基础设施模块
- **界面语言（UI_Language）**：支持中文/英文切换的界面本地化设置
- **主题模式（Theme_Mode）**：支持日间/夜间切换的界面主题设置

---

## 需求

### 需求 1：用户注册

**用户故事：** 作为新用户，我希望通过邮箱注册账号，以便保存我的学习进度和个人设置。

#### 验收条件

1. THE User_System SHALL 提供邮箱注册入口，要求用户填写邮箱地址和密码。
2. WHEN 用户提交注册表单，THE Email_Service SHALL 向该邮箱发送 6 位数字验证码，有效期为 10 分钟。
3. WHEN 用户在 10 分钟内输入正确验证码，THE User_System SHALL 创建账号并自动登录。
4. IF 用户输入的验证码错误，THEN THE User_System SHALL 提示"验证码错误，请重新输入"，并允许重试。
5. IF 用户输入的验证码已过期，THEN THE User_System SHALL 提示"验证码已过期"，并提供重新发送入口。
6. IF 用户提交的邮箱已被注册，THEN THE User_System SHALL 提示"该邮箱已注册，请直接登录"。
7. THE User_System SHALL 对密码进行加密存储，不得以明文形式保存。

---

### 需求 2：用户登录

**用户故事：** 作为已注册用户，我希望通过邮箱和密码登录，以便继续我的学习进度。

#### 验收条件

1. WHEN 用户输入正确的邮箱和密码，THE User_System SHALL 完成登录并跳转至主页。
2. IF 用户输入的邮箱不存在，THEN THE User_System SHALL 提示"邮箱或密码错误"。
3. IF 用户输入的密码错误，THEN THE User_System SHALL 提示"邮箱或密码错误"（不区分具体原因，防止枚举攻击）。
4. THE User_System SHALL 在登录成功后颁发 JWT Token，有效期不少于 7 天。
5. WHILE 用户处于已登录状态，THE User_System SHALL 在 Token 有效期内免登录访问所有功能。

---

### 需求 3：选择单词书

**用户故事：** 作为学习者，我希望从多本预置单词书中选择适合自己的一本，以便按难度系统学习。

#### 验收条件

1. THE Learning_System SHALL 展示所有可用单词书列表，每本单词书显示名称、难度级别和总词汇数。
2. WHEN 用户选择一本单词书，THE Learning_System SHALL 将该单词书设为当前学习书目，并记录用户的选择。
3. THE Learning_System SHALL 允许用户同时激活多本单词书。
4. WHEN 用户切换单词书，THE Learning_System SHALL 保留原单词书中已有的学习进度，不重置。

---

### 需求 4：单词卡学习

**用户故事：** 作为学习者，我希望通过翻卡方式学习单词，以便在轻松的交互中记忆印尼语词汇。

#### 验收条件

1. WHEN 用户进入学习模式，THE Learning_System SHALL 展示单词卡正面，默认显示印尼语单词。
2. THE Learning_System SHALL 在单词卡正面隐藏中文翻译、英文翻译、例句及例句翻译。
3. WHEN 用户点击"显示翻译"，THE Learning_System SHALL 展示该单词卡的完整内容：印尼语单词、中文翻译、英文翻译、印尼语例句、例句中文翻译、例句英文翻译。
4. WHEN 单词卡翻转后，THE Learning_System SHALL 显示"认识"和"不认识"两个反馈按钮。
5. THE Learning_System SHALL 支持顺序模式（按单词书顺序展示）和随机模式（随机打乱顺序展示），用户可在学习前选择。
6. WHEN 用户点击"认识"，THE Learning_System SHALL 记录该次正向反馈，并展示下一张单词卡。
7. WHEN 用户点击"不认识"，THE Learning_System SHALL 记录该次负向反馈，并展示下一张单词卡。
8. WHILE 用户处于学习模式，THE Learning_System SHALL 在页面顶部显示当前进度（如"12 / 20"）。

---

### 需求 5：单词收藏

**用户故事：** 作为学习者，我希望收藏感兴趣的单词，以便日后快速查找和复习。

#### 验收条件

1. THE Learning_System SHALL 在每张单词卡上提供收藏按钮（星形图标）。
2. WHEN 用户点击收藏按钮，THE Learning_System SHALL 将该单词加入收藏列表，按钮状态切换为已收藏。
3. WHEN 用户再次点击已收藏按钮，THE Learning_System SHALL 取消收藏，按钮状态恢复为未收藏。
4. THE Learning_System SHALL 提供收藏列表页面，展示所有已收藏单词。
5. THE Learning_System SHALL 确保收藏操作不影响单词的推荐逻辑和复习队列。

---

### 需求 6：标记已记住

**用户故事：** 作为学习者，我希望将已完全掌握的单词标记为"已记住"，以便系统不再重复推荐，专注于未掌握的词汇。

#### 验收条件

1. THE Learning_System SHALL 在每张单词卡上提供"已记住"标记按钮。
2. WHEN 用户点击"已记住"，THE Learning_System SHALL 将该单词状态设为已记住，并从推荐队列中移除。
3. WHILE 单词处于已记住状态，THE Review_System SHALL 不再将该单词加入复习队列。
4. THE Learning_System SHALL 提供"已记住"单词列表页面，展示所有已标记单词。
5. WHEN 用户在"已记住"列表中点击"移出"，THE Learning_System SHALL 将该单词状态恢复为未记住，并重新纳入推荐队列。

---

### 需求 7：间隔重复复习

**用户故事：** 作为学习者，我希望系统根据我的反馈智能安排复习计划，以便高效巩固记忆。

#### 验收条件

1. THE Review_System SHALL 基于类 SM-2 的轻量间隔重复算法管理每个单词的复习间隔。
2. WHEN 用户对某单词点击"认识"，THE Review_System SHALL 延长该单词的下次复习间隔（最短间隔翻倍，上限为 30 天）。
3. WHEN 用户对某单词点击"不认识"，THE Review_System SHALL 将该单词的复习间隔重置为 1 天，并在当日复习队列中优先安排。
4. WHEN 用户进入复习模式，THE Review_System SHALL 展示所有到期需复习的单词卡。
5. THE Review_System SHALL 在主页显示今日待复习单词数量。
6. IF 今日无待复习单词，THEN THE Review_System SHALL 显示"今日复习已完成"提示。

---

### 需求 8：错题本

**用户故事：** 作为学习者，我希望查看所有标记为"不认识"的单词，以便集中攻克薄弱词汇。

#### 验收条件

1. THE Review_System SHALL 自动将所有被标记为"不认识"的单词收录至错题本。
2. THE Review_System SHALL 提供错题本页面，展示所有错题单词及其翻译。
3. WHEN 用户在错题本中对某单词点击"认识"，THE Review_System SHALL 将该单词从错题本中移除，并更新复习间隔。
4. THE Review_System SHALL 在错题本页面显示当前错题总数。

---

### 需求 9：每日目标设置

**用户故事：** 作为学习者，我希望自定义每日学习新词数量，以便根据自己的时间安排制定合理计划。

#### 验收条件

1. THE User_System SHALL 在用户首次登录时引导设置每日目标单词数，默认推荐值为 20 个。
2. THE User_System SHALL 允许用户将每日目标设置为不小于 1 的任意正整数，无上限限制。
3. WHEN 用户修改每日目标，THE User_System SHALL 立即保存新目标，并从次日起生效。
4. THE Learning_System SHALL 在主页显示今日新词学习进度（如"已学 8 / 目标 20"）。

---

### 需求 10：每日打卡

**用户故事：** 作为学习者，我希望完成每日目标后自动打卡，以便通过连续打卡记录激励自己坚持学习。

#### 验收条件

1. WHEN 用户完成今日新词目标数量且完成今日所有到期复习词，THE User_System SHALL 自动触发当日打卡成功。
2. WHEN 打卡成功，THE User_System SHALL 在页面展示打卡成功动效，并更新连续打卡天数。
3. THE User_System SHALL 在主页显示用户当前连续打卡天数。
4. IF 用户当日未完成目标，THEN THE User_System SHALL 在次日将连续打卡天数重置为 0。
5. THE User_System SHALL 提供打卡日历视图，展示历史打卡记录（已打卡日期高亮显示）。

---

### 需求 11：今日学习记录

**用户故事：** 作为学习者，我希望查看今天学习了哪些新词，以便回顾和巩固当日学习成果。

#### 验收条件

1. THE Learning_System SHALL 记录用户每日首次学习的新单词列表（今日新词）。
2. THE Learning_System SHALL 提供今日学习记录页面，展示当日所有新词及其翻译。
3. THE Learning_System SHALL 在主页显示今日已学新词数量。
4. WHEN 用户查看今日学习记录，THE Learning_System SHALL 按学习时间顺序排列单词。

---

### 需求 12：界面语言切换

**用户故事：** 作为用户，我希望在中文和英文界面之间切换，以便根据个人偏好使用 App。

#### 验收条件

1. THE User_System SHALL 在系统设置中提供界面语言切换选项，支持中文和英文两种语言。
2. WHEN 用户切换界面语言，THE User_System SHALL 立即刷新所有界面文本为所选语言，无需重新登录。
3. THE User_System SHALL 持久化保存用户的语言偏好，下次登录时自动应用。
4. THE User_System SHALL 默认使用中文界面。

---

### 需求 13：日间/夜间模式切换

**用户故事：** 作为用户，我希望在日间和夜间模式之间切换，以便在不同光线环境下舒适地使用 App。

#### 验收条件

1. THE User_System SHALL 在系统设置中提供主题模式切换选项，支持日间模式和夜间模式。
2. WHEN 用户切换主题模式，THE User_System SHALL 立即应用新主题，无需刷新页面。
3. THE User_System SHALL 持久化保存用户的主题偏好，下次登录时自动应用。
4. THE User_System SHALL 默认使用日间模式。

---

### 需求 14：单词数据管理

**用户故事：** 作为管理员，我希望通过 JSON 格式批量导入单词数据，以便高效维护单词书内容。

#### 验收条件

1. THE Learning_System SHALL 支持通过 JSON 文件批量导入单词卡数据，每条数据包含：印尼语单词、中文翻译、英文翻译、印尼语例句、例句中文翻译、例句英文翻译。
2. WHEN 导入 JSON 文件，THE Learning_System SHALL 校验每条数据的必填字段完整性。
3. IF 导入数据中存在字段缺失，THEN THE Learning_System SHALL 跳过该条数据并在导入报告中列出错误项。
4. THE Learning_System SHALL 支持将单词卡分配至指定单词书（基础、进阶等）。
5. WHEN 导入完成，THE Learning_System SHALL 返回导入成功数量和失败数量的汇总报告。
