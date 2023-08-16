package com.kanyun.ui;

import com.kanyun.ui.splash.SplashPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Sql提示工具类
 */
public class SqlSuggestionUtil {
    private static final Logger log = LoggerFactory.getLogger(SqlSuggestionUtil.class);

    /**
     * @param newValue    当前已有字符
     * @param cursorIndex 光标所在的字符位置
     * @return
     */
    public static List<String> search(String newValue, int cursorIndex) {
        String keyWord = "";
        char[] chars = newValue.toCharArray();
        if (newValue.length() == cursorIndex + 1) {
//            说明当前光标在最后一个字符的后面
            log.info("当前光标在最后一个文字后");
            searchKeyWord(chars[0,cursorIndex])
        }

        return null;
    }

    public static void searchKeyWord(char[] chars) {

    }
}
