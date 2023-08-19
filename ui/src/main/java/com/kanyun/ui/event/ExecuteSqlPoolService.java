package com.kanyun.ui.event;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kanyun.sql.QueryInfoHolder;
import com.kanyun.sql.SqlExecutor;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * 多线程异步执行
 * JavaFX的Service类本身并没有提供线程池的功能。它只是一个抽象类，用于实现后台任务的执行。在Service类中，
 * 有一个名为"worker"的属性，它是一个实现了JavaFX Worker接口的实例。真正的任务执行是在Worker接口的doWork()方法中完成的
 * {@link ExecuteSqlService}
 * 如果需要使用线程池来管理多个任务，你可以在Service的createTask()方法中创建一个线程池，并将任务提交给线程池执行
 */
public class ExecuteSqlPoolService extends Service<Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>>> {

    private static final Logger log = LoggerFactory.getLogger(ExecuteSqlPoolService.class);

    /**
     * modeJson内容
     */
    private String modelJson;

    /**
     * 默认Schema
     */
    private String defaultSchema;


    /**
     * 多条SQL的查询结果
     */
    private Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>> queryResultCollection = new LinkedHashMap<>();

    /**
     * 多条SQL的查询信息
     */
    private Map<String, Object> queryInfoCollection = new LinkedHashMap<>();


    /**
     * 创建一个固定大小线程池,并设置线程名
     */
    ExecutorService sqlExecutorThreadPool = Executors.newFixedThreadPool(5,
            new ThreadFactoryBuilder().setNameFormat("execute-sql-service-pool-%d").build());

    /**
     * 待执行的SQL集合
     */
    private List<String> sqlList = new LinkedList<>();


    public ExecuteSqlPoolService setModelJson(String modelJson) {
        this.modelJson = modelJson;
        return this;
    }

    public ExecuteSqlPoolService setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
        return this;
    }

    public ExecuteSqlPoolService addSql(String sql) {
        sqlList.add(sql);
        return this;
    }

    public ExecuteSqlPoolService addAllSql(Collection<String> sqls) {
        sqlList.addAll(sqls);
        return this;
    }

    @Override
    protected Task<Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>>> createTask() {
        Task<Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>>> executeSqlTasks = new Task<Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>>>() {
            @Override
            protected Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>> call() throws Exception {
//                创建future集合,所有sql异步执行返回的future放到集合中,最后遍历拿到结果
                Map<String, Future<Pair<Map<String, Integer>, List<Map<String, Object>>>>> futures = new LinkedHashMap<>();
//                先清空查询结果/查询信息集合
                queryInfoCollection.clear();
                queryResultCollection.clear();
//                线程池添加任务,批量执行
                for (String sql : sqlList) {
                    Future<Pair<Map<String, Integer>, List<Map<String, Object>>>> future = sqlExecutorThreadPool.submit(new Callable<Pair<Map<String, Integer>, List<Map<String, Object>>>>() {
                        @Override
                        public Pair<Map<String, Integer>, List<Map<String, Object>>> call() throws Exception {
//                            调用SQL执行器获取结果
                            Pair<Map<String, Integer>, List<Map<String, Object>>> result = SqlExecutor.execute(modelJson, defaultSchema, sql);
//                            调用完成后,将线程变量设置到查询信息集合中
                            queryInfoCollection.put(sql, QueryInfoHolder.getQueryInfo());
                            return result;
                        }
                    });
//                    将SQL与future一一对应,放到future集合中
                    futures.put(sql, future);
                }
//                清空待执行的SQL集合
                sqlList.clear();
//                遍历所有的future取出结果
                for (Map.Entry<String, Future<Pair<Map<String, Integer>, List<Map<String, Object>>>>> futureEntity : futures.entrySet()) {
                    String sql = futureEntity.getKey();
                    Pair<Map<String, Integer>, List<Map<String, Object>>> sqlResult = futureEntity.getValue().get();
                    queryResultCollection.put(sql, sqlResult);
                }
                return queryResultCollection;
            }
        };
        return executeSqlTasks;
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
