package com.kanyun.ui.tabs;

import com.jfoenix.controls.JFXButton;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.components.SqlComponent;
import com.kanyun.ui.event.StatusBarProgressTask;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.model.DataBaseModel;
import com.sun.javafx.event.EventUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.StatusBar;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 新建查询Tab
 */
public class TabQueryPane extends VBox implements TabKind {
    private static final Logger log = LoggerFactory.getLogger(TabQueryPane.class);

    private static final Integer TOOLBAR_IMG_SIZE = 15;

    /**
     * SQL组件
     */
    private SqlComponent sqlComponent;

    /**
     * 动态信息栏
     */
    private StatusBar dynamicInfoStatusBar;

    /**
     * 动态信息属性
     */
    private SimpleStringProperty dynamicInfoProperty = new SimpleStringProperty();

    /**
     * 运行SQL按钮
     */
    private Button runButton;

    /**
     * 停止SQL运行按钮
     */
    private Button stopButton;

    /**
     * 当前正在执行的SQL
     */
    private String currentExecuteSql;

    /**
     * 进度条展示异步任务线程池(主要用来执行进度条展示),静态变量
     */
    private static ExecutorService progressTaskExecutorPool = Executors.newCachedThreadPool();

    /**
     * 自定义StatusBar进度条任务
     */
    private StatusBarProgressTask statusBarProgressTask;

    /**
     * 当前Schema
     */
    private SimpleStringProperty currentSchema = new SimpleStringProperty();

    public TabQueryPane() {
        log.debug("TabQueryPane 新建查询页面被打开,当前默认数据库是：[{}]", currentSchema.get());
//        工具栏
        HBox toolBar = new HBox();
//        设置子组件间间距
        toolBar.setSpacing(10);
        toolBar.setPadding(new Insets(5, 0, 5, 2));
        initToolBar(toolBar);
//        初始化SQL组件
        sqlComponent = new SqlComponent(currentSchema);
//        设置SQL子组件总是填充剩余空间
        VBox.setVgrow(sqlComponent, Priority.ALWAYS);
        getChildren().addAll(toolBar, sqlComponent);
        createDynamicInfoStatusBar();
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
        runButton = getRunButton();
        stopButton = getStopButton();
//        工具栏添加子元素
        toolBar.getChildren().addAll(dataBaseComboBox, runButton, stopButton, getBeautifyButton());
    }


    /**
     * 创建SQL运行按钮
     *
     * @return
     */
    public Button getRunButton() {
        FontAwesomeIconView fontAwesomeIcon
                = new FontAwesomeIconView(FontAwesomeIcon.PLAY);
        fontAwesomeIcon.setFill(Color.GREEN);
        JFXButton runBtn = new JFXButton("运行", fontAwesomeIcon);
        ImageView runImageView = new ImageView("/asserts/run.png");
        runImageView.setFitWidth(TOOLBAR_IMG_SIZE);
        runImageView.setFitHeight(TOOLBAR_IMG_SIZE);
        runBtn.setGraphic(runImageView);

        TranslateTransition translateTransition = getTranslateTransition(runImageView);

        runBtn.setOnAction(event -> {
            currentExecuteSql = sqlComponent.getCurrentSql(false);
            if (StringUtils.isEmpty(currentExecuteSql)) return;
            fireEventAndExecuteSQL(currentExecuteSql);
        });
        setButtonTransitionAnimation(runBtn, translateTransition);
        return runBtn;
    }

    /**
     * 发射事件(底部信息栏查询语句展示及执行进度和执行时间展示)和执行SQL
     * @param sql
     */
    private void fireEventAndExecuteSQL(String sql) {
        UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL);
        userEvent.setSql(sql);
//       发射事件,将执行的SQL设置到信息栏中
        EventUtil.fireEvent(this, userEvent);
//       执行SQL
        sqlComponent.executeSQL(currentSchema.get(), sql);
    }

    /**
     * 创建SQL停止查询按钮
     *
     * @return
     */
    public Button getStopButton() {
        FontAwesomeIconView fontAwesomeIcon
                = new FontAwesomeIconView(FontAwesomeIcon.STOP);
        fontAwesomeIcon.setFill(Color.RED);
        JFXButton stopBtn = new JFXButton("停止", fontAwesomeIcon);
//        按钮默认被禁用,当点击运行按钮时,该按钮启用
        stopBtn.setDisable(true);
        stopBtn.setOnAction(event -> {
            log.debug("停止SQL执行按钮被触发,停止SQL：[{}] 执行", currentExecuteSql);
            sqlComponent.stopSQL();
            stopSqlExecuteProgressTask();
            controlButtonEnableOrDisable(true);
        });
        return stopBtn;
    }

    /**
     * 创建SQL美化按钮
     *
     * @return
     */
    public Button getBeautifyButton() {
        FontAwesomeIconView fontAwesomeIcon
                = new FontAwesomeIconView(FontAwesomeIcon.MAGIC);
        fontAwesomeIcon.setFill(Color.BLUE);
        ImageView beautifyImageView = new ImageView("/asserts/beautify.png");
        beautifyImageView.setFitWidth(TOOLBAR_IMG_SIZE);
        beautifyImageView.setFitHeight(TOOLBAR_IMG_SIZE);
        JFXButton beautifyBtn = new JFXButton("美化SQL", fontAwesomeIcon);

        TranslateTransition translateTransition = getTranslateTransition(beautifyImageView);
        beautifyBtn.setOnAction(event -> {
            sqlComponent.beautifySQL();
        });
        setButtonTransitionAnimation(beautifyBtn, translateTransition);

        return beautifyBtn;
    }

    /**
     * 创建动画实例类
     *
     * @param target 移动目标
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
//        设定动画目的位置(下沉效果,Y轴移动2个像素)
//        translateTransition.setToX(2);
        translateTransition.setToY(2);
//        设定动画执行对象
        translateTransition.setNode(target);
//        是否自动反转动画
        translateTransition.setAutoReverse(false);
        return translateTransition;
    }

    /**
     * 设置按钮位移动画
     */
    private void setButtonTransitionAnimation(Button btn, TranslateTransition translateTransition) {
//        按钮按下事件
        btn.setOnMousePressed(event -> {
//            执行正向动画
            translateTransition.setRate(1);
            translateTransition.play();
        });
//        按钮释放事件
        btn.setOnMouseReleased(event -> {
//           执行反向动画
            translateTransition.setRate(-1);
            translateTransition.play();

        });

//        鼠标退出事件
        btn.setOnMouseExited(event -> {
//            执行反向动画
            translateTransition.setRate(-1);
            translateTransition.play();
        });
    }

    @Override
    public TabKindEnum getTabKind() {
        return TabKindEnum.SQL_TAB;
    }

    @Override
    public void createDynamicInfoStatusBar() {
        dynamicInfoStatusBar = new StatusBar();
//        不设置的话,默认有个OK字样
        dynamicInfoStatusBar.setText("");
        dynamicInfoStatusBar.textProperty().bind(dynamicInfoProperty);
        addStatusBarEventListener();
    }

    @Override
    public StatusBar getDynamicInfoStatusBar() {
        return dynamicInfoStatusBar;
    }

    @Override
    public void addStatusBarEventListener() {
        addEventHandler(UserEvent.EXECUTE_SQL, event -> {
//            去掉SQL中的换行符
            String sql = event.getSql().replaceAll("\r|\n|\t", "");
            log.debug("设置动态SQL信息:[{}]", sql);
//            执行SQL时,设置动态信息栏的SQL信息,同时移除动态信息栏右侧的子项(同一个窗口多次执行的情况)
            dynamicInfoProperty.set(sql);
            dynamicInfoStatusBar.getRightItems().removeAll(dynamicInfoStatusBar.getRightItems());
//            开启进度条
            startSqlExecuteProgressTask();
            controlButtonEnableOrDisable(false);
        });

        addEventHandler(UserEvent.EXECUTE_SQL_COMPLETE, event -> {
            log.debug("接收到SQL执行完成事件,准备停止进度条,并设置查询记录数及查询耗时");
//            关闭进度条
            stopSqlExecuteProgressTask();
            controlButtonEnableOrDisable(true);
            Map<String, Object> queryInfo = event.getQueryInfo();
            String cost = "查询耗时：" + TabKind.getSecondForMilliSecond(queryInfo.get("cost")) + "秒";
            String record = "总记录数：" + queryInfo.get("count");
            Label costLabel = TabKind.createCommonLabel(cost, dynamicInfoStatusBar, null, Color.GREEN);
            costLabel.setPrefHeight(dynamicInfoStatusBar.getHeight());
            Label recordLabel = TabKind.createCommonLabel(record, dynamicInfoStatusBar, null, Color.GREEN);
            recordLabel.setPrefHeight(dynamicInfoStatusBar.getHeight());
//            注意这里如果是set(index,node),那么如果指定索引处没有Node将会报错
            dynamicInfoStatusBar.getRightItems().add(0, new Separator(Orientation.VERTICAL));
            dynamicInfoStatusBar.getRightItems().add(1, costLabel);
            dynamicInfoStatusBar.getRightItems().add(2, new Separator(Orientation.VERTICAL));
            dynamicInfoStatusBar.getRightItems().add(3, recordLabel);

        });

        addEventHandler(UserEvent.EXECUTE_SQL_FAIL, event -> {
            controlButtonEnableOrDisable(true);
            stopSqlExecuteProgressTask();
            ExceptionDialog sqlExecuteErrDialog = new ExceptionDialog(event.getException());
            sqlExecuteErrDialog.setTitle("SQL执行失败");
            sqlExecuteErrDialog.show();
        });
    }


    /**
     * 开启SQL执行进度
     */
    public void startSqlExecuteProgressTask() {
        statusBarProgressTask = new StatusBarProgressTask();
        progressTaskExecutorPool.execute(statusBarProgressTask);
//        当StatusBar进度属性绑定到task的进度属性时,StatusBar就展示了进度条,同理当task的进度属性为100%时,StatusBar进度条将消失
        dynamicInfoStatusBar.progressProperty().bind(statusBarProgressTask.progressProperty());
    }

    /**
     * 停止SQL执行进度
     */
    public void stopSqlExecuteProgressTask() {
        statusBarProgressTask.stopProgress();
//        属性解绑
        dynamicInfoStatusBar.progressProperty().unbind();
    }

    /**
     * 控制按钮的启用与禁用,这里仅控制运行按钮和停止按钮
     * 由于此两个按钮总是互斥关系,因此用Boolean类型判断
     * 当参数为true,则运行按钮启用,停止按钮禁用
     * 当参数为false,则运行按钮禁用,停止按钮启用
     */
    public void controlButtonEnableOrDisable(boolean flag) {

        if (flag) {
            runButton.setDisable(false);
            stopButton.setDisable(true);
        } else {
            runButton.setDisable(true);
            stopButton.setDisable(false);
        }
    }
}
