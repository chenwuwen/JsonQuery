package com.kanyun.ui.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.controlsfx.control.StatusBar;

public interface TabKind {

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
     */
    void addStatusBarEventListener();

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
