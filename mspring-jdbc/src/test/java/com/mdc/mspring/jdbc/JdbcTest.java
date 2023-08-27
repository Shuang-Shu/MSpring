package com.mdc.mspring.jdbc;

import com.mdc.mspring.context.factory.impl.AnnotationConfigApplicationContext;
import com.mdc.mspring.jdbc.config.JdbcConfiguration;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class JdbcTest {
    AnnotationConfigApplicationContext context;

    @Before
    public void initTest() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        context = new AnnotationConfigApplicationContext(JdbcConfiguration.class);
    }

    @Test
    public void testBasic() {
        System.out.println(context.getBean(DataSource.class));
    }
}
