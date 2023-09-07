package com.kanyun.ui.components;

import com.jfoenix.controls.JFXButton;
import com.kanyun.ui.IconProperties;
import com.kanyun.ui.tabs.TabQueryTablePane;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义分页工具栏
 */
public class SimplicityPaginationToolBar extends HBox {
    private static final Logger log = LoggerFactory.getLogger(SimplicityPaginationToolBar.class);

    private static final Double ICON_SIZE = 10.0;

    /**
     * 每页显示数据条数
     */
    private static final Long PAGE_LIMIT = 10L;

    /**
     * 当前页码属性
     */
    private SimpleStringProperty currentPageProperty = new SimpleStringProperty();

    /**
     * 设置分页按钮
     */
    private JFXButton settingPageButton;
    /**
     * 最后一页按钮
     */
    private JFXButton lastedPageButton;
    /**
     * 下一页按钮
     */
    private JFXButton nextPageButton;
    /**
     * 上一页按钮
     */
    private JFXButton previousPageButton;
    /**
     * 第一页按钮
     */
    private JFXButton firstPageButton;
    /**
     * 当前页(可编辑,可随动) TextField#textProperty() 一旦绑定了定义的property {@link currentPageProperty}
     * 则TextField#setText()方法将失效
     */
    private TextField currentPageTextField;

    /**
     * 每页显示数量
     */
    private TextField pageLimitTextField;

    /**
     * 分页组件
     */
    private Node paginationOptNode;

    /**
     * 分页设置组件
     */
    private Node paginationSetNode;


    /**
     * 表数据
     */
    private TabQueryTablePane table;


    public SimplicityPaginationToolBar(TabQueryTablePane table) {
        setId("SimplicityPaginationToolBar");
        this.table = table;
        setPrefHeight(8);
        setPadding(new Insets(0, 0, 0, 0));
//        设置对齐方式从右向左
        setAlignment(Pos.BASELINE_RIGHT);
        settingPageButton = new JFXButton(null, IconProperties.getIcon("pagination.setting", ICON_SIZE, Color.GRAY));
        paginationOptNode = buildPaginationNode();
        paginationSetNode = buildLimitNode();
        addComponentsAction();
        getChildren().addAll(paginationOptNode, settingPageButton);
    }


    /**
     * 构建分页按钮组件
     */
    private Node buildPaginationNode() {
        Pane group = createButtonGroup();
        Color color = Color.GRAY;
        lastedPageButton = new JFXButton(null, IconProperties.getIcon("pagination.latest", ICON_SIZE, color));
        nextPageButton = new JFXButton(null, IconProperties.getIcon("pagination.next", ICON_SIZE, color));
        currentPageTextField = new TextField();
        currentPageTextField.setAlignment(Pos.CENTER);
        currentPageTextField.prefWidthProperty().bind(currentPageTextField.prefHeightProperty());
        currentPageTextField.textProperty().bindBidirectional(currentPageProperty);
        currentPageProperty.set(String.valueOf(1));
        previousPageButton = new JFXButton(null, IconProperties.getIcon("pagination.previous", ICON_SIZE, color));
        firstPageButton = new JFXButton(null, IconProperties.getIcon("pagination.first", ICON_SIZE, color));
        group.getChildren().addAll(firstPageButton, previousPageButton, currentPageTextField, nextPageButton, lastedPageButton);
        return group;
    }

    /**
     * 构建分页数量组件
     */
    private Node buildLimitNode() {
        Pane group = createButtonGroup();
        pageLimitTextField = new TextField(String.valueOf(PAGE_LIMIT));
        Label info = new Label("每页显示数量");
        CheckBox checkBox = new CheckBox("无限制");
        group.getChildren().addAll(checkBox, pageLimitTextField, info);
        return group;
    }

    /**
     * 添加组件动作
     */
    private void addComponentsAction() {
        lastedPageButton.setOnAction(event -> {
            log.debug("最后一页按钮被点击");
            table.lastedPage();
        });

        nextPageButton.setOnAction(event -> {
            log.debug("下一页按钮被点击");
            currentPageProperty.set(String.valueOf(Integer.parseInt(currentPageProperty.getValue()) + 1));
            table.nextPage();
        });

        previousPageButton.setOnAction(event -> {
//            上一页按钮被点击先判断当前页码是否是第一页,如果是则什么都不做
            if (!currentPageProperty.get().equals("1")) {
                currentPageProperty.set(String.valueOf(Integer.parseInt(currentPageProperty.getValue()) - 1));
                table.previousPage();
            }
        });

        firstPageButton.setOnAction(event -> {
//            第一页按钮被点击先判断当前页码是否是第一页,如果是则什么都不做
            if (!currentPageProperty.get().equals("1")) {
                currentPageProperty.set("1");
                table.firstPage();
            }
        });
        settingPageButton.setOnAction(event -> {
//            分页设置按钮主要用来切换显示内容
            if (getChildren().get(0) == paginationOptNode) {
                getChildren().set(0, paginationSetNode);
            } else {
                getChildren().set(0, paginationOptNode);
            }
        });
//        监听TextField中设置的值,如果不是数字,设置为旧值,如果开头是0,则设置为旧制
        currentPageTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!NumberUtils.isDigits(newValue) || newValue.startsWith("0")) {
                    currentPageTextField.setText(oldValue);
                }
            }
        });
//        TextField监听键盘事件
        currentPageTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    table.customPage();
                }
            }
        });
    }


    /**
     * 获取每页显示条数
     *
     * @return
     */
    public Integer getPageLimit() {
        return Integer.parseInt(pageLimitTextField.getText());
    }

    /**
     * 获取当前页码
     *
     * @return
     */
    public Integer getCurrentPage() {
        return Integer.parseInt(StringUtils.isBlank(currentPageTextField.getText()) ? "1" : currentPageTextField.getText());
    }

    /**
     * 创建按钮分组
     *
     * @return
     */
    private Pane createButtonGroup() {
        HBox hBox = new HBox();
        return hBox;
    }
}
