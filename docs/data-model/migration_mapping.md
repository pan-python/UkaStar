# 数据迁移映射方案

## 概述
以旧系统手机号 + 家长账号为锚点，将家庭、孩子、积分等数据迁移至新模型。迁移遵循幂等、可回放原则。

## 字段映射

| 旧系统表 | 新系统表 | 关键字段映射 | 备注 |
| --- | --- | --- | --- |
| `users` | `accounts` | `users.phone` → `accounts.phone`；`users.role` → `account_type` | 平台管理员映射为 `PLATFORM_ADMIN`，其余按租户角色映射 |
| `families` | `families` | `families.code` → `family_code`；`families.name` → `name` | 若旧系统无租户概念，需通过配置映射 `tenant_id` |
| `parents` | `parents` | `parents.user_id` → `accounts.id`；`parents.name` → `display_name` | 缺失 `relationship` 时默认 `OTHER` |
| `children` | `children` | `children.code` → `child_code`；`children.name` → `name`；`children.birth` → `birthday` | 性别字段映射 `M/F`，缺失为 `U` |
| `family_members` | `parent_child` | `family_members.parent_id/child_id` → `parent_id/child_id` | 生成多对多关系，并补充监护人标识 |
| `point_types` | `point_categories` | `point_types.code/name` 保持一致 | 同步默认排序 |
| `point_items` | `point_items` | `point_items.score` → `score`；`allow_negative` 取旧字段 | 如旧系统仅记录加分，扣分项需新增 |
| `rewards` | `reward_items` | `rewards.cost` → `cost`；`rewards.stock` → `stock` | 描述字段整合 |
| `point_logs` | `point_records` | `point_logs.type` → `record_type`；`point_logs.value` → `points` | 需提前补全 `before_points/after_points` |
| `daily_points` | `daily_points` | 直接映射 `stat_date`、`balance` | 若缺少则运行回放脚本重算 |
| `growth_settings` | `system_configs` | 成长树阈值写入 `system_configs` 表（`category='growth'`） | 没有此表时按租户维度创建默认阈值 |
| `chat_history` | `chat_sessions` + `chat_messages` | 按 `session_id` 聚合，首条记录生成 session | 仅保留最近 N 条上下文 |
| `uploads` | `file_objects` | `uploads.path` → `object_key`；`uploads.filename` → `filename` | 对接 MinIO 后更新 bucket 信息 |
| `audit_logs` | `audit_logs` | 字段一一对应，补全 `trace_id` | 无 trace 时可根据迁移批次生成 |

## 迁移步骤
1. **租户初始化**：手工创建平台租户与各学校租户，记录旧系统与新租户 ID 映射表。
2. **账号迁移**：按租户划分导出账号列表，生成密码重置链接或初始密码（通知用户）。
3. **家庭与成员**：根据手机号关联家长账号，创建家庭与家长、孩子记录，再写入 `parent_child` 关系。
4. **成长树阈值**：读取旧系统 `growth_settings` 或配置文件，写入新表 `system_configs`（新增 `category='growth'`、`key='milestone_thresholds'`）。
5. **积分流水**：按 `child_id` 分批导入，计算导入前余额写入 `before_points`，导入后更新 `after_points`。
6. **日统计回放**：导入全部流水后，按日期重算 `daily_points`，校验与旧系统报表一致。
7. **AI/OCR、审计**：迁移近 6 个月数据，历史更久数据归档于对象存储。
8. **验收与锁定**：迁移完毕后进行抽样核对与全量对账，确认无误后锁定旧系统写权限。

## 风险与回滚
- 迁移前导出全量备份，保留 CSV/SQL 与对象存储快照。
- 引入迁移版本号表（`migration_jobs`）记录批次，可根据该表回滚或重放。
- 若积分流水导入失败，可按 `tenant_id + child_id` 删除对应数据后重新导入。
