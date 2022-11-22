package com.kanyun.ui.layout;

import com.google.common.io.Files;
import com.jfoenix.controls.JFXTreeView;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.model.DataBaseModel;
import com.kanyun.ui.model.TableModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 数据库列表组件
 */
public class DataBasePane extends VBox {

    private static final Logger log = LoggerFactory.getLogger(DataBasePane.class);

    /**
     * 数据库列表
     */
    private ListView<TreeView> dataBases;

    public DataBasePane() {
        setId("DataBasePane");
        setAlignment(Pos.TOP_LEFT);
//        setPadding(new Insets(0,0,0,0));
        dataBases = new ListView<>();
//        在调整父节点高度时,子节点高度垂直增长.不设置此项,在没有其他子组件的情况下,listView无法占满Vbox的高度
        VBox.setVgrow(dataBases, Priority.ALWAYS);
//        空白页显示内容
        dataBases.setPlaceholder(new Label("没有数据,请添加数据库"));
        addEventHandler(UserEvent.CREATE_DATABASE, event -> {
            addDataBase(event.getDataBaseModel());
        });
//        dataBases.setPrefHeight(getPrefHeight());
        ObservableList<DataBaseModel> dataBaseModels = JsonQuery.dataBaseModels;
        for (DataBaseModel dataBaseModel : dataBaseModels) {
            dataBases.getItems().add(setDataBaseTree(dataBaseModel));
        }

        getChildren().addAll(dataBases);

    }

    /**
     * 设置数据库树结构
     *
     * @param dataBase
     */
    public JFXTreeView setDataBaseTree(DataBaseModel dataBase) {
        JFXTreeView<String> dataBaseTreeView = new JFXTreeView();
        dataBaseTreeView.setId(dataBase.getName());
//            节点选中改变监听(得到的是Index)
        dataBaseTreeView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                log.debug("DataBasePane TreeItem选中改变,由于存在多个TreeView 因此不适用");
            }
        });
//          节点选中变化监听(得到的是Item)
        dataBaseTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
                UserEvent userEvent = new UserEvent(UserEvent.CURRENT_DATABASE);
                TreeItem<String> dataBaseItem = newValue;
                if (newValue.isLeaf()) {
                    dataBaseItem = newValue.getParent();
                }
                DataBaseModel dataBaseModel = new DataBaseModel();
                dataBaseModel.setName(dataBaseItem.getValue());
                userEvent.setDataBaseModel(dataBaseModel);
                UserEventBridgeService.bridgeUserEvent2BottomInfoPane(userEvent);
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
                        log.debug("DataBasePane组件双击了表:[{}]", selectedItem.getValue());
                        UserEvent userEvent = new UserEvent(UserEvent.QUERY_TABLE);
                        TableModel tableModel = new TableModel();
                        tableModel.setTableName(selectedItem.getValue());
                        tableModel.setDataBaseName(selectedItem.getParent().getValue());
                        userEvent.setTableModel(tableModel);
                        UserEventBridgeService.bridgeUserEvent2ContentPane(userEvent);
                    } else {
                        log.debug("DataBasePane组件双击了数据库:[{}]", selectedItem.getValue());
                        UserEvent userEvent = new UserEvent(UserEvent.QUERY_DATABASE);
                        DataBaseModel dataBaseModel = new DataBaseModel();
                        dataBaseModel.setName(selectedItem.getValue());
                        userEvent.setDataBaseModel(dataBaseModel);
                        UserEventBridgeService.bridgeUserEvent2ContentPane(userEvent);
                    }


                }
            }
        });
        dataBaseTreeView.setPadding(new Insets(0, 0, 0, 0));
        TreeItem<String> dataBaseRootTreeItem = new TreeItem<>(dataBase.getName());
//            根节点默认不展开
        dataBaseRootTreeItem.setExpanded(false);
//            设置根节点(即数据库名称)
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
        return dataBaseTreeView;
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

    /**
     * 添加数据库
     *
     * @param dataBaseModel
     */
    public void addDataBase(DataBaseModel dataBaseModel) {
        JsonQuery.dataBaseModels.add(dataBaseModel);
        dataBases.getItems().add(setDataBaseTree(dataBaseModel));
        JsonQuery.persistenceConfig();
    }
}
