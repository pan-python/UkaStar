package com.ukastar.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("point_records")
public class PointRecordEntity {
    @TableId
    private Long id;
    private Long tenantId;
    private String recordType; // INCREASE/DECREASE/REDEEM
    private Long pointItemId;
    private Long rewardItemId;
    private Long childId;
    private Long operatorAccountId;
    private Integer quantity;
    private Integer points;
    private Integer beforePoints;
    private Integer afterPoints;
    private LocalDateTime occurredAt;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    public Long getPointItemId() { return pointItemId; }
    public void setPointItemId(Long pointItemId) { this.pointItemId = pointItemId; }
    public Long getRewardItemId() { return rewardItemId; }
    public void setRewardItemId(Long rewardItemId) { this.rewardItemId = rewardItemId; }
    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }
    public Long getOperatorAccountId() { return operatorAccountId; }
    public void setOperatorAccountId(Long operatorAccountId) { this.operatorAccountId = operatorAccountId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public Integer getBeforePoints() { return beforePoints; }
    public void setBeforePoints(Integer beforePoints) { this.beforePoints = beforePoints; }
    public Integer getAfterPoints() { return afterPoints; }
    public void setAfterPoints(Integer afterPoints) { this.afterPoints = afterPoints; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

