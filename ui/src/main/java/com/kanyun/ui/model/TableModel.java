package com.kanyun.ui.model;


public class TableModel {

    private DataBaseModel dataBaseModel;

    private String dataBaseName;

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

    public String getDataBaseName() {
        return dataBaseName;
    }

    public void setDataBaseName(String dataBaseName) {
        this.dataBaseName = dataBaseName;
    }
}
