package com.kanyun.ui.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 添加修改数据库弹窗(原生样式)
 */
public abstract class NativeDataBaseDialog {
    private static final Logger log = LoggerFactory.getLogger(NativeDataBaseDialog.class);
    /**
     * 弹窗标题
     */
    private String title;

    /**
     * 数据库名称属性
     */
    private SimpleStringProperty dataBaseNameProperty = new SimpleStringProperty();

    /**
     * 数据库地址属性
     */
    private SimpleStringProperty dataBaseUrlProperty = new SimpleStringProperty();

    public NativeDataBaseDialog(String title) {
        this.title = title;
    }

    /**
     * 设置Property的值
     *
     * @param databaseName
     * @param databaseUrl
     * @return
     */
    public NativeDataBaseDialog setDataBaseNameAndDataBaseUrl(String databaseName, String databaseUrl) {
        dataBaseNameProperty.set(databaseName);
        dataBaseUrlProperty.set(databaseUrl);
        return this;
    }

    /**
     * Dialog显示
     *
     * @param source 来源节点,即哪个Node调用的show()方法
     */
    public void show(Node source) {
        Dialog jfxDialog = new Dialog();
        DialogPane dialogPane = jfxDialog.getDialogPane();
        jfxDialog.setTitle(title);
        dialogPane.setContent(createDataBaseDialogContent());
        ObservableList<ButtonType> buttonTypes = dialogPane.getButtonTypes();
        buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL);
        Button btnOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        Button btnCancel = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        jfxDialog.show();
        btnOk.setOnAction(event -> {
            apply(dataBaseNameProperty.get(), dataBaseUrlProperty.get());
        });
    }

    /**
     * 应用按钮点击抽象方法
     *
     * @param dataBaseName
     * @param dataBaseUrl
     */
    protected abstract void apply(String dataBaseName, String dataBaseUrl);

    /**
     * 创建Dialog内容区
     *
     * @return
     */
    private Node createDataBaseDialogContent() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(15);

        TextField dataBaseNameTextField = new TextField();
        dataBaseNameTextField.textProperty().bindBidirectional(dataBaseNameProperty);
        Label dataBaseNameLabel = new Label("数据库名称: ");
        Label dataBaseDirLabel = new Label("数据库地址: ");
        HBox hBox = new HBox();
//        设置元素间距
        hBox.setSpacing(5);
        TextField dataBaseUrlTextField = new TextField();
        dataBaseUrlTextField.textProperty().bindBidirectional(dataBaseUrlProperty);
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
        return gridPane;

    }
}
