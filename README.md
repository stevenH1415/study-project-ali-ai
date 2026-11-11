# mm 学习项目 · Spring Cloud Alibaba + Spring AI

一个从 0 搭建的 Java 微服务学习项目，模块统一使用 `mm-` 前缀：

| 模块 | 说明 | 端口 |
| --- | --- | --- |
| `mm-gateway` | API 网关：统一入口 / 路由 / CORS | 18081 |
| `mm-aichatboot` | Spring AI 聊天机器人（普通对话 + SSE 流式） | 18082 |
| `mm-backend` | 业务后端：登录认证（PostgreSQL + BCrypt） | 18083 |
| `mm-front` | FEBS 风格前端（Vue3 + Vite + Ant Design Vue） | 18084 |

## 已实现功能

- **登录**：`admin / 123456`，用户存 PostgreSQL，密码 BCrypt
- **AI 聊天**：`POST /api/chat` 一次性返回；`GET /api/chat/stream` SSE 流式
- **会话记忆**：内存保存最近 N 条消息，可清空
- **前端**：启动 → 登录页 → 主框架（侧边菜单 / 顶栏 / 面包屑）→ 首页 → AI 聊天页
- **数据库**：`springali` 库，`sys_user` / `chat_conversation` / `chat_message`
- **可选基础设施**：Nacos、RocketMQ（docker-compose 已准备，默认关闭）

## 技术栈版本

- JDK 17
- Spring Boot 3.5.16
- Spring Cloud 2025.0.3
- Spring Cloud Alibaba 2025.0.0.0
- Spring AI 1.1.8
- Vue 3 + Vite 6 + Ant Design Vue 4
- PostgreSQL 10+（本机：`D:\soft\PostgreSQL\10`）

## 目录结构

```text
E:\学习\SpringAli
├── pom.xml                  # Maven 父工程（mm-parent）
├── mm-gateway               # 18081 统一入口 / 路由 / CORS
├── mm-aichatboot            # 18082 Spring AI 聊天（SSE）
├── mm-backend               # 18083 业务后端 / 登录认证（PG + BCrypt）
├── mm-front                 # 18084 FEBS 风格前端（Vue3 + Vite + AntDV）
│   └── src
│       ├── views/Login.vue  # 登录页
│       ├── views/Home.vue   # 首页
│       ├── views/Chat.vue   # AI 聊天页
│       └── layout/BasicLayout.vue
├── sql                      # PostgreSQL 脚本
│   └── 01-schema.sql
├── prompt                   # 提示词记录（按用户要求）
│   └── PROMPTS.md
├── docker                   # Nacos / RocketMQ 配置
├── docker-compose.yml       # 可选基础设施
├── scripts                  # 启动脚本
└── README.md
```

## 快速开始

### 1. 初始化 PostgreSQL

本机 PostgreSQL：`localhost:5432`，账号 `postgres`，密码 `826118`。

```powershell
$env:PGPASSWORD='826118'
$psql='D:\soft\PostgreSQL\10\bin\psql.exe'

# 创建数据库（已创建可跳过）
& $psql -h localhost -p 5432 -U postgres -d postgres -c "CREATE DATABASE springali WITH ENCODING 'UTF8' TEMPLATE template0"

# 建表
& $psql -h localhost -p 5432 -U postgres -d springali -f E:\学习\SpringAli\sql\01-schema.sql
```

> `mm-backend` 首次启动会自动插入 `admin / 123456`（BCrypt 加密）。

### 2. 配置 AI Key

`mm-aichatboot` 默认使用 DeepSeek 官方地址，需要设置环境变量后再启动：

```powershell
$env:AI_BASE_URL="https://api.deepseek.com"
$env:AI_MODEL="deepseek-chat"
$env:AI_API_KEY="sk-你的key"
```

其他 OpenAI 兼容服务（`base-url` 填到 `/v1` 之前）：

| 服务 | AI_BASE_URL | AI_MODEL |
| --- | --- | --- |
| DeepSeek 官方 | `https://api.deepseek.com` | `deepseek-chat` |
| OpenCode Go | `https://opencode.ai/zen/go` | `deepseek-v4-pro` |
| 本地 Ollama | `http://localhost:11434` | `qwen2.5:7b` |
| 其他网关 | 例如 `https://token.bayesdl.com/api/maas` | 按网关文档 |

> 不配置 Key 也能启动，只是聊天接口会返回 502 / SSE `[ERROR]`。

### 3. 启动后端

项目自带便携版 Maven：`.tools\apache-maven-3.9.9`（也可以用 IDEA 自带的 Maven）。

```powershell
cd E:\学习\SpringAli
.\.tools\apache-maven-3.9.9\bin\mvn.cmd -DskipTests package
```

分别开 3 个终端启动：

```powershell
java -jar E:\学习\SpringAli\mm-backend\target\mm-backend-1.0.0.jar
java -jar E:\学习\SpringAli\mm-aichatboot\target\mm-aichatboot-1.0.0.jar
java -jar E:\学习\SpringAli\mm-gateway\target\mm-gateway-1.0.0.jar
```

或直接运行 `scripts\start-backend.ps1`（会分别弹出窗口）。

### 4. 启动前端

```powershell
cd E:\学习\SpringAli\mm-front
npm install
npm run dev
```

浏览器打开 <http://localhost:18084>，登录账号：**admin / 123456**。

## 接口一览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录，返回 token + 用户信息 |
| GET | `/api/auth/info` | 当前登录用户（Header: `Authorization: Bearer xxx`） |
| POST | `/api/auth/logout` | 退出登录 |
| POST | `/api/chat` | 普通对话 `{conversationId?, message}` |
| GET | `/api/chat/stream?message=...&conversationId=...` | SSE 流式对话 |
| DELETE | `/api/chat/{conversationId}` | 清空会话记忆 |

## 网关路由

`mm-gateway` 将请求转发到：

- `/api/auth/**` → `http://localhost:18083`（mm-backend）
- `/api/chat/**` → `http://localhost:18082`（mm-aichatboot）

## Nacos（可选）

默认关闭（`spring.cloud.nacos.discovery.enabled: false`），不启动 Nacos 也能跑。

```powershell
docker compose up -d nacos
```

然后把三个服务 `application.yml` 中的 `spring.cloud.nacos.discovery.enabled` 改为 `true`，
并把 `spring.cloud.service-registry.auto-registration.enabled` 改为 `true`。
Nacos 控制台：<http://localhost:8848/nacos>（默认 nacos / nacos）。

## MQ 学习（下一步）

RocketMQ 的 docker-compose 已准备好，先把框架和聊天机器人跑通，再开始 MQ：

```powershell
docker compose up -d rmqnamesrv rmqbroker rmqdashboard
```

- NameServer：`localhost:9876`
- Dashboard：<http://localhost:8180>

后续可以新建 `mm-mq` 模块，学习：

1. RocketMQ 基本概念（Topic / Tag / Producer / Consumer / 消费组）
2. 同步、异步、单向发送
3. 集群消费 / 广播消费、重试与死信队列
4. 与 Spring Cloud Stream / Spring Cloud Alibaba RocketMQ Binder 集成

## 关于 FEBS

FEBS 官方前端是 Vue 2 + Ant Design Vue 1.x 的老技术栈，在 Node 24 下安装运行容易踩坑，
且与 Spring AI / Boot 3.5 生态不匹配。因此 `mm-front` 按 **FEBS 的视觉与布局规范**，
用 Vue 3 + Ant Design Vue 4 复刻了「登录 → 主框架 → 首页」流程。
如果你确实需要原版 FEBS-Vue，可以再单独拉取，但建议用它学习页面结构即可。

## 提示词记录

按你的要求，所有提示词都记录在：`prompt/PROMPTS.md`。
