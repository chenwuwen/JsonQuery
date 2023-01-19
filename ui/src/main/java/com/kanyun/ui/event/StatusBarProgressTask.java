package com.kanyun.ui.event;

import com.kanyun.ui.tabs.TabQueryPane;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义Task任务,主要用来展示Statusbar上的进度条,本身并无进度作用
 * 只是起到一个任务进行中的作用,类似于loading,StatusBar的progressProperty
 * 绑定到了Task的progressProperty上,因此只有Task的progressProperty为完成
 * StatusBar才认为是完成,而非执行Task的done()方法
 */
public class StatusBarProgressTask extends Task<Void> {
    private static final Logger log = LoggerFactory.getLogger(TabQueryPane.class);


    @Override
    protected Void call() throws Exception {
        while (!isCancelled()) {
            log.trace("进度条任务执行中.......");
        }
//        更新任务进度方法
//        updateProgress();
//        更新提示文字用于属性绑定:
//        dynamicInfoStatusBar.textProperty().bind(task.messageProperty());
//        updateMessage();
        log.trace("进度条任务执行完毕.......");
        return null;
    }

    /**
     * 设置进度完成,并结束任务
     */
    public void stopProgress() {
//        只有更新Progress为完成,进度条才消失
        updateProgress(0, 0);
//        执行该方法,call()方法将返回,该Task任务结束
        cancel();
    }

}
