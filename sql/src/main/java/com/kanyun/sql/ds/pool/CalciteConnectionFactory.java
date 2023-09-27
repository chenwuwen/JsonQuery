package com.kanyun.sql.ds.pool;

import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * 管理池化对象的状态,比如创建,初始化,验证对象状态和销毁对象
 * 资源工厂
 * 对象的状态,可以查看枚举类 {@link org.apache.commons.pool2.PooledObjectState}
 * 一般来说{@link #activateObject(PooledObject)}和{@link #passivateObject(PooledObject)}是成对出现的.
 * 前者是在对象从对象池取出时做一些操作,后者是在对象归还到对象池做一些操作,可以根据自己的业务需要进行取舍
 */
public class CalciteConnectionFactory implements PooledObjectFactory<CalciteConnection> {

    private static final Logger logger = LoggerFactory.getLogger(CalciteConnectionFactory.class);

    /**
     * Calcite连接信息
     */
    private String modelJson;

    public CalciteConnectionFactory(String modelJson) {

        this.modelJson = modelJson;
    }

    /**
     * 所有从pool中可以被立即拿出去用的对象是一个idle状态(空闲状态)
     * 在拿到之后,可以对该对象做一些必要操作,比如设置部分属性等
     * 可以参照apache的common-dbcp2连接池的实现
     * {@link org.apache.commons.dbcp2.PoolableConnectionFactory#activateObject(PooledObject)}
     *
     * @param pooledObject
     */
    @Override
    public void activateObject(PooledObject<CalciteConnection> pooledObject) {
//        激活这个对象,使对象处于激活状态(取到对象,在对象真正被使用前,进行操作)
        CalciteConnection connection = pooledObject.getObject();
        logger.debug("获取实例连接对象:{}", connection);

    }

    /**
     * 销毁对象,当对象池检测到某个对象的空闲时间(idle)超时,或使用完对象归还到对象池之前被检测到对象已经无效时,
     * 就会调用这个方法销毁对象.对象的销毁一般和业务相关,但必须明确的是,当调用这个方法之后,对象的生命周期必须结束.
     * 如果是对象是线程,线程必须已结束,如果是socket,socket必须已close,如果是文件操作,文件数据必须已flush,
     * 且文件正常关闭.
     * @param pooledObject
     */
    @Override
    public void destroyObject(PooledObject<CalciteConnection> pooledObject) {
//        断开连接
        try {
            CalciteConnection connection = pooledObject.getObject();
            connection.close();
            logger.warn("销毁连接");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 创建一个池化对象,不仅包含对象,还封装了对象状态,声明周期等参数
     * 当{@link org.apache.commons.pool2.ObjectPool#borrowObject()}被调用时,
     * 如果当前对象池中没有空闲的对象,且对象池中的所有对象数,未超过设定的最大管理对象数,
     * ObjectPool会调用这个方法,创建一个对象,并把这个对象封装到{@link PooledObject}类中,并交给对象池管理
     * @return
     */
    @Override
    public PooledObject makeObject() {
//        创建一个新的对象(包装原始对象,用于一些属性控制)
        return new CalciteConnectionPooledObject(CalciteConnectionBuilder.build(modelJson));
    }

    /**
     * 在向对象池归还一个对象时会调用这个方法,进行钝化操作,将对象更改为idle状态(空闲状态)
     * 可以理解为反初始化,这里可以对对象做一些清理操作.比如清理掉过期的数据,下次获得对象时,不受旧数据的影响
     *
     * @param pooledObject
     */
    @Override
    public void passivateObject(PooledObject<CalciteConnection> pooledObject) {
        try {
            CalciteConnection calciteConnection = pooledObject.getObject();
            logger.debug("归还实例连接对象:{}", calciteConnection);
//            SQLWarning是JDBC API中的一种异常,用于表示数据库发生的警告信息,调用clearWarnings()方法不会关闭连接
            calciteConnection.clearWarnings();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 用于每次创建对象放入到pool
     * 或者从pool中取出对象出来的时候验证对象是否合法
     *
     * @param pooledObject
     * @return
     */
    @Override
    public boolean validateObject(PooledObject<CalciteConnection> pooledObject) {
//        判断这个对象是否是保持连接状态
        CalciteConnection connection = pooledObject.getObject();
        return true;
    }
}
