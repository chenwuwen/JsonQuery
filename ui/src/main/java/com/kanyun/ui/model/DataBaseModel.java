package com.kanyun.ui.model;

import javafx.collections.ObservableList;

public class DataBaseModel implements BaseModel {

    /**
     * 数据库名称
     */
    private String name;


    /**
     * 数据库地址
     */
    private String url;

    /**
     * 数据库表集合(不进行持久化,因为表的数量是变动的)
     */
    private transient ObservableList<TableModel> tables;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ObservableList<TableModel> getTables() {
        return tables;
    }

    public void setTables(ObservableList<TableModel> tables) {
        this.tables = tables;
    }

    @Override
    public String toString() {
        return getName();
    }
}
