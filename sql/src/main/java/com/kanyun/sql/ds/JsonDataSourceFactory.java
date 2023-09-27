package com.kanyun.sql.ds;

import org.apache.calcite.jdbc.CalciteConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DataSource工厂,用于获取Connection
 * Deprecated use {@link DataSourceConnectionPool}
 */
@Deprecated
public class JsonDataSourceFactory {

    private static final Logger logger = LoggerFactory.getLogger(JsonDataSourceFactory.class);

    /**
     * DataSource
     */
    private static JsonDataSource jsonDataSource;

    /**
     * 初始化DataSource
     */
    public static void initDataSource(String modelJson) {
        try {
            jsonDataSource = new JsonDataSource(modelJson);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 从DataSource中获取连接
     * @return
     */
    public static CalciteConnection getConnection() {
        if (jsonDataSource != null) {
            try {
                return jsonDataSource.getConnection();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}
