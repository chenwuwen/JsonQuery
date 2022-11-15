package com.kanyun.ui.layout;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class BottomInfoPane extends FlowPane {

    private String recordCount;

    private String sql;

    private Label label;

    public BottomInfoPane() {
        setHeight(30);
        setAlignment(Pos.CENTER_LEFT);
        label = new Label();
        label.setText("qqqqqqqqqqq");
        getChildren().add(label);
    }
}
