package com.ukastar.infra.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ukastar.common.audit.AuditFieldNames;
import com.ukastar.security.SecurityConstants;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * 统一审计字段填充逻辑。
 */
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    private final Clock clock;

    public AuditMetaObjectHandler(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = now();
        fillIfNull(metaObject, AuditFieldNames.CREATED_AT, now);
        fillIfNull(metaObject, AuditFieldNames.UPDATED_AT, now);
        fillIfNull(metaObject, AuditFieldNames.CREATED_BY, SecurityConstants.SYSTEM_USER_ID);
        fillIfNull(metaObject, AuditFieldNames.UPDATED_BY, SecurityConstants.SYSTEM_USER_ID);
        fillIfNull(metaObject, AuditFieldNames.IS_DELETED, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = now();
        setFieldValByName(AuditFieldNames.UPDATED_AT, now, metaObject);
        setFieldValByName(AuditFieldNames.UPDATED_BY, SecurityConstants.SYSTEM_USER_ID, metaObject);
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private void fillIfNull(MetaObject metaObject, String fieldName, Object value) {
        Object current = getFieldValByName(fieldName, metaObject);
        if (Objects.isNull(current)) {
            setFieldValByName(fieldName, value, metaObject);
        }
    }
}
