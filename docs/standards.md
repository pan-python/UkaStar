# 统一规范

本规范适用于“优卡星”升级后全栈项目，涵盖编码、提交、错误码、日志与审计等统一约定。

## 1. 编码规范

### 1.1 Java（backend-service）
- 遵循 Google Java Style（基于 Spotless 校验）。
- 包结构：`com.ukastar.<layer>`，按 `api/service/domain/repository/infra/security/common` 划分。
- Controller 层仅负责请求响应映射，业务逻辑进入 Service，数据访问通过 Repository。
- 常量放置在 `common.constants`，避免魔法值。
- DTO 使用 `record` 或 Lombok `@Data`（如需可配置），入参启用 `jakarta.validation` 注解。

### 1.2 前端（admin-ui / miniapp）
- TypeScript 全量开启严格模式；使用 ESLint + Prettier 自动化格式化。
- 组件命名遵循 PascalCase，组合式 API（`<script setup lang="ts">`）。
- 状态管理统一使用 Pinia；请求封装 Axios/uni.request，集中处理响应码与错误提示。
- CSS 使用 UnoCSS/SCSS 模块化，禁止全局污染，配合主题变量。

### 1.3 Python（ocr-service）
- 遵循 PEP 8，使用 Ruff + Black 格式化。
- FastAPI 路由按功能拆分模块化；依赖注入管理配置与客户端实例。
- 所有外部交互（文件、OCR 推理）需捕获异常并记录日志。

## 2. 提交规范
- Commit Message 采用 Conventional Commits：`<type>(scope): <subject>`。
- 常用 type：`feat`、`fix`、`docs`、`style`、`refactor`、`test`、`chore`、`build`、`ci`。
- Subject 使用中文或英文均可，建议 50 字符以内；Body 描述动机和细节。
- PR 描述包含变更摘要、测试情况与影响面。

## 3. 接口错误码
- 采用五位数编码：`A/B/C` 类（借鉴阿里规范）。
  - `A`：客户端参数/权限问题（例如 `A0403` 未授权）。
  - `B`：服务处理异常（例如 `B0001` 通用错误、`B0404` 资源不存在）。
  - `C`：第三方依赖错误（例如 `C0400` OCR 调用失败）。
- 返回体结构：
  ```json
  {
    "code": "A0403",
    "message": "无访问权限",
    "data": null,
    "traceId": "..."
  }
  ```
- 成功固定返回 `code = "00000"`。

## 4. 日志与审计规范
- 使用统一日志框架（后端：Logback + JSON Layout；前端：埋点统一上报；OCR：结构化日志）。
- 每个请求生成 TraceId，贯穿调用链，写入日志与返回体。
- 关键动作必须写入审计日志：登录、登出、权限变更、积分操作、导出、AI/OCR 调用。
- 审计日志字段：事件类型、操作人、租户、目标资源、结果、上下文、时间戳、TraceId。
- 日志等级：`INFO`（业务流程）、`WARN`（可恢复异常）、`ERROR`（不可恢复），调试阶段可启用 `DEBUG`。

## 5. 文档与评审
- 所有新模块提交前必须附带 README/ADR 或在此文档补充说明。
- 设计变更需发起评审，结论纳入 `docs/` 目录归档。
- 代码合并前通过自动化测试与静态检查。
