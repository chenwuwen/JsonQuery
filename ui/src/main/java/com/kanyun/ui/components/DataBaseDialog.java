package com.kanyun.ui.components;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * 添加修改数据库弹窗
 */
public abstract class DataBaseDialog {
    private static final Logger log = LoggerFactory.getLogger(DataBaseDialog.class);
    private static final String FX_LABEL_FLOAT_TRUE = "-fx-label-float:true;";
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
    /**
     * 数据库名称输入框
     */
    private JFXTextField dataBaseNameField = new JFXTextField();
    /**
     * 数据库地址输入框
     */
    private JFXTextField dataBaseUrlTextField = new JFXTextField();


    public DataBaseDialog(String title) {
        this.title = title;
    }


    /**
     * 设置Property的值
     *
     * @param databaseName
     * @param databaseUrl
     * @return
     */
    public DataBaseDialog setDataBaseNameAndDataBaseUrl(String databaseName, String databaseUrl) {
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
        JFXAlert alert = new JFXAlert(source.getScene().getWindow());
//        模态框
        alert.initModality(Modality.APPLICATION_MODAL);
//        是否点击空白区域关闭弹窗
        alert.setOverlayClose(false);
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label(title));
        layout.setBody(createDataBaseDialogContent());
        JFXButton applyButton = new JFXButton("应用");
        applyButton.setButtonType(JFXButton.ButtonType.RAISED);
        JFXButton closeButton = new JFXButton("关闭");
        closeButton.setButtonType(JFXButton.ButtonType.RAISED);
        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> alert.hideWithAnimation());
        applyButton.setOnAction(event -> {
            log.info("[{}] 点击应用按钮,内容:[{}=={}]", title, dataBaseNameProperty.get(), dataBaseUrlProperty.get());
            if (dataBaseNameField.validate() && dataBaseUrlTextField.validate()) {
//                应用按钮点击时,调用抽象方法,由子类实现
                apply(dataBaseNameProperty.get(), dataBaseUrlProperty.get());
                alert.close();
            }

        });
        layout.setActions(closeButton, applyButton);
        alert.setContent(layout);
        alert.show();
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
        VBox content = new VBox();
        content.setSpacing(20);
        content.setStyle("-fx-background-color:WHITE;-fx-padding:10;");
//        bind()是单向绑定,bindBidirectional()是双向绑定
        dataBaseNameField.textProperty().bindBidirectional(dataBaseNameProperty);
        dataBaseNameField.setLabelFloat(true);
        dataBaseNameField.setPromptText("数据库名称");

        HBox dataBaseUrlRegion = new HBox();

//        bind()是单向绑定,bindBidirectional()是双向绑定
        dataBaseUrlTextField.textProperty().bindBidirectional(dataBaseUrlProperty);
        dataBaseUrlTextField.setStyle(FX_LABEL_FLOAT_TRUE);
        dataBaseUrlTextField.setPromptText("数据库路径");
        dataBaseUrlTextField.setDisable(true);
        JFXButton dataBaseUrlBtn = new JFXButton("数据库路径");
        dataBaseUrlBtn.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("请选择你的数据库路径");
            File selectedDirectory = directoryChooser.showDialog(new Stage());
            if (selectedDirectory != null && selectedDirectory.isDirectory()) {
                dataBaseUrlTextField.setText(selectedDirectory.getPath());
            }
        });
//        数据框添加验证
        addTextFieldValidate();
        dataBaseUrlRegion.setSpacing(5);
        HBox.setHgrow(dataBaseUrlTextField, Priority.ALWAYS);
        dataBaseUrlRegion.getChildren().addAll(dataBaseUrlTextField, dataBaseUrlBtn);
//        数据库名称添加到Pane,数据库URL(TextField+Button)添加到Pane,注意添加顺序
        content.getChildren().addAll(dataBaseNameField, dataBaseUrlRegion);

        return content;
    }

    /**
     * 添加数据框验证器
     *
     */
    private void addTextFieldValidate() {
//        数据库名验证配置
        RequiredFieldValidator dataBaseNameValidator = new RequiredFieldValidator("数据库名称不能为空");
        dataBaseNameField.setValidators(dataBaseNameValidator);
        dataBaseNameField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                observable是Property对象,即当前的focus状态,newValue为false表示失去焦点
                log.debug("数据库名称输入框焦点状态,新值:{} 旧值:{}",newValue,oldValue);
                if (!newValue) {
//                    不满足验证规则
                    dataBaseNameValidator.validate();
                }
            }
        });

//        数据库URL验证配置
        RequiredFieldValidator dataBaseUrlValidator = new RequiredFieldValidator("数据库路径不能为空");
        dataBaseUrlTextField.setValidators(dataBaseUrlValidator);
        dataBaseUrlTextField.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                dataBaseUrlValidator.validate();
            }
        });
    }

}
