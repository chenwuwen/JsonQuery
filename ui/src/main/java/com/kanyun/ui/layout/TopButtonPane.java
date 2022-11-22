package com.kanyun.ui.layout;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.kanyun.sql.func.AbstractFuncSource;
import com.kanyun.sql.func.FuncSourceType;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.components.FunctionDialog;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.model.DataBaseModel;
import com.sun.javafx.event.EventUtil;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.StringJoiner;

/**
 * 顶部按钮区组件
 */
public class TopButtonPane extends FlowPane {

    private static final Logger log = LoggerFactory.getLogger(TopButtonPane.class);


    public TopButtonPane() {
//        setAlignment(Pos.CENTER_LEFT);
        MaterialDesignIconView materialDesignIconView =
                new MaterialDesignIconView(MaterialDesignIcon.THUMB_UP);
//        ImageView imageView = new ImageView("/assets/茶壶.png");
//        imageView.setFitWidth(16);
//        imageView.setFitHeight(16);
        JFXButton dataBaseBtn = new JFXButton("新建数据库", materialDesignIconView);

//        dataBaseBtn.setPrefWidth();
        dataBaseBtn.setButtonType(JFXButton.ButtonType.FLAT);
        JFXButton queryBtn = new JFXButton("新的查询");

        JFXButton udfBtn = new JFXButton("添加函数");

        queryBtn.setOnAction(event -> {
            UserEvent userEvent = new UserEvent(UserEvent.NEW_QUERY);
            UserEventBridgeService.bridgeUserEvent2ContentPane(userEvent);
        });

        dataBaseBtn.setOnAction(event -> {
            createDataBaseDialog();
        });

        udfBtn.setOnAction(event -> {
            addUdfDialog();
        });

        getChildren().add(dataBaseBtn);
        getChildren().add(queryBtn);
        getChildren().add(udfBtn);

    }

    /**
     * 添加数据库弹窗
     */
    private void createDataBaseDialog() {
        Dialog jfxDialog = new Dialog();
        DialogPane dialogPane = jfxDialog.getDialogPane();
        jfxDialog.setTitle("添加数据库");

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(15);

        TextField dataBaseNameTextField = new TextField();
        Label dataBaseNameLabel = new Label("数据库名称: ");

        Label dataBaseDirLabel = new Label("数据库地址: ");
        HBox hBox = new HBox();
//        设置元素间距
        hBox.setSpacing(5);
        TextField dataBaseUrlTextField = new TextField();
        dataBaseUrlTextField.setEditable(false);
        Button button = new Button("数据库文件路径");
        hBox.getChildren().addAll(dataBaseUrlTextField, button);

        button.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("请选择你的数据库路径");
            File selectedDirectory = directoryChooser.showDialog(new Stage());
            if (selectedDirectory != null && selectedDirectory.isDirectory()) {
                dataBaseUrlTextField.setText(selectedDirectory.getPath());

            }
        });
        gridPane.add(dataBaseNameLabel, 0, 0);
        gridPane.add(dataBaseNameTextField, 1, 0);
        gridPane.add(dataBaseDirLabel, 0, 1);
        gridPane.add(hBox, 1, 1);
        dialogPane.setPrefSize(400, 250);
        dialogPane.setContent(gridPane);
        ObservableList<ButtonType> buttonTypes = dialogPane.getButtonTypes();
        buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL);
        Button btnOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        Button btnCancel = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        jfxDialog.show();
        btnOk.setOnAction(event -> {
            DataBaseModel dataBase = new DataBaseModel();
            dataBase.setName(dataBaseNameTextField.getText());
            dataBase.setUrl(dataBaseUrlTextField.getText());
            UserEvent userEvent = new UserEvent(UserEvent.CREATE_DATABASE);
            userEvent.setDataBaseModel(dataBase);
            UserEventBridgeService.bridgeUserEvent2DataBasePane(userEvent);
        });
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
//        btnApply.setVisible(false);

        dialogPane.setPrefSize(500,300);
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
        dialog.showAndWait();
    }


}
