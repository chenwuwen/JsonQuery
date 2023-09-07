package com.kanyun.ui.components;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.validation.RequiredFieldValidator;
import com.jfoenix.validation.base.ValidatorBase;
import com.kanyun.sql.core.column.AbstractAnalysisJsonTableColumn;
import com.kanyun.sql.core.column.JsonTableColumn;
import com.kanyun.ui.model.TableMetaData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.stage.Modality;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 重生成表字段信息弹窗
 * 输入json内容,重新生成字段
 */
public abstract class ReGenerateTableFieldDialog {

    private static final Logger log = LoggerFactory.getLogger(ReGenerateTableFieldDialog.class);

    /**
     * 数据库名称
     */
    private String schema;

    /**
     * 表名
     */
    private String table;

    public ReGenerateTableFieldDialog(String schema, String table) {
        this.schema = schema;
        this.table = table;
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
        layout.setHeading(new Label("字段信息重生成"));
        JFXTextArea jsonItemArea = new JFXTextArea();
        jsonItemArea.setWrapText(true);
        jsonItemArea.setPromptText("输入json元素内容,点击生成!");
        RequiredFieldValidator requiredFieldValidator = new RequiredFieldValidator("内容不能为空");
//        自定义验证器,验证输入内容是jsonObject类型
        ValidatorBase jsonValidator = new ValidatorBase() {
            @Override
            protected void eval() {
                TextInputControl textField = (TextInputControl) srcControl.get();
                String json = textField.getText();
                if (!JsonParser.parseString(json).isJsonObject()) {
                    setMessage("数据不是JsonObject类型");
                    hasErrors.set(true);
                } else {
                    hasErrors.set(false);
                }
            }
        };
        jsonItemArea.setValidators(requiredFieldValidator, jsonValidator);
        layout.setBody(jsonItemArea);
        JFXButton applyButton = new JFXButton("生成");
        applyButton.setButtonType(JFXButton.ButtonType.RAISED);
        JFXButton closeButton = new JFXButton("关闭");
        closeButton.setButtonType(JFXButton.ButtonType.RAISED);
//        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> alert.hideWithAnimation());
        applyButton.setOnAction(event -> {
            if (jsonItemArea.validate()) {
//                获取输入的Json内容,并解析字段及类型
                String item = jsonItemArea.getText();
                JsonObject jsonObject = JsonParser.parseString(item).getAsJsonObject();
                List<JsonTableColumn> jsonTableColumnList = AbstractAnalysisJsonTableColumn.analysisJsonItem(jsonObject);
                List<TableMetaData> collect = jsonTableColumnList.stream().map(jsonTableColumn -> {
                    return TableMetaData.newBuilder(schema, table)
                            .setColumnName(jsonTableColumn.getName())
                            .setColumnType(jsonTableColumn.getType().toCode())
                            .setColumnDefaultValue(null)
                            .builder();
                }).collect(Collectors.toList());
                handlerResult(FXCollections.observableList(collect));
                alert.close();
            }

        });
        layout.setActions(closeButton, applyButton);
        alert.setContent(layout);
        alert.show();
    }

    /**
     * 处理Json解析后的结果,由子类实现
     *
     * @param tableMetaDataObservableList 包含表名,schema名,字段名及字段类型
     */
    abstract public void handlerResult(ObservableList<TableMetaData> tableMetaDataObservableList);
}
