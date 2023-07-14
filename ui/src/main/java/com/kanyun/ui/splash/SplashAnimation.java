package com.kanyun.ui.splash;

import com.kanyun.ui.model.Constant;
import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 过场动画
 */
public class SplashAnimation {
    private static final Logger log = LoggerFactory.getLogger(SplashAnimation.class);

    /**
     * 创建路径动画
     *
     * @param canvas   画布
     * @param duration 动画持续时间
     * @param path     路径
     * @param trackBall      画笔(轨迹点)
     */
    public Animation createPathAnimation(Canvas canvas, Duration duration, Shape path, Shape trackBall) {
//        绘制图形的一个类,提供了各种绘制图形的函数和属性
        GraphicsContext gc = canvas.getGraphicsContext2D();
        PathTransition pathTransition = new PathTransition(duration, path, trackBall);
        pathTransition.setOnFinished(event -> {
            log.warn("=====过场动画执行结束=====");
//            多线程同步锁计数器-1
            Constant.SCENE_SWITCH_FLAG.countDown();
//            Pane root = (Pane) canvas.getParent();
//            root.getChildren().remove(trackBall);
        });
//        路径动画添加监听器
        pathTransition.currentTimeProperty().addListener(new ChangeListener<Duration>() {

            Location oldLocation;

            /**
             * Draw a line from the old location to the new location
             */
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
//                log.debug("上一个时间:" + oldValue + "当前时间:" + newValue);
//                 跳过开始的时间段
                if (oldValue == Duration.ZERO) return;

//                 得到当前轨迹球的位置(偏移量)
                double x = trackBall.getTranslateX();
                double y = trackBall.getTranslateY();

//                 如果旧的位置为空,则初始化旧的位置
                if (oldLocation == null) {
                    oldLocation = new Location();
                    oldLocation.x = x;
                    oldLocation.y = y;
                    return;
                }
//                画线
                gc.setStroke(Color.BLUE);
                gc.setFill(Color.YELLOW);
                gc.setLineWidth(1);
                gc.fill();
                gc.strokeLine(oldLocation.x, oldLocation.y, x, y);
//                更新旧位置为轨迹球当前位置
                oldLocation.x = x;
                oldLocation.y = y;
            }
        });
        return pathTransition;
    }


    /**
     * 自定义位置类
     */
    public static class Location {
        double x;
        double y;
    }
}
