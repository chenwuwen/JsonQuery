package com.kanyun.sql;

import com.kanyun.sql.core.ColumnValueConvert;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.sql.func.AbstractFuncSource;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.Lex;
import org.apache.calcite.config.NullCollation;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.model.JsonJdbcSchema;
import org.apache.calcite.model.JsonSchema;
import org.apache.calcite.model.JsonTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.util.ConversionUtil;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * SQL执行类
 */
public class SqlExecute {

    private static final Logger log = LoggerFactory.getLogger(SqlExecute.class);

    static Properties info;

    static CalciteConnection calciteConnection;

    static {
        System.setProperty("saffron.default.charset", ConversionUtil.NATIVE_UTF16_CHARSET_NAME);
        System.setProperty("saffron.default.nationalcharset", ConversionUtil.NATIVE_UTF16_CHARSET_NAME);
        System.setProperty("saffron.default.collation.name", ConversionUtil.NATIVE_UTF16_CHARSET_NAME + "$en_US");
        info = new Properties();
        info.setProperty(CalciteConnectionProperty.LEX.camelName(), Lex.JAVA.name());
        info.setProperty(CalciteConnectionProperty.DEFAULT_NULL_COLLATION.camelName(), NullCollation.LAST.name());
        info.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), "false");
        info.setProperty(CalciteConnectionProperty.PARSER_FACTORY.camelName(), "org.apache.calcite.sql.parser.impl.SqlParserImpl#FACTORY");
    }

    /**
     * 构建CalciteConnection,这里需要注意的是,除非是添加/删除了Schema外,CalciteConnection只初始化一次
     * 为什么只初始化一次,因为每次初始化都需要重新执行com.kanyun.sql.core.JsonSchemaFactory类
     * 这个可能会耗费性能
     *
     * @param modelJson
     * @throws SQLException
     */
    public static void buildCalciteConnection(String modelJson) throws SQLException {
        if (calciteConnection != null) return;
//        另一种获取链接的方法
//        info.put("model",modelJson)
//        Connection connection =  DriverManager.getConnection("jdbc:calcite:", info);
//        获取连接,此操作将初始化数据库,将调用 com.kanyun.sql.core.JsonSchemaFactory
        Connection connection = DriverManager.getConnection("jdbc:calcite:model=inline:" + modelJson, info);
//       转换为Calcite连接
        calciteConnection = connection.unwrap(CalciteConnection.class);
    }

    /**
     * 重建Calcite连接,将触发JsonSchemaFactory.create()方法
     */
    public static void rebuildCalciteConnection(String modelJson) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:calcite:model=inline:" + modelJson, info);
//       转换为Calcite连接
        calciteConnection = connection.unwrap(CalciteConnection.class);
    }

    /**
     * 执行SQL
     *
     * @param modelJson     calcite model.json文件
     * @param sql           待执行的sql
     * @param defaultSchema 默认Schema
     * @return
     * @throws SQLException
     */
    public static Pair<Map<String, Integer>, List<Map<String, Object>>> execute(String modelJson, String defaultSchema, String sql) throws Exception {
        TimeUnit.SECONDS.sleep(2);
        buildCalciteConnection(modelJson);
//        取出rootSchema,需要注意的是rootSchema不等于model.json中的defaultSchema,rootSchema一般是空
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
//        注册自定义函数(Calcite中内置的函数主要在org.apache.calcite.sql.fun.SqlStdOperatorTable中,包括常见的算术运算符、时间函数等)
        AbstractFuncSource.registerFunction(rootSchema);
//        动态设置defaultSchema(之所以动态设置,是避免重新获取Connection,因为使用ModelJson获取Connection浪费性能)
        calciteConnection.setSchema(defaultSchema);
        if (sql.endsWith(";")) {
//            去掉sql中最后的分号
            sql = sql.substring(0, sql.length() - 1);
        }
        Statement statement = calciteConnection.createStatement();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("准备执行SQL：[{}]", sql);
//        SqlParseHelper.getKind(sql);
//        执行SQL脚本
        ResultSet resultSet = statement.executeQuery(sql);
        stopWatch.stop();
        QueryInfoHolder.setQueryCost(stopWatch.getTime(TimeUnit.MILLISECONDS));
        QueryInfoHolder.setRecordCount(resultSet.getFetchSize());
        log.info("SQL执行用时：[{}毫秒] ", stopWatch.getTime(TimeUnit.MILLISECONDS));
//        得到执行结果,取出元数据信息,并得到字段数量
        int columnCount = resultSet.getMetaData().getColumnCount();
        Map<String, Integer> columnInfos = new HashMap();
//        注意 resultSet的get()方法,要从1开始,而不是0
        for (int i = 1; i <= columnCount; i++) {
//            如果sql中未使用AS来指定别名,则getColumnLabel返回的值将与getColumnName方法返回的值相同
            String columnLabel = resultSet.getMetaData().getColumnLabel(i);
//            这里要判断一下map中是否存在字段名称一致的情况,因为会存在两张表中存在同名字段,如果不设置别名的话,map将被覆盖
            if (columnInfos.containsKey(columnLabel)) {
//                遇见同名的情况,修改Label为 columnLabel(table)
                columnLabel = columnLabel +"("+resultSet.getMetaData().getTableName(i)+")" ;

            }
//            字段类型,见类 java.sql.Types
            int columnType = resultSet.getMetaData().getColumnType(i);
            columnInfos.put(columnLabel, columnType);
        }
//        实例化字段名与字段值转换取出关系类
        ColumnValueConvert columnValueConvert = new ColumnValueConvert(columnInfos);
        List<Map<String, Object>> data = new ArrayList<>();
        log.info("SQL执行完成,开始抽取数据");
//        resultSet.next()将会调用com.kanyun.sql.core.JsonEnumerator.current()/moveNext()方法
        while (resultSet.next()) {
            Map<String, Object> row = columnValueConvert.extractRowData(resultSet);
            data.add(row);
        }
        return Pair.of(columnInfos, data);
    }
}
