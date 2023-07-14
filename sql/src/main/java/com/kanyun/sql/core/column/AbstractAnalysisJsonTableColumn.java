package com.kanyun.sql.core.column;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.kanyun.sql.util.NumberTypeHelper;
import org.apache.calcite.sql.type.SqlTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 解析Json文件获取表字段及表字段类型抽象类
 */
public abstract class AbstractAnalysisJsonTableColumn {

    private static final Logger log = LoggerFactory.getLogger(AbstractAnalysisJsonTableColumn.class);

    protected final String TYPE_VARCHAR = "varchar";
    protected final String TYPE_UNKNOWN = "unknown";
    protected final String TYPE_INTEGER = "integer";
    protected final String TYPE_BOOLEAN = "boolean";
    protected final String TYPE_LONG = "bigint";
    protected final String TYPE_FLOAT = "float";
    protected final String TYPE_DOUBLE = "double";


    /**
     * Json数据文件
     */
    protected File file;

    /**
     * 表名称
     */
    protected String tableName;

    /**
     * 模式名(数据库名)
     */
    protected String schemaName;

    protected AbstractAnalysisJsonTableColumn(File file, String tableName, String schemaName) {
        this.file = file;
        this.tableName = tableName;
        this.schemaName = schemaName;
    }


    /**
     * 检测Json文件的内容是否标准,标准类型为
     * [{},{},{}]
     * 需要注意 {@link LargeFileAnalysisColumn}类在验证Json文件是否
     * 规范时可能并不准确,因为他只读取Json文件的第一个元素
     *
     * @return
     */
    abstract boolean checkJsonFileIsStand() throws Exception;

    /**
     * 解析表字段数据
     *
     * @return
     */
    abstract List<JsonTableColumn> analysisTableField() throws Exception;

    /**
     * 分析字段的数值类型
     *
     * @param number
     * @param jsonTableColumn
     */
    protected static void analysisColumnTypeForNumber(Number number, JsonTableColumn jsonTableColumn) {
//       判断Number类型的值的具体类型,这里要注意的是,判断的类型范围要从精确到宽泛
        if (NumberTypeHelper.isInteger(number)) {
            jsonTableColumn.setType(ColumnType.INTEGER);
            return;
        }
        if (NumberTypeHelper.isLong(number)) {
            jsonTableColumn.setType(ColumnType.BIGINT);
            return;
        }
        if (NumberTypeHelper.isFloat(number)) {
            jsonTableColumn.setType(ColumnType.FLOAT);
            return;
        }
        if (NumberTypeHelper.isDouble(number)) {
            jsonTableColumn.setType(ColumnType.DOUBLE);
            return;
        }
    }

    /**
     * 从JsonFile中获取表的字段信息
     *
     * @return
     */
    List<JsonTableColumn> getTableColumnFromJsonFile() throws Exception {
        if (checkJsonFileIsStand()) {
            return analysisTableField();
        }
        String msg = String.format("Schema: %s , Table: %s , Json文件:{} 不符合标准,不能解析出字段和字段类型", schemaName, tableName, file.getName());
        log.error(msg);
        throw new Exception(msg);
    }

    /**
     * 分析Json元素(Json数组中的一个元素)
     * @param jsonObject
     * @return
     */
    public static List<JsonTableColumn> analysisJsonItem(JsonObject jsonObject) {
        List<JsonTableColumn> jsonTableColumnList = new ArrayList<>();
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
