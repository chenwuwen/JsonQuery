package com.kanyun.sql.ds.pool;

import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.commons.pool2.PooledObjectState;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

/**
 * 继承DefaultPooledObject,用来维护池化对象(原始对象)的一系列状态参数.
 * 比如状态信息{@link PooledObjectState}(已用、未用、空闲)，创建时间，激活时间，关闭时间等。这些添加的信息方便pool来管理和实现一些特定的操作
 */
public class CalciteConnectionPooledObject extends DefaultPooledObject<CalciteConnection> {

    private static final Logger logger = LoggerFactory.getLogger(CalciteConnectionPooledObject.class);

    /**
     * Creates a new instance that wraps the provided object so that the pool can
     * track the state of the pooled object.
     *
     * @param object The object to wrap
     */
    public CalciteConnectionPooledObject(CalciteConnection object) {
        super(object);
    }

    /**
     * 代理对象(前提是使用代理连接池)每个方法被调用时,会回调这个方法
     * {@link org.apache.commons.pool2.proxy.ProxiedObjectPool}
     */
    @Override
    public void use() {
        CalciteConnection connection = getObject();

        logger.debug("代理对象use()方法被调用,{}", connection);
        super.use();
    }
}
