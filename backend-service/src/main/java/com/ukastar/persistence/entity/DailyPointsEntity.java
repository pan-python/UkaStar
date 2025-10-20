package com.ukastar.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;

@TableName("daily_points")
public class DailyPointsEntity {
    @TableId
    private Long id;
    private Long tenantId;
    private Long childId;
    private LocalDate statDate;
    private Integer totalIncrease;
    private Integer totalDecrease;
    private Integer totalRedeem;
    private Integer balance;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }
    public LocalDate getStatDate() { return statDate; }
    public void setStatDate(LocalDate statDate) { this.statDate = statDate; }
    public Integer getTotalIncrease() { return totalIncrease; }
    public void setTotalIncrease(Integer totalIncrease) { this.totalIncrease = totalIncrease; }
    public Integer getTotalDecrease() { return totalDecrease; }
    public void setTotalDecrease(Integer totalDecrease) { this.totalDecrease = totalDecrease; }
    public Integer getTotalRedeem() { return totalRedeem; }
    public void setTotalRedeem(Integer totalRedeem) { this.totalRedeem = totalRedeem; }
    public Integer getBalance() { return balance; }
    public void setBalance(Integer balance) { this.balance = balance; }
}

