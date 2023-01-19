package com.kanyun.ui.components;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.kanyun.sql.func.AbstractFuncSource;
import com.kanyun.sql.func.FuncSourceType;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.model.JsonQueryConfig;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.Notifications;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import java.util.StringJoiner;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 添加函数弹窗
 */
public class FunctionDialog extends StackPane {
    private static final Logger log = LoggerFactory.getLogger(FunctionDialog.class);

    /**
     * 定义Tab面板
     */
    private TabPane tabPane = new TabPane();

    /**
     * Jar方式函数路径property
     */
    private SimpleStringProperty jarPathProperty = new SimpleStringProperty();
    /**
     * Maven方式GroupId路径property
     */
    private SimpleStringProperty mavenGroupIdProperty = new SimpleStringProperty();
    /**
     * Maven方式ArtifactId property
     */
    private SimpleStringProperty mavenArtifactIdProperty = new SimpleStringProperty();
    /**
     * Maven方式版本property
     */
    private SimpleStringProperty mavenVersionProperty = new SimpleStringProperty();

    /**
     * 消息通知组件
     */
    private NotificationPane notificationPane = new NotificationPane();

    public FunctionDialog() {
        setId("FunctionDialog");
        setStyle("-fx-border-color: red;");
        Tab jarTab = createJarTab();
        Tab mvnTab = createMvnTab();
        tabPane.getTabs().addAll(jarTab, mvnTab);
        setPadding(new Insets(0, 0, 0, 0));
//        构建通知组件(提示文字,显示方向,主题,显示组件等)
        buildFuncNotifyInfo(tabPane);
//        如果函数之前设置过,则绑定对应的值,并选择到对应的tab页
        JsonQueryConfig jsonQueryConfig = JsonQuery.getJsonQueryConfig();
        if (jsonQueryConfig != null && jsonQueryConfig.getFuncType() != null) {
            String funcPath = jsonQueryConfig.getFuncPath();
            if (jsonQueryConfig.getFuncType().equals(FuncSourceType.MAVEN.getType())) {
                String[] split = funcPath.split(":");
                mavenGroupIdProperty.set(split[0]);
                mavenArtifactIdProperty.set(split[1]);
                mavenVersionProperty.set(split[2]);
                tabPane.getSelectionModel().select(mvnTab);
            } else {
                jarPathProperty.set(funcPath);
                tabPane.getSelectionModel().select(jarTab);
            }
        }
        getChildren().add(notificationPane);
//        此处监听失效,由外部调用applyFunc()方法
        addEventHandler(UserEvent.APPLY_FUNC, event -> {
            applyFunc();
        });
//        直接在构造方法里显示通知是行不通的,除非使用异步任务
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                showFuncNotifyInfo();
            }
        });
    }

    /**
     * 构建函数提醒消息
     */
    public void buildFuncNotifyInfo(Node node) {
//        设置通知位置(覆盖)
        notificationPane.setContent(node);
//        设置提醒文字
        notificationPane.setText("注意：只识别 public static 修饰的方法,且jar包需包含依赖");
//        设置提醒出现的方向
        notificationPane.setShowFromTop(true);
    }

    /**
     * 打开通知,这里需要注意，一定要先判断通知是否是显示状态
     * 如果通知是显示状态,再次显示是没有效果的,因此需要先隐藏
     * 再开启，需要注意的是通知组件是显示状态并不代表通知是显示的
     */
    public void showFuncNotifyInfo() {
        if (notificationPane.isShowing()) {
            notificationPane.hide();
        }
        notificationPane.show();
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    TimeUnit.SECONDS.sleep(10);
//                    notificationPane.hide();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    /**
     * 创建Jar方式函数Tab
     *
     * @return
     */
    private Tab createJarTab() {
        Tab jarTab = createCommonTab(FuncSourceType.FILE.getType());
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(15);
        gridPane.setHgap(5);
        gridPane.setStyle("-fx-border-color: green;");
        Label jarPathLabel = new Label("输入Jar包路径,网络路径/本地路径");
        JFXButton jarPathSelectBtn = new JFXButton("Jar路径");
        jarPathSelectBtn.setPrefWidth(gridPane.getPrefWidth());
        JFXTextField jarPathField = new JFXTextField();
        jarPathField.textProperty().bindBidirectional(jarPathProperty);
//        设置文本框的placeHolder
//        jarPathField.setPromptText("Jar包路径");
        gridPane.add(jarPathLabel, 0, 0);
        gridPane.add(jarPathField, 0, 1, 2, 1);
        gridPane.add(jarPathSelectBtn, 1, 2);
        jarTab.setContent(gridPane);
        jarPathSelectBtn.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
//            设置初始目录
//            fileChooser.setInitialDirectory();
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("只支持.jar文件", "*.jar");
            fileChooser.getExtensionFilters().add(extensionFilter);
            fileChooser.setTitle("请选择你的jar路径");
            File file = fileChooser.showOpenDialog(new Stage());
            if (file != null) {
//                由于采用了Property的方式绑定了TextField组件,因此不能在通过TextField.set()方法来设置值,否则报错： A bound value cannot be set.
//                jarPathField.setText(selectedDirectory.getPath());
                jarPathProperty.set(file.getAbsolutePath());
            }
        });
        return jarTab;
    }

    /**
     * 创建Maven方式函数Tab
     *
     * @return
     */
    private Tab createMvnTab() {
        Tab mvnTab = createCommonTab(FuncSourceType.MAVEN.getType());
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(15);
        gridPane.setHgap(5);
        gridPane.setStyle("-fx-border-color: green;");
        Label groupIdLabel = new Label("groupId:");
        JFXTextField groupIdField = new JFXTextField();
        Label artifactIdLabel = new Label("artifactId:");
        JFXTextField artifactIdField = new JFXTextField();
        Label versionLabel = new Label("version:");
        JFXTextField versionField = new JFXTextField();
        gridPane.add(groupIdLabel, 0, 0);
        gridPane.add(groupIdField, 1, 0);
        gridPane.add(artifactIdLabel, 0, 1);
        gridPane.add(artifactIdField, 1, 1);
        gridPane.add(versionLabel, 0, 2);
        gridPane.add(versionField, 1, 2);
        mvnTab.setContent(gridPane);
        return mvnTab;
    }


    /**
     * 创建公共的Tab页
     *
     * @param tabName
     * @return
     */
    public Tab createCommonTab(String tabName) {
        Tab tab = new Tab(tabName);
        tab.setId(tabName);
        tab.setClosable(false);
        return tab;
    }

    /**
     * 应用函数
     */
    public void applyFunc() {
//        得到当前选中的Tab,当应用函数的时候,应用的是Maven形式还是Jar形式,主要看的就是当前显示的是哪个Tab
        Tab selectedItem = tabPane.getSelectionModel().getSelectedItem();
        if (selectedItem.getId().equals(FuncSourceType.MAVEN.getType())) {
            String groupId = mavenGroupIdProperty.get();
            String artifactId = mavenArtifactIdProperty.get();
            String version = mavenVersionProperty.get();
            log.info("添加函数,Maven形式：[{}:{}:{}]", groupId, artifactId, version);
            registerUdf(FuncSourceType.MAVEN, groupId, artifactId, version);
        } else {
            String jarPath = jarPathProperty.get();
            log.info("添加函数,Jar形式：[{}]", jarPath);
            registerUdf(FuncSourceType.FILE, jarPath);
        }
    }

    /**
     * 注册自定义函数
     */
    public void registerUdf(FuncSourceType funcSourceType, String... args) {
        try {
            AbstractFuncSource abstractFuncSource = funcSourceType.newInstance();
            abstractFuncSource.loadJar(args);
//            添加的函数写入配置文件
            StringJoiner stringJoiner = new StringJoiner(":");
            for (String arg : args) {
                stringJoiner.add(arg);
            }
            JsonQuery.persistenceFunctionConfig(funcSourceType, stringJoiner.toString());
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog sqlExecuteErrDialog = new ExceptionDialog(e);
            sqlExecuteErrDialog.setTitle("添加函数失败");
            sqlExecuteErrDialog.show();
        }
    }
}
