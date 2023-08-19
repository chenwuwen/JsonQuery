package com.kanyun.ui;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.sql.core.column.JsonTableColumnFactory;
import com.kanyun.sql.func.AbstractFuncSource;
import com.kanyun.sql.func.ExternalFuncClassLoader;
import com.kanyun.sql.func.FuncSourceType;
import com.kanyun.ui.model.DataBaseModel;
import com.kanyun.ui.model.JsonQueryConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        loadFunctionConfig();
        if (jsonQueryConfig.getDataBaseModelList() == null) {
            List<DataBaseModel> dataBaseModelList = new ArrayList<>();
//           这里不使用 FXCollections.emptyObservableList()方法,是因为该方法返回的EmptyObservableList不支持add数据
            dataBaseModels = FXCollections.observableList(dataBaseModelList);
        } else {
            dataBaseModels = FXCollections.observableList(jsonQueryConfig.getDataBaseModelList());
        }
        createCalciteConnection();
        JsonTableColumnFactory.loadTableColumnInfo();
    }

    /**
     * 持久化配置,每次数据库发生变动,都会进行一次持久化配置
     * 此时应该刷新Calcite的ModelJson及CalciteConnection
     */
    public static void persistenceConfig() {
        Gson gson = new Gson();
        jsonQueryConfig.setDataBaseModelList(dataBaseModels);
        String config = gson.toJson(jsonQueryConfig);
        File configFile = new File(configPath);
        try {
            Files.write(config.getBytes(StandardCharsets.UTF_8), configFile);
            refreshCalciteConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 刷新Calcite连接
     * 1.先重建model.json文件
     * 2.重新创建calcite连接
     */
    public static void refreshCalciteConnection() {
        log.info("JsonQuery配置信息发生变更,准备重新生成model.json并重建Calcite连接");
        JsonArray schemas = new JsonArray();
        for (DataBaseModel dataBaseModel : dataBaseModels) {
            schemas.add(ModelJson.buildSchema(dataBaseModel.getName(), dataBaseModel.getUrl()));
        }
        String modelJson = ModelJson.buildModelJson(schemas, "");
        ModelJson.rebuildCalciteConnection(modelJson);
    }


    /**
     * 刷新Calcite连接
     * 1.先生成model.json文件
     * 2.创建calcite连接
     */
    public static void createCalciteConnection() {
        log.info("JsonQuery应用初始化,准备生成model.json并创建Calcite连接");
        JsonArray schemas = new JsonArray();
        for (DataBaseModel dataBaseModel : dataBaseModels) {
            schemas.add(ModelJson.buildSchema(dataBaseModel.getName(), dataBaseModel.getUrl()));
        }
        String modelJson = ModelJson.buildModelJson(schemas, "");
        Thread.currentThread().setContextClassLoader(ExternalFuncClassLoader.getInstance());
        ModelJson.createCalciteConnection(modelJson);
    }

    /**
     * 构建calcite model.json配置文件
     * 并创建Schema,将触发JsonSchemaFactory.create()
     */
    public static void rebuildModelJson() {
        JsonArray schemas = new JsonArray();
        for (DataBaseModel dataBaseModel : dataBaseModels) {
            schemas.add(ModelJson.buildSchema(dataBaseModel.getName(), dataBaseModel.getUrl()));
        }
        String modelJson = ModelJson.buildModelJson(schemas, "");
        ModelJson.rebuildCalciteConnection(modelJson);
    }

    /**
     * 加载函数配置
     */
    public static void loadFunctionConfig() throws Exception {
//        反射机制,触发类加载,执行静态代码块,获取内置函数
        Class.forName(AbstractFuncSource.class.getName());
        if (StringUtils.isNotBlank(jsonQueryConfig.getFuncPath())) {
            String funcPath = jsonQueryConfig.getFuncPath();
            String funcType = jsonQueryConfig.getFuncType();
            log.info("准备解析并缓存外置函数,外置函数路径:{}", funcPath);
            if (FuncSourceType.getInstanceFromType(funcType) == FuncSourceType.MAVEN) {
                log.info("准备解析缓存来自Maven的函数:[{}]", funcPath);
                String[] split = funcPath.split(":");
                FuncSourceType.MAVEN.newInstance().loadJar(split[0], split[1], split[2]);
            } else {
                log.info("准备解析缓存来自jar文件的函数:[{}]", funcPath);
                FuncSourceType.FILE.newInstance().loadJar(funcPath);
            }
            log.info("外置函数解析并缓存完毕");
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
