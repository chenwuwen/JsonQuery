package com.kanyun.ui.splash;

import com.kanyun.ui.model.Constant;
import javafx.animation.Animation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


/**
 * 欢迎页面过渡场景
 * 这里是svg图像居中的办法是：由于该类是StackPane的子类,因此此类中的子元素都是自动居中的
 * 1:根据svg图像的尺寸及需要放大的比例,计算画布的尺寸
 * 2:创建画布Canvas
 * 3:利用路径动画(PathTransition)和画笔(GraphicsContext)画图
 * 因此只需要保证svg在画布中是居中的,就可以保证svg图像在Scene中是居中的
 * 保证svg在画布中是居中的关键要素是:保证画布的大小与计算后的svg的大小是一致的,需要注意的是这里的
 * 计算维度有很多svg的缩放比例、svg的X/Y轴偏移量、svg在X/Y轴的分布数量,需要准确计算好
 */
public class SplashPane extends StackPane {

    private static final Logger log = LoggerFactory.getLogger(SplashPane.class);


    private static final double SCENE_WIDTH = 800;
    private static final double SCENE_HEIGHT = 400;
    /**
     * 动画列表
     */
    private static final List<Animation> animationList = new ArrayList<>();
    /**
     * 动画执行时间
     */
    private static final double ANIMATION_SUSTAIN_TIME = 5;

    /**
     * Svg图像位置
     */
    private static String svg_img_path = "classpath:/logo.svg";


    public SplashPane() {
        setPrefWidth(SCENE_WIDTH);
        setPrefHeight(SCENE_HEIGHT);
        svg_img_path = getClass().getClassLoader().getResource("logo.svg").getPath();
        List<String> paths = SvgAnalysisHelper.getSvgPath(svg_img_path);

        Pair<Double, Double> svgSize = SvgAnalysisHelper.getSvgSize(svg_img_path);
        initAnimation(paths, svgSize);
//        有几个动画就设置几个
        Constant.SCENE_SWITCH_FLAG = new CountDownLatch(animationList.size());
    }

    /**
     * 初始化动画
     *
     * @param paths
     * @param svgSize
     */
    private void initAnimation(List<String> paths, Pair<Double, Double> svgSize) {
//        svg图像在X/Y轴方向的缩放比例
        double svgScaleX = 2;
        double svgScaleY = 3;
//        初始不设置画布的大小,当计算好svg缩放后尺寸后再设置画布的大小
        Canvas canvas = new Canvas();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.GRAY);

//        计算svg图在X/Y轴上的平移距离,以保证svg在画布中处于居中位置
        double svgTranslateX = calculateSvgTranslateX(svgSize, svgScaleX, paths.size());
        double canvasHeight = calculateCanvasHeight(svgSize, svgScaleY);
        double canvasWidth = calculateCanvasWidth(svgSize, svgScaleX, paths.size(), svgTranslateX);
        log.debug("计算得到画布的尺寸:[{},{}]", canvasWidth, canvasHeight);
        canvas.setHeight(canvasHeight);
        canvas.setWidth(canvasWidth + 20);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        log.debug("计算得出SVG在Y轴偏移量:{}", svgTranslateX);
        SplashAnimation splashAnimation = new SplashAnimation();
        getChildren().add(canvas);
        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(path);
//            svg图像设置缩放
            svgPath.setScaleX(svgScaleX);
            svgPath.setScaleY(svgScaleY);
//            svg图像放大后会重叠住,因此设置每个path在X轴上的偏移方向(这里的加1也是针对第一个svg图像做偏移以避免最左侧像素看不到)
            svgPath.setTranslateX(svgTranslateX * (i + 1));
            svgPath.setTranslateY(10);
            Circle trackBall = new Circle(2);
            trackBall.setFill(Color.RED);
//            动画持续时间
            Duration seconds = Duration.seconds(ANIMATION_SUSTAIN_TIME);
            Animation pathAnimation = splashAnimation.createPathAnimation(canvas, seconds, svgPath, trackBall);
//            pathAnimation.setOnFinished(event -> {
//
//            });
            animationList.add(pathAnimation);
        }
    }

    /**
     * 播放动画,虽然是循环播放,但由于是播放动画是异步
     * 所以看起来是同步播放
     */
    public void playAnimation() {
        if (animationList.size() != 0) {
            for (Animation animation : animationList) {
                animation.play();
            }
        }
    }


    /**
     * 根据Svg图形的高度及缩放比例来计算画布的高度
     *
     * @param svgSize
     * @param svgScaleY
     * @return
     */
    private double calculateCanvasHeight(Pair<Double, Double> svgSize, Double svgScaleY) {
        double svgHeight = svgSize.getRight();
        if (svgHeight > SCENE_HEIGHT || svgHeight * svgScaleY > SCENE_HEIGHT) {
            return SCENE_HEIGHT;
        }
        return svgHeight * svgScaleY;
    }

    /**
     * 计算canva的宽度
     *
     * @param svgSize
     * @param svgScaleX
     * @param svgNumber
     * @param svgTranslateX
     * @return
     */
    private double calculateCanvasWidth(Pair<Double, Double> svgSize, Double svgScaleX, Integer svgNumber, double svgTranslateX) {
//        svg图像的总宽度
        double svgWidth = svgSize.getLeft();
        if (svgWidth > SCENE_WIDTH) {
            return SCENE_WIDTH;
        }
        if (svgWidth * svgScaleX > SCENE_WIDTH) {
//            当svg图像缩放后大于场景的宽度,则画布的宽度也是视窗的宽度
            return SCENE_WIDTH;
        }
//        当svg图像缩放后仍小于场景的宽度
        return svgWidth * svgScaleX + svgTranslateX;
    }

    /**
     * 算svg图像在画布中的水平方向偏移距离,以保证在svg图像水平居中,注意:水平方向存在多个svg图像
     * 需要注意由于X轴上存在多个svg图像,因此在设置了svg的X轴缩放时,相连的svg图像会重叠在一起(X轴左右共同作用缩放)
     * 因此当设置了缩放svg图像的倍数后如果整体svg尺寸大于了画布,则还是做一定量的偏移用来避免svg图像挤在一起
     *
     * @param svgSize
     * @param svgScaleX x轴方向缩放比例
     * @param svgNumber svg图像数量
     * @return
     */
    private double calculateSvgTranslateX(Pair<Double, Double> svgSize, Double svgScaleX, Integer svgNumber) {
//        svg图像的总宽度
        double svgWidth = svgSize.getLeft();
//        单个svg图像的宽度
        double singleSvgWith = svgWidth / svgNumber;
        if (svgScaleX == 1) {
            return 0;
        }
        return singleSvgWith * (svgScaleX - 1);
    }
}
