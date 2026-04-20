# 印尼语学习 Web 应用 — 部署说明书

本文档指导你从零开始，将本项目部署到生产服务器（以 Ubuntu 22.04/24.04 为例）。

---

## 一、环境准备

### 1.1 你需要的服务器

- **操作系统**：Ubuntu 22.04 LTS 或 24.04 LTS
- **CPU/内存**：至少 2 核 4GB（推荐 4 核 8GB）
- **硬盘**：至少 40GB SSD
- **带宽**：1Mbps 起步（访问速度取决于带宽）
- **域名**：可选，但强烈建议准备一个（如 `yourdomain.com`），并解析到服务器 IP

### 1.2 服务器基础环境

连接服务器后，依次执行以下命令安装基础依赖：

```bash
# 更新系统包
sudo apt update && sudo apt upgrade -y

# 安装常用工具
sudo apt install -y curl wget git vim ufw

# 安装 Docker（官方脚本）
curl -fsSL https://get.docker.com | sudo sh

# 启动 Docker 并设置开机自启
sudo systemctl start docker
sudo systemctl enable docker

# 将当前用户加入 docker 组（避免每次用 sudo）
sudo usermod -aG docker $USER

# 安装 Docker Compose
sudo apt install -y docker-compose-plugin

# 验证安装
docker --version
docker compose version
```

> **说明**：安装完成后，**退出并重新登录 SSH**，`docker` 命令才不需要 `sudo`。

### 1.3 本地开发环境（构建用）

你的本地电脑（Windows/macOS/Linux）需要安装：

| 软件 | 版本要求 | 下载地址 |
|------|---------|---------|
| Node.js | 18.x 或更高 | https://nodejs.org/ |
| Java JDK | 17 | https://adoptium.net/ |
| Maven | 3.6+ | https://maven.apache.org/download.cgi |
| Git | 任意版本 | https://git-scm.com/ |

---

## 二、获取项目代码

### 2.1 在服务器上创建项目目录

```bash
# 登录服务器后
mkdir -p ~/app
cd ~/app

# 将项目代码上传到服务器（以下任选一种方式）
```

### 2.2 上传代码到服务器（三种方式）

**方式 A：Git 克隆（推荐，后续更新方便）**

```bash
git clone https://github.com/你的用户名/你的仓库.git indonesian-learning
cd indonesian-learning
```

**方式 B：SCP 上传（适合没有 Git 仓库的情况）**

在本地电脑打开终端/PowerShell，执行：

```bash
# 将本地项目上传到服务器（替换 your-server-ip 为你的服务器 IP）
scp -r E:\developfile\VibeCoding\MyApp root@your-server-ip:~/app/indonesian-learning
```

**方式 C：直接压缩上传**

```bash
# 本地：先压缩项目
cd E:\developfile\VibeCoding\MyApp
tar -czvf indonesian-learning.tar.gz .

# 上传到服务器
scp indonesian-learning.tar.gz root@your-server-ip:~/app/

# 服务器上解压
ssh root@your-server-ip "cd ~/app && mkdir indonesian-learning && tar -xzvf indonesian-learning.tar.gz -C indonesian-learning"
```

---

## 三、配置环境变量

环境变量是项目的核心配置，包含数据库密码、邮箱密钥、JWT 密钥等敏感信息。

### 3.1 复制环境变量模板

```bash
cd ~/app/indonesian-learning
cp .env.example .env
```

### 3.2 编辑 .env 文件

```bash
vim .env
```

按 `i` 进入编辑模式，修改以下内容（**必须全部修改**）：

```bash
# MySQL 配置（数据库 root 密码，自行设置一个强密码）
MYSQL_ROOT_PASSWORD=YourStrongMysqlPassword123!
MYSQL_DB=indonesian_learning
MYSQL_USER=root

# JWT 密钥（极其重要，必须修改！）
# 生成方式：在终端执行 openssl rand -base64 32
# 或者自己编造一个至少 32 位的随机字符串
JWT_SECRET=your-very-strong-random-secret-key-at-least-32-chars

# 163 邮箱 SMTP 配置（用于发送注册验证码）
# MAIL_USERNAME：你的 163 邮箱地址
# MAIL_PASSWORD：不是你的邮箱登录密码，而是 163 邮箱的【授权码】
# 授权码获取方式：登录 163 邮箱 → 设置 → POP3/SMTP/IMAP → 开启 SMTP 服务 → 获取授权码
MAIL_USERNAME=your_email@163.com
MAIL_PASSWORD=your_163_auth_code

# 跨域配置（生产环境必填）
# 填写你的真实域名，如果有多个来源用逗号分隔
# 例如：ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
ALLOWED_ORIGINS=https://yourdomain.com
```

编辑完成后，按 `Esc`，输入 `:wq` 保存退出。

> **安全警告**：
> - `.env` 文件包含敏感信息，**绝对不要**提交到 Git 仓库
> - JWT_SECRET 必须足够长且随机，否则攻击者可以伪造用户身份
> - 163 邮箱授权码不是登录密码，不要泄露

---

## 四、构建前端项目

### 4.1 构建用户端前端

在你的**本地电脑**上执行：

```bash
# 进入用户端前端目录
cd E:/developfile/VibeCoding/MyApp/frontend

# 安装依赖（如果之前安装过可以跳过）
npm install

# 生产构建
npm run build
```

构建成功后，会生成 `frontend/dist` 目录，里面包含所有静态文件。

### 4.2 构建管理后台前端

```bash
# 进入管理后台目录
cd E:/developfile/VibeCoding/MyApp/frontend-admin

# 安装依赖
npm install

# 生产构建
npm run build
```

构建成功后，会生成 `frontend-admin/dist` 目录。

### 4.3 上传构建产物到服务器

```bash
# 在本地电脑的终端/PowerShell 中执行
# 上传用户端
scp -r E:\developfile\VibeCoding\MyApp\frontend\dist root@your-server-ip:~/app/indonesian-learning/frontend/

# 上传管理后台
scp -r E:\developfile\VibeCoding\MyApp\frontend-admin\dist root@your-server-ip:~/app/indonesian-learning/frontend-admin/
```

---

## 五、构建后端项目

### 5.1 在本地编译打包

```bash
cd E:/developfile/VibeCoding/MyApp/backend

# 使用 Maven 打包（跳过测试以加快速度，-DskipTests 可选）
mvn clean package -DskipTests
```

打包成功后，`backend/target/` 目录下会生成一个 `indonesian-learning-*.jar` 文件。

### 5.2 上传 JAR 包到服务器

```bash
# 在本地终端执行
scp E:\developfile\VibeCoding\MyApp\backend\target\*.jar root@your-server-ip:~/app/indonesian-learning/backend/target/
```

---

## 六、初始化数据库

### 6.1 创建数据库

项目启动时会自动通过 Docker 创建 MySQL 容器，但数据库表结构需要手动初始化。

```bash
# 先启动 MySQL 容器（单独启动，方便执行初始化脚本）
cd ~/app/indonesian-learning
docker compose up -d mysql

# 等待 30 秒，让 MySQL 完全启动
sleep 30

# 进入 MySQL 容器执行初始化
# 先查看容器是否运行
docker ps

# 进入容器（容器名应该是 indonesian_mysql）
docker exec -it indonesian_mysql mysql -uroot -p
```

输入你在 `.env` 中设置的 `MYSQL_ROOT_PASSWORD` 密码。

进入 MySQL 命令行后，执行：

```sql
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS indonesian_learning CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 退出
EXIT;
```

> **注意**：项目使用了 MyBatis-Plus，表结构通常会在应用启动时通过 `schema.sql` 或自动创建（如果配置了）。如果启动后报错表不存在，请联系开发团队获取 `schema.sql` 文件手动导入。

---

## 七、启动项目（Docker Compose）

### 7.1 停止之前单独启动的 MySQL（如果有）

```bash
cd ~/app/indonesian-learning
docker compose down
```

### 7.2 一键启动所有服务

```bash
cd ~/app/indonesian-learning

# 构建并启动所有服务（后台运行）
docker compose up --build -d
```

这条命令会：
1. 构建后端 Docker 镜像
2. 启动 MySQL 容器
3. 启动 Spring Boot 后端服务
4. 启动 Nginx 反向代理

### 7.3 查看启动状态

```bash
# 查看所有容器状态
docker compose ps

# 查看日志（实时）
docker compose logs -f

# 只看后端日志
docker compose logs -f backend

# 只看 Nginx 日志
docker compose logs -f nginx
```

如果看到以下输出，说明启动成功：

```
NAME                   STATUS          PORTS
indonesian_backend     Up 10 seconds   8080/tcp
indonesian_mysql       Up 10 seconds   3306/tcp
indonesian_nginx       Up 10 seconds   0.0.0.0:80->80/tcp
```

### 7.4 验证服务是否正常

```bash
# 测试后端 API 是否响应
curl http://localhost:8080/api/v1/auth/register

# 应该返回 JSON 错误（因为没传参数，但说明服务在运行）
# 例如：{"success":false,"error":"请求参数错误"}
```

---

## 八、配置域名和 HTTPS（强烈推荐）

没有 HTTPS，浏览器会标记你的网站为"不安全"，且部分功能（如语音合成）可能受限。

### 8.1 配置 Nginx 使用你的域名

编辑服务器上的 `nginx.conf`：

```bash
cd ~/app/indonesian-learning
vim nginx.conf
```

将 `server_name localhost;` 改为你的域名：

```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;  # 修改这里

    # ... 其余配置保持不变
}
```

保存后重启 Nginx：

```bash
docker compose restart nginx
```

### 8.2 使用 Let's Encrypt 申请免费 HTTPS 证书

```bash
# 安装 Certbot
sudo apt install -y certbot

# 申请证书（替换为你的域名）
sudo certbot certonly --standalone -d yourdomain.com -d www.yourdomain.com

# 按提示输入邮箱，同意协议
# 成功后证书会保存在 /etc/letsencrypt/live/yourdomain.com/
```

### 8.3 修改 Nginx 配置支持 HTTPS

编辑 `nginx.conf`：

```bash
vim ~/app/indonesian-learning/nginx.conf
```

替换为以下内容（注意修改域名和证书路径）：

```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    
    # 所有 HTTP 请求重定向到 HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;

    # SSL 证书配置
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    
    # SSL 优化配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 1d;

    # 前端静态资源（用户端）
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 管理后台
    location /admin {
        alias /usr/share/nginx/admin;
        index index.html;
        try_files $uri $uri/ /admin/index.html;
    }

    # API 反向代理到后端服务
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 8.4 挂载证书到 Nginx 容器

编辑 `docker-compose.yml`，在 nginx 服务的 volumes 中新增证书挂载：

```yaml
  nginx:
    image: nginx:alpine
    container_name: indonesian_nginx
    restart: always
    ports:
      - "80:80"
      - "443:443"  # 新增 HTTPS 端口
    volumes:
      - ./frontend/dist:/usr/share/nginx/html:ro
      - ./frontend-admin/dist:/usr/share/nginx/admin:ro
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
      - /etc/letsencrypt:/etc/letsencrypt:ro  # 新增证书挂载
    depends_on:
      - backend
```

### 8.5 重启服务生效

```bash
cd ~/app/indonesian-learning
docker compose down
docker compose up -d
```

---

## 九、开放服务器端口

### 9.1 配置防火墙

```bash
# 允许 SSH（必须，否则你无法登录服务器）
sudo ufw allow 22/tcp

# 允许 HTTP
sudo ufw allow 80/tcp

# 允许 HTTPS
sudo ufw allow 443/tcp

# 启用防火墙
sudo ufw enable

# 查看状态
sudo ufw status
```

### 9.2 云服务器安全组配置

如果你使用的是阿里云、腾讯云、AWS 等云服务器，还需要在**控制台 → 安全组**中开放以下端口：

| 端口 | 用途 | 允许来源 |
|------|------|---------|
| 22 | SSH 远程登录 | 你的 IP |
| 80 | HTTP 访问 | 0.0.0.0/0 |
| 443 | HTTPS 访问 | 0.0.0.0/0 |

> **不要**开放 3306（MySQL）和 8080（后端）到公网，这些只应在 Docker 网络内部通信。

---

## 十、验证部署

### 10.1 访问测试

在浏览器中打开：

- **用户端**：`https://yourdomain.com`
- **管理后台**：`https://yourdomain.com/admin`
- **API 文档**：`https://yourdomain.com/doc.html`（Knife4j 接口文档）

### 10.2 功能测试清单

逐项验证以下功能是否正常：

- [ ] 用户端首页可以正常打开
- [ ] 可以注册新账号（能收到验证码邮件）
- [ ] 可以登录
- [ ] 进入学习页面，闪卡可以正常翻转
- [ ] 点击单词可以播放发音
- [ ] 可以标记"认识"和"不认识"
- [ ] 复习页面可以正常加载
- [ ] 签到功能正常
- [ ] 设置页面可以切换语言和主题
- [ ] 管理后台 `/admin` 可以访问
- [ ] 管理后台可以导入词书
- [ ] 管理后台可以创建/编辑/删除单词

### 10.3 常见问题排查

**问题 1：页面打开空白，控制台报错 404**

原因：前端构建产物没有正确上传到服务器，或 Nginx 配置错误。

解决：
```bash
# 检查 dist 目录是否存在
ls -la ~/app/indonesian-learning/frontend/dist
ls -la ~/app/indonesian-learning/frontend-admin/dist

# 检查 Nginx 容器内能否访问
 docker exec indonesian_nginx ls /usr/share/nginx/html
 docker exec indonesian_nginx ls /usr/share/nginx/admin
```

**问题 2：API 请求报错 CORS 错误**

原因：`ALLOWED_ORIGINS` 环境变量没有配置正确。

解决：
```bash
# 检查 .env 文件中的 ALLOWED_ORIGINS
# 必须包含你访问时使用的域名（包括 https://）
# 修改后重启容器
cd ~/app/indonesian-learning
docker compose down
docker compose up -d
```

**问题 3：收不到验证码邮件**

原因：163 邮箱配置错误。

解决：
```bash
# 查看后端日志
docker compose logs -f backend

# 检查 .env 中的 MAIL_USERNAME 和 MAIL_PASSWORD
# 注意：MAIL_PASSWORD 必须是 163 的【授权码】，不是登录密码
```

**问题 4：MySQL 连接失败**

原因：MySQL 容器未启动或密码错误。

解决：
```bash
# 查看 MySQL 容器状态
docker compose ps

# 查看 MySQL 日志
docker compose logs mysql

# 确认 .env 中的密码和实际一致
```

**问题 5：证书过期**

Let's Encrypt 证书有效期为 90 天，需要设置自动续期：

```bash
# 测试续期
sudo certbot renew --dry-run

# 设置定时任务自动续期
sudo crontab -e

# 添加以下行（每天凌晨 3 点检查并续期）
0 3 * * * /usr/bin/certbot renew --quiet && cd ~/app/indonesian-learning && docker compose restart nginx
```

---

## 十一、日常维护

### 11.1 查看日志

```bash
cd ~/app/indonesian-learning

# 查看所有服务日志
docker compose logs -f

# 只看后端日志（最后 100 行）
docker compose logs --tail=100 backend

# 将日志导出到文件
docker compose logs backend > backend.log 2>&1
```

### 11.2 重启服务

```bash
cd ~/app/indonesian-learning

# 重启所有服务
docker compose restart

# 重启单个服务
docker compose restart backend
docker compose restart nginx
```

### 11.3 更新部署（发布新版本）

```bash
cd ~/app/indonesian-learning

# 1. 拉取最新代码（如果用 Git）
git pull origin main

# 2. 本地重新构建前端和后端
# （在本地电脑执行 npm run build 和 mvn package，然后上传到服务器）

# 3. 重启容器
docker compose down
docker compose up --build -d
```

### 11.4 备份数据库

```bash
# 导出数据库到文件
docker exec indonesian_mysql mysqldump -uroot -pYourPassword indonesian_learning > backup_$(date +%Y%m%d).sql

# 恢复数据库
docker exec -i indonesian_mysql mysql -uroot -pYourPassword indonesian_learning < backup_20260101.sql
```

### 11.5 清理 Docker 空间

```bash
# 删除无用的镜像和卷（谨慎执行）
docker system prune -a
docker volume prune
```

---

## 十二、目录结构说明

部署完成后，服务器上的目录结构如下：

```
~/app/indonesian-learning/
├── .env                          # 环境变量（敏感信息，不提交 Git）
├── .env.example                  # 环境变量模板
├── docker-compose.yml            # Docker Compose 配置
├── nginx.conf                    # Nginx 配置
├── README.md                     # 项目说明
├── DEPLOY.md                     # 本部署文档
├── backend/
│   ├── Dockerfile                # 后端 Docker 构建文件
│   ├── pom.xml                   # Maven 配置
│   ├── target/
│   │   └── *.jar                 # 编译后的 JAR 包
│   └── src/                      # 后端源码
├── frontend/
│   ├── dist/                     # 用户端构建产物（上传到服务器）
│   └── src/                      # 用户端源码（本地构建用）
└── frontend-admin/
    ├── dist/                     # 管理后台构建产物（上传到服务器）
    └── src/                      # 管理后台源码（本地构建用）
```

---

## 十三、联系与支持

如果在部署过程中遇到问题：

1. 先查看本文档的 **10.3 常见问题排查** 章节
2. 查看后端日志：`docker compose logs -f backend`
3. 查看 Nginx 日志：`docker compose logs -f nginx`
4. 确认 `.env` 文件中的所有配置都已正确填写

---

**部署完成！祝你使用愉快。**
