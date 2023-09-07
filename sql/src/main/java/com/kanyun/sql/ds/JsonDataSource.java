package com.kanyun.sql.ds;

import com.kanyun.sql.func.AbstractFuncSource;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.Lex;
import org.apache.calcite.config.NullCollation;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.util.ConversionUtil;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * DataSource类用以获取Connection,池化
 */
public class JsonDataSource implements DataSource {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JsonDataSource.class);

    /**
     * 连接池,当需要获取连接时,从连接池中获取,并移除该连接,当调用连接的close()方法时,将连接重新放到连接池中
     * 可配合当前可用连接池和不可用连接池来实现最大/小连接数,当获取连接时如果可用连接池已用尽,然后创建新的连接并添加到不可用连接池
     * 当再次创建连接时,先判断指定的连接池大小和当前不可用连接池的大小之和是否超过最大连接数
     */
    private static final LinkedList<Connection> connectionPool = new LinkedList<>();

    static Properties info;

    private String modelJson;

    static {
//        也可以通过saffron.properties文件配置
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

    protected JsonDataSource(String modelJson) throws SQLException {
//        实例datasource前先清空连接池
        connectionPool.clear();
        this.modelJson = modelJson;
        for (int i = 0; i < 5; i++) {
            connectionPool.add(createConnection(modelJson));
        }
    }


    @Override
    public Connection getConnection() throws SQLException {
//        判断当前连接池的可用连接数量
        if (connectionPool.size() > 0) {
//            取出连接池的第一个连接
            return connectionPool.removeFirst();
        }
//        如果连接池中连接数量不足,则创建新的连接
        return createNewConnection(modelJson);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }


    /**
     * 创建可复用的连接
     *
     * @param modelJson
     * @return
     * @throws SQLException
     */
    private Connection createConnection(String modelJson) throws SQLException {
//        创建连接并实例化schema/table,亦可以通过编程方法创建schema/table
        Connection connection = DriverManager.getConnection("jdbc:calcite:model=inline:" + modelJson, info);
        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
//        函数注册
        registerFunc(calciteConnection);
        ClassLoader classLoader = calciteConnection.getClass().getClassLoader();
//        创建动态代理对象(代理CalciteConnection接口的子类)
        Connection connectionProxy = (Connection) Proxy.newProxyInstance(classLoader, new Class[]{CalciteConnection.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                    判断执行方法是否是close()方法,如果是:则增强,如果不是:则执行被代理者原本的方法
                if (method.getName().equals("close")) {
                    logger.debug("当前Connection是被代理的Connection,调用close()方法时,执行增强的close()方法");
//                    增强close(),将当前调用关闭方法的连接对象加到连接池中,实现修改close()方法
                    connectionPool.addLast((Connection) proxy);
                    return null;
                }
//                    不需要增强的方法,就不需要改动
                return method.invoke(proxy, args);
            }
        });
        return connectionProxy;
    }

    /**
     * 创建新的连接,该连接不可复用
     * @param modelJson
     * @return
     * @throws SQLException
     */
    private Connection createNewConnection(String modelJson) throws SQLException {
//       创建连接并实例化schema/table,亦可以通过编程方法创建schema/table
        Connection connection = DriverManager.getConnection("jdbc:calcite:model=inline:" + modelJson, info);
        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
//        函数注册
        registerFunc(calciteConnection);
        return calciteConnection;
    }

    /**
     * 注册函数,仅在创建/重建 连接时注册,直接注册到RootSchema
     * 这里把函数注册到RootSchema是因为
     * 在实际应用中，RootSchema是根所有schema的路径，所有注册在RootSchema上的table或者是udf/udaf都是全局的，
     * 意思就是说可以被SubSchema直接使用，而注册在SubSchema里的table或者是udf，则在使用中必须声明是哪个SubSchema拥有的。
     */
    public static void registerFunc(CalciteConnection calciteConnection) {
//        取出rootSchema,需要注意的是rootSchema不等于model.json中的defaultSchema,rootSchema一般是空
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
//        注册自定义函数(Calcite中内置的函数主要在org.apache.calcite.sql.fun.SqlStdOperatorTable中,包括常见的算术运算符、时间函数等)
        AbstractFuncSource.registerFunction(rootSchema);
//        注册聚合函数
        AbstractFuncSource.registerAggFunction(rootSchema);
    }
}
