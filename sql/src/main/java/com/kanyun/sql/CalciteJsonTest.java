package com.kanyun.sql;

import com.google.common.io.Files;
import com.kanyun.sql.core.CustomFunc;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.NullCollation;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.util.ConversionUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;


public class CalciteJsonTest {
    static Properties info;

    public static void main(String[] args) throws SQLException {

//        System.setProperty("saffron.default.charset", "UTF-8");
        System.setProperty("saffron.default.charset", ConversionUtil.NATIVE_UTF16_CHARSET_NAME);
        System.setProperty("saffron.default.nationalcharset", ConversionUtil.NATIVE_UTF16_CHARSET_NAME);
        System.setProperty("saffron.default.collation.name", ConversionUtil.NATIVE_UTF16_CHARSET_NAME + "$en_US");

        info = new Properties();
        info.setProperty(CalciteConnectionProperty.DEFAULT_NULL_COLLATION.camelName(), NullCollation.LAST.name());
        info.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), "false");
        // 字符串方式
        String model = getFile("model.json");
        Connection connection = DriverManager.getConnection("jdbc:calcite:model=inline:" + model, info);
//         取得Calcite连接
        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
//        Calcite中内置的函数主要在SqlStdOperatorTable
//        注册自定义函数
        rootSchema.add("custom_suffix", ScalarFunctionImpl.create(CustomFunc.class, "customSuffix"));
        ResultSet tables = connection.getMetaData().getTables(null, null, null, null);
        Statement statement = connection.createStatement();
        String sql = "SELECT custom_suffix(a.name,'###') as name,b.score, c.name AS SUBJECT FROM students a, student_courses b, courses c WHERE  a.id = b.student_id AND b.course_id = c.id";
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            System.out.println(resultSet.getString(1) + ":" + resultSet.getString(2) + ":" + resultSet.getString(3));
        }


    }

    /**
     * 得到Calcite的配置信息
     *
     * @param fileName
     * @return
     */
    public static String getFile(String fileName) {
        ClassLoader classLoader = CalciteJsonTest.class.getClassLoader();

//        getResource()方法会去classpath下找这个文件，获取到url resource, 得到这个资源后，调用url.getFile获取到 文件 的绝对路径
        URL url = classLoader.getResource(fileName);
//        url.getFile() 得到这个文件的绝对路径
        System.out.println(url.getFile());
        File file = new File(url.getFile());
        try {
            return Files.asCharSource(file, StandardCharsets.UTF_8).read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
