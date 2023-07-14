package com.kanyun.ui.tabs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;
import com.kanyun.sql.core.column.ColumnType;
import com.kanyun.sql.core.column.JsonTableColumn;
import com.kanyun.sql.core.column.JsonTableColumnFactory;
import com.kanyun.ui.components.TableColumnActionBar;
import com.kanyun.ui.model.TableMetaData;
import com.kanyun.ui.model.TableModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.tableview2.TableColumn2;
import org.controlsfx.control.tableview2.TableView2;
import org.controlsfx.control.tableview2.cell.ComboBox2TableCell;
import org.controlsfx.control.tableview2.cell.TextField2TableCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 检查表Tab
 */
public class TabInspectTablePane extends VBox implements TabKind {

    private static final Logger log = LoggerFactory.getLogger(TabInspectTablePane.class);

    /**
     * 动态信息栏
     */
    private StatusBar dynamicInfoStatusBar;

    /**
     * 动态信息属性
     */
    private SimpleStringProperty dynamicInfoProperty = new SimpleStringProperty();

    /**
     * 表字段信息Table
     */
    private TableView2<TableMetaData> metaInfoTableView;


    public TabInspectTablePane(TableModel tableModel) throws Exception {
        log.debug("检查表页面被新建,[{}.{}] 被打开", tableModel.getSchemaName(), tableModel.getTableName());
        TabPane tabPane = new TabPane();
        Tab fieldTab = createFieldTab(tableModel);
        TableColumnActionBar toolBar = new TableColumnActionBar(metaInfoTableView,tableModel.getSchemaName(), tableModel.getTableName());
        tabPane.getTabs().add(fieldTab);
        statusBarInit();
        getChildren().addAll(toolBar, tabPane);
    }

    /**
     * 创建字段类型Tab
     */
    public Tab createFieldTab(TableModel tableModel) {
        Tab fieldTab = new Tab("字段");
        fieldTab.setClosable(false);

        metaInfoTableView = new TableView2<>();
//        显示行头
        metaInfoTableView.rowHeaderVisibleProperty().set(true);
//        显示表头按钮
        metaInfoTableView.tableMenuButtonVisibleProperty().set(true);
//        设置是否多选(默认单选)
        metaInfoTableView.getSelectionModel().selectionModeProperty().setValue(SelectionMode.SINGLE);
//        表格允许编辑(如需修改Cell的值,1:需要配合column.setEditable(true)使用,2:column需要设置cellFactory)
        metaInfoTableView.editableProperty().set(true);

        final TableColumn2<TableMetaData, String> columnNameField = new TableColumn2<>("名称");
        final TableColumn2<TableMetaData, String> columnTypeField = new TableColumn2<>("类型");

        columnNameField.setCellValueFactory(meta -> meta.getValue().columnNameProperty());
//        由于未设置该列的cellFactory属性,因此即使该列设置为允许编辑,也无法进行编辑
        columnNameField.setEditable(true);
        columnNameField.setCellFactory(TextField2TableCell.forTableColumn());
        columnNameField.setPrefWidth(200);

        columnTypeField.setCellValueFactory(meta -> meta.getValue().columnTypeProperty());
//        设置此列单元格格式为下拉框
        columnTypeField.setCellFactory(ComboBox2TableCell.forTableColumn(ColumnType.codes()));
//        设置列可编辑
        columnTypeField.setEditable(true);
        columnTypeField.setPrefWidth(150);

        metaInfoTableView.getColumns().setAll(columnNameField, columnTypeField);

        List<JsonTableColumn> tableColumnInfoList = JsonTableColumnFactory.getTableColumnInfoList(new File(tableModel.getPath()), tableModel.getSchemaName(), tableModel.getTableName());
        List<TableMetaData> collect = tableColumnInfoList.stream().map(jsonTableColumn -> {
            return TableMetaData.newBuilder(tableModel.getSchemaName(), tableModel.getTableName())
                    .setColumnName(jsonTableColumn.getName())
                    .setColumnType(jsonTableColumn.getType().toCode()).builder();
        }).collect(Collectors.toList());
//        设置tableView的数据
        metaInfoTableView.setItems(FXCollections.observableList(collect));
        fieldTab.setContent(metaInfoTableView);
        dynamicInfoProperty.set("字段总数：" + tableColumnInfoList.size());
        return fieldTab;
    }


    @Override
    public TabKindEnum getTabKind() {
        return TabKindEnum.INSPECT_TAB;
    }

    @Override
    public void createDynamicInfoStatusBar() {
        dynamicInfoStatusBar = new StatusBar();
//        不设置的话,默认有个OK字样
        dynamicInfoStatusBar.setText("");
        dynamicInfoStatusBar.textProperty().bind(dynamicInfoProperty);
    }

    @Override
    public StatusBar getDynamicInfoStatusBar() {
        return dynamicInfoStatusBar;
    }

    @Override
    public void addStatusBarEventListener() {

    }

}
