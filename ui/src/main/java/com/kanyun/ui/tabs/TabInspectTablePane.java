package com.kanyun.ui.tabs;

import com.kanyun.ui.model.TableModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 检查表Tab
 */
public class TabInspectTablePane extends VBox {

    private static final Logger log = LoggerFactory.getLogger(TabInspectTablePane.class);

    public TabInspectTablePane(TableModel tableModel) throws Exception {
        log.debug("检查表页面被新建,[{}.{}] 被打开", tableModel.getDataBaseName(), tableModel.getTableName());
        HBox toolBar = new HBox();
        initToolBar(toolBar);
        TabPane tabPane = new TabPane();
        Tab fieldTab = createFieldTab(tableModel);
        tabPane.getTabs().add(fieldTab);
        getChildren().addAll(toolBar, tabPane);
    }

    /**
     * 创建字段类型Tab
     */
    public Tab createFieldTab(TableModel tableModel) {
        Tab fieldTab = new Tab("字段");
        SpreadsheetView spreadsheetView = new SpreadsheetView();
        GridBase grid = new GridBase(10, 2);
//        显示列头
        spreadsheetView.setShowColumnHeader(true);
//        显示行头
        spreadsheetView.setShowRowHeader(true);
        ObservableList<ObservableList<SpreadsheetCell>> fieldRows = FXCollections.observableArrayList();
        Map<String, String> fieldInfos = tableModel.getFieldInfo();
        int rowIndex = 0;
        for (Map.Entry<String, String> entry : fieldInfos.entrySet()) {
            String column = entry.getKey();
            String type = entry.getValue();
            ObservableList<SpreadsheetCell> fieldRow = FXCollections.observableArrayList();
            SpreadsheetCell columnCell = SpreadsheetCellType.STRING.createCell(rowIndex, 0, 1, 1, column);
            SpreadsheetCell typeCell = SpreadsheetCellType.STRING.createCell(rowIndex, 1, 1, 1, type);
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

}
