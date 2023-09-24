package com.kanyun.sql.ds.pool;

import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

/**
 * 管理池化对象的状态，比如创建，初始化，验证对象状态和销毁对象
 * 资源工厂
 */
public class CalciteConnectionFactory implements PooledObjectFactory<CalciteConnection> {

    private static final Logger logger = LoggerFactory.getLogger(CalciteConnectionFactory.class);

    private String modelJson;

    public CalciteConnectionFactory(String modelJson) {
        this.modelJson = modelJson;
    }

    /**
     * 所有从pool中可以被立即拿出去用的对象是一个idle状态(空闲状态)
     * 在拿到之后,用该方法激活
     * @param pooledObject
     */
    @Override
    public void activateObject(PooledObject<CalciteConnection> pooledObject)  {
//        激活这个对象,使对象处于激活状态
    }

    @Override
    public void destroyObject(PooledObject pooledObject) {
//        断开连接
    }

    /**
     * 创建一个池化对象,不仅包含对象,还封装了对象状态,声明周期等参数
     * @return
     */
    @Override
    public PooledObject makeObject() {
//        创建一个新的对象(包装原始对象,用于一些属性控制)
        return new CalciteConnectionPooledObject(CalciteConnectionBuilder.build(modelJson)) ;
    }

    /**
     * 当销毁时,进行钝化操作,将对象更改为idle状态(空闲状态)
     * @param pooledObject
     */
    @Override
    public void passivateObject(PooledObject pooledObject) {
    }

    /**
     * 用于每次创建对象放入到pool
     * 或者从pool中取出对象出来的时候验证对象是否合法
     * @param pooledObject
     * @return
     */
    @Override
    public boolean validateObject(PooledObject pooledObject) {
//        判断这个对象是否是保持连接状态
        return false;
    }
}
