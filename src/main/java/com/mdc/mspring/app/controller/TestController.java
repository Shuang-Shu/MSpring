package com.mdc.mspring.app.controller;

import com.mdc.mspring.anno.ioc.*;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/12/11:19
 * @Description:
 */
@Primary(value = true)
@Order(10)
@Controller
public class TestController {
    @PostConstruct
    public void testInit() {
        System.out.println("init");
    }

    @BeforeDestroy
    public void testDestroy() {
        System.out.println("destroy");
    }
}
