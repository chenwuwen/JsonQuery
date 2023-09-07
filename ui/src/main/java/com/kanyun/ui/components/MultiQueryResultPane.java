package com.kanyun.ui.components;

import com.kanyun.ui.tabs.TabKind;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 多查询结果Tab
 */
public class MultiQueryResultPane extends TabPane {

    private static final Logger log = LoggerFactory.getLogger(MultiQueryResultPane.class);

    /**
     * 查询信息集合
     * key:sql
     * value:查询耗时等信息
     */
    private Map<String, Map<String, Object>> queryInfoCollection;

    /**
     * 执行总耗时
     */
    private String totalCost;

    /**
     * 动态信息栏组件 Node
     */
    StatusBar dynamicInfoStatusBar;


    /**
     * 查询结果集合
     * key: sql
     * value:元组类型,left:字段及对应的类型信息,right:查询出来的数据
     */
    private Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>> queryResultCollection;

    public void setContent(Map<String, Map<String, Object>> queryInfoCollection,
                           Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>> queryResultCollection,
                           StatusBar statusBar, String totalCost) {
        getTabs().clear();
        this.queryInfoCollection = queryInfoCollection;
        this.queryResultCollection = queryResultCollection;
        this.dynamicInfoStatusBar = statusBar;
        this.totalCost = totalCost;
        addMessageTab();
        addResultTabs();
        addTabSwitchListener();
//        如果当前Tab页数量大于1,则选中第一个结果集Tab,否则选择信息Tab页
        if (getTabs().size() > 1) {
            getSelectionModel().select(1);
        } else {
            getSelectionModel().select(0);
        }
    }


    public MultiQueryResultPane() {
    }


    /**
     * 监听Tab页切换,主要用于设置动态信息栏
     */
    private void addTabSwitchListener() {

        getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                clearStatusBar();
                if (newValue == null) return;
                ObservableMap<Object, Object> properties = newValue.getProperties();
                if (properties == null || properties.isEmpty()) {
//                    说明切换到了信息的标签页
                    Label costLabel = TabKind.createCommonLabel("总耗时:" + totalCost + "秒", dynamicInfoStatusBar, null, Color.GREEN);
                    dynamicInfoStatusBar.getRightItems().add(0, costLabel);
                    dynamicInfoStatusBar.setText("执行SQL数:" + queryInfoCollection.size() + "条");
                } else {
                    String cost = "查询耗时：" + TabKind.getSecondForMilliSecond(properties.get("cost")) + "秒";
                    String record = "总记录数：" + properties.get("count");
                    Label costLabel = TabKind.createCommonLabel(cost, dynamicInfoStatusBar, null, Color.GREEN);
                    costLabel.setPrefHeight(dynamicInfoStatusBar.getHeight());
                    Label recordLabel = TabKind.createCommonLabel(record, dynamicInfoStatusBar, null, Color.GREEN);
                    recordLabel.setPrefHeight(dynamicInfoStatusBar.getHeight());
//                   注意这里如果是set(index,node),那么如果指定索引处没有Node将会报错
                    dynamicInfoStatusBar.getRightItems().add(0, new Separator(Orientation.VERTICAL));
                    dynamicInfoStatusBar.getRightItems().add(1, costLabel);
                    dynamicInfoStatusBar.getRightItems().add(2, new Separator(Orientation.VERTICAL));
                    dynamicInfoStatusBar.getRightItems().add(3, recordLabel);
                    String sql = String.valueOf(properties.get("sql"));
                    sql = sql.replaceAll("\\s+", " ");
                    dynamicInfoStatusBar.setText(sql);
                }

            }
        });
    }

    private void setTabStyle() {
        setStyle("");
    }

    /**
     * 添加查询结果Tab
     */
    public void addResultTabs() {
        int i = 1;
        for (Map.Entry<String, Pair<Map<String, Integer>, List<Map<String, Object>>>> queryResultEntity : queryResultCollection.entrySet()) {
            Tab tab = new Tab("结果 " + i + " ");
            tab.setClosable(false);
            log.debug("添加SQL:{}的结果集到,查询结果集合Tab中", queryResultEntity.getKey());
            TableViewPane tableViewPane = new TableViewPane();
            Pair<Map<String, Integer>, List<Map<String, Object>>> result = queryResultEntity.getValue();
            buildQueryResult(tableViewPane, result);
            tab.setContent(tableViewPane);
//            将SQL的查询信息,设置到Tab的属性中
            tab.getProperties().putAll(queryInfoCollection.get(queryResultEntity.getKey()));
            getTabs().add(tab);
            i += 1;
        }
    }


    /**
     * 构建查询结果TableViewPane
     *
     * @param tableViewPane
     * @param result
     */
    private void buildQueryResult(TableViewPane tableViewPane, Pair<Map<String, Integer>, List<Map<String, Object>>> result) {
//        得到结果字段信息(字段名和字段类型)
        Map<String, Integer> columnInfos = result.getLeft();
        tableViewPane.setTableColumns(columnInfos);
        tableViewPane.setTableRows(FXCollections.observableList(result.getRight()));
    }

    /**
     * 构建信息Tab页
     */
    private void addMessageTab() {
        Tab messageTab = new Tab("信息");
        messageTab.setClosable(false);
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            return;
        });

        textArea.setStyle("-fx-font-size: 14;-fx-border-width: 0;" +
                "-fx-background-color: transparent;-fx-padding: 0 0 0 0;" +
                "-fx-focus-color: transparent;-fx-faint-focus-color: transparent;" +
                "-fx-focus-border-width: 0;");
        for (Map.Entry<String, Map<String, Object>> queryInfoEntity : queryInfoCollection.entrySet()) {
            String sql = queryInfoEntity.getKey();
            Map<String, Object> queryInfo = queryInfoEntity.getValue();
            String cost = ">  查询耗时:" + TabKind.getSecondForMilliSecond(queryInfo.get("cost")) + "s";
            String count = ">  查询总数:" + queryInfo.get("count");
            String content = sql + "\n" + cost + "\n" + count + "\n";
            textArea.appendText(content);
//            添加换行
            textArea.appendText("\n\n");
        }
        messageTab.setContent(textArea);
        getTabs().add(messageTab);
    }

    /**
     * 清除所有的Tab
     */
    public void clearTab() {
        getTabs().clear();
    }

    /**
     * 清除StatusBar上的信息
     */
    private void clearStatusBar() {
//        注意:如果Node的property已经被绑定,那么执行dynamicInfoStatusBar.setText("")将报错:A bound value cannot be set
//        同时执行dynamicInfoStatusBar.textProperty().set("");也无法修改值,只能解绑或不进行绑定
        dynamicInfoStatusBar.setText("");
        dynamicInfoStatusBar.getRightItems().clear();
        dynamicInfoStatusBar.getLeftItems().clear();
    }
}
