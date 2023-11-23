package com.kanyun.ui.tabs;

import com.jfoenix.controls.JFXButton;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.IconProperties;
import com.kanyun.ui.components.objectslabel.ObjectsLabel;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.model.BaseModel;
import com.kanyun.ui.model.DataBaseModel;
import com.kanyun.ui.model.ObjectsTypeEnum;
import com.kanyun.ui.model.TableModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ObjectsTab 主要用来显示数据库中的表
 */
public class TabObjectsPane extends AbstractTab {

    private static final Logger log = LoggerFactory.getLogger(TabObjectsPane.class);

    public static final String TAB_NAME = "Objects";

    /**
     * 动态信息栏
     */
    private StatusBar dynamicInfoStatusBar;

    /**
     * 动态信息属性
     * 初始化放在 {@link this#createDynamicInfoStatusBar()}
     * 因为子类在实例化时会先调用父类的构造方法,而此时该成员变量尚未初始化,
     * 由于父类的构造方法调用了子类的{@link this#createDynamicInfoStatusBar()}
     * 因此初始化放在 {@link this#createDynamicInfoStatusBar()}
     */
    private SimpleStringProperty dynamicInfoProperty;

    /**
     * Objects容器,即Objects存放的位置
     */
    private FlowPane objectsContainer = new FlowPane();
    private ScrollPane scrollPane = new ScrollPane();

    /**
     * 最后一个被点击的Object对象
     */
    private Node lastFocusedNode;

    public TabObjectsPane() {

        setId("TabObjectsPane");
        setMaxWidth(Integer.MAX_VALUE);
        getChildren().add(toolBar);
        createObjectsContainer();
        createScrollContainer();
        getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        addEventHandler(UserEvent.SHOW_OBJECTS, event -> {
//            接收到展示对象事件,先清空内容,再设置。如果事件参数为空,则只清空,不设置内容
            ObservableList<Node> children = objectsContainer.getChildren();
            objectsContainer.getChildren().removeAll(children);
            if (event.getDataBaseModel() == null) return;
            buildObjects(event.getObjectsTypeEnum(), event.getDataBaseModel());
        });
    }

    /**
     * 创建滚动区域,用来解决当内容过多时FlowPane显示不完全的问题
     */
    private void createScrollContainer() {
        scrollPane = new ScrollPane();
        scrollPane.setContent(objectsContainer);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//        不允许显示纵向滚动条
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.prefViewportHeightProperty().bind(scrollPane.heightProperty());
    }


    /**
     * 创建Objects的容器,使用FlowPane组件,并定义子组件的排列方式
     * 当FlowPane的子组件数量在高度/宽度中的某一项超过FlowPane时,FlowPane将
     * 自动换行或换列,以优化显示效果,但是当子组件数量足够多
     * 并且在宽度/高度中都超过了FlowPane的显示面积,FlowPane却不会显示滚动条,
     * 因此当FlowPane中内容过多时将无法完全显示其内容
     */
    public void createObjectsContainer() {
        objectsContainer.setMaxWidth(Integer.MAX_VALUE);
        objectsContainer.setPadding(new Insets(10, 0, 10, 10));
//        设置方向为纵项排列
        objectsContainer.setOrientation(Orientation.VERTICAL);
//        设置子组件纵项间隙
        objectsContainer.setVgap(1);
//        设置子组件横项间隙
        objectsContainer.setHgap(5);
        objectsContainer.prefHeightProperty().bind(scrollPane.prefViewportHeightProperty());
        objectsContainer.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node focusOwner = objectsContainer.getScene().getFocusOwner();
                log.info("objectsContainer 监听鼠标点击事件,获取焦点,判断获取焦点的元素是否是ObjectsLabel类型:{}", focusOwner instanceof ObjectsLabel);
                if (focusOwner instanceof ObjectsLabel) {
                    lastFocusedNode = focusOwner;
                } else {
                    lastFocusedNode = null;
                }

            }
        });
    }


    /**
     * 构建Objects并装进容器,
     * 这里的对象只有三种类型,一种是函数,一种是表
     * 根据类型枚举来判断需要展示的内容
     * 即:传输的类型为schema时,表示要展示的是table
     * 或者为空
     */
    private void buildObjects(ObjectsTypeEnum typeEnum, BaseModel baseModel) {
        if (typeEnum == ObjectsTypeEnum.SCHEMA) {
//            传输的对象的时Schema,说明展示的是表信息
            buildObjectsForTable((DataBaseModel) baseModel);
        }
    }

    /**
     * 构建Table类型的Object组件
     *
     * @param dataBaseModel
     */
    private void buildObjectsForTable(DataBaseModel dataBaseModel) {
        for (TableModel tableModel : dataBaseModel.getTables()) {
            ObjectsLabel objectsLabel = new ObjectsLabel(tableModel.getTableName());
            objectsLabel.setGraphic(IconProperties.getImageView("/asserts/table.png", 20));
            objectsLabel.setOnMouseClicked(event -> {
                MouseButton button = event.getButton();
                if (button == MouseButton.PRIMARY) {
                    if (event.getClickCount() == 2) {
//                        鼠标左键双击
                        openTable(tableModel.getSchemaName(), tableModel.getTableName());
                    }
                }
            });
            objectsContainer.getChildren().add(objectsLabel);
        }
    }

    /**
     * 初始化工具栏
     *
     * @return
     */
    @Override
    public void initToolBar() {
        toolBar.setPrefHeight(20);
        toolBar.setPadding(new Insets(2, 4, 2, 4));
        Button openTableBtn = new JFXButton("打开表");
//        当点击打开表按钮时,ObjectsLabel的焦点将自动清除
        openTableBtn.setOnAction(event -> {
            ObservableList<Node> children = objectsContainer.getChildren();
            for (Node child : children) {
                if (child instanceof ObjectsLabel) {
                    ObjectsLabel objectsLabel = (ObjectsLabel) child;
//                    todo 判断组件是否被选中
                    if (objectsLabel.isFocused()) {
                        if (objectsLabel.getTypeEnum() == ObjectsTypeEnum.TABLE) {
                            TableModel model = (TableModel) objectsLabel.getModel();
                            openTable(model.getSchemaName(), model.getTableName());
                        }
                    }
                }
            }

        });
        toolBar.getItems().add(openTableBtn);
    }

    /**
     * 双击表打开表
     *
     * @param dataBaseName
     * @param tableName
     */
    private void openTable(String dataBaseName, String tableName) {
        UserEvent userEvent = new UserEvent(UserEvent.QUERY_TABLE);
        TableModel tableModel = new TableModel();
        tableModel.setTableName(tableName);
        tableModel.setSchemaName(dataBaseName);
        userEvent.setTableModel(tableModel);
        UserEventBridgeService.bridgeUserEvent2ContentPane(userEvent);
    }

    @Override
    public TabKindEnum getTabKind() {
        return TabKindEnum.OBJECT_TAB;
    }

    @Override
    public void createDynamicInfoStatusBar() {
        dynamicInfoStatusBar = new StatusBar();
//        不设置的话,默认有个OK字样
        dynamicInfoStatusBar.setText("");
        dynamicInfoProperty = new SimpleStringProperty();
        dynamicInfoStatusBar.textProperty().bind(dynamicInfoProperty);
    }

    @Override
    public StatusBar getDynamicInfoStatusBar() {
        return dynamicInfoStatusBar;
    }

    @Override
    public void addStatusBarEventListener() {
        addEventHandler(UserEvent.CURRENT_DATABASE, event -> {
            DataBaseModel dataBaseModel = event.getDataBaseModel();
            ImageView imageView = new ImageView("/asserts/database.png");
            imageView.setFitWidth(dynamicInfoStatusBar.getPrefHeight() / 2);
            imageView.setFitHeight(dynamicInfoStatusBar.getPrefHeight() / 2);
            Label currentDbLabel = TabKind.createCommonLabel(dataBaseModel.getName(), dynamicInfoStatusBar, Color.ORANGE, null);
            if (dynamicInfoStatusBar.getLeftItems().size() > 0) {
                dynamicInfoStatusBar.getLeftItems().remove(0);
            }
            dynamicInfoStatusBar.getLeftItems().add(currentDbLabel);
            ModelJson.getModelJson(dataBaseModel.getName());
        });
    }

    @Override
    public Node getTabGraphic() {
        return null;
    }

    @Override
    public void onShown() {
        lastFocusedNode = null;
    }


}
