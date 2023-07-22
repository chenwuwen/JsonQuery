package com.kanyun.ui.components;

import com.kanyun.ui.layout.TopButtonPane;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 顶部按钮菜单组件(自定义控件)
 */
public class TopButtonComponent extends VBox {
    private static final Logger log = LoggerFactory.getLogger(TopButtonComponent.class);

    /**
     * @param btnName 按钮名称
     * @param imgPath 按钮图片
     */
    public TopButtonComponent(String btnName, String imgPath) {
        getTypeSelector();
        setAlignment(Pos.CENTER);
        setPrefWidth(50);
        setPrefHeight(50);
//        设置元素间距(图片与文字的间距)
        setSpacing(5);
        setPadding(new Insets(5, 5, 5, 5));
        ImageView imageView = new ImageView(imgPath);
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        Label label = new Label(btnName);
//        设置文字居中
        label.setAlignment(Pos.CENTER);
        getChildren().addAll(imageView, label);
//        监听高度变化,设置组件宽度与高度一致
        heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                todo 这里不使用Platform.runLater()的话,界面展示后,当鼠标移动到该组件的父类后,会出现组件向右移动的情况,应该是设置宽度未生效导致
//                在Platform.runLater()中设置宽度,界面展示,不会出现组件位移的情况
                Platform.runLater(()->{
                    setPrefWidth(newValue.doubleValue());
                });
            }
        });

//        鼠标移入事件
//        addEventHandler(MouseEvent.MOUSE_ENTERED, e -> setStyle(getMouseEnteredStyle()));
//        鼠标移出事件
//        addEventHandler(MouseEvent.MOUSE_EXITED, e -> setStyle(getNormalStyle()));
//        鼠标按下事件
//        addEventHandler(MouseEvent.MOUSE_PRESSED, e -> setStyle(getMousePressedStyle()));
    }

    /**
     * 返回鼠标移入时的样式
     *
     * @return
     */
    public String getMouseEnteredStyle() {
        return "-fx-effect: dropshadow(gaussian, black, 50, 0, 0, 0);" +
                "-fx-background-insets: 5;" +
                "";
    }

    /**
     * 返回正常样式
     *
     * @return
     */
    public String getNormalStyle() {
        return "";
    }

    /**
     * 返回点击样式
     *
     * @return
     */
    public String getMousePressedStyle() {
        return "";
    }
}
