package com.mdc.mspring.context.test.property;

import com.mdc.mspring.context.annotation.Component;
import com.mdc.mspring.context.annotation.Value;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/16 9:21
 */
@Component
public class TestBean {
    @Value("user")
    String name;
    @Value("age")
    int age;

    @Override
    public String toString() {
        return "name: " + name + ", age: " + age;
    }
}
