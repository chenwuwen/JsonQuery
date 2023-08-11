package com.kanyun.sql.analysis;

import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.Planner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SQL重写分析器
 */
public class SqlRewriteAnalyzer extends AbstractSqlAnalysis {
    private static Logger log = LoggerFactory.getLogger(SqlRewriteAnalyzer.class);

    @Override
    protected String analysis(Planner planner, String sql) throws SqlParseException {
        log.debug("开始进行SQL重写分析:[{}]", sql);
        SqlNode sqlNode = planner.parse(sql);

        CalciteSqlUtils.handlerSqlTableAlias(SqlDialect.DatabaseProduct.CALCITE, sql);

        return sql;
    }

}
