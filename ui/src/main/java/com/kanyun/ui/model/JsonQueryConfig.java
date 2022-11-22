package com.kanyun.ui.model;

import java.util.List;

public class JsonQueryConfig {
    private String funcType;
    private String funcPath;
    private List<DataBaseModel> dataBaseModelList;

    public String getFuncType() {
        return funcType;
    }

    public void setFuncType(String funcType) {
        this.funcType = funcType;
    }

    public String getFuncPath() {
        return funcPath;
    }

    public void setFuncPath(String funcPath) {
        this.funcPath = funcPath;
    }

    public List<DataBaseModel> getDataBaseModelList() {
        return dataBaseModelList;
    }

    public void setDataBaseModelList(List<DataBaseModel> dataBaseModelList) {
        this.dataBaseModelList = dataBaseModelList;
    }
}
