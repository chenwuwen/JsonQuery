package com.kanyun.sql.core.column;

import java.io.Serializable;

/**
 * JsonTable字段
 */
public class JsonTableColumn implements Serializable {
    /**
     * 字段名
     */
    private String name;
    /**
     * 字段类型
     */
    private ColumnType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnType getType() {
        return type;
    }

    public void setType(ColumnType type) {
        this.type = type;
    }
}
