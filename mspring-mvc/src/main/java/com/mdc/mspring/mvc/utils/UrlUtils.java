package com.mdc.mspring.mvc.utils;

public class UrlUtils {
    public static String getRelativeUrl(String url) {
        // 获取相对URL，例如http://localhost/hello/Bob返回/hello/Bob
        int idx = url.indexOf("/", url.indexOf("//") + 2);
        return url.substring(idx);
    }
}
