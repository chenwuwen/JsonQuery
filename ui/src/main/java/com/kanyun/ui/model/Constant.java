package com.kanyun.ui.model;

import java.util.concurrent.CountDownLatch;

public class Constant {

    /**
     * 应用名
     */
    public static final String APP_NAME = "JsonQuery";

    /**
     * 场景是否切换标致
     * 多线程同步计数器
     */
    public static CountDownLatch SCENE_SWITCH_FLAG;

    /**
     * jar文件类型函数的包的分隔符
     */
    public static final String FUNC_JAR_FILE_SEPARATOR = ";";

    /**
     * 左侧数据库列表树区域的最大宽度
     * 同时作用于底部信息栏的分割线的最大位置
     */
    public static final Double DATABASE_TREE_PANE_MAX_WIDTH = 300d;
}
