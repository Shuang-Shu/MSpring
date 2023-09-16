package com.mdc.mspring.context.test.property;

import com.mdc.mspring.context.factory.AnnotationConfigApplicationContext;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author ShuangShu
 * @version 1.0
 * @description: TODO
 * @date 2023/9/16 9:22
 */
public class PropertyTest {
    @SneakyThrows
    @Test
    public void basicTest() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class);
        TestBean testBean = context.getBean(TestBean.class);
        Assert.assertNotNull(testBean);
        Assert.assertEquals("shuangshu", testBean.name);
        Assert.assertEquals(18, testBean.age);
    }
}
