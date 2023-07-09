package com.kanyun.ui;

import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.layout.BottomInfoPane;
import com.kanyun.ui.layout.ContentPane;
import com.kanyun.ui.layout.DataBasePane;
import com.kanyun.ui.layout.TopButtonPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterfaceInitializer {

    private static final Logger log = LoggerFactory.getLogger(JsonQueryApplication.class);

    /**
     * 初始化主场景布局
     * @return
     */
    public static Scene initializeMainScene() {
        log.info("初始化主场景信息");
//        主体布局
        BorderPane rootPane = new BorderPane();
        rootPane.setPrefHeight(500);
        rootPane.setPrefWidth(800);

//        布局设置到场景中去(原来定义Scene是在布局完成后定义的,由于组件:ContentPane在初始化时,需要使用Scene发射事件,因此提前定义)
        Scene scene = new Scene(rootPane);
//        设置事件桥接类中的Scene引用
        UserEventBridgeService.setScene(scene);

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


//        监听分割组件中的第一个子组件的分割大小,然后改变BottomInfoPane中的dataBaseInfoStatusBar的大小
        centerPane.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                log.debug("监听到centerPane第一个子组件的分隔比例发生变化,原值:[{}],现值:[{}]", oldValue.doubleValue(), newValue.doubleValue());
                bottomInfoPane.setDataBaseInfoStatusBarWith(null, newValue.doubleValue());
            }
        });
        return scene;
    }
}
