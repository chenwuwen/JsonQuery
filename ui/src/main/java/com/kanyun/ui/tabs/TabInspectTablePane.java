package com.kanyun.ui.tabs;

import com.kanyun.sql.core.column.ColumnType;
import com.kanyun.sql.core.column.JsonTableColumn;
import com.kanyun.sql.core.column.JsonTableColumnFactory;
import com.kanyun.ui.model.TableModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.spreadsheet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
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

    public TabInspectTablePane(TableModel tableModel) throws Exception {
        log.debug("检查表页面被新建,[{}.{}] 被打开", tableModel.getSchemaName(), tableModel.getTableName());
        HBox toolBar = new HBox();
        initToolBar(toolBar);
        TabPane tabPane = new TabPane();
        Tab fieldTab = createFieldTab(tableModel);
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
        SpreadsheetView spreadsheetView = new SpreadsheetView();
//        表字段类型集合
        List<String> columnsTypes = Arrays.stream(ColumnType.values()).map(x -> x.toCode()).collect(Collectors.toList());
//        显示列头(列标题)
        spreadsheetView.setShowColumnHeader(true);
//        显示行头(行序号)
        spreadsheetView.setShowRowHeader(true);
        ObservableList<ObservableList<SpreadsheetCell>> fieldRows = FXCollections.observableArrayList();
//        JsonTableColumnFactory是sql模块下的类,它将从缓存中拿到表的字段信息,如果缓存中没有,则从数据库获取,如果数据库中没有,则解析表对应的json文件.
        List<JsonTableColumn> tableColumnInfoList = JsonTableColumnFactory.getTableColumnInfoList(new File(tableModel.getPath()), tableModel.getSchemaName(), tableModel.getTableName());
        GridBase grid = new GridBase(tableColumnInfoList.size(), 2);
        dynamicInfoProperty.set("字段总数：" + tableColumnInfoList.size());
        int rowIndex = 0;
        for (JsonTableColumn jsonTableColumn : tableColumnInfoList) {
            String columnName = jsonTableColumn.getName();
            String columnType = jsonTableColumn.getType().toCode();
            ObservableList<SpreadsheetCell> fieldRow = FXCollections.observableArrayList();
            SpreadsheetCell columnCell = SpreadsheetCellType.STRING.createCell(rowIndex, 0, 1, 1, columnName);
//            字段名不支持修改,如果要修改字段名也需要修改Json文件中字段名
            columnCell.setEditable(false);
            SpreadsheetCell typeCell = SpreadsheetCellType.LIST(columnsTypes).createCell(rowIndex, 1, 1, 1, columnType);
            fieldRow.addAll(columnCell, typeCell);
            fieldRows.add(fieldRow);
        }
        grid.setRows(fieldRows);
        spreadsheetView.setGrid(grid);
        fieldTab.setContent(spreadsheetView);
        return fieldTab;
    }

    /**
     * 初始化工具栏
     */
    public void initToolBar(HBox toolBar) {
        Button button = new Button("保存");
//        工具栏添加子元素
        toolBar.getChildren().addAll(button);
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
