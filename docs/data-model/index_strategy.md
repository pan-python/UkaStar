# 索引与唯一性约束策略

## 总体原则
- 所有业务查询均需携带 `tenant_id`，因此联合索引优先包含 `tenant_id`。
- 唯一约束采用 `tenant_id + 业务键`，平台级数据允许 `tenant_id IS NULL`。
- 时间序查询（流水、审计、聊天）增加 `(tenant_id, target_id, occurred_at/created_at)` 复合索引。

## 关键索引一览

| 表名 | 索引/约束 | 说明 |
| --- | --- | --- |
| `accounts` | `uk_accounts_phone_tenant(tenant_id, phone)` | 同一租户手机号唯一 |
|  | `idx_accounts_type(account_type)` | 支持按账号类型筛选 |
| `roles` | `uk_roles_code_tenant(tenant_id, code)` | 角色编码唯一 |
| `perms` | `uk_perms_code_tenant(tenant_id, code)` | 权限编码唯一 |
| `role_perm` | `uk_role_perm(tenant_id, role_id, perm_id)` | 角色权限绑定去重 |
| `account_role` | `uk_account_role(tenant_id, account_id, role_id)` | 账号角色绑定去重 |
| `menus` | `uk_menus_path(tenant_id, path)` | 前端路由唯一 |
| `buttons` | `uk_buttons_code(tenant_id, code)` | 按钮资源码唯一 |
| `families` | `uk_families_code(tenant_id, family_code)` | 家庭编码唯一 |
| `children` | `uk_children_code(tenant_id, child_code)` | 孩子编码唯一 |
| `parent_child` | `uk_parent_child(tenant_id, parent_id, child_id)` | 家长与孩子关联唯一 |
| `point_categories` | `uk_point_categories_code(tenant_id, code)` | 积分类别唯一 |
| `point_items` | `uk_point_items_code(tenant_id, code)` | 积分项目唯一 |
| `reward_items` | `uk_reward_items_code(tenant_id, code)` | 兑换项唯一 |
| `system_configs` | `uk_system_configs(tenant_id, category, config_key)` | 系统配置键唯一 |
| `point_records` | `idx_point_records_child(tenant_id, child_id, occurred_at)` | 常用孩子维度流水查询 |
|  | `idx_point_records_operator(tenant_id, operator_account_id, occurred_at)` | 操作人查询 |
| `daily_points` | `uk_daily_points(tenant_id, child_id, stat_date)` | 每日统计唯一 |
| `chat_sessions` | `idx_chat_sessions_account(tenant_id, account_id)` | 会话列表查询 |
| `chat_messages` | `idx_chat_messages_session(tenant_id, session_id, created_at)` | 消息历史分页 |
| `file_objects` | `uk_file_objects_key(object_key)` | 对象存储 key 唯一 |
| `audit_logs` | `idx_audit_logs_event(tenant_id, event_type, created_at)` | 审计报表按类型查询 |

## 维护建议
- 结合 MyBatis-Plus 自动维护 `tenant_id` 条件，避免遗漏。
- 大表（`point_records`, `audit_logs`, `chat_messages`）按月份归档或分区可在后续扩展。
