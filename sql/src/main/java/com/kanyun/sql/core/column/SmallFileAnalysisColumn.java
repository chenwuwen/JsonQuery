package com.kanyun.sql.core.column;

import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 小文件字段解析,直接加载json文件进行解析
 */
public class SmallFileAnalysisColumn extends AbstractAnalysisJsonTableColumn {
    private static final Logger log = LoggerFactory.getLogger(SmallFileAnalysisColumn.class);

    protected SmallFileAnalysisColumn(File file, String tableName, String schemaName) {
        super(file, tableName, schemaName);
    }


    @Override
    boolean checkJsonFileIsStand() throws Exception {
        try {
//			  加载json文件
            String content = Files.asCharSource(file, StandardCharsets.UTF_8).read();
//            将json文件转换为JsonElement对象
            JsonElement jsonElement = JsonParser.parseString(content);
//            需要保证Json文件内容是JsonArray类型
            return jsonElement.isJsonArray() && jsonElement.getAsJsonArray().get(0).isJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    List<JsonTableColumn> analysisTableField() throws Exception {
        List<JsonTableColumn> jsonTableColumnList = new ArrayList<>();
//        加载json文件
        String content = Files.asCharSource(file, StandardCharsets.UTF_8).read();
//       取出Json文件并转换成数组,然后取出数组中的第一个元素(JsonObject)
        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonArray().get(0).getAsJsonObject();
        for (Map.Entry<String, JsonElement> rowData : jsonObject.entrySet()) {
            JsonTableColumn jsonTableColumn = new JsonTableColumn();
            String column = rowData.getKey();
            jsonTableColumn.setName(column);
            JsonElement columnData = rowData.getValue();
//            判断Json对象key对应的值类型是否是基本类型
            if (columnData.isJsonPrimitive()) {
                JsonPrimitive columnValue = columnData.getAsJsonPrimitive();
                if (columnValue.isBoolean()) {
                    jsonTableColumn.setType(ColumnType.BOOLEAN);
                } else if (columnValue.isString()) {
                    jsonTableColumn.setType(ColumnType.VARCHAR);
                } else if (columnValue.isNumber()) {
                    Number number = columnValue.getAsNumber();
                    analysisColumnTypeForNumber(number, jsonTableColumn);
                } else {
//                    找不到对应类型就创建未知类型
                    jsonTableColumn.setType(ColumnType.UNKNOWN);
                    log.warn("构建表的字段类型,字段:[{}],未找到符合的类型,取值可能为空或则报错", column);
                }
            } else {
                if (columnData.isJsonObject() || columnData.isJsonArray()) {
                    jsonTableColumn.setType(ColumnType.VARCHAR);
                } else if (columnData.isJsonNull()) {
                    jsonTableColumn.setType(ColumnType.VARCHAR);
                } else {
                    log.warn("解析字段类型,字段：{} ,值：{},非基本字段类型,也不是Json类型", column, columnData);
//                如果不是基本类型的值,也可以考虑使用typeFactory.createJavaType() 创建自己的类型,注意观察与typeFactory.createSqlType()的区别
//                if (columnData.isJsonObject() || columnData.isJsonArray()) {
//                    realTypes.add(typeFactory.createJavaType(String.class));
//                    continue;
//                }
                }
            }
            jsonTableColumnList.add(jsonTableColumn);
        }
        return jsonTableColumnList;
    }
}
