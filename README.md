# wait

交友系统。当前包含两个子项目：

- `server/` — Java 17 + Spring Boot 3 的后端服务
- `web/` — Vite + React + TypeScript 的 Web 前端
- `db/` — 数据库初始化脚本

## 技术栈

| 层 | 选型 |
| --- | --- |
| 前端 | React 18 + TypeScript + Vite + React Router + TanStack Query + TailwindCSS |
| 后端 | Spring Boot 3 + Java 17 + MyBatis-Plus + Spring Security |
| 数据库 | MySQL 8（127.0.0.1:3306） |
| 鉴权 | Casdoor（http://127.0.0.1:18000），前端走 OIDC 授权码流程 |

## 快速开始

### 1. 初始化数据库

```bash
mysql -uroot -p < db/init.sql
mysql -uroot -p wait < db/migration/V1__profile_and_match.sql
mysql -uroot -p wait < db/migration/V2__conversation.sql
```

### 2. 配置本地凭据

后端：

```bash
cp server/src/main/resources/application-local.yml.example server/src/main/resources/application-local.yml
# 编辑 application-local.yml 填入 MySQL 密码、Casdoor client_id/secret
```

前端：

```bash
cp web/.env.example web/.env
# 编辑 .env 填入 Casdoor client_id/secret
```

### 3. 启动后端

```bash
cd server
mvn spring-boot:run -Dspring.profiles.active=local
```

后端默认端口 `13001`，context-path `/api`。

### 4. 启动前端

```bash
cd web
npm install
npm run dev
```

前端默认端口 `5173`，会自动 proxy `/api` 和 `/ws` 到后端。

## Casdoor 配置

Casdoor 部署在 `http://127.0.0.1:18000`，需要在其后台创建一个 Application：

- **Name / Organization**: `wait`
- **Redirect URLs**: `http://localhost:5173/auth/callback`
- **Token format**: `JWT-Standard`
- **Token signing algorithm**: `RS256`

`client_id` / `client_secret` 在 Application 详情页获取，填入：

- 前端：`web/.env`（VITE_CASDOOR_*）
- 后端：`server/src/main/resources/application-local.yml`（casdoor.*）
