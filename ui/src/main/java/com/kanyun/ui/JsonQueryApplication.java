package com.kanyun.ui;

import com.kanyun.ui.model.Constant;
import com.kanyun.ui.splash.SplashPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
    public void start(Stage splashStage) {
//        去掉最大化,最小化,关闭 按钮 不显示标题栏
        splashStage.initStyle(StageStyle.TRANSPARENT);
        splashStage.setOnShowing(event -> {
//            todo 窗口显示监听器数据初始化
        });
        splashStage.setTitle(Constant.APP_NAME);
//        设置为true时,需要按ESC才能退出全屏,全屏状态下,将隐藏标题栏,无法缩小与隐藏
        splashStage.setFullScreen(false);
//        欢迎页(过渡页)
        SplashPane splashPane = new SplashPane();
        Scene splashScene = new Scene(splashPane);
        splashStage.setScene(splashScene);
//        异步执行初始化操作
        new Thread(new AsyncInitializer(splashStage)).start();
        splashStage.show();
//        播放动画
        splashPane.playAnimation();
    }

    /**
     * 不能在这个方法中创建Stage或Scene
     * 将数据初始化放到过渡动画中做
     *
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        log.info("===========应用数据初始化===========");
//        JsonQuery jsonQuery = new JsonQuery();
//        jsonQuery.initConfig();
    }
}
