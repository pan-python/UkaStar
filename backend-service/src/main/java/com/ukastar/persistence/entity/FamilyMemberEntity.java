package com.ukastar.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("family_member")
public class FamilyMemberEntity {
    @TableId
    private Long id;
    private Long tenantId;
    private Long familyId;
    private String memberType; // PARENT/CHILD
    private Long memberId;
    private String relation;
    private Integer isGuardian;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getFamilyId() { return familyId; }
    public void setFamilyId(Long familyId) { this.familyId = familyId; }
    public String getMemberType() { return memberType; }
    public void setMemberType(String memberType) { this.memberType = memberType; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    public Integer getIsGuardian() { return isGuardian; }
    public void setIsGuardian(Integer isGuardian) { this.isGuardian = isGuardian; }
}

