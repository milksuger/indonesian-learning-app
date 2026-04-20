# 印尼语学习 Web 应用 — 部署问题排查与优化指南

本文档记录了在 CentOS / Alibaba Cloud Linux 服务器上部署本项目时遇到的全部问题及解决方案，以及针对 2G 内存服务器的优化配置。

---

## 一、服务器环境确认

### 1.1 操作系统识别

本项目部署文档默认基于 Ubuntu/Debian 编写（使用 `apt` 包管理器），但阿里云等国内云服务商默认提供的是 **CentOS / Alibaba Cloud Linux / RHEL** 系统。

**识别方法：**
```bash
cat /etc/os-release
```

如果输出包含 `CentOS`、`Alibaba Cloud Linux` 或 `Red Hat`，则以下命令需要调整：

| 操作 | Ubuntu (apt) | CentOS/RHEL (yum) |
|------|-------------|-------------------|
| 更新系统 | `apt update && apt upgrade -y` | `yum update -y` |
| 安装工具 | `apt install -y curl wget git vim ufw` | `yum install -y curl wget git vim` |
| 防火墙 | `ufw` | `firewalld` 或 `iptables` |
| 安装 Docker Compose | `apt install -y docker-compose-plugin` | `yum install -y docker-compose-plugin` |

> **注意**：CentOS 默认没有 `ufw`，开放端口用 `firewalld`：
> ```bash
> firewall-cmd --permanent --add-port=80/tcp
> firewall-cmd --permanent --add-port=443/tcp
> firewall-cmd --permanent --add-port=22/tcp
> firewall-cmd --reload
> ```

---

## 二、Docker 与 Docker Compose

### 2.1 验证安装

```bash
docker --version
docker compose version
```

如果已安装则跳过，未安装执行：
```bash
# 安装 Docker
curl -fsSL https://get.docker.com | sh
systemctl start docker
systemctl enable docker

# 安装 Docker Compose
yum install -y docker-compose-plugin
```

---

## 三、项目文件准备

### 3.1 目录结构要求

确保以下构建产物已上传到服务器项目目录：

```
indonesian-learning-app/
├── backend/
│   └── target/*.jar          # 必须存在
├── frontend/
│   └── dist/                 # 必须存在
├── frontend-admin/
│   └── dist/                 # 必须存在
├── docker-compose.yml
├── nginx.conf
└── .env
```

### 3.2 常见问题：构建产物位置错误

如果 `backend/target/`、`frontend/dist/`、`frontend-admin/dist/` 不在 `indonesian-learning-app/` 目录内，而是放在上级目录，需要复制进去：

```bash
cd ~/indonesian-learning/indonesian-learning-app

# 从上级目录复制构建产物
cp -r ~/indonesian-learning/backend/target ./backend/
cp -r ~/indonesian-learning/frontend/dist ./frontend/
cp -r ~/indonesian-learning/frontend-admin/dist ./frontend-admin/
```

**验证方法：**
```bash
docker exec indonesian_nginx ls /usr/share/nginx/html/
docker exec indonesian_nginx ls /usr/share/nginx/admin/
```

如果输出 `No such file or directory`，说明 `dist` 目录没有正确挂载，需要检查复制步骤。

---

## 四、Docker Compose 配置问题

### 4.1 version 字段废弃警告

**现象：**
```
WARN[0000] docker-compose.yml: the attribute `version` is obsolete...
```

**原因：** Docker Compose v2 不再需要 `version` 字段。

**修复：**
```bash
cd ~/indonesian-learning/indonesian-learning-app
sed -i '/^version:/d' docker-compose.yml
```

### 4.2 后端镜像拉取失败（500 错误）

**现象：**
```
failed to solve: eclipse-temurin:17-jdk-alpine:
failed to copy: httpReadSeeker: failed open:
unexpected status code 500 Internal Server Error
```

**原因：** 阿里云 Docker 镜像源对 `eclipse-temurin` 官方镜像支持不稳定，返回 500 错误。

**修复：** 更换为国内源友好的基础镜像

```bash
# 方案一：使用 openjdk（推荐）
sed -i 's|eclipse-temurin:17-jdk-alpine|openjdk:17-jdk-alpine|' backend/Dockerfile

# 方案二：使用 Amazon Corretto（备选）
sed -i 's|eclipse-temurin:17-jdk-alpine|amazoncorretto:17-alpine-jdk|' backend/Dockerfile
```

修改后重新构建：
```bash
docker compose down
docker compose up --build -d
```

---

## 五、数据库问题

### 5.1 数据库连接认证失败

**现象：**
后端日志报错：
```
java.sql.SQLException: Public Key Retrieval is not allowed
```

访问 API 返回 500 错误。

**原因：** MySQL 8.0 默认使用 `caching_sha2_password` 认证插件，JDBC 驱动需要允许公钥检索。

**修复：** 在 `application.yml` 的 JDBC URL 中添加参数

```bash
cd ~/indonesian-learning/indonesian-learning-app
sed -i 's|useSSL=false|useSSL=false\&allowPublicKeyRetrieval=true|' backend/src/main/resources/application.yml
```

修改后的 URL 示例：
```yaml
url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DB:indonesian_learning}?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
```

然后重启后端：
```bash
docker compose restart backend
```

### 5.2 数据库未初始化（表不存在）

**现象：**
```sql
SHOW TABLES;
Empty set
```

**修复：** 手动导入表结构和初始数据

```bash
cd ~/indonesian-learning/indonesian-learning-app

# 导入表结构
docker exec -i indonesian_mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD:-root} indonesian_learning < backend/src/main/resources/schema.sql

# 导入初始数据（词书、单词）
docker exec -i indonesian_mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD:-root} indonesian_learning < backend/src/main/resources/data.sql
```

---

## 六、域名与 DNS 问题

### 6.1 DNS 解析错误

**现象：** `nslookup huakai.wang` 返回 GitHub Pages 的 IP（`185.199.xxx.xxx`），而不是服务器公网 IP。

**原因：** 之前配置过 GitHub Pages 的 CNAME 或 A 记录，需要删除。

**修复（阿里云 DNS 控制台）：**
1. 打开 https://dc.console.aliyun.com/
2. 找到域名，点击「解析」
3. **删除**以下记录：
   - 类型 `CNAME`，值指向 `milksuger.github.io` 的记录
   - 类型 `A`，值为 `185.199.xxx.xxx` 的 GitHub Pages IP
4. **添加/确认**只有这两条 A 记录：

| 主机记录 | 记录类型 | 记录值 |
|---------|---------|--------|
| @ | A | 你的服务器公网 IP |
| www | A | 你的服务器公网 IP |

### 6.2 Nginx server_name 配置

**现象：** 域名解析正确，但访问时 Nginx 不响应或返回默认页面。

**修复：**
```bash
cd ~/indonesian-learning/indonesian-learning-app
sed -i 's/server_name localhost;/server_name huakai.wang www.huakai.wang;/' nginx.conf
docker compose restart nginx
```

### 6.3 CORS 跨域配置

确保 `.env` 中的 `ALLOWED_ORIGINS` 包含你的实际访问域名：

```bash
ALLOWED_ORIGINS=https://huakai.wang,https://www.huakai.wang
```

修改 `.env` 后需要重启后端：
```bash
docker compose restart backend
```

---

## 七、内存优化（2G 服务器必做）

### 7.1 问题现象

- 内存使用率超过 85%
- 服务响应缓慢或间歇性 502/504 错误
- 容器被系统 OOM Killer 强制终止
- `docker stats` 显示 MySQL 占用 800MB+，Java 占用 500MB+

### 7.2 优化方案

#### 步骤 1：限制 MySQL 内存

创建优化配置文件：

```bash
mkdir -p ~/indonesian-learning/indonesian-learning-app/mysql/conf.d

cat > ~/indonesian-learning/indonesian-learning-app/mysql/conf.d/my.cnf << 'EOF'
[mysqld]
# InnoDB 缓冲池（主要内存占用），2G 服务器建议 128M
innodb_buffer_pool_size = 128M

# 键缓存
key_buffer_size = 16M

# 最大连接数限制
max_connections = 30

# 关闭查询缓存（MySQL 8.0 已废弃，但保险起见关闭）
query_cache_size = 0

# 临时表限制
tmp_table_size = 16M
max_heap_table_size = 16M

# 关闭性能模式（节省约 100MB）
performance_schema = OFF

# 跳过 DNS 解析（加快连接）
skip-host-cache
skip-name-resolve
EOF
```

在 `docker-compose.yml` 的 MySQL 服务中挂载配置：

```yaml
  mysql:
    image: mysql:8.0
    container_name: indonesian_mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root}
      MYSQL_DATABASE: ${MYSQL_DB:-indonesian_learning}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./mysql/conf.d:/etc/mysql/conf.d:ro  # 新增：挂载优化配置
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
```

#### 步骤 2：限制 JVM 内存

修改 `backend/Dockerfile`：

```dockerfile
FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

# 限制 JVM 最大堆内存为 256MB，初始 128MB
ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-jar", "app.jar"]
```

#### 步骤 3：增加 Swap 虚拟内存

```bash
# 创建 2GB swap 文件
fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile

# 开机自动挂载
echo '/swapfile none swap sw 0 0' >> /etc/fstab

# 验证
free -h
swapon --show
```

#### 步骤 4：可选 — 限制容器内存上限

在 `docker-compose.yml` 中为后端服务添加资源限制：

```yaml
  backend:
    build: ./backend
    container_name: indonesian_backend
    restart: always
    deploy:
      resources:
        limits:
          memory: 512M  # 限制后端容器最多使用 512MB
    # ... 其他配置不变
```

### 7.3 优化后的资源占用预估

| 组件 | 优化前 | 优化后 |
|------|--------|--------|
| MySQL 8 | ~800-1200 MB | ~200-256 MB |
| Java Spring Boot | ~500-1000 MB | ~256-350 MB |
| Nginx | ~10 MB | ~10 MB |
| 系统预留 | ~300 MB | ~300 MB |
| **总计** | **~1.6-2.5 GB** | **~766-916 MB** |

### 7.4 实际优化后运行数据（2G 服务器）

```
CONTAINER ID   NAME                 CPU %     MEM USAGE / LIMIT     MEM %
264082bb2262   indonesian_nginx     0.00%     1.879MiB / 1.827GiB   0.10%
318259e8f495   indonesian_backend   0.10%     148.7MiB / 1.827GiB   7.95%
771322f839b0   indonesian_mysql     28.03%    327.3MiB / 1.827GiB   17.49%
```

**容器总占用约 478MB**，系统及其他进程约 1GB，**2G 服务器可以正常运行**。

> ⚠️ **重要提醒**：当前 Swap 仍为 0B，强烈建议配置 2G Swap 作为缓冲，防止突发内存不足导致服务崩溃。

---

## 八、重启服务生效

完成所有修改后，执行以下命令重启：

```bash
cd ~/indonesian-learning/indonesian-learning-app

# 停止所有服务
docker compose down

# 重新构建并启动
docker compose up --build -d

# 查看状态
docker compose ps
docker stats --no-stream

# 查看后端日志（确认正常启动）
docker compose logs --tail=50 backend
```

---

## 九、验证清单

部署完成后，逐项验证：

- [ ] `docker compose ps` 显示 3 个容器都 Up
- [ ] `curl http://localhost/api/v1/admin/stats` 返回 JSON 数据
- [ ] 浏览器访问 `http://你的域名` 显示用户端首页
- [ ] 浏览器访问 `http://你的域名/admin` 显示管理后台
- [ ] 管理后台可以正常登录和操作
- [ ] 注册时可以收到验证码邮件
- [ ] `free -h` 显示内存使用率低于 80%

---

## 十、常用运维命令速查

```bash
# 查看容器状态
docker compose ps

# 查看实时日志
docker compose logs -f backend
docker compose logs -f nginx
docker compose logs -f mysql

# 重启单个服务
docker compose restart backend
docker compose restart nginx

# 进入 MySQL 容器
docker exec -it indonesian_mysql mysql -uroot -p

# 进入后端容器
docker exec -it indonesian_backend sh

# 查看容器资源占用
docker stats --no-stream

# 备份数据库
docker exec indonesian_mysql mysqldump -uroot -pYourPassword indonesian_learning > backup_$(date +%Y%m%d).sql

# 查看系统内存
free -h
```

---

如有其他问题，请参考 `DEPLOY.md`（详细部署步骤）和 `OPS.md`（日常运维与数据更新）。
