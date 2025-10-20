package com.ukastar.infra.mybatis;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.ukastar.common.tenant.TenantConstants;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 默认的租户行级过滤器，后续可结合登录用户上下文返回真实租户。
 */
@Component
public class PlatformTenantLineHandler implements TenantLineHandler {

    private static final Expression DEFAULT_TENANT_ID = new LongValue(TenantConstants.PLATFORM_TENANT_ID);
    private static final Set<String> IGNORED_TABLES = Set.of();

    @Override
    public Expression getTenantId() {
        return DEFAULT_TENANT_ID;
    }

    @Override
    public String getTenantIdColumn() {
        return TenantConstants.TENANT_ID_COLUMN;
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return IGNORED_TABLES.contains(tableName);
    }
}
