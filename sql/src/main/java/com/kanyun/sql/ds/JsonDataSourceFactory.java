package com.kanyun.sql.ds;

import java.sql.Connection;
import java.sql.SQLException;

public class JsonDataSourceFactory {

    private static JsonDataSource jsonDataSource;


    public static Connection getConnection(String modelJson) {
        try {
            jsonDataSource = new JsonDataSource(modelJson);
            return jsonDataSource.getConnection();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static Connection getConnection() {
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
