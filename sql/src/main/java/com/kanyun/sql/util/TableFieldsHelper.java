package com.kanyun.sql.util;

import com.google.common.io.Files;
import com.google.gson.*;
import com.kanyun.sql.core.JsonTable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.type.SqlTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 获取表字段及类型
 */
public class TableFieldsHelper {
    private static final Logger log = LoggerFactory.getLogger(TableFieldsHelper.class);

    public static void getTableFieldInfo(String schema, String table, File jsonFile) {

    }

    public static void getTableFieldInfo(File jsonFile) {
        Map<String, SqlTypeName> fieldInfo = new HashMap();
        JsonArray jsonArray = checkJsonFile(jsonFile);
//        取JsonArray的第一个子元素作为判断字段类型的基准
        JsonElement element = jsonArray.get(0);
//          判断JsonArray的第一个子元素是否是JsonObject
        if (element.isJsonObject()) {
            JsonObject rwoData = element.getAsJsonObject();
            Set<String> columns = rwoData.keySet();
            for (String column : columns) {
                JsonElement columnData = rwoData.get(column);
//			       判断Json key对应的值类型是否是基本类型
                if (columnData.isJsonPrimitive()) {
                    JsonPrimitive columnValue = columnData.getAsJsonPrimitive();
                    if (columnValue.isBoolean()) {
                        fieldInfo.put(column, SqlTypeName.BOOLEAN);
                        continue;
                    }
                    if (columnValue.isString()) {
                        fieldInfo.put(column, SqlTypeName.VARCHAR);
                        continue;
                    }
                    if (columnValue.isNumber()) {
                        Number number = columnValue.getAsNumber();
//                        判断Number类型的值的具体类型,这里要注意的是,判断的类型范围要从精确到宽泛
                        if (NumberTypeHelper.isInteger(number)) {
                            fieldInfo.put(column, SqlTypeName.INTEGER);
                            continue;
                        }
                        if (NumberTypeHelper.isLong(number)) {
                            fieldInfo.put(column, SqlTypeName.BIGINT);
                            continue;
                        }
                        if (NumberTypeHelper.isFloat(number)) {
                            fieldInfo.put(column, SqlTypeName.FLOAT);
                            continue;
                        }
                        if (NumberTypeHelper.isDouble(number)) {
                            fieldInfo.put(column, SqlTypeName.DOUBLE);
                            continue;
                        }
                    }
//                   找不到对应类型就创建位置类型
                    fieldInfo.put(column, null);
                    log.warn("构建表的字段类型,字段:[{}],未找到符合的类型,取值可能为空或则报错", column);
                } else {
//                    如果不是基本类型的值,可以考虑使用typeFactory.createJavaType() 创建自己的类型,注意观察与typeFactory.createSqlType()的区别
//                    if (columnData.isJsonObject() || columnData.isJsonArray()) {
//                        realTypes.add(typeFactory.createJavaType(String.class));
//                        continue;
//                    }
//                    if (columnData.isJsonNull()) {
//                        realTypes.add(typeFactory.createJavaType(Object.class));
//                    }

                }
            }
        }
    }

    /**
     * 检查Json文件内容是否符合规范
     *
     * @return
     */
    public static JsonArray checkJsonFile(File file) {
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
