package com.mdc.mspring.jdbc;

import com.mdc.mspring.context.factory.impl.AnnotationConfigApplicationContext;
import com.mdc.mspring.jdbc.config.JdbcConfiguration;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class App {
    public static void main(String[] args) throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                JdbcConfiguration.class
        );
        DataSource dataSource = context.getBean(DataSource.class);
        System.out.println(dataSource);
    }
}
