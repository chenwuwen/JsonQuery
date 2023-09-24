package com.kanyun.sql;

import com.kanyun.sql.analysis.SqlAnalyzerFactory;
import com.kanyun.sql.ds.DataSourceConnectionPool;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 抽象的SQL执行器
 */
public abstract class AbstractSqlExecutor {

    private static final Logger log = LoggerFactory.getLogger(AbstractSqlExecutor.class);

    /**
     * SQL分析
     */
    private static String analyzeSql(CalciteConnection connection, String defaultSchema, String sql) throws Exception {
        log.info("准备执行SQL分析(责任链),原始SQL:[{}]", sql);
//        SqlParseHelper.getKind(sql);
        if (StringUtils.isNotEmpty(defaultSchema)) {
            sql = SqlAnalyzerFactory.analysisSql(sql, connection.getRootSchema().getSubSchema(defaultSchema));
        } else {
            sql = SqlAnalyzerFactory.analysisSql(sql, connection.getRootSchema());
        }
        log.info("准备执行分析后的SQL:[{}],线程ID：{}", sql, Thread.currentThread().getId());
        return sql;
    }

    /**
     * 执行SQL
     * 注意Calcite SQL 不支持双引号,在SQL语法中双引号用于标识表名,列名等标识符,但是在Calcite中,
     * 双引号不被视为标识符的一部分,因此不支持双引号(暂时没有找到可以转义字符的工具)
     *
     * @param modelJson     calcite model.json文件
     * @param sql           待执行的sql
     * @param defaultSchema 默认Schema
     * @return
     * @throws SQLException
     */
    protected Pair<Map<String, Integer>, List<Map<String, Object>>> executeSql(String modelJson, String defaultSchema, String sql) {
//        使用连接池获取CalciteConnection
        CalciteConnection connection = DataSourceConnectionPool.getConnection();
        try {
//            动态设置defaultSchema(之所以动态设置,是避免重新获取Connection,因为如果再次使用model.json中的defaultSchema来获取Connection,会浪费性能)
            connection.setSchema(defaultSchema);
            if (sql.endsWith(";")) {
//              去掉sql中最后的分号
                sql = sql.substring(0, sql.length() - 1);
            }
            sql = analyzeSql(connection, defaultSchema, sql);
            return actualityExecute(connection, sql);
        } catch (Exception e) {
            log.error("");
        }finally {
//            归还连接到练级吃
            DataSourceConnectionPool.recoveryConnection(connection);
        }
        return null;
    }

    /**
     * 真实执行SQL
     * @param connection
     * @param sql
     * @return
     * @throws Exception
     */
    abstract Pair<Map<String, Integer>, List<Map<String, Object>>> actualityExecute(CalciteConnection connection,String sql)  throws Exception;


}
