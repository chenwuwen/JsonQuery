package com.kanyun.sql.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.kanyun.calcite.table.JsonTable;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * 类似数据库，Schema表示数据库
 */
public class JsonSchema extends AbstractSchema {

    private static final Logger log = LoggerFactory.getLogger(JsonSchema.class);

    private final File directory;

    public JsonSchema(File directory) {
        this.directory = directory;
    }

    @Override
    protected Map<String, Table> getTableMap() {
//         从指定路径下,获取指定文件
        File[] files = directory.listFiles((dir, name) -> name.endsWith("json"));
        if (files == null) {
            log.warn("从指定路径[{}]未找到符合条件的文件", directory);
            files = new File[0];
        }

        final ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();
        // for each file create a json table
        for (File file : files) {
            String tableName = Files.getNameWithoutExtension(file.getName());
            log.info("创建表： {} 从文件 {}", tableName, file.getName());
//             一个数据库有多个表名，这里初始化，大小写要注意了,这里tableName是json文件的文件名
            builder.put(tableName, new JsonTable(file));
        }

        return builder.build();
    }
}
