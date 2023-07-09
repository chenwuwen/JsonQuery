package com.kanyun.sql.analysis;

/**
 * SQL分析工厂类
 * 主要用来定义分析器责任链
 */
public class SqlAnalyzerFactory {

    /**
     * 分析SQL,先定义分析器链,在调用beginAnalysis()方法
     * @param sql
     * @return
     */
    public static String analysisSql(String sql) throws Exception {
        SqlSyntaxAnalyzer sqlSyntaxAnalyzer = new SqlSyntaxAnalyzer();
        SqlRewriteAnalyzer sqlRewriteAnalyzer = new SqlRewriteAnalyzer();
        sqlSyntaxAnalyzer.setNextSqlAnalyzer(sqlRewriteAnalyzer);
        return sqlSyntaxAnalyzer.beginAnalysis(sql);
    }
}
