package com.ukastar.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("chat_messages")
public class ChatMessageEntity {
    @TableId
    private Long id;
    private Long tenantId;
    private Long sessionId;
    private String messageRole; // USER / ASSISTANT / SYSTEM
    private String content;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getMessageRole() { return messageRole; }
    public void setMessageRole(String messageRole) { this.messageRole = messageRole; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

