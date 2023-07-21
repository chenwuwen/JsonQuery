package com.kanyun.func.string;

import com.google.gson.JsonParser;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.util.Stack;
import java.util.UUID;

public class StringFuncUtil {

    /**
     * 字符串反转
     *
     * @param s
     * @return
     */
    public static String reverse(String s) {
        char[] str = s.toCharArray();
        Stack<Character> stack = new Stack<>();
        for (char aStr1 : str) {
            stack.push(aStr1);
        }
        StringBuilder reversed = new StringBuilder();
        while (!stack.isEmpty()) {
            reversed.append(stack.pop());
        }
        return reversed.toString();
    }

    /**
     * 获取16位大写的随机UUID，且去掉中线
     *
     * @return
     */
    public static String genUUIDMini16() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "").substring(16).toUpperCase();
    }

    /**
     * 将目标字符串按照base64算法加密
     *
     * @param value
     * @return
     */
    public static String base64(String value) throws UnsupportedEncodingException {
        if (value == null) {
            value = "";
        }
        String base64Code = null;
        base64Code = new BASE64Encoder().encode(value.getBytes("utf-8"));
        return base64Code;
    }

    /**
     * 从json串获取对应字段的值
     * @param fieldName
     * @param jsonString
     * @return
     */
    public String getFiledFromJson(String fieldName,
                                   String jsonString) {

        if (JsonParser.parseString(jsonString).isJsonObject()) {
            return JsonParser.parseString(jsonString).getAsJsonObject().get(fieldName).getAsString();
        }
        return "";
    }
}
