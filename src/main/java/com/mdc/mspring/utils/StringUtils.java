package com.mdc.mspring.utils;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/11/19:11
 * @Description:
 */
public class StringUtils {
    public static String simpleName(String name) {
        int index = name.lastIndexOf(".");
        return setHeadLower(index == -1 ? name : name.substring(index + 1));
    }

    public static boolean isEmptyString(String str) {
        return str == null || str.isEmpty();
    }

    public static String setHeadLower(String name) {
        char[] cs = name.toCharArray();
        cs[0] = Character.toLowerCase(cs[0]);
        return new String(cs);
    }

    public static String getLowerCase(String str) {
        char[] cs = str.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            cs[i] = Character.toLowerCase(cs[i]);
        }
        return new String(cs);
    }
}
