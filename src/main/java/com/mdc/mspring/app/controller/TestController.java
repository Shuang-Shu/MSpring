package com.mdc.mspring.app.controller;

import com.mdc.mspring.anno.ioc.*;
import com.mdc.mspring.app.config.TestConfig;
import com.mdc.mspring.app.dao.TestDao;
import com.mdc.mspring.app.service.TestService;

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
    public TestController(@Autowired TestDao testDao) {
//        System.out.println(testDao);
        this.testDao = testDao;
    }

    @Autowired
    public void setTestDao(@Autowired TestService service) {
        this.testService = service;
    }

    private TestDao testDao;
    private TestService testService;

    @Autowired
    private TestConfig testConfig;

    @PostConstruct
    public void testInit() {
        System.out.println("init");
    }

    @BeforeDestroy
    public void testDestroy() {
        System.out.println("destroy");
    }
}
