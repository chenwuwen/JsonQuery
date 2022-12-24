package com.kanyun.ui.model;


import java.util.Map;

public class TableModel implements BaseModel {

    private DataBaseModel dataBaseModel;

    private String dataBaseName;

    private String tableName;

    private String path;

    private Map<String, String> fieldInfo;

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

    public String getDataBaseName() {
        return dataBaseName;
    }

    public void setDataBaseName(String dataBaseName) {
        this.dataBaseName = dataBaseName;
    }

    public Map<String, String> getFieldInfo() {
        return fieldInfo;
    }

    public void setFieldInfo(Map<String, String> fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    @Override
    public String toString() {
        return getTableName();
    }
}
