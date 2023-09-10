package com.kanyun.ui.components;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbarLayout;
import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;
import com.kanyun.sql.core.column.ColumnType;
import com.kanyun.sql.core.column.JsonTableColumn;
import com.kanyun.sql.core.column.JsonTableColumnFactory;
import com.kanyun.ui.IconProperties;
import com.kanyun.ui.model.TableMetaData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.tableview2.TableView2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 检查表标签页的工具栏,继承自ToolBar
 */
public class TableColumnActionBar extends ToolBar {
    private static final Logger log = LoggerFactory.getLogger(TableColumnActionBar.class);

    private static final int ICON_SIZE = 13;

    /**
     * 表字段信息Table
     */
    private TableView2<TableMetaData> metaInfoTableView;
    /**
     * 数据库名称
     */
    private String schema;

    /**
     * 表名
     */
    private String table;

    public TableColumnActionBar(TableView2<TableMetaData> metaInfoTableView, String schema, String table) {
        this.metaInfoTableView = metaInfoTableView;
        this.schema = schema;
        this.table = table;
        setPadding(new Insets(0.5, 0, 0.5, 2));
        addTableFieldActions();
    }

    /**
     * 检查表Tab,字段操作工具条
     */
    private void addTableFieldActions() {
        Color color = Color.GRAY;

//      (icomoon.floppy-o, save) icomoon为图标的自定义前缀,其在加载字体时设置.
        JFXButton saveFieldBtn = new JFXButton("保存", IconProperties.getIcon("field.inspect.save", ICON_SIZE, color));
//        分割线(同一个组件对象只能添加一次)
        Separator separator0 = new Separator(Orientation.VERTICAL);
        Separator separator1 = new Separator(Orientation.VERTICAL);
        Separator separator2 = new Separator(Orientation.VERTICAL);

        JFXButton addFieldBtn = new JFXButton("添加字段", IconProperties.getIcon("field.inspect.add_field", ICON_SIZE, color));
        JFXButton delFieldBtn = new JFXButton("移除字段", IconProperties.getIcon("field.inspect.remove_field", ICON_SIZE, color));
        JFXButton istFieldBtn = new JFXButton("插入字段", IconProperties.getIcon("field.inspect.insert_field", ICON_SIZE, color));
        JFXButton moveUpFieldBtn = new JFXButton("上移字段", IconProperties.getIcon("field.inspect.move_up", ICON_SIZE, color));
        JFXButton moveDownFieldBtn = new JFXButton("下移字段", IconProperties.getIcon("field.inspect.move_down", ICON_SIZE, color));
        JFXButton reGenerateFieldBtn = new JFXButton("重生成字段", IconProperties.getIcon("field.inspect.regenerate", ICON_SIZE, color));
//        工具栏添加子元素
        getItems().addAll(saveFieldBtn, separator0, addFieldBtn, delFieldBtn, istFieldBtn, separator1, moveUpFieldBtn, moveDownFieldBtn, separator2, reGenerateFieldBtn);
        addFieldBtn.setOnAction(event -> {
            metaInfoTableView.getItems().add(new TableMetaData());
        });
        saveFieldBtn.setOnAction(event -> {
            try {
                ObservableList<TableMetaData> items = metaInfoTableView.getItems();
                List<JsonTableColumn> collect = items.stream()
                        .filter(item -> StringUtils.isNotEmpty(item.getColumnName()) && StringUtils.isNotEmpty(item.getColumnType()))
                        .map(item -> {
                            JsonTableColumn jsonTableColumn = new JsonTableColumn();
                            jsonTableColumn.setName(item.getColumnName());
                            jsonTableColumn.setDefaultValue(item.getColumnDefaultValue());
                            jsonTableColumn.setType(ColumnType.getColumnTypeByCode(item.getColumnType()));
                            return jsonTableColumn;
                        }).collect(Collectors.toList());
                JsonTableColumnFactory.refreshTableColumnInfo(items.get(0).getSchema(), items.get(0).getTable(), collect);
//            dynamicInfoProperty.set("字段总数：" + items.size());
                showSuccessMsg();
            } catch (Exception e) {
                log.error("字段信息保存失败:", e);
                showErrorMsg(e.getMessage());
            }
        });
        delFieldBtn.setOnAction(event -> {
//            当前被选中行
            ObservableList<TableMetaData> selectedItems = metaInfoTableView.getSelectionModel().getSelectedItems();
            metaInfoTableView.getItems().removeAll(selectedItems);
        });
        istFieldBtn.setOnAction(event -> {
//            选中行的索引(没有选中为-1)
            int selectedIndex = metaInfoTableView.getSelectionModel().getSelectedIndex();
            log.info("插入字段,当前选中行索引:{}", selectedIndex);
            if (selectedIndex != -1) {
//                插入字段的具体思路是,创建一个新的数据集合,以选中行为分隔点,分批将旧数据集合的内容添加到新的数据集合中去
                ObservableList<TableMetaData> newItems = FXCollections.observableArrayList();
                for (int i = 0; i < selectedIndex; i++) {
                    newItems.add(metaInfoTableView.getItems().get(i));
                }
//                新添加的字段行
                newItems.add(new TableMetaData());
                int size = metaInfoTableView.getItems().size();
                for (int i = selectedIndex; i < size; i++) {
                    newItems.add(metaInfoTableView.getItems().get(i));
                }
                metaInfoTableView.setItems(newItems);
            } else {
//                如果未选中任何行,则直接在最后添加一行
                metaInfoTableView.getItems().add(new TableMetaData());
            }
        });
        moveUpFieldBtn.setOnAction(event -> {
            int selectedIndex = metaInfoTableView.getSelectionModel().getSelectedIndex();
            if (selectedIndex == -1 || selectedIndex == 0) {
                return;
            }
//            当前选中的Item
            TableMetaData selectedItem = metaInfoTableView.getSelectionModel().getSelectedItem();
//            与之交换的Item
            TableMetaData swapItem = metaInfoTableView.getItems().get(selectedIndex - 1);
            metaInfoTableView.getItems().set(selectedIndex, swapItem);
            metaInfoTableView.getItems().set(selectedIndex - 1, selectedItem);
        });
        moveDownFieldBtn.setOnAction(event -> {
            int selectedIndex = metaInfoTableView.getSelectionModel().getSelectedIndex();
            int lastIndex = metaInfoTableView.getItems().size() - 1;
            if (selectedIndex == -1 || selectedIndex == lastIndex) {
                return;
            }
//            当前选中的Item
            TableMetaData selectedItem = metaInfoTableView.getSelectionModel().getSelectedItem();
//            与之交换的Item
            TableMetaData swapItem = metaInfoTableView.getItems().get(selectedIndex + 1);
            metaInfoTableView.getItems().set(selectedIndex, swapItem);
            metaInfoTableView.getItems().set(selectedIndex + 1, selectedItem);
        });
        reGenerateFieldBtn.setOnAction(event -> {
            ReGenerateTableFieldDialog reGenerateTableFieldDialog = new ReGenerateTableFieldDialog(schema, table) {
                @Override
                public void handlerResult(ObservableList<TableMetaData> tableMetaDataObservableList) {
                    metaInfoTableView.setItems(tableMetaDataObservableList);
                }
            };
            reGenerateTableFieldDialog.show(this);
        });
    }


    /**
     * 字段保存成功提示
     */
    private void showSuccessMsg() {
//        Notifications notificationBuilder = Notifications.create()
//                .text("保存成功")
//                .hideAfter(Duration.seconds(2))
//                .position(Pos.CENTER)
////                通知数超过阈值时折叠所有通知为一个通知,0为禁用阈值
//                .threshold(3, Notifications.create().title("Threshold Notification"));
//        notificationBuilder.showInformation();

        JFXSnackbar snackbar = new JFXSnackbar((Pane)this.getParent());
        snackbar.setPrefWidth(300);
        snackbar.fireEvent(new JFXSnackbar.SnackbarEvent(new JFXSnackbarLayout("Toast Message " )));

    }

    /**
     * 字段保存失败提示
     */
    private void showErrorMsg(String msg) {
        Notifications notificationBuilder = Notifications.create()
                .title("保存失败")
                .text(msg)
                .hideAfter(Duration.seconds(2))
                .position(Pos.CENTER)
//                通知数超过阈值时折叠所有通知为一个通知,0为禁用阈值
                .threshold(3, Notifications.create().title("Threshold Notification"));
        notificationBuilder.showInformation();
    }
}
