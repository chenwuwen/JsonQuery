package com.kanyun.sql.ds.pool;

import com.kanyun.sql.func.AbstractFuncSource;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.Lex;
import org.apache.calcite.config.NullCollation;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.util.ConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Calcite连接构建器
 */
public class CalciteConnectionBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CalciteConnectionBuilder.class);

    static Properties info;

    static {
//        也可以通过saffron.properties文件配置属性
        System.setProperty("saffron.default.charset", ConversionUtil.NATIVE_UTF16_CHARSET_NAME);
        System.setProperty("saffron.default.nationalcharset", ConversionUtil.NATIVE_UTF16_CHARSET_NAME);
        System.setProperty("saffron.default.collation.name", ConversionUtil.NATIVE_UTF16_CHARSET_NAME + "$en_US");
        info = new Properties();
        info.setProperty(CalciteConnectionProperty.LEX.camelName(), Lex.JAVA.name());
        info.setProperty(CalciteConnectionProperty.DEFAULT_NULL_COLLATION.camelName(), NullCollation.LAST.name());
//        是否忽略大小写
        info.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), "true");
        info.setProperty(CalciteConnectionProperty.PARSER_FACTORY.camelName(), "org.apache.calcite.sql.parser.impl.SqlParserImpl#FACTORY");
    }

    /**
     * 构建CalciteConnection,这里需要注意的是,除非是添加/删除了Schema外,CalciteConnection只初始化一次
     * 为什么只初始化一次?因为每次初始化都需要重新执行{@link com.kanyun.sql.core.JsonSchemaFactory#create(SchemaPlus, String, Map)}
     * 这个会耗费性能,也可以创建连接池，初始化创建多个链接(池化),满足并发使用Connection的需求
     *
     * @param modelJson
     * @throws SQLException
     */
    public static CalciteConnection build(String modelJson) {
        logger.info("准备创建Calcite连接对象");
//        创建连接并实例化schema/table,亦可以通过编程方法创建schema/table
        CalciteConnection calciteConnection = null;
        try {
//            另一种获取链接的方法
//            info.put("model", modelJson)
//            Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
//            获取连接,此操作将初始化数据库,将调用 com.kanyun.sql.core.JsonSchemaFactory
            Connection connection = DriverManager.getConnection("jdbc:calcite:model=inline:" + modelJson, info);
//            转换为Calcite连接
            calciteConnection = connection.unwrap(CalciteConnection.class);
//            自定义函数注册
            registerFunc(calciteConnection);
            return calciteConnection;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }


    /**
     * 注册函数,仅在创建/重建 连接时注册,直接注册到RootSchema
     * 这里把函数注册到RootSchema是因为
     * 在实际应用中，RootSchema是根所有schema的路径，所有注册在RootSchema上的table或者是udf/udaf都是全局的，
     * 意思就是说可以被SubSchema直接使用，而注册在SubSchema里的table或者是udf，则在使用中必须声明是哪个SubSchema拥有的。
     */
    private static void registerFunc(CalciteConnection calciteConnection) {
//        取出rootSchema,需要注意的是rootSchema不等于model.json中的defaultSchema,rootSchema一般是空
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
//        注册自定义函数(Calcite中内置的函数主要在org.apache.calcite.sql.fun.SqlStdOperatorTable中,包括常见的算术运算符、时间函数等)
        AbstractFuncSource.registerFunction(rootSchema);
//        注册聚合函数
        AbstractFuncSource.registerAggFunction(rootSchema);
    }

}
