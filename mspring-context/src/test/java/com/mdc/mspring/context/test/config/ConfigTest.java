package com.mdc.mspring.context.test.config;

import com.mdc.mspring.context.factory.AnnotationConfigApplicationContext;
import com.mdc.mspring.context.factory.support.BeanFactory;
import com.mdc.mspring.context.test.config.bean.Bean1;
import com.mdc.mspring.context.test.config.bean2.Bean2;
import com.mdc.mspring.context.test.config.bean3.Bean3;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/16 15:14
 */
public class ConfigTest {
    @Test
    public void basicTest() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        BeanFactory beanFactory = new AnnotationConfigApplicationContext(TestConfig1.class);
        Bean1 bean1 = (Bean1) beanFactory.getBean("bean1");
        Bean2 bean2 = (Bean2) beanFactory.getBean("bean2");
        Bean3 bean3 = (Bean3) beanFactory.getBean("bean3");

        Assert.assertNotNull(bean1);
        Assert.assertNotNull(bean2);
        Assert.assertNotNull(bean3);

        Assert.assertEquals("bean1.test", bean1.name);
        Assert.assertEquals("bean2.test", bean2.name);
        Assert.assertEquals("bean3.test", bean3.name);
    }
}
