package com.kanyun.sql;

import com.kanyun.sql.analysis.SqlAnalyzerFactory;
import com.kanyun.sql.core.ColumnValueConvert;
import com.kanyun.sql.ds.DataSourceConnectionPool;
import com.kanyun.sql.func.AbstractFuncSource;
import com.kanyun.sql.func.ExternalFuncClassLoader;
import org.apache.calcite.avatica.AvaticaResultSet;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.Lex;
import org.apache.calcite.config.NullCollation;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteResultSet;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.rel.metadata.JaninoRelMetadataProvider;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.util.ConversionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SQL执行类
 * https://blog.csdn.net/weixin_45371411/article/details/128961679
 */
public class SqlExecutor extends AbstractSqlExecutor{

    private static final Logger log = LoggerFactory.getLogger(SqlExecutor.class);

    public static Pair<Map<String, Integer>, List<Map<String, Object>>> execute(String modelJson, String defaultSchema, String sql) {
        return new SqlExecutor().executeSql(modelJson, defaultSchema, sql);
    }

    @Override
    Pair<Map<String, Integer>, List<Map<String, Object>>> actualityExecute(CalciteConnection connection, String sql)  throws Exception{
//        创建Statement,作用于创建出来的ResultSet
//        第一个参数:允许在列表中向前或向后移动，甚至可以进行特定定位 第二个参数:指定不可以更新 ResultSet(缺省值)
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
//        执行SQL脚本,并获取结果集,注:resultSet在没有调用next()方法时,getRow()的值为0,当前游标指向第一行的前一行
        ResultSet resultSet = statement.executeQuery(sql);

//        CachedRowSet cachedRowSet = new CachedRowSetImpl();
//        cachedRowSet.populate(resultSet);
//        cachedRowSet.getMaxRows();
        log.info("SQL执行耗时:[{}毫秒]线程ID：{} ", stopWatch.getTime(TimeUnit.MILLISECONDS), Thread.currentThread().getId());
//        获取查询记录数,注意使用类型如Long类型的可变与不可变
        AtomicLong recordCount = new AtomicLong(0);
//        遍历结果集,抽取数据及元数据信息,注意结果集遍历后,ResultSet的游标已指向最后一行的后一行
        Pair<Map<String, Integer>, List<Map<String, Object>>> executeResult = extractMetaInfoAndResult(resultSet, recordCount);
        stopWatch.stop();

//        设置线程变量:查询耗时
        QueryInfoHolder.setQueryCost(stopWatch.getTime(TimeUnit.MILLISECONDS));
//       设置线程变量:得到当前行号,也就是记录数,注意resultSet.getFetchSize()方法不是获得记录数,而是获得每次抓取的记录数,默认是0,也就是说不限制,可以用setFetchSize()来设置
        QueryInfoHolder.setRecordCount(statisticsResultOfCount(resultSet, recordCount));
        QueryInfoHolder.setExecuteSql(sql);
        resultSet.close();
        statement.close();
        connection.close();
        return executeResult;
    }

    /**
     * 延时执行
     *
     * @throws InterruptedException
     */
    private static void delayedExecute() throws Exception {
//        实际上janino框架是会取上下文类加载器的org.codehaus.janino.SimpleCompiler#parentClassLoader,但是calcite的
//        org.apache.calcite.rel.metadata.JaninoRelMetadataProvider#compile()方法调用了
//        SimpleCompiler的setParentClassLoader()方法且参数传递的是JaninoRexCompiler类的类加载器,即AppClassLoader
//        而这个AppClassLoader的来源通过 org.apache.calcite.rel.metadata.JaninoRelMetadataProvider#compile()方法
//        来看发现是 JaninoRelMetadataProvider类的类加载器
//        由于sql执行是异步操作,因此需要在此处重新设置线程上下文类加载器
        Thread.currentThread().setContextClassLoader(ExternalFuncClassLoader.getInstance());
        ClassLoader classLoader = JaninoRelMetadataProvider.class.getClassLoader();
//        org.codehaus.commons.compiler.properties
//        使用线程上下文类加载器加载类的前提是:当前线程已经通过 Thread.currentThread().setContextClassLoader()设置了类加载器,否则就是默认的AppClassLoader
//        加以验证看设置的线程上下文类加载器是否可以加载外部的自定义类
        Class<?> clazz = Class.forName("com.kanyun.func.string.StringFuncUtil", false, Thread.currentThread().getContextClassLoader());
//        Method reverseMethod = clazz.getMethod("reverse", String.class);
//        Object invoke = reverseMethod.invoke(clazz.newInstance(),"123");
//        log.info(invoke.toString());


        log.info("设置线程上下文类加载器,当前线程ID:{}", Thread.currentThread().getId());
        TimeUnit.SECONDS.sleep(0);
    }

    /**
     * 统计查询结果记录数
     * 调用此方法时 ResultSet 已遍历过,游标在最后一行的后一行
     *
     * @param resultSet 注意如果ResultSet对象的实例是CalciteResultSet类型,则其不支持 last()/beforeFirst()等函数操做
     *                  否则抛出 java.sql.SQLFeatureNotSupportedException异常
     *                  {@link AvaticaResultSet#last()}
     *                  {@link AvaticaResultSet#first()}
     *                  {@link AvaticaResultSet#beforeFirst()}
     * @return
     * @throws SQLException
     */
    private static Long statisticsResultOfCount(ResultSet resultSet, AtomicLong recordCount) throws SQLException {
        long totalRecord = 0;
        if (resultSet instanceof CalciteResultSet) {
            CalciteResultSet calciteResultSet = (CalciteResultSet) resultSet;
//            由于游标已执行最后一行的后一行,因此calciteResultSet.getRow()为0
//            record = calciteResultSet.getRow();
            totalRecord = recordCount.get();
        } else {
//            结果集移到最后一行。这里注意如果需要获取查询记录数,有两种方法,1是遍历结果集累加,2游标执行结果集最后一行并取到行数
            resultSet.last();
            totalRecord = resultSet.getRow();
//            如果还要用结果集,将游标移动到结果集的初始位置，即在第一行之前,这里注意游标能不能移动要看在创建Statement时用的是什么参数
            resultSet.beforeFirst();
        }
        return totalRecord;
    }

    /**
     * 抽取SQL执行后的元数据信息(字段->类型) 及数据信息
     *
     * @param resultSet SQL执行后的结果集,需要保证为原始结果,即游标在第一行之前
     * @return
     * @throws Exception
     */
    private static Pair<Map<String, Integer>, List<Map<String, Object>>> extractMetaInfoAndResult(ResultSet resultSet, AtomicLong recordCount) throws Exception {
//        得到执行结果,取出元数据信息,并得到字段数量
//        注：这里取出的字段数量,com.kanyun.sql.core.JsonTable设置的字段数量,并不准确,除非确保每行的字段数一致,对于关系型数据库来说这个是一定的,而对于解析的json文件来说是不一定的,因为json文件很可能每个Json元素的子元素数据不一致
        int columnCount = resultSet.getMetaData().getColumnCount();
//        这里使用LinkedHashMap主要为了保证返回给界面的字段顺序与界面检查表字段的顺序一致,因为ResultSet中的字段元数据与界面检查表字段的顺序一致
        Map<String, Integer> columnInfos = new LinkedHashMap();
//        注意 resultSet的get()方法,要从1开始,而不是0
        for (int i = 1; i <= columnCount; i++) {
//            如果sql中未使用AS来指定别名,则getColumnLabel返回的值将与getColumnName方法返回的值相同
            String columnLabel = resultSet.getMetaData().getColumnLabel(i);
//            这里要判断一下map中是否存在字段名称一致的情况,因为会存在两张表中存在同名字段,如果不设置别名的话,map将被覆盖
            if (columnInfos.containsKey(columnLabel)) {
//                遇见同名的情况,修改Label为 columnLabel(table)
                columnLabel = columnLabel + "(" + resultSet.getMetaData().getTableName(i) + ")";

            }
//            字段类型,见类 java.sql.Types
            int columnType = resultSet.getMetaData().getColumnType(i);
            columnInfos.put(columnLabel, columnType);
        }
//        实例化字段名与字段值转换取出关系类
        ColumnValueConvert columnValueConvert = new ColumnValueConvert(columnInfos);
        List<Map<String, Object>> data = new ArrayList<>();
        log.info("SQL执行完成,开始抽取数据");
//        resultSet.next()将会调用com.kanyun.sql.core.JsonEnumerator.current()/moveNext()方法
        while (resultSet.next()) {
            Map<String, Object> row = columnValueConvert.extractRowData(resultSet);
            data.add(row);
            recordCount.incrementAndGet();
        }
        return Pair.of(columnInfos, data);
    }
}
