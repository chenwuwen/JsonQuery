package com.kanyun.ui.event;

import com.sun.javafx.event.EventUtil;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 事件传递桥接服务,用来方便查找事件对应关系
 * 如果A类发射一个事件,且该事件的消费者也是A类,则无需调用本类
 * 因为此场景发射事件不必指定EventTarget,因此也不需要getScene().lookup("#xxx")
 */
public class UserEventBridgeService {

    private static final Logger log = LoggerFactory.getLogger(UserEventBridgeService.class);


    /**
     * 场景,在JavaFx中scene是仅次于窗口(stage)的结构。是次顶级结构。一个stage包含多个scene
     * 这里使用scene是用来使用其lookup()方法来获取指定Id的javaFx结构组件,得到了指定的javaFx
     * 组件,可以以它为EventTarget,用来发射自定义事件,从而实现解耦,但是当scene切换时,有些组件可能
     * 找不到。
     */
    private static Scene scene;

    /**
     * 当Scene切换时需要调用此方法
     *
     * @param scene
     */
    public static void setScene(Scene scene) {
        UserEventBridgeService.scene = scene;
    }

    /**
     * 事件发射到ContentPane组件
     *
     * @param event
     */
    public static void bridgeUserEvent2ContentPane(Event event) {
        log.debug("事件桥接服务,接收到[{}]类型的事件,该事件由ContentPane.class监听", event.getEventType().getName());
        Node eventTarget = scene.lookup("#ContentPane");
        EventUtil.fireEvent(eventTarget, event);
    }

    /**
     * 事件发射到DataBasePane组件
     *
     * @param event
     */
    public static void bridgeUserEvent2DataBasePane(Event event) {
        log.debug("事件桥接服务,接收到[{}]类型的事件,该事件由DataBasePane.class监听", event.getEventType().getName());
        Node eventTarget = scene.lookup("#DataBasePane");
        EventUtil.fireEvent(eventTarget, event);
    }


    /**
     * 事件发射到BottomInfoPane组件
     *
     * @param event
     */
    public static void bridgeUserEvent2BottomInfoPane(Event event) {
        log.debug("事件桥接服务,接收到[{}]类型的事件,该事件由BottomInfoPane.class监听", event.getEventType().getName());
        Node eventTarget = scene.lookup("#BottomInfoPane");
        EventUtil.fireEvent(eventTarget, event);
    }

    /**
     * 事件发射到BottomInfoPane组件
     *
     * @param event
     */
    public static void bridgeUserEvent2SqlComponent(Event event) {
        log.debug("事件桥接服务,接收到[{}]类型的事件,该事件由SqlComponent.class监听", event.getEventType().getName());
        Node eventTarget = scene.lookup("#SqlComponent");
        EventUtil.fireEvent(eventTarget, event);
    }

    /**
     * 事件发射到BottomInfoPane组件
     *
     * @param event
     */
    public static void bridgeUserEvent2TabObjectsPane(Event event) {
        log.debug("事件桥接服务,接收到[{}]类型的事件,该事件由TabObjectsPane.class监听", event.getEventType().getName());
        Node eventTarget = scene.lookup("#TabObjectsPane");
        EventUtil.fireEvent(eventTarget, event);
    }

    /**
     * 事件发射到FunctionDialog组件
     *
     * @param event
     */
    public static void bridgeUserEvent2FunctionDialog(Event event) {
        log.debug("事件桥接服务,接收到[{}]类型的事件,该事件由FunctionDialog.class监听", event.getEventType().getName());
        Node eventTarget = scene.lookup("#FunctionDialog");
        EventUtil.fireEvent(eventTarget, event);
    }
}
