package com.kanyun.ui.components.objectslabel;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;


/**
 * 在javaFX中每一个UI组件都由一个Control,Skin组成。
 * 首先创建一个Control类继承javafx.scene.control.Control，
 * 它持有组件的属性，并且作为主的class，也就是说由它实例化，并且被加到父节点中。skin则负责展示，
 */
public class ObjectsLabelSkin extends SkinBase<ObjectsLabel> {
    /**
     * ObjectLabel容器
     */
    private HBox container = new HBox();

    protected ObjectsLabelSkin(ObjectsLabel control) {
        super(control);
//        super(control, new ObjectsLabelBehavior(control));
        container.setAlignment(Pos.CENTER_LEFT);
//        焦点可遍历性
        container.setFocusTraversable(true);
        container.getStyleClass().add("container");
        container.setPrefWidth(Region.USE_COMPUTED_SIZE);
        container.setSpacing(5.0);
        Text text = new Text(control.getText());
        Node graphic = control.getGraphic();
        container.getChildren().addAll(graphic, text);
        getChildren().setAll(container);
        container.setOnMouseClicked(event -> {
//            主动设置获取焦点
            container.requestFocus();
        });

    }


}
