package com.kanyun.ui;

import com.jfoenix.assets.JFoenixResources;
import com.jfoenix.svg.SVGGlyphLoader;
import com.kanyun.ui.model.Constant;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 异步初始化
 * 1:初始化应用配置
 * 2:初始化主场景布局
 * 3:切换场景(关闭过渡场景创建并显示总场景)
 */
public class AsyncInitializer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(AsyncInitializer.class);

    /**
     * 过渡场景(旧场景)
     */
    private final Stage splashStage;

    public AsyncInitializer(Stage splashStage) {
        this.splashStage = splashStage;
    }

    @Override
    public void run() {
        JsonQuery jsonQuery = new JsonQuery();
        try {
//            初始化应用配置
            jsonQuery.initConfig();
            Scene mainScene = InterfaceInitializer.initializeMainScene();
            loadAllGlyphsFont();
//            阻塞,需要等待动画播放完毕再切换场景
            Constant.SCENE_SWITCH_FLAG.await();
            log.info("过渡页动画播放完毕,准备切换到主场景");
//            由于当前线程是异步线程因此切换场景会报错,此时需要使用Platform.runLater()来更新UI,它会先将线程切换为Fx线程,并执行操作
//            因此如果需要在异步线程中更新UI,需要使用Platform.runLater()方法
            Platform.runLater(() -> switchScene(mainScene));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 切换Scene 这里切换Scene采用了创建新的Stage,关闭旧的Stage的方案
     * 这里注意需要使用Fx线程去更新UI,否则会报错
     * Platform.runLater()：如果需要从非GUI线程更新GUI组件，则可以使用它将更新放入队列中，并*尽快*由GUI线程处理
     * 因此使用Platform.runLater()可以进行一些简单的UI更新,如果复杂了容易出现无响应,页面卡顿
     * 同样可以实现异步功能的还有 javafx.concurrent.Service/javafx.concurrent.Task
     * 它们主要是异步执行耗时任务,然后同步监听状态并更新UI
     * https://blog.csdn.net/baidu_25117757/article/details/117381419
     *
     * @param mainScene 主Scene
     */
    private void switchScene(Scene mainScene) {
        log.info("当前切换场景的线程是否是Fx线程：{}", Platform.isFxApplicationThread());
//        切换场景需要注意：如果前一个场景跟当前场景使用同一个Stage时,后一个场景的左上角坐标会与第一个场景的左上角坐标重叠,这样当前后
//        两个场景的大小不一致时,会导致后一个场景的窗口不居中,此时可以使用Stage.centerOnScreen()方法使窗口居中,但是这样在切换场景
//        后会存在一个窗口移动的问题,同时也可以先将窗口隐藏(Stage.hide()),再设置居中,再设置窗口显示(Stage.show()),这样存在一个窗口
//        闪动的问题,解决方案是创建一个新窗口,在新窗口展示后,再将原来的窗口关闭,如果原来的窗口不关闭,则系统会出现两个窗口,需要注意的是
//        新窗口也要设置好标题等相关元素
//        将窗口重定位到屏幕中间位置
//        splashStage.centerOnScreen();
        Stage mainStage = new Stage();
        mainStage.setTitle(splashStage.getTitle());
        mainScene.getStylesheets().addAll("css/button.css", "css/components.css", "css/context-menu.css");
        mainScene.getStylesheets().add(JFoenixResources.load("css/jfoenix-design.css").toExternalForm());
        mainScene.getStylesheets().add(JFoenixResources.load("css/jfoenix-fonts.css").toExternalForm());
//        场景设置到窗口区域
        mainStage.setScene(mainScene);
        mainStage.show();
        splashStage.close();
        mainStage.setOnCloseRequest(request -> Platform.exit());
    }

    /**
     * 加载全部的SVG图形字体,同时初始化IconProperties,UI界面需要使用图标,从IconProperties中获取图标名称{@link IconProperties}
     * 使用JFoenix SVG Loader 加载字体,使用SVGGlyph前需要先加载字体,有提供加载的多个方法,
     * 主要作用是向SVGGlyphLoader类的glyphsMap变量赋值
     * svg 下载网站 (https://icomoon.io/app/#/select) account: rawzez@mvcgd.com/icomoon.io
     * 导出方式: 生成字体(选择svg)
     */
    private void loadAllGlyphsFont() throws IOException {
//       参数1:svg字体输入流
//       参数2:图标名称前缀(当加载多个字体,且多个字体存在一个同名的图标,因此添加前缀加以区分,取字体时使用SVGGlyphLoader.getGlyph("前缀"+"名称"))
        SVGGlyphLoader.loadGlyphsFont(this.getClass().getResourceAsStream("/fonts/icomoon.svg"),
                IconProperties.getIconNamePrefix());
        log.debug("全部字体加载完毕:{}", SVGGlyphLoader.getAllGlyphsIDs());
    }
}
