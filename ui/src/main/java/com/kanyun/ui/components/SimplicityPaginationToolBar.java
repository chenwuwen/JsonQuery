package com.kanyun.ui.components;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.kanyun.ui.IconProperties;
import com.kanyun.ui.tabs.TabQueryTablePane;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义分页工具栏
 */
public class SimplicityPaginationToolBar extends ToolBar {
    private static final Logger log = LoggerFactory.getLogger(SimplicityPaginationToolBar.class);

    private static final Double ICON_SIZE = 10.0;

    /**
     * 每页显示数据条数
     */
    private static final Long PAGE_LIMIT = 10L;
    private static final double PAGE_OPERATE_WIDTH = 30;

    /**
     * 当前页码属性
     */
    private SimpleStringProperty currentPageProperty = new SimpleStringProperty();

    /**
     * 设置分页按钮
     */
    private Button settingPageButton;
    /**
     * 最后一页按钮
     */
    private Button lastedPageButton;
    /**
     * 下一页按钮
     */
    private Button nextPageButton;
    /**
     * 上一页按钮
     */
    private Button previousPageButton;
    /**
     * 第一页按钮
     */
    private Button firstPageButton;
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
     * 分页组件组
     */
    private ObservableList<Node> paginationNodeList;

    /**
     * 分页设置组件组
     */
    private ObservableList<Node> paginationSetNodeList ;


    /**
     * 表数据
     */
    private TabQueryTablePane table;


    public SimplicityPaginationToolBar(TabQueryTablePane table) {
        setId("SimplicityPaginationToolBar");
        this.table = table;
//        设置组件排列方向由右向左(注意是Node类的方法[节点方向描述了节点内的视觉数据流]),而节点方向默认为跟从父类方向,因此子元素若想正常显示需要重新手动设置
        setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        setPadding(new Insets(0, 0, 0, 0));
        settingPageButton = new JFXButton(null, IconProperties.getIcon("pagination.setting", ICON_SIZE, Color.GRAY));
        buildPaginationNode();
        buildPaginationSetNode();

        addComponentsAction();
//        添加设置按钮
        getItems().add(settingPageButton);
//        添加分页组件按钮组
        getItems().addAll(paginationNodeList);
//        由于父类ToolBar设置了元素绘制方向,而当前子元素绘制方向默认为跟从父类方向(NodeOrientation.INHERIT),因此这里需要正确设置绘制方向,避免按钮触发不正确,除非对按钮排列顺序没有要求,或没有歧义的符号误导等要求
        for (Node node : paginationNodeList) {
            node.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }

    }


    /**
     * 构建分页按钮组件
     */
    private void buildPaginationNode() {
        paginationNodeList = FXCollections.observableArrayList();
        Color color = Color.GRAY;
        lastedPageButton = new JFXButton(null, IconProperties.getIcon("pagination.latest", ICON_SIZE, color));
        nextPageButton = new JFXButton(null, IconProperties.getIcon("pagination.next", ICON_SIZE, color));
        currentPageTextField = new TextField();
        currentPageTextField.setAlignment(Pos.CENTER);
        currentPageTextField.setPrefWidth(PAGE_OPERATE_WIDTH);
        currentPageTextField.textProperty().bindBidirectional(currentPageProperty);
        currentPageProperty.set(String.valueOf(1));
        previousPageButton = new JFXButton(null, IconProperties.getIcon("pagination.previous", ICON_SIZE, color));
        firstPageButton = new JFXButton(null, IconProperties.getIcon("pagination.first", ICON_SIZE, color));
        paginationNodeList.addAll(lastedPageButton,nextPageButton,currentPageTextField,previousPageButton,firstPageButton);

    }

    /**
     * 构建分页数量组件
     */
    private void buildPaginationSetNode() {
        paginationSetNodeList = FXCollections.observableArrayList();

        StackPane pageLimitPane = new StackPane();
        pageLimitTextField = new TextField(String.valueOf(PAGE_LIMIT));
        pageLimitTextField.setPrefWidth(PAGE_OPERATE_WIDTH);
        pageLimitPane.getChildren().add(pageLimitTextField);
//        使用StackPane包装TextField,用来设置组件外边距
        pageLimitPane.setPadding(new Insets(0,8,0,8));
//        文字添加空格,主要也是为了添加边距
        Label illustrate = new Label("每页显示数量 ");
//        勾选为限制数量(即pageLimitTextField的值),不勾选为不限制,默认是限制数量
        CheckBox checkBox = new CheckBox("限制数量 ");
//        设置checkbox默认为选中状态
        checkBox.selectedProperty().set(true);
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                触发每页无限制复选框时,将当前页码设置为1
                currentPageProperty.set("1");
                if (!newValue) {
                    log.info("当前设置每页显示数量为无限制,将重新查询所有表数据,并隐藏分页按钮,刷新页面.");
                    for (Node node : paginationNodeList) {
                        node.setVisible(false);
                    }
                    table.allRow();
                } else {
                    log.info("当前设置每页显示数量为:{},将重新查询带有分页信息表数据,显示分页按钮,刷新页面.", pageLimitTextField.getText());
                    for (Node node : paginationNodeList) {
                        node.setVisible(true);
                    }
                    table.firstPage();
                }
            }
        });
//        由于父组件设置了绘制方向,因此此处添加子组件应注意合理安排添加顺序
        paginationSetNodeList.addAll(illustrate, pageLimitPane,checkBox );

    }

    /**
     * 添加组件动作
     */
    private void addComponentsAction() {
        lastedPageButton.setOnAction(event -> {
            log.debug("最后一页按钮被点击");
            table.lastedPage(currentPageProperty);
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
            if (getItems().containsAll(paginationNodeList)) {
                getItems().removeAll(paginationNodeList);
                getItems().addAll(paginationSetNodeList);
            } else {
                getItems().removeAll(paginationSetNodeList);
                getItems().addAll(paginationNodeList);
            }
        });
//        监听TextField中设置的值,如果不是数字,设置为旧值,如果开头是0,则设置为旧值
        currentPageTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!NumberUtils.isDigits(newValue) || newValue.startsWith("0")) {
                    currentPageTextField.setText(oldValue);
                }
            }
        });
        pageLimitTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!NumberUtils.isDigits(newValue) || newValue.startsWith("0")) {
                    pageLimitTextField.setText(oldValue);
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
        pageLimitTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
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
