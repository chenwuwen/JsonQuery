package com.kanyun.ui.components.appstatusbar;

import javafx.scene.control.Skin;
import org.controlsfx.control.StatusBar;

/**
 * 自定义StatusBar,{@link org.controlsfx.control.StatusBar}
 * 由于 {@link  org.controlsfx.control.ControlsFXControl} 是不开放的,因此
 * 只能继承StatusBar,之所以自定义 StatusBar 是因为调用StatusBar.setText()方法时
 * 如果内容过长,导致内容占用右侧Item的空间,因此自定义AppStatusBar来解决内容过长时的显示问题
 * 解决思路是保持左/右侧Item空间不变,StatusBar的Label显示内容使用ScrollPane包装,使之可以滚动显示文字
 * 主要实现: {@link AppStatusBarSkin}
 */
public class AppStatusBar extends StatusBar {

    private String stylesheet;

    /**
     * Constructs a new status bar control.
     */
    public AppStatusBar() {
        getStyleClass().add("app-status-bar"); //$NON-NLS-1$
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AppStatusBarSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
//        加载用户自定义的样式文件(注意此样式文件的存放位置,在resources下的同package目录内,放在这个路径的好处是,打包后该css与class在同一位置)
        return getUserAgentStylesheet(AppStatusBar.class, "app-status-bar.css");
    }

}
