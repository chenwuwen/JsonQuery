package com.kanyun.ui.layout;

import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.model.DataBaseModel;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 下方信息条组件
 */
public class BottomInfoPane extends HBox {

    private static final Logger log = LoggerFactory.getLogger(DataBasePane.class);

//    private SimpleStringProperty simpleStringProperty = new SimpleStringProperty();

    private SimpleDoubleProperty posProperty = new SimpleDoubleProperty();
    private SimpleDoubleProperty parentWidthProperty = new SimpleDoubleProperty();

    /**
     * DataBase侧状态组件
     */
    private StatusBar dataBaseInfoStatusBar;
    /**
     * 内容区动态信息组件
     */
    private StatusBar dynamicInfoStatusBar;

    public BottomInfoPane() {
        setId("BottomInfoPane");
        setPrefHeight(30);
        setAlignment(Pos.CENTER_LEFT);
//        设置节点之间的间距
        setSpacing(0);
        getChildren().addAll(createDataBaseInfo(), createDynamicInfo());
//      Hgrow是 horizontal grow缩写意为水平增长，在这里是水平增长沾满窗口
//        HBox.setHgrow(dataBaseInfoStatusBar, Priority.ALWAYS);
//        这里只设置一个组件为动态增长,另一个组件则手动设置值(通过监听器),如果设置两个组件都水平增长,则单独给组件设置宽度值是没有效果的
        HBox.setHgrow(dynamicInfoStatusBar, Priority.ALWAYS);
        widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                log.debug("监测到BottomInfoPane组件宽度发生变化,旧值:[{}],新值:[{}],将动态改变dataBaseInfoStatusBar的宽度", oldValue, newValue);
//                这里不再设置dataBaseInfoStatusBar的宽度,因为默认的分隔比例可能已经失效,此处只记录新值
//                setDataBaseInfoStatusBarWith(newValue.doubleValue(), 0.2);
                parentWidthProperty.set(newValue.doubleValue());
            }
        });

    }

    public StatusBar createDataBaseInfo() {
        dataBaseInfoStatusBar = new StatusBar();
//        不设置的话,默认有个OK字样
        dataBaseInfoStatusBar.setText("");
//        提前设置一个Node
        dataBaseInfoStatusBar.getLeftItems().add(new Label(JsonQuery.dataBaseModels.size() + "个数据库"));
        addEventHandler(UserEvent.DATABASE_MODIFY, event -> {
//            注意这里是set(index,node),如果指定索引没有Node会报错
            dataBaseInfoStatusBar.getLeftItems().set(0, new Label(JsonQuery.dataBaseModels.size() + "个数据库"));
        });
        addEventHandler(UserEvent.ITEMS_COUNT, event -> {
//            注意这里是set(index,node),如果指定索引没有Node会报错
            dataBaseInfoStatusBar.getLeftItems().set(0, new Label(JsonQuery.dataBaseModels.size() + "被选中"));
        });

        addEventHandler(UserEvent.CURRENT_DATABASE, event -> {
            DataBaseModel dataBaseModel = event.getDataBaseModel();
            ImageView imageView = new ImageView("/asserts/database.png");
            if (dataBaseInfoStatusBar.getRightItems().size() < 1) {
//                添加分隔线
                dataBaseInfoStatusBar.getRightItems().add(new Separator(Orientation.VERTICAL));
//               当前选中的数据库
                dataBaseInfoStatusBar.getRightItems().add(1, new Label(dataBaseModel.getName()));
            } else {
                dataBaseInfoStatusBar.getRightItems().set(1, new Label(dataBaseModel.getName()));
            }

            ModelJson.getModelJson(dataBaseModel.getName());
        });
        return dataBaseInfoStatusBar;
    }

    public StatusBar createDynamicInfo() {
        dynamicInfoStatusBar = new StatusBar();
//        不设置的话,默认有个OK字样
        dynamicInfoStatusBar.setText("");
//        提前设置一个空Label,避免后面直接set报错
        dynamicInfoStatusBar.getLeftItems().add(new Label(""));
        addEventHandler(UserEvent.EXECUTE_SQL, event -> {
//            去掉SQL中的换行符
            String sql = event.getSql().replaceAll("\r|\n|\t", "");
            log.debug("设置动态SQL信息:[{}]", sql);
            dynamicInfoStatusBar.getLeftItems().set(0, new Label(event.getSql()));
        });
        return dynamicInfoStatusBar;
    }

    /**
     * 动态设置dataBaseInfoStatusBar的宽度
     *
     * @param pos        比例
     * @param parentWith 父级宽度
     */
    public void setDataBaseInfoStatusBarWith(Double parentWith, Double pos) {
        dataBaseInfoStatusBar.setPrefWidth(parentWidthProperty.get() * pos);
    }


}
