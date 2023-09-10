package com.kanyun.ui.event;

import com.kanyun.sql.QueryInfoHolder;
import com.kanyun.sql.SqlExecutor;
import javafx.concurrent.Task;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * 简单SQL执行任务(Task方式异步)
 */
public class SimpleSqlExecuteTask  extends Task<Pair<Map<String, Integer>, List<Map<String, Object>>>> {


    /**
     * modeJson内容
     */
    private String modelJson;

    /**
     * 默认Schema
     */
    private String defaultSchema;

    /**
     * 待执行的SQL脚本
     */
    private String sql;


    /**
     * 查询信息
     */
    private Map<String, Object> queryInfo;

    public SimpleSqlExecuteTask(String modelJson, String defaultSchema, String sql) {
        this.modelJson = modelJson;
        this.defaultSchema = defaultSchema;
        this.sql = sql;
    }

    @Override
    protected Pair<Map<String, Integer>, List<Map<String, Object>>> call() throws Exception {
        Pair<Map<String, Integer>, List<Map<String, Object>>> data = SqlExecutor.execute(modelJson, defaultSchema, sql);
        queryInfo = QueryInfoHolder.getQueryInfo();
        return data;
    }
}
