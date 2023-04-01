package com.kanyun.ui.layout;

import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.model.TableModel;
import com.kanyun.ui.tabs.*;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * 中间内容区域组件
 */
public class ContentPane extends TabPane {
    private static final Logger log = LoggerFactory.getLogger(ContentPane.class);

    public ContentPane() {
        setId("ContentPane");
        addTabEventListener();
        addUserEventListener();
//        ObjectTab永远在第一个,且不可关闭,需要切换到该Tab时:只需 getTabs().get(0) 即可
        Tab objTab = new Tab(TabObjectsPane.TAB_NAME);
        objTab.setContent(new TabObjectsPane());
        objTab.setClosable(false);
        getTabs().add(objTab);
//        注意手动切换一下用以触发监听,由于当前Tab页中只存在一个Tab,因此在add()后会显示当前的Tab,但不会触发监听
//        同时需要注意的是,由于触发监听后会发射事件,因此要保证在发射事件时,可以lookup()到对应的EventTarget
        getSelectionModel().select(objTab);
    }

    /**
     * 添加用户事件监听器
     */
    public void addUserEventListener() {

        /**
         * 新建查询按钮监听
         */
        addEventHandler(UserEvent.NEW_QUERY, event -> {
            log.debug("ContentPane 接收到新建查询事件,准备开启Tab页");
            Tab queryTab = new Tab("新的查询");
            TabQueryPane tabQueryPane = new TabQueryPane();
            queryTab.setContent(tabQueryPane);
            getTabs().add(queryTab);
            getSelectionModel().select(queryTab);
        });

        /**
         * 双击数据库表监听
         */
        addEventHandler(UserEvent.QUERY_TABLE, event -> {
            log.debug("ContentPane 接收到数据页,先确定是哪张表,然后开启tab页");
            String tabName = event.getTableModel().getTableName() + " @" + event.getTableModel().getSchemaName();
            Tab tableTab = new Tab(tabName);
            FontAwesomeIconView fontAwesomeIcon
                    = new FontAwesomeIconView(FontAwesomeIcon.TABLE);
            fontAwesomeIcon.setFill(Color.BLUE);
            tableTab.setGraphic(fontAwesomeIcon);
            try {
                TabQueryTablePane tabQueryTablePane = new TabQueryTablePane(event.getTableModel());
                tableTab.setContent(tabQueryTablePane);
                getTabs().add(tableTab);
                getSelectionModel().select(tableTab);
            } catch (Exception e) {
                e.printStackTrace();
                ExceptionDialog sqlExecuteErrDialog = new ExceptionDialog(e);
                sqlExecuteErrDialog.setTitle("打开表异常");
                sqlExecuteErrDialog.show();
            }

        });

        /**
         * 双击数据库事件监听
         */
        addEventHandler(UserEvent.QUERY_DATABASE, event -> {
            log.debug("ContentPane 接收到对象页,先确定是哪个库,然后开启tab页");
            Tab objectsTab = getTabs().get(0);
            UserEvent userEvent = new UserEvent(UserEvent.SHOW_OBJECTS);
            userEvent.setDataBaseModel(event.getDataBaseModel());
            UserEventBridgeService.bridgeUserEvent2TabObjectsPane(userEvent);
            getSelectionModel().select(objectsTab);
        });

        /**
         * 检查表事件监听
         */
        addEventHandler(UserEvent.INSPECT_TABLE, event -> {
            log.debug("ContentPane 接收到对象页,检查表");
            String tabName = event.getTableModel().getTableName() + " @" + event.getTableModel().getSchemaName();
            Tab tableInspectTab = new Tab(tabName);
            try {
                TabInspectTablePane tabInspectTablePane = new TabInspectTablePane(event.getTableModel());
                tableInspectTab.setContent(tabInspectTablePane);
                getTabs().add(tableInspectTab);
                getSelectionModel().select(tableInspectTab);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 添加Tab页监听器
     */
    public void addTabEventListener() {
//        EventFilter先于EventHandler执行,这里可以用来判断Tab也是否存在,存在则切换到Tab页,不存在则新建Tab页
        addEventFilter(UserEvent.ANY, new EventHandler<UserEvent>() {
            @Override
            public void handle(UserEvent event) {
                EventType<? extends Event> eventType = event.getEventType();
                if (eventType == UserEvent.NEW_QUERY) {
//                    如果是新建查询按钮被点击,则继续执行
                    return;
                }
                if (eventType == UserEvent.QUERY_DATABASE) {
//                    如果是查询数据库,则继续执行
                    return;
                }
                if (eventType == UserEvent.QUERY_TABLE) {
                    TableModel tableModel = event.getTableModel();
                    String newTabName = tableModel.getTableName() + " @" + tableModel.getSchemaName();
//                    查看当前Tab页是否存在,不存在继续执行,存在则切换到对应Tab页,并且消费掉事件,EventHandler不再执行
                    ObservableList<Tab> tabs = getTabs();
                    Optional<Tab> first = tabs.stream().filter(tab -> tab.getText().equals(newTabName)).findFirst();
                    if (first.isPresent()) {
//                        切换到指定Tab上
                        getSelectionModel().select(first.get());
//                        阻止事件传递 addEventHandler() 将不再执行
                        event.consume();
                    }
                }
                if (eventType == UserEvent.INSPECT_TABLE) {
//                    如果是检查表,则继续执行
                    return;
                }
            }
        });


//        监听Tab页切换(新增Tab页时也会触发该监听)
        getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (oldValue == null) {
                    log.debug("应用初始化,旧Tab页为null");
                } else {
                    log.debug("检测到Tab切换,原Tab：[{}],新Tab：[{}]", oldValue.getText(), newValue.getText());
                }
                Node content = newValue.getContent();
                TabKind tabKind = (TabKind) content;
                TabKindEnum kind = tabKind.getTabKind();
                UserEvent userEvent = new UserEvent(UserEvent.DYNAMIC_SETTING_STATUS_BAR);
                if (kind == TabKindEnum.INSPECT_TAB) {

                }
                if (kind == TabKindEnum.TABLE_TAB) {

                }
                if (kind == TabKindEnum.OBJECT_TAB) {

                }
                if (kind == TabKindEnum.SQL_TAB) {

                }
                userEvent.setStatusBar(tabKind.getDynamicInfoStatusBar());
                UserEventBridgeService.bridgeUserEvent2BottomInfoPane(userEvent);
            }
        });
    }
}
