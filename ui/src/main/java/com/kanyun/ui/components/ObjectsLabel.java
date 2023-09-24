package com.kanyun.ui.components;

import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.model.BaseModel;
import com.kanyun.ui.model.ObjectsTypeEnum;
import com.kanyun.ui.model.TableModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * property().addListener() 或
 * ObservableValue生成两种类型的事件：
 * 更改事件ChangeListener和无效事件InvalidationListener。
 * 更改事件表示该值已更改。如果当前值不再有效，则会生成一个无效事件。
 * 如果ObservableValue支持延迟求值，则这一区别就变得很重要，因为对于延迟求值的值，直到重新计算无效值，才知道该值是否真的发生了变化。
 * 因此，生成更改事件需要进行急切的评估， 而对于懒惰的实现可以生成无效事件。
 */
public class ObjectsLabel extends Region {

    private static final Logger logger = LoggerFactory.getLogger(ObjectsLabel.class);

    /**
     * 显示的名称
     */
    private String text;


    /**
     * 显示的图像
     */
    private Node graphic;

    /**
     * 绘画对象
     */
    private GraphicsContext gc;

    /**
     * Canvas对象用于画图
     */
    private Canvas canvas;

    /**
     * 业务数据
     */
    private BaseModel model;

    private ObjectsTypeEnum typeEnum;


    private static Background selectedBackground = new Background(new BackgroundFill(Color.BLUE, new CornerRadii(2), new Insets(2)));
    private static Background defaultBackground = new Background(new BackgroundFill(Color.GREEN, new CornerRadii(2), new Insets(2)));

    /**
     * 是否选中属性
     */
    private SimpleBooleanProperty selectedProperty = new SimpleBooleanProperty(false);


    public ObjectsLabel(String text) {
        this.text = text;
        canvas = new Canvas();
        getChildren().add(canvas);
    }

    /**
     * 重写layoutChildren()方法,用于布局子组件
     */
    @Override
    protected void layoutChildren() {
        logger.debug("layoutChildren() executing............");
        super.layoutChildren();
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.GRAY);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.strokeLine(0,0,20, 10);
        gc.fillText(text, 0, 0);
    }

    /**
     * 重写updateBounds()方法,用于更新组件边界
     */
    @Override
    protected void updateBounds() {
        logger.debug("updateBounds() executing............");
        super.updateBounds();
    }

    @Override
    protected double computePrefHeight(double width) {
        return super.computePrefHeight(width);
    }

    @Override
    protected double computePrefWidth(double height) {
        return super.computePrefWidth(height);
    }

    /**
     * 添加监听
     */
    public void addListener() {

//        是否选中属性监听
        selectedProperty.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    gc.setFill(Color.BLUE);
                } else {
                    gc.setFill(Color.TRANSPARENT);
                }
            }
        });

        canvas.setOnMousePressed(event -> {
            selectedProperty.set(true);
        });

        /**
         * 双击事件
         */
        canvas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (typeEnum == ObjectsTypeEnum.TABLE) {
                    UserEvent userEvent = new UserEvent(UserEvent.QUERY_TABLE);
                    TableModel tableModel = (TableModel) model;
                    userEvent.setTableModel(tableModel);
                    UserEventBridgeService.bridgeUserEvent2ContentPane(userEvent);
                }
            }
        });
    }
}
