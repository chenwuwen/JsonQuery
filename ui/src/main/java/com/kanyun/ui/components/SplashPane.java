package com.kanyun.ui.components;

import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;
import javafx.animation.PathTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;


/**
 * 欢迎页面过度场景
 */
public class SplashPane extends StackPane {

    private Path path;
    private Rectangle rectangle;

    public SplashPane() {
        setPrefWidth(400);
        setPrefHeight(300);
        rectangle = new Rectangle(200,200,50, 50);
        rectangle.setFill(Color.GOLD);
        Path path = initAnimationPath();
        getChildren().addAll(path,rectangle);

    }

    public Path initAnimationPath() {
      


//        SVGPath svgPath = new SVGPath();
//        svgPath.
        path = new Path();
        MoveTo start = new MoveTo(0, 0);
        LineTo line1 = new LineTo(350, 150);
        LineTo line2 = new LineTo(300, 300);
        LineTo line3 = new LineTo(250, 150);
        LineTo line4 = new LineTo(200, 300);
        LineTo line5 = new LineTo(150, 150);
        LineTo line6 = new LineTo(100, 300);
//        path.getElements().addAll(start, line1, line2, line3, line4, line5, line6);
        path.getElements().add(start);
        path.getElements().addAll( line1);
        return path;
    }

    public void playAnimation() {
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(4000));
        pathTransition.setPath(path);
        pathTransition.setNode(rectangle);
        pathTransition.setCycleCount(1);
        pathTransition.play();
    }
}
