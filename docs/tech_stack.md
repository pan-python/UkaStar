# 技术选型与非功能性要求

本文档汇总“优卡星”系统从 Python/Flask 迁移至 Java/Spring Boot + Vue3 + uni-app x + OCR 服务升级后的技术栈与关键非功能性要求。内容来自业务目标与现有系统痛点，作为后续设计与实施的约束项。

## 总体架构

- **后端服务**：Java 21、Spring Boot 3.3.x（WebFlux 模式），MyBatis-Plus 作为 ORM，Gradle 构建（Kotlin DSL）。
- **前端管理端**：Vue 3、TypeScript、Vite、Pinia、Vue Router、Element Plus，自定义主题支持暗黑模式。
- **小程序端**：uni-app x（目标微信小程序），UI 组件优先使用 uView/uvui 生态。
- **OCR 服务**：Python 3.11 + FastAPI（可选 Uvicorn/Gunicorn 部署），集成 EasyOCR 推理。
- **存储**：MySQL 8（字符集 utf8mb4_0900_ai_ci），MinIO 作为对象存储预留（默认全私有，预签名 URL 下载）。
- **AI 能力**：DeepSeek（兼容 DashScope），通过 WebSocket 流式推送消息。
- **部署**：Docker Compose + Traefik（自动证书与 HTTPS/WSS 反向代理）。

## 横切关注点

### 多租户与数据安全
- 所有业务表均包含 `tenant_id`，平台超管可跨租户操作，其余账号受租户约束。
- 软删除字段 `is_deleted`，审计字段 `created_by/created_at/updated_by/updated_at` 全表覆盖。
- 数据权限：接口需区分 TENANT/SELF/GROUP 范围，结合 RBAC 菜单/按钮/接口权限。

### 鉴权与安全
- JWT：Access Token 有效期 15 分钟，Refresh Token 有效期 7 天。
- 支持令牌版本号/白名单机制实现“踢人”。
- 预留 Sentinel 限流位点（默认关闭），上传仅允许白名单文件类型。
- WebSocket 连接与 OCR 服务均需服务间鉴权 Token。

### 性能与稳定性
- Controller 层单元测试覆盖率 100%，关键路径提供 WebTestClient/Jacoco 保障。
- AI/OCR 服务需具备详尽日志与错误回退策略，支持失败重试与熔断预留。
- Docker 化部署提供健康检查与一键启停脚本。

### 日志、审计与合规
- 统一 TraceId 贯穿后端日志链路。
- 覆盖登录、权限变更、积分操作、导出等审计事件，支持报表导出。
- 移动端对敏感字段脱敏显示，后台不脱敏但需严格权限控制。

### 测试与质量
- 集成 Jacoco，保障 Controller 覆盖率 100%。
- 构建自动化测试用例覆盖鉴权、租户隔离、积分边界、导出、聊天、WebSocket、OCR。
- 迁移阶段提供对账脚本，确保数据口径一致。

## 交付物
- 本文档即“技术选型与非功能性要求”确认记录，后续如需变更需更新并通知团队。
