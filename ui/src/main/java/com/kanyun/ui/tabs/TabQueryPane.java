package com.kanyun.ui.tabs;

import com.jfoenix.controls.JFXButton;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.IconProperties;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.components.MultiQueryResultPane;
import com.kanyun.ui.components.SqlComponent;
import com.kanyun.ui.components.appstatusbar.AppStatusBar;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.StatusBar;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 新建查询Tab
 * 不包含分页组件
 */
public class TabQueryPane extends AbstractTab {
    private static final Logger log = LoggerFactory.getLogger(TabQueryPane.class);

    private static final Integer TOOLBAR_IMG_SIZE = 15;

    /**
     * SQL组件
     */
    private SqlComponent sqlComponent;

    /**
     * SQL执行结果TabPane
     */
    private MultiQueryResultPane multiQueryResultPane = new MultiQueryResultPane();

    /**
     * 动态信息栏
     */
    private StatusBar dynamicInfoStatusBar;

    /**
     * 运行SQL按钮
     */
    private MenuButton runButton;

    /**
     * 停止SQL运行按钮
     */
    private Button stopButton;

    /**
     * 导出查询结果按钮
     */
    private Button exportButton;
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
     * 主要的内容区域,采用分隔组件,默认分隔组件只有一个Item
     * 即 {@link #sqlComponent} 当SQL运行并产生结果时,再添加一个Item {@link #multiQueryResultPane}
     */
    private SplitPane contentArea = new SplitPane();

    /**
     * 当前Schema
     */
    private SimpleStringProperty currentSchema = new SimpleStringProperty();

    public TabQueryPane() {
        log.debug("TabQueryPane 新建查询页面被打开,当前默认数据库是：[{}]", currentSchema.get());
//        工具栏
        ToolBar toolBar = new ToolBar();
//        设置子组件间间距
        toolBar.setPadding(new Insets(3, 5, 3, 5));
        initToolBar(toolBar);
//        初始化SQL组件
        sqlComponent = new SqlComponent(currentSchema);
//        设置分隔布局的方向
        contentArea.setOrientation(Orientation.VERTICAL);
        contentArea.getItems().add(sqlComponent);
//        设置SQL子组件总是填充剩余空间
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        getChildren().addAll(toolBar, contentArea);
    }

    /**
     * 初始化工具栏
     */
    public void initToolBar(ToolBar toolBar) {
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
        exportButton = getExportButton();
//        工具栏添加子元素
        toolBar.getItems().addAll(dataBaseComboBox, runButton, stopButton, getBeautifyButton(), exportButton);

    }

    /**
     * 创建导出SQL执行结果按钮
     *
     * @return
     */
    private Button getExportButton() {
        JFXButton exportBtn = new JFXButton("结果导出");
        exportBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
//                设置初始目录(桌面)
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + File.separator + "Desktop"));
                fileChooser.setTitle("请选择导出结果的存放位置");
                fileChooser.getExtensionFilters().add(0, new FileChooser.ExtensionFilter("csv", "*.csv"));
                fileChooser.getExtensionFilters().add(1, new FileChooser.ExtensionFilter("xlsx", "*.xlsx"));
//                注意:这里是[保存文件对话框]注意与showOpenMultipleDialog()/showOpenDialog()[打开文件对话框的区别]
                File saveFile = fileChooser.showSaveDialog(new Stage());
                if (saveFile != null) {
                    String saveFilePath = saveFile.getAbsolutePath();
                    try {
                        multiQueryResultPane.exportQueryData2XlsxOrCsv(saveFilePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        return exportBtn;
    }


    /**
     * 创建SQL运行按钮
     *
     * @return
     */
    public MenuButton getRunButton() {
        FontAwesomeIconView fontAwesomeIcon
                = new FontAwesomeIconView(FontAwesomeIcon.PLAY);
        fontAwesomeIcon.setFill(Color.GREEN);
        MenuItem runCurrentStatementItemBtn = new MenuItem("运行当前选中的SQL");
        SplitMenuButton splitMenuButton = new SplitMenuButton(runCurrentStatementItemBtn);
        splitMenuButton.setText("运行");
        splitMenuButton.setGraphic(fontAwesomeIcon);
        TranslateTransition translateTransition = getTranslateTransition(fontAwesomeIcon);

//        如果设置了SplitMenuButton的setOnMouseReleased()/setOnMousePressed()方法,则setOnAction()不会触发,可以用setOnMouseClicked()方法代替
//        splitMenuButton.setOnAction(event -> {
//            currentExecuteSql = sqlComponent.getCurrentSql(false);
//            if (StringUtils.isEmpty(currentExecuteSql)) return;
//            fireEventAndExecuteSQL(currentExecuteSql);
//        });

        splitMenuButton.setOnMouseClicked(event -> {
            currentExecuteSql = sqlComponent.getCurrentSql(false);
            if (StringUtils.isEmpty(currentExecuteSql)) return;
            fireEventAndExecuteSQL(currentExecuteSql);
        });

        runCurrentStatementItemBtn.setOnAction(event -> {
            currentExecuteSql = sqlComponent.getCurrentSql(true);
            if (StringUtils.isEmpty(currentExecuteSql)) return;
            fireEventAndExecuteSQL(currentExecuteSql);
        });

//        点击空格键显示子菜单
        splitMenuButton.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                splitMenuButton.show();
            }
        });

        setButtonTransitionAnimation(splitMenuButton, translateTransition);
        return splitMenuButton;
    }

    /**
     * 发射事件(底部信息栏查询语句展示及执行进度和执行时间展示)和执行SQL
     *
     * @param sql
     */
    private void fireEventAndExecuteSQL(String sql) {
        UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL);
        userEvent.setSql(sql);
//       发射事件,将执行的SQL设置到信息栏中
        EventUtil.fireEvent(this, userEvent);
//       执行SQL
        sqlComponent.executeSQL(currentSchema.get(), sql);
//        执行SQL时,判断当前TableViewPane是否已加载到界面,如果加载过了,说明之前执行过SQL了,现在重新执行,需要将之前执行的结果清除掉
        if (contentArea.getItems().size() > 1) {
//            由于multiQueryResultPane是成员变量,因此只在界面移除multiQueryResultPane是不够的,multiQueryResultPane依然保留了之前查询结果的Tab,因此需要将这些信息移除掉
            multiQueryResultPane.clearTab();
            contentArea.getItems().remove(1);
        }
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
    private static TranslateTransition getTranslateTransition(Node target) {
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
    private void setButtonTransitionAnimation(ButtonBase btn, TranslateTransition translateTransition) {
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
        dynamicInfoStatusBar = new AppStatusBar();
//        不设置的话,默认有个OK字样
        dynamicInfoStatusBar.setText("");
    }

    @Override
    public StatusBar getDynamicInfoStatusBar() {
        return dynamicInfoStatusBar;
    }

    @Override
    public void addStatusBarEventListener() {
        addEventHandler(UserEvent.EXECUTE_SQL, event -> {
//            去掉SQL中的换行符和多余的空格(\s 可以匹配空格、制表符、换页符等空白字符的其中任意一个,\s+ 表示一个及以上)
            String sql = event.getSql().replaceAll("\\s+", " ");
            log.debug("设置动态信息栏执行的SQL(去除空格/换行等字符):[{}]", sql);
//            执行SQL时,设置动态信息栏的SQL信息,同时移除动态信息栏右侧的子项(同一个窗口多次执行的情况)
            dynamicInfoStatusBar.setText(sql);
            dynamicInfoStatusBar.getRightItems().removeAll(dynamicInfoStatusBar.getRightItems());
//            开启进度条
            startSqlExecuteProgressTask();
            controlButtonEnableOrDisable(false);
        });

        addEventHandler(UserEvent.EXECUTE_MULTI_SQL_COMPLETE, event -> {
            log.debug("接收到多条SQL执行完成事件,准备展示查询结果,设置动态信息栏,停止进度条");
            Map<String, Map<String, Object>> multiSqlExecuteInfo = event.getMultiSqlExecuteInfo();
            Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>> multiSqlExecuteResult = event.getMultiSqlExecuteResult();
            String totalCost = event.getTotalCost();
            multiQueryResultPane.setContent(multiSqlExecuteInfo, multiSqlExecuteResult, dynamicInfoStatusBar, totalCost);
            contentArea.getItems().add(multiQueryResultPane);
//            关闭进度条
            stopSqlExecuteProgressTask();
            controlButtonEnableOrDisable(true);
        });

        addEventHandler(UserEvent.EXECUTE_SQL_FAIL, event -> {
            controlButtonEnableOrDisable(true);
            stopSqlExecuteProgressTask();
            ExceptionDialog sqlExecuteErrDialog = new ExceptionDialog(event.getException());
            sqlExecuteErrDialog.setTitle("SQL执行失败");
            sqlExecuteErrDialog.setResizable(true);
//            controlfx的ExceptionDialog弹窗大小是根据异常信息长度自适应的,又由于错误信息可能会比较长,
//            而ExceptionDialog没有setMaxWidth()/setMaxHeight()方法,因此这里截断一部分字符,设置错误信息
//            由于ExceptionDialog还拥有textarea子组件,因此并不妨碍查看完整的错误信息.
            String contentText = sqlExecuteErrDialog.getContentText();
//            ExceptionDialog弹窗大小主要取决于内容的大小,setContentText()方法会将错误信息赋值到其label子组件,因此当错误内容较多时,截断一部分错误内容
            sqlExecuteErrDialog.setContentText(contentText.substring(0, Math.min(contentText.length(), 500)));
            sqlExecuteErrDialog.show();
        });
    }

    @Override
    public Node getTabGraphic() {
        return IconProperties.getIcon("tab.query", TAB_GRAPHIC_SIZE, Color.BLUE);
    }

    @Override
    public void onShown() {

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
//        执行进度属性解绑
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
