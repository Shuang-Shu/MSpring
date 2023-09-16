package com.mdc.mspring.aop.test.bean;

import com.mdc.mspring.aop.annotation.Around;
import com.mdc.mspring.aop.annotation.Enhanced;
import com.mdc.mspring.context.annotation.Component;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/16 23:24
 */
@Around("testAopHandler")
@Component("testBean")
public class TestBean {
    @Enhanced
    public String test() {
        return "hello!";
    }
}
