package com.kanyun.sql.util;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Number 类型帮助类
 * Java Number包括以下6种类型：
 * Byte类型 Short类型  Integer类型 Long类型 Float类型  Double类型
 * http://www.51gjie.com/java/183.html
 */
public class NumberTypeHelper {

    public static boolean isInteger(Number number) {
//        检查是否是纯数字
        if (!NumberUtils.isDigits(number.toString())) {
            return false;
        }
        try {

//            number.intValue()不会报错,如果数值超出精度,会返回一个不一样的值,因此判断number.intValue()方法是否执行成功,还是将转换前后的数字做下比对吧
            int c = number.intValue();
            return String.valueOf(c).equals(number.toString());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDouble(Number number) {
        if (!NumberUtils.isDigits(number.toString())) {
            try {
                double d = number.doubleValue();
                return String.valueOf(d).equals(number.toString());
            } catch (Exception e) {
                return false;
            }

        }
        return false;
    }

    public static boolean isLong(Number number) {
        if (!NumberUtils.isDigits(number.toString())) {
            return false;
        }
        try {
            long c = number.longValue();
            return String.valueOf(c).equals(number.toString());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isFloat(Number number) {
        if (!NumberUtils.isDigits(number.toString())) {
            try {
                float f = number.floatValue();
                return String.valueOf(f).equals(number.toString());
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}
