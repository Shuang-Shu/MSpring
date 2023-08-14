package com.mdc.mspring.app.bean;

import com.mdc.mspring.aop.anno.Around;
import com.mdc.mspring.app.anno.Enhanced;
import com.mdc.mspring.context.anno.Component;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/16:41
 * @Description:
 */
@Component
@Around("testHandler")
public class TestTopBean {
    @Enhanced
    public void testEnhanced() {
        System.out.println("origin bean test");
    }

    public void testNotEnhanced() {
        System.out.println("origin bean test");
    }
}
