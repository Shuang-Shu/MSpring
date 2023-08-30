package com.mdc.mspring.app.resolver;

import com.mdc.mspring.app.anno.Enhanced;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/10:03
 * @Description:
 */
public class EnhancedHandler implements InvocationHandler {
    @Override
    public Object invoke(Object bean, Method method, Object[] args) throws Throwable {
        if (method.getAnnotation(Enhanced.class) != null) {
            System.out.println("EnhancedHandler invoke before");
            String ret = (String) method.invoke(bean, args);
            System.out.println("EnhancedHandler invoke after");
            return ret;
        }
        return method.invoke(bean, args);
    }
}
