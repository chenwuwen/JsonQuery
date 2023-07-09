package com.kanyun.sql.analysis;

import org.apache.calcite.sql.parser.SqlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

/**
 * SQL重写分析器
 */
public class SqlRewriteAnalyzer extends AbstractSqlAnalysis {
    private static Logger log = LoggerFactory.getLogger(SqlRewriteAnalyzer.class);

    @Override
    protected String analysis(String sql) {
        log.debug("开始进行SQL重写分析:[{}]", sql);

        return sql;
    }
}
