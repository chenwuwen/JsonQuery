package com.kanyun.ui;

import javafx.event.Event;
import javafx.event.EventType;

public class UserEvent extends Event {

    private String tableName;
    private String dataBaseName;

    public static final EventType<UserEvent> ANY = new EventType<>(Event.ANY, "ANY");

    /**
     * 新的查询按钮事件
     */
    public static final EventType<UserEvent> NEW_QUERY = new EventType<>(ANY, "NEW_QUERY");

    /**
     * 新建数据库事件
     */
    public static final EventType<UserEvent> QUERY_DATABASE = new EventType<>(ANY, "QUERY_DATABASE");

    /**
     * 双击表查询表事件
     */
    public static final EventType<UserEvent> QUERY_TABLE = new EventType<>(ANY, "QUERY_TABLE");


    public UserEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDataBaseName() {
        return dataBaseName;
    }

    public void setDataBaseName(String dataBaseName) {
        this.dataBaseName = dataBaseName;
    }
}
