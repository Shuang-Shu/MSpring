package com.mdc.mspring.app.bean;

import com.mdc.mspring.aop.anno.Around;
import com.mdc.mspring.app.anno.Enhanced;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/12/13:18
 * @Description:
 */
@Around("testHandler")
public class TestBean {
    public void testBeanInit() {
        System.out.println("testBean init");
    }

    public void testBeanDestroy() {
        System.out.println("testBean destroy");
    }

    @Enhanced
    public void testEnhanced() {
        System.out.println("origin bean test");
    }

    public void testNotEnhanced() {
        System.out.println("origin bean test");
    }
}
