package com.kanyun.ui.components.objectslabel;

import com.kanyun.ui.model.BaseModel;
import com.kanyun.ui.model.ObjectsTypeEnum;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * property().addListener() 或
 * ObservableValue生成两种类型的事件：
 * 更改事件->ChangeListener和无效事件->InvalidationListener。
 * 更改事件表示该值已更改。如果当前值不再有效，则会生成一个无效事件。
 * 如果ObservableValue支持延迟求值，则这一区别就变得很重要，因为对于延迟求值的值，直到重新计算无效值，才知道该值是否真的发生了变化。
 * 因此，生成更改事件需要进行急切的评估， 而对于懒惰的实现可以生成无效事件。
 */
public class ObjectsLabel extends Control {

    private static final Logger logger = LoggerFactory.getLogger(ObjectsLabel.class);

    private String stylesheet;

    /**
     * 显示文字
     */
    private final StringProperty text = new SimpleStringProperty("");

    /**
     * 显示图标
     */
    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>();

    /**
     * 样式
     */
    private final StringProperty styleTextProperty = new SimpleStringProperty();

    public final StringProperty textProperty() {
        return text;
    }

    public final ObjectProperty<Node> graphicProperty() {
        return graphic;
    }


    public final void setText(String text) {
        textProperty().set(text);
    }


    public final String getText() {
        return textProperty().get();
    }

    public final Node getGraphic() {
        return graphicProperty().get();
    }


    public final void setGraphic(Node node) {
        graphicProperty().set(node);
    }

    public void setStyleText(String style) {
        styleTextProperty.set(style);
    }


    public String getStyleText() {
        return styleTextProperty.get();
    }

    public final StringProperty styleTextProperty() {
        return styleTextProperty;
    }

    /**
     * Canvas对象用于画图
     */
    private Canvas canvas;

    /**
     * 业务数据
     */
    private BaseModel model;

    /**
     * 组件类型
     */
    private ObjectsTypeEnum typeEnum;


    /**
     * 是否选中属性
     */
    private SimpleBooleanProperty selectedProperty = new SimpleBooleanProperty(false);


    @Override
    protected Skin<?> createDefaultSkin() {
        return new ObjectsLabelSkin(this);
    }

    public ObjectsLabel() {
        getStyleClass().add("objects-label");
    }

    public ObjectsLabel(String text) {
        setText(text);
        getStyleClass().add("objects-label");
    }

    public BaseModel getModel() {
        return model;
    }

    public void setModel(BaseModel model) {
        this.model = model;
    }

    public ObjectsTypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(ObjectsTypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    @Override
    public String getUserAgentStylesheet() {
//        加载用户自定义的样式文件(注意此样式文件的存放位置,在resources下的同package目录内,放在这个路径的好处是,打包后该css与class在同一位置)
        return getUserAgentStylesheet(ObjectsLabel.class, "objects-label.css");
    }

    protected final String getUserAgentStylesheet(Class<?> clazz,
                                                  String fileName) {
        if (stylesheet == null) {
            stylesheet = clazz.getResource(fileName).toExternalForm();
        }

        return stylesheet;
    }

}
