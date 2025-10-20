package com.ukastar.domain.rbac;

/**
 * 数据范围定义，用于约束账号可访问的数据级别。
 */
public enum DataScope {
    SELF,
    GROUP,
    TENANT;

    public static DataScope max(DataScope first, DataScope second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.ordinal() >= second.ordinal() ? first : second;
    }
}
