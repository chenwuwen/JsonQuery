package com.kanyun.ui.event;

import com.kanyun.ui.model.DataBaseModel;
import com.kanyun.ui.model.ObjectsTypeEnum;
import com.kanyun.ui.model.TableModel;
import com.kanyun.ui.tabs.TabObjectsPane;
import com.kanyun.ui.tabs.TabQueryPane;
import javafx.event.Event;
import javafx.event.EventType;
import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.control.StatusBar;

import java.util.List;
import java.util.Map;

/**
 * 自定义事件
 */
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
     * 单条SQL执行过程的信息
     */
    private Map<String, Object> signalSqlExecuteInfo;

    /**
     * 多条SQL执行过程的信息
     * key为sql,value为sql执行对应的信息
     */
    private Map<String, Map<String, Object>> multiSqlExecuteInfo;


    /**
     * 多条SQL执行结果集合
     * key为sql,value为sql执行结果(pair#left[结果集的字段信息] pair#right[结果集])
     */
    private Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>> multiSqlExecuteResult;

    private String totalCost;

    /**
     * 状态条
     */
    private StatusBar statusBar;

    /**
     * 错误信息
     */
    private Throwable exception;

    /**
     * 传递的对象类型
     */
    private ObjectsTypeEnum objectsTypeEnum;

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
     * 多条SQL执行完成事件
     * {@link TabQueryPane#addStatusBarEventListener()}
     */
    public static final EventType<UserEvent> EXECUTE_MULTI_SQL_COMPLETE = new EventType<>(ANY, "EXECUTE_MULTI_SQL_COMPLETE");

    /**
     * SQL执行失败事件
     */
    public static final EventType<UserEvent> EXECUTE_SQL_FAIL = new EventType<>(ANY, "EXECUTE_SQL_FAIL");

    /**
     * 应用函数事件
     */
    public static final EventType<UserEvent> APPLY_FUNC = new EventType<>(ANY, "APPLY_FUNC");

    /**
     * 显示Objects事件
     * {@link TabObjectsPane#TabObjectsPane()}
     */
    public static final EventType<UserEvent> SHOW_OBJECTS = new EventType<>(ANY, "SHOW_OBJECTS");

    /**
     * 动态设置Statusbar事件
     * {@link com.kanyun.ui.layout.BottomInfoPane#dynamicSettingInformationBar()}
     */
    public static final EventType<UserEvent> DYNAMIC_SETTING_STATUS_BAR = new EventType<>(ANY, "DYNAMIC_SETTING_STATUS_BAR");


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

    public Map<String, Object> getSignalSqlExecuteInfo() {
        return signalSqlExecuteInfo;
    }

    public void setSignalSqlExecuteInfo(Map<String, Object> signalSqlExecuteInfo) {
        this.signalSqlExecuteInfo = signalSqlExecuteInfo;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void setStatusBar(StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public ObjectsTypeEnum getObjectsTypeEnum() {
        return objectsTypeEnum;
    }

    public void setObjectsTypeEnum(ObjectsTypeEnum objectsTypeEnum) {
        this.objectsTypeEnum = objectsTypeEnum;
    }

    public Map<String, Map<String, Object>> getMultiSqlExecuteInfo() {
        return multiSqlExecuteInfo;
    }

    public void setMultiSqlExecuteInfo(Map<String, Map<String, Object>> multiSqlExecuteInfo) {
        this.multiSqlExecuteInfo = multiSqlExecuteInfo;
    }

    public Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>> getMultiSqlExecuteResult() {
        return multiSqlExecuteResult;
    }

    public void setMultiSqlExecuteResult(Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>> multiSqlExecuteResult) {
        this.multiSqlExecuteResult = multiSqlExecuteResult;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }
}
