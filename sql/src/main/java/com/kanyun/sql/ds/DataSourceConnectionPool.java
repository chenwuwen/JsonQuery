package com.kanyun.sql.ds;

import com.kanyun.sql.ds.pool.CalciteConnectionFactory;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.commons.pool2.proxy.JdkProxySource;
import org.apache.commons.pool2.proxy.ProxiedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.time.Duration;

/**
 * Calcite连接池
 */
public class DataSourceConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(CalciteConnectionFactory.class);


    /**
     * 定义连接池(这里使用了代理连接池)
     * 每次代理对象被调用时(每个方法调用前),都会执行CalciteConnectionPooledObject里的use方法
     */
    private static GenericObjectPool<CalciteConnection> connectionPool = null;


    /**
     * 初始化连接池
     * 注意:连接池内的连接时延迟创建的(使用时创建)
     * @param modelJson
     */
    public static void initConnectionPool(String modelJson) {
        CalciteConnectionFactory calciteConnectionFactory = new CalciteConnectionFactory(modelJson);
        AbandonedConfig abandonedConfig = createConnectionAbandonedCondition();
        GenericObjectPoolConfig<CalciteConnection> genericObjectPoolConfig = new GenericObjectPoolConfig();
//        定义一个常规对象池
        connectionPool = new GenericObjectPool(calciteConnectionFactory, genericObjectPoolConfig, abandonedConfig);
//        设置对象池中允许的最大对象数量
        connectionPool.setMaxTotal(3);
//        设置最大空闲数
        connectionPool.setMaxIdle(2);
//        设置最小空闲数
        connectionPool.setMinIdle(3);
//        该选项用于指定在从连接池借用连接对象时是否执行验证操作,默认为false.当设置为true时,在每次借用连接对象之前,连接池会调用PooledObjectFactory的validateObject()方法对连接对象进行验证.如果连接对象不可用,则会将其移除,并尝试重新创建一个新的连接对象
        connectionPool.setTestOnBorrow(true);
//        该选项用于指定空闲连接是否进行验证操作,默认为false.当设置为true时,在连接池中的空闲连接将周期性地进行验证操作.如果连接对象不可用,则会将其移除
        connectionPool.setTestWhileIdle(true);
//        设置最大等待时间,即获取连接的最大等待时间,默认是-1,表示永不超时,直到有空闲连接
        connectionPool.setMaxWait(Duration.ofSeconds(3));
//        设置在创建获取归还空闲检测的时候,是否对池化对象进行有效性检测
        connectionPool.setTestOnCreate(false);
        connectionPool.setTestOnReturn(true);
        connectionPool.setTestOnBorrow(false);

////        创建代理对象,使用jdk自带的代理机制
//        JdkProxySource<Connection> proxySource = new JdkProxySource<>(Connection.class.getClassLoader(), new Class[]{Connection.class});
////        创建代理连接池
//        connectionPool = new ProxiedObjectPool(genericObjectPool, proxySource);
        logger.info("Calcite连接池初始化完成");
    }


    /**
     * 刷新连接池,通常当Schema发生变化或注册函数发生变化时需要刷新
     *
     * @param modelJson
     */
    public static void refreshConnectionPool(String modelJson) throws Exception {
//        清空对象池中的所有资源对象，并将对象池恢复到初始状态
        connectionPool.clear();
        initConnectionPool(modelJson);
    }

    /**
     * 从连接池中取出连接
     * 取出的连接使用完毕后,一般需要显示的归还{@link org.apache.commons.pool2.ObjectPool#returnObject(Object)}
     * 可以通过设置连接池的配置选项来实现连接对象的隐式归还.这样无需显式调用 returnObject() 方法来归还连接对象
     * 而是由连接池自动管理连接的借用和归还。
     * 要实现连接对象的隐式归还，需要注意以下两个配置选项：
     * config.setTestOnBorrow(true)/config.setTestWhileIdle(true);
     * 通过设置上述选项,连接池会自动在借用连接对象之前和空闲连接周期性验证的过程中,检测连接对象的可用性并回收废弃的连接对象.
     * 这样即使没有显式调用 returnObject() 方法,连接对象也会被隐式归还到连接池中供下次使用
     * 需要注意的是,隐式归还依赖于配置选项的设置和连接对象的验证过程.确保连接对象的有效性验证逻辑是正确的,并根据需求合理配置连接池的验证选项
     * @return
     */
    public static CalciteConnection getConnection() {
        try {
            logger.info("当前连接池->空闲对象数量:{},被借出对象数量{}", connectionPool.getNumIdle(), connectionPool.getNumActive());
//            从对象池中获取对象(
            CalciteConnection connection = connectionPool.borrowObject();
//            这里输出class com.sun.proxy.$Proxy0,说明这是一个代理对象
            logger.info("从连接池中获取连接对象：{}", connection);
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 回收连接
     * @param connection
     */
    public static void recoveryConnection(CalciteConnection connection) {
        connectionPool.returnObject(connection);
    }


    /**
     * 配置连接废弃的条件,主要是配置{@link AbandonedConfig}类
     * {@link AbandonedConfig}用于配置对象池中的资源被认为是"废弃"的条件
     * {@link AbandonedConfig}允许你设置一些配置选项，以检测并处理这种资源泄漏情况
     * 在对象池中,当资源（如数据库连接、线程等）不再使用时,它们应该返回到对象池中以供其他代码复用。
     * 但是,有时候资源可能会被错误地丢弃或泄漏，没有正确返回到对象池中，这会导致资源的浪费和系统性能问题
     */
    private static AbandonedConfig createConnectionAbandonedCondition() {
        AbandonedConfig abandonedConfig = new AbandonedConfig();
//        如果使用了代理对象,一定设置useUsageTracking属性为true,这样代理对象的方法被调用时,才会去调用DefaultPooledObject的use()方法
        abandonedConfig.setUseUsageTracking(true);
//        设置是否在借用资源时检测废弃状态,默认为false.
        abandonedConfig.setRemoveAbandonedOnBorrow(false);
//        设置是否记录废弃资源的日志信息,默认为false.
        abandonedConfig.setLogAbandoned(true);
//        设置是否在维护任务运行时检测废弃状态,默认为false.
        abandonedConfig.setRemoveAbandonedOnMaintenance(false);
//        设置资源被认为是废弃的超时时间
        abandonedConfig.setRemoveAbandonedTimeout(Duration.ofSeconds(3));
        return abandonedConfig;
    }

}
