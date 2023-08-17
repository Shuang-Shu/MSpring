package com.mdc;

import com.mdc.mspring.app.config.TestConfig;
import com.mdc.mspring.app.dao.TestDao;
import com.mdc.mspring.app.dao.TestJdbcDao;
import com.mdc.mspring.app.entity.Student;
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
        ResourceResolver resolver = new ResourceResolver("com.mdc.mspring");
        PropertyResolver propertyResolver = new PropertyResolver();
        this.applicationContext = new AnnotationConfigApplicationContext(
                TestConfig.class, resolver, propertyResolver
        );
    }

    @Test
    public void testJdbc() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, SQLException {
        ResourceResolver resolver = new ResourceResolver("com.mdc");
        PropertyResolver propertyResolver = new PropertyResolver();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                TestConfig.class, resolver, propertyResolver
        );
        DataSource dataSource = (DataSource) applicationContext.getBean("dataSource");
        Connection connectionn = dataSource.getConnection();
        Statement statement = connectionn.createStatement();
        String sql = "SELECt * FROM student;";
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            System.out.println(resultSet.getString("name"));
        }
    }

    @Test
    public void testJdbct2() throws SQLException {
        JdbcTemplate jdbcTemplate = (JdbcTemplate) applicationContext.getBean("jdbcTemplate");
//        System.out.println(jdbcTemplate);
        String sql = "SELECt * FROM student;";
//        Student student = jdbcTemplate.queryForObject(sql, Student.class);
//        System.out.println(student);
//        List<Student> students = jdbcTemplate.queryForList(sql, Student.class);
//        System.out.println(students);
        int studentAge = (Integer) jdbcTemplate.queryForNumber("SELECt age FROM student WHERE name = ?;", "赵刚");
        System.out.println(studentAge);
    }

    @Test
    public void testTx() {
        TestJdbcDao testJdbcDao = (TestJdbcDao) applicationContext.getBean("testJdbcDao");
        testJdbcDao.testJdbc();
    }
}
