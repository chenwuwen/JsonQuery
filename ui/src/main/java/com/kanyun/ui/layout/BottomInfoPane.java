package com.kanyun.ui.layout;

import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.model.Constant;
import com.kanyun.ui.model.DataBaseModel;
import com.kanyun.ui.tabs.TabKind;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 下方信息条组件
 */
public class BottomInfoPane extends HBox {

    private static final Logger log = LoggerFactory.getLogger(DataBasePane.class);

    /**
     * 数据库信息属性
     */
    private SimpleStringProperty dataBaseInfoProperty = new SimpleStringProperty();

    /**
     * 动态信息属性
     */
    private SimpleStringProperty dynamicInfoProperty = new SimpleStringProperty();


    /**
     * 当前底部状态栏宽度属性(也是整个界面的宽度属性)
     */
    private SimpleDoubleProperty parentWidthProperty = new SimpleDoubleProperty();
    /**
     * DataBase侧状态组件
     */
    private StatusBar dataBaseInfoStatusBar;
    /**
     * 动态信息显示组件
     */
    private StackPane dynamicInfoPane;

    public BottomInfoPane() {
        setId("BottomInfoPane");
        setPrefHeight(30);
        setAlignment(Pos.CENTER_LEFT);
//        设置节点之间的间距
        setSpacing(0);
//        getChildren().addAll(createDataBaseInfo(), createDynamicInfo());
        dynamicInfoPane = new StackPane();

        getChildren().addAll(createDataBaseInfo(), dynamicInfoPane);
//      Hgrow是 horizontal grow缩写意为水平增长，在这里是水平增长沾满窗口
//        HBox.setHgrow(dataBaseInfoStatusBar, Priority.ALWAYS);
//        这里只设置一个组件为动态增长,另一个组件则手动设置值(通过监听器),如果设置两个组件都水平增长,则单独给组件设置宽度值是没有效果的
        HBox.setHgrow(dynamicInfoPane, Priority.ALWAYS);
        widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                log.debug("监测到BottomInfoPane组件宽度发生变化,旧值:[{}],新值:[{}],将动态改变dataBaseInfoStatusBar的宽度", oldValue, newValue);
//                这里不再设置dataBaseInfoStatusBar的宽度,因为默认的分隔比例可能已经失效,此处只记录新值
//                setDataBaseInfoStatusBarWith(newValue.doubleValue(), 0.2);
                parentWidthProperty.set(newValue.doubleValue());
            }
        });

        addEventHandler(UserEvent.DYNAMIC_SETTING_STATUS_BAR, event -> {
            StatusBar statusBar = event.getStatusBar();
            if (dynamicInfoPane.getChildren().size() < 1) {
                dynamicInfoPane.getChildren().add(statusBar);
            } else {
                dynamicInfoPane.getChildren().set(0, statusBar);
            }
        });

    }

    /**
     * 左侧数据库连接信息条(宽度跟随SplitPane设置的比例)
     *
     * @return
     */
    public StatusBar createDataBaseInfo() {
        dataBaseInfoStatusBar = new StatusBar();
//        不设置的话,默认有个OK字样
        dataBaseInfoStatusBar.setText("没有数据库");
        dataBaseInfoStatusBar.textProperty().bind(dataBaseInfoProperty);
        dataBaseInfoProperty.set(JsonQuery.dataBaseModels.size() + "个数据库");
        addEventHandler(UserEvent.DATABASE_MODIFY, event -> {
            dataBaseInfoProperty.set(JsonQuery.dataBaseModels.size() + "个数据库");

        });
        addEventHandler(UserEvent.ITEMS_COUNT, event -> {
            dataBaseInfoProperty.set(JsonQuery.dataBaseModels.size() + "被选中");
        });

        addEventHandler(UserEvent.CURRENT_DATABASE, event -> {
            DataBaseModel dataBaseModel = event.getDataBaseModel();
            ImageView imageView = new ImageView("/asserts/database.png");
            imageView.setFitWidth(dataBaseInfoStatusBar.getPrefHeight() / 2);
            imageView.setFitHeight(dataBaseInfoStatusBar.getPrefHeight() / 2);
            Label currentDbLabel = TabKind.createCommonLabel(dataBaseModel.getName(), dataBaseInfoStatusBar, Color.ORANGE, null);
            if (dataBaseInfoStatusBar.getRightItems().size() < 1) {
//                添加分隔线
                dataBaseInfoStatusBar.getRightItems().add(new Separator(Orientation.VERTICAL));
//               当前选中的数据库
                dataBaseInfoStatusBar.getRightItems().add(1, currentDbLabel);
            } else {
                dataBaseInfoStatusBar.getRightItems().set(1, currentDbLabel);
            }

            ModelJson.getModelJson(dataBaseModel.getName());
        });
        return dataBaseInfoStatusBar;
    }


    /**
     * 通过当前组件的总宽度和设置的分割比例动态设置dataBaseInfoStatusBar的宽度
     * 同时由于设置了左侧信息栏的最大宽度属性,因此需要加以判断
     * 宽度+3 是因为分割线的像素稍多几个
     * 如果后面更改了 SplitPane的分割线的样式(宽度) 将更新此值
     * @param pos 比例
     */
    public void setDataBaseInfoStatusBarWith(Double pos) {
        dataBaseInfoStatusBar.setPrefWidth(parentWidthProperty.get() * pos > Constant.DATABASE_TREE_PANE_MAX_WIDTH
                ? Constant.DATABASE_TREE_PANE_MAX_WIDTH + 3 : parentWidthProperty.get() * pos);
    }


}
