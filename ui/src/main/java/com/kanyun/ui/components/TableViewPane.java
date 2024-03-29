package com.kanyun.ui.components;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

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
        configTableView();
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

    /**
     * 配置TableView
     */
    private void configTableView() {
        tableView.setRowFactory(new Callback<TableView<Map<String, Object>>, TableRow<Map<String, Object>>>() {
            @Override
            public TableRow<Map<String, Object>> call(TableView<Map<String, Object>> param) {
                return new TableRow<Map<String, Object>>(){
                    @Override
                    protected void updateItem(Map<String, Object> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
//                            空行不显示样式
                            setStyle("-fx-background-color: transparent;-fx-border-width: 0;-fx-border-color: transparent;");
                        }
                    }
                };
            }
        });
    }

    /**
     * 导出数据
     * @return
     */
    public Pair<List<String>,List<Map<String, Object>>> exportData() {
        ObservableList<Map<String, Object>> items = tableView.getItems();
        List<String> collect = tableView.getColumns().stream().map(column -> column.getText()).collect(Collectors.toList());
        Iterator<Map<String, Object>> iterator = items.iterator();
        List<Map<String, Object>> tableData = new LinkedList<>();
        while (iterator.hasNext()) {
            Map<String, Object> next = iterator.next();
            tableData.add(next);
        }
        return Pair.of(collect, tableData);
    }

}
