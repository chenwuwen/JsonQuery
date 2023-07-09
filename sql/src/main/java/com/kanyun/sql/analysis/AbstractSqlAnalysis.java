package com.kanyun.sql.analysis;

import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;

/**
 * SQL分析责任链抽象类
 */
public abstract class AbstractSqlAnalysis {


    protected Planner planner = Frameworks.getPlanner(Frameworks.newConfigBuilder()
//           忽略SQL大小写,calcite默认会转换为大写
            .parserConfig(SqlParser.config().withCaseSensitive(false)).build());

    /**
     * 下一个分析器
     */
    AbstractSqlAnalysis nextSqlAnalyzer;

    /**
     * 设置下一个分析器
     *
     * @param nextSqlAnalyzer
     */
    protected void setNextSqlAnalyzer(AbstractSqlAnalysis nextSqlAnalyzer) {
        this.nextSqlAnalyzer = nextSqlAnalyzer;
    }

    /**
     * SQL分析的抽象方法,外部不直接调用该方法,应调用 {@link AbstractSqlAnalysis#beginAnalysis(String)}
     *
     * @param sql 有可能是原始SQL,也有可能是被上一个分析器修该过的SQL
     * @return 需要返回一个SQL, 如果没有对SQL进行改写, 则返回原始SQL, 如果对SQL进行了改写, 则返回改写后的SQL
     */
    protected abstract String analysis(String sql) throws Exception;

    /**
     * 判断当前分析器是否需要继续分析
     *
     * @param sql 有可能是原始SQL,也有可能是被上一个分析器修该过的SQL
     * @return
     */
    private String continueAnalyzer(String sql) throws Exception {
        if (nextSqlAnalyzer != null) {
            String analysisSql = nextSqlAnalyzer.analysis(sql);
//            这里使用递归表示,如有下一个分析器,则继续执行分析
            return nextSqlAnalyzer.continueAnalyzer(analysisSql);
        }
        return sql;
    }

    /**
     * 开始分析执行SQL,分析后继续执行其他分析器的分析内容
     * 这里的一个好处是,当定义好一个分析链路时 如 A->B->C->D
     * 既可以执行 A->B->C->D 链路
     * 也可以执行 B->C->D 链路
     * 只需要 选择合适的分析器即可,如 A.beginAnalysis() / B.beginAnalysis()。而无需调整链路
     * client直接调用该方法
     * @param originSql
     * @return
     */
    public String beginAnalysis(String originSql) throws Exception {
        String sql = analysis(originSql);
        return continueAnalyzer(sql);
    }
}
