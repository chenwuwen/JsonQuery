package com.kanyun.ui.components;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.calcite.avatica.util.DateTimeUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.sql.Types;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 自定义TableViewPane,主要用来存放TableView
 */
public class TableViewPane extends StackPane {

    private TableView<Map<String, Object>> tableView;


    public TableViewPane() {
        setId("TableViewPane");
//        tableView设置数据(构造方法传入),也可以先初始化TableView 然后再设置数据  tableView.setItems(data);
        tableView = new TableView<>();
        getChildren().add(tableView);
    }

    /**
     * 设置表个内容
     *
     * @param items
     */
    public void setTableRows(ObservableList<Map<String, Object>> items) {
        tableView.setItems(items);
    }

    /**
     * 设置表格的字段信息
     * 参数的key:表示字段名
     * 参数value:表示字段类型 Integer类型 {@link java.sql.Types}
     *
     * @param columnInfos
     */
    public void setTableColumns(Map<String, Integer> columnInfos) {

        for (Map.Entry<String, Integer> columnInfo : columnInfos.entrySet()) {
            String columnName = columnInfo.getKey();
            int columnType = columnInfo.getValue().intValue();
            TableColumn<Map<String, Object>, String> col = new TableColumn<>(columnName);
//            根据结果字段的不同类型创建不同的Cell,由于columnType是int类型,Types类下的静态变量也是int类型,因此==比较没有风险,可以使用switch case
            switch (columnType) {
                case Types.INTEGER | Types.VARCHAR | Types.BIGINT:
                    col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map<String, Object>, String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue<String> call(TableColumn.CellDataFeatures<Map<String, Object>, String> param) {
                            return new SimpleStringProperty(String.valueOf((param.getValue().get(columnName))));
                        }
                    });
                case Types.DATE:
                    col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map<String, Object>, String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue<String> call(TableColumn.CellDataFeatures<Map<String, Object>, String> param) {
                            Date date = (Date) param.getValue().get(columnName);
                            String cellValue = DateFormatUtils.format(date, "yyyyMMdd HH:ss:mm");
                            return new SimpleStringProperty(cellValue);
                        }
                    });
                default:
                    col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map<String, Object>, String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue<String> call(TableColumn.CellDataFeatures<Map<String, Object>, String> param) {
                            return new SimpleObjectProperty(param.getValue().get(columnName));
                        }
                    });

            }


            tableView.getColumns().add(col);
        }
    }

    /**
     * 清空表数据及表字段信息
     */
    public void clearTableView() {
        tableView.getColumns().removeAll(tableView.getColumns());
        tableView.getItems().removeAll(tableView.getItems());
    }


}
