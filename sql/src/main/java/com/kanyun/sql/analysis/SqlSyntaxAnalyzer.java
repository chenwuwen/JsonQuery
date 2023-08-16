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
//        1.解析（Parser），Calcite 通过Java CC 将 SQL 解析成未经校验的的 AST
        SqlNode sqlNode = planner.parse(sql);
//        2.验证（Validate），该步主要作用是校验上一步中的 AST 是否合法，比如如验证 SQL scheme、字段、函数等是否存在，SQL 语句是否合法等等，此步完成之后就生成了 RelNode 树
        planner.validate(sqlNode);
        log.info("SQL分析[{}]语法校验通过", sql);
        return sql;
    }
}
