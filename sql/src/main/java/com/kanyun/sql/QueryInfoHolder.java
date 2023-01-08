package com.kanyun.sql;


import com.google.common.collect.Collections2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QueryInfoHolder {

    /**
     * 注意：初始化时不要使用Collections.EMPTY_MAP，因为
     * 该方法将返回不可变的空Map,不能put值,否则报错
     */
    private final static ThreadLocal<Map> QUERY_INFO = ThreadLocal.withInitial(() -> new HashMap());

    public static void setQueryCost(long millisecond) {
        QUERY_INFO.get().put("cost", millisecond);
    }

    public static void setRecordCount(long count) {
        QUERY_INFO.get().put("count", count);
    }

    public static Map getQueryInfo() {
        return QUERY_INFO.get();
    }
}
