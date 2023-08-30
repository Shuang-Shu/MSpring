package com.mdc.mspring.app.dao;

import com.mdc.mspring.app.anno.Enhanced;
import com.mdc.mspring.context.anno.Autowired;
import com.mdc.mspring.context.anno.Component;
import com.mdc.mspring.context.anno.Value;
import com.mdc.mspring.jdbc.anno.Transactional;
import com.mdc.mspring.jdbc.template.JdbcTemplate;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/12/18:14
 * @Description:
 */
@Component
@Transactional
public class TestDao {
    @Value("username")
    public String username;
    @Value("password")
    public String passwd;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void test() {
        System.out.println("dao 2");
    }

    @Enhanced
    public void testTranscational() {
        System.out.println("dao 2");
    }
}
