package com.kanyun.ui;

import com.google.gson.Gson;
import com.kanyun.ui.model.DataBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class JsonQuery {

    /**
     * 数据库列表
     */
    public static ObservableList<DataBase> dataBases;


    public void initConfig() throws Exception {
        String userHome = System.getProperty("user.home");
        String configPath = userHome + File.separator + "jsonQuery.json";
        System.out.println("配置文件地址：" + configPath);
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            configFile.createNewFile();
            FileWriter fileWriter = new FileWriter(configFile);
            fileWriter.write("[]");
//            fileWriter.flush();
            fileWriter.close();
        }
        FileReader fileReader = new FileReader(configPath);
        Gson gson = new Gson();
        ArrayList<DataBase> arrayList = gson.fromJson(fileReader,
                new com.google.common.reflect.TypeToken<ArrayList<DataBase>>() {
                }.getType());
        for (int i = 0; i < 15; i++) {
            DataBase dataBase = new DataBase();
            dataBase.setName("数据库名称"+i);
            dataBase.setUrl("C:\\Users\\yingxu.zhao\\Desktop\\华西银行");
            arrayList.add(dataBase);

        }
        fileReader.close();
        dataBases = FXCollections.observableList(arrayList);
    }

}
