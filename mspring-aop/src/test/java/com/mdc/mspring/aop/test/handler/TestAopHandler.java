package com.mdc.mspring.aop.test.handler;

import com.mdc.mspring.context.annotation.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/16 23:24
 */
@Component
public class TestAopHandler implements InvocationHandler {
    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        String result = method.invoke(o, objects).toString();
        result = "pilote " + result;
        return result;
    }
}
