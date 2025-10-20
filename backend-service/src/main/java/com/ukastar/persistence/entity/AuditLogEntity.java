package com.ukastar.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("audit_logs")
public class AuditLogEntity {
    @TableId
    private Long id;
    private Long tenantId;
    private String eventType;
    private Long operatorAccountId; // 可为空
    private String targetType;
    private String targetId;
    private String detail; // 可存放 summary
    private String userAgent; // 借用保存 actor

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getOperatorAccountId() { return operatorAccountId; }
    public void setOperatorAccountId(Long operatorAccountId) { this.operatorAccountId = operatorAccountId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}

