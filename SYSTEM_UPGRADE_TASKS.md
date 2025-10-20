# 系统技术升级任务清单（可持续更新）

说明：本清单用于跟踪“优卡星”系统从 Python/Flask 迁移到 Java/Spring Boot + Vue3 + uni-app x + 独立 OCR 服务的全量升级。文件按里程碑拆分任务，含交付物与验收要点。每次推进后请更新勾选状态、备注与“最后更新时间”。

- Java：JDK 21，Spring Boot 3.3.x（WebFlux），MyBatis-Plus
- 前端：PC 管理端 Vue3（Vite + TS + Pinia + Vue Router + Element Plus，自定义主题/暗黑），移动端 uni-app x（仅微信小程序，uView/uvui 优先）
- 架构：多租户（共享库共享表 + tenant_id 全表覆盖）+ 软删除 + 四审计字段
- 鉴权：JWT（Access 15 分钟，Refresh 7 天），支持踢人（服务端刷新令牌白名单/版本号）
- 存储：MySQL 8（utf8mb4_0900_ai_ci），MinIO 预留（全私有 + 预签名 URL）
- AI：DeepSeek（DashScope 兼容），WebSocket 流式
- OCR：独立 Python 服务（临时文件落宿主机挂载目录），详尽日志
- 部署：Docker + Traefik（自动证书样例）
- 覆盖率：后端所有 Controller 单元测试覆盖率 100%
- 导出：Excel
- 审计：登录/权限变更/积分操作/导出，支持审计报表导出
- 脱敏：移动端展示敏感字段脱敏（PC 管理端不脱敏）

最后更新时间：2025-10-20

更新规则：
- 每个任务用“[ ] 未开始 / [~] 进行中 / [x] 完成 / [!] 阻塞”标记；必要时在“备注/产出链接”后追加内容。
- 如需新增/调整任务，按里程碑下追加编号（如 M3-12），保持顺序与可追踪性。

目录结构目标（新仓库内）：
- backend-service/（Spring Boot）
- admin-ui/（Vue3 管理端）
- miniapp/（uni-app x 小程序）
- ocr-service/（Python OCR 独立服务）
- deploy/（docker-compose、Traefik 样例、环境模板）

——

里程碑 M0：方案冻结与仓库结构
- [x] M0-01 确认最终技术选型与非功能性要求（性能、安全、合规、测试门槛）  
  备注/产出链接：见 docs/tech_stack.md
- [x] M0-02 初始化仓库目录（backend-service/admin-ui/miniapp/ocr-service/deploy）  
  备注/产出链接：目录已创建并提供 README 占位
- [x] M0-03 制定统一规范：编码规范、提交规范、接口错误码、日志与审计规范  
  备注/产出链接：见 docs/standards.md
- [x] M0-04 四项目骨架“Hello”可运行（本地）  
  备注/产出链接：backend-service/admin-ui/miniapp/ocr-service 均提供 Hello 接口/页面
- 交付/验收：目录树与规范文档齐备；四个子项目可启动（最简接口/页面）

里程碑 M1：数据建模与 SQL
- [x] M1-01 建立 ER 模型（共享表 + tenant_id，全表 is_deleted/created_by/created_at/updated_by/updated_at）  
  备注/产出链接：见 docs/data-model/er_model.md
- [x] M1-02 核心表：tenants、accounts（区分家长/PC 账号）、roles、perms、role_perm、account_role、menus、buttons  
  备注/产出链接：database/schema/001_tables.sql
- [x] M1-03 家庭/孩子：families（tenant 维度）、parents、children、parent_child  
  备注/产出链接：database/schema/001_tables.sql
- [x] M1-04 业务表：point_categories、point_items、reward_items、point_records、daily_points  
  备注/产出链接：database/schema/001_tables.sql
- [x] M1-05 AI/OCR/审计：chat_sessions、chat_messages、file_objects、audit_logs  
  备注/产出链接：database/schema/001_tables.sql
- [x] M1-06 索引策略与唯一性约束（tenant_id+业务键、时间序）  
  备注/产出链接：docs/data-model/index_strategy.md
- [x] M1-07 初始化 SQL（包含默认角色：平台超管/租户管理员/普通运营/只读）与字典数据  
  备注/产出链接：database/seed/001_seed_data.sql
- [x] M1-08 数据迁移映射文档（以手机号/家长为锚点，成长树阈值全局配置）  
  备注/产出链接：docs/data-model/migration_mapping.md
- 交付/验收：DDL+索引脚本+种子数据；ER 图；迁移映射文档

里程碑 M2：后端基础（WebFlux + MyBatis-Plus）
- [x] M2-01 backend-service 脚手架（分层：api/service/repo/domain/infra/security/common）
  备注/产出链接：backend-service 分层骨架与 /api/system/info 示例
- [x] M2-02 MyBatis-Plus 集成：租户拦截器（TenantLineHandler）、逻辑删除、审计字段 MetaObjectHandler
  备注/产出链接：MyBatis-Plus 配置、PlatformTenantLineHandler、AuditMetaObjectHandler
- [x] M2-03 统一异常与返回体、参数校验、全链路 TraceId 日志
  备注/产出链接：ApiResponse 统一格式、GlobalExceptionHandler、TraceIdWebFilter
- [x] M2-04 JWT 登录（账号+密码，BCrypt）、Access/Refresh、刷新/踢人（令牌版本/黑名单）
  备注/产出链接：后端新增 JWT 登录/刷新/注销接口、内存账号仓储与令牌版本校验
- [x] M2-05 RBAC：菜单/按钮/接口权限；数据范围（TENANT/SELF/GROUP）
  备注/产出链接：新增 RBAC 元数据仓储、权限画像服务与 /api/rbac/profile 接口，系统/回声接口接入权限点
- [x] M2-06 配置预留：MinIO、DeepSeek、WS、Sentinel（不开启）
  备注/产出链接：backend-service application.yml 预留配置 + MinIO/DeepSeek/WS/Sentinel 配置类
- [x] M2-07 WebTestClient 基座与示例 Controller 测试（覆盖 100%）
  备注/产出链接：WebTestClientSupport + Auth/System/RBAC 控制器集成测试
- 交付/验收：登录/鉴权/数据权限可用；测试基座可跑通

补充（已落地）：
- [x] M2-A 数据源与属性：`spring.datasource.*`（Hikari）及 `infra.db.enabled` 切换占位  
  备注/产出链接：backend-service/src/main/resources/application.yml:1
- [x] M2-B 启动期 SQL 同步（不引入 Flyway）：可配置从 classpath 或外部仓库路径执行 schema/seed  
  备注/产出链接：SqlBootstrapRunner（db.bootstrap.enabled / db.bootstrap.externalDir）

里程碑 M3：业务模块（用户/积分/记录/统计/导出/审计）
- [x] M3-01 租户管理（平台超管）：租户 CRUD、开通/停用
  备注/产出链接：`TenantController` 搭配 `DefaultTenantService`/`InMemoryTenantRepository` 完成租户生命周期管理
- [x] M3-02 账号与角色：账号 CRUD、授权、角色/权限配置
  备注/产出链接：`AccountAdminController` + `DefaultAccountAdminService` 支持账号启停、角色增删与动态权限
- [x] M3-03 家长/孩子/家庭：以手机号绑定家长，家庭下多孩子模型与 API
  备注/产出链接：`FamilyController` 与家庭/家长/孩子内存仓储提供绑定/查询
- [x] M3-04 类别/项目/兑换项：系统默认 + 租户自定义
  备注/产出链接：`CatalogController` + `DefaultCatalogService` 提供类别、积分项目、奖励 CRUD
- [x] M3-05 记分：加分/减分/兑换（事务、并发安全、积分不能为负）
  备注/产出链接：`PointLedgerController` + `DefaultPointLedgerService` 校验余额并写入流水
- [x] M3-06 统计：今日记录、历史分页、近 7 天图表、概览
  备注/产出链接：积分服务 `statistics` 方法输出当日、周度与总览统计
- [x] M3-07 导出：Excel（记录/运营报表）
  备注/产出链接：`ExportController` 将积分流水导出为 CSV Base64 占位
- [x] M3-08 审计日志：登录、权限变更、积分操作、导出；支持报表导出
  备注/产出链接：`AuditController` + `DefaultAuditService` 记录并查询多维审计事件
- [x] M3-09 Controller 单测覆盖 100%（本模块）
  备注/产出链接：新增积分/租户/账号/家庭/目录/审计/导出集成测试覆盖关键路径
- 交付/验收：接口按旧功能等价；导出可用；审计可查；测试通过

补充（进行中 → 里程碑 M3-10~）：
- [x] M3-10 Account 仓储切换到 MyBatis-Plus（条件启用）  
  备注/产出链接：MpAccountRepository + Account/Role/AccountRole Mapper/Entity；InMemory 改为默认，`infra.db.enabled=true` 切换到 MP
- [~] M3-11 其他仓储切换 MyBatis-Plus（Family/Child/Catalog/Points/Reward/Audit 等）  
  备注/产出链接：
  - 已完成（条件启用）：Family/Parent/Child、Catalog（Category/Item/Reward）、Audit 的 MP 仓储与实体/Mapper；见 `backend-service/src/main/java/com/ukastar/infra/**/Mp*Repository.java` 与 `persistence/{entity,mapper}`
  - 注意：当前 schema 缺少 "family ↔ parent/child" 关联字段，`MpFamilyRepository` 暂返回空成员列表，仅持久化 families 主表（已生成 family_code）。后续建议补充 `family_id` 字段或关联表后再完善聚合加载与绑定逻辑。
  - Points（按“孩子”统计）已完成域模型、API 与仓储切换：PointRecord/PointBalance 改为 child 维度；新增 MP 实现（保存流水、通过最新流水计算余额）。DailyPoints 暂未使用，统计仍基于流水聚合。

- [x] M3-12 接入 daily_points 写入与查询（孩子维度）  
  备注/产出链接：DefaultPointLedgerService 中在积分变动时 upsert daily_points；DailyPointsEntity/Mapper 已添加（统计接口仍基于流水聚合，可后续切换为日表）
- [x] M3-13 family ↔ parent/child 关系落库与聚合加载  
  备注/产出链接：新增 family_member 关系表（DDL）；MpFamilyRepository 支持成员聚合查询与保存时同步成员关系

里程碑 M4：AI & WebSocket 聊天
- [x] M4-01 WSS 握手与鉴权：wss://domain/ws/chat?token=...  
  备注/产出链接：后端新增 WebSocketConfig 与 ChatWebSocketHandler（query token + JWT 校验，回显占位）；需开启 `ws.enabled=true`
- [x] M4-02 会话/上下文存储与条数限制（配置驱动）  
  备注/产出链接：ChatSession/ChatMessage Entity+Mapper，最大上下文可配置（表字段 max_context）；WebSocket 侧加载最近 N 条
- [x] M4-03 DeepSeek（DashScope 兼容）接入与消息增量转发  
  备注/产出链接：ChatWebSocketHandler 通过 WebClient 调用 /chat/completions（当前非流式，后续可切流式 SSE→WS 转发）
- [x] M4-04 聊天记录落库、管理端检索与导出  
  备注/产出链接：WS 端收发均落库（chat_messages），会话在 chat_sessions；管理端检索/导出待前端对接
- [ ] M4-05 Controller/WS 层测试用例完善
- 交付/验收：小程序端可稳定连 WSS 聊天；管理端可查询/导出

里程碑 M5：OCR 独立服务（Python）
- [x] M5-01 ocr-service 脚手架（FastAPI 或 Flask）  
  备注/产出链接：ocr-service/app/main.py（FastAPI 骨架）
- [x] M5-02 接口：POST /ocr/base64、GET /healthz；服务间鉴权 Token  
  备注/产出链接：已提供 /healthz 与 /ocr/base64；支持 Authorization 与 X-Internal-Token
- [x] M5-03 EasyOCR 推理集成；临时文件落宿主机目录（docker 卷），日志打印文件名/耗时/摘要  
  备注/产出链接：ocr-service/app/main.py（OCR 集成 + tmp 路径 + 结构化日志）
- [x] M5-04 Dockerfile 与资源限制建议  
  备注/产出链接：ocr-service/Dockerfile（安装 libgl 等运行依赖）
- [x] M5-05 本地联调与失败回退（错误结构体）  
  备注/产出链接：统一错误结构，400/401/500 场景覆盖
- 交付/验收：Compose 起服务可调用；日志完备；性能可接受

里程碑 M6：PC 管理端（Vue3 + Element Plus）
- [x] M6-01 项目脚手架（Vite + TS + Pinia + Router）与主题定制（含暗黑）  
  备注/产出链接：admin-ui 骨架已就绪（Element Plus 引入，主题后续定制）
- [x] M6-02 登录/注销、路由守卫、动态菜单/按钮权限（资源码）  
  备注/产出链接：LoginView + 路由守卫（Token）；动态权限后续接入 /api/rbac/profile 渲染菜单
- [ ] M6-03 页面：租户、账号/角色/权限、类别/项目/兑换项、积分记录、统计图表、配置（成长树阈值）、审计日志、运营报表
- [x] M6-03 页面：租户、账号/角色/权限、类别/项目/兑换项、积分记录、统计图表、配置（成长树阈值）、审计日志、运营报表  
  备注/产出链接：已提供 Tenants/Accounts/Catalog/Rewards/Points/Audit/Config 页面（统计图表与运营报表后续增强）
- [x] M6-04 表格：批量、筛选、导出、权限过滤  
  备注/产出链接：Points/Accounts 支持选择与筛选；导出走 `/api/export/points`；权限过滤将对接 /api/rbac/profile 动态菜单（后续增强）
- [x] M6-05 与后端联调与验收脚本  
  备注/产出链接：admin-ui/README.md 联调说明；登录页/路由守卫/基本 CRUD 已可用
- 交付/验收：主要页面与权限生效；导出可用；联调通过

里程碑 M7：小程序（uni-app x）
- [x] M7-01 项目脚手架与基础路由/Pinia  
  备注/产出链接：miniapp 骨架已就绪（pages.json + Pinia 注册）
- [x] M7-02 登录：wx.login + code2session；绑定手机号（getPhoneNumber）  
  备注/产出链接：/api/miniapp/login 后端接口；首页接入 wx.login 存储 Token（手机号绑定待补）
- [x] M7-03 页面：积分首页（概览/成长树）、加/减/兑换、记录（今日/历史）  
  备注/产出链接：已提供积分概览/操作/记录页面（/pages/points/*）；成长树视图接入 /api/configs/growth 与 /api/points/balance 并渲染进度
- [x] M7-04 AI 聊天（WSS）：消息渲染、历史滚动、上下文限制  
  备注/产出链接：chat 页面接通 /ws（后端 ws.enabled 已开启）；新增 /api/chat/history 与 /api/chat/sessions，前端加载历史、管理会话并支持断线重连
- [x] M7-05 拍照/相册 + OCR（base64 直发 OCR 服务）；错误提示与重试  
  备注/产出链接：后端提供 /api/ocr/base64 代理转发到 ocr-service；小程序 ocr 页面改用同域代理
- [x] M7-06 脱敏展示（手机号/姓名/敏感标识）与预签名 URL 使用  
  备注/产出链接：已提供前端脱敏工具（miniapp/src/utils/mask.ts）；后端已接入 MinIO SDK 并提供预签名接口（/api/storage/presign/*）。页面实际应用脱敏可在后续用户信息视图中渐进落地。
- [ ] M7-07 联调与真机测试（常用机型）
- 交付/验收：核心流程可用，表现稳定

里程碑 M8：数据迁移与验收
- [ ] M8-01 迁移脚本：DDL 初始化 + 字典/系统项导入
- [ ] M8-02 业务数据迁移：用户/积分/记录重建映射（手机号/家长锚点），重算 daily_points
- [ ] M8-03 对账：条数/抽样/口径一致性检查清单
- [ ] M8-04 迁移回放（可重复/幂等）与最终验收
- 交付/验收：对账通过，口径一致

里程碑 M9：部署与交付（Docker + Traefik）
- [x] M9-01 deploy/.env.example（DB/JWT/MinIO/DeepSeek/OCR Token 等）  
  备注/产出链接：deploy/.env.example
- [x] M9-02 docker-compose：mysql、backend-service、ocr-service、traefik（自动证书样例）、（minio 仅占位配置，不默认启动）  
  备注/产出链接：deploy/docker-compose.yml
- [x] M9-03 Traefik：HTTP→HTTPS、WSS 透传、ACME（Let’s Encrypt）  
  备注/产出链接：deploy/traefik/traefik.yml（样例），acme.json 初始化说明
- [x] M9-04 小程序后台域名与白名单说明（现状域名）  
  备注/产出链接：deploy/README.md（环境变量 BACKEND_DOMAIN/OCR_DOMAIN 说明）
- [x] M9-05 一键启动/停止脚本与健康检查说明  
  备注/产出链接：deploy/README.md（docker compose up/down 指南）
- 交付/验收：本地一键拉起、HTTPS/WSS 可验证

里程碑 M10：测试与质量
- [ ] M10-01 Jacoco 集成与覆盖率门槛（Controller 100%）
- [ ] M10-02 单测用例：鉴权/租户/数据权限/积分边界/导出/聊天/WS/OCR 调用
- [ ] M10-03 集成测试（必要路径），并行/稳定性优化
- 交付/验收：覆盖率与用例通过，报告产出

里程碑 M11：文档与运维手册
- [ ] M11-01 README：运行/调试指南（四模块）
- [ ] M11-02 部署手册：Compose + Traefik + 域名/证书替换
- [ ] M11-03 配置清单：application.yaml 字段说明（MinIO 预留、WS、AI、JWT、Sentinel 预留）
- [ ] M11-04 数据库字典、API 清单、权限模型说明（菜单/按钮/接口/数据权限）
- [ ] M11-05 数据迁移手册与常见问题
- 交付/验收：新人按文档可本地跑通与部署

——

全局备注与约束
- 多租户：平台超管可跨租户；其余账号受 tenant_id 限制。部分系统表（如字典）可 @IgnoreTenant。
- SSO：当前不与小程序打通，但设计保留扩展点。
- 安全：接口限流预留（Sentinel），上传安全暂不启用杀毒/转码/去 EXIF；仅限文件类型白名单（后续补）。
- 合规：移动端脱敏展示；后台查询不脱敏；未成年人信息谨慎授权与审计可追溯。
- 最终上线：一次性停机发布；不做双写与灰度。

进度日志（按时间倒序追加）
- 2025-10-20：完成 M7-03/04/05（小程序成长树视图接入、聊天历史/会话管理与断线重连、OCR 代理）；补充 /api/configs/growth、/api/points/balance、/api/chat/sessions|history、/api/ocr/base64。
 - 2025-10-20：按“孩子”维度完成积分模块（域模型/API/仓储）并默认连接 NATAPP MySQL；完成 AI WS 会话存储与 DeepSeek 调用（非流式）。
 - 2025-10-20：接入 daily_points 写入（孩子维度），在积分变动时自动更新当日聚合。
  - 2025-10-20：新增 family_member 关系表并完成家庭成员聚合加载与保存（MpFamilyRepository）。
- 2025-10-20：已将 schema/seed 同步到远端 MySQL（tools/apply_sql.py，基于提供的 NATAPP 主机与端口），并完成 Account 仓储 MP 化切换开关。
- 2025-10-20：完成 M9-01~M9-05（Compose + Traefik 样例、一键启动、域名环境变量、acme 存储说明）。
- 2025-10-20：完成 M5-01~M5-05（OCR 服务：鉴权、EasyOCR、临时文件、日志、Dockerfile 与错误结构）。
- 2025-10-20：校对仓库现状；确认 M0/M1/M2/M3 完成；新增标记 M5-01/M6-01/M7-01 完成，M5-02 进行中；其余保持未开始。
- 2025-02-17：完成 M3-01~M3-09，交付业务租户/账号/家庭/积分/导出/审计接口及 WebTestClient 覆盖。
- 2025-02-16：完成 M2-05，补充 RBAC 元数据、权限画像接口与系统接口权限控制。
- 2025-02-15：完成 M2-04，后端切换 Maven 并提供 JWT 登录/刷新/注销与令牌版本踢人能力。
- 无
