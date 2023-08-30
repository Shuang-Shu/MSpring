package com.mdc.mspring.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import com.mdc.mspring.app.config.TestConfig;
import com.mdc.mspring.app.entity.Student;
import com.mdc.mspring.context.factory.impl.AnnotationConfigApplicationContext;
import com.mdc.mspring.context.resolver.PropertyResolver;
import com.mdc.mspring.context.resolver.ResourceResolver;
import com.mdc.mspring.jdbc.template.JdbcTemplate;

public class App {
    public static void main(String[] args) throws IOException, URISyntaxException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        PropertyResolver propertyResolver = new PropertyResolver();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                TestConfig.class, resolver, propertyResolver);
        JdbcTemplate jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
        String sql = "SELECT * FROM student;";
        System.out.println(jdbcTemplate.queryForList(sql, Student.class, args));
    }
}
