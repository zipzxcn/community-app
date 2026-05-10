# Community App

Community App 是一个内容社区全栈项目，当前仓库采用 **Vue 3 + TypeScript + Vite** 前端和 **Spring Boot 3 + MyBatis-Plus + MySQL + MinIO** 后端实现，覆盖内容发布、Markdown 写作、评论互动、用户关系、通知、私信聊天和浏览历史等社区核心流程。



## 功能特性

- 用户注册、登录、JWT 鉴权、当前用户信息获取。
- 首页帖子流、关键词搜索、标签筛选、最新/热门排序。
- 帖子详情、发布、编辑、隐藏、删除。
- Markdown 发帖工作台：
  - `markdown-it` 渲染；
  - H1-H6、段落、引用、代码块、表格、任务列表、分割线、链接、图片；
  - 左侧悬浮格式工具栏；
  - 正文图片上传后按光标位置插入；
  - 左右双栏、专注编辑、全屏预览模式。
- 草稿箱、草稿续写、草稿发布。
- 评论、回复、点赞、收藏。
- 用户资料、头像上传、个人中心、公开主页、用户搜索。
- 关注、粉丝、互相关注。
- 通知中心、未读统计。
- 私信聊天，前端采用 REST 历史消息 + WebSocket 推送。
- 浏览历史记录和定时清理。
- MinIO/S3 风格对象存储，后端代理公开文件访问。

## 技术栈

### 前端 `community-web`

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Arco Design Vue
- axios
- markdown-it
- markdown-it-task-lists
- Sass

### 后端 `community-backend`

- Java 17
- Spring Boot 3.3.5
- Spring Web MVC
- Spring Security
- WebSocket
- MyBatis-Plus
- MySQL 8
- Flyway
- MinIO Java SDK
- JJWT
- SpringDoc OpenAPI
- JUnit / Spring Boot Test

## 项目结构

```text
community-app
├── community-backend/                 # Spring Boot 后端
│   ├── src/main/java/com/community/backend
│   │   ├── controller/                # Auth/Post/Comment/File/Chat 等控制器
│   │   ├── service/                   # 业务服务
│   │   ├── mapper/                    # MyBatis-Plus Mapper
│   │   ├── entity/                    # 数据库实体
│   │   ├── dto/                       # 请求 DTO
│   │   ├── vo/                        # 响应 VO
│   │   └── config/                    # 安全、存储、WebSocket 等配置
│   └── src/main/resources
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/              # Flyway 数据库迁移脚本
├── community-web/                     # Vue 3 前端
│   ├── src/api/                       # API 请求封装
│   ├── src/components/                # 通用组件
│   ├── src/layouts/                   # DefaultLayout
│   ├── src/router/                    # 前端路由
│   ├── src/stores/                    # Pinia 状态
│   ├── src/styles/                    # 全局样式
│   ├── src/utils/                     # Markdown、格式化等工具
│   └── src/views/                     # 页面
└── docker/                            # Docker 一键部署文件
```

## 本地开发

### 环境要求

- Node.js 18+，建议 20+
- npm
- JDK 17
- Maven 3.9+ 或项目自带 Maven Wrapper
- MySQL 8+
- MinIO

### 启动前端

```powershell
cd D:\AppData\community-app\community-web
npm install
npm run dev
```

前端开发环境变量位于：

```text
community-web/.env.development
```

当前主要变量：

```env
VITE_APP_TITLE=Community Web
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_WS_BASE_URL=ws://localhost:8080/ws/chat
```

### 启动后端

先准备 MySQL 数据库和 MinIO，然后启动后端：

```powershell
cd D:\AppData\community-app\community-backend
mvn spring-boot:run
```

或使用 Maven Wrapper：

```powershell
cd D:\AppData\community-app\community-backend
.\mvnw.cmd spring-boot:run
```

后端默认激活 `dev` 环境，配置文件：

```text
community-backend/src/main/resources/application.yml
community-backend/src/main/resources/application-dev.yml
```

默认开发配置连接：

- MySQL：`127.0.0.1:3306/community_app`
- MinIO：`http://127.0.0.1:9000`
- MinIO 默认账号：`minioadmin / minioadmin`

## 数据库

项目使用 Flyway 管理数据库结构：

```text
community-backend/src/main/resources/db/migration/
├── V1__init.sql
├── V2__expand_file_object_access_url.sql
├── V3__normalize_file_access_url_to_backend_proxy.sql
└── V4__expand_post_draft_payload.sql
```

后端启动时会根据配置执行迁移。当前 `application-dev.yml` 和 Docker 部署均启用了：

```yaml
spring.flyway.enabled: true
spring.flyway.baseline-on-migrate: true
spring.flyway.baseline-version: 0
```

## 前端路由

主要页面：

| 路由 | 页面 |
| --- | --- |
| `/` | 首页帖子流 |
| `/posts/publish` | 发布帖子 |
| `/posts/:postId` | 帖子详情 |
| `/posts/:postId/edit` | 编辑帖子 |
| `/drafts` | 草稿箱 |
| `/histories` | 浏览历史 |
| `/users/search` | 用户搜索 |
| `/users/:userId` | 用户公开主页 |
| `/me` | 个人中心 |
| `/notifications` | 通知中心 |
| `/chat` | 私信聊天 |
| `/login` | 登录 |
| `/register` | 注册 |

## 后端模块

主要控制器位于：

```text
community-backend/src/main/java/com/community/backend/controller/
```

包含：

- `AuthController`：登录、注册、验证码、当前用户。
- `PostController`：帖子流、详情、发布、编辑、点赞、收藏、隐藏、删除。
- `DraftController`：草稿创建、更新、发布、删除。
- `CommentController`：评论和回复。
- `FileController`：上传凭证、上传完成回调、文件公开访问代理。
- `FollowController`：关注、取关、粉丝、关注列表、互相关注。
- `NotificationController`：通知列表、未读统计、已读。
- `ChatController`：会话、消息历史、已读。
- `HistoryController`：浏览历史记录、删除、清空。
- `UserController`：个人资料、公开主页、用户搜索。

OpenAPI/Swagger：

```text
http://localhost:8080/swagger-ui.html
```

## 常用验证命令

### 前端

```powershell
cd D:\AppData\community-app\community-web
npm run type-check
npm run build
```

### 后端

```powershell
cd D:\AppData\community-app\community-backend
mvn test
```

集成测试：

```powershell
cd D:\AppData\community-app\community-backend
mvn -P integration verify
```

## Docker 一键部署

Docker 部署文件位于：

```text
docker/
├── docker-compose.yml
├── backend.Dockerfile
├── frontend.Dockerfile
├── nginx.conf
├── minio-cors.json
└── .env.example
```

根目录还提供了 `.dockerignore`，用于减少 Docker 构建上下文体积，同时保留后端 Jar 和前端 dist 产物。

### 部署前准备

Docker 部署使用已经打包好的产物：

```text
后端 Jar：D:\AppData\community-app\community-backend\target\community-backend-0.0.1-SNAPSHOT.jar
前端 dist：D:\AppData\community-app\community-web\dist\
```

如果产物不存在，先执行：

```powershell
cd D:\AppData\community-app\community-backend
mvn -DskipTests package

cd D:\AppData\community-app\community-web
npm install
npm run build
```

### 启动

```powershell
cd D:\AppData\community-app\docker
Copy-Item .env.example .env
docker compose --env-file .env up -d --build
```

启动后访问：

| 服务 | 地址 |
| --- | --- |
| 前端入口 | `http://localhost:8088` |
| 后端 API | `http://localhost:8080/api/v1` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| MinIO API | `http://localhost:9000` |
| MinIO 控制台 | `http://localhost:9001` |
| MySQL | `localhost:3306` |

停止：

```powershell
cd D:\AppData\community-app\docker
docker compose --env-file .env down
```

停止并删除数据卷：

```powershell
cd D:\AppData\community-app\docker
docker compose --env-file .env down -v
```

### Docker 服务组成

`docker-compose.yml` 会启动：

- `mysql`：MySQL 8.4，保存业务数据。
- `minio`：对象存储，保存头像、封面、正文图片。
- `minio-init`：初始化 bucket 和 CORS。
- `backend`：Spring Boot 后端，使用已打包 Jar。
- `frontend`：Nginx 托管前端 dist，并反向代理 API 和 WebSocket。

### Docker 环境变量说明

环境变量示例文件：

```text
docker/.env.example
```

建议复制为 `.env` 后按部署环境修改。

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `TZ` | `Asia/Shanghai` | 容器时区 |
| `FRONTEND_PORT` | `8088` | 前端 Nginx 对外端口 |
| `BACKEND_PORT` | `8080` | 后端对外端口 |
| `MYSQL_PORT` | `3306` | MySQL 对外端口 |
| `MINIO_API_PORT` | `9000` | MinIO API 对外端口 |
| `MINIO_CONSOLE_PORT` | `9001` | MinIO 控制台端口 |
| `MYSQL_DATABASE` | `community_app` | 数据库名 |
| `MYSQL_ROOT_PASSWORD` | `community_root_password` | MySQL root 密码，生产必须修改 |
| `APP_JWT_SECRET` | `change-this-secret-change-this-secret` | JWT 密钥，生产必须修改为长随机值 |
| `APP_JWT_ACCESS_TOKEN_EXPIRE_SECONDS` | `7200` | Access Token 有效期 |
| `APP_JWT_REFRESH_TOKEN_EXPIRE_SECONDS` | `1209600` | Refresh Token 有效期 |
| `MINIO_ROOT_USER` | `community_minio` | MinIO 管理账号 |
| `MINIO_ROOT_PASSWORD` | `community_minio_password` | MinIO 管理密码，生产必须修改 |
| `MINIO_BUCKET` | `community-prod` | 默认文件桶 |
| `MINIO_PUBLIC_ENDPOINT` | `http://host.docker.internal:9000` | 浏览器可访问的 MinIO 公开端点 |
| `APP_STORAGE_UPLOAD_EXPIRE_SECONDS` | `900` | 预签名上传 URL 有效期 |
| `APP_HISTORY_CLEANUP_ENABLED` | `true` | 是否启用浏览历史清理任务 |
| `APP_HISTORY_CLEANUP_RETENTION_DAYS` | `180` | 浏览历史保留天数 |
| `APP_HISTORY_CLEANUP_CRON` | `0 15 3 * * *` | 清理任务 cron |

### 关于 `MINIO_PUBLIC_ENDPOINT`

正文图片上传采用“后端生成预签名 URL，前端直传 MinIO”的方式，因此 `MINIO_PUBLIC_ENDPOINT` 必须同时满足：

1. 后端容器可以通过它连接到 MinIO；
2. 浏览器可以通过它直接上传文件。

本机 Docker Desktop 默认使用：

```env
MINIO_PUBLIC_ENDPOINT=http://host.docker.internal:9000
```

云服务器部署时建议改成服务器公网 IP 或对象存储域名，例如：

```env
MINIO_PUBLIC_ENDPOINT=http://your-server-ip:9000
```

如果放到 HTTPS 域名或反向代理后面，也要同步修改该变量。

### 前端 Docker 代理规则

`docker/nginx.conf` 做了两类代理：

- `/api/v1/` -> `backend:8080/api/v1/`
- `/ws/chat` -> `backend:8080/ws/chat`

因此前端生产环境 `.env.production` 中使用相对路径即可：

```env
VITE_API_BASE_URL=/api/v1
VITE_WS_BASE_URL=/ws/chat
```

## 当前进度

已完成主要一期 MVP 能力：

- 认证登录；
- 发帖、草稿、详情、编辑；
- Markdown 编辑和预览；
- 图片/文件上传；
- 评论互动；
- 点赞、收藏；
- 关注关系；
- 通知中心；
- 私信聊天；
- 浏览历史；
- Docker Compose 一键部署基础文件。

后续可继续优化：

- 更接近 Typora 的所见即所得编辑体验；
- 推荐策略和用户画像；
- 后台管理；
- 部署脚本自动打包制品；
- CI/CD；
- 更完整的端到端测试。
