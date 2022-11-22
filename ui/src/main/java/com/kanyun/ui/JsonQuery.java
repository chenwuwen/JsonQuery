package com.kanyun.ui;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.sql.func.FuncSourceType;
import com.kanyun.ui.model.DataBaseModel;
import com.kanyun.ui.model.JsonQueryConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebParam;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JsonQuery {


    private static final Logger log = LoggerFactory.getLogger(JsonQuery.class);

    /**
     * JsonQuery配置文件路径
     */
    private static String configPath;

    /**
     * JsonQuery配置类实体
     */
    private static JsonQueryConfig jsonQueryConfig;

    /**
     * 数据库列表
     */
    public static ObservableList<DataBaseModel> dataBaseModels;

    static {
        String userHome = System.getProperty("user.home");
        configPath = userHome + File.separator + "jsonQuery.json";
        log.info("JsonQuery初始化配置,配置文件地址：[{}]", configPath);
    }


    /**
     * 初始化配置
     *
     * @throws Exception
     */
    public void initConfig() throws Exception {
        Gson gson = new Gson();
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            configFile.createNewFile();
            jsonQueryConfig = new JsonQueryConfig();
            String config = gson.toJson(jsonQueryConfig);
            FileWriter fileWriter = new FileWriter(configFile);
            fileWriter.write(config);
//            fileWriter.flush();
            fileWriter.close();
        }
        FileReader fileReader = new FileReader(configPath);
//        gson序列化数组
//        ArrayList<DataBaseModel> arrayList = gson.fromJson(fileReader,
//                new com.google.common.reflect.TypeToken<ArrayList<DataBaseModel>>() {
//                }.getType());
        jsonQueryConfig = gson.fromJson(fileReader, JsonQueryConfig.class);
        fileReader.close();
//        加载函数配置
//        loadFunctionConfig();
        if (jsonQueryConfig.getDataBaseModelList() == null) {
            List<DataBaseModel> dataBaseModelList = new ArrayList<>();
//           这里不使用 FXCollections.emptyObservableList()方法,是因为该方法返回的EmptyObservableList不支持add数据
            dataBaseModels = FXCollections.observableList(dataBaseModelList);
        } else {
            dataBaseModels = FXCollections.observableList(jsonQueryConfig.getDataBaseModelList());
        }
        rebuildModelJson();
    }

    /**
     * 持久化配置,每次数据库发生变动,都会进行一次持久化配置
     * 此时亦更新Calcite的ModelJson
     */
    public static void persistenceConfig() {
        Gson gson = new Gson();
        jsonQueryConfig.setDataBaseModelList(dataBaseModels);
        String config = gson.toJson(jsonQueryConfig);
        File configFile = new File(configPath);
        try {
            Files.write(config.getBytes(StandardCharsets.UTF_8), configFile);
            rebuildModelJson();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建calcite model.json配置文件
     */
    public static void rebuildModelJson() {
        JsonArray schemas = new JsonArray();
        for (DataBaseModel dataBaseModel : dataBaseModels) {
            schemas.add(ModelJson.buildSchema(dataBaseModel.getName(), dataBaseModel.getUrl()));
        }
        ModelJson.buildModelJson(schemas, "");
    }

    /**
     * 加载函数配置
     */
    public static void loadFunctionConfig() throws Exception {
        if (StringUtils.isNotBlank(jsonQueryConfig.getFuncPath())) {
            String funcPath = jsonQueryConfig.getFuncPath();
            log.info("准备加载函数:[{}]", funcPath);
            if (FuncSourceType.valueOf(jsonQueryConfig.getFuncType()) == FuncSourceType.MAVEN) {
                String[] split = funcPath.split(":");
                FuncSourceType.MAVEN.newInstance().loadJar(split[0], split[1], split[2]);
            } else {
                FuncSourceType.FILE.newInstance().loadJar(funcPath);
            }
        }
    }

    /**
     * 持久化函数配置
     */
    public static void persistenceFunctionConfig(FuncSourceType funcSourceType, String funcTarget) {
        jsonQueryConfig.setFuncType(funcSourceType.getType());
        jsonQueryConfig.setFuncPath(funcTarget);
        persistenceConfig();
    }

    public static JsonQueryConfig getJsonQueryConfig() {

        return jsonQueryConfig;
    }
}
