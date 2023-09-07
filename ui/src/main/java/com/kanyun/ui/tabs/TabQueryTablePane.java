package com.kanyun.ui.tabs;

import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.IconProperties;
import com.kanyun.ui.components.SimplicityPaginationToolBar;
import com.kanyun.ui.components.TableViewPane;
import com.kanyun.ui.event.ExecuteSqlService;
import com.kanyun.ui.event.StatusBarProgressTask;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.model.TableModel;
import com.sun.javafx.event.EventUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 表全量数据Tab
 * 包含分页组件
 */
public class TabQueryTablePane extends VBox implements TabKind {

    private static final Logger log = LoggerFactory.getLogger(TabQueryTablePane.class);

    /**
     * SQL模板
     */
    private static final String SQL_TEMPLATE = "SELECT * FROM %s LIMIT %d OFFSET %d ";

    /**
     * 动态信息栏
     */
    private StatusBar dynamicInfoStatusBar;

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
     */
    private SimpleStringProperty dynamicInfoProperty = new SimpleStringProperty();

    /**
     * 当前Tab页所属的表信息
     */
    private TableModel tableModel;


    public TabQueryTablePane(TableModel tableModel) throws Exception {
        log.debug("查询表页面被新建,[{}.{}] 被打开", tableModel.getSchemaName(), tableModel.getTableName());
        try {
            executeSqlService = new ExecuteSqlService();
            paginationToolBar = new SimplicityPaginationToolBar(this);
            createDynamicInfoStatusBar();
            this.tableModel = tableModel;
            queryTable(tableModel);
            addAsyncTaskListener();

        } catch (Exception exception) {
            log.error("打开表异常：", exception);
            throw exception;
        }
    }

    /**
     * 空的构造方法,由子类集成{@link SimplicityPaginationToolBar}用以获取实例
     * 外部不能调用
     */
    public TabQueryTablePane() {
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
        String sql = generateSql(tableModel);
        UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL);
        userEvent.setSql(sql);
//        SQL执行事件,自己发送,自己接收
        EventUtil.fireEvent(this, userEvent);
        executeSqlService.setSql(sql).setDefaultSchema(defaultSchema).setModelJson(modelJson);
        executeSqlService.start();
    }

    /**
     * 生成SQL
     *
     * @param tableModel
     * @return
     */
    private String generateSql(TableModel tableModel) {
//        获取表名(schema+table)
        String tableName = tableModel.getSchemaName() + "." + tableModel.getTableName();
//        获取分页信息,并计算offset的值
        Integer currentPage = paginationToolBar.getCurrentPage();
        Integer limit = paginationToolBar.getPageLimit();
        Integer offset = (currentPage - 1) * limit;
        return String.format(SQL_TEMPLATE, tableName, limit, offset);
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
        dynamicInfoStatusBar.textProperty().bind(dynamicInfoProperty);
        addStatusBarEventListener();
    }

    @Override
    public StatusBar getDynamicInfoStatusBar() {
        return dynamicInfoStatusBar;
    }

    @Override
    public void addStatusBarEventListener() {
        addEventHandler(UserEvent.EXECUTE_SQL, event -> {
//            去掉SQL中的换行符
            String sql = event.getSql().replaceAll("\r|\n|\t", "");
            log.debug("设置动态SQL信息:[{}]", sql);
            dynamicInfoProperty.set(sql);
//            开启进度条
            startSqlExecuteProgressTask();
        });

        addEventHandler(UserEvent.EXECUTE_SQL_COMPLETE, event -> {
            log.debug("接收到SQL执行完成事件,准备停止进度条,并设置查询记录数及查询耗时");
//            这里就不需要再移除动态信息栏右侧的Item了,因为该Tab页不存在重用的情况
//            dynamicInfoStatusBar.getRightItems().removeAll(dynamicInfoStatusBar.getRightItems());
            Map<String, Object> queryInfo = event.getQueryInfo();
            String cost = "查询耗时：" + TabKind.getSecondForMilliSecond(queryInfo.get("cost")) + "秒";
            String record = "总记录数：" + queryInfo.get("count");
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
        return IconProperties.getIcon("tab.query_table", TAB_GRAPHIC_SIZE, Color.BLUE);
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
        if (getChildren().size() ==  2) {
            getChildren().set(0, tableViewPane);
        }else {
            getChildren().addAll(tableViewPane, paginationToolBar);
        }

    }

    /**
     * 添加异步任务监听
     */
    public void addAsyncTaskListener() {
//        异步任务成功执行监听
        executeSqlService.setOnSucceeded(event -> {
            stopSqlExecuteProgressTask();
            Object tableData = event.getSource().getValue();
            Pair<Map<String, Integer>, List<Map<String, Object>>> result = (Pair<Map<String, Integer>, List<Map<String, Object>>>) tableData;
//            发送SQL执行完成事件
            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL_COMPLETE);
            userEvent.setQueryInfo(executeSqlService.getQueryInfo());
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
//        属性解绑
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
     */
    public void lastedPage() {
        queryTable(tableModel);
    }

    /**
     * 跳转到自定义页
     */
    public void customPage() {
        queryTable(tableModel);
    }
}

