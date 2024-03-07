package com.kanyun.common;

import java.util.concurrent.*;

/**
 * 消息管道,用来传递一些消息
 */
public class MessageChannel {

    /**
     * 错误信息消息队列
     */
    private static BlockingQueue<String> errorMessageQueue = new LinkedBlockingQueue<>();


    public static void putErrorMessage(String error) {
        try {
            errorMessageQueue.put(error);
        } catch (Exception e) {

        }
    }

}
