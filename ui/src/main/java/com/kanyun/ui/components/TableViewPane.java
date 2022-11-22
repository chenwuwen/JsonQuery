package com.kanyun.ui.components;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import java.util.Collections;
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

    public void setItems(ObservableList<Map<String, Object>> items) {
        tableView.setItems(items);
    }

    public void setTableViewColumns(List<String> columns) {
        Collections.sort(columns);
        for (String column : columns) {
            TableColumn<Map<String, Object>, String> col = new TableColumn<>(column);
            col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map<String, Object>, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Map<String, Object>, String> param) {
                    return new SimpleObjectProperty(param.getValue().get(column));
                }
            });
            tableView.getColumns().add(col);
        }
    }


}
