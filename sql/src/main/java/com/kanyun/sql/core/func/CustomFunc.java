package com.kanyun.sql.core.func;

/**
 * 自定义函数,SQL中可调用,现已加入外部函数
 * @See com.kanyun.sql.func
 */
public class CustomFunc {

    public static String customSuffix(String value,String suffix) {
        return value + suffix;
    }
}
