package com.kanyun.ui.tabs;

import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.IconProperties;
import com.kanyun.ui.components.SimplicityPaginationToolBar;
import com.kanyun.ui.components.TableViewPane;
import com.kanyun.ui.event.ExecuteSqlService;
import com.kanyun.ui.event.SimpleSqlExecuteTask;
import com.kanyun.ui.event.StatusBarProgressTask;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.model.TableModel;
import com.sun.javafx.event.EventUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 表全量数据Tab
 * 包含分页组件
 */
public class TabQueryTablePane extends AbstractTab {

    private static final Logger log = LoggerFactory.getLogger(TabQueryTablePane.class);

    /**
     * SQL分页查询模板
     */
    private static final String SQL_ROW_PAGE_TEMPLATE = "SELECT * FROM %s LIMIT %d OFFSET %d ";

    /**
     * SQL查询模板
     */
    private static final String SQL_ROW_ALL_TEMPLATE = "SELECT * FROM %s ";

    /**
     * SQL行总数模板
     */
    private static final String SQL_ROW_COUNT_TEMPLATE = "SELECT count(*) AS rowCount FROM %s ";

    /**
     * 动态信息栏
     */
    private StatusBar dynamicInfoStatusBar;

    /**
     * 表数据总行数,并设置初始值为-1,表示未获取过当前表的总记录数
     */
    private SimpleIntegerProperty rowCount = new SimpleIntegerProperty(-1);

    /**
     * 异步SQL执行Service
     */
    private ExecuteSqlService executeSqlService;

    /**
     * 进度条展示异步任务线程池(主要用来执行进度条展示),静态变量
     */
    private static ExecutorService progressTaskExecutorPool = Executors.newCachedThreadPool();

    /**
     * 分页组件
     */
    private SimplicityPaginationToolBar paginationToolBar;

    /**
     * 自定义StatusBar进度条任务
     */
    private StatusBarProgressTask statusBarProgressTask;

    /**
     * 动态信息属性
     * 初始化放在 {@link this#createDynamicInfoStatusBar()}
     * 因为子类在实例化时会先调用父类的构造方法,而此时该成员变量尚未初始化,
     * 由于父类的构造方法调用了子类的{@link this#createDynamicInfoStatusBar()}
     * 因此初始化放在 {@link this#createDynamicInfoStatusBar()}
     */
    private SimpleStringProperty dynamicInfoProperty;

    /**
     * 当前Tab页所属的表信息
     */
    private TableModel tableModel;


    public TabQueryTablePane(TableModel tableModel) throws Exception {
        log.debug("查询表页面被新建,[{}.{}] 被打开", tableModel.getSchemaName(), tableModel.getTableName());
        try {
            executeSqlService = new ExecuteSqlService();
            paginationToolBar = new SimplicityPaginationToolBar(this);
            this.tableModel = tableModel;
            queryTable(tableModel);
            addAsyncTaskListener();

        } catch (Exception exception) {
            log.error("打开表异常：", exception);
            throw exception;
        }
    }


    /**
     * 异步执行SQL查询,并得到数据
     * 返回值是元组Pair 元组左值是字段对应的类型 元组右值是表数据
     *
     * @param tableModel
     * @return
     * @throws SQLException
     */
    private void queryTable(TableModel tableModel) {
        String defaultSchema = tableModel.getSchemaName();
        String modelJson = ModelJson.getModelJson(defaultSchema);
        String sql = generateRowListSql(tableModel);
        UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL);
        userEvent.setSql(sql);
//        SQL执行事件,自己发送,自己接收
        EventUtil.fireEvent(this, userEvent);
        executeSqlService.setSql(sql).setDefaultSchema(defaultSchema).setModelJson(modelJson);
        executeSqlService.start();
    }

    /**
     * 查询表记录数
     *
     * @param tableModel
     */
    private Long queryRowCount(TableModel tableModel) {
        String defaultSchema = tableModel.getSchemaName();
        String modelJson = ModelJson.getModelJson(defaultSchema);
        String sql = generateRowCountSql(tableModel);
        SimpleSqlExecuteTask simpleSqlExecuteTask = new SimpleSqlExecuteTask(modelJson, defaultSchema, sql);
        Executors.newFixedThreadPool(1).execute(simpleSqlExecuteTask);
        while (!simpleSqlExecuteTask.isDone()) {
            try {
                Pair<Map<String, Integer>, List<Map<String, Object>>> pair = simpleSqlExecuteTask.get();
                Object rowCount = pair.getRight().get(0).get("rowCount");
                return Long.parseLong(rowCount.toString());
            } catch (Exception e) {
                log.error("获取表:{}总记录数报错", defaultSchema + "." + tableModel.getTableName(), e);
            }
        }
        return 0L;
    }

    /**
     * 生成表行查询SQL
     *
     * @param tableModel
     * @return
     */
    private String generateRowListSql(TableModel tableModel) {
//        获取表名(schema+table)
        String tableName = tableModel.getSchemaName() + "." + tableModel.getTableName();
//        获取分页信息,并计算offset的值
        Integer currentPage = paginationToolBar.getCurrentPage();
        Integer limit = paginationToolBar.getPageLimit();
        Integer offset = (currentPage - 1) * limit;
        return String.format(SQL_ROW_PAGE_TEMPLATE, tableName, limit, offset);
    }

    /**
     * 生成表行总数查询SQL
     *
     * @param tableModel
     * @return
     */
    private String generateRowCountSql(TableModel tableModel) {
//        获取表名(schema+table)
        String tableName = tableModel.getSchemaName() + "." + tableModel.getTableName();
        return String.format(SQL_ROW_COUNT_TEMPLATE, tableName);
    }

    public TableColumn getTableColumn(String columnLabel, Integer columnType) {
        switch (columnType) {
            case Types.VARCHAR:
                TableColumn<HashMap, String> strTableColumn = new TableColumn<>(columnLabel);
                strTableColumn.setCellValueFactory(new PropertyValueFactory<>(columnLabel));
                return strTableColumn;
            case Types.INTEGER:
                TableColumn<HashMap, Integer> intTableColumn = new TableColumn<>(columnLabel);
                intTableColumn.setCellValueFactory(new PropertyValueFactory<>(columnLabel));
                return intTableColumn;
            case Types.FLOAT:
                TableColumn<HashMap, Float> floatTableColumn = new TableColumn<>(columnLabel);
                floatTableColumn.setCellValueFactory(new PropertyValueFactory<>(columnLabel));
                return floatTableColumn;
            case Types.DATE:
                TableColumn<HashMap, Date> dateTableColumn = new TableColumn<>(columnLabel);
                dateTableColumn.setCellValueFactory(new PropertyValueFactory<>(columnLabel));
                return dateTableColumn;
            case Types.DOUBLE:
                TableColumn<HashMap, Double> doubleTableColumn = new TableColumn<>(columnLabel);
                doubleTableColumn.setCellValueFactory(new PropertyValueFactory<>(columnLabel));
                return doubleTableColumn;
        }
        return null;
    }

    @Override
    public TabKindEnum getTabKind() {
        return TabKindEnum.TABLE_TAB;
    }

    @Override
    public void createDynamicInfoStatusBar() {
        dynamicInfoStatusBar = new StatusBar();
//        不设置的话,默认有个OK字样
        dynamicInfoStatusBar.setText("");
        dynamicInfoProperty = new SimpleStringProperty();
        dynamicInfoStatusBar.textProperty().bind(dynamicInfoProperty);
    }

    @Override
    public StatusBar getDynamicInfoStatusBar() {
        return dynamicInfoStatusBar;
    }

    @Override
    public void addStatusBarEventListener() {
        addEventHandler(UserEvent.EXECUTE_SQL, event -> {
            log.debug("设置动态信息栏执行的SQL:[{}]", event.getSql());
            dynamicInfoProperty.set(event.getSql());
//            开启进度条
            startSqlExecuteProgressTask();
        });

        addEventHandler(UserEvent.EXECUTE_SQL_COMPLETE, event -> {
            log.debug("接收到SQL执行完成事件,准备停止进度条,并设置查询记录数及查询耗时");
            Map<String, Object> queryInfo = event.getSignalSqlExecuteInfo();
            String cost = "查询耗时：" + TabKind.getSecondForMilliSecond(queryInfo.get("cost")) + "秒";
            String record = "当前页记录数：" + queryInfo.get("count");
            Label costLabel = TabKind.createCommonLabel(cost, dynamicInfoStatusBar, null, Color.GREEN);
            costLabel.setPrefHeight(dynamicInfoStatusBar.getHeight());
            Label recordLabel = TabKind.createCommonLabel(record, dynamicInfoStatusBar, null, Color.GREEN);
            recordLabel.setPrefHeight(dynamicInfoStatusBar.getHeight());
//            先清除在添加
            dynamicInfoStatusBar.getRightItems().clear();
//            注意这里如果是set(index,node),那么如果指定索引处没有Node将会报错
            dynamicInfoStatusBar.getRightItems().add(0, new Separator(Orientation.VERTICAL));
            dynamicInfoStatusBar.getRightItems().add(1, costLabel);
            dynamicInfoStatusBar.getRightItems().add(2, new Separator(Orientation.VERTICAL));
            dynamicInfoStatusBar.getRightItems().add(3, recordLabel);
        });
    }

    @Override
    public Node getTabGraphic() {
        return IconProperties.getImageView("/asserts/table.png", TAB_GRAPHIC_SIZE);
    }

    @Override
    public void onShown() {

    }

    /**
     * 添加TableView数据及分页信息组件,并添加到视图中
     *
     * @param result
     */
    public void addTableViewAndPagination(Pair<Map<String, Integer>, List<Map<String, Object>>> result) {

//        字段名与字段类型映射信息
        Map<String, Integer> columnInfos = result.getLeft();
//         表数据
        List<Map<String, Object>> data = result.getRight();
//        实例化自定义TableView组件
        TableViewPane tableViewPane = new TableViewPane();
//        设置查询结果(表格tableView)子组件总是填充剩余空间
        VBox.setVgrow(tableViewPane, Priority.ALWAYS);
//       设置table列信息
        tableViewPane.setTableColumns(columnInfos);
//       设置table行数据
        tableViewPane.setTableRows(FXCollections.observableList(data));
        if (getChildren().size() == 2) {
            getChildren().set(0, tableViewPane);
        } else {
            getChildren().addAll(tableViewPane, paginationToolBar);
        }

    }

    /**
     * 添加异步任务监听
     */
    public void addAsyncTaskListener() {
//        异步任务成功执行监听
        executeSqlService.setOnSucceeded(event -> {
            Object sqlResult = event.getSource().getValue();
            Pair<Map<String, Integer>, List<Map<String, Object>>> result = (Pair<Map<String, Integer>, List<Map<String, Object>>>) sqlResult;
            stopSqlExecuteProgressTask();
//            发送SQL执行完成事件
            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL_COMPLETE);
            userEvent.setSignalSqlExecuteInfo(executeSqlService.getQueryInfo());
            EventUtil.fireEvent(this, userEvent);
            addTableViewAndPagination(result);
            executeSqlService.reset();
        });

//        异步任务执行失败监听
        executeSqlService.setOnFailed(event -> {
            stopSqlExecuteProgressTask();
            executeSqlService.reset();
        });

//        异步任务取消执行
        executeSqlService.setOnCancelled(event -> {
            stopSqlExecuteProgressTask();
            executeSqlService.reset();
        });
    }

    /**
     * 开启SQL执行进度
     */
    public void startSqlExecuteProgressTask() {
        statusBarProgressTask = new StatusBarProgressTask();
        progressTaskExecutorPool.execute(statusBarProgressTask);
//        当StatusBar进度属性绑定到task的进度属性时,StatusBar就展示了进度条,同理当task的进度属性为100%时,StatusBar进度条将消失
        dynamicInfoStatusBar.progressProperty().bind(statusBarProgressTask.progressProperty());
    }

    /**
     * 停止SQL执行进度
     */
    public void stopSqlExecuteProgressTask() {
        statusBarProgressTask.stopProgress();
//        SQL执行进度属性解绑
        dynamicInfoStatusBar.progressProperty().unbind();
    }

    /**
     * 跳转到下一页
     */
    public void nextPage() {
        queryTable(tableModel);
    }

    /**
     * 跳转到第一页
     */
    public void firstPage() {
        queryTable(tableModel);
    }

    /**
     * 跳转到上一页
     */
    public void previousPage() {
        queryTable(tableModel);
    }

    /**
     * 跳转到最后一页
     * 分为三步
     * 第一步:查询总记录数
     * 第二步:根据总记录数和当前设置的每页显示数确定总分页数
     * 第三步:跳转到最后一页
     */
    public void lastedPage(SimpleStringProperty currentPageProperty) {
        Long totalCount = queryRowCount(tableModel);
        long totalPage = (totalCount + paginationToolBar.getPageLimit() - 1) / paginationToolBar.getPageLimit();
        log.info("获取到当前表:{} 总记录数:{},设置的每页显示数:{},计算得到分页总数:{}",
                tableModel.getSchemaName() + "." + tableModel.getTableName(), totalCount, paginationToolBar.getPageLimit(), totalPage);
//        修改分页组件当前页码显示值
        currentPageProperty.set(String.valueOf(totalPage));
        queryTable(tableModel);
    }

    /**
     * 跳转到自定义页
     */
    public void customPage() {
        queryTable(tableModel);
    }

    /**
     * 显示全部行数据
     */
    public void allRow() {
        String defaultSchema = tableModel.getSchemaName();
        String modelJson = ModelJson.getModelJson(defaultSchema);
        String sql = String.format(SQL_ROW_ALL_TEMPLATE, defaultSchema + "." + tableModel.getTableName());
        UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL);
        userEvent.setSql(sql);
//        SQL执行事件,自己发送,自己接收
        EventUtil.fireEvent(this, userEvent);
        executeSqlService.setSql(sql).setDefaultSchema(defaultSchema).setModelJson(modelJson);
        executeSqlService.start();
    }
}

