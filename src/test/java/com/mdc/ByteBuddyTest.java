package com.mdc;

import com.mdc.mspring.aop.resolver.ProxyResolver;
import com.mdc.mspring.app.bean.TestBean;
import com.mdc.mspring.app.bean.TestTopBean;
import com.mdc.mspring.app.config.TestConfig;
import com.mdc.mspring.app.resolver.EnhancedHandler;
import com.mdc.mspring.context.factory.impl.AnnotationConfigApplicationContext;
import com.mdc.mspring.context.resolver.PropertyResolver;
import com.mdc.mspring.context.resolver.ResourceResolver;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/9:48
 * @Description:
 */
public class ByteBuddyTest {
    @Test
    public void testHelloWorld() throws InstantiationException, IllegalAccessException {
        Class<?> dynamicType = new ByteBuddy()
                .subclass(Object.class)
                .method(ElementMatchers.named("toString"))
                .intercept(FixedValue.value("Hello World!"))
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

//        assertThat(dynamicType.newInstance().toString(), is("Hello World!"));
        System.out.println(dynamicType.newInstance().toString());
    }

    @Test
    public void testProxyResolver() {
        TestBean testBean = new TestBean();
        TestBean proxy = new ProxyResolver().createProxy(testBean, new EnhancedHandler());
    }

    @Test
    public void testEnhancingMethod() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        PropertyResolver propertyResolver = new PropertyResolver();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                TestConfig.class, resolver, propertyResolver
        );
        TestTopBean testTopBean = applicationContext.getBean(TestTopBean.class);
        testTopBean.testEnhanced();
        System.out.println("====================================");
        testTopBean.testNotEnhanced();
        TestBean testBean1 = applicationContext.getBean(TestBean.class);
        System.out.println("good");
    }
}
