package com.kanyun.ui.components;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import com.google.common.io.Files;
import com.jfoenix.controls.JFXTextArea;
import com.kanyun.sql.core.ModelJson;
import com.kanyun.ui.SqlSuggestionUtil;
import com.kanyun.ui.event.ExecuteSqlPoolService;
import com.kanyun.ui.event.UserEvent;
import com.sun.javafx.event.EventUtil;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import impl.org.controlsfx.skin.AutoCompletePopup;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 新建查询组件(内容区域SQL)
 */
public class SqlComponent extends VBox {

    private static final Logger log = LoggerFactory.getLogger(SqlComponent.class);

    /**
     * 异步任务Service(线程池)
     */
    private ExecuteSqlPoolService executeSqlPoolService;

    /**
     * SQL编写区域
     */
    private JFXTextArea writeSqlArea = new JFXTextArea();


    /**
     * 当前编写的SQL,与writeSqlArea.textProperty()属性进行了绑定
     */
    private SimpleStringProperty currentSqlProperty = new SimpleStringProperty();

    /**
     * 自动完成弹窗组件
     */
    private AutoCompletePopup<String> autoCompletePopup = new AutoCompletePopup();

    /**
     * 当前Schema
     */
    private SimpleStringProperty currentSchema;

    public SqlComponent(SimpleStringProperty currentSchema) {
        setId("SqlComponent");
        this.currentSchema = currentSchema;
//        将TextArea的文本属性绑定到SimpleStringProperty,方便后面取值,设值以及监听,双向绑定,注意不要绑定反了,否则TextArea将不能编辑
//        writeSqlArea.textProperty().bind(currentSqlProperty);
        currentSqlProperty.bind(writeSqlArea.textProperty());
        executeSqlPoolService = new ExecuteSqlPoolService();
        addAsyncTaskListener();
//        设置节点是否可见
//        writeSqlArea.setVisible(false);
//        自动获取焦点,需放在Platform.runLater()中执行
        Platform.runLater(() -> writeSqlArea.requestFocus());
        writeSqlArea.setWrapText(false);
        try {
//            从文件中读取每一行数据,并使用系统换行符进行拼接,实现换行效果
            List<String> promptItemList = Files.asCharSource(
                    new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("sql_prompt.txt")).getPath()),
                    StandardCharsets.UTF_8
            ).readLines();
            String promptText = StringUtils.join(promptItemList, System.lineSeparator());
            writeSqlArea.setPromptText(promptText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        VBox.setVgrow(writeSqlArea, Priority.ALWAYS);
        getChildren().add(writeSqlArea);
    }

    /**
     * 异步执行SQL
     *
     * @param defaultSchema
     */
    public void executeSQL(String defaultSchema, String sql) {
//        这里之所以不通过TextArea的textProperty的属性取获取SQL,主要是因为存在只执行选中SQL的情况
//        String sql = currentSqlProperty.get();
        String modelJson = ModelJson.getModelJson(defaultSchema);
        if (executeSqlPoolService.isRunning()) {
            log.warn("准备执行SQL:[{}],查询到异步任务当前为运行状态", sql);
        }
        executeSqlPoolService.addAllSql(Arrays.asList(sql.split(";"))).setDefaultSchema(defaultSchema).setModelJson(modelJson);
//        javaFx Service异步任务执行start()方法时,需要保证Service为ready状态,service成功执行后其状态时successed状态,因此再任务结束后(成功/失败/取消),要重置service的状态
        executeSqlPoolService.start();
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
        log.debug("当前执行状态:{}", executeSqlPoolService.getState());
        if (executeSqlPoolService.isRunning()) {
            boolean cancel = executeSqlPoolService.cancel();
            log.debug("当前SQL任务:[{}],正在执行,取消任务执行,操作结果:[{}],并重置任务状态[setOnCancelled()]", executeSqlPoolService.getSqlList(), cancel);
        }
        executeSqlPoolService.reset();
    }

    /**
     * 添加异步任务监听器
     */
    private void addAsyncTaskListener() {
//        异步任务成功执行完成
        executeSqlPoolService.setOnSucceeded(event -> {
            Object result = event.getSource().getValue();
            Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>> queryResultCollection = (Map<String, Pair<Map<String, Integer>, List<Map<String, Object>>>>) result;
            Map<String, Map<String, Object>> queryInfoCollection = executeSqlPoolService.getQueryInfoCollection();
            log.debug("异步(批量)任务{}成功执行完成,将发射事件给父组件,用以更新显示SQL执行结果及动态信息栏", queryInfoCollection.keySet());
//            发送SQL执行完成事件
            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_MULTI_SQL_COMPLETE);
            userEvent.setMultiSqlExecuteInfo(queryInfoCollection);
            userEvent.setMultiSqlExecuteResult(queryResultCollection);
            userEvent.setTotalCost(executeSqlPoolService.getTotalCost());
//            给该组件的父组件发射SQL执行完成事件
            EventUtil.fireEvent(getParent(), userEvent);
            executeSqlPoolService.reset();
        });

//        异步任务执行失败
        executeSqlPoolService.setOnFailed(event -> {
            log.error("异步(批量)任务[{}]执行失败", executeSqlPoolService.getSqlList(), event.getSource().getException());
//            创建任务执行失败事件
            UserEvent userEvent = new UserEvent(UserEvent.EXECUTE_SQL_FAIL);
            userEvent.setException(event.getSource().getException());
//            给该组件的父组件发射SQL执行失败事件,用于弹出告警框
            EventUtil.fireEvent(getParent(), userEvent);
            stopSQL();
        });

//        异步任务取消
        executeSqlPoolService.setOnCancelled(event -> {
            log.warn("异步(批量)任务[{}]被取消", executeSqlPoolService.getSqlList());
            stopSQL();
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

        EventHandler<KeyEvent> shortcutKeyEventHandler = createShortcutKey();
//        给TextArea添加键盘按下事件(用作快捷键,注意:需要防止快捷键冲突,当按下快捷键不生效时,检查下快捷键是否与系统或其他应用冲突了)
        writeSqlArea.addEventHandler(KeyEvent.KEY_PRESSED, shortcutKeyEventHandler);
        writeSqlArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                log.debug("编写SQL区域内容发生变化,oldValue:{},newValue:{}", oldValue, newValue);
                autoCompletePopup.hide();
//                插入符号(光标)在文本中的位置,其实就是光标所在的文字中的索引
                int cursorIndex = writeSqlArea.getCaretPosition();
                log.debug("当前光标在文字中的索引位置(从0开始):{}", cursorIndex);

                List<String> suggestions = SqlSuggestionUtil.searchSuggestion(currentSchema.get(), newValue, cursorIndex);
                if (!suggestions.isEmpty()) {
                    ObservableList<String> suggestionsList = autoCompletePopup.getSuggestions();
                    suggestionsList.clear();
                    suggestionsList.addAll(suggestions);
//                    最后两个参数要注意,它要求传递的是屏幕坐标
                    autoCompletePopup.show(writeSqlArea, computeAutoCompletePopupCoordinate().getX(), computeAutoCompletePopupCoordinate().getY());
                }

            }
        });

//        SQL自动完成弹窗监听事件,当选中建议时,将建议插入到SQL中(插入建议时要考虑是否需要删除已输入的字符,避免出现sSELECT的情况)
        autoCompletePopup.setOnSuggestion(event -> {
            if (event.getEventType() == AutoCompletePopup.SuggestionEvent.SUGGESTION) {
                String selectedSuggestion = event.getSuggestion();
                log.debug("选中代码提示框中的建议:{},并将建议插入到SQL中", selectedSuggestion);
                while (true) {
//                    为了避免出现SQL插入建议后,出现sSELECT的情况,需要将当前光标前的字符进行删除处理,只有当遇到空格或.的情况才停止删除
                    int caretPosition = writeSqlArea.getCaretPosition();
                    if (caretPosition == 0) break;
//                    判断光标前的第一个字符是否是点,或者空格,或者分号(遇到分号表示到达上一个SQL),如果是则跳过
                    if (writeSqlArea.getText(caretPosition - 1, caretPosition).equals(".")
                            || writeSqlArea.getText(caretPosition - 1, caretPosition).equals(";")
                            || StringUtils.isBlank(writeSqlArea.getText(caretPosition - 1, caretPosition))) {
                        break;
                    }
                    writeSqlArea.deleteText(caretPosition - 1, caretPosition);
                }
                writeSqlArea.insertText(writeSqlArea.getCaretPosition(), selectedSuggestion);
                autoCompletePopup.hide();
            }
        });
    }

    /**
     * 计算SQL提示弹窗显示位置的坐标(屏幕坐标),这里我们要求代码提示弹窗在光标的下面且不影响已输入的文字显示
     * 这里重点的要注意获取光标坐标的方法是:
     * Bounds caretBounds = TextAreaSkin.getCaretBounds()
     * caretBounds.getMaxX() / caretBounds.getMaxY() / caretBounds.getMinY() / caretBounds.getMinY()
     * 上面四个方法分别对应获取光标的右下角X/右下角Y/左上角Y/左上角X 的坐标信息
     * caret 和 cursor 在中文里都被会被翻译为光标，但实际上指的是两种完全不同的东西，caret 指的是那个一闪一闪提示用户输入的小图标，
     * 它的位置相对固定的，作用是提示用户当前文档的输入位置；而 cursor 指的是鼠标的图标，跟着一直随着鼠标一起移动，
     * 它的作用是提示当前文档的模式，是只读还是可编辑。在编辑器的编辑模式下，通常会把 cursor 设置成 “工” 的图形，
     * 这个只需要一行 css 代码就能解决
     *
     * @return Point2D 计算好的提示弹窗显示的坐标(屏幕坐标)
     */
    private Point2D computeAutoCompletePopupCoordinate() {
        TextAreaSkin skin = (TextAreaSkin) writeSqlArea.getSkin();
//        javafx.geometry.Bounds是JavaFX中用于表示几何形状边界的类,它提供了各种构造函数和方法来处理和操作边界,以及获取点位置的方法
        Bounds caretBounds = skin.getCaretBounds();
//        获取caretBounds边界的右下角X轴坐标
        double caretMaxX = caretBounds.getMaxX();
//        获取caretBounds边界的右下角Y轴坐标
        double caretMaxY = caretBounds.getMaxY();

//        将本地坐标(即TextArea内的0,0坐标)转换为场景坐标
        Point2D point2D = writeSqlArea.localToScene(0.0, 0.0);
//        获取窗口对象
        Window window = writeSqlArea.getScene().getWindow();
        double fontSize = writeSqlArea.getFont().getSize();
//        sql提示弹窗坐标X位置为:窗口的x坐标值+(TextArea原点)所在的场景的x坐标值+当前光标的X值
        double anchorX = window.getX() + point2D.getX() + caretMaxX;
//        sql提示弹窗坐标Y位置为:窗口的Y坐标值+(TextArea原点)所在的场景的Y坐标值+当前光标的Y值+字体尺寸+冗余值(避免弹窗盖住TextArea中的文字)
        double anchorY = window.getY() + point2D.getY() + caretMaxY + fontSize + 20;

        return new Point2D(anchorX, anchorY);
    }


    /**
     * 得到当前的sql内容
     * 需要考虑只执行选中部分的sql情况
     * 如一个输入框存在多条SQL,只执行选中的部分
     *
     * @param select 是否只执行选中的SQL
     * @return
     */
    public String getCurrentSql(boolean select) {
        if (select) {
//            获取选中部分的SQL
            return writeSqlArea.getSelectedText();
        }
        return writeSqlArea.getText();
    }


    /**
     * 快捷键
     */
    private EventHandler<KeyEvent> createShortcutKey() {
        EventHandler<KeyEvent> keyEventEventHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.isAltDown() && event.getCode() == KeyCode.SLASH) {
                    log.debug("TextArea 区域  Alt + / 同时按下,将触发sql提示");
//                    todo
                    return;
                }
                if (event.isControlDown() && event.getCode() == KeyCode.R) {
                    log.debug("TextArea 区域 Ctrl + Space 同时按下,将运行当前查询窗口的SQL语句");
                    String currentSql = getCurrentSql(false);
//                    todo
                    return;
                }
                if (event.isControlDown() && event.isShiftDown() && event.getCode() == KeyCode.R) {
                    log.debug("TextArea 区域 Ctrl + Space 同时按下,将运行当前查询窗口选中的SQL语句");
                    String currentSql = getCurrentSql(true);
//                    todo
                    return;
                }
                if (event.isControlDown() && event.getCode() == KeyCode.D) {
                    log.debug("TextArea 区域 Ctrl + D 同时按下,将光标所在行的内容复制到下一行,如果存在字符被选中,则只复制选中字符,到选中字符状态的末尾");
                    String selectedText = writeSqlArea.getSelectedText();
//                    选中的字符非空
                    if (StringUtils.isNotBlank(selectedText)) {
                        IndexRange selection = writeSqlArea.getSelection();
                        int endIndex = selection.getEnd();
                        writeSqlArea.insertText(endIndex, selectedText);
                        return;
                    }
//                    未选中字符的情况
                    Pair<Integer, Integer> caretPositionLineRange = getCaretPositionLineRange();
                    Integer lineStartIndex = caretPositionLineRange.getLeft();
                    Integer lineEndIndex = caretPositionLineRange.getRight();
//                    获取到了光标所在行的内容
                    String caretPositionLineContent = writeSqlArea.getText(lineStartIndex, lineEndIndex);
                    writeSqlArea.insertText(lineEndIndex, "\n" + caretPositionLineContent);

                }
            }
        };
        return keyEventEventHandler;
    }

    /**
     * 获取光标所在行的索引范围
     * 具体做法是以当前光标索引为起点,向前向后遍历
     * 只到遇到换行符或到字符结尾或字符开头
     *
     * @return
     */
    private Pair<Integer, Integer> getCaretPositionLineRange() {
//        先获取光标的索引位置
        int caretPosition = writeSqlArea.getCaretPosition();
//        向前遍历
        Integer lineStartIndex = caretPosition;
        while (lineStartIndex > 0) {
            String text = writeSqlArea.getText(lineStartIndex - 1, lineStartIndex);
            if (text.equals("\n")) {
                break;
            }
            lineStartIndex = lineStartIndex - 1;
        }
//        向后遍历
        Integer lineEndIndex = caretPosition;
        while (lineEndIndex < writeSqlArea.getText().length()) {
            String text = writeSqlArea.getText(lineEndIndex, lineEndIndex + 1);
            if (text.equals("\n")) {
                break;
            }
            lineEndIndex = lineEndIndex + 1;
        }
        log.debug("光标所在行的索引范围是:[{}-{}]", lineStartIndex, lineEndIndex);
        return Pair.of(lineStartIndex, lineEndIndex);
    }

}
