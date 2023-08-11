package com.kanyun.sql.analysis;

import org.apache.calcite.tools.Planner;

/**
 * Sql元数据分析器
 */
public class SqlMetaDataAnalyzer extends AbstractSqlAnalysis{
    @Override
    protected String analysis(Planner planner, String sql) throws Exception {
        return sql;
    }
}
