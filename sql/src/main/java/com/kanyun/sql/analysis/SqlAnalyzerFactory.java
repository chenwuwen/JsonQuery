package com.kanyun.sql.analysis;

import org.apache.calcite.schema.SchemaPlus;

/**
 * SQL分析工厂类
 * 主要用来定义分析器责任链
 */
public class SqlAnalyzerFactory {

    /**
     * 分析SQL,先定义分析器链,再调用{@link #analysisSql(String, SchemaPlus)}方法
     * 定义好分析器链后 例：A->B->C->D-E
     * 如A调用beginAnalysis() 则执行顺序为 A->B->C->D-E
     * 如B调用beginAnalysis() 则执行顺序为 B->C->D-E
     * 如D调用beginAnalysis() 则执行顺序为 D-E
     * 当然话虽然是这么说,但是要看每个分析器之间有没有数据上的依赖关系,如果没有可以随意指定分析顺序,否则需要按照数据顺序来执行
     * @param sql
     * @return
     */
    public static String analysisSql(String sql, SchemaPlus defaultSchema) throws Exception {
        SqlSyntaxAnalyzer sqlSyntaxAnalyzer = new SqlSyntaxAnalyzer();
        SqlMetaDataAnalyzer sqlMetaDataAnalyzer = new SqlMetaDataAnalyzer();
        SqlRewriteAnalyzer sqlRewriteAnalyzer = new SqlRewriteAnalyzer();
        sqlSyntaxAnalyzer.setNextSqlAnalyzer(sqlMetaDataAnalyzer);
        sqlMetaDataAnalyzer.setNextSqlAnalyzer(sqlRewriteAnalyzer);
        return sqlSyntaxAnalyzer.beginAnalysis(sql, defaultSchema);
    }
}
