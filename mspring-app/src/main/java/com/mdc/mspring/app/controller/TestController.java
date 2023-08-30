package com.mdc.mspring.app.controller;

import com.mdc.mspring.app.service.TestService;
import com.mdc.mspring.context.anno.*;

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
    public TestController(@Autowired TestService testService) {
//        System.out.println(testDao);
        this.testService = testService;
    }

    private TestService testService;

    public void test() {
        testService.service();
    }

    @PostConstruct
    public void testInit() {
        System.out.println("init");
    }

    @BeforeDestroy
    public void testDestroy() {
        System.out.println("destroy");
    }
}
