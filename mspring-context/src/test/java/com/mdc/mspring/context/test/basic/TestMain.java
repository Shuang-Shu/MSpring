package com.mdc.mspring.context.test.basic;

import com.mdc.mspring.context.factory.AnnotationConfigApplicationContext;
import com.mdc.mspring.context.test.basic.beans.TestBean;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class TestMain {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, URISyntaxException {
        AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext(
                TestConfig.class);
        System.out.println(configApplicationContext);
        System.out.println(configApplicationContext.getBean(TestBean.class));
    }
}
