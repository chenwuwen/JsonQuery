package com.kanyun.sql.analysis;

import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.config.Lex;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;

/**
 * SQL分析责任链抽象类
 * 一般来说 Calcite 解析 SQL 有下面几步:
 * 1.解析（Parser），Calcite 通过Java CC 将 SQL 解析成未经校验的的 AST
 * 2.验证（Validate），该步主要作用是校验上一步中的 AST 是否合法，比如如验证 SQL scheme、字段、函数等是否存在，SQL 语句是否合法等等，此步完成之后就生成了 RelNode 树
 * 3.优化（Optimize），该步主要作用是优化 RelNode 树，把它转化成物理执行计划。涉及的 SQL 规则优化一般有两种：基于规则的优化（RBO）、基于成本的优化（CBO）这一步原则上说是可选的，经过 Validate 后的 RelNode 树实际就可以直接转化物理执行计划，但现代的 SQL 解析器基本上都有这一步，目的是优化 SQL 执行计划。该步骤得到的结果是物理执行计划。
 * 4.执行（Execute），这一步主要做的是把物理执行计划转换成可在特定平台执行的程序。如 Hive 、Flink 都在此阶段将物理执行计划 CodeGen 生成相应的可执行代码
 */
public abstract class AbstractSqlAnalysis {

    /**
     * Calcite计划器,计划引擎的主要功能是不断触发优化规则，直至达到最优结果
     * Calcite提供两种类型的计划器,用户也可以按照 Calcite 规范，自定义计划引擎
     * HepPlanner:启发式计划器,应用一组规则来优化查询,它迭代应用规则,在每次迭代中选择具有最低成本的计划
     * VolcanoPlanner:基于成本的计划器,应用一组规则用来最小化执行查询的成本
     * planner内部是有状态的(只能由一种状态转换为另一种状态),使用后应调用其close()方法,或继续转换到planner的下一个状态
     * 否则可能报错:(java.lang.IllegalArgumentException: cannot move to STATE_2_READY from STATE_4_VALIDATED)
     * {@link org.apache.calcite.prepare.PlannerImpl#ensure()}
     * 注意:该成员变量应设置为静态变量,因为它是由子类进行初始化的,如果该抽象类存在多个子类(A/B)
     * 且该成员变量不被static修饰,则由子类A初始化的父类成员变量,子类B是无法使用的,子类B获取到的父类成员变量为null
     * 但是设置为static时存在线程安全问题,当多个线程同时操作静态的Planner时,可能会改变Planner的内部状态或属性
     * 解决方案是：每个子类通过抽象方法接收一个Planner的实例
     */
    protected Planner planner;

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
     * SQL分析的抽象方法,外部不直接调用该方法,应调用 {@link AbstractSqlAnalysis#beginAnalysis(String, SchemaPlus)}
     *
     * @param sql     有可能是原始SQL,也有可能是被上一个分析器修该过的SQL
     * @param planner 查询计划器
     * @return 需要返回一个SQL, 如果没有对SQL进行改写, 则返回原始SQL, 如果对SQL进行了改写, 则返回改写后的SQL
     */
    protected abstract String analysis(Planner planner, String sql) throws Exception;

    /**
     * 判断当前分析器是否需要继续分析
     *
     * @param sql     有可能是原始SQL,也有可能是被上一个分析器修该过的SQL
     * @param planner 查询计划器
     * @return
     */
    private String continueAnalyzer(Planner planner, String sql) throws Exception {
        if (nextSqlAnalyzer != null) {
//            在继续执行分析前,先调用计划器的close()方法,以恢复计划器内部状态
            planner.close();
            String afterAnalysisSql = nextSqlAnalyzer.analysis(planner, sql);
//            这里使用递归表示,如有下一个分析器,则继续执行分析
            return nextSqlAnalyzer.continueAnalyzer(planner, afterAnalysisSql);
        }
//        所有分析器执行完毕,调用计划器的close()方法
        planner.close();
        return sql;
    }

    /**
     * 开始分析执行SQL,分析后继续执行其他分析器的分析内容
     * 这里的一个好处是,当定义好一个分析链路时 如 A->B->C->D
     * 既可以执行 A->B->C->D 链路
     * 也可以执行 B->C->D 链路
     * 只需要 选择合适的分析器即可,如 A.beginAnalysis() / B.beginAnalysis()。而无需调整链路
     * client直接调用该方法
     *
     * @param originSql     原始SQL(用户输入的SQL)
     * @param defaultSchema
     * @return
     */
    public String beginAnalysis(String originSql, SchemaPlus defaultSchema) throws Exception {
        initPlanner(defaultSchema);
        String afterAnalysisSql = analysis(planner, originSql);
        return continueAnalyzer(planner, afterAnalysisSql);
    }

    /**
     * 初始化Planner
     * Frameworks.getPlanner()是用于获取默认规划器（Planner）的静态方法
     * 通过调用Frameworks.getPlanner()方法，可以获取一个默认的规划器对象，该对象用于处理和优化查询的计划。规划器负责根据给定的查询语句和数据模型，生成最优的执行计划，以提高查询的性能和效率
     *
     * @param defaultSchema 即 当前默认的schema(不能为空,可以为rootSchema)
     *                      如果sql中包含schemaName 如:select * from user.class 则defaultSchema可以为rootSchema
     *                      如果sql中不包含schemaName 如:select * from class 则defaultSchema只能为user,否则会报错class找不到
     */
    private void initPlanner(SchemaPlus defaultSchema) {
        planner = Frameworks.getPlanner(Frameworks.newConfigBuilder()
//                parserConfig()方法用于设置SQL解析器的配置
                .parserConfig(
                        SqlParser.config()
//                              忽略SQL大小写,calcite默认会转换为大写(大小写敏感)
                                .withCaseSensitive(false)
//                               指定了使用MySQL的词法分析器
                                .withLex(Lex.MYSQL)
                                .withQuoting(Quoting.BACK_TICK)
                                .withQuotedCasing(Casing.TO_LOWER)
                                .withUnquotedCasing(Casing.TO_LOWER)
                                .withConformance(SqlConformanceEnum.MYSQL_5))
//                planner不设置defaultSchema,sql校验会报错
                .defaultSchema(defaultSchema)
                .build());
    }
}
