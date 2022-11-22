package com.kanyun.ui.tabs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.components.SqlComponent;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.model.DataBaseModel;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 新建查询Tab
 */
public class TabQueryPane extends VBox {
    private static final Logger log = LoggerFactory.getLogger(TabQueryPane.class);

    public TabQueryPane() {
        log.debug("TabQueryPane 新建查询页面被打开,当前默认数据库是：[{}]");
        HBox hBox = new HBox();
//        数据库下拉框,这里不再对该组件设置监听,因为当同时有多个查询Tab时,默认Schema会混乱,因此点击运行时,读取下拉框选中数据库,再设置默认Schema
        JFXComboBox<String> dataBaseComboBox = new JFXComboBox<>();
        ObservableList<DataBaseModel> dataBaseModels = JsonQuery.dataBaseModels;
        List<String> dataBaseNames = dataBaseModels.stream().map(dataBaseModel -> dataBaseModel.getName()).collect(Collectors.toList());
        dataBaseComboBox.getItems().addAll(dataBaseNames);
        String defaultSchema = ModelJson.getDefaultSchema();
        if (StringUtils.isNotEmpty(defaultSchema)) {
//            当有默认Schema时,打开新的查询页,下拉框默认选中默认的数据库
            dataBaseComboBox.getSelectionModel().select(defaultSchema);
        }

        JFXButton runBtn = new JFXButton("运行");
        runBtn.setOnAction(event -> {
            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL);
            DataBaseModel dataBaseModel = new DataBaseModel();
            dataBaseModel.setName(dataBaseComboBox.getValue());
            userEvent.setDataBaseModel(dataBaseModel);
            UserEventBridgeService.bridgeUserEvent2SqlComponent(userEvent);
        });
        JFXButton beautifyBtn = new JFXButton("美化SQL");



        beautifyBtn.setOnAction(event -> {
            UserEvent userEvent = new UserEvent(UserEvent.BEAUTIFY_SQL);
            UserEventBridgeService.bridgeUserEvent2SqlComponent(userEvent);
        });
        hBox.getChildren().addAll(dataBaseComboBox, runBtn, beautifyBtn);
        SqlComponent sqlComponent = new SqlComponent();
        getChildren().addAll(hBox, sqlComponent);
    }
}
