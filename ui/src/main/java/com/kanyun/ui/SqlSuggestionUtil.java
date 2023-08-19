package com.kanyun.ui;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Sql提示工具类
 */
public class SqlSuggestionUtil {
    private static final Logger log = LoggerFactory.getLogger(SqlSuggestionUtil.class);

    private static String[] SQL_OPR = {"SELECT", "FROM", "WHERE", "GROUP", "BY", "HAVING", "IN", "AND", "LIMIT"};

    /**
     * 获取SQL自动完成建议
     *
     * @param defaultSchema 当前选中的Schema
     * @param content       当前已有字符
     * @param cursorIndex   光标所在的字符位置
     * @return
     */
    public static List<String> searchSuggestion(String defaultSchema, String content, int cursorIndex) {
        String keyWord = "";
        Pair<String, Integer> contextSqlPair = getContextSql(content, cursorIndex);
        String contextSql = contextSqlPair.getLeft();
        Integer contextIndex = contextSqlPair.getRight();
        char[] chars = contextSql.toCharArray();
        if (contextSql.length() == contextIndex + 1) {
//            说明当前光标在最后一个字符的后面
            log.debug("当前光标在SQL的最后一个字符后");
//            拷贝数组,参数1：原始数组,参数2：拷贝长度(从索引位置为0的地方开始拷贝),获取需要查询关键字的字符串
            char[] keywordContent = Arrays.copyOf(chars, contextIndex + 1);
            searchKeyWord(keywordContent, keywordContent.length - 1, "");
        }
        return Collections.emptyList();
//        return Arrays.asList(SQL_OPR);
    }

    /**
     * 获取光标所在位置的上下文SQL,考虑存在多条SQL的情况
     * 1:先获取当前光标所在位置的上下文SQL
     * 1.1:判断content中是否包含分号,如果包含分号说明存在多条SQL,不包含分号则说明 content就是当前当前需要自动完成的SQL
     * 1.2:存在分号,则根据当前光标位置向前遍历至分号或第一个字符,获取索引(startIndex),同时再向后遍历至分号或最后一个索引(endIndex)
     * 并获取content (startIndex-endIndex) 索引区间的字符,获取当前需要自动完成建议的SQL
     * 2:获取到当前需要自动生成建议的SQL后,需要重新计算光标所在的位置,即 cursorIndex - startIndex
     * 当前sql为content的第一条SQL时,cursorIndex不变,也即 startIndex=0
     *
     * @param content
     * @param cursorIndex
     * @return
     */
    public static Pair<String, Integer> getContextSql(String content, int cursorIndex) {
        if (content.contains(";")) {
            Integer startIndex = getContextSqlStartIndex(content, cursorIndex);
            Integer endIndex = getContextSqlEndIndex(content, cursorIndex);
            String contextSql = String.valueOf(Arrays.copyOfRange(content.toCharArray(), startIndex, endIndex));
            return Pair.of(contextSql, cursorIndex - startIndex);
        }
//        当前只存在一条SQL
        return Pair.of(content, cursorIndex);

    }

    /**
     * 获取当前光标所在SQL的前置索引
     *
     * @param content
     * @param cursorIndex
     * @return
     */
    public static Integer getContextSqlStartIndex(String content, int cursorIndex) {
        if (cursorIndex == 0) return 0;
        char[] chars = content.toCharArray();
        while (cursorIndex > 0) {
            cursorIndex = cursorIndex - 1;
            if (chars[cursorIndex] == ';') {
                return cursorIndex;
            }
        }
        return cursorIndex;
    }

    /**
     * 获取当前光标所在SQL的后置索引
     *
     * @param content
     * @param cursorIndex
     * @return
     */
    public static Integer getContextSqlEndIndex(String content, int cursorIndex) {
        if (cursorIndex == content.length() - 1) return cursorIndex;
        char[] chars = content.toCharArray();
        while (cursorIndex > content.length() - 1) {
            cursorIndex = cursorIndex + 1;
            if (chars[cursorIndex] == ';') {
                return cursorIndex;
            }
        }
        return cursorIndex;
    }


    public static void searchKeyWord(char[] chars, int index, String keyword) {
        if (chars[index] == ' ') {

        }
    }
}
