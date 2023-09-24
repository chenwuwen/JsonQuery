package com.kanyun.sql.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kanyun.sql.SqlExecutor;
import com.kanyun.sql.ds.DataSourceConnectionPool;
import org.apache.calcite.model.ModelHandler;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * calcite model.json配置文件
 */
public class ModelJson {

    private static final Logger log = LoggerFactory.getLogger(ModelJson.class);

    private static JsonObject modelJsonObj;

    static {
        modelJsonObj = new JsonObject();
        modelJsonObj.addProperty("version", "1.0");
        modelJsonObj.add("defaultSchema", null);
        modelJsonObj.add("schemas", null);
    }

    /**
     * 得到 model.json文件
     *
     * @param defaultSchema 默认数据库
     * @return
     */
    public static String getModelJson(String defaultSchema) {
        if (StringUtils.isNotEmpty(defaultSchema)) {
            modelJsonObj.addProperty("defaultSchema", defaultSchema);
        }
        String modelJson = modelJsonObj.toString();
        log.info("生成的modelJson内容为[{}]", modelJson);
        return modelJson;
    }

    /**
     * 构建model.json字符串
     * model.json各模块详解:https://calcite.apache.org/docs/model.html
     *
     * @param schemas       数据库列表
     * @param defaultSchema 默认数据库
     * @return
     */
    public static String buildModelJson(JsonArray schemas, String defaultSchema) {
        modelJsonObj.add("schemas", schemas);
        modelJsonObj.addProperty("defaultSchema", defaultSchema);
        String modelJson = modelJsonObj.toString();
        log.info("生成的modelJson内容为[{}]", modelJson);
        return modelJson;
    }

    /**
     * 获得当前默认的数据库
     *
     * @return
     */
    public static String getDefaultSchema() {
        return modelJsonObj.get("defaultSchema").getAsString();
    }

    /**
     * 构建单个Schema
     *
     * @param dataBaseName 数据库名称
     * @param path         数据库地址(路径)
     * @return
     */
    public static JsonObject buildSchema(String dataBaseName, String path) {
        JsonObject schemaObj = new JsonObject();
        schemaObj.addProperty("name", dataBaseName);
        schemaObj.addProperty("type", "custom");
//        如果不想在Schema中添加表,则设置为false
//        schemaObj.addProperty("mutable", "false");
//        指定使用factory类(类的全限定名)
        schemaObj.addProperty("factory", com.kanyun.sql.core.JsonSchemaFactory.class.getName());
        JsonObject operand = new JsonObject();
        operand.addProperty("directory", path);
        schemaObj.add("operand", operand);
        return schemaObj;
    }

    /**
     * 重建Calcite连接。
     * 重建连接的时机：
     * 1：schema发生变动(新增/移除)
     * 2：自定义函数变更
     * 这里除了重新生成model.json文件,并创建新的连接外(耗费性能),还可以通过
     * 已有连接 calciteConnection.getRootSchema().add() 来添加schema
     * 移除的化目前还有找到api
     */
    public static void rebuildCalciteConnection(String modelJson) {
//            todo 重建连接池

    }

    /**
     * 创建Calcite连接
     *
     * @param modelJson
     */
    public static void createCalciteConnection(String modelJson) {
        DataSourceConnectionPool.initConnectionPool(modelJson);
    }


}
