package com.kanyun.ui.components;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.jfoenix.controls.JFXTextArea;
import com.kanyun.sql.SqlExecute;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.layout.TopButtonPane;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 新建查询组件(内容区域SQL+结果)
 */
public class SqlComponent extends SplitPane {

    private static final Logger log = LoggerFactory.getLogger(SqlComponent.class);

    /**
     * SQL编写区域
     */
    private TextArea sqlArea = new TextArea();

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
        sqlArea.prefHeightProperty().bind(prefHeightProperty());
        setDividerPositions(1);
        addListener();
    }

    private void addListener() {
//        监听SQL执行
        addEventHandler(UserEvent.EXECUTE_SQL, event -> {
            String sql = sqlArea.getText();
            String defaultSchema = event.getDataBaseModel().getName();
            String modelJson = ModelJson.getModelJson(defaultSchema);
            try {
                Pair<Map<String, Integer>, List<Map<String, Object>>> execute = SqlExecute.execute(modelJson, defaultSchema, sql);
                List<String> columns = new ArrayList(execute.getLeft().keySet());
                tableViewPane.setTableViewColumns(columns);
                tableViewPane.setItems(FXCollections.observableList(execute.getRight()));
//                移除tableView,再添加tableView(注意先判断当前项数量再移除)
                if (getItems().size() > 1) {
                    getItems().remove(1);
                }
                getItems().add(tableViewPane);
            } catch (SQLException exception) {
                exception.printStackTrace();
                ExceptionDialog sqlExecuteErrDialog = new ExceptionDialog(new Exception());
                sqlExecuteErrDialog.setTitle("SQL执行报错");
                sqlExecuteErrDialog.show();
            }

            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL);
            userEvent.setSql(sql);
            UserEventBridgeService.bridgeUserEvent2BottomInfoPane(userEvent);
        });

//        美化SQL事件
        addEventHandler(UserEvent.BEAUTIFY_SQL, event -> {
            String sql = sqlArea.getText();
            String beautifySql = SqlFormatter.standard().format(sql);
            sqlArea.setText(beautifySql);
        });
    }
}
