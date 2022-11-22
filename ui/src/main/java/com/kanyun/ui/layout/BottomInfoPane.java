package com.kanyun.ui.layout;

import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.model.DataBaseModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 下方信息条组件
 */
public class BottomInfoPane extends HBox {

    private static final Logger log = LoggerFactory.getLogger(DataBasePane.class);

    public BottomInfoPane() {
        setId("BottomInfoPane");
        setPrefHeight(30);
        setAlignment(Pos.CENTER_LEFT);
//        设置节点之间的间距
        setSpacing(20);
        Label selectItemsLabel = new Label();
//        selectItemsLabel.setPadding(new Insets(0, 5, 0, 5));
        selectItemsLabel.setPrefWidth(160);
        selectItemsLabel.setText(JsonQuery.dataBaseModels.size() + "个数据库");
        addEventHandler(UserEvent.DATABASE_MODIFY, event -> {
            selectItemsLabel.setText(JsonQuery.dataBaseModels.size() + "个数据库");
        });
        addEventHandler(UserEvent.SELECT_ITEMS, event -> {
            selectItemsLabel.setText(JsonQuery.dataBaseModels.size() + "个被选择");
        });

        Label dynamicInfoLabel = new Label();
//        Hgrow是 horizontal grow缩写意为水平增长，在这里是水平增长沾满窗口
        HBox.setHgrow(dynamicInfoLabel, Priority.ALWAYS);
        addEventHandler(UserEvent.CURRENT_DATABASE, event -> {
            DataBaseModel dataBaseModel = event.getDataBaseModel();
            dynamicInfoLabel.setText(dataBaseModel.getName());
            ModelJson.getModelJson(dataBaseModel.getName());
        });

        addEventHandler(UserEvent.EXECUTE_SQL, event -> {
            log.debug("设置动态SQL信息:[{}]", event.getSql());
            dynamicInfoLabel.setText(event.getSql());
        });
        getChildren().addAll(selectItemsLabel, dynamicInfoLabel);

        widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                log.debug("监测到BottomInfoPane组件宽度发生变化,旧值:[{}],新值:[{}]", oldValue, newValue);
                selectItemsLabel.setPrefWidth(newValue.intValue() * 0.2);
            }
        });
    }
}
