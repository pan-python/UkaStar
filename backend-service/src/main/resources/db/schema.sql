-- Bundled schema for bootstrap (copied from database/schema/001_tables.sql)
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Only include a subset essential to bootstrap; full DDL resides in database/schema/001_tables.sql

CREATE TABLE IF NOT EXISTS tenants (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NULL,
    tenant_code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    contact_name VARCHAR(64) NULL,
    contact_phone VARCHAR(32) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    plan VARCHAR(64) NULL,
    expire_at DATETIME NULL,
    remarks VARCHAR(255) NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    created_by BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT UNSIGNED NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenants_code (tenant_code),
    KEY idx_tenants_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT='租户信息';

CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NULL,
    account_type ENUM('PLATFORM_ADMIN', 'TENANT_ADMIN', 'OPERATOR', 'READONLY', 'PARENT') NOT NULL,
    username VARCHAR(64) NULL,
    phone VARCHAR(32) NOT NULL,
    email VARCHAR(128) NULL,
    password_hash VARCHAR(255) NULL,
    salt VARCHAR(64) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    avatar_url VARCHAR(255) NULL,
    last_login_at DATETIME NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    created_by BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT UNSIGNED NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_accounts_phone_tenant (tenant_id, phone),
    UNIQUE KEY uk_accounts_username_tenant (tenant_id, username),
    KEY idx_accounts_type (account_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT='账号信息';

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255) NULL,
    built_in TINYINT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    created_by BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT UNSIGNED NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_roles_code_tenant (tenant_id, code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT='角色定义';

CREATE TABLE IF NOT EXISTS account_role (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NULL,
    account_id BIGINT UNSIGNED NOT NULL,
    role_id BIGINT UNSIGNED NOT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    created_by BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT UNSIGNED NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_account_role (tenant_id, account_id, role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT='账号角色绑定';

SET FOREIGN_KEY_CHECKS = 1;

