package com.kanyun.ui.tabs;

import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;

public abstract class AbstractTab extends VBox implements TabKind {

    /**
     * 实例化工具条
     */
    protected ToolBar toolBar = new ToolBar();

    /**
     * 子类构造方法执行时,先调用父类的构造方法,需要注意的是此时子类的成员变量还未初始化
     * 因此 {@link TabKind#statusBarInit()} 子类的实现,要注意成员变量初始化的问题
     * 这里添加构造方法是因为每个子类都需要初始化动态信息栏,为了避免某个子类忘记初始化,或代码冗余
     * 因此创建抽象基类,使子类实例化时先调用基类构造方法,初始化动态信息栏
     */
    public AbstractTab() {
//        保证子类在实例化时,先执行动态信息栏的初始化
        statusBarInit();
    }


    @Override
    public void onCreated() {
        initToolBar();
    }


    /**
     * 初始化工具栏
     */
    abstract void initToolBar();
}
