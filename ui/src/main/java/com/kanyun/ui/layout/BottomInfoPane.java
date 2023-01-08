package com.kanyun.ui.layout;

import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.model.DataBaseModel;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.TaskProgressView;
import org.controlsfx.glyphfont.Glyph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 下方信息条组件
 */
public class BottomInfoPane extends HBox {

    private static final Logger log = LoggerFactory.getLogger(DataBasePane.class);

    /**
     * 数据库信息属性
     */
    private SimpleStringProperty dataBaseInfoProperty = new SimpleStringProperty();
    /**
     * 动态信息属性
     */
    private SimpleStringProperty dynamicInfoProperty = new SimpleStringProperty();

    private SimpleDoubleProperty posProperty = new SimpleDoubleProperty();
    private SimpleDoubleProperty parentWidthProperty = new SimpleDoubleProperty();

    /**
     * 线程池异步任务
     */
    private ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 进度条任务(仅做展示使用)
     */
    private Task<Void> progressTask;
    /**
     * DataBase侧状态组件
     */
    private StatusBar dataBaseInfoStatusBar;
    /**
     * 内容区动态信息组件
     */
    private StatusBar dynamicInfoStatusBar;

    public BottomInfoPane() {
        setId("BottomInfoPane");
        setPrefHeight(30);
        setAlignment(Pos.CENTER_LEFT);
//        设置节点之间的间距
        setSpacing(0);
        getChildren().addAll(createDataBaseInfo(), createDynamicInfo());
//      Hgrow是 horizontal grow缩写意为水平增长，在这里是水平增长沾满窗口
//        HBox.setHgrow(dataBaseInfoStatusBar, Priority.ALWAYS);
//        这里只设置一个组件为动态增长,另一个组件则手动设置值(通过监听器),如果设置两个组件都水平增长,则单独给组件设置宽度值是没有效果的
        HBox.setHgrow(dynamicInfoStatusBar, Priority.ALWAYS);
        widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                log.debug("监测到BottomInfoPane组件宽度发生变化,旧值:[{}],新值:[{}],将动态改变dataBaseInfoStatusBar的宽度", oldValue, newValue);
//                这里不再设置dataBaseInfoStatusBar的宽度,因为默认的分隔比例可能已经失效,此处只记录新值
//                setDataBaseInfoStatusBarWith(newValue.doubleValue(), 0.2);
                parentWidthProperty.set(newValue.doubleValue());
            }
        });

    }

    /**
     * 左侧数据库连接信息条(宽度跟随SplitPane设置的比例)
     *
     * @return
     */
    public StatusBar createDataBaseInfo() {
        dataBaseInfoStatusBar = new StatusBar();
//        不设置的话,默认有个OK字样
        dataBaseInfoStatusBar.setText("没有数据库");
        dataBaseInfoStatusBar.textProperty().bind(dataBaseInfoProperty);
        dataBaseInfoProperty.set(JsonQuery.dataBaseModels.size() + "个数据库");
        addEventHandler(UserEvent.DATABASE_MODIFY, event -> {
            dataBaseInfoProperty.set(JsonQuery.dataBaseModels.size() + "个数据库");

        });
        addEventHandler(UserEvent.ITEMS_COUNT, event -> {
            dataBaseInfoProperty.set(JsonQuery.dataBaseModels.size() + "被选中");
        });

        addEventHandler(UserEvent.CURRENT_DATABASE, event -> {
            DataBaseModel dataBaseModel = event.getDataBaseModel();

            ImageView imageView = new ImageView("/asserts/database.png");
            imageView.setFitWidth(dataBaseInfoStatusBar.getPrefHeight() / 2);
            imageView.setFitHeight(dataBaseInfoStatusBar.getPrefHeight() / 2);
            Label currentDbLabel = createCommonLabel(dataBaseModel.getName(), dataBaseInfoStatusBar, Color.ORANGE, null);
            if (dataBaseInfoStatusBar.getRightItems().size() < 1) {
//                添加分隔线
                dataBaseInfoStatusBar.getRightItems().add(new Separator(Orientation.VERTICAL));
//               当前选中的数据库
                dataBaseInfoStatusBar.getRightItems().add(1, currentDbLabel);
            } else {
                dataBaseInfoStatusBar.getRightItems().set(1, currentDbLabel);
            }

            ModelJson.getModelJson(dataBaseModel.getName());
        });
        return dataBaseInfoStatusBar;
    }

    /**
     * 右侧动态信息条(自动占满横向剩余空间)
     *
     * @return
     */
    public StatusBar createDynamicInfo() {
        dynamicInfoStatusBar = new StatusBar();
//        不设置的话,默认有个OK字样
        dynamicInfoStatusBar.setText("");
        dynamicInfoStatusBar.textProperty().bind(dynamicInfoProperty);
        addEventHandler(UserEvent.EXECUTE_SQL, event -> {
            log.warn("88999");
//            去掉SQL中的换行符
            String sql = event.getSql().replaceAll("\r|\n|\t", "");
            log.debug("设置动态SQL信息:[{}]", sql);
            dynamicInfoProperty.set(sql);
//            开启进度条
            startSqlExecuteProgress();
        });

        addEventHandler(UserEvent.EXECUTE_SQL_COMPLETE, event -> {
            log.debug("接收到SQL执行完成事件,准备停止进度条,并设置查询记录数及查询耗时");
            dynamicInfoStatusBar.getRightItems().removeAll(dynamicInfoStatusBar.getRightItems());
            Map<String, Object> queryInfo = event.getQueryInfo();
            String cost = "查询耗时：" + getSecondForMilliSecond(queryInfo.get("cost")) + "秒";
            String record = "总记录数：" + queryInfo.get("count");
            Label costLabel = createCommonLabel(cost, dynamicInfoStatusBar, null, Color.GREEN);
            costLabel.setPrefHeight(dynamicInfoStatusBar.getHeight());
            Label recordLabel = createCommonLabel(record, dynamicInfoStatusBar, null, Color.GREEN);
            recordLabel.setPrefHeight(dynamicInfoStatusBar.getHeight());
//            注意这里如果是set(index,node),那么如果指定索引处没有Node将会报错
            dynamicInfoStatusBar.getRightItems().add(0, new Separator(Orientation.VERTICAL));
            dynamicInfoStatusBar.getRightItems().add(1, costLabel);
            dynamicInfoStatusBar.getRightItems().add(2, new Separator(Orientation.VERTICAL));
            dynamicInfoStatusBar.getRightItems().add(3, recordLabel);
            boolean cancel = progressTask.cancel();
            dynamicInfoStatusBar.progressProperty().unbind();
        });
        return dynamicInfoStatusBar;
    }

    /**
     * 动态设置dataBaseInfoStatusBar的宽度
     *
     * @param pos        比例
     * @param parentWith 父级宽度
     */
    public void setDataBaseInfoStatusBarWith(Double parentWith, Double pos) {
        dataBaseInfoStatusBar.setPrefWidth(parentWidthProperty.get() * pos);
    }

    public void startSqlExecuteProgress() {
        progressTask = new Task() {
            @Override
            protected Object call() throws Exception {

                while (true) {
                    if (isCancelled()) {
                        updateProgress(0,0);
                        break;
                    }
                }
//                更新任务进度方法
//                updateProgress();
//                更新提示文字用于属性绑定:dynamicInfoStatusBar.textProperty().bind(task.messageProperty());
//                updateMessage();
                done();

                return null;
            }
        };
//        当StatusBar进度属性绑定到task的任务属性时,StatusBar就展示了进度条
        dynamicInfoStatusBar.progressProperty().bind(progressTask.progressProperty());
        executorService.execute(progressTask);

    }

    /**
     * 毫秒转化为秒,保留两位小数
     *
     * @param time
     * @return
     */
    public static String getSecondForMilliSecond(Object time) {
        float milliSecond = Float.valueOf(String.valueOf(time));
        float second = milliSecond / 1000;
        return String.format("%.2f", second);
    }

    /**
     * 创建公共Label
     *
     * @param text
     * @param parent
     * @param backGroundColor
     * @param textColor
     * @return
     */
    public Label createCommonLabel(String text, Control parent, Color backGroundColor, Color textColor) {
        Label commonLabel = new Label(text);
        if (textColor != null) {
//            设置Label字体颜色
            commonLabel.setTextFill(textColor);
        }
//        设置Label的内边距
        commonLabel.setPadding(new Insets(0, 8, 0, 8));
//        设置Label中的文字垂直居中
        commonLabel.setAlignment(Pos.CENTER);
//        设置Label的高度与父组件高度一致
        commonLabel.setPrefHeight(parent.getHeight());
        if (backGroundColor != null) {
//            设置Label背景色
            commonLabel.setBackground(new Background(new BackgroundFill(backGroundColor,
                    new CornerRadii(2), new Insets(2))));
        }
        return commonLabel;
    }

}
