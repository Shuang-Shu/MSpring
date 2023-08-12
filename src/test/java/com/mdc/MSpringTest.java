package com.mdc;

import com.mdc.mspring.anno.ioc.ComponentScan;
import com.mdc.mspring.app.config.TestConfig;
import com.mdc.mspring.context.impl.AnnotationConfigApplicationContext;
import com.mdc.mspring.entity.Resource;
import com.mdc.mspring.resolver.ioc.PropertyResolver;
import com.mdc.mspring.resolver.ioc.ResourceResolver;
import com.mdc.mspring.utils.ClassUtils;
import org.junit.Test;

import javax.print.attribute.HashDocAttributeSet;
import java.io.IOException;
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
    public void testApplicationContext() throws IOException, URISyntaxException, NoSuchMethodException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        PropertyResolver propertyResolver = new PropertyResolver();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                TestConfig.class, resolver, propertyResolver
        );
    }

    @Test
    public void testGetAnnotation() throws IOException, URISyntaxException, NoSuchMethodException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        PropertyResolver propertyResolver = new PropertyResolver();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                TestConfig.class, resolver, propertyResolver
        );
    }
}
