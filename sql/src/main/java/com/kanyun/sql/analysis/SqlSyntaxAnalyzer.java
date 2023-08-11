package com.kanyun.sql.analysis;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.tools.Planner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sql语法分析
 */
public class SqlSyntaxAnalyzer extends AbstractSqlAnalysis {
    private static Logger log = LoggerFactory.getLogger(SqlSyntaxAnalyzer.class);

    @Override
    protected String analysis(Planner planner, String sql) throws Exception {
        log.debug("开始进行SQL语法分析:[{}]", sql);
        SqlNode sqlNode = planner.parse(sql);
        planner.validate(sqlNode);
        log.info("SQL分析[{}]语法校验通过", sql);
        return sql;
    }
}
