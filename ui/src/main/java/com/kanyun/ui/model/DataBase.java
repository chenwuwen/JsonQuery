package com.kanyun.ui.model;

import javafx.collections.ObservableList;

public class DataBase {

    /**
     * 数据库名称
     */
    private String name;


    /**
     * 数据库地址
     */
    private String url;

    /**
     * 数据库表集合
     */
    private ObservableList<String> tables;

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

    public ObservableList<String> getTables() {
        return tables;
    }

    public void setTables(ObservableList<String> tables) {
        this.tables = tables;
    }
}
