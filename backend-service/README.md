# backend-service 模块

Spring Boot WebFlux + MyBatis-Plus 升级版骨架，提供分层结构与系统信息示例接口。

## 快速开始

```bash
cd backend-service
mvn spring-boot:run
```

服务默认监听 `http://localhost:8080`，测试接口：

- `POST /api/auth/login` 以租户 + 账号 + 密码获取 Access/Refresh Token，Access Token 15 分钟、Refresh Token 7 天。
- `POST /api/auth/refresh` 使用 Refresh Token 换取新令牌。
- `POST /api/auth/logout` 使用 Refresh Token 使当前账号所有令牌失效（踢下线）。
- `GET /api/system/info` 需携带 `Authorization: Bearer <accessToken>`，返回脚手架系统信息，并自动携带 TraceId。
- `POST /api/system/echo` 校验请求体 `message` 非空并回显，同样需带 Access Token。

内置平台超管账号（仅用于开发验证）：

| 租户 ID | 用户名           | 密码       | 角色                      |
|---------|------------------|------------|---------------------------|
| 1       | `platform-admin` | `Admin@123` | `ROLE_PLATFORM_ADMIN` |

## 目录结构

- `src/main/java/com/ukastar`：Spring Boot 启动类。
- `src/main/java/com/ukastar/api`：对外暴露的 WebFlux Controller 层。
- `src/main/java/com/ukastar/service`：业务服务层，承载业务编排逻辑。
- `src/main/java/com/ukastar/repo`：仓储层，封装数据访问能力。
- `src/main/java/com/ukastar/domain`：领域模型与值对象。
- `src/main/java/com/ukastar/infra`：基础设施实现（如内存/数据库等具体实现）。
- `src/main/java/com/ukastar/security`：安全相关抽象（当前提供默认系统用户实现）。
- `src/main/java/com/ukastar/common`：通用工具与返回体定义。
- `src/main/resources/application.yml`：基础配置，占位端口。
