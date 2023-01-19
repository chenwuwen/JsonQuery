package com.kanyun.ui.components;

import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.model.TableModel;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * property().addListener() 或
 * ObservableValue生成两种类型的事件：
 * 更改事件ChangeListener和无效事件InvalidationListener。
 * 更改事件表示该值已更改。如果当前值不再有效，则会生成一个无效事件。
 * 如果ObservableValue支持延迟求值，则这一区别就变得很重要，因为对于延迟求值的值，直到重新计算无效值，才知道该值是否真的发生了变化。
 * 因此，生成更改事件需要进行急切的评估， 而对于懒惰的实现可以生成无效事件。
 */
public class ObjectsItem extends HBox {

    private String text;

    private Node graphic;

    private static Background selectBackground = new Background(new BackgroundFill(Color.BLUE, new CornerRadii(2), new Insets(2)));
    private static Background defaultBackground = new Background(new BackgroundFill(Color.GREEN, new CornerRadii(2), new Insets(2)));


    public ObjectsItem(String text) {
        this(text, null);
    }

    public ObjectsItem(String text, Node graphic) {
        this.text = text;
        this.graphic = graphic;

        setSpacing(2);
        Label label = new Label(text);
        if (graphic == null) {
            getChildren().add(label);
        } else {
            getChildren().addAll(graphic, label);
        }


    }

    public void addListener() {
//        焦点属性监听,得到焦点设置选中,否则取消选中
        focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
//                    获取焦点
                    setBackground(selectBackground);
                } else {
//                    失去焦点
                    setBackground(defaultBackground);
                }
            }
        });

//        设置组件双击事件
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
//                UserEvent userEvent = new UserEvent(UserEvent.QUERY_TABLE);
//                TableModel tableModel = (TableModel) selectedItem.getValue();
//                userEvent.setTableModel(tableModel);
//                UserEventBridgeService.bridgeUserEvent2ContentPane(userEvent);
            }
        });
    }
}
