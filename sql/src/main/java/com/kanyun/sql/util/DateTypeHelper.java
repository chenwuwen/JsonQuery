package com.kanyun.sql.util;


import java.util.Date;

public class DateTypeHelper {

    /**
     * 时间戳转换日期
     * @param timeStamp
     * @return
     */
    public static Date getDateFromTimeStamp(Long timeStamp) {
        int length = String.valueOf(timeStamp).length();
        if (length == 10) {
//            对应时间戳为秒的情况
            timeStamp = timeStamp * 1000;
        }
        return new Date(timeStamp);
    }
}
