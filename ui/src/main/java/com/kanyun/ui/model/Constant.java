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
}
