package com.mdc.mspring.app.handler;

import com.mdc.mspring.context.anno.Component;

import java.beans.ConstructorProperties;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/14:40
 * @Description:
 */
@Component
public class TestHandler implements InvocationHandler {
    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        System.out.println("TestHandler invoke before");
        Object ret = method.invoke(o, objects);
        System.out.println("TestHandler invoke after");
        return ret;
    }
}
