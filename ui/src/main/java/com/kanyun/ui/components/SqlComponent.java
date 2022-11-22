package com.kanyun.ui.components;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.jfoenix.controls.JFXTextArea;
import com.kanyun.sql.SqlExecute;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.layout.TopButtonPane;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import org.apache.commons.lang3.tuple.Pair;
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

    public SqlComponent() {
        setId("SqlComponent");
//        节点中子布局不会随拖动变化大小
//        setResizableWithParent();

        setOrientation(Orientation.VERTICAL);
        TextArea sqlArea = new TextArea();
        getItems().addAll(sqlArea);
//        setDividerPositions(0.2);

//        监听SQL执行
        addEventHandler(UserEvent.EXECUTE_SQL, event -> {
            String sql = sqlArea.getText();
            String defaultSchema = event.getDataBaseModel().getName();
            String modelJson = ModelJson.getModelJson(defaultSchema);
            try {
                Pair<Map<String, Integer>, List<Map<String, Object>>> execute = SqlExecute.execute(modelJson, sql);
                TableViewPane tableViewPane = new TableViewPane();
                List<String> columns = new ArrayList<String>(execute.getLeft().keySet());
                tableViewPane.setTableViewColumns(columns);
                tableViewPane.setItems(FXCollections.observableList(execute.getRight()));
//                移除tableView,再添加tableView(注意先判断当前项数量再移除)
                if (getItems().size() > 1) {
                    getItems().remove(1);
                }
                getItems().add(tableViewPane);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }

            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL);
            userEvent.setSql(sql);
            UserEventBridgeService.bridgeUserEvent2BottomInfoPane(userEvent);
        });

//        美化SQL
        addEventHandler(UserEvent.BEAUTIFY_SQL, event -> {
            String sql = sqlArea.getText();
            String beautifySql = SqlFormatter.standard().format(sql);
            sqlArea.setText(beautifySql);
        });

    }
}
