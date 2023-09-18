package com.kanyun.ui.components.statusbar;

import com.sun.javafx.scene.control.skin.ScrollPaneSkin;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义StatusBarSkin,{@link impl.org.controlsfx.skin.StatusBarSkin}
 */
public class AppStatusBarSkin extends SkinBase<AppStatusBar> {
    private static final Logger logger = LoggerFactory.getLogger(AppStatusBarSkin.class);
    /**
     * 左侧Item集合容器
     */
    private HBox leftBox;
    /**
     * 右侧Item集合容器
     */
    private HBox rightBox;
    /**
     * 进度条组件
     */
    private ProgressBar progressBar;
    /**
     * StatusBar显示设置的内容位置
     */
    private Label label;

    /**
     * StatusBar 复制Label的文本区域,与label绑定
     */
    private TextField textField;

    /**
     * 主要为了保证,当Label内容过长时,内容可以横向滚动
     */
    private ScrollPane scrollPane;

    /**
     * 放置Label,主要为了保证Label在ScrollPane中保持居中放置
     */
    private StackPane stackPane;

    public AppStatusBarSkin(AppStatusBar statusBar) {
        super(statusBar);
        final BooleanBinding notZeroProgressProperty = Bindings.notEqual(0, statusBar.progressProperty());

        GridPane gridPane = new GridPane();

//        初始化左侧Item盒子
        leftBox = new HBox();
        leftBox.getStyleClass().add("left-items"); //$NON-NLS-1$

//        初始化右侧Item盒子
        rightBox = new HBox();
        rightBox.getStyleClass().add("right-items"); //$NON-NLS-1$

//        初始化进度条,并绑定statusBar的进度属性
        progressBar = new ProgressBar();
        progressBar.progressProperty().bind(statusBar.progressProperty());
        progressBar.visibleProperty().bind(notZeroProgressProperty);
        progressBar.managedProperty().bind(notZeroProgressProperty);


        buildStatusBarMessageContainer(statusBar);
        addStatusBarMessageContainerAction(statusBar);

        leftBox.getChildren().setAll(getSkinnable().getLeftItems());
        rightBox.getChildren().setAll(getSkinnable().getRightItems());

        statusBar.getLeftItems().addListener(
                (Observable evt) -> {
                    leftBox.getChildren().setAll(
                            getSkinnable().getLeftItems());


                });

        statusBar.getRightItems().addListener(
                (Observable evt) -> {
                    rightBox.getChildren().setAll(
                            getSkinnable().getRightItems());
                });

//        GridPane.setFillHeight(leftBox, true);
//        GridPane.setFillHeight(rightBox, true);
//        GridPane.setFillHeight(scrollPane, true);
//        GridPane.setFillHeight(progressBar, true);
//
//        GridPane.setVgrow(leftBox, Priority.ALWAYS);
//        GridPane.setVgrow(rightBox, Priority.ALWAYS);
//        GridPane.setVgrow(progressBar, Priority.ALWAYS);

//        设置左右盒子及进度条的最小宽度为可见的宽度(常量:Region.USE_COMPUTED_SIZE),避免它们的组件被其他组件占用
//        leftBox.setMinWidth(Region.USE_COMPUTED_SIZE);
//        leftBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
//        rightBox.setMinWidth(Region.USE_COMPUTED_SIZE);
//        rightBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
//        progressBar.setMinWidth(Region.USE_COMPUTED_SIZE);
//        progressBar.setPrefWidth(Region.USE_COMPUTED_SIZE);

        GridPane.setHgrow(scrollPane, Priority.ALWAYS);

//        将子组件添加到网格组件中
        gridPane.add(leftBox, 0, 0);
        gridPane.add(scrollPane, 1, 0);
        gridPane.add(progressBar, 2, 0);
        gridPane.add(rightBox, 3, 0);

        getChildren().add(gridPane);
    }

    /**
     * 构建AppStatusBar的信息容器
     *
     * @param statusBar
     */
    private void buildStatusBarMessageContainer(AppStatusBar statusBar) {
        buildStatusBarMessageNode(statusBar);
        scrollPane = new ScrollPane();
        stackPane = new StackPane();
        stackPane.setPadding(new Insets(0));
        stackPane.setBackground(statusBar.getBackground());
        scrollPane.setBackground(statusBar.getBackground());
        scrollPane.getStyleClass().add("app-status-bar-scroll-pane");
//        stackPane.setAlignment(Pos.CENTER_LEFT);
        stackPane.getChildren().add(label);

        scrollPane.getStyleClass().add("status-bar-scroll-pane");
//        JavaFX8中带有一个名为edge-to-edge的class,可以直接实现无边框效果,默认的灰色边框和选中时蓝色边框都会消失，效果非常好
        scrollPane.getStyleClass().add("edge-to-edge");
//        设置横/纵向滚动条策略(不显示滚动条),仅仅只是不展示了,但滚动条的空间还是占用的
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefViewportHeight(10);
//        消除滚动条的空间,由于jdk8没有直接获取ScrollBar的方法,只能暂时使用这种形式
//        直接使用lookup()查找ScrollBar的方式,得到的是一个Null,因为此时ScrollPane还未创建好
        ScrollBar hScrollBar = (ScrollBar) scrollPane.lookup(".scroll-bar:horizontal");
        assert hScrollBar == null : "直接使用ScrollPane.lookup()获取ScrollBar的结果非空";
//        直接使用此种方式获取的ScrollPaneSkin也为Null
        ScrollPaneSkin scrollPaneSkin = (ScrollPaneSkin) scrollPane.getSkin();
        assert scrollPaneSkin == null : "直接使用ScrollPane.getSkin()获取ScrollPaneSkin的结果非空";
        scrollPane.skinProperty().addListener(new ChangeListener<Skin<?>>() {
            @Override
            public void changed(ObservableValue<? extends Skin<?>> observable, Skin<?> oldValue, Skin<?> newValue) {
                ScrollPaneSkin scrollPaneSkin = (ScrollPaneSkin) newValue;
                for (Node child : scrollPaneSkin.getChildren()) {
                    if (child instanceof ScrollBar) {
//                        已知:设置-fx-pref-height: 0,滚动条还是会占用空间,而设置为更大的数,则占用空间则更大
                        ScrollBar scrollBar = (ScrollBar) child;
//                        获取ScrollBar的方向(滚动条方向)
                        Orientation orientation = scrollBar.getOrientation();
                        if (orientation == Orientation.HORIZONTAL) {
                            scrollBar.setMaxSize(0,0);
                        }else {
                            scrollBar.setMaxSize(0,0);
                        }
                    }
                }
            }
        });


//        是否根据内容自动调高(对于高度来说,设置为true,可以保证ScrollPane中的子元素居中)
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        scrollPane.setContent(stackPane);
    }


    /**
     * 构建消息显示的Node组件,信息展示在Label和TextField上
     *
     * @param statusBar
     */
    private void buildStatusBarMessageNode(AppStatusBar statusBar) {
        label = new Label();
        textField = new TextField();
        textField.setEditable(false);
        textField.textProperty().bind(label.textProperty());
        textField.maxWidthProperty().bind(label.widthProperty());
        textField.setBackground(statusBar.getBackground());

//        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.textProperty().bind(statusBar.textProperty());
        label.graphicProperty().bind(statusBar.graphicProperty());
        label.styleProperty().bind(getSkinnable().styleProperty());
        label.getStyleClass().add("status-label");
//        内容超长不显示省略号
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
    }


    /**
     * 添加AppStatusBar的信息容器区域监听
     */
    private void addStatusBarMessageContainerAction(AppStatusBar statusBar) {

        scrollPane.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                logger.debug("AppStatusBar鼠标移动到Label区域,将Label替换为TextField");
                if (StringUtils.isNotEmpty(label.getText())) {
                    if (stackPane.getChildren().get(0) != textField) {
                        stackPane.getChildren().set(0, textField);
                    }
                }
            }
        });

        scrollPane.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                logger.debug("AppStatusBar鼠标移出Label区域,将TextField替换为Label");
                stackPane.getChildren().set(0, label);
            }
        });
//        scrollPane.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                if (event.isStillSincePress())
//                logger.debug("AppStatusBar鼠标移出Label区域,将TextField替换为Label");
//                stackPane.getChildren().set(0, label);
//            }
//        });

    }
}
