package com.kanyun.sql.core;

import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * 自定义schemaFactory 入口
 * 由配置文件配置工厂类
 * ModelHandler 会调这个工厂类
 */
public class JsonSchemaFactory implements SchemaFactory {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaFactory.class);


    /**
     * 创建数据库
     *
     * @param parentSchema 当前schema的父节点，一般为root
     * @param name         数据库的名字，它在model.json中定义的
     * @param operand      也是在model中定义的，是Map类型，用于传入自定义参数
     * @return
     */
    @Override
    public Schema create(SchemaPlus parentSchema, String name, Map<String, Object> operand) {
        logger.debug("准备创建数据库:[{}],parentSchema:[{}]", name, parentSchema.getName());
//		从哪个路径下得到表数据 model.json -> schemas -> operand -> directory (需要注意的是:不管当前查询几个数据库,它都会初始化所有的数据库)
        String dataDirectory = (String) operand.get("directory");
        if (dataDirectory.startsWith("classpath:") || dataDirectory.startsWith("classPath:")) {
//            获取classPath的绝对路径
            String classPath = this.getClass().getResource("/").getPath();
//            获取数据路径相对于classPath的路径
            String relativePath = dataDirectory.substring(10);
            dataDirectory = classPath + relativePath;
        }
//        此时只返回JsonSchema实例,并不调用Schema的getTableMap()方法
        return new JsonSchema(new File(dataDirectory), name);
    }

}
