package com.kanyun.ui.layout;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import com.kanyun.ui.model.DataBase;
import com.kanyun.ui.UserEvent;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import func.AbstractFuncSource;
import func.FuncSourceType;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class TopButtonPane extends FlowPane {


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
            System.out.println("发送新建查询事件,接收事件方是ContentPane");
            UserEvent userEvent = new UserEvent(UserEvent.NEW_QUERY);
//            通过Scene然后查找指定Id的Node对象,也就是EventTarget,如果要接收响应的对象在同一个类,则不需要EventTarget
            EventTarget contentPane = getScene().lookup("#ContentPane");
            Event.fireEvent(contentPane, userEvent);
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
            DataBase dataBase = new DataBase();
            dataBase.setName(dataBaseNameTextField.getText());
            dataBase.setUrl(dataBaseUrlTextField.getText());

        });
    }

    /**
     * 添加函数弹窗
     */
    private void addUdfDialog() {
        Dialog jfxDialog = new Dialog();
//        javaFx Dialog无法关闭：https://www.freesion.com/article/8839730889/
        DialogPane dialogPane = jfxDialog.getDialogPane();
        ObservableList<ButtonType> buttonTypes = dialogPane.getButtonTypes();
        buttonTypes.addAll(ButtonType.CLOSE);
        Button btnClose = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
        btnClose.setVisible(false);
        TabPane tabPane = new TabPane();
        tabPane.setPadding(new Insets(0, 0, 0, 0));
        tabPane.getTabs().addAll(createJarTab(), createMvnTab());
        dialogPane.setPadding(new Insets(0, 0, 0, 0));
        dialogPane.setContent(tabPane);
        jfxDialog.setTitle("添加自定义函数");
        jfxDialog.show();
    }

    private Tab createJarTab() {
        Tab jarTab = new Tab("Jar方式");
        jarTab.setClosable(false);
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(15);
        Label jarPathLabel = new Label("Jar路径:");
        TextField jarPathField = new TextField();
        JFXButton btn = new JFXButton("确定");
        gridPane.add(jarPathLabel, 0, 0);
        gridPane.add(jarPathField, 1, 0);
        gridPane.add(btn, 3, 3);
        jarTab.setContent(gridPane);
        btn.setOnAction(event -> {
            System.out.println("添加函数,JAR方式：" + jarPathField.getText());
            registerUdf(FuncSourceType.FILE, jarPathField.getText());
        });
        return jarTab;
    }

    private Tab createMvnTab() {
        Tab mvnTab = new Tab("Maven方式");
        mvnTab.setClosable(false);
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(15);
        Label groupIdLabel = new Label("groupId:");
        TextField groupIdField = new TextField();
        Label artifactIdLabel = new Label("artifactId:");
        TextField artifactIdField = new TextField();
        Label versionLabel = new Label("version:");
        TextField versionField = new TextField();
        JFXButton btn = new JFXButton("确定");
        gridPane.add(groupIdLabel, 0, 0);
        gridPane.add(groupIdField, 1, 0);
        gridPane.add(artifactIdLabel, 0, 1);
        gridPane.add(artifactIdField, 1, 1);
        gridPane.add(versionLabel, 0, 2);
        gridPane.add(versionField, 1, 2);
        gridPane.add(btn, 3, 3);
        mvnTab.setContent(gridPane);
        btn.setOnAction(event -> {
            System.out.println("添加函数,MAVEN方式：" + groupIdField.getText());
            registerUdf(FuncSourceType.MAVEN, groupIdField.getText(), artifactIdField.getText(), versionField.getText());
        });
        return mvnTab;
    }

    /**
     * 注册自定义函数
     */
    public void registerUdf(FuncSourceType funcSourceType, String... args) {
        try {
            AbstractFuncSource abstractFuncSource = funcSourceType.newInstance();
            abstractFuncSource.loadJar(args);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
