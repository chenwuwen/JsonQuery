package com.kanyun.sql.core;

import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

import java.io.File;
import java.util.Map;

/**
 * 自定义schemaFactory 入口
 * 由配置文件配置工厂类
 * ModelHandler 会调这个工厂类
 */
public class JsonSchemaFactory implements SchemaFactory {

    public JsonSchemaFactory() {
    }

    /**
     * @param parentSchema 当前schema的父节点，一般为root
     * @param name         数据库的名字，它在model.json中定义的
     * @param operand      也是在model中定义的，是Map类型，用于传入自定义参数
     * @return
     */
    @Override
    public Schema create(SchemaPlus parentSchema, String name, Map<String, Object> operand) {
//		从哪个路径下得到表数据 model.json -> schema -> operand -> directory
        String dataDirectory = (String) operand.get("directory");
        if (dataDirectory.startsWith("classpath:") || dataDirectory.startsWith("classPath:")) {
//            获取classPath的绝对路径
            String classPath = this.getClass().getResource("/").getPath();
//            获取数据路径相对于classPath的路径
            String relativePath = dataDirectory.substring(10);
            dataDirectory = classPath + relativePath;
        }
        return new JsonSchema(new File(dataDirectory));
    }

}
