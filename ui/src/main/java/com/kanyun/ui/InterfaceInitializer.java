package com.kanyun.ui;

import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.layout.BottomInfoPane;
import com.kanyun.ui.layout.ContentPane;
import com.kanyun.ui.layout.DataBasePane;
import com.kanyun.ui.layout.TopButtonPane;
import com.kanyun.ui.model.Constant;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主界面布局构建
 * 主布局采用BorderPane
 * 主要由:上(顶部按钮区域),中(内容区域),下(状态信息展示区域) 组成
 * 中间布局采用SplitPane,主要由左侧数据库表树区域和右侧内容区域组成,并设置了分割比例,同时对分割比例设置了监听
 * 当前类只负责生成场景
 */
public class InterfaceInitializer {

    private static final Logger log = LoggerFactory.getLogger(JsonQueryApplication.class);

    /**
     * 默认的中间区域分隔比例
     */
    private static final Double DEFAULT_CENTER_AREA_DIVIDER_POSITIONS = 0.2;

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
//        设置左侧数据库列表树的最大宽度,这样当拖动分割线至设置的最大宽度时,分割线将不能被拖动
        dataBasePane.setMaxWidth(Constant.DATABASE_TREE_PANE_MAX_WIDTH);

//        右侧主内容区域
        ContentPane contentPane = new ContentPane();
//        设置分割区域宽度比例
        centerPane.setDividerPositions(DEFAULT_CENTER_AREA_DIVIDER_POSITIONS);
//        监听分割组件中的第一个子组件的分割大小,然后改变BottomInfoPane中的dataBaseInfoStatusBar的大小
        centerPane.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                log.debug("监听到centerPane第一个子组件的分隔比例发生变化,原值:[{}],现值:[{}]", oldValue.doubleValue(), newValue.doubleValue());
//                todo 需要注意的是设置左侧StatusBar的宽度,要放在Platform.runLater()中执行,否则当宽度突然发生变化,StatusBar的宽度可能不会发生变化,或需要其他事件才能发生变化 见 类TopButtonComponent
                Platform.runLater(() -> {bottomInfoPane.setDataBaseInfoStatusBarWith(newValue.doubleValue());});
            }
        });

//        分割布局添加子项
        centerPane.getItems().addAll(dataBasePane, contentPane);
        rootPane.setCenter(centerPane);
        return scene;
    }
}
