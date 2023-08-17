package com.mdc;

import com.mdc.mspring.app.bean.TestImportBean;
import com.mdc.mspring.app.bean.TestTopBean;
import com.mdc.mspring.app.controller.TestController2;
import com.mdc.mspring.app.service.TestService;
import com.mdc.mspring.context.anno.ComponentScan;
import com.mdc.mspring.app.config.TestConfig;
import com.mdc.mspring.app.controller.TestController;
import com.mdc.mspring.app.dao.TestDao;
import com.mdc.mspring.context.factory.impl.AnnotationConfigApplicationContext;
import com.mdc.mspring.context.entity.Resource;
import com.mdc.mspring.context.resolver.PropertyResolver;
import com.mdc.mspring.context.resolver.ResourceResolver;
import com.mdc.mspring.utils.ClassUtils;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/10/16:37
 * @Description:
 */
public class MSpringTest {
    @Test
    public void test() throws IOException {
        System.out.println("hello world");
    }

    @Test
    public void testResourceResolver() throws IOException, URISyntaxException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        System.out.println(resolver.scanClass(Resource::name));
    }

    @Test
    public void testAddAllNull() throws IOException, URISyntaxException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        System.out.println(resolver.scanClass(Resource::path));
    }

    @Test
    public void testFindOtherResource() throws IOException, URISyntaxException {
        ResourceResolver resolver = new ResourceResolver("");
        Set<String> suffixSet = new HashSet<>();
        suffixSet.add(".properties");
        suffixSet.add(".yaml");
        System.out.println(resolver.scan("", Resource::name, suffixSet, 2));
    }

    @Test
    public void testPropertyResolver() throws IOException, URISyntaxException {
        PropertyResolver propertyResolver = new PropertyResolver();
        System.out.println(propertyResolver.getProperty("${spring.mysql.url1:${key:1234}}"));
        System.out.println(propertyResolver.getProperty("${spring.mysql.url1:${key1:1234}}"));
        System.out.println(propertyResolver.getProperty("${spring.mysql.url:${key:1234}}"));
    }

    @Test
    public void testAnnoUtil() {
        ComponentScan annotation = (ComponentScan) ClassUtils.getAnnotation(TestConfig.class, ComponentScan.class, new HashSet<>());
        assert annotation != null;
        System.out.println(annotation.value());
    }

    @Test
    public void testApplicationContext() throws IOException, URISyntaxException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        PropertyResolver propertyResolver = new PropertyResolver();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                TestConfig.class, resolver, propertyResolver
        );
    }

    @Test
    public void testGetAnnotation() throws IOException, URISyntaxException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        PropertyResolver propertyResolver = new PropertyResolver();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                TestConfig.class, resolver, propertyResolver
        );
    }

    @Test
    public void testInstantiate() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        PropertyResolver propertyResolver = new PropertyResolver();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                TestConfig.class, resolver, propertyResolver
        );
        TestTopBean testTopBean = applicationContext.getBean(TestTopBean.class);
        testTopBean.testEnhanced();
        testTopBean.testNotEnhanced();
    }

    @Test
    public void testImportAnno() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        PropertyResolver propertyResolver = new PropertyResolver();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                TestConfig.class, resolver, propertyResolver
        );
        TestImportBean testImportBean = applicationContext.getBean(TestImportBean.class);
        System.out.println(testImportBean);
    }
}
