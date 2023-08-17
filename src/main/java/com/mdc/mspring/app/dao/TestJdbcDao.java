package com.mdc.mspring.app.dao;

import com.mdc.mspring.app.anno.Enhanced;
import com.mdc.mspring.context.anno.Autowired;
import com.mdc.mspring.context.anno.Component;
import com.mdc.mspring.jdbc.anno.Transactional;
import com.mdc.mspring.jdbc.template.JdbcTemplate;

/**
 * @Author: ShuangShu
 * @Email: 1103725164@qq.com
 * @Date: 2023/08/17/9:26
 * @Description:
 */
@Component
@Transactional
public class TestJdbcDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Enhanced
    public void testJdbc() {
        System.out.println("dao 2");
    }
}
