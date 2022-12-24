package com.kanyun.ui.tabs;

import com.jfoenix.controls.JFXButton;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.model.DataBaseModel;
import com.kanyun.ui.model.TableModel;
import com.sun.javafx.event.EventUtil;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ObjectsTab 主要用来显示数据库中的表
 */
public class TabObjectPane extends VBox {

    private static final Logger log = LoggerFactory.getLogger(TabObjectPane.class);

    public static final String TAB_NAME = "Objects";


    public TabObjectPane(String dataBaseName) {

        getChildren().add(initToolBar());

        getChildren().add(buildObjectShowPane(dataBaseName));
    }

    /**
     * 构建对象展示组件,这里的对象只有三种类型,一种是函数,一种是表
     * 或者为空
     *
     * @param dataBaseName
     * @return
     */
    public Node buildObjectShowPane(String dataBaseName) {
        FlowPane objectListPane = new FlowPane();
        objectListPane.setPadding(new Insets(10, 0, 10, 10));
//        设置方向为纵项排列
        objectListPane.setOrientation(Orientation.VERTICAL);
//        设置子组件纵项间隙
        objectListPane.setVgap(10);
//        设置子组件横项间隙
        objectListPane.setHgap(20);
        if (StringUtils.isNotEmpty(dataBaseName)) {
            ObservableList<DataBaseModel> dataBases = JsonQuery.dataBaseModels;
            for (DataBaseModel dataBase : dataBases) {
                if (dataBase.getName().equals(dataBaseName)) {
                    ObservableList<String> tables = dataBase.getTables();
                    for (String table : tables) {
                        Label label = new Label(table);
                        label.setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) {
//                                打开表
                                openTable(dataBaseName, label.getText());
                            }
                        });
                        objectListPane.getChildren().add(label);
                    }
                    break;
                }
            }
        }
        return objectListPane;
    }

    /**
     * 初始化工具栏
     *
     * @return
     */
    public Node initToolBar() {
//        顶部按钮区域
        HBox btnBox = new HBox();
        btnBox.setPrefHeight(20);
        btnBox.setStyle("-fx-background-color: #0ac");
        btnBox.setPadding(new Insets(2, 4, 2, 4));
        Button openTableBtn = new JFXButton("打开表");
        btnBox.getChildren().add(openTableBtn);
        return btnBox;
    }

    /**
     * 双击表打开表
     *
     * @param dataBaseName
     * @param tableName
     */
    private void openTable(String dataBaseName, String tableName) {
        UserEvent userEvent = new UserEvent(UserEvent.QUERY_TABLE);
        TableModel tableModel = new TableModel();
        tableModel.setTableName(tableName);
        tableModel.setDataBaseName(dataBaseName);
        userEvent.setTableModel(tableModel);
        UserEventBridgeService.bridgeUserEvent2ContentPane(userEvent);
    }
}
