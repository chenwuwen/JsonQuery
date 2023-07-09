package com.kanyun.sql;


import java.util.HashMap;
import java.util.Map;

/**
 * 线程变量,用于参数传递
 */
public class QueryInfoHolder {

    /**
     * 注意：初始化时不要使用Collections.EMPTY_MAP，因为
     * 该方法将返回不可变的空Map,不能put值,否则报错
     */
    private final static ThreadLocal<Map<String, Object>> QUERY_INFO = ThreadLocal.withInitial(() -> new HashMap<String, Object>());

    /**
     * 设置执行耗时
     *
     * @param millisecond
     */
    public static void setQueryCost(long millisecond) {
        QUERY_INFO.get().put("cost", millisecond);
    }

    /**
     * 设置执行记录数
     *
     * @param count
     */
    public static void setRecordCount(long count) {
        QUERY_INFO.get().put("count", count);
    }

    public static Map<String, Object> getQueryInfo() {
        return QUERY_INFO.get();
    }
}
