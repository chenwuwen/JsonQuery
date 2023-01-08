package com.kanyun.ui.components;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.jfoenix.controls.JFXTextArea;
import com.kanyun.sql.QueryInfoHolder;
import com.kanyun.sql.SqlExecute;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.event.ExecuteSqlService;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.layout.TopButtonPane;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import org.apache.calcite.util.Static;
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

        addListener();
    }

    private void addListener() {
//        监听SQL执行
        addEventHandler(UserEvent.EXECUTE_SQL, event -> {
            log.warn("88999");
            String sql = sqlProperty.get();
            String defaultSchema = event.getDataBaseModel().getName();
            String modelJson = ModelJson.getModelJson(defaultSchema);
            executeSqlService.setSql(sql).setDefaultSchema(defaultSchema).setModelJson(modelJson);
            executeSqlService.start();
        });

//        美化SQL事件
        addEventHandler(UserEvent.BEAUTIFY_SQL, event -> {
            String sql = sqlProperty.get();
            String beautifySql = SqlFormatter.standard().format(sql);
            sqlArea.setText(beautifySql);
        });

        executeSqlService.setOnSucceeded(event -> {
            Object value = event.getSource().getValue();
            Pair<Map<String, Integer>, List<Map<String, Object>>> execute = (Pair<Map<String, Integer>, List<Map<String, Object>>>) value;
            List<String> columns = new ArrayList(execute.getLeft().keySet());
            tableViewPane.setTableColumns(columns);
            tableViewPane.setTableRows(FXCollections.observableList(execute.getRight()));
//                移除tableView,再添加tableView(注意先判断当前项数量再移除),注意:getItems().add(1,tableViewPane)这种形式是有问题的
            if (getItems().size() > 1) {
                getItems().remove(1);
            }
            getItems().add(tableViewPane);
//            发送SQL执行完成事件
            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL_COMPLETE);
            userEvent.setQueryInfo(executeSqlService.getQueryInfo());
            UserEventBridgeService.bridgeUserEvent2BottomInfoPane(userEvent);
        });

        executeSqlService.setOnFailed(event -> {

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

    /**
     * 执行SQL并且更新UI
     */
    private void executeSqlAndUpdateUI(String modelJson, String defaultSchema, String sql) {
        try {

            Pair<Map<String, Integer>, List<Map<String, Object>>> execute = SqlExecute.execute(modelJson, defaultSchema, sql);
            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL_COMPLETE);
            userEvent.setQueryInfo(QueryInfoHolder.getQueryInfo());
            UserEventBridgeService.bridgeUserEvent2BottomInfoPane(userEvent);
            List<String> columns = new ArrayList(execute.getLeft().keySet());
            tableViewPane.setTableColumns(columns);
            tableViewPane.setTableRows(FXCollections.observableList(execute.getRight()));
//                移除tableView,再添加tableView(注意先判断当前项数量再移除),注意:getItems().add(1,tableViewPane)这种形式是有问题的
            if (getItems().size() > 1) {
                getItems().remove(1);
            }
            getItems().add(tableViewPane);
        } catch (Exception exception) {
            exception.printStackTrace();
            ExceptionDialog sqlExecuteErrDialog = new ExceptionDialog(exception);
            sqlExecuteErrDialog.setTitle("SQL执行报错");
            sqlExecuteErrDialog.show();
        }
    }
}
