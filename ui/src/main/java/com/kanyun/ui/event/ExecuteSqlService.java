package com.kanyun.ui.event;

import com.kanyun.sql.QueryInfoHolder;
import com.kanyun.sql.SqlExecutor;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 异步执行SQL服务
 */
public class ExecuteSqlService extends Service<Pair<Map<String, Integer>, List<Map<String, Object>>>> {

    private static final Logger log = LoggerFactory.getLogger(ExecuteSqlService.class);

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
    private Map queryInfo;


    public ExecuteSqlService setModelJson(String modelJson) {
        this.modelJson = modelJson;
        return this;
    }

    public ExecuteSqlService setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
        return this;
    }

    public ExecuteSqlService setSql(String sql) {
        this.sql = sql;
        return this;
    }

    public Map<String, Object> getQueryInfo() {
        return queryInfo;
    }

    @Override
    protected Task<Pair<Map<String, Integer>, List<Map<String, Object>>>> createTask() {
        Task<Pair<Map<String, Integer>, List<Map<String, Object>>>> executeSqlTask = new Task<Pair<Map<String, Integer>, List<Map<String, Object>>>>() {
            @Override
            protected Pair<Map<String, Integer>, List<Map<String, Object>>> call() throws Exception {
                Pair<Map<String, Integer>, List<Map<String, Object>>> data = SqlExecutor.execute(modelJson, defaultSchema, sql);
                queryInfo = QueryInfoHolder.getQueryInfo();
                return data;
            }


        };
        return executeSqlTask;
    }


    @Override
    protected void succeeded() {
        super.succeeded();
        log.debug("succeeded()是否是JavaFX Application Thread: [{}]", Platform.isFxApplicationThread());
    }

    /**
     * 重写该方法很重要
     * 在测试时发现,如果Json文件异常
     * 如：json内容每个对象的元素数量不一致
     * 或解析出的json字段类型不正确将导致SQL执行失败
     * 且抛出的异常会被吞掉,重写该方法将打出异常日志
     */
    @Override
    protected void failed() {
        super.failed();
        log.error("异步SQL执行任务异常:", super.getException());
    }
}
