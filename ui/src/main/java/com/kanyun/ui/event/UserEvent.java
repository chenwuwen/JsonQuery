package com.kanyun.ui.event;

import com.kanyun.ui.model.DataBaseModel;
import com.kanyun.ui.model.TableModel;
import javafx.event.Event;
import javafx.event.EventType;

import java.util.Map;

public class UserEvent extends Event {

    /**
     * 数据库模型
     */
    private DataBaseModel dataBaseModel;

    /**
     * 数据库表模型
     */
    private TableModel tableModel;

    /**
     * 执行的SQL
     */
    private String sql;

    /**
     * 查询过程的信息
     */
    private Map<String, Object> queryInfo;

    public static final EventType<UserEvent> ANY = new EventType<>(Event.ANY, "ANY");

    /**
     * 新的查询按钮事件
     */
    public static final EventType<UserEvent> NEW_QUERY = new EventType<>(ANY, "NEW_QUERY");

    /**
     * 双击数据库查询表事件
     */
    public static final EventType<UserEvent> QUERY_DATABASE = new EventType<>(ANY, "QUERY_DATABASE");

    /**
     * 检查表事件
     */
    public static final EventType<UserEvent> INSPECT_TABLE = new EventType<>(ANY, "INSPECT_TABLE");

    /**
     * 新建数据库事件
     */
    public static final EventType<UserEvent> CREATE_DATABASE = new EventType<>(ANY, "CREATE_DATABASE");

    /**
     * 双击表查询表事件
     */
    public static final EventType<UserEvent> QUERY_TABLE = new EventType<>(ANY, "QUERY_TABLE");

    /**
     * 数据库发生变化事件
     */
    public static final EventType<UserEvent> DATABASE_MODIFY = new EventType<>(ANY, "DATABASE_MODIFY");

    /**
     * 选中(或子项)元素数量变化事件,如:当前数据库数量,选中库下的表数量,选中的表(函数数量)
     */
    public static final EventType<UserEvent> ITEMS_COUNT = new EventType<>(ANY, "ITEMS_COUNT");

    /**
     * 选择数据库事件
     */
    public static final EventType<UserEvent> CURRENT_DATABASE = new EventType<>(ANY, "CURRENT_DATABASE");

    /**
     * 执行SQL事件
     */
    public static final EventType<UserEvent> EXECUTE_SQL = new EventType<>(ANY, "EXECUTE_SQL");

    /**
     * SQL执行完成事件
     */
    public static final EventType<UserEvent> EXECUTE_SQL_COMPLETE = new EventType<>(ANY, "EXECUTE_SQL_COMPLETE");

    /**
     * 美化按钮
     */
    public static final EventType<UserEvent> BEAUTIFY_SQL = new EventType<>(ANY, "BEAUTIFY_SQL");

    /**
     * 应用函数事件
     */
    public static final EventType<UserEvent> APPLY_FUNC = new EventType<>(ANY, "APPLY_FUNC");


    public UserEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public DataBaseModel getDataBaseModel() {
        return dataBaseModel;
    }

    public void setDataBaseModel(DataBaseModel dataBaseModel) {
        this.dataBaseModel = dataBaseModel;
    }

    public TableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(TableModel tableModel) {
        this.tableModel = tableModel;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Map<String, Object> getQueryInfo() {
        return queryInfo;
    }

    public void setQueryInfo(Map<String, Object> queryInfo) {
        this.queryInfo = queryInfo;
    }
}
