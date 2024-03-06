package com.kanyun.sql.util;


import org.apache.commons.lang3.ClassPathUtils;

import java.io.File;
import java.sql.*;

/**
 * H2数据库工具类
 * H2连接信息：http://www.h2database.com/html/features.html
 */
public class H2Utils {

//    打成Jar包后执行,报空指针错误
//    private static final String CLASS_PATH = H2Utils.class.getClassLoader().getResource("").getPath();
//    private static final String INIT_SQL_FILE_PATH = CLASS_PATH + "sql/"  + "init.sql";


    /**
     * 定义H2数据库的连接地址
     * 可以使用 ~ 符号 代替System.getProperty("user.home")
     */
    private static final String URL = "jdbc:h2:file:" + System.getProperty("user.home")
            + File.separator + "JsonQuery;"
//            + "INIT=RUNSCRIPT FROM'" + INIT_SQL_FILE_PATH + "';" //初始化运行SQL文件
            + "MODE=MySQL;" //兼容模式，H2兼容多种数据库，该值可以为：DB2、Derby、HSQLDB、MSSQLServer、MySQL、Oracle、PostgreSQL
            + "DB_CLOSE_DELAY=-1;" //最后一个正在连接的连接断开后，不要关闭数据库
            + "AUTO_RECONNECT=TRUE;" //连接丢失后自动重新连接
            + "AUTO_SERVER=TRUE"; //启动自动混合模式，允许开启多个连接，该参数不支持在内存中运行模式

    private static final String INITIALIZATION_SQL = "CREATE TABLE IF NOT EXISTS " +
            "field_info(`schema` VARCHAR,`table` VARCHAR, `name` VARCHAR, `type` VARCHAR, `default_value` VARCHAR)";
    private static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(URL);
            Statement stmt = connection.createStatement();
            stmt.execute(INITIALIZATION_SQL);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static ResultSet execQuery(String sql) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    public static boolean executeSql(String sql) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        return preparedStatement.execute();
    }
}
