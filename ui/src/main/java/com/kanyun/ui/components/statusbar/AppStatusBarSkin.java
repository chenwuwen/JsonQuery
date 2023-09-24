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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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

    /**
     * AppStatusBar显示信息内容信息区域组件 {@link this#scrollPane 的父组件}
     * StackPane背景色默认是透明的,会显示父组件的背景色
     */
    private StackPane statusBarMessageContainer;

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

        GridPane.setFillHeight(leftBox, true);
        GridPane.setFillHeight(rightBox, true);
        GridPane.setFillHeight(statusBarMessageContainer, true);
        GridPane.setFillHeight(progressBar, true);

        GridPane.setVgrow(leftBox, Priority.ALWAYS);
        GridPane.setVgrow(rightBox, Priority.ALWAYS);
        GridPane.setVgrow(progressBar, Priority.ALWAYS);
        GridPane.setVgrow(statusBarMessageContainer, Priority.ALWAYS);

//        设置左右盒子及进度条的最小宽度为可见的宽度(常量:Region.USE_COMPUTED_SIZE),避免它们的组件被其他组件占用
        leftBox.setMinWidth(Region.USE_COMPUTED_SIZE);
        leftBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        rightBox.setMinWidth(Region.USE_COMPUTED_SIZE);
        rightBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        progressBar.setMinWidth(Region.USE_COMPUTED_SIZE);
        progressBar.setPrefWidth(Region.USE_COMPUTED_SIZE);

        GridPane.setHgrow(statusBarMessageContainer, Priority.ALWAYS);

//        将子组件添加到网格组件中
        gridPane.add(leftBox, 0, 0);
        gridPane.add(statusBarMessageContainer, 1, 0);
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
        statusBarMessageContainer = new StackPane();
        scrollPane = new ScrollPane();
        stackPane = new StackPane();
        stackPane.setPadding(new Insets(0));
        scrollPane.setPadding(new Insets(0));
        stackPane.setAlignment(Pos.CENTER_LEFT);
//        JavaFX8中带有一个名为edge-to-edge的class,可以直接实现无边框效果,默认的灰色边框和选中时蓝色边框都会消失,效果非常好(但是这种方式可能会随着api变化而产生变动)
        stackPane.getStyleClass().add("edge-to-edge");
        scrollPane.getStyleClass().add("edge-to-edge");


//       由于设置ScrollPane背景色太麻烦,因此直接设置ScrollPane的背景为透明,再为ScrollPane添加父组件,然后ScrollPane的背景色就显示为其父组件的背景色
        scrollPane.setStyle("-fx-background: transparent");
//        setScrollPaneBackground(statusBar);

        stackPane.getChildren().add(label);


//        设置横/纵向滚动条策略(不显示滚动条),仅仅只是不展示了,但滚动条的空间还是占用的
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//        消除滚动条的空间,由于jdk8没有直接获取ScrollBar的方法,只能暂时使用这种形式,设置ScrollPane的最小高度,用以抵消ScrollBar占用的空间
        scrollPane.setMinHeight(0);
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
                        ScrollBar scrollBar = (ScrollBar) child;
//                        获取ScrollBar的方向(滚动条方向)
                        Orientation orientation = scrollBar.getOrientation();
                    }
                }
            }
        });


//        设置子组件尺寸是否填充ScrollPane的窗口
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
//        设置ScrollPane的子组件
        scrollPane.setContent(stackPane);
        statusBarMessageContainer.getChildren().add(scrollPane);
    }


    /**
     * 构建消息显示的Node组件,信息展示在Label和TextField上
     *
     * @param statusBar
     */
    private void buildStatusBarMessageNode(AppStatusBar statusBar) {
//        TextField设置
        textField = new TextField();
        textField.setEditable(false);
        textField.textProperty().bind(statusBar.textProperty());
        textField.getStyleClass().add("app-status-text");

//        Label设置
        label = new Label();
        label.textProperty().bind(statusBar.textProperty());
        label.graphicProperty().bind(statusBar.graphicProperty());
        label.getStyleClass().add("app-status-label");
//        内容超长不显示省略号
        label.setTextOverrun(OverrunStyle.CLIP);
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
//                        设置光标到第一个字符前,如果之前光标移动过,此时光标会保持之前的位置,因此需要重新设置一下
                        textField.positionCaret(0);
                        stackPane.getChildren().set(0, textField);
                    }
                }
            }
        });

//        scrollPane.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent event) {
//                logger.debug("AppStatusBar鼠标移出Label区域,将TextField替换为Label");
//                stackPane.getChildren().set(0, label);
//            }
//        });

        scrollPane.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                logger.debug("AppStatusBar鼠标移出Label区域,将TextField替换为Label");
                stackPane.getChildren().set(0, label);
            }
        });

    }


    /**
     * 设置ScrollPane的背景色
     * 取StatusBar的主色来设置ScrollPane的背景色
     * 由于UI界面存在一条白色的边框,因此设置ScrollPane的背景色(ScrollPane的背景色就是一个边框)
     * 但是发现ScrollPane对背景色的要求(参数)比较严格,随意设置可能不会生效,使用css设置效果不错
     * 但不一定能满足需求,因此该方法弃用,将ScrollPane的背景色设置为透明,然后显示ScrollPane父组件的颜色
     * 同时设置ScrollPane父组件的背景色为StatusBar的主色(或设定的其他颜色)
     */
    @Deprecated
    private void setScrollPaneBackground(StatusBar statusBar) {
//        设置ScrollPane背景色需要注意一个点就是ScrollPane的背景色是一个圈,类似边框颜色.Background实例不同,可能导致背景色设置不成功
        BackgroundFill statusBarBackgroundFill = statusBar.getBackground().getFills().get(0);
        Paint statusBarFill = statusBarBackgroundFill.getFill();
//        Paint statusBarFill = Color.BLUE;
//        后两个参数对ScrollPane设置背景色起到重要作用,这里设置为Null,相当于只取StatusBar的主色,给ScrollPane设置背景色
        Background scrollPaneBackground = new Background(new BackgroundFill(statusBarFill, null, null));
        scrollPane.setBackground(scrollPaneBackground);
    }
}
