package com.kanyun.ui.layout;

import com.google.common.io.Files;
import com.jfoenix.controls.JFXTreeView;
import com.kanyun.ui.model.DataBase;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.UserEvent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 数据库列表组件
 */
public class DataBasePane extends VBox {

    public DataBasePane() {
        setAlignment(Pos.TOP_LEFT);
//        setPadding(new Insets(0,0,0,0));
        ListView<TreeView> dataBases = new ListView<>();
//        在调整父节点高度时,子节点高度垂直增长.不设置此项,在没有其他子组件的情况下,listView无法占满Vbox的高度
        VBox.setVgrow(dataBases, Priority.ALWAYS);
//        空白页显示内容
        dataBases.setPlaceholder(new Label("没有数据,请添加数据库"));

//        dataBases.setPrefHeight(getPrefHeight());
        setDataBaseTree(dataBases);
        getChildren().addAll(dataBases);

    }

    /**
     * 设置数据库树结构
     * @param listView
     */
    public void setDataBaseTree(ListView<TreeView> listView) {
        ObservableList<DataBase> dataBases = JsonQuery.dataBases;
        for (DataBase dataBase : dataBases) {
            JFXTreeView<String> dataBaseTreeView = new JFXTreeView();
            dataBaseTreeView.setId(dataBase.getName());
//            节点选中改变监听
            dataBaseTreeView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    System.out.println("TreeItem 选中改变,由于存在多个TreeView 因此不适用");
                }
            });
//            设置鼠标点击
            dataBaseTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() == 2) {
                        TreeItem<String> selectedItem = dataBaseTreeView.getSelectionModel().getSelectedItem();
                        if (selectedItem.isLeaf()) {
//                            是叶子节点说明是表
                            System.out.println("双击了表:" + selectedItem.getValue());
                            System.out.println("发送查询表事件,接收事件方是ContentPane");
                            UserEvent userEvent = new UserEvent(UserEvent.QUERY_TABLE);
                            userEvent.setTabName(selectedItem.getValue());
//                            通过Scene然后查找指定Id的Node对象,也就是EventTarget,如果要接收响应的对象在同一个类,则不需要EventTarget
                            EventTarget contentPane = getScene().lookup("#ContentPane");
                            Event.fireEvent(contentPane, userEvent);
                        } else {
                            System.out.println("双击了数据库:" + selectedItem.getValue());
                            System.out.println("发送查询表事件,接收事件方是ContentPane");
                            UserEvent userEvent = new UserEvent(UserEvent.QUERY_DATABASE);
                            userEvent.setDataBaseName(selectedItem.getValue());
//                            通过Scene然后查找指定Id的Node对象,也就是EventTarget,如果要接收响应的对象在同一个类,则不需要EventTarget
                            EventTarget contentPane = getScene().lookup("#ContentPane");
                            Event.fireEvent(contentPane, userEvent);
                        }


                    }
                }
            });

            dataBaseTreeView.setPadding(new Insets(0, 0, 0, 0));

            TreeItem<String> dataBaseRootTreeItem = new TreeItem<>(dataBase.getName());
//            根节点默认不展开
            dataBaseRootTreeItem.setExpanded(false);
//            设置根节点
            dataBaseTreeView.setRoot(dataBaseRootTreeItem);
            Collection<File> tables = getDataBaseTable(dataBase.getUrl());
            List<String> tableNames = new ArrayList<>();
            for (File table : tables) {
                String tableName = Files.getNameWithoutExtension(table.getName());
                TreeItem<String> tableItem = new TreeItem<>(tableName);
                dataBaseRootTreeItem.getChildren().add(tableItem);
                tableNames.add(tableName);
            }
            dataBase.setTables(FXCollections.observableList(tableNames));
            listView.getItems().add(dataBaseTreeView);
        }
    }

    /**
     * 得到数据库下的表
     *
     * @param parentPath
     * @return
     */
    public Collection<File> getDataBaseTable(String parentPath) {
        File dir = new File(parentPath);
        IOFileFilter ioFileFilter = FileFilterUtils.suffixFileFilter(".json");
        Collection<File> files = FileUtils.listFiles(dir, ioFileFilter, null);
        return files;
    }
}
