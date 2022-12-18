package com.kanyun.ui.tabs;

import com.kanyun.sql.SqlExecute;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.components.TableViewPane;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.model.TableModel;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * 表全量数据Tab
 */
public class TabTablePane extends VBox {

    private static final Logger log = LoggerFactory.getLogger(TabTablePane.class);

    public TabTablePane(TableModel tableModel) {
        log.debug("查询表页面被新建,[{}.{}] 被打开", tableModel.getDataBaseName(), tableModel.getTableName());

        try {
            Pair<Map<String, Integer>, List<Map<String, Object>>> result = getTableData(tableModel);
//            字段名与字段类型映射信息
            Map<String, Integer> columnInfos = result.getLeft();
//            表数据
            List<Map<String, Object>> data = result.getRight();

            List<String> columns = new ArrayList<String>(columnInfos.keySet());

            TableViewPane tableViewPane = new TableViewPane();
            tableViewPane.setTableViewColumns(columns);
            tableViewPane.setItems(FXCollections.observableList(data));

            getChildren().add(tableViewPane);


        } catch (SQLException exception) {
            exception.printStackTrace();
        }

    }

    /**
     * 执行SQL查询,并得到数据
     * 返回值是元组Pair 元组左值是字段对应的类型 元组右值是表数据
     *
     * @param tableModel
     * @return
     * @throws SQLException
     */
    public Pair<Map<String, Integer>, List<Map<String, Object>>> getTableData(TableModel tableModel) throws SQLException {
        String defaultSchema = tableModel.getDataBaseName();
        String modelJson = ModelJson.getModelJson(defaultSchema);
        String sql = "select * from " + tableModel.getDataBaseName() + "." + tableModel.getTableName();
        UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL);
        userEvent.setSql(sql);
        UserEventBridgeService.bridgeUserEvent2BottomInfoPane(userEvent);
        return SqlExecute.execute(modelJson, defaultSchema, sql);
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
}
