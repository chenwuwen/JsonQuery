package com.kanyun.sql.analysis;

import org.apache.calcite.sql.SqlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sql语法分析
 */
public class SqlSyntaxAnalyzer extends AbstractSqlAnalysis {
    private static Logger log = LoggerFactory.getLogger(SqlSyntaxAnalyzer.class);

    @Override
    protected String analysis(String sql) throws Exception{
        log.debug("开始进行SQL语法分析:[{}]", sql);
//        SqlNode sqlNode = planner.parse(sql);
//        planner.validate(sqlNode);
        return sql;
    }
}
