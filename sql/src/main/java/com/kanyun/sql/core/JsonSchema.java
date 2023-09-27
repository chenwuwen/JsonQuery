package com.kanyun.sql.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * 类似数据库，Schema表示数据库
 * 注意:getTableMap()会被调用多次
 */
public class JsonSchema extends AbstractSchema {

    private static final Logger log = LoggerFactory.getLogger(JsonSchema.class);

    /**
     * 模式对应的路径
     */
    private File directory;

    /**
     * 模式名
     */
    private String schema;

    /**
     * 缓存Schema下的表信息
     * key是表名,value是Table实例
     */
    private Map<String, Table> tableMap;

    public JsonSchema(File directory, String schema) {
        this.directory = directory;
        this.schema = schema;
    }

    public JsonSchema(File directory, String schema, Map<String, Table> tableMap) {
        this.directory = directory;
        this.schema = schema;
        this.tableMap = tableMap;
    }


    @Override
    protected Map<String, Table> getTableMap() {
        if (tableMap != null) return tableMap;
//         从指定路径下,获取指定文件
        File[] files = directory.listFiles((dir, name) -> name.endsWith("json"));
        if (files == null) {
            log.warn("Schema: [{}],从指定路径[{}] 下未找到符合条件的文件", schema, directory);
            files = new File[0];
        }
        final ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();
//         循环文件创建JsonTable
        for (File file : files) {
            String tableName = Files.getNameWithoutExtension(file.getName());
            log.debug("Schema:[{}], 创建表： [{}] 从文件  [{}]", schema, tableName, file.getPath());
//             一个数据库有多个表名，这里初始化，大小写要注意了,这里tableName是json文件的文件名
            builder.put(tableName, new JsonTable(file, tableName, schema));
        }
        tableMap = builder.build();
        return tableMap;
    }

}
