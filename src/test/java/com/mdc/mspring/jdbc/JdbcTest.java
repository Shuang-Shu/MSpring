package com.mdc.mspring.jdbc;

import com.mdc.mspring.context.factory.impl.AnnotationConfigApplicationContext;
import com.mdc.mspring.context.resolver.PropertyResolver;
import com.mdc.mspring.context.resolver.ResourceResolver;
import com.mdc.mspring.jdbc.template.JdbcTemplate;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/14/17:38
 * @Description:
 */
public class JdbcTest {
    private AnnotationConfigApplicationContext applicationContext;

    @Before
    public void initContext() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    }

    @Test
    public void testJdbc() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, SQLException {
    }

    @Test
    public void testJdbct2() throws SQLException {
    }

    @Test
    public void testTx() {
    }
}
