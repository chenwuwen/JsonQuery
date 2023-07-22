package com.kanyun.ui.tabs;

import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.components.TableViewPane;
import com.kanyun.ui.event.ExecuteSqlService;
import com.kanyun.ui.event.StatusBarProgressTask;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.model.TableModel;
import com.sun.javafx.event.EventUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
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
 */
public class TabQueryTablePane extends VBox implements TabKind {

    private static final Logger log = LoggerFactory.getLogger(TabQueryTablePane.class);

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
     * 自定义StatusBar进度条任务
     */
    private StatusBarProgressTask statusBarProgressTask;

    /**
     * 动态信息属性
     */
    private SimpleStringProperty dynamicInfoProperty = new SimpleStringProperty();

    public TabQueryTablePane(TableModel tableModel) throws Exception {
        log.debug("查询表页面被新建,[{}.{}] 被打开", tableModel.getSchemaName(), tableModel.getTableName());
        try {
            executeSqlService = new ExecuteSqlService();
            createDynamicInfoStatusBar();
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
    public void queryTable(TableModel tableModel) throws Exception {
        String defaultSchema = tableModel.getSchemaName();
        String modelJson = ModelJson.getModelJson(defaultSchema);
        String sql = "select * from " + tableModel.getSchemaName() + "." + tableModel.getTableName();
        UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL);
        userEvent.setSql(sql);
//        SQL执行事件,自己发送,自己接收
        EventUtil.fireEvent(this, userEvent);
        executeSqlService.setSql(sql).setDefaultSchema(defaultSchema).setModelJson(modelJson);
        executeSqlService.start();
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
//            注意这里如果是set(index,node),那么如果指定索引处没有Node将会报错
            dynamicInfoStatusBar.getRightItems().add(0, new Separator(Orientation.VERTICAL));
            dynamicInfoStatusBar.getRightItems().add(1, costLabel);
            dynamicInfoStatusBar.getRightItems().add(2, new Separator(Orientation.VERTICAL));
            dynamicInfoStatusBar.getRightItems().add(3, recordLabel);
        });
    }

    /**
     * 设置TableView数据
     *
     * @param result
     */
    public void buildTableView(Pair<Map<String, Integer>, List<Map<String, Object>>> result) {
//        字段名与字段类型映射信息
        Map<String, Integer> columnInfos = result.getLeft();
//         表数据
        List<Map<String, Object>> data = result.getRight();
//        实例化自定义TableView组件
        TableViewPane tableViewPane = new TableViewPane();
//       设置table列信息
        tableViewPane.setTableColumns(columnInfos);
//       设置table行数据
        tableViewPane.setTableRows(FXCollections.observableList(data));
        getChildren().add(tableViewPane);
    }

    public void addAsyncTaskListener() {
//        异步任务成功执行监听
        executeSqlService.setOnSucceeded(event -> {
            stopSqlExecuteProgressTask();
            Object tableData = event.getSource().getValue();
            Pair<Map<String, Integer>, List<Map<String, Object>>> result = (Pair<Map<String, Integer>, List<Map<String, Object>>>) tableData;
            buildTableView(result);
//            发送SQL执行完成事件
            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL_COMPLETE);
            userEvent.setQueryInfo(executeSqlService.getQueryInfo());
            EventUtil.fireEvent(this, userEvent);
        });

//        异步任务执行失败监听
        executeSqlService.setOnFailed(event -> {
            stopSqlExecuteProgressTask();
        });

//        异步任务取消执行
        executeSqlService.setOnCancelled(event -> {
            stopSqlExecuteProgressTask();
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
}

