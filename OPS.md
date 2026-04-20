# 印尼语学习 Web 应用 — 运维手册

本文档解答两个最常见的运维问题：
1. 代码更新后如何部署新版本？
2. 如何向系统新增单词数据？

---

## 一、代码更新后怎么办？

### 核心原则

你的数据（用户账号、学习进度、单词等）全部存在 MySQL 容器的数据卷里，**和代码是完全分离的**。更新代码只需要替换前端静态文件和后端 JAR 包，数据库完全不受影响。

### 快速更新流程（约 2 分钟）

```bash
# 1. 在服务器进入项目目录
cd ~/app/indonesian-learning

# 2. 用你本地构建好的新代码覆盖服务器上的旧代码
#    - 上传 frontend/dist 到服务器的 ./frontend/dist
#    - 上传 frontend-admin/dist 到服务器的 ./frontend-admin/dist
#    - 上传 backend/target/*.jar 到服务器的 ./backend/target/

# 3. 只重启后端和 Nginx，MySQL 不动
docker compose restart backend nginx
```

就这么简单。MySQL 容器不需要重启，里面的数据完全保留。

### 如果改动很大（比如加了新表）

```bash
cd ~/app/indonesian-learning

# 先备份数据库（保险起见）
docker exec indonesian_mysql mysqldump -uroot -pYourPassword indonesian_learning \
  > backup_before_update_$(date +%Y%m%d_%H%M%S).sql

# 更新代码后重启全部服务
docker compose down
docker compose up --build -d
```

> **为什么 `docker compose down` 不会丢数据？**
> MySQL 使用了命名卷 `mysql_data`，`docker compose down` 只会停止容器，不会删除命名卷。
> 除非你执行 `docker compose down -v`，数据才会丢失。

---

## 二、如何新增单词数据？

你有 **3 种方式**，按推荐程度排序：

### 方式 1：管理后台网页操作（最简单，适合日常维护）

部署后访问 `https://你的域名/admin`

- **批量导入**：准备 JSON 文件，一键导入整个词书
- **单个添加**：在词书管理页点击「+ 添加单词」，填表单即可
- **编辑 / 删除**：直接点单词旁边的按钮

**JSON 导入文件格式示例：**

```json
{
  "name": "日常用语 100 句",
  "nameEn": "Daily Expressions",
  "level": "A1",
  "words": [
    {
      "indonesian": "Selamat pagi",
      "chinese": "早上好",
      "english": "Good morning",
      "exampleIndonesian": "Selamat pagi, apa kabar?",
      "exampleZh": "早上好，你好吗？",
      "exampleEn": "Good morning, how are you?"
    }
  ]
}
```

**导入规则：**
- 如果 JSON 中没有 `wordbookId` 参数，会**创建一个新词书**
- 如果提供 `wordbookId` 参数，会**追加到已有词书**
- 重复的单词不会自动去重，导入前请自行检查

### 方式 2：直接操作数据库（适合大批量，会 SQL 的话）

```bash
# 进入 MySQL 容器
docker exec -it indonesian_mysql mysql -uroot -p

# 选择数据库
USE indonesian_learning;

# 插入单个单词
INSERT INTO words (
  wordbook_id, indonesian, chinese, english,
  example_indonesian, example_zh, example_en,
  sort_order, created_at
) VALUES (
  1, 'Halo', '你好', 'Hello',
  'Halo, apa kabar?', '你好，你好吗？', 'Hello, how are you?',
  1, NOW()
);
```

> **注意**：`sort_order` 需要手动计算，建议先查询该词书现有最大 sort_order，再 +1。

### 方式 3：写脚本调用 API 自动导入（适合有外部词库）

如果你有一个 Excel 或外部词库，可以写脚本调用管理后台接口批量导入。

**API 示例（curl）：**

```bash
# 1. 先登录获取管理员权限（如果需要）
# 2. 调用导入接口
curl -X POST https://yourdomain.com/api/v1/admin/import \
  -F "file=@/path/to/your/wordbook.json" \
  -F "wordbookId=1"  # 可选：追加到已有词书，不传则新建
```

**Python 脚本示例：**

```python
import requests
import json

API_BASE = "https://yourdomain.com/api/v1"
TOKEN = "your-jwt-token"  # 从登录接口获取

# 读取 JSON 文件
with open("new_words.json", "r", encoding="utf-8") as f:
    wordbook = json.load(f)

# 调用导入接口
headers = {"Authorization": f"Bearer {TOKEN}"}
files = {"file": ("wordbook.json", json.dumps(wordbook, ensure_ascii=False), "application/json")}
response = requests.post(f"{API_BASE}/admin/import", files=files, headers=headers)

print(response.json())
```

---

## 三、常见运维场景速查表

| 场景 | 操作 | 是否影响已有数据 |
|------|------|----------------|
| 修复前端 Bug | 重新构建 `frontend/dist`，上传，重启 nginx | ❌ 不影响 |
| 修复后端 Bug | 重新打包 JAR，上传，重启 backend | ❌ 不影响 |
| 新增前端页面 | 重新构建前端，上传，重启 nginx | ❌ 不影响 |
| 新增后端接口 | 重新打包 JAR，上传，重启 backend | ❌ 不影响 |
| 数据库结构变更（加字段/表） | `docker compose down && up --build` | ⚠️ 先备份 |
| 新增单词 | 管理后台 `/admin` 网页操作 | ❌ 不影响 |
| 批量导入单词 | 管理后台上传 JSON 或脚本调用 API | ❌ 不影响 |
| 修改服务器配置 | 编辑 `.env`，重启相关服务 | ❌ 不影响 |

---

## 四、数据备份建议

强烈建议设置自动备份，防止意外丢失。

```bash
# 手动备份
docker exec indonesian_mysql mysqldump -uroot -pYourPassword indonesian_learning \
  > backup_$(date +%Y%m%d).sql

# 自动备份（添加到 crontab，每天凌晨 2 点执行）
0 2 * * * docker exec indonesian_mysql mysqldump -uroot -pYourPassword indonesian_learning \
  > /root/backups/indonesian_learning_$(date +\%Y\%m\%d).sql
```

---

## 五、重要提醒

1. **数据卷不会随容器删除而丢失**：`docker compose down` 默认保留命名卷，`docker compose down -v` 才会删除。

2. **更新代码前备份数据库**：养成习惯，每次更新前执行一次 `mysqldump`，有备无患。

3. **不要直接在服务器上修改代码**：所有代码修改应在本地开发环境完成，测试通过后再部署到服务器。

4. **新增单词优先用管理后台**：管理后台有表单验证和错误提示，比直接写 SQL 更安全。

---

如有其他运维问题，请参考 `DEPLOY.md` 中的故障排查章节。
