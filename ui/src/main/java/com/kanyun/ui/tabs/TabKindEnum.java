package com.kanyun.ui.tabs;

/**
 * Tab页种类枚举
 */
public enum TabKindEnum {
    OBJECT_TAB("对象Tab页"), SQL_TAB("SQL查询TAB页"), TABLE_TAB("表数据Tab页"), INSPECT_TAB("表字段Tab页");

    /**
     * Tab页种类描述
     */
    private String describe;

    TabKindEnum(String describe) {
        this.describe = describe;
    }
}
