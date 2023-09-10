package com.kanyun.ui.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.controlsfx.control.StatusBar;

/**
 * Tab种类接口
 * 当添加新的Tab类型时,需要实现该接口
 *
 * 创建新子类步骤
 * 1.子类添加 成员变量  StatusBar dynamicInfoStatusBar;
 * 2.子类构造方法调用createDynamicInfoStatusBar()方法,并实例化 StatusBar
 * 3.子类实现的getDynamicInfoStatusBar() 方法返回实例化好的StatusBar
 * 4.子类实现addStatusBarEventListener() 方法
 * 5.子类实现getTabKind()方法,返回类型{@link TabKindEnum}
 * 6.子类构造方法,调用接口的默认方法 statusBarInit() 方法
 *
 *
 */
public interface TabKind {

    /**
     * tab 图标尺寸
     */
    Double TAB_GRAPHIC_SIZE = 18.0;

    /**
     * 获取Tab页类型枚举
     *
     * @return
     */
    TabKindEnum getTabKind();

    /**
     * 创建动态信息栏
     * @return
     */
    void createDynamicInfoStatusBar();

    /**
     * 得到动态信息栏
     * @return
     */
    StatusBar getDynamicInfoStatusBar();


    /**
     * 添加StatusBar事件监听器
     * 监听相应的事件,来更改statusBar的显示内容
     */
    void addStatusBarEventListener();

    /**
     * 获取Tab图标
     * @return
     */
    Node getTabGraphic();


    /**
     * 动态信息栏初始化
     */
    default void statusBarInit() {
        createDynamicInfoStatusBar();
        addStatusBarEventListener();
    }

    /**
     * 毫秒转化为秒,保留两位小数
     *
     * @param time
     * @return
     */
    static String getSecondForMilliSecond(Object time) {
        float milliSecond = Float.valueOf(String.valueOf(time));
        float second = milliSecond / 1000;
        return String.format("%.2f", second);
    }

    /**
     * 创建公共Label
     *
     * @param text
     * @param parent
     * @param backGroundColor
     * @param textColor
     * @return
     */
    static Label createCommonLabel(String text, Control parent, Color backGroundColor, Color textColor) {
        Label commonLabel = new Label(text);
        if (textColor != null) {
//            设置Label字体颜色
            commonLabel.setTextFill(textColor);
        }
//        设置Label的内边距
        commonLabel.setPadding(new Insets(0, 8, 0, 8));
//        设置Label中的文字垂直居中
        commonLabel.setAlignment(Pos.CENTER);
//        设置Label的高度与父组件高度一致
        commonLabel.setPrefHeight(parent.getHeight());
        if (backGroundColor != null) {
//            设置Label背景色
            commonLabel.setBackground(new Background(new BackgroundFill(backGroundColor,
                    new CornerRadii(2), new Insets(2))));
        }
        return commonLabel;
    }
}
