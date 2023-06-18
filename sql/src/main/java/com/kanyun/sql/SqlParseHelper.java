package com.kanyun.sql;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.ddl.SqlDdlParserImpl;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * sql经过calcite解析之后，得到一棵抽象语法树，也就是我们说的AST，这棵语法树是由不同的节点组成，节点称之为SqlNode，
 * 根据不同类型的dml、ddl得到不同的类型的SqlNode，
 * 例如select语句转换为SqlSelect，delete语句转换为SqlDelete，join语句转换为SqlJoin。
 * https://lixiyan4633.gitee.io/categories/calcite/
 */
public class SqlParseHelper {

    private static Logger log = LoggerFactory.getLogger(SqlParseHelper.class);

    public static void getKind(String sql) {

        SqlParser.Config config = SqlParser.configBuilder()
                .setLex(Lex.MYSQL) //使用mysql 语法
                .setParserFactory(SqlDdlParserImpl.FACTORY)
                .build();
//        SqlParser 语法解析器
        SqlParser sqlParser = SqlParser
                .create(StringUtils.isNotBlank(sql) ? sql : "select a.id,a.name,a.age FROM stu a where age<20", config);
        SqlNode sqlNode = null;
        try {
//            SqlUtil.isCallTo()
//            sql经过parser.parseStmt()解析之后,会生成一个SqlNode
            sqlNode = sqlParser.parseStmt();
            getSelect(sqlNode);
        } catch (SqlParseException e) {
            throw new RuntimeException("", e);
        }

    }


    /**
     * 解析一个select的语句，那么得到的sqlNode就是一个SqlSelect
     * 一个select语句包含from部分、where部分、select部分等，每一部分都表示一个SqlNode
     * SqlKind是一个枚举类型，包含了各种SqlNode类型：SqlSelect、SqlIdentifier、SqlLiteral等。SqlIdentifier表示标识符
     * 例如表名称、字段名；SqlLiteral表示字面常量，一些具体的数字、字符
     */
    public static void getSelect(SqlNode sqlNode) {
        if (SqlKind.SELECT.equals(sqlNode.getKind())) {
            SqlSelect sqlSelect = (SqlSelect) sqlNode;
//            SqlSelect的子节点包含了from 和 where 和 查询的字段
            SqlNode from = sqlSelect.getFrom();
            SqlNode where = sqlSelect.getWhere();
//            查询的哪些字段
            SqlNodeList selectFields = sqlSelect.getSelectList();
//            标识符
            if (SqlKind.IDENTIFIER.equals(from.getKind())) {
                log.debug("from 字段值：{}", from);
            }

            if (where != null) {
                if (SqlKind.LESS_THAN.equals(where.getKind())) {
                    SqlBasicCall sqlBasicCall = (SqlBasicCall) where;
                    for (SqlNode operandNode : sqlBasicCall.getOperandList()) {
                        if (SqlKind.LITERAL.equals(operandNode.getKind())) {
                            log.debug("where 中的操作符：{}", operandNode);
                        }
                    }
                }
            }

//            解析查询的字段
            selectFields.getList().forEach(x -> {
                if (SqlKind.IDENTIFIER.equals(x.getKind())) {
                    x = SqlValidatorUtil.addAlias(x, x + "1");
                    log.debug("查询字段：{}", x);
                }
//                聚合操作
                if (SqlKind.SUM.equals(x.getKind())) {
//                    SqlBasicCall对比SqlSelect/SqlDelete而言，可以理解为表示的是一些基本的、简单的调用，例如聚合函数、比较函数等
                    SqlBasicCall sqlBasicCall = (SqlBasicCall) x;
                    System.out.println(sqlBasicCall.getOperandList().get(0));
                }
            });
        }
    }
}
