package com.kanyun.ui.tabs;

import com.jfoenix.controls.JFXButton;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.components.ObjectsLabel;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.model.BaseModel;
import com.kanyun.ui.model.DataBaseModel;
import com.kanyun.ui.model.ObjectsTypeEnum;
import com.kanyun.ui.model.TableModel;
import com.sun.javafx.event.EventUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
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
    private SimpleStringProperty dynamicInfoProperty ;

    /**
     * Objects容器,即Objects存放的位置
     */
    private FlowPane objectsContainer;

    public TabObjectsPane() {

        setId("TabObjectsPane");
        getChildren().add(initToolBar());
        createObjectsContainer();
        getChildren().add(objectsContainer);
        addEventHandler(UserEvent.SHOW_OBJECTS, event -> {
//            接收到展示对象事件,先清空内容,再设置。如果事件参数为空,则只清空,不设置内容
            ObservableList<Node> children = objectsContainer.getChildren();
            objectsContainer.getChildren().removeAll(children);
            if (event.getDataBaseModel() == null) return;
            buildObjects(event.getObjectsTypeEnum(), event.getDataBaseModel());

        });
    }


    /**
     * 创建Objects的容器
     */
    public void createObjectsContainer() {
        objectsContainer = new FlowPane();
        objectsContainer.setPadding(new Insets(10, 0, 10, 10));
//        设置方向为纵项排列
        objectsContainer.setOrientation(Orientation.VERTICAL);
//        设置子组件纵项间隙
        objectsContainer.setVgap(10);
//        设置子组件横项间隙
        objectsContainer.setHgap(20);
    }


    /**
     * 构建Objects并装进容器,
     * 这里的对象只有三种类型,一种是函数,一种是表
     * 根据类型枚举来判断需要展示的内容
     * 即:传输的类型为schema时,表示要展示的是table
     * 或者为空
     *
     */
    private void buildObjects(ObjectsTypeEnum typeEnum, BaseModel baseModel) {
        if (typeEnum == ObjectsTypeEnum.SCHEMA) {
//            传输的对象的时Schema,说明展示的是表信息
            buildObjectsForTable((DataBaseModel) baseModel);
        }
    }

    private void buildObjectsForTable(DataBaseModel dataBaseModel) {
        for (TableModel tableModel : dataBaseModel.getTables()) {
            ObjectsLabel objectsLabel = new ObjectsLabel(tableModel.getTableName());
//            Label objectsLabel = new Label(tableModel.getTableName());
            objectsContainer.getChildren().add(objectsLabel);
        }
    }

    /**
     * 初始化工具栏
     *
     * @return
     */
    public Node initToolBar() {
//        顶部按钮区域
        HBox btnBox = new HBox();
        btnBox.setPrefHeight(20);
        btnBox.setStyle("-fx-background-color: #0ac");
        btnBox.setPadding(new Insets(2, 4, 2, 4));
        Button openTableBtn = new JFXButton("打开表");
        btnBox.getChildren().add(openTableBtn);
        return btnBox;
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


}
