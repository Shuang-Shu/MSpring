package com.mdc.mspring.aop.resolver;

import com.mdc.mspring.aop.anno.Enhanced;
import com.mdc.mspring.context.anno.Component;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/9:44
 * @Description:
 */
@Component
public class ProxyResolver {
    ByteBuddy byteBuddy = new ByteBuddy();

    public <T> T createProxy(T bean, InvocationHandler handler) {
        Class<T> beanClazz = (Class<T>) bean.getClass();
        Class<?> proxyClass = byteBuddy
                .subclass(beanClazz, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                .method(ElementMatchers.isAnnotatedWith(Enhanced.class))
                .intercept(
                        InvocationHandlerAdapter.of(
                                new InvocationHandler() {
                                    @Override
                                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                        return handler.invoke(bean, method, args); // transfer `invoke` to origin bean
                                    }
                                }
                        )
                ).make() // generate byte code
                .load(beanClazz.getClassLoader())
                .getLoaded();
        // generate bean instance
        Object proxy = null;
        try {
            proxy = proxyClass.getConstructors()[0].newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T) proxy;
    }
}
