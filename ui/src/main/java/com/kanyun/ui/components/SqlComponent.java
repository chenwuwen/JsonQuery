package com.kanyun.ui.components;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.jfoenix.controls.JFXTextArea;
import com.kanyun.sql.QueryInfoHolder;
import com.kanyun.sql.SqlExecutor;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.event.ExecuteSqlService;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.layout.TopButtonPane;
import com.sun.javafx.event.EventUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import org.apache.calcite.util.Static;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 新建查询组件(内容区域SQL+结果)
 */
public class SqlComponent extends SplitPane {

    private static final Logger log = LoggerFactory.getLogger(SqlComponent.class);

    /**
     * SQL执行线程池
     */
    private static ExecutorService sqlExecutor = Executors.newCachedThreadPool();

    /**
     * 异步任务Service
     */
    private ExecuteSqlService executeSqlService;

    /**
     * SQL编写区域
     */
    private JFXTextArea sqlArea = new JFXTextArea();

    /**
     * SQL执行结果tableView
     */
    private TableViewPane tableViewPane = new TableViewPane();

    /**
     * SQL
     */
    private SimpleStringProperty sqlProperty = new SimpleStringProperty();

    public SqlComponent() {
        setId("SqlComponent");
//        节点中子布局不会随拖动变化大小
//        setResizableWithParent();
//        设置分隔布局的方向
        setOrientation(Orientation.VERTICAL);
//        添加子组件
        getItems().addAll(sqlArea);
//        将TextArea的文本属性绑定到SimpleStringProperty,方便后面取值,设值以及监听,双向绑定,注意不要绑定反了,否则TextArea将不能编辑
//        sqlArea.textProperty().bind(sqlProperty);
        sqlProperty.bind(sqlArea.textProperty());
        executeSqlService = new ExecuteSqlService();
        addAsyncTaskListener();
    }

    /**
     * 异步执行SQL
     *
     * @param defaultSchema
     */
    public void executeSQL(String defaultSchema) {
        String sql = sqlProperty.get();
        String modelJson = ModelJson.getModelJson(defaultSchema);
        if (executeSqlService.isRunning()) {
            log.warn("准备执行SQL:[{}],查询到异步任务当前为运行状态");
        }
//        执行SQL时,判断当前TableViewPane是否已加载到界面,如果加载过了,说明之前执行过SQL了,现在重新执行,需要将之前执行的结果清除掉
        if (getItems().size() > 1) {
//            由于tableViewPane是成员变量,因此只在界面移除tableViewPane是不够的,tableViewPane依然保留了之前查询结果的字符和数据信息,因此需要将这些信息移除掉
            tableViewPane.clearTableView();
            getItems().remove(1);
        }
        executeSqlService.setSql(sql).setDefaultSchema(defaultSchema).setModelJson(modelJson);
//        javaFx Service异步任务执行start()方法时,需要保证Service为ready状态,service成功执行后其状态时successed状态,因此再任务结束后(成功/失败/取消),要重置service的状态
        executeSqlService.start();
    }

    /**
     * 美化SQL
     */
    public void beautifySQL() {
        String sql = sqlProperty.get();
        if (StringUtils.isEmpty(sql)) return;
        String beautifySql = SqlFormatter.standard().format(sql);
        sqlArea.setText(beautifySql);
    }

    /**
     * 取消SQL执行
     */
    public void stopSQL() {
        if (executeSqlService.isRunning()) {
            boolean cancel = executeSqlService.cancel();
            log.debug("当前SQL任务:[{}],正在执行,取消任务执行,操作结果:[{}],并重置任务状态[setOnCancelled()]", getCurrentSql(), cancel);
        }
    }

    /**
     * 添加异步任务监听器
     */
    private void addAsyncTaskListener() {
//        异步任务成功执行完成
        executeSqlService.setOnSucceeded(event -> {
            log.debug("异步任务[{}]成功执行完成,将发射事件给父组件,用以更新动态信息栏", getCurrentSql());
            Object result = event.getSource().getValue();
            Pair<Map<String, Integer>, List<Map<String, Object>>> execute = (Pair<Map<String, Integer>, List<Map<String, Object>>>) result;
            List<String> columns = new ArrayList(execute.getLeft().keySet());
            tableViewPane.setTableColumns(columns);
            tableViewPane.setTableRows(FXCollections.observableList(execute.getRight()));
//            注意:getItems().add(1,tableViewPane)这种形式是有问题的
            getItems().add(tableViewPane);
//            发送SQL执行完成事件
            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL_COMPLETE);
            userEvent.setQueryInfo(executeSqlService.getQueryInfo());
//            给该组件的父组件发射SQL执行完成事件
            EventUtil.fireEvent(getParent(), userEvent);
            executeSqlService.reset();
        });

//        异步任务执行失败
        executeSqlService.setOnFailed(event -> {
            log.error("异步任务[{}]执行失败", getCurrentSql(), event.getSource().getException());
//            executeSqlService
            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL_FAIL);
            userEvent.setException(event.getSource().getException());
//            给该组件的父组件发射SQL执行完成事件
            EventUtil.fireEvent(getParent(), userEvent);
            executeSqlService.reset();
        });

//        异步任务取消
        executeSqlService.setOnCancelled(event -> {
            log.warn("异步任务[{}]被取消", getCurrentSql());
            executeSqlService.reset();
        });
    }


    /**
     * 得到当前的sql内容
     *
     * @return
     */
    public String getCurrentSql() {
        return sqlArea.getText();
    }

}
