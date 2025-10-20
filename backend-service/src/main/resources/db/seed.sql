-- Seed subset for bootstrap (copied/adapted from database/seed/001_seed_data.sql)

INSERT INTO tenants (id, tenant_id, tenant_code, name, contact_name, contact_phone, status, plan)
VALUES
    (1, NULL, 'platform', '优卡星平台', '平台管理员', '000-0000-0000', 1, 'platform')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO accounts (id, tenant_id, account_type, username, phone, email, password_hash, status)
VALUES
    (1, NULL, 'PLATFORM_ADMIN', 'platform_admin', '18800000000', 'admin@ukastar.com', '$2a$10$abcdefghijklmnopqrstuv', 1)
ON DUPLICATE KEY UPDATE email=VALUES(email);

INSERT INTO roles (id, tenant_id, code, name, description, built_in)
VALUES
    (1, NULL, 'PLATFORM_ADMIN', '平台超管', '拥有平台全部权限', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO account_role (tenant_id, account_id, role_id)
VALUES
    (NULL, 1, 1)
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

