package com.kanyun.ui.layout;

import com.google.common.io.Files;
import com.jfoenix.controls.JFXTreeView;
import com.kanyun.ui.JsonQuery;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.model.BaseModel;
import com.kanyun.ui.model.DataBaseModel;
import com.kanyun.ui.model.TableModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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


    private static Integer dataBaseOrTableIconSize = 15;

    /**
     * 数据库列表(原来我是用的是ListView<TreeView>的形式,后面发现这种形式在布局上出现问题,因此采用单一的TreeView来构建树)
     * TreeView的根节点为空,一级子节点是数据库,二级子节点是表
     */
    private TreeView<BaseModel> dataBasesTreeView;

    public DataBasePane() {
        setId("DataBasePane");
        setAlignment(Pos.TOP_LEFT);
        dataBasesTreeView = new JFXTreeView();
//        设置子节点垂直方向总是填充父节点
        VBox.setVgrow(dataBasesTreeView, Priority.ALWAYS);
//        数据库创建事件监听
        addEventHandler(UserEvent.CREATE_DATABASE, event -> {
            addDataBase(event.getDataBaseModel());
        });
        ObservableList<DataBaseModel> dataBaseModels = JsonQuery.dataBaseModels;
//        创建一个空的树节点,用来做根节点
        TreeItem<BaseModel> rootTreeItem = new TreeItem(null);
//        不显示根节点
        dataBasesTreeView.setShowRoot(false);
        for (DataBaseModel dataBaseModel : dataBaseModels) {
            rootTreeItem.getChildren().add(getDataBaseItem(dataBaseModel));
        }
//        设置数据库树的根节点
        dataBasesTreeView.setRoot(rootTreeItem);
        addDataBaseListener();
        getChildren().addAll(dataBasesTreeView);
    }

    /**
     * 得到数据库树字结构(不含根节点)
     *
     * @param dataBase
     */
    public TreeItem<BaseModel> getDataBaseItem(DataBaseModel dataBase) {
//        创建数据库item(注意,我们传入的是对象,此时属性值也设置好了,而item展示的值就是设置对象的toString()的返回值),因此TreeItem没有单独的设置扩展属性的方法
        TreeItem<BaseModel> dataBaseItem = new TreeItem<>(dataBase);
        ImageView databaseImageView = new ImageView("/asserts/database.png");
        databaseImageView.setFitHeight(dataBaseOrTableIconSize);
        databaseImageView.setFitWidth(dataBaseOrTableIconSize);
        dataBaseItem.setGraphic(databaseImageView);
        Collection<File> tables = getDataBaseTable(dataBase.getUrl());
        List<String> tableNames = new ArrayList<>();
        for (File table : tables) {
            String tableName = Files.getNameWithoutExtension(table.getName());
            TableModel tableModel = new TableModel();
            tableModel.setTableName(tableName);
            tableModel.setDataBaseName(dataBase.getName());
            tableModel.setPath(table.getPath());
            tableModel.setDataBaseModel(dataBase);
//            创建表item
            TreeItem<BaseModel> tableItem = new TreeItem<>(tableModel);
            ImageView tableImageView = new ImageView("/asserts/table.png");
            tableImageView.setFitHeight(dataBaseOrTableIconSize);
            tableImageView.setFitWidth(dataBaseOrTableIconSize);
            tableItem.setGraphic(tableImageView);
//            将表item放到库item中(建立父子关系)
            dataBaseItem.getChildren().add(tableItem);
            tableNames.add(tableName);
        }
        dataBase.setTables(FXCollections.observableList(tableNames));
        return dataBaseItem;
    }

    /**
     * 得到数据库(给定的URL)下的表(json文件)
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
        dataBasesTreeView.getRoot().getChildren().add(getDataBaseItem(dataBaseModel));
        JsonQuery.persistenceConfig();
    }

    /**
     * 移除数据库
     *
     * @param treeItem
     */
    public void delDataBase(TreeItem treeItem) {
        treeItem.getParent().getChildren().remove(treeItem);
        treeItem.getChildren().removeAll(treeItem.getChildren());
        if (treeItem.getValue() instanceof DataBaseModel) {
            DataBaseModel dataBaseModel = (DataBaseModel) treeItem.getValue();
            JsonQuery.dataBaseModels.remove(dataBaseModel);
//            JsonQuery.persistenceConfig();
        }


    }

    /**
     * 添加数据库树的监听器
     */
    public void addDataBaseListener() {
//        节点选中改变监听(得到的是Index,注意index是从root下开始算的,从0开始依次向下,先遍历完根节点下第一个子节点的所有内容[递归],然后再开始算根节点下的第二个子节点)
        dataBasesTreeView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                log.debug("DataBasePane TreeItem选中改变");
            }
        });
//          节点选中变化监听(得到的是Item)
        dataBasesTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<BaseModel>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<BaseModel>> observable, TreeItem<BaseModel> oldValue, TreeItem<BaseModel> newValue) {
                UserEvent userEvent = new UserEvent(UserEvent.CURRENT_DATABASE);
                TreeItem<BaseModel> dataBaseItem = newValue;
                if (newValue.isLeaf()) {
                    dataBaseItem = newValue.getParent();
                }
                DataBaseModel dataBaseModel = (DataBaseModel) dataBaseItem.getValue();
                userEvent.setDataBaseModel(dataBaseModel);
                UserEventBridgeService.bridgeUserEvent2BottomInfoPane(userEvent);
            }
        });
//        设置鼠标点击事件监听
        dataBasesTreeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setMouseDoubleClickHandler(event);
                setMouseRightClickHandler(event);
            }
        });
    }

    /**
     * 设置鼠标右键事件
     *
     * @param event
     */
    private void setMouseRightClickHandler(MouseEvent event) {
        MouseButton button = event.getButton();
//        判断是否是右键
        if (button == MouseButton.SECONDARY) {
            MenuItem delDatabaseItem = new MenuItem("移除数据库");
            MenuItem inspectTable = new MenuItem("检查表");
            ContextMenu menu = new ContextMenu();
            TreeItem<BaseModel> selectedItem = dataBasesTreeView.getSelectionModel().getSelectedItem();
//          todo  未选中项时,selectedItem是null,此时也可以直接触发添加数据库操作
            if (selectedItem == null) return;
//           得到当前树项的层级,root为0
            int treeItemLevel = dataBasesTreeView.getTreeItemLevel(selectedItem);
//            说明点击的是表
            if (selectedItem.isLeaf()) {
                menu.getItems().add(inspectTable);
                dataBasesTreeView.setContextMenu(menu);
            }
//            说明右键的是数据库
            if (treeItemLevel == 1) {
                menu.getItems().add(delDatabaseItem);
                dataBasesTreeView.setContextMenu(menu);
            }
//            点击了删除数据库
            delDatabaseItem.setOnAction(e -> {
                delDataBase(selectedItem);
            });
//            点击了检查表按钮
            inspectTable.setOnAction(e -> {
                UserEvent userEvent = new UserEvent(UserEvent.INSPECT_TABLE);
                TableModel tableModel = (TableModel) selectedItem.getValue();
                userEvent.setTableModel(tableModel);
                UserEventBridgeService.bridgeUserEvent2ContentPane(userEvent);
            });

        }

    }


    /**
     * 设置鼠标双击事件
     *
     * @param event
     */
    private void setMouseDoubleClickHandler(MouseEvent event) {
        if (event.getClickCount() == 2) {
            TreeItem<BaseModel> selectedItem = dataBasesTreeView.getSelectionModel().getSelectedItem();
//           得到当前树项的层级,root为0
            int treeItemLevel = dataBasesTreeView.getTreeItemLevel(selectedItem);
            if (selectedItem.isLeaf()) {
//                是叶子节点说明是表
                log.debug("DataBasePane组件双击了表:[{}]", selectedItem.getValue());
                UserEvent userEvent = new UserEvent(UserEvent.QUERY_TABLE);
                TableModel tableModel = (TableModel) selectedItem.getValue();
                userEvent.setTableModel(tableModel);
                UserEventBridgeService.bridgeUserEvent2ContentPane(userEvent);
            }

            if (treeItemLevel == 1) {
                log.debug("DataBasePane组件双击了数据库:[{}]", selectedItem.getValue());
                UserEvent userEvent = new UserEvent(UserEvent.QUERY_DATABASE);
                DataBaseModel dataBaseModel = (DataBaseModel) selectedItem.getValue();
                userEvent.setDataBaseModel(dataBaseModel);
                UserEventBridgeService.bridgeUserEvent2ContentPane(userEvent);
            }
        }
    }
}
