package com.kanyun.ui.tabs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.components.SqlComponent;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.model.DataBaseModel;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.SearchableComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 新建查询Tab
 */
public class TabQueryPane extends VBox {
    private static final Logger log = LoggerFactory.getLogger(TabQueryPane.class);

    private static final Integer TOOLBAR_IMG_SIZE = 15;

    /**
     * 当前Schema
     */
    private SimpleStringProperty currentSchema = new SimpleStringProperty();

    public TabQueryPane() {
        log.debug("TabQueryPane 新建查询页面被打开,当前默认数据库是：[{}]");
//        工具栏
        HBox toolBar = new HBox();
//        设置子组件间间距
        toolBar.setSpacing(5);
        initToolBar(toolBar);
//        初始化SQL组件
        SqlComponent sqlComponent = new SqlComponent();
        getChildren().addAll(toolBar, sqlComponent);
    }

    /**
     * 初始化工具栏
     */
    public void initToolBar(HBox toolBar) {
//        数据库下拉框
//        JFXComboBox<String> dataBaseComboBox = new JFXComboBox<>();
        ComboBox<String> dataBaseComboBox = new SearchableComboBox<>();
//        对下拉框组件设置监听,读取下拉框选中数据库,即当前SQL的默认数据库
        dataBaseComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                currentSchema.set(newValue);
            }
        });
        ObservableList<DataBaseModel> dataBaseModels = JsonQuery.dataBaseModels;
        List<String> dataBaseNames = dataBaseModels.stream().map(dataBaseModel -> dataBaseModel.getName()).collect(Collectors.toList());
        dataBaseComboBox.getItems().addAll(dataBaseNames);
        String defaultSchema = ModelJson.getDefaultSchema();
        if (StringUtils.isNotEmpty(defaultSchema)) {
//            当有默认Schema时,打开新的查询页,下拉框默认选中默认的数据库
            dataBaseComboBox.getSelectionModel().select(defaultSchema);
            currentSchema.set(defaultSchema);
        }

//        工具栏添加子元素
        toolBar.getChildren().addAll(dataBaseComboBox, getRunButton(), getBeautifyButton());
    }


    /**
     * 创建SQL运行按钮
     *
     * @return
     */
    public Button getRunButton() {
        JFXButton runBtn = new JFXButton("运行");
        ImageView runImageView = new ImageView("/asserts/run.png");
        runImageView.setFitWidth(TOOLBAR_IMG_SIZE);
        runImageView.setFitHeight(TOOLBAR_IMG_SIZE);
        runBtn.setGraphic(runImageView);
//        鼠标是否退出状态
        SimpleBooleanProperty mouseExits = new SimpleBooleanProperty(true);
        TranslateTransition translateTransition = getTranslateTransition(runImageView);
//        按钮按下事件
        runBtn.setOnMousePressed(event -> {
//            执行正向动画
            translateTransition.setRate(1);
            translateTransition.play();
        });

//        按钮释放事件
        runBtn.setOnMouseReleased(event -> {
            if (mouseExits.get()) {
//                鼠标释放时,只有鼠标还没有退出按钮才可以出发操作
//                执行反向动画
                translateTransition.setRate(-1);
                translateTransition.play();
                UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL);
                DataBaseModel dataBaseModel = new DataBaseModel();
                dataBaseModel.setName(currentSchema.get());
                userEvent.setDataBaseModel(dataBaseModel);
                UserEventBridgeService.bridgeUserEvent2SqlComponent(userEvent);
            }
        });

//        鼠标退出事件
        runBtn.setOnMouseExited(event -> {
//            执行反向动画
            translateTransition.setRate(-1);
            translateTransition.play();
            mouseExits.set(false);
        });
        return runBtn;
    }

    /**
     * 创建SQL美化按钮
     *
     * @return
     */
    public Button getBeautifyButton() {
        ImageView beautifyImageView = new ImageView("/asserts/beautify.png");
        beautifyImageView.setFitWidth(TOOLBAR_IMG_SIZE);
        beautifyImageView.setFitHeight(TOOLBAR_IMG_SIZE);
        JFXButton beautifyBtn = new JFXButton("美化SQL", beautifyImageView);
//        鼠标是否退出状态
        SimpleBooleanProperty mouseExits = new SimpleBooleanProperty(true);
        TranslateTransition translateTransition = getTranslateTransition(beautifyImageView);
//        按钮按下事件
        beautifyBtn.setOnMousePressed(event -> {
//            执行正向动画
            translateTransition.setRate(1);
            translateTransition.play();
        });

//        按钮释放事件
        beautifyBtn.setOnMouseReleased(event -> {
            if (mouseExits.get()) {
//                鼠标释放时,只有鼠标还没有退出按钮才可以出发操作
//                执行反向动画
                translateTransition.setRate(-1);
                translateTransition.play();
                UserEvent userEvent = new UserEvent(UserEvent.BEAUTIFY_SQL);
                UserEventBridgeService.bridgeUserEvent2SqlComponent(userEvent);
            }
        });

//        鼠标退出事件
        beautifyBtn.setOnMouseExited(event -> {
//            执行反向动画
            translateTransition.setRate(-1);
            translateTransition.play();
            mouseExits.set(false);
        });

        return beautifyBtn;
    }

    /**
     * 创建动画实例类
     *
     * @param target
     * @return
     */
    private static TranslateTransition getTranslateTransition(ImageView target) {
//        实例化动画类
        TranslateTransition translateTransition = new TranslateTransition();
//        动画持续时长
        translateTransition.setDuration(Duration.seconds(0.02));
//        设定动画起始位置
        translateTransition.setFromX(0);
        translateTransition.setFromY(0);
//        设定动画目的位置
        translateTransition.setToX(2);
        translateTransition.setToY(2);
//        设定动画执行对象
        translateTransition.setNode(target);
//        是否自动反转动画
        translateTransition.setAutoReverse(false);
        return translateTransition;
    }

}
