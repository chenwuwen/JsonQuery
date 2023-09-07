package com.kanyun.ui.model;

import javafx.beans.property.*;

/**
 * 数据库表的元信息
 * 数据库表的元信息是指表的结构信息，包括表的列、数据类型、主键、外键、索引等信息
 * 这里添加这个类,主要是为了在检查表的Tab中设置值
 */
public class TableMetaData {

    private String schema;

    private String table;

    /**
     * 字段名
     */
    private final StringProperty columnName = new SimpleStringProperty();
    /**
     * 字段类型
     */
    private final StringProperty columnType = new SimpleStringProperty();
    /**
     * 字段默认值
     */
    private final StringProperty columnDefaultValue = new SimpleStringProperty();


    /**
     * 是否是索引
     */
    private final BooleanProperty index = new SimpleBooleanProperty();

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getColumnName() {
        return columnName.get();
    }

    public StringProperty columnNameProperty() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName.set(columnName);
    }

    public String getColumnType() {
        return columnType.get();
    }

    public StringProperty columnTypeProperty() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType.set(columnType);
    }

    public boolean isIndex() {
        return index.get();
    }

    public BooleanProperty indexProperty() {
        return index;
    }

    public void setIndex(boolean index) {
        this.index.set(index);
    }

    public String getColumnDefaultValue() {
        return columnDefaultValue.get();
    }

    public StringProperty columnDefaultValueProperty() {
        return columnDefaultValue;
    }

    public void setColumnDefaultValue(String columnDefaultValue) {
        this.columnDefaultValue.set(columnDefaultValue);
    }

    public static Builder newBuilder(String schemaName, String tableName) {
        return new Builder(schemaName, tableName);
    }

    public static final class Builder {
        private final String schemaName;
        private final String tableName;
        private String columnName;
        private String columnType;
        private String columnDefaultValue;

        private Builder(String schemaName, String tableName) {
            this.tableName = tableName;
            this.schemaName = schemaName;
        }

        public Builder setColumnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

        public Builder setColumnType(String columnType) {
            this.columnType = columnType;
            return this;
        }
        public Builder setColumnDefaultValue(String columnDefaultValue) {
            this.columnDefaultValue = columnDefaultValue;
            return this;
        }
        public TableMetaData builder() {
            TableMetaData tableMetaData = new TableMetaData();
            tableMetaData.setTable(tableName);
            tableMetaData.setSchema(schemaName);
            tableMetaData.setColumnName(columnName);
            tableMetaData.setColumnType(columnType);
            tableMetaData.setColumnDefaultValue(columnDefaultValue);
            return tableMetaData;
        }
    }
}
