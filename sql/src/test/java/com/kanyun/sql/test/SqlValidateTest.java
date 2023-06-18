package com.kanyun.sql.test;


import com.google.common.base.Joiner;
import com.kanyun.sql.simple.SimpleSchema;
import com.kanyun.sql.simple.SimpleTable;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.NullCollation;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.metadata.RelColumnOrigin;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.IntStream;


public class SqlValidateTest {

    static CalciteConnection calciteConnection;

    @Before
    public void before() throws SQLException {
        Properties info = new Properties();
        info.setProperty(CalciteConnectionProperty.DEFAULT_NULL_COLLATION.camelName(), NullCollation.LAST.name());
        info.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), "false");
        CalciteConnectionConfig config = new CalciteConnectionConfigImpl(info);
        Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
        calciteConnection = connection.unwrap(CalciteConnection.class);
    }

    @Test
    public void simpleTableTest() {
        SimpleTable userTable = SimpleTable.newBuilder("users")
                .addField("id", SqlTypeName.VARCHAR)
                .addField("name", SqlTypeName.VARCHAR)
                .addField("age", SqlTypeName.INTEGER)
//                .withFilePath("/path/to/user.csv")
                .withRowCount(10).build();
        SimpleTable orderTable = SimpleTable.newBuilder("orders")
                .addField("id", SqlTypeName.VARCHAR)
                .addField("user_id", SqlTypeName.VARCHAR)
                .addField("goods", SqlTypeName.VARCHAR)
                .addField("price", SqlTypeName.DECIMAL)
//                .withFilePath("/path/to/order.csv")
                .withRowCount(10).build();
//       创建Schema, 一个Schema中包含多个表. Calcite中的Schema类似于RDBMS中的Database
        SimpleSchema simpleSchema = SimpleSchema.newBuilder("test").addTable(userTable).addTable(orderTable).build();

        SchemaPlus rootSchema = calciteConnection.getRootSchema();
//        将自定义的Schema(数据源)添加到RootSchema中
        rootSchema.add(simpleSchema.getSchemaName(), simpleSchema);

        String sql = "select u.name,u.age,o.goods,o.price from test.users u , test.orders o where u.id = o.user_id";
        lineage(rootSchema, sql);
//        ResultSet resultSet = calciteConnection.createStatement().executeQuery(sql);
//        System.out.println("=========");
    }

    @Test
    public void jdbcSchema(String[] args) throws SQLException {

        try {
//            创建JdbcSchema(数据源)
            Class.forName("com.mysql.cj.jdbc.Driver");
            DataSource dataSource = JdbcSchema.dataSource("jdbc:mysql://localhost:3306/drools", "com.mysql.cj.jdbc.Driver", "root", "db482b08e95c08d4");
            SchemaPlus rootSchema = calciteConnection.getRootSchema();
            JdbcSchema jdbcSchema = JdbcSchema.create(rootSchema, "ds", dataSource, null, null);
//            得到Mysql数据库的所有表
            Set<String> tableNames = jdbcSchema.getTableNames();
            rootSchema.add("ds", jdbcSchema);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    static void lineage(SchemaPlus schemaPlus, String sql) {
        try {
            Planner planner = Frameworks.getPlanner(Frameworks.newConfigBuilder()
//                        忽略SQL大小写,calcite默认会转换为大写
                    .parserConfig(SqlParser.config().withCaseSensitive(false))
//                    设置默认的Schema,由于参数传递的是RootSchema,因此SQL语句需要添加schema名
                    .defaultSchema(schemaPlus).build());
//            解析sql
            SqlNode parse = planner.parse(sql);
//            验证sql
            planner.validate(parse);
            RelRoot relRoot = planner.rel(parse);
            List<RelDataTypeField> fieldList = relRoot.project().getRowType().getFieldList();
            RelMetadataQuery metadataQuery = relRoot.project().getCluster().getMetadataQuery();
            IntStream.range(0, fieldList.size()).boxed().forEach(index -> {
                RelDataTypeField relDataTypeField = fieldList.get(index);
                RelColumnOrigin columnOrigin = metadataQuery.getColumnOrigin(relRoot.rel, index);
                assert columnOrigin != null;
                System.out.println(relDataTypeField.getName() + "->" + wrapMsg(columnOrigin));

            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String wrapMsg(RelColumnOrigin columnOrigin) {
        int originColumnOrdinal = columnOrigin.getOriginColumnOrdinal();
        RelOptTable originTable = columnOrigin.getOriginTable();
        RelDataTypeField relDataTypeField = originTable.getRowType().getFieldList().get(originColumnOrdinal);
        return Joiner.on(",").join(originTable.getQualifiedName(), relDataTypeField.getName());
    }
}

