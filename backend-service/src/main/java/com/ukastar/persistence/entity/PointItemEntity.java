package com.ukastar.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("point_items")
public class PointItemEntity {
    @TableId
    private Long id;
    private Long tenantId;
    private Long categoryId;
    private String code;
    private String name;
    private Integer points;
    private Integer allowNegative;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public Integer getAllowNegative() { return allowNegative; }
    public void setAllowNegative(Integer allowNegative) { this.allowNegative = allowNegative; }
}

