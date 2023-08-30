package com.mdc.mspring.app.service;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/13/17:01
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TestServicePorxy extends TestService {
    private TestService testService;

    public TestServicePorxy(TestService testService) {
        this.testService = testService;
    }

    @Override
    public void service() {
        System.out.println("proxy before");
        testService.service();
        System.out.println("proxy after");
    }
}
