package com.kanyun.sql.test;

import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.interpreter.Bindables;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgram;
import org.apache.calcite.rel.RelHomogeneousShuttle;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelShuttle;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.SqlWriterConfig;
import org.apache.calcite.sql.pretty.SqlPrettyWriter;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelBuilder;
import org.apache.calcite.tools.RelRunner;
import org.junit.Before;
import org.junit.Test;
import org.verdictdb.commons.DBTablePrinter;

import javax.sql.DataSource;
import java.sql.*;
import java.util.function.UnaryOperator;


public class GenerateSqlTest {

    private static final String MYSQL_SCHEMA = "demo";

    FrameworkConfig config;
    CalciteConnection calciteConnection;


    @Before
    public void before() throws SQLException {
        // Build our connection
        Connection connection = DriverManager.getConnection("jdbc:calcite:");

        // Unwrap our connection using the CalciteConnection
        calciteConnection = connection.unwrap(CalciteConnection.class);

        // Get a pointer to our root schema for our Calcite Connection
        SchemaPlus rootSchema = calciteConnection.getRootSchema();

        // Instantiate a data source, this can be autowired in using Spring as well
        DataSource mysqlDataSource = JdbcSchema.dataSource(
                "jdbc:mysql://mysql.sqlpub.com:3306/drools",
                "com.mysql.cj.jdbc.Driver", // Change this if you want to use something like MySQL, Oracle, etc.
                "kanyun", // username
                "db482b08e95c08d4"   // password
        );

        // Attach our MySQL Jdbc Datasource to our Root Schema
        rootSchema.add(MYSQL_SCHEMA, JdbcSchema.create(rootSchema, MYSQL_SCHEMA, mysqlDataSource, null, null));
        config = Frameworks.newConfigBuilder()
                .defaultSchema(rootSchema)
                .build();

    }


    @Test
    public void generateNormalSql() {
        RelBuilder rb = RelBuilder.create(config);
//        构建查询
        RelNode node = rb
                // 第一个参数是模式名,第二个参数是表名
                .scan(MYSQL_SCHEMA, "human")
                // If you want to select from more than one table, you can do so by adding a second scan parameter
                .filter(
                        rb.equals(rb.field("age"), rb.literal("18"))
                )
                // 下面这些字段是希望从查询中返回的字段
                .project(
                        rb.field("id"),
                        rb.field("name"),
                        rb.field("age")
                )
                .build();
        String sql = toSql(node, SqlDialect.DatabaseProduct.MYSQL.getDialect());

        System.out.println(sql);
    }

    @Test
    public void generateAggregateSql() {
        RelBuilder rb = RelBuilder.create(config);
//        构建查询
        RelNode node = rb
                .scan(MYSQL_SCHEMA, "human")
                .aggregate(rb.groupKey("likes"),
                        rb.count(false, "C"))
                .build();
        String sql = toSql(node, SqlDialect.DatabaseProduct.MYSQL.getDialect());

        System.out.println(sql);
    }


    @Test
    public void executeAndPrint() throws Exception {
        HepProgram program = HepProgram.builder().build();
        HepPlanner planner = new HepPlanner(program);

        RelBuilder rb = RelBuilder.create(config);
//        构建查询
        RelNode node = rb
                // 第一个参数是模式名,第二个参数是表名
                .scan(MYSQL_SCHEMA, "human")
                // If you want to select from more than one table, you can do so by adding a second scan parameter
                .filter(
                        rb.equals(rb.field("age"), rb.literal("18"))
                )
                // 下面这些字段是希望从查询中返回的字段
                .project(
                        rb.field("id"),
                        rb.field("name"),
                        rb.field("age")
                )
                .build();
        planner.setRoot(node);
        RelNode optimizedNode = planner.findBestExp();
        final RelShuttle shuttle = new RelHomogeneousShuttle() {
            @Override
            public RelNode visit(TableScan scan) {
                final RelOptTable table = scan.getTable();
                if (scan instanceof LogicalTableScan && Bindables.BindableTableScan.canHandle(table)) {
                    return Bindables.BindableTableScan.create(scan.getCluster(), table);
                }
                return super.visit(scan);
            }
        };

        optimizedNode = optimizedNode.accept(shuttle);
        final RelRunner runner = calciteConnection.unwrap(RelRunner.class);
        try (PreparedStatement ps = runner.prepareStatement(optimizedNode)) {
            // System.out.println(ps);
            ps.execute();
            try (ResultSet resultSet = ps.getResultSet()) {
                // 格式化输出打印到控制台
                DBTablePrinter.printResultSet(resultSet);
            }

        }
    }


    /**
     * Converts a relational expression to SQL in a given dialect.
     */
    private static String toSql(RelNode root, SqlDialect dialect) {
        return toSql(root, dialect, c ->
                c.withAlwaysUseParentheses(false)
                        .withSelectListItemsOnSeparateLines(false)
                        .withUpdateSetListNewline(false)
                        .withIndentation(0));
    }

    /**
     * Converts a relational expression to SQL in a given dialect
     * and with a particular writer configuration.
     */
    private static String toSql(RelNode root, SqlDialect dialect,
                                UnaryOperator<SqlWriterConfig> transform) {
        final RelToSqlConverter converter = new RelToSqlConverter(dialect);
        final SqlNode sqlNode = converter.visitRoot(root).asStatement();
        return sqlNode.toSqlString(c -> transform.apply(c.withDialect(dialect)))
                .getSql();
    }

    protected static String writeSql(SqlPrettyWriter sqlWriter, RelToSqlConverter relToSql, RelNode query) {
        sqlWriter.reset();
        SqlSelect select = relToSql.visit(query).asSelect();
        return sqlWriter.format(select);
    }
}
