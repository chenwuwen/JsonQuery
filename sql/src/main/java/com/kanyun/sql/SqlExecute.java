package com.kanyun.sql;

import com.kanyun.sql.core.ColumnValueConvert;
import com.kanyun.sql.func.AbstractFuncSource;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.NullCollation;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.util.ConversionUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class SqlExecute {

    private static final Logger log = LoggerFactory.getLogger(SqlExecute.class);
    static Properties info;

    static {
        System.setProperty("saffron.default.charset", ConversionUtil.NATIVE_UTF16_CHARSET_NAME);
        System.setProperty("saffron.default.nationalcharset", ConversionUtil.NATIVE_UTF16_CHARSET_NAME);
        System.setProperty("saffron.default.collation.name", ConversionUtil.NATIVE_UTF16_CHARSET_NAME + "$en_US");
        info = new Properties();
        info.setProperty(CalciteConnectionProperty.DEFAULT_NULL_COLLATION.camelName(), NullCollation.LAST.name());
        info.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), "false");
    }

    /**
     * 执行SQL
     *
     * @param modelJson calcite model.json文件
     * @param sql       待执行的sql
     * @return
     * @throws SQLException
     */
    public static Pair<Map<String, Integer>, List<Map<String, Object>>> execute(String modelJson, String sql) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:calcite:model=inline:" + modelJson, info);
//         取得Calcite连接
        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        AbstractFuncSource.registerFunction(rootSchema);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        int columnCount = resultSet.getMetaData().getColumnCount();
        Map<String, Integer> columnInfos = new HashMap();
//        注意 resultSet的get()方法,要从1开始,而不是0
        for (int i = 1; i <= columnCount; i++) {
//            如果sql中未使用AS来指定别名,则getColumnLabel返回的值将与getColumnName方法返回的值相同
            String columnLabel = resultSet.getMetaData().getColumnLabel(i);
            int columnType = resultSet.getMetaData().getColumnType(i);
            columnInfos.put(columnLabel, columnType);
        }

        ColumnValueConvert columnValueConvert = new ColumnValueConvert(columnInfos);
        List<Map<String, Object>> data = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> row = columnValueConvert.extractRowData(resultSet);
            data.add(row);
        }
        return Pair.of(columnInfos, data);
    }
}
