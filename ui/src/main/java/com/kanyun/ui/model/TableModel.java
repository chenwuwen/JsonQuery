package com.kanyun.ui.model;

/**
 * 表的实体类
 */
public class TableModel implements BaseModel {

    private DataBaseModel dataBaseModel;

    private String schemaName;

    private String tableName;

    private String path;


    public DataBaseModel getDataBaseModel() {
        return dataBaseModel;
    }

    public void setDataBaseModel(DataBaseModel dataBaseModel) {
        this.dataBaseModel = dataBaseModel;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }


    @Override
    public String toString() {
        return getTableName();
    }
}
