package com.mdc.mspring.aop.test;

import com.mdc.mspring.aop.test.bean.TestBean;
import com.mdc.mspring.context.factory.AnnotationConfigApplicationContext;
import com.mdc.mspring.context.factory.support.BeanFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/16 23:35
 */
public class AopTest {
    @Test
    public void basicTest() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        BeanFactory beanFactory = new AnnotationConfigApplicationContext(TestConfig.class);
        var testBean = beanFactory.getBean("testBean");
        Assert.assertNotNull(testBean);
        Assert.assertEquals(true, ((TestBean) testBean).test().startsWith("pilote"));
    }
}
