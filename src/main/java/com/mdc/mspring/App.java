package com.mdc.mspring;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/10/15:32
 * @Description:
 */
public class App {
    public static void main(String[] args) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources("com/mdc/mspring/resolver");
        System.out.println(classLoader.getResources("com/mdc/mspring/resolver"));
    }
}
