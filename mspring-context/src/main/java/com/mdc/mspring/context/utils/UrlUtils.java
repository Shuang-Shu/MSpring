package com.mdc.mspring.context.utils;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/15 17:15
 */
public class UrlUtils {
    public static String convertToClassName(String urlStr, String basePackage) {
        basePackage = basePackage.replace(".", "/");
        int idx = urlStr.indexOf(basePackage);
        if (idx != -1) {
            urlStr = urlStr.substring(idx);
        }
        return urlStr.substring(0, urlStr.lastIndexOf(".")).replace("/", ".");
    }
}
