package com.kanyun.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * 顶部按钮菜单组件
 */
public class TopButtonComponent extends VBox {

    public TopButtonComponent(String btnName, String imgPath) {
        setAlignment(Pos.CENTER);
//        设置元素间距
        setSpacing(5);
        setPadding(new Insets(5, 5, 5, 20));
        ImageView imageView = new ImageView(imgPath);
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        Label label = new Label(btnName);
        getChildren().addAll(imageView, label);
//        鼠标移入事件
        addEventHandler(MouseEvent.MOUSE_ENTERED, e -> setStyle(getMouseEnteredStyle()));
//        鼠标移出事件
        addEventHandler(MouseEvent.MOUSE_EXITED, e -> setStyle(getNormalStyle()));
//        鼠标按下事件
        addEventHandler(MouseEvent.MOUSE_PRESSED, e -> setStyle(getMousePressedStyle()));
    }

    /**
     * 返回鼠标移入时的样式
     * @return
     */
    public String getMouseEnteredStyle() {
        return "-fx-effect: dropshadow(gaussian, black, 50, 0, 0, 0);" +
                "-fx-background-insets: 5;" +
                "";
    }

    /**
     * 返回正常样式
     * @return
     */
    public String getNormalStyle() {
        return "";
    }

    /**
     * 返回点击样式
     * @return
     */
    public String getMousePressedStyle() {
        return "";
    }
}
