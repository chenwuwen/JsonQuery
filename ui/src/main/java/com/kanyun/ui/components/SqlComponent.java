package com.kanyun.ui.components;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import com.jfoenix.controls.JFXTextArea;
import com.kanyun.sql.QueryInfoHolder;
import com.kanyun.sql.SqlExecutor;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.SqlSuggestionUtil;
import com.kanyun.ui.event.ExecuteSqlService;
import com.kanyun.ui.event.UserEvent;
import com.kanyun.ui.event.UserEventBridgeService;
import com.kanyun.ui.layout.TopButtonPane;
import com.sun.javafx.event.EventUtil;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import impl.org.controlsfx.skin.AutoCompletePopup;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Transform;
import javafx.stage.Window;
import org.apache.calcite.sql.SqlUtil;
import org.apache.calcite.util.Static;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 新建查询组件(内容区域SQL+结果)
 */
public class SqlComponent extends SplitPane {

    private static final Logger log = LoggerFactory.getLogger(SqlComponent.class);

    /**
     * SQL执行线程池
     */
    private static ExecutorService sqlExecutor = Executors.newCachedThreadPool();

    /**
     * 异步任务Service
     */
    private ExecuteSqlService executeSqlService;

    /**
     * SQL编写区域
     */
    private JFXTextArea writeSqlArea = new JFXTextArea();

    /**
     * SQL执行结果tableView
     */
    private TableViewPane tableViewPane = new TableViewPane();

    /**
     * 当前编写的SQL,与writeSqlArea.textProperty()属性进行了绑定
     */
    private SimpleStringProperty currentSqlProperty = new SimpleStringProperty();

    /**
     * 自动完成弹窗组件
     */
    private AutoCompletePopup autoCompletePopup = new AutoCompletePopup();

    public SqlComponent() {
        setId("SqlComponent");
//        节点中子布局不会随拖动变化大小
//        setResizableWithParent();
//        设置分隔布局的方向
        setOrientation(Orientation.VERTICAL);
//        添加子组件
        getItems().addAll(writeSqlArea);
//        将TextArea的文本属性绑定到SimpleStringProperty,方便后面取值,设值以及监听,双向绑定,注意不要绑定反了,否则TextArea将不能编辑
//        writeSqlArea.textProperty().bind(currentSqlProperty);
        currentSqlProperty.bind(writeSqlArea.textProperty());
        executeSqlService = new ExecuteSqlService();
        addAsyncTaskListener();
//        设置节点是否可见
//        writeSqlArea.setVisible(false);
//        自动获取焦点,需放在Platform.runLater()中执行
        Platform.runLater(() -> writeSqlArea.requestFocus());
        writeSqlArea.setPromptText("提示：1、SQL中可以使用单引号,不要使用双引号");
    }

    /**
     * 异步执行SQL
     *
     * @param defaultSchema
     */
    public void executeSQL(String defaultSchema) {
        String sql = currentSqlProperty.get();
        String modelJson = ModelJson.getModelJson(defaultSchema);
        if (executeSqlService.isRunning()) {
            log.warn("准备执行SQL:[{}],查询到异步任务当前为运行状态", sql);
        }
//        执行SQL时,判断当前TableViewPane是否已加载到界面,如果加载过了,说明之前执行过SQL了,现在重新执行,需要将之前执行的结果清除掉
        if (getItems().size() > 1) {
//            由于tableViewPane是成员变量,因此只在界面移除tableViewPane是不够的,tableViewPane依然保留了之前查询结果的字符和数据信息,因此需要将这些信息移除掉
            tableViewPane.clearTableView();
            getItems().remove(1);
        }
        executeSqlService.setSql(sql).setDefaultSchema(defaultSchema).setModelJson(modelJson);
//        javaFx Service异步任务执行start()方法时,需要保证Service为ready状态,service成功执行后其状态时successed状态,因此再任务结束后(成功/失败/取消),要重置service的状态
        executeSqlService.start();
    }

    /**
     * 美化SQL
     */
    public void beautifySQL() {
        String sql = currentSqlProperty.get();
        if (StringUtils.isEmpty(sql)) return;
        String beautifySql = SqlFormatter.of(Dialect.MySql).format(sql);
        writeSqlArea.setText(beautifySql);
    }

    /**
     * 取消SQL执行
     */
    public void stopSQL() {
        if (executeSqlService.isRunning()) {
            boolean cancel = executeSqlService.cancel();
            log.debug("当前SQL任务:[{}],正在执行,取消任务执行,操作结果:[{}],并重置任务状态[setOnCancelled()]", getCurrentSql(), cancel);
        }
    }

    /**
     * 添加异步任务监听器
     */
    private void addAsyncTaskListener() {
//        异步任务成功执行完成
        executeSqlService.setOnSucceeded(event -> {
            log.debug("异步任务[{}]成功执行完成,将发射事件给父组件,用以更新动态信息栏", getCurrentSql());
            Object result = event.getSource().getValue();
            Pair<Map<String, Integer>, List<Map<String, Object>>> execute = (Pair<Map<String, Integer>, List<Map<String, Object>>>) result;
//            得到结果字段信息(字段名和字段类型)
            Map<String, Integer> columnInfos = execute.getLeft();
            tableViewPane.setTableColumns(columnInfos);
            tableViewPane.setTableRows(FXCollections.observableList(execute.getRight()));
//            注意:getItems().add(1,tableViewPane)这种形式是有问题的
            getItems().add(tableViewPane);
//            发送SQL执行完成事件
            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL_COMPLETE);
            userEvent.setQueryInfo(executeSqlService.getQueryInfo());
//            给该组件的父组件发射SQL执行完成事件
            EventUtil.fireEvent(getParent(), userEvent);
            executeSqlService.reset();
        });

//        异步任务执行失败
        executeSqlService.setOnFailed(event -> {
            log.error("异步任务[{}]执行失败", getCurrentSql(), event.getSource().getException());
//            创建任务执行失败事件
            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL_FAIL);
            userEvent.setException(event.getSource().getException());
//            给该组件的父组件发射SQL执行失败事件
            EventUtil.fireEvent(getParent(), userEvent);
            executeSqlService.reset();
        });

//        异步任务取消
        executeSqlService.setOnCancelled(event -> {
            log.warn("异步任务[{}]被取消", getCurrentSql());
            executeSqlService.reset();
        });

        EventHandler<MouseEvent> mouseEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                log.info("光标所在位置:{},{}", x, y);
            }
        };
//        给TextArea添加鼠标移动事件(暂时没用)
//        writeSqlArea.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEventHandler);

        writeSqlArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                log.info("编写SQL区域内容发生变化,oldValue:{},newValue:{}", oldValue, newValue);
//                插入符号(光标)在文本中的位置,其实就是光标所在的文字中的索引
                int cursorIndex = writeSqlArea.getCaretPosition();
                log.info("当前光标在文字中的索引位置(从0开始):{}", cursorIndex);
                List<String> suggestions = SqlSuggestionUtil.search(newValue, cursorIndex);
                if (suggestions != null && !suggestions.isEmpty()) {
                    ObservableList suggestionsList = autoCompletePopup.getSuggestions();
                    suggestionsList.removeAll(suggestionsList);
                    suggestionsList.addAll(suggestions);
                    autoCompletePopup.show(writeSqlArea, computeAutoCompletePopupCoordinate().getLeft(), computeAutoCompletePopupCoordinate().getRight());
                }

            }
        });
    }

    /**
     * 计算SQL提示弹窗显示坐标(屏幕坐标)
     *
     * @return
     */
    private Pair<Double, Double> computeAutoCompletePopupCoordinate() {
        TextAreaSkin skin = (TextAreaSkin) writeSqlArea.getSkin();
        Bounds caretBounds = skin.getCaretBounds();
//        将本地坐标(即TextArea内的0,0坐标)转换为场景坐标
        Point2D point2D = writeSqlArea.localToScene(0.0, 0.0);
//        获取窗口对象
        Window window = writeSqlArea.getScene().getWindow();
        double fontSize = writeSqlArea.getFont().getSize();
//        sql提示弹窗坐标X位置为 窗口的x坐标值+(TextArea原点)所在的场景的x坐标值+光标移动的X值
        double anchorX = window.getX() + point2D.getX() + caretBounds.getMaxX();
//        sql提示弹窗坐标Y位置为 窗口的Y坐标值+(TextArea原点)所在的场景的Y坐标值+光标移动的Y值+字体尺寸+冗余值(避免弹窗盖住TextArea中文字)
        double anchorY = window.getY() + point2D.getY() + caretBounds.getMaxY() + fontSize + 20;
        return Pair.of(anchorX, anchorY);
    }


    /**
     * 得到当前的sql内容
     *
     * @return
     */
    public String getCurrentSql() {
        return writeSqlArea.getText();
    }

}
