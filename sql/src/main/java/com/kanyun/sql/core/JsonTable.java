package com.kanyun.sql.core;

import com.kanyun.sql.core.column.ColumnType;
import com.kanyun.sql.core.column.JsonTableColumn;
import com.kanyun.sql.core.column.JsonTableColumnFactory;
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
import java.util.ArrayList;
import java.util.List;

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
//        解析Json文件,获取字段信息(字段名和类型)
        List<JsonTableColumn> tableColumnInfoList = JsonTableColumnFactory.getTableColumnInfoList(file, schema, tableName);
        List<String> fieldNames = new ArrayList<>();
        List<RelDataType> fieldTypes = new ArrayList<>();
        for (JsonTableColumn jsonTableColumn : tableColumnInfoList) {
            fieldNames.add(jsonTableColumn.getName());
            RelDataType sqlType;
            log.debug("准备创建[{}.{}]的字段:{} 类型:{}", schema, tableName, jsonTableColumn.getName(), jsonTableColumn.getType());
            if (jsonTableColumn.getType() == ColumnType.UNKNOWN) {
                sqlType = typeFactory.createUnknownType();
            } else {
                sqlType = typeFactory.createSqlType(SqlTypeName.get(jsonTableColumn.getType().name()));
            }
            log.debug("[{}.{}]的字段:{} 类型创建完毕", schema, tableName, jsonTableColumn.getName());
            fieldTypes.add(sqlType);
        }
        return typeFactory.createStructType(Pair.zip(fieldNames, fieldTypes));
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

}
