package com.kanyun.ui;

import com.kanyun.ui.layout.BottomInfoPane;
import com.kanyun.ui.layout.ContentPane;
import com.kanyun.ui.layout.DataBasePane;
import com.kanyun.ui.layout.TopButtonPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * JsonQuery启动类
 */
public class JsonQueryApplication extends Application {

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
//        主体布局
        BorderPane rootPane = new BorderPane();
        rootPane.setPrefHeight(500);
        rootPane.setPrefWidth(800);

//        上方按钮区域
        TopButtonPane topButtonPane = new TopButtonPane();
        rootPane.setTop(topButtonPane);

//        下方信息区域
        rootPane.setBottom(new BottomInfoPane());

//        中间分割区域
        SplitPane splitPane = new SplitPane();
//        左侧数据库区域
        DataBasePane dataBasePane = new DataBasePane();
//        右侧主内容区域
        ContentPane contentPane = new ContentPane();

//        分割布局添加子项
        splitPane.getItems().addAll(dataBasePane, contentPane);
//        设置分割区域宽度比例
        splitPane.setDividerPositions(0.2);
        rootPane.setCenter(splitPane);

//        布局设置到场景中去
        Scene scene = new Scene(rootPane);

//        场景设置到窗口区域
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void init() throws Exception {
        JsonQuery jsonQuery = new JsonQuery();
        jsonQuery.initConfig();


    }
}
