-- 默认种子数据：平台超管与基础字典

INSERT INTO tenants (id, tenant_id, tenant_code, name, contact_name, contact_phone, status, plan)
VALUES
    (1, NULL, 'platform', '优卡星平台', '平台管理员', '000-0000-0000', 1, 'platform');

INSERT INTO accounts (id, tenant_id, account_type, username, phone, email, password_hash, status)
VALUES
    (1, NULL, 'PLATFORM_ADMIN', 'platform_admin', '18800000000', 'admin@ukastar.com', '$2a$10$abcdefghijklmnopqrstuv', 1),
    (2, 1, 'TENANT_ADMIN', 'tenant_admin', '18800000001', 'tenant@ukastar.com', '$2a$10$abcdefghijklmnopqrstuv', 1);

INSERT INTO roles (id, tenant_id, code, name, description, built_in)
VALUES
    (1, NULL, 'platform_super_admin', '平台超管', '拥有平台全部权限', 1),
    (2, 1, 'tenant_admin', '租户管理员', '租户全局配置权限', 1),
    (3, 1, 'tenant_operator', '运营专员', '日常运营与积分操作权限', 1),
    (4, 1, 'tenant_viewer', '数据只读', '只读权限', 1);

INSERT INTO perms (id, tenant_id, perm_type, code, name, description)
VALUES
    (1, NULL, 'MENU', 'dashboard', '仪表盘', '平台仪表盘'),
    (2, NULL, 'MENU', 'tenant_manage', '租户管理', '租户 CRUD'),
    (3, NULL, 'API', 'tenant:create', '创建租户', 'POST /api/tenants'),
    (4, NULL, 'API', 'tenant:update', '修改租户', 'PUT /api/tenants/{id}'),
    (5, NULL, 'API', 'tenant:list', '查询租户', 'GET /api/tenants'),
    (6, NULL, 'API', 'tenant:disable', '停用租户', 'PATCH /api/tenants/{id}/disable'),
    (7, 1, 'MENU', 'points', '积分管理', '积分模块'),
    (8, 1, 'API', 'points:record', '积分记录', 'POST /api/points/record'),
    (9, 1, 'API', 'points:list', '积分查询', 'GET /api/points'),
    (10, 1, 'MENU', 'audit', '审计日志', '审计菜单');

INSERT INTO menus (id, tenant_id, parent_id, name, path, component, icon, order_no, visible, perm_code)
VALUES
    (1, NULL, NULL, '仪表盘', '/dashboard', 'DashboardView', 'Odometer', 1, 1, 'dashboard'),
    (2, NULL, NULL, '租户管理', '/tenants', 'TenantList', 'OfficeBuilding', 2, 1, 'tenant_manage'),
    (3, 1, NULL, '积分管理', '/points', 'PointsView', 'Medal', 1, 1, 'points'),
    (4, 1, NULL, '审计日志', '/audit', 'AuditView', 'Document', 2, 1, 'audit');

INSERT INTO buttons (id, tenant_id, menu_id, name, code, order_no)
VALUES
    (1, NULL, 2, '创建租户', 'tenant:create', 1),
    (2, NULL, 2, '修改租户', 'tenant:update', 2),
    (3, 1, 3, '积分记账', 'points:record', 1);

INSERT INTO role_perm (tenant_id, role_id, perm_id, data_scope)
VALUES
    (NULL, 1, 1, 'TENANT'),
    (NULL, 1, 2, 'TENANT'),
    (NULL, 1, 3, 'TENANT'),
    (NULL, 1, 4, 'TENANT'),
    (NULL, 1, 5, 'TENANT'),
    (NULL, 1, 6, 'TENANT'),
    (1, 2, 7, 'TENANT'),
    (1, 2, 8, 'TENANT'),
    (1, 2, 9, 'TENANT'),
    (1, 2, 10, 'TENANT'),
    (1, 3, 8, 'TENANT'),
    (1, 3, 9, 'TENANT'),
    (1, 4, 9, 'SELF');

INSERT INTO account_role (tenant_id, account_id, role_id)
VALUES
    (NULL, 1, 1),
    (1, 2, 2);

INSERT INTO point_categories (id, tenant_id, code, name, description, order_no)
VALUES
    (1, 1, 'default_positive', '基础奖励', '系统默认正向积分', 1),
    (2, 1, 'default_negative', '改进项', '系统默认扣减项', 2);

INSERT INTO point_items (id, tenant_id, category_id, code, name, score, description, allow_negative)
VALUES
    (1, 1, 1, 'daily_homework', '按时完成作业', 10, '每日作业完成奖励', 0),
    (2, 1, 2, 'late_submit', '作业延迟提交', -5, '扣减积分', 1);

INSERT INTO reward_items (id, tenant_id, code, name, cost, stock, description)
VALUES
    (1, 1, 'gift_card', '礼品卡', 200, 100, '通用礼品卡'),
    (2, 1, 'movie_ticket', '电影票', 150, 50, '周末电影票');

INSERT INTO system_configs (id, tenant_id, category, config_key, config_value, description)
VALUES
    (1, NULL, 'growth', 'milestone_thresholds', JSON_OBJECT('bronze', 100, 'silver', 300, 'gold', 600), '成长树等级阈值默认配置');

