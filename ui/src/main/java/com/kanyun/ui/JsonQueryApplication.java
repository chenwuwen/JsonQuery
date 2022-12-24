package com.kanyun.ui;

import com.kanyun.ui.components.SplashPane;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.layout.BottomInfoPane;
import com.kanyun.ui.layout.ContentPane;
import com.kanyun.ui.layout.DataBasePane;
import com.kanyun.ui.layout.TopButtonPane;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JsonQuery启动类
 */
public class JsonQueryApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(JsonQueryApplication.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
//        去掉最大化,最小化,关闭 按钮
//        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setOnShowing(event -> {
//            todo 窗口显示监听器数据初始化
        });
        primaryStage.setOnCloseRequest(event -> {
//            todo 弹出关闭确认
        });
        primaryStage.setTitle("JsonQuery");
//        设置为true时,需要按ESC才能退出全屏,全屏状态下,将隐藏标题栏,无法缩小与隐藏
        primaryStage.setFullScreen(false);

//        欢迎页(过渡页)
        SplashPane splashPane = new SplashPane();
        Scene splashScene = new Scene(splashPane);
        primaryStage.setScene(splashScene);

//        异步操作,同时可以更新UI
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                JsonQuery jsonQuery = new JsonQuery();
                try {
                    jsonQuery.initConfig();
//                    切换到主场景
                    setMainLayout(primaryStage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        primaryStage.show();
        splashPane.playAnimation();
    }

    /**
     * 设置主场景布局
     *
     * @param stage
     */
    public void setMainLayout(Stage stage) {
//        主体布局
        BorderPane rootPane = new BorderPane();
        rootPane.setPrefHeight(500);
        rootPane.setPrefWidth(800);

//        上方按钮区域
        TopButtonPane topButtonPane = new TopButtonPane();
        rootPane.setTop(topButtonPane);

//        下方信息区域
        BottomInfoPane bottomInfoPane = new BottomInfoPane();
        rootPane.setBottom(bottomInfoPane);
//        中间分割区域
        SplitPane centerPane = new SplitPane();

//        左侧数据库区域
        DataBasePane dataBasePane = new DataBasePane();
//        右侧主内容区域
        ContentPane contentPane = new ContentPane();

//        分割布局添加子项
        centerPane.getItems().addAll(dataBasePane, contentPane);
//        设置分割区域宽度比例
        centerPane.setDividerPositions(0.2);
        rootPane.setCenter(centerPane);

//        布局设置到场景中去
        Scene scene = new Scene(rootPane);
        UserEventBridgeService.setScene(scene);
//        监听分割组件中的第一个子组件的分割大小,然后改变BottomInfoPane中的dataBaseInfoStatusBar的大小
        centerPane.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                log.debug("监听到centerPane第一个子组件的分隔比例发生变化,原值:[{}],现值:[{}]", oldValue.doubleValue(), newValue.doubleValue());
                bottomInfoPane.setDataBaseInfoStatusBarWith(null, newValue.doubleValue());
            }
        });

//        场景设置到窗口区域
        stage.setScene(scene);
    }

    /**
     * 不能在这个方法中创建Stage或Scene
     * 将数据初始化放到过渡动画中做
     *
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        log.info("===========JsonQuery数据初始化===========");
//        JsonQuery jsonQuery = new JsonQuery();
//        jsonQuery.initConfig();
    }
}
