package com.kanyun.sql.core;

import com.google.common.io.Files;
import com.google.gson.*;
import com.kanyun.sql.util.NumberTypeHelper;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * JsonTable,JsonSchema中实例化
 * getRowType()用来处理列的类型的
 * scan()提供了全表扫面的功能，这里主要需要高速引擎，如何遍历及获取数据
 */
public class JsonTable extends AbstractTable implements ScannableTable {

    private static final Logger log = LoggerFactory.getLogger(JsonTable.class);

    /**
     * Json文件
     */
    private File file;
    /**
     * 表名称
     */
    private String tableName;
    /**
     * 模式名称
     */
    private String schema;


    public JsonTable(File file, String tableName, String schema) {
        this.file = file;
        this.tableName = tableName;
        this.schema = schema;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {

        JsonArray jsonArray = checkJsonFile();
//        取JsonArray的第一个子元素作为判断字段类型的基准
        JsonElement element = jsonArray.get(0);
//          判断JsonArray的第一个子元素是否是JsonObject
        if (element.isJsonObject()) {
//            定义一个链表存放字段对应的类型
            List<RelDataType> realTypes = new LinkedList<>();
//            定义一个链表存放表中的所有字段
            List<String> fields = new LinkedList<>();
            JsonObject rwoData = element.getAsJsonObject();
            Set<String> columns = rwoData.keySet();
            for (String column : columns) {
                JsonElement columnData = rwoData.get(column);
                fields.add(column);
//			       判断Json key对应的值类型是否是基本类型
                if (columnData.isJsonPrimitive()) {
                    JsonPrimitive columnValue = columnData.getAsJsonPrimitive();
                    if (columnValue.isBoolean()) {

                        realTypes.add(typeFactory.createSqlType(SqlTypeName.BOOLEAN));
                        continue;
                    }
                    if (columnValue.isString()) {
                        realTypes.add(typeFactory.createSqlType(SqlTypeName.VARCHAR));
                        continue;
                    }
                    if (columnValue.isNumber()) {
                        Number number = columnValue.getAsNumber();
//                        判断Number类型的值的具体类型,这里要注意的是,判断的类型范围要从精确到宽泛
                        if (NumberTypeHelper.isInteger(number)) {
                            realTypes.add(typeFactory.createSqlType(SqlTypeName.INTEGER));
                            continue;
                        }
                        if (NumberTypeHelper.isLong(number)) {
                            realTypes.add(typeFactory.createSqlType(SqlTypeName.BIGINT));
                            continue;
                        }
                        if (NumberTypeHelper.isFloat(number)) {
                            realTypes.add(typeFactory.createSqlType(SqlTypeName.FLOAT));
                            continue;
                        }
                        if (NumberTypeHelper.isDouble(number)) {
                            realTypes.add(typeFactory.createSqlType(SqlTypeName.DOUBLE));
                            continue;
                        }

                    }
//                   找不到对应类型就创建位置类型
                    realTypes.add(typeFactory.createUnknownType());
                    log.warn("构建表的字段类型,字段:[{}],未找到符合的类型,取值可能为空或则报错", column);
                } else {
//                    如果不是基本类型的值,可以考虑使用typeFactory.createJavaType() 创建自己的类型,注意观察与typeFactory.createSqlType()的区别
                    if (columnData.isJsonObject() || columnData.isJsonArray()) {
                        realTypes.add(typeFactory.createJavaType(String.class));
                        continue;
                    }
                    if (columnData.isJsonNull()) {
                        realTypes.add(typeFactory.createJavaType(Object.class));
                    }

                }
            }
            return typeFactory.createStructType(Pair.zip(fields, realTypes));
        } else {
            log.warn("检测到文件:[{}],行内容不是JsonObject类型", file.getPath());
        }

        return null;
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root) {
        return new AbstractEnumerable<Object[]>() {
            @Override
            public Enumerator<Object[]> enumerator() {
                return new JsonEnumerator(file);
            }
        };
    }

    /**
     * 检查Json文件内容是否符合规范
     *
     * @return
     */
    public JsonArray checkJsonFile() {
        try {
//			  加载json文件
            String content = Files.asCharSource(file, StandardCharsets.UTF_8).read();
//            将json文件转换为JsonElement对象
            JsonElement jsonElement = JsonParser.parseString(content);
//            需要保证Json文件内容是JsonArray类型
            if (jsonElement.isJsonArray()) {
                return jsonElement.getAsJsonArray();
            }
            log.error("检测到文件:[{}],内容不是JsonArray类型,构建字段类型失败", file.getPath());
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
