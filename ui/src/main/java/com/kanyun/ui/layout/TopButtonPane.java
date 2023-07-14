package com.kanyun.ui.layout;

import com.kanyun.ui.components.FunctionDialog;
import com.kanyun.ui.components.NativeDataBaseDialog;
import com.kanyun.ui.components.TopButtonComponent;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.model.DataBaseModel;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.controlsfx.control.NotificationPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 顶部按钮区组件
 */
public class TopButtonPane extends FlowPane {

    private static final Logger log = LoggerFactory.getLogger(TopButtonPane.class);


    public TopButtonPane() {
        setPrefHeight(50);
//        setAlignment(Pos.CENTER_LEFT);
//        MaterialDesignIconView materialDesignIconView =
//                new MaterialDesignIconView(MaterialDesignIcon.THUMB_UP);
//        设置内边距,避免第一个子元素离左边框太近
        setPadding(new Insets(5,0,5,30));
//        设置元素排列方向
        setOrientation(Orientation.HORIZONTAL);
//        设置子节点元素间距
        setHgap(50);

        TopButtonComponent dataBaseBtn = new TopButtonComponent("新建", "/asserts/new_database.png");

        TopButtonComponent queryBtn = new TopButtonComponent("新的查询", "/asserts/query.png");

        TopButtonComponent udfBtn = new TopButtonComponent("添加函数", "/asserts/function.png");

//        新建查询按钮点击事件
        queryBtn.setOnMouseClicked(event -> {
            UserEvent userEvent = new UserEvent(UserEvent.NEW_QUERY);
            UserEventBridgeService.bridgeUserEvent2ContentPane(userEvent);
        });
//        添加数据库按钮点击事件
        dataBaseBtn.setOnMouseClicked(event -> {
            NativeDataBaseDialog nativeDataBaseDialog = new NativeDataBaseDialog("添加数据库") {
                @Override
                protected void apply(String dataBaseName, String dataBaseUrl) {
                    DataBaseModel dataBase = new DataBaseModel();
                    dataBase.setName(dataBaseName);
                    dataBase.setUrl(dataBaseUrl);
//                    发送添加数据库事件
                    UserEvent userEvent = new UserEvent(UserEvent.CREATE_DATABASE);
                    userEvent.setDataBaseModel(dataBase);
                    UserEventBridgeService.bridgeUserEvent2DataBasePane(userEvent);
                }
            };
            nativeDataBaseDialog.show(this);
        });

//        自定义函数按钮点击事件
        udfBtn.setOnMouseClicked(event -> {
            addUdfDialog();
        });

        getChildren().add(dataBaseBtn);
        getChildren().add(queryBtn);
        getChildren().add(udfBtn);

    }


    /**
     * 添加函数弹窗
     */
    private void addUdfDialog() {
        Dialog dialog = new Dialog();
//        javaFx Dialog无法关闭：https://www.freesion.com/article/8839730889/
        DialogPane dialogPane = dialog.getDialogPane();
        ObservableList<ButtonType> buttonTypes = dialogPane.getButtonTypes();
        buttonTypes.addAll(ButtonType.APPLY);
        Button btnApply = (Button) dialogPane.lookupButton(ButtonType.APPLY);
        dialogPane.setPrefSize(500, 300);
        dialogPane.setPadding(new Insets(0, 0, 0, 0));
        dialogPane.setStyle("-fx-border-color: #00ccff;");
        FunctionDialog functionDialog = new FunctionDialog();
        dialogPane.setContent(functionDialog);
        btnApply.setOnAction(event -> {
//            todo 此处无法使用事件传递机制,因为无法获取EventTarget,可能是因为Dialog会生成新的场景,暂时无法获取
//            UserEventBridgeService.bridgeUserEvent2FunctionDialog(new UserEvent(UserEvent.APPLY_FUNC));
            functionDialog.applyFunc();
        });
        dialog.setTitle("添加自定义函数");
//        设置dialog显示后要执行的代码,此代码一定要放在dialog.show()方法之前,否则不起作用
        dialog.setOnShown(event -> {
//            已由FunctionDialog内部实现自动显示
//            functionDialog.showFuncNotifyInfo();
        });
        dialog.showAndWait();

    }


}
