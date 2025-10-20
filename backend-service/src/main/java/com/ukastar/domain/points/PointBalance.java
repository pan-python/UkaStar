package com.ukastar.domain.points;

/**
 * 家庭积分余额。 
 */
public record PointBalance(
        Long familyId,
        Long tenantId,
        int balance
) {
    public PointBalance withBalance(int newBalance) {
        return new PointBalance(familyId, tenantId, newBalance);
    }
}
