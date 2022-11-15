package com.kanyun.ui.layout;

import com.kanyun.ui.tabs.TabObjectPane;
import com.kanyun.ui.tabs.TabQueryPane;
import com.kanyun.ui.tabs.TabTablePane;
import com.kanyun.ui.UserEvent;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.Optional;

public class ContentPane extends TabPane {

    public ContentPane() {
        setId("ContentPane");
//        ObjectTab永远在第一个,且不可关闭,需要切换到该Tab时:只需 getTabs().get(0) 即可
        Tab objTab = new Tab(TabObjectPane.TAB_NAME);
        objTab.setContent(new TabObjectPane(""));
        objTab.setClosable(false);
        getTabs().add(objTab);

//        EventFilter先与EventHandler执行,这里可以用来判断Tab也是否存在,存在则切换到Tab页,不存在则新建Tab页
        addEventFilter(UserEvent.ANY, new EventHandler<UserEvent>() {
            @Override
            public void handle(UserEvent event) {
                String newTabName = "";
                EventType<? extends Event> eventType = event.getEventType();
                if (eventType == UserEvent.NEW_QUERY) {
//                    如果是新建查询按钮被点击,则继续执行
                    return;
                } else if (eventType == UserEvent.QUERY_DATABASE) {
//                    如果是查询数据库,则继续执行
                    return;
                } else {
//                    查看当前Tab页是否存在,不存在继续执行,存在则切换到对应Tab页,并且消费掉事件,EventHandler不再执行
                    ObservableList<Tab> tabs = getTabs();
                    Optional<Tab> first = tabs.stream().filter(tab -> tab.getText().equals(newTabName)).findFirst();
                    if (first.isPresent()) {
//                        切换到指定Tab上
                        getSelectionModel().select(first.get());
//                        阻止事件传递
                        event.consume();
                    }

                }
            }
        });

        /**
         * 新建查询按钮监听
         */
        addEventHandler(UserEvent.NEW_QUERY, event -> {
            System.out.println("接收到新建查询事件,准备开启Tab页");
            Tab queryTab = new Tab("新的查询");
            final TabQueryPane tabQueryPane = new TabQueryPane();
            queryTab.setContent(tabQueryPane);
            getTabs().add(queryTab);
            getSelectionModel().select(queryTab);
        });

        /**
         * 双击数据库表监听
         */
        addEventHandler(UserEvent.QUERY_TABLE, event -> {
            System.out.println("接收到数据页,先确定是哪张表,然后开启tab页");
            Tab tableTab = new Tab(event.getTableName());
            final TabTablePane tabTablePane = new TabTablePane(event.getDataBaseName(), event.getTableName());
            tableTab.setContent(tabTablePane);
            getTabs().add(tableTab);
            getSelectionModel().select(tableTab);
        });

        /**
         * 双击数据库事件监听
         */
        addEventHandler(UserEvent.QUERY_DATABASE, event -> {
            System.out.println("接收到对象页,先确定是哪个库,然后开启tab页");
            Tab objectsTab = getTabs().get(0);
            final TabObjectPane tabObjectPane = new TabObjectPane(event.getDataBaseName());
            objectsTab.setContent(tabObjectPane);
            getSelectionModel().select(objectsTab);
        });

    }
}